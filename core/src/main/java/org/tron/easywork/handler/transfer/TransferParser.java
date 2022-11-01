package org.tron.easywork.handler.transfer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferInfo;
import org.tron.trident.proto.Chain;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-31 14:05
 */
public interface TransferParser {

    /**
     * 转账信息解析
     *
     * @param transaction 交易信息
     * @return 转账信息
     * @throws InvalidProtocolBufferException unpack解包异常
     * @throws SmartParamDecodeException      转账解析异常
     * @throws FunctionSelectorException      智能合约 函数选择器 错误异常
     */
    TransferInfo parse(Chain.Transaction transaction) throws InvalidProtocolBufferException, SmartParamDecodeException, FunctionSelectorException;
}
