package org.tron.easywork.enums;

import org.tron.trident.proto.Chain;

/**
 * 转账类型
 *
 * @author Admin
 * @version 1.0
 * @time 2022-09-21 14:27
 */
public enum TransferType {
    /**
     * TRX
     */
    TRX(Chain.Transaction.Contract.ContractType.TransferContract),
    /**
     * TRC20
     */
    TRC20(Chain.Transaction.Contract.ContractType.TriggerSmartContract),
    /**
     * TRC10
     */
    TRC10(Chain.Transaction.Contract.ContractType.TransferAssetContract);

    /**
     * 支持的合约类型
     */
    public final Chain.Transaction.Contract.ContractType supportContractType;

    TransferType(Chain.Transaction.Contract.ContractType supportContractType) {
        this.supportContractType = supportContractType;
    }
}
