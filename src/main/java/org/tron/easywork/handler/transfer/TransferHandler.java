package org.tron.easywork.handler.transfer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.trident.proto.Chain;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-11 10:31
 */
public interface TransferHandler {

    /**
     * 判断是否支持处理传入的合约类型
     *
     * @param contractType 合约类型
     * @return 是否支持
     */
    boolean supportContractType(Chain.Transaction.Contract.ContractType contractType);

    /**
     * 构建本地转账交易
     *
     * @param transfer       转账信息
     * @param referenceBlock 引用区块，范围最新区块 65535 以内
     * @return 交易信息
     */
    Chain.Transaction buildLocalTransfer(Transfer transfer, ReferenceBlock referenceBlock);

    /**
     * 转账信息解析
     *
     * @param transaction 交易信息
     * @return 转账信息
     * @throws InvalidProtocolBufferException unpack解包异常(大概率合约输入类型有误)
     * @throws SmartParamDecodeException      转账解析异常(ABI解码错误)
     * @throws FunctionSelectorException      智能合约 函数选择器 错误异常
     */
    Transfer parse(Chain.Transaction transaction) throws InvalidProtocolBufferException, SmartParamDecodeException, FunctionSelectorException;
}
