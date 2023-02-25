package org.tron.easywork.handler.collection;

import lombok.Getter;
import lombok.Setter;
import org.tron.easywork.model.Trc20ContractInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-11-08 17:07
 */
@Getter
@Setter
public class FundCollectionConfig {

    /**
     * Trc20合约地址
     */
    private final Trc20ContractInfo trc20ContractInfo;
    /**
     * Trc20目标地址
     */
    private final String targetAddressOfTrc20;
    /**
     * Trx目标地址
     */
    private final String targetAddressOfTrx;
    /**
     * 矿工费派发者地址
     */
    private final String handingFeeProviderAddress;
    /**
     * 矿工费派发者使用权限ID
     */
    private int handingFeeProviderPermissionId;
    /**
     * 矿工费派发者的私钥
     * <p>
     * 多签则为多个key
     */
    private final List<String> handingFeeProviderKeys;

    /**
     * 矿工费 - 单位 trx
     */
    private final BigDecimal handingFeeWithTrx;


    public FundCollectionConfig(Trc20ContractInfo trc20ContractInfo, String targetAddressOfTrc20,
                                String targetAddressOfTrx, String handingFeeProviderAddress,
                                List<String> handingFeeProviderKeys, BigDecimal handingFeeWithTrx) {
        this.trc20ContractInfo = trc20ContractInfo;
        this.targetAddressOfTrc20 = targetAddressOfTrc20;
        this.targetAddressOfTrx = targetAddressOfTrx;
        this.handingFeeProviderAddress = handingFeeProviderAddress;
        this.handingFeeProviderKeys = handingFeeProviderKeys;
        this.handingFeeWithTrx = handingFeeWithTrx;
    }
}
