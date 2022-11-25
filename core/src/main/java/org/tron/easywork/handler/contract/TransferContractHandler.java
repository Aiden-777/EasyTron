package org.tron.easywork.handler.contract;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.util.TransactionParser;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 策略：trx处理器
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-02 14:52
 */
@Deprecated
public class TransferContractHandler extends ContractHandlerAbstract {

    public TransferContractHandler(ApiWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TransferContract.class);
    }

    @Override
    public TransferInfo getTransferInfo(GeneratedMessageV3 contract) {
        if (contract instanceof Contract.TransferContract transferContract) {
            return TransactionParser.getTransferInfo(transferContract);
        }
        throw new ClassCastException("不支持的类：" + contract.getClass().getName() + "。请提供 TransferContract 类型对象!");
    }

    @Override
    public String transfer(TransferInfo transferInfo, KeyPair keyPair) throws IllegalException {
        // 此处使用一次gRPC请求
        Response.TransactionExtention transfer =
                wrapper.transfer(
                        transferInfo.getFrom()
                        , transferInfo.getTo()
                        , transferInfo.getAmount().longValue()
                );

        if (null != transferInfo.getMemo()) {
            Chain.Transaction.raw raw = transfer.getTransaction().getRawData().toBuilder()
                    // 设置备注
                    .setData(ByteString.copyFromUtf8(transferInfo.getMemo())).build();
            Chain.Transaction transaction = transfer.getTransaction().toBuilder().setRawData(raw).build();
            // 重新计算交易ID
            byte[] txId = ApiWrapper.calculateTransactionHash(transaction);
            transfer = transfer.toBuilder().setTransaction(transaction).setTxid(ByteString.copyFrom(txId)).build();
        }

        Chain.Transaction signTransaction;
        if (null != keyPair) {
            signTransaction = wrapper.signTransaction(transfer, keyPair);
        } else {
            signTransaction = wrapper.signTransaction(transfer);
        }
        return wrapper.broadcastTransaction(signTransaction);
    }

    @Override
    public String transfer(TransferInfo transferInfo, Collection<KeyPair> keyPairs, Integer permissionId) throws IllegalException {

        Response.TransactionExtention transfer =
                wrapper.transfer(
                        transferInfo.getFrom()
                        , transferInfo.getTo()
                        , transferInfo.getAmount().longValue()
                );

        Chain.Transaction.Builder transactionBuilder = transfer.getTransaction().toBuilder();

        // 使用 permissionId 号活跃权限
        transactionBuilder.getRawDataBuilder().getContractBuilder(0).setPermissionId(permissionId);

        if (null != transferInfo.getMemo()) {
            Chain.Transaction.raw raw = transactionBuilder.getRawDataBuilder()
                    // 设置备注
                    .setData(ByteString.copyFromUtf8(transferInfo.getMemo())).build();
            transactionBuilder.setRawData(raw);
        }
        // ----------------------------------------------------------------

        AtomicReference<Chain.Transaction> transaction = new AtomicReference<>(transactionBuilder.build());

        keyPairs.forEach(keyPair -> transaction.set(wrapper.signTransaction(transaction.get(), keyPair)));

        return wrapper.broadcastTransaction(transaction.get());
    }
}
