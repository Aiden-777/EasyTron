package org.tron.easywork.util;

import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-01 16:52
 */
public class TransactionUtil {

    /**
     * 解析交易ID（交易hash）
     *
     * @param transaction 交易
     * @return hash
     */
    public static String getTransactionId(Chain.Transaction transaction) {
        /*
            byte[] rawData = transaction.getRawData().toByteArray();
            byte[] rawDataHash256 = Hash.sha256(rawData);
            return Hex.toHexString(rawDataHash256);
        */
        byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
        return Hex.toHexString(bytes);
    }

    /**
     * 检查交易是否成功
     *
     * @param transaction 交易
     * @return 是否成功
     */
    public static boolean isTransactionSuccess(Chain.Transaction transaction) {
        return transaction.getRet(0).getContractRet().getNumber() == 1;
    }

    /**
     * 获取交易中的第一个合约
     *
     * @param transaction 交易
     * @return 合约
     */
    public static Chain.Transaction.Contract getFirstContract(Chain.Transaction transaction) {
        return transaction.getRawData().getContract(0);
    }


    /**
     * 获取交易中第一个合约的类型
     *
     * @param transaction 交易
     * @return 合约类型
     */
    public static Chain.Transaction.Contract.ContractType getFirstContractType(Chain.Transaction transaction) {
        return getFirstContract(transaction).getType();
    }

    /**
     * 获取交易中第一个合约的权限ID
     *
     * @param transaction 交易
     * @return 权限ID
     */
    public static int getFirstPermissionId(Chain.Transaction transaction) {
        return getFirstContract(transaction).getPermissionId();
    }


}
