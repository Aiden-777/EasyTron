package org.tron.easywork.demo;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc10TransferInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.BlockUtil;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-30 14:31
 */
@Slf4j
public class DemoLocalTransferHandler {

    public Chain.Transaction buildLocalTransfer(TransferInfo transferInfo, Chain.BlockHeader refBlockHeader) {
        long blockHeight = refBlockHeader.getRawData().getNumber();
        String blockId = BlockUtil.parseBlockId(refBlockHeader);

        byte[] refBlockNum = ByteBuffer.allocate(8).putLong(blockHeight).array();
        byte[] blockHash = Hash.sha256(refBlockHeader.getRawData().toByteArray());

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, 8);

        // 备注
        ByteString memo = StringUtil.isNullOrEmpty(transferInfo.getMemo()) ? ByteString.empty() : ByteString.copyFromUtf8(transferInfo.getMemo());

        // 合约类型
        Chain.Transaction.Contract.ContractType contractType = this.getContractType(transferInfo.getClass());
        // 合约信息
        GeneratedMessageV3 message = this.getParameter(transferInfo);

        // 构造交易信息
        Chain.Transaction.Builder transactionBuilder = Chain.Transaction.newBuilder();
        transactionBuilder.setRawData(
                Chain.Transaction.raw.newBuilder()
                        // 创建时间
                        .setTimestamp(now.getTime())
                        // 过期时间
                        .setExpiration(calendar.getTimeInMillis())
                        // 参考区块
                        .setRefBlockHash(ByteString.copyFrom(subArray(blockHash, 8, 16)))
                        // 参考区块
                        .setRefBlockBytes(ByteString.copyFrom(subArray(Hex.decode(blockId), 6, 8)))
                        // 添加合约信息
                        .addContract(
                                Chain.Transaction.Contract.newBuilder()
                                        // 设置合约类型
                                        .setType(contractType)
                                        // 合约内容
                                        .setParameter(Any.pack(message))
                                        // 权限ID
                                        .setPermissionId(0)
                        )
                        // 备注
                        .setData(memo)
                        // trc20 手续费限制
                        .setFeeLimit(Convert.toSun(BigDecimal.TEN, Convert.Unit.TRX).longValue())
        );
        return transactionBuilder.build();
    }

    public Contract.TransferContract buildTransferContract(TransferInfo transfer) {
        return org.tron.trident.proto.Contract.TransferContract.newBuilder()
                .setAmount(transfer.getAmount().longValue())
                .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                .setToAddress(ApiWrapper.parseAddress(transfer.getTo()))
                .build();
    }

    public Contract.TransferAssetContract buildTransferContract(Trc10TransferInfo trc10Transfer) {
        return Contract.TransferAssetContract.newBuilder()
                .setAmount(trc10Transfer.getAmount().longValue())
                .setOwnerAddress(ApiWrapper.parseAddress(trc10Transfer.getFrom()))
                .setToAddress(ApiWrapper.parseAddress(trc10Transfer.getTo()))
                .setAssetName(ByteString.copyFrom(trc10Transfer.getAssetName().toByteArray()))
                .build();
    }

    public Contract.TriggerSmartContract buildTransferContract(Trc20TransferInfo trc20Transfer) {
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
        return Contract.TriggerSmartContract.newBuilder()
                .setOwnerAddress(ApiWrapper.parseAddress(trc20Transfer.getFrom()))
                .setContractAddress(ApiWrapper.parseAddress(trc20Transfer.getContractAddress()))
                .setData(ApiWrapper.parseHex(encodedHex))
                .build();
    }

    private GeneratedMessageV3 getParameter(TransferInfo transferInfo) {
        if (transferInfo instanceof Trc20TransferInfo trc20Transfer) {
            return this.buildTransferContract(trc20Transfer);
        } else if (transferInfo instanceof Trc10TransferInfo trc10Transfer) {
            return this.buildTransferContract(trc10Transfer);
        } else {
            return this.buildTransferContract(transferInfo);
        }
    }

    private static byte[] subArray(byte[] input, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, end - start);
        return result;
    }


    /**
     * 根据转账类型获取合约类型
     *
     * @param clazz 转账类型
     * @return 合约类型
     */
    private <T extends TransferInfo> Chain.Transaction.Contract.ContractType getContractType(Class<T> clazz) {
        if (Trc20TransferInfo.class.equals(clazz)) {
            return Chain.Transaction.Contract.ContractType.TriggerSmartContract;
        } else if (Trc10TransferInfo.class.equals(clazz)) {
            return Chain.Transaction.Contract.ContractType.TransferAssetContract;
        } else {
            return Chain.Transaction.Contract.ContractType.TransferContract;
        }
    }
}
