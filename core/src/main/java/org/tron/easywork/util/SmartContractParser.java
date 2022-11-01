package org.tron.easywork.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.tron.easywork.constant.TronConstants;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferFunctionParam;
import org.tron.trident.abi.TypeDecoder;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.NumericType;
import org.tron.trident.proto.Contract;

import java.math.BigDecimal;

/**
 * 智能合约解码
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-01 13:36
 */
@Slf4j
public class SmartContractParser {

    /**
     * 转账事件解析
     *
     * @param triggerSmartContract 智能合约
     * @return 到账地址+金额
     */
    public static TransferFunctionParam getTransferFunctionParam(Contract.TriggerSmartContract triggerSmartContract) throws SmartParamDecodeException, FunctionSelectorException {
        String data = Hex.toHexString(triggerSmartContract.getData().toByteArray());
        return getTransferFunctionParam(data);
    }

    /**
     * 智能合约转账函数数据解析
     *
     * @param data triggerSmartContract.data
     * @return 转账数据(到账地址 、 金额)
     * @throws SmartParamDecodeException 转账数据解析错误
     */
    public static TransferFunctionParam getTransferFunctionParam(String data) throws SmartParamDecodeException, FunctionSelectorException {
        // 函数选择器，必须为【a9059cbb】
        String funcId = data.substring(0, 8);
        if (!TronConstants.TRANSFER_FUNC_ID_BY_KECCAK256.equals(funcId)) {
            throw new FunctionSelectorException(funcId + "不是标准转账函数！");
        }
        // 收款人地址
        /*String toAddress = data.substring(8, 72);*/
        String toAddress = data.substring(32, 72);
        // 发送金额
        String amount = data.substring(72, 136);
        try {
            Address addressType = (Address) TypeDecoder.instantiateType("address", toAddress);
            NumericType amountType = (NumericType) TypeDecoder.instantiateType("uint256", amount);
            return new TransferFunctionParam(addressType.getValue(), new BigDecimal(amountType.getValue()));
        } catch (Exception e) {
            throw new SmartParamDecodeException("智能合约转账函数参数异常:" + data, e.getCause());
        }
    }

}
