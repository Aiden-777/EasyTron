package org.tron.easywork.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tron.easywork.enums.TransferType;
import org.tron.trident.proto.Chain;

import java.math.BigDecimal;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-09-21 14:29
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Trc20TransferInfo extends TransferInfo {

    /**
     * 合约地址
     */
    private String contractAddress;

    /**
     * 矿工费限制 - 单位sum
     */
    private Long feeLimit;

    @Override
    public TransferType getTransferType() {
        return TransferType.TRC20;
    }

    public Trc20TransferInfo() {
    }

    public Trc20TransferInfo(String from, String to, BigDecimal amount, String contractAddress) {
        super(from, to, amount);
        this.contractAddress = contractAddress;
    }


    @Override
    public Chain.Transaction.Contract.ContractType supportContractType() {
        return Chain.Transaction.Contract.ContractType.TriggerSmartContract;
    }

    @Override
    public boolean contractTargetEquals(Object contractTarget) {
        if (contractTarget instanceof String target) {
            return contractAddress.equals(target);
        }
        return false;
    }
}
