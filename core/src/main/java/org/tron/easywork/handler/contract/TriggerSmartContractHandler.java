package org.tron.easywork.handler.contract;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.TransactionUtil;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 策略：trc20处理器
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-02 14:47
 */
@Deprecated
public class TriggerSmartContractHandler extends ContractHandlerAbstract {

    /**
     * 默认矿工费限制 - 15TRX
     */
    protected Long defaultFeeLimit = Convert.toSun(BigDecimal.valueOf(15), Convert.Unit.TRX).longValue();

    public TriggerSmartContractHandler(ApiWrapper wrapper) {
        super(wrapper);
    }

    public Long getDefaultFeeLimit() {
        return defaultFeeLimit;
    }

    public void setDefaultFeeLimit(Long defaultFeeLimit) {
        this.defaultFeeLimit = defaultFeeLimit;
    }

    @Override
    public GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TriggerSmartContract.class);
    }

    @Override
    public Trc20TransferInfo getTransferInfo(GeneratedMessageV3 contract) throws SmartParamDecodeException, FunctionSelectorException {
        if (contract instanceof Contract.TriggerSmartContract triggerSmartContract) {
            return TransactionUtil.getTransferInfo(triggerSmartContract);
        }
        throw new ClassCastException("不支持的类：" + contract.getClass().getName() + "。请提供 TriggerSmartContract 类型对象!");
    }

    @Override
    public String transfer(TransferInfo transferInfo, KeyPair keyPair) {
        if (transferInfo instanceof Trc20TransferInfo trc20TransferInfo) {
            // 转账函数
            Function transfer = new Function(
                    "transfer",
                    Arrays.asList(
                            new Address(trc20TransferInfo.getTo()),
                            new Uint256(trc20TransferInfo.getAmount().toBigInteger())),
                    List.of(new TypeReference<Bool>() {
                    })
            );
            // 构造智能合约调用
            TransactionBuilder builder = wrapper.triggerCall(trc20TransferInfo.getFrom(),
                    trc20TransferInfo.getContractAddress(), transfer);
            // 矿工费限制
            if (null != trc20TransferInfo.getFeeLimit()) {
                builder.setFeeLimit(trc20TransferInfo.getFeeLimit());
            } else {
                builder.setFeeLimit(defaultFeeLimit);
            }
            // 备注
            if (null != trc20TransferInfo.getMemo()) {
                builder.setMemo(trc20TransferInfo.getMemo());
            }
            // 签名
            Chain.Transaction signedTxn;
            if (null != keyPair) {
                signedTxn = wrapper.signTransaction(builder.build(), keyPair);
            } else {
                signedTxn = wrapper.signTransaction(builder.build());
            }
            // 广播
            return wrapper.broadcastTransaction(signedTxn);
        }
        throw new ClassCastException("转账信息类型错误【" + transferInfo.getClass().getName() + "】，请提供 Trc20TransferInfo 类型对象");
    }

    @Override
    public String transfer(TransferInfo transferInfo, Collection<KeyPair> keyPairs, Integer permissionId) {
        if (transferInfo instanceof Trc20TransferInfo trc20TransferInfo) {
            // 转账函数
            Function transfer = new Function(
                    "transfer",
                    Arrays.asList(
                            new Address(trc20TransferInfo.getTo()),
                            new Uint256(trc20TransferInfo.getAmount().toBigInteger())
                    ),
                    List.of(new TypeReference<Bool>() {
                    })
            );
            // 构造智能合约调用
            TransactionBuilder triggerSmartTransactionBuilder = wrapper.triggerCall(trc20TransferInfo.getFrom(),
                    trc20TransferInfo.getContractAddress(), transfer);

            // 矿工费限制
            if (null != trc20TransferInfo.getFeeLimit()) {
                triggerSmartTransactionBuilder.setFeeLimit(trc20TransferInfo.getFeeLimit());
            } else {
                triggerSmartTransactionBuilder.setFeeLimit(defaultFeeLimit);
            }
            // 备注
            if (null != trc20TransferInfo.getMemo()) {
                triggerSmartTransactionBuilder.setMemo(trc20TransferInfo.getMemo());
            }

            Chain.Transaction.Builder transactionBuilder = triggerSmartTransactionBuilder.getTransaction().toBuilder();

            // 使用 permissionId 号活跃权限
            transactionBuilder.getRawDataBuilder().getContractBuilder(0).setPermissionId(permissionId);
            // ----------------------------------------------------------------

            Chain.Transaction transaction = transactionBuilder.build();

            triggerSmartTransactionBuilder.setTransaction(transaction);

            AtomicReference<Chain.Transaction> triggerSmartTransaction = new AtomicReference<>(triggerSmartTransactionBuilder.build());

            keyPairs.forEach(keyPair -> triggerSmartTransaction.set(wrapper.signTransaction(triggerSmartTransaction.get(), keyPair)));

            return wrapper.broadcastTransaction(triggerSmartTransaction.get());
        }
        throw new ClassCastException("转账信息类型错误【" + transferInfo.getClass().getName() + "】，请提供 Trc20TransferInfo 类型对象");
    }

}
