package org.tron.easywork;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.handler.transfer.*;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.util.BlockUtil;
import org.tron.easywork.util.TransactionUtil;
import org.tron.easywork.util.TransferUtil;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.Constant;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;

import java.util.List;

/**
 * 读取区块信息
 *
 * @author Admin
 * @version 1.0
 * @time 2022-11-02 17:30
 */
@Slf4j
public class ReadBlockTest extends BaseTest {


    /**
     * 获取区块中的交易信息 （trident原生思路参考）
     */
    @Test
    public void blockReadTest_trident() throws IllegalException {
        // 此处使用主网 - 测试网读取区块看不出效果，交易数量太少
        wrapper = new ApiWrapper(Constant.TRONGRID_MAIN_NET, Constant.TRONGRID_MAIN_NET_SOLIDITY, key);
        // 获取最新区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 区块ID
        String blockId = BlockUtil.parseBlockId(nowBlock);
        log.info("区块ID：{}", blockId);
        log.info("区块高度：{}", nowBlock.getBlockHeader().getRawData().getNumber());
        if (nowBlock.getTransactionsCount() <= 0) {
            log.debug("交易数量为0");
            return;
        }
        // 区块中的所有交易
        List<Chain.Transaction> transactionsList = nowBlock.getTransactionsList();
        // 遍历
        transactionsList.forEach(
                transaction -> {
                    log.debug("--------------------------------");
                    // 交易ID
                    String transactionId = TransactionUtil.getTransactionId(transaction);
                    log.info("交易ID：{}", transactionId);
                    // 交易状态
                    boolean status = transaction.getRet(0).getContractRet().getNumber() == 1;
                    log.debug("交易状态：{}", status ? "成功" : "失败");
                    if (!status) {
                        return;
                    }
                    // 合约
                    Chain.Transaction.Contract contract = transaction.getRawData().getContract(0);
                    // 合约类型
                    Chain.Transaction.Contract.ContractType contractType = contract.getType();
                    // parameter - 数据|入参
                    Any parameter = contract.getParameter();
                    // 根据合约类型使用不同的工具进行解码
                    // 如果是 触发智能合约 操作
                    if (contractType == Chain.Transaction.Contract.ContractType.TriggerSmartContract) {
                        try {
                            log.debug("这是智能合约交易");
                            // 解码
                            org.tron.trident.proto.Contract.TriggerSmartContract triggerSmartContract =
                                    parameter.unpack(org.tron.trident.proto.Contract.TriggerSmartContract.class);
                            // 获取交易详情
                            Transfer transfer = TransferUtil.getTransferInfo(triggerSmartContract);
                            log.debug("类型：{}\t到账地址：{}\t金额：{}", transfer.getTransferType(), transfer.getTo(), transfer.getAmount());
                            // ......
                        } catch (InvalidProtocolBufferException e) {
                            log.debug("unpack解包异常");
                            e.printStackTrace();
                        } catch (SmartParamDecodeException e) {
                            log.debug("智能合约 转账参数 数据解析异常");
                            e.printStackTrace();
                        } catch (FunctionSelectorException e) {
                            // 函数选择器错误
                        } catch (Exception e) {
                            log.error("兜底异常：{}", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    // 如果是trx
                    else if (contractType == Chain.Transaction.Contract.ContractType.TransferContract) {
                        try {
                            log.debug("这是TRX交易");
                            Contract.TransferContract unpack = parameter.unpack(Contract.TransferContract.class);
                            Transfer transferInfo = TransferUtil.getTransferInfo(unpack);
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }


    /**
     * 读取主网最新区块的转账交易内容
     */
    @Test
    public void readBlockTest() throws IllegalException {
        // 此处使用主网 - 测试网读取区块看不出效果，交易数量太少
        wrapper = new ApiWrapper(Constant.TRONGRID_MAIN_NET, Constant.TRONGRID_MAIN_NET_SOLIDITY, key);
        // 获取最新块
        Chain.Block nowBlock = wrapper.getNowBlock();

        // trx转账处理器
        TrxTransferHandler trxTransferHandler = new TrxTransferHandler();
        // trc10转账处理器
        Trc10TransferHandler trc10TransferHandler = new Trc10TransferHandler();
        // trc20转账处理器
        Trc20TransferHandler trc20TransferHandler = new Trc20TransferHandler();

        // 转账处理器上下文
        TransferHandlerContext transferHandlerContext = new TransferHandlerContext();
        transferHandlerContext.addHandler("trxTransferHandler", trxTransferHandler);
        transferHandlerContext.addHandler("trc10TransferHandler", trc10TransferHandler);
        transferHandlerContext.addHandler("trc20TransferHandler", trc20TransferHandler);

        // 遍历交易列表
        nowBlock.getTransactionsList().forEach(transaction -> {
            // 根据交易合约类型获取处理器
            TransferHandler handler = transferHandlerContext.getHandler(transaction.getRawData().getContract(0).getType());
            if (null == handler) {
                return;
            }
            try {
                // 解析交易，不忽略失败的交易
                Transfer transfer = handler.parse(transaction);
                log.debug("状态：{}\t类型：{}\t到账地址：{}\t金额：{}", transfer.getStatus(), transfer.getTransferType(), transfer.getTo(), transfer.getAmount());
                // 其他逻辑 ................
            } catch (InvalidProtocolBufferException e) {
                log.error("解包错误：{}", e.getMessage());
            } catch (SmartParamDecodeException e) {
                log.error("转账解析错误：{}", e.getMessage());
            } catch (FunctionSelectorException e) {
                // 标准转账函数 - a9059cbb
            } catch (Exception e) {
                log.error("异常兜底：{}", e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
