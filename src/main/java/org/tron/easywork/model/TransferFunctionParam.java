package org.tron.easywork.model;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-01 13:38
 */
@Data
@ToString
public class TransferFunctionParam {

    /**
     * 到账地址
     */
    private String toAddress;

    /**
     * 金额
     */
    private BigDecimal amount;

    public TransferFunctionParam(String toAddress, BigDecimal amount) {
        this.toAddress = toAddress;
        this.amount = amount;
    }
}
