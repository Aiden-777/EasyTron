package org.tron.easywork.util;

import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.FunctionReturnDecoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 合约工具类
 *
 * @author Admin
 * @version 1.0
 * @time 2022-10-11 00:24
 */
public class Trc20Utils {

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


}
