package org.tron.easywork;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.handler.contract.TransferContractHandler;
import org.tron.easywork.handler.contract.TriggerSmartContractHandler;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-11-02 17:47
 */
@Slf4j
public class TransferTest extends BaseTest {

    /**
     * trx 转账 - 本地构造交易
     */
    @Test
    public void transferTrxLocal() throws IllegalException {
        // 引用区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 转账金额 单位 sun
        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        // 构造转账交易
        TransferInfo transferInfo = new TransferInfo(fromAccount.getBase58CheckAddress(), toAddress, amount);
        // 备注
        transferInfo.setMemo("一个备注");
        // trx 转账处理器
        TrxTransferHandler handler = new TrxTransferHandler();
        // 转账并返回交易ID
        Chain.Transaction transaction = handler.buildLocalTransfer(transferInfo, nowBlock.getBlockHeader());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction, fromAccount.getKeyPair());
        // 广播
        String id = wrapper.broadcastTransaction(signTransaction);
        log.debug(id);
    }

    /**
     * trc20 转账- 本地构造交易
     */
    @Test
    public void transferTrc20Local() throws IllegalException {
        // 引用区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 金额
        BigDecimal amount = BigDecimal.valueOf(0.1);
        for (int i = 0; i < 1; i++) {
            // trc20 合约信息
            Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(testContractAddress, wrapper);
            // 获取系统转账金额
            BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(amount);
            // 转账交易信息
            Trc20TransferInfo trc20TransferInfo = new Trc20TransferInfo(
                    fromAccount.getBase58CheckAddress(),
                    toAddress,
                    transferAmount,
                    trc20ContractInfo.getAddress()
            );
            // 备注
            trc20TransferInfo.setMemo("备注：trc20 转账");
            // trc20 转账处理器
            Trc20TransferHandler handler = new Trc20TransferHandler();
            // 转账并返回交易ID
            Chain.Transaction transaction = handler.buildLocalTransfer(trc20TransferInfo, nowBlock.getBlockHeader());
            // 签名
            Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
            // 广播
            String id = wrapper.broadcastTransaction(signTransaction);
            log.debug(id);
        }
    }


    /**
     * trc20 转账 - trident原生 - 非本地构造交易
     */
    @Test
    public void transferTrc20_trident() {
        // 真实金额 - 单位个
        long realAmount = 11;

        // 根据合约地址获取合约信息
        org.tron.trident.core.contract.Contract contract = wrapper.getContract(testContractAddress);
        // 构造trc20合约
        Trc20Contract trc20Contract = new Trc20Contract(contract, fromAccount.getBase58CheckAddress(), wrapper);

        // trc20 合约转账
        String tid = trc20Contract.transfer(
                toAddress,
                realAmount,
                // 精度
                trc20Contract.decimals().intValue(),
                "备注",
                Convert.toSun("10", Convert.Unit.TRX).longValue()
        );
        log.debug(tid);
    }

    /**
     * trx 转账 - trident原生 - 非本地构造交易
     */
    @Test
    public void transferTrx_trident() throws IllegalException {
        // trx 个数
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // sun 个数
        BigDecimal sun = Convert.toSun(realAmount, Convert.Unit.TRX);

        // 远程构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(fromAccount.getBase58CheckAddress(), toAddress, sun.longValue());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transfer);
        // 广播
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
    }


    /**
     * trx 转账 - 非本地构造交易 - 过时的
     */
    @Test
    public void transferTrx() throws IllegalException {
        // 转账金额 单位 sum
        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        // 构造转账交易
        TransferInfo transferInfo = new TransferInfo(fromAccount.getBase58CheckAddress(), toAddress, amount);
        // 备注
        transferInfo.setMemo("一个备注");
        // trx 转账处理器
        TransferContractHandler handler = new TransferContractHandler(wrapper);
        // 转账并返回交易ID
        String id = handler.transfer(transferInfo, null);
        log.debug(id);
    }

    /**
     * trc20 转账- 非本地构造交易 - 过时的
     */
    @Test
    public void transferTrc20() {
        // 金额
        BigDecimal amount = BigDecimal.valueOf(1);
        // trc20 合约信息
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(testContractAddress, wrapper);
        // 获取系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(amount);
        // 转账交易信息
        Trc20TransferInfo trc20TransferInfo = new Trc20TransferInfo(
                fromAccount.getBase58CheckAddress(),
                toAddress,
                transferAmount,
                trc20ContractInfo.getAddress()
        );
        // 备注
        trc20TransferInfo.setMemo("备注：trc20 转账");
        // trc20 处理器
        TriggerSmartContractHandler handler = new TriggerSmartContractHandler(wrapper);
        // 转账并返回交易ID，不用自定义私钥
        String id = handler.transfer(trc20TransferInfo, null);
        log.debug("交易ID：{}", id);
    }

}
