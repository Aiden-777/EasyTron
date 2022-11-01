package org.tron.easywork.handler.contract;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferInfo;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;

import java.util.Collection;

/**
 * 合约处理策略接口
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-02 14:27
 */
@Deprecated
public interface ContractHandlerInterface {

    /**
     * 解包
     *
     * @param any any
     * @return 合约
     * @throws InvalidProtocolBufferException 无效数据异常
     */
    GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException;

    /**
     * 获取交易信息
     *
     * @param contract 合约信息
     * @return 转账信息
     * @throws SmartParamDecodeException 转账数据解析错误
     * @throws FunctionSelectorException 智能合约 函数选择器 错误异常
     */
    TransferInfo getTransferInfo(GeneratedMessageV3 contract) throws SmartParamDecodeException, FunctionSelectorException;

    /**
     * 转账
     *
     * @param transferInfo 交易信息
     * @param keyPair      私钥
     * @return 交易ID
     * @throws IllegalException ex
     */
    String transfer(TransferInfo transferInfo, KeyPair keyPair) throws IllegalException;

    /**
     * 转账
     *
     * @param transferInfo 交易信息
     * @param keyPairs     私钥集合
     * @param permissionId 权限ID
     * @return 交易ID
     * @throws IllegalException ex
     */
    String transfer(TransferInfo transferInfo, Collection<KeyPair> keyPairs, Integer permissionId) throws IllegalException;
}
