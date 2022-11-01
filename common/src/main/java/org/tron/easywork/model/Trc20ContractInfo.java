package org.tron.easywork.model;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

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
     * 合约精度
     */
    private final BigDecimal decimals;

    /**
     * 比值: 10的合约精度次方
     */
    private final BigDecimal rate;

    public Trc20ContractInfo(String address, BigDecimal decimals) {
        this.address = address;
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
        return realAmount.multiply(BigDecimal.TEN.pow(decimals.intValue()));
    }
}
