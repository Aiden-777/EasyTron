package org.tron.easywork.util;

import org.bouncycastle.util.encoders.Hex;
import org.tron.easywork.constant.TronConstants;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferFunctionParam;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.FunctionReturnDecoder;
import org.tron.trident.abi.TypeDecoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.NumericType;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 智能合约解码
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-01 13:36
 */
public class Trc20ContractUtil {


    /**
     * 读取 trc20 信息
     *
     * @param concatAddress 合约地址
     * @param wrapper       wrapper
     * @return trc20 信息
     */
    public static Trc20ContractInfo readTrc20ContractInfo(String concatAddress, ApiWrapper wrapper) {
        org.tron.trident.core.contract.Contract contract = wrapper.getContract(concatAddress);
        Trc20Contract trc20Contract = new Trc20Contract(contract, concatAddress, wrapper);
        BigInteger decimals = trc20Contract.decimals();
        return new Trc20ContractInfo(concatAddress, new BigDecimal(decimals));
    }

    /**
     * 查询trc20余额
     *
     * @param contractAddress 合约地址
     * @param address         账户地址
     * @return 余额
     */
    public static BigDecimal trc20BalanceOf(String contractAddress, String address, ApiWrapper wrapper) {
        // 构造trc20查余额函数
        Function balanceOf = new Function(
                "balanceOf",
                List.of(new Address(address)),
                List.of(new TypeReference<Uint256>() {
                })
        );
        // 编码
        String encodedHex = FunctionEncoder.encode(balanceOf);
        // 构造trc20合约信息
        org.tron.trident.proto.Contract.TriggerSmartContract contract = org.tron.trident.proto.Contract.TriggerSmartContract.newBuilder()
                .setContractAddress(ApiWrapper.parseAddress(contractAddress))
                .setData(ApiWrapper.parseHex(encodedHex))
                .build();
        // 查询余额
        Response.TransactionExtention tx = wrapper.blockingStub.triggerConstantContract(contract);
        // 余额
        String result = Numeric.toHexString(tx.getConstantResult(0).toByteArray());
        BigInteger balance = (BigInteger) FunctionReturnDecoder.decode(result, balanceOf.getOutputParameters()).get(0).getValue();
        return new BigDecimal(balance);
    }

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
            Address addressType = (Address) TypeDecoder.instantiateType(new TypeReference<Address>() {
            }, toAddress);
            NumericType amountType = (NumericType) TypeDecoder.instantiateType(new TypeReference<Uint256>() {
            }, amount);
            return new TransferFunctionParam(addressType.getValue(), new BigDecimal(amountType.getValue()));
        } catch (Exception e) {
            throw new SmartParamDecodeException("智能合约转账函数参数异常:" + data, e.getCause());
        }
    }

}
