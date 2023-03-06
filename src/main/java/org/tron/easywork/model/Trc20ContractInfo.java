package org.tron.easywork.model;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 智能合约 模型
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-01 14:36
 */
@Getter
@ToString
public class Trc20ContractInfo {

    /**
     * 合约地址
     */
    private final String address;

    /**
     * 合约名称
     */
    private final String symbol;

    /**
     * 合约精度
     */
    private final BigDecimal decimals;

    /**
     * 比值: 10的合约精度次方
     */
    private final BigDecimal rate;

    public Trc20ContractInfo(String address, String symbol, BigDecimal decimals) {
        this.address = address;
        this.symbol = symbol;
        this.decimals = decimals;
        this.rate = BigDecimal.TEN.pow(decimals.intValue());
    }

    /**
     * 获取转账金额
     *
     * @param realAmount 真实金额 单位个
     * @return 转账金额
     */
    public BigDecimal getTransferAmount(BigDecimal realAmount) {
        return realAmount.multiply(rate);
    }

    /**
     * 获取真实金额
     *
     * @param transferAmount 转账金额
     * @return 真实金额
     */
    public BigDecimal getRealAmount(BigDecimal transferAmount) {
        return transferAmount.divide(rate, decimals.intValue(), RoundingMode.DOWN);
    }

}
