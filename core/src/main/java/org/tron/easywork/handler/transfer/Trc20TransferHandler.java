package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.enums.TransferType;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.TransactionUtil;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-30 16:12
 */
public class Trc20TransferHandler extends BaseTransferHandler {

    /**
     * 默认矿工费限制，单位sum
     */
    private final Long defaultFeeLimit;

    public Trc20TransferHandler() {
        // 设置默认矿工费限制为 30 TRX
        this.defaultFeeLimit = Convert.toSun(BigDecimal.valueOf(30), Convert.Unit.TRX).longValue();
    }

    public Trc20TransferHandler(Long defaultFeeLimit) {
        this.defaultFeeLimit = defaultFeeLimit;
    }

    @Override
    public Chain.Transaction.Contract.ContractType getContractType() {
        return Chain.Transaction.Contract.ContractType.TriggerSmartContract;
    }

    @Override
    public TransferType getTransferType() {
        return TransferType.TRC20;
    }

    @Override
    protected Any createContractParameter(TransferInfo transferInfo) {
        Trc20TransferInfo trc20Transfer = this.checkAndTranslate(transferInfo);

        // 构造trc20转账函数
        Function function = new Function(
                "transfer",
                Arrays.asList(
                        new Address(trc20Transfer.getTo()),
                        new Uint256(trc20Transfer.getAmount().toBigInteger())),
                List.of(new TypeReference<Bool>() {
                })
        );
        // 编码
        String encodedHex = FunctionEncoder.encode(function);
        // 构造trc20合约信息
        Contract.TriggerSmartContract contract = Contract.TriggerSmartContract.newBuilder()
                .setOwnerAddress(ApiWrapper.parseAddress(trc20Transfer.getFrom()))
                .setContractAddress(ApiWrapper.parseAddress(trc20Transfer.getContractAddress()))
                .setData(ApiWrapper.parseHex(encodedHex))
                .build();
        return Any.pack(contract);
    }

    @Override
    protected void setFeeLimit(Chain.Transaction.raw.Builder rawBuilder, TransferInfo transferInfo) {
        Trc20TransferInfo trc20Transfer = this.checkAndTranslate(transferInfo);
        if (null != trc20Transfer.getFeeLimit()) {
            rawBuilder.setFeeLimit(trc20Transfer.getFeeLimit());
        } else {
            rawBuilder.setFeeLimit(defaultFeeLimit);
        }
    }

    @Override
    protected Trc20TransferInfo checkAndTranslate(TransferInfo transferInfo) {
        if (transferInfo instanceof Trc20TransferInfo trc20TransferInfo) {
            return trc20TransferInfo;
        }
        throw new UnsupportedOperationException("仅支持trc20操作，提供了错误的交易类型：" + transferInfo.getClass());
    }

    @Override
    protected GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TriggerSmartContract.class);
    }

    @Override
    public Trc20TransferInfo getTransferInfo(GeneratedMessageV3 contract) throws SmartParamDecodeException, FunctionSelectorException {
        if (contract instanceof Contract.TriggerSmartContract triggerSmartContract) {
            return TransactionUtil.getTransferInfo(triggerSmartContract);
        }
        throw new ClassCastException("不支持的类：" + contract.getClass().getName() + "。请提供 TriggerSmartContract 类型对象!");
    }
}
