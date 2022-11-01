package org.tron.easywork.util;

import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 合约工具类
 *
 * @author Admin
 * @version 1.0
 * @time 2022-10-11 00:24
 */
public class ContractUtils {

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


}
