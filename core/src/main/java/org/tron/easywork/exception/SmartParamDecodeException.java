package org.tron.easywork.exception;

/**
 * 智能合约 转账参数 数据解析异常
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-21 04:06
 */
public class SmartParamDecodeException extends Exception {

    public SmartParamDecodeException(String message) {
        super(message);
    }

    public SmartParamDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
