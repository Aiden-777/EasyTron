package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.easywork.enums.TransferType;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.util.TransactionParser;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-30 18:48
 */
@Slf4j
public class TrxTransferHandler extends BaseTransferHandler {

    @Override
    public Chain.Transaction.Contract.ContractType getContractType() {
        return Chain.Transaction.Contract.ContractType.TransferContract;
    }

    @Override
    public TransferType getTransferType() {
        return TransferType.TRX;
    }

    @Override
    protected Any createContractParameter(TransferInfo transferInfo) {
        TransferInfo transfer = this.checkAndTranslate(transferInfo);

        Contract.TransferContract contract = Contract.TransferContract.newBuilder()
                .setAmount(transfer.getAmount().longValue())
                .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                .setToAddress(ApiWrapper.parseAddress(transfer.getTo()))
                .build();

        return Any.pack(contract);
    }

    @Override
    protected TransferInfo checkAndTranslate(TransferInfo transferInfo) {
        if (TransferInfo.class == transferInfo.getClass()) {
            return transferInfo;
        }
        throw new UnsupportedOperationException("仅支持trx操作，提供了错误的交易类型：" + transferInfo.getClass());
    }

    @Override
    protected GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TransferContract.class);
    }

    @Override
    public TransferInfo getTransferInfo(GeneratedMessageV3 contract) {
        if (contract instanceof Contract.TransferContract transferContract) {
            return TransactionParser.getTransferInfo(transferContract);
        }
        throw new ClassCastException("不支持的类：" + contract.getClass().getName() + "。请提供 TransferContract 类型对象!");
    }

}
