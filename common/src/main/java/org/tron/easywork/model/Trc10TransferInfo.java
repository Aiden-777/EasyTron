package org.tron.easywork.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tron.easywork.enums.TransferType;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-09-21 14:29
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Trc10TransferInfo extends TransferInfo {

    /**
     * Trc10 资源名称
     */
    private BigInteger assetName;

    @Override
    public TransferType getTransferType() {
        return TransferType.TRC10;
    }

    public Trc10TransferInfo() {
    }

    public Trc10TransferInfo(String from, String to, BigDecimal amount, BigInteger assetName) {
        super(from, to, amount);
        this.assetName = assetName;
    }

    @Override
    public boolean contractTargetEquals(Object contractTarget) {
        if (contractTarget instanceof BigInteger target) {
            return this.contractTargetEquals(target);
        } else if (contractTarget instanceof String target) {
            return this.contractTargetEquals(target);
        }
        return false;
    }

    public boolean contractTargetEquals(BigInteger contractTarget) {
        return assetName.compareTo(contractTarget) == 0;
    }

    public boolean contractTargetEquals(String contractTarget) {
        try {
            BigDecimal bigDecimal = new BigDecimal(contractTarget);
            return this.contractTargetEquals(bigDecimal);
        } catch (Exception ex) {
            return false;
        }
    }
}
