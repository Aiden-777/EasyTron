package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.enums.TransferType;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc10TransferInfo;
import org.tron.easywork.util.TransactionUtil;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-30 17:06
 */
public class Trc10TransferHandler extends BaseTransferHandler {

    @Override
    public Chain.Transaction.Contract.ContractType getContractType() {
        return Chain.Transaction.Contract.ContractType.TransferAssetContract;
    }

    @Override
    public TransferType getTransferType() {
        return TransferType.TRC10;
    }


    @Override
    protected Any createContractParameter(TransferInfo transferInfo) {
        Trc10TransferInfo trc10Transfer = this.checkAndTranslate(transferInfo);

        Contract.TransferAssetContract contract = Contract.TransferAssetContract.newBuilder()
                .setAmount(trc10Transfer.getAmount().longValue())
                .setOwnerAddress(ApiWrapper.parseAddress(trc10Transfer.getFrom()))
                .setToAddress(ApiWrapper.parseAddress(trc10Transfer.getTo()))
                .setAssetName(ByteString.copyFrom(trc10Transfer.getAssetName().toByteArray()))
                .build();

        return Any.pack(contract);
    }

    @Override
    protected Trc10TransferInfo checkAndTranslate(TransferInfo transferInfo) {
        if (transferInfo instanceof Trc10TransferInfo trc10TransferInfo) {
            return trc10TransferInfo;
        }
        throw new UnsupportedOperationException("仅支持trc10操作，提供了错误的交易类型：" + transferInfo.getClass());
    }


    @Override
    protected GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TransferAssetContract.class);
    }

    @Override
    public Trc10TransferInfo getTransferInfo(GeneratedMessageV3 contract) {
        if (contract instanceof Contract.TransferAssetContract transferAssetContract) {
            return TransactionUtil.getTransferInfo(transferAssetContract);
        }
        throw new ClassCastException("不支持的类：" + contract.getClass().getName() + "。请提供 TransferAssetContract 类型对象!");
    }
}
