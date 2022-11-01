package org.tron.easywork.util;

import com.google.protobuf.ByteString;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.utils.Base58Check;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 转换工具类
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-13 21:20
 */
public class TronConverter {

    public static String hexToBase58(String hex) {
        byte[] decode = Hex.decode(hex);
        return Base58Check.bytesToBase58(decode);
    }

    public static String base58ToHex(String base58) {
        byte[] base58ToBytes = Base58Check.base58ToBytes(base58);
        return Hex.toHexString(base58ToBytes);
    }

    public static Integer hexToInt(String hex) {
        byte[] decode = Hex.decode(hex);
        String tokenId = new String(decode);
        return Integer.parseInt(tokenId);
    }

    /**
     * 获取真实金额
     * 单位：个
     *
     * @param transferAmount 余额原始值
     * @param decimals       精度
     * @return 真实金额
     */
    public static BigDecimal getRealAmount(BigDecimal transferAmount, int decimals) {
        return transferAmount.divide(BigDecimal.TEN.pow(decimals), RoundingMode.HALF_UP);
    }

    /**
     * 获取系统转账时发送的金额
     * 精度完全的金额
     * 单位：最小合约单位
     *
     * @param realAmount 真实余额
     * @param decimals   精度
     * @return 系统转账金额
     */
    public static BigDecimal getTransferAmount(BigDecimal realAmount, int decimals) {
        return realAmount.multiply(BigDecimal.TEN.pow(decimals));
    }

    /**
     * 从 log topic 当中获取 tron hex 地址
     *
     * @param addressTopic topic
     * @return tron hex 地址
     */
    public static String getTronHexAddressFromTopic(ByteString addressTopic) {
        String hex = Hex.toHexString(addressTopic.toByteArray());
        return "41" + hex.substring(hex.length() - 40);
    }

    /**
     * 从 log topic 当中获取 tron base58 地址
     *
     * @param addressTopic topic
     * @return tron base58 地址
     */
    public static String getTronBase58AddressFromTopic(ByteString addressTopic) {
        String hexAddress = getTronHexAddressFromTopic(addressTopic);
        return hexToBase58(hexAddress);
    }

}
