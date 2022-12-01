package org.tron.easywork.util;

/**
 * 进制转换
 *
 * @author Admin
 * @version 1.0
 * @time 2022-03-25 16:10
 */
public class BaseConvert {

    /**
     * 转16进制补零
     *
     * @param value 10 进制
     * @return 16
     */
    public static String toBase16StringWithZero(long value) {
        String blockIdStart = Long.toHexString(value);
        String zeros = "0".repeat(16 - blockIdStart.length());
        return zeros + blockIdStart;
    }

}
