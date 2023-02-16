package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.util.TransferUtil;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-11 10:38
 */
public class TrxTransferHandler extends BaseTransferHandler<Contract.TransferContract> {

    @Override
    protected Chain.Transaction.Contract.ContractType getContractType() {
        return Chain.Transaction.Contract.ContractType.TransferContract;
    }

    @Override
    protected Any createContractParameter(Transfer transfer) {
        Contract.TransferContract contract = Contract.TransferContract.newBuilder()
                .setAmount(transfer.getAmount().longValue())
                .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                .setToAddress(ApiWrapper.parseAddress(transfer.getTo()))
                .build();
        return Any.pack(contract);
    }

    @Override
    protected Contract.TransferContract unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TransferContract.class);
    }

    @Override
    protected Transfer getTransferInfo(Contract.TransferContract transferContract) {
        return TransferUtil.getTransferInfo(transferContract);
    }
}
