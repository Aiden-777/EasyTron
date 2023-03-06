package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.util.TransferUtil;
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
 * @time 2023-02-11 09:18
 */
public class Trc20TransferHandler extends BaseTransferHandler<Contract.TriggerSmartContract> {
    /**
     * 默认矿工费限制，单位sum
     */
    private final Long defaultFeeLimit;

    public Trc20TransferHandler() {
        // 设置默认矿工费限制为 50 TRX
        this.defaultFeeLimit = Convert.toSun(BigDecimal.valueOf(50), Convert.Unit.TRX).longValue();
    }

    public Trc20TransferHandler(Long defaultFeeLimit) {
        this.defaultFeeLimit = defaultFeeLimit;
    }

    @Override
    protected Chain.Transaction.Contract.ContractType getContractType() {
        return Chain.Transaction.Contract.ContractType.TriggerSmartContract;
    }


    @Override
    protected Any createContractParameter(Transfer transfer) {
        // 构造trc20转账函数
        Function function = new Function(
                "transfer",
                Arrays.asList(
                        new Address(transfer.getTo()),
                        new Uint256(transfer.getAmount().toBigInteger())),
                List.of(new TypeReference<Bool>() {
                })
        );
        // 编码
        String encodedHex = FunctionEncoder.encode(function);
        // 构造trc20合约信息
        Contract.TriggerSmartContract contract = Contract.TriggerSmartContract.newBuilder()
                .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                .setContractAddress(ApiWrapper.parseAddress(transfer.getContractAddress()))
                .setData(ApiWrapper.parseHex(encodedHex))
                .build();
        return Any.pack(contract);
    }

    @Override
    protected Chain.Transaction.raw.Builder initTransactionRawBuilder(Transfer transferInfo, ReferenceBlock referenceBlock) {
        Chain.Transaction.raw.Builder rawBuilder = super.initTransactionRawBuilder(transferInfo, referenceBlock);
        // 设置智能合约手续费限制
        if (null != transferInfo.getFeeLimit()) {
            rawBuilder.setFeeLimit(transferInfo.getFeeLimit());
        } else {
            rawBuilder.setFeeLimit(defaultFeeLimit);
        }
        return rawBuilder;
    }

    @Override
    public Transfer parse(Chain.Transaction transaction) throws InvalidProtocolBufferException, SmartParamDecodeException, FunctionSelectorException {
        Transfer parse = super.parse(transaction);
        parse.setFeeLimit(transaction.getRawData().getFeeLimit());
        return parse;
    }

    @Override
    protected Contract.TriggerSmartContract unpack(Any any) throws InvalidProtocolBufferException {
        return any.unpack(Contract.TriggerSmartContract.class);
    }

    @Override
    protected Transfer getTransferInfo(Contract.TriggerSmartContract triggerSmartContract) throws SmartParamDecodeException, FunctionSelectorException {
        return TransferUtil.getTransferInfo(triggerSmartContract);
    }
}
