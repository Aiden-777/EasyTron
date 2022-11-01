package org.tron.easywork.exception;

/**
 * 智能合约 函数选择器 错误异常
 *
 * @author Admin
 * @version 1.0
 * @time 2022-11-01 18:51
 */
public class FunctionSelectorException extends Exception {

    public FunctionSelectorException(String message) {
        super(message);
    }

    public FunctionSelectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
