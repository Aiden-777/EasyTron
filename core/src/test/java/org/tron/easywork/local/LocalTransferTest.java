package org.tron.easywork.local;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.handler.transfer.TransferHandler;
import org.tron.easywork.handler.transfer.TransferHandlerContext;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-12 07:35
 */
@Slf4j
public class LocalTransferTest extends BaseTest {

    /**
     * 构造任何类型的转账交易信息、转账
     * 引入 TransferHandlerContext ，可以不用在意传入的转账是什么类型，程序自动识别
     */
    @Test
    public void TransferContextTest() throws IllegalException {
        // 转账处理器上下文
        TransferHandlerContext context = this.createTransferHandlerContext();

        // 真实金额，单位：个
        BigDecimal realAmount = BigDecimal.valueOf(1.2);
        // TRC20合约信息
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(contractAddress, wrapper);
        // 实际金额，单位：合约最小单位
        BigDecimal amount = trc20ContractInfo.getTransferAmount(realAmount);
        // Trc20转账信息
        Transfer transfer = this.createTrc20Transfer(from, to, amount);
        // 转账处理器
        TransferHandler handler = context.getHandler(transfer.getTransferType().supportContractType());

        ApiWrapper apiWrapper = ApiWrapper.ofShasta(key);
        Chain.Block nowBlock = apiWrapper.getNowBlock();
        ReferenceBlock referenceBlock = new ReferenceBlock(nowBlock.getBlockHeader());

        Chain.Transaction transaction = handler.buildLocalTransfer(transfer, referenceBlock);
        this.sendTransaction(transaction);
    }

    /**
     * TRX转账
     */
    @Test
    public void transferTrx() throws IllegalException {
        // 真实金额，单位：个
        BigDecimal realAmount = BigDecimal.valueOf(1.2);
        // 实际金额，单位：sum
        BigDecimal amount = Convert.toSun(realAmount, Convert.Unit.TRX);
        // TRX转账信息
        Transfer transfer = this.createTrxTransfer(from, to, amount);
        // TRX转账处理器
        TrxTransferHandler handler = new TrxTransferHandler();
        // 引用区块
        ReferenceBlock referenceBlock = this.getReferenceBlock(wrapper);
        // 交易
        Chain.Transaction transaction = handler.buildLocalTransfer(transfer, referenceBlock);
        this.sendTransaction(transaction);
    }

    /**
     * TRC20转账
     */
    @Test
    public void transferTrc20() throws IllegalException {
        // 真实金额，单位：个
        BigDecimal realAmount = BigDecimal.valueOf(1.2);
        // TRC20合约信息
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(contractAddress, wrapper);
        // 实际金额，单位：合约最小单位
        BigDecimal amount = trc20ContractInfo.getTransferAmount(realAmount);
        // Trc20转账信息
        Transfer transfer = this.createTrc20Transfer(from, to, amount);
        // Trc20转账处理器
        Trc20TransferHandler handler = new Trc20TransferHandler();
        // 引用区块
        ReferenceBlock referenceBlock = this.getReferenceBlock(wrapper);
        // 交易
        Chain.Transaction transaction = handler.buildLocalTransfer(transfer, referenceBlock);
        this.sendTransaction(transaction);

    }


    /**
     * # 333 - 完整的本地交易构造（参考）
     */
    @Test
    public void localTransferTest() throws IllegalException {
        // 真实金额，单位：个
        BigDecimal realAmount = BigDecimal.valueOf(1.2);
        // TRC20合约信息
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(contractAddress, wrapper);
        // 实际金额，单位：合约最小单位
        BigDecimal amount = trc20ContractInfo.getTransferAmount(realAmount);
        // Trc20转账信息
        Transfer transfer = this.createTrc20Transfer(from, to, amount);
        // 本地转账
        this.localTransfer(transfer);
    }

    /**
     * # 333 -完整的本地交易构造（参考）
     *
     * <p>
     * 原本流程一个交易是将信息通过 gRPC接口 在远程构建，现在使用代码在本地构建交易。
     * 好处是减少网络IO次数，更加灵活的配置交易变量
     * 需要注意的点，本地构造交易需要一个引用区块，这个区块距离最新区块高度不能超过65535，比如可以在系统中配置一个引用区块全局变量，每两个小时刷新一次，以达到复用效果。
     * <a href="https://cn.developers.tron.network/v3.7/docs/%E6%9C%AC%E5%9C%B0%E6%9E%84%E5%BB%BA%E4%BA%A4%E6%98%93">文档搜索：本地构建交易</a>
     * 1dd048b5183e0d468a7891ad8db79cce6e1046957cd218b75e4e44aed5be27b3
     */
    protected void localTransfer(Transfer transfer) throws IllegalException {
        // 当前时间
        Date now = new Date();

        // 当前时间 +8 小时 - 用于过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, 8);

        // 合约类型
        Chain.Transaction.Contract.ContractType contractType = transfer.getTransferType().supportContractType();
        // 合约信息
        GeneratedMessageV3 message;

        if (contractType == Chain.Transaction.Contract.ContractType.TriggerSmartContract) {
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
            message = org.tron.trident.proto.Contract.TriggerSmartContract.newBuilder()
                    .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                    .setContractAddress(ApiWrapper.parseAddress(transfer.getContractAddress()))
                    .setData(ApiWrapper.parseHex(encodedHex))
                    .build();
        } else if (contractType == Chain.Transaction.Contract.ContractType.TransferAssetContract) {
            // 构造trc10合约信息
            message = org.tron.trident.proto.Contract.TransferAssetContract.newBuilder()
                    .setAmount(transfer.getAmount().longValue())
                    .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                    .setToAddress(ApiWrapper.parseAddress(transfer.getTo()))
                    .setAssetName(ByteString.copyFrom(transfer.getAssetName().toByteArray()))
                    .build();
        } else if (contractType == Chain.Transaction.Contract.ContractType.TransferContract) {
            // 构造trx转账合约
            message = org.tron.trident.proto.Contract.TransferContract.newBuilder()
                    .setAmount(transfer.getAmount().longValue())
                    .setOwnerAddress(ApiWrapper.parseAddress(transfer.getFrom()))
                    .setToAddress(ApiWrapper.parseAddress(transfer.getTo()))
                    .build();
        } else {
            return;
        }

        // 获取参考区块
        Chain.Block nowBlock = wrapper.getNowBlock();

        // 自定义引用区块信息类
        ReferenceBlock referenceBlock = new ReferenceBlock(nowBlock.getBlockHeader());

        // 构造交易信息
        Chain.Transaction.Builder transactionBuilder = Chain.Transaction.newBuilder();
        // 设置交易原数据
        transactionBuilder.setRawData(
                Chain.Transaction.raw.newBuilder()
                        // 创建时间
                        .setTimestamp(now.getTime())
                        // 过期时间
                        .setExpiration(calendar.getTimeInMillis())
                        // 参考区块信息
                        .setRefBlockHash(referenceBlock.getRefBlockHash())
                        // 参考区块信息
                        .setRefBlockBytes(referenceBlock.getRefBlockBytes())
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
                        .setData(ByteString.copyFromUtf8("备注一份"))
                        // trc20 手续费限制
                        .setFeeLimit(Convert.toSun(BigDecimal.valueOf(15), Convert.Unit.TRX).longValue())
        );
        Chain.Transaction transaction = transactionBuilder.build();
        this.sendTransaction(transaction);
    }


}
