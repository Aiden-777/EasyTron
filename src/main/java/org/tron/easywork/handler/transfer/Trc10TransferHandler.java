package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.util.TransferUtil;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-11 10:58
 */
public class Trc10TransferHandler extends BaseTransferHandler<Contract.TransferAssetContract> {

    @Override
    protected Chain.Transaction.Contract.ContractType getContractType() {
        return Chain.Transaction.Contract.ContractType.TransferAssetContract;
    }

    @Override
    protected Any createContractParameter(Transfer transfer) {
        Contract.TransferAssetContract contract = Contract.TransferAssetContract.newBuilder()
                .setAmount(transfer.getAmount().longValue())
                .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                .setToAddress(ApiWrapper.parseAddress(transfer.getTo()))
                .setAssetName(ByteString.copyFrom(transfer.getAssetName().toByteArray()))
                .build();
        return Any.pack(contract);
    }

    @Override
    protected Contract.TransferAssetContract unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TransferAssetContract.class);
    }

    @Override
    protected Transfer getTransferInfo(Contract.TransferAssetContract transferAssetContract) {
        return TransferUtil.getTransferInfo(transferAssetContract);
    }
}
