package org.tron.easywork.handler.collection;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.AccountInfo;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;

/**
 * 资金归集
 * <p>
 * 归集指定 trc20合约余额、trx余额 到目标地址
 *
 * @author Admin
 * @version 1.0
 * @time 2022-11-08 15:57
 */
@Slf4j
public class FundCollection {

    /**
     * 归集配置
     */
    private final FundCollectionConfig fundCollectionConfig;

    /**
     * Trx转账处理器
     */
    private final TrxTransferHandler trxTransferHandler;

    /**
     * Trc20转账处理器
     */
    private final Trc20TransferHandler trc20TransferHandler;

    /**
     * ApiWrapper
     */
    private final ApiWrapper wrapper;


    public FundCollection(FundCollectionConfig fundCollectionConfig, TrxTransferHandler trxTransferHandler, Trc20TransferHandler trc20TransferHandler, ApiWrapper wrapper) {
        this.fundCollectionConfig = fundCollectionConfig;
        this.trxTransferHandler = trxTransferHandler;
        this.trc20TransferHandler = trc20TransferHandler;
        this.wrapper = wrapper;
    }

    /**
     * 资金归集 - 归集 trc20 trx
     *
     * @param privateKey     小号私钥 - 转出账户
     * @param referenceBlock 引用区块 - 用于本地构造交易参数
     * @throws InterruptedException 延迟阻塞异常|Sleep睡眠异常
     */
    public void collection(@NonNull String privateKey, @NonNull ReferenceBlock referenceBlock) throws InterruptedException {
        if (privateKey.trim().length() != 64) {
            throw new RuntimeException("错误私钥:" + privateKey);
        }
        KeyPair keyPair;
        try {
            keyPair = new KeyPair(privateKey.trim());
        } catch (Exception e) {
            throw new RuntimeException("错误私钥:" + privateKey, e);
        }

        // 账户信息
        AccountInfo account = new AccountInfo(keyPair);

        // 过滤本地账户 - 不处理归集目标地址 - 不能自己给自己转账
        if (account.getBase58CheckAddress().equals(fundCollectionConfig.getTargetAddressOfTrc20()) ||
                account.getBase58CheckAddress().equals(fundCollectionConfig.getTargetAddressOfTrx()) ||
                account.getBase58CheckAddress().equals(fundCollectionConfig.getHandingFeeProviderAddress())) {
            log.debug("本地账户，不予处理");
            return;
        }

        log.debug("开始处理账户：{}", account.getBase58CheckAddress());

        // 查询Trc20余额
        BigDecimal balance = Trc20ContractUtil.trc20BalanceOf(fundCollectionConfig.getTrc20ContractInfo().getAddress(), account.getBase58CheckAddress(), wrapper);
        log.debug("Trc20余额：{}", fundCollectionConfig.getTrc20ContractInfo().getRealAmount(balance).stripTrailingZeros().toPlainString());

        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("无需归集trc20");
        } else {
            try {
                // 派发Trx矿工费 - 无需单独检查矿工费交易ID，如果失败会直接报错
                // trx转账信息
                Transfer handingFeeTransfer = Transfer.trxTransferBuilder(
                        fundCollectionConfig.getHandingFeeProviderAddress(),
                        account.getBase58CheckAddress(),
                        Convert.toSun(fundCollectionConfig.getHandingFeeWithTrx(), Convert.Unit.TRX)
                ).build();
                // 构建交易
                Chain.Transaction handingFeeTransaction = trxTransferHandler.buildLocalTransfer(handingFeeTransfer, referenceBlock);
                // 签名
                Chain.Transaction signTransaction = null;
                for (String key : fundCollectionConfig.getHandingFeeProviderKeys()) {
                    signTransaction = wrapper.signTransaction(handingFeeTransaction, new KeyPair(key));
                }
                // 广播
                String tid = wrapper.broadcastTransaction(signTransaction);
                log.debug("矿工费派发成功：{}", tid);
            } catch (Exception e) {
                log.error("矿工费派发失败：{}，原因：{}", account.getBase58CheckAddress(), e.getMessage());
                e.printStackTrace();
                return;
            }

            // 防止节点反应不过来 - 必须
            Thread.sleep(1000);

            // trc20转账信息
            Transfer transfer = Transfer.trc20TransferBuilder(account.getBase58CheckAddress(),
                            fundCollectionConfig.getTargetAddressOfTrc20(),
                            balance, fundCollectionConfig.getTrc20ContractInfo().getAddress())
                    .build();
            // 构建交易
            Chain.Transaction transaction = trc20TransferHandler.buildLocalTransfer(transfer, referenceBlock);
            // 签名
            Chain.Transaction signTransaction = wrapper.signTransaction(transaction, account.getKeyPair());
            // 广播交易
            String tid = wrapper.broadcastTransaction(signTransaction);

            // 防止节点反应不过来 - 必须
            Thread.sleep(1000);

            // 检查交易 - 需要单独检查trc20交易 - 失败不会报错
            try {
                Chain.Transaction checkTransaction = wrapper.getTransactionById(tid);
                boolean status = checkTransaction.getRet(0).getContractRet().getNumber() == 1;
                if (!status) {
                    log.error("trc20转目标地址失败:{},交易ID：{}", account.getBase58CheckAddress(), tid);
                    return;
                }
                log.debug("trc20转目标地址成功：{}", tid);
            } catch (IllegalException e) {
                log.error("trc20转目标地址失败:{},交易ID：{}", account.getBase58CheckAddress(), tid);
                e.printStackTrace();
                return;
            }
            log.debug("归集trc20成功");


            // 防止节点反应不过来 - 必须
            Thread.sleep(1000);
        }


        // 查询账户 trx 余额、带宽数量
        Response.Account target = wrapper.getAccount(account.getBase58CheckAddress());
        // trx 余额
        long trxBalance = target.getBalance();
        log.debug("trx余额:{}", Convert.fromSun(BigDecimal.valueOf(trxBalance), Convert.Unit.TRX));
        // 带宽费
        if (trxBalance > 1000000) {
            // 剩余带宽
            long net = 1500 - target.getFreeNetUsage();
            log.debug("剩余带宽：{}", net);
            // 需要带宽费
            if (net < 300) {
                log.debug("带宽不足，需要支付带宽费用:0.3trx");
                trxBalance = trxBalance - 300000;
            }

            // trx转账信息
            Transfer incomeTransferInfo = Transfer.trxTransferBuilder(account.getBase58CheckAddress(),
                    fundCollectionConfig.getTargetAddressOfTrx(), BigDecimal.valueOf(trxBalance)).build();
            // 构造trx转账交易
            Chain.Transaction incomeTransfer = trxTransferHandler.buildLocalTransfer(incomeTransferInfo, referenceBlock);
            // 签名
            Chain.Transaction signTransactionIncome = wrapper.signTransaction(incomeTransfer, account.getKeyPair());
            // 广播
            String incomeTid = wrapper.broadcastTransaction(signTransactionIncome);
            log.debug("trx归集成功:{}", incomeTid);
        } else {
            log.debug("trx余额较低，无需归集");
        }

    }

}
