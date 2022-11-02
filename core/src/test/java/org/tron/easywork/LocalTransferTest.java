package org.tron.easywork;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.tron.easywork.demo.DemoLocalTransferHandler;
import org.tron.easywork.handler.transfer.LocalTransferContext;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.*;
import org.tron.easywork.util.BlockParser;
import org.tron.easywork.util.ContractUtils;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 本地转账交易
 *
 * @author Admin
 * @version 1.0
 * @time 2022-10-29 18:18
 */
@Slf4j
public class LocalTransferTest extends BaseTest {

    /**
     * 从 LocalTransferContext 执行转账 （可实用级别）
     * 引入 LocalTransferContext ，可以不用在意传入的转账是什么类型，程序自动识别
     * <p>
     * 1273a14d335dbfb47a003b3ac92192488fe135f5a7babe14ff3454b47262aadf
     */
    @Test
    public void localTransferContext() throws IllegalException {
        // 转出地址
        String fromAddress = fromAccount.getBase58CheckAddress();
        // 实际转账金额
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // 合约
        Trc20ContractInfo trc20ContractInfo = ContractUtils.readTrc20ContractInfo(testContractAddress, wrapper);
        // 系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(realAmount);
        // 构造trc20交易
        Trc20TransferInfo trc20TransferInfo = new Trc20TransferInfo(fromAddress, toAddress, transferAmount, trc20ContractInfo.getAddress());
        // 备注 - 可以为空
        trc20TransferInfo.setMemo("大聪明...");
        // 矿工费限制 - 可以为空，处理器默认设置为10trx
        trc20TransferInfo.setFeeLimit(defaultFeeLimit);
        // 获取参考区块
        Chain.Block refBlock = wrapper.getNowBlock();
        // 处理器上下文
        LocalTransferContext localTransferContext = new LocalTransferContext();
        // 添加trc20处理器
        localTransferContext.addHandler("trc20TransferHandler", new Trc20TransferHandler());
        // 添加trx处理器
        localTransferContext.addHandler("trxTransferHandler", new TrxTransferHandler());
        try {
            // 构造本地交易
            Chain.Transaction transaction = localTransferContext.buildLocalTransfer(trc20TransferInfo, refBlock.getBlockHeader());
            // 签名
            Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
            // 广播并返回ID
            String tid = wrapper.broadcastTransaction(signTransaction);
            log.debug(tid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * # 333 - 完整的本地交易构造（参考）
     */
    @Test
    public void localTransferTest() throws IllegalException {
        // 转出地址
        String fromAddress = fromAccount.getBase58CheckAddress();
        // 实际转账金额
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // 合约
        Trc20ContractInfo trc20ContractInfo = ContractUtils.readTrc20ContractInfo(testContractAddress, wrapper);
        // 系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(realAmount);
        // 构造trc20交易
        Trc20TransferInfo trc20TransferInfo = new Trc20TransferInfo(fromAddress, toAddress, transferAmount, trc20ContractInfo.getAddress());
        // 本地转账
        this.localTransfer(trc20TransferInfo);
    }

    /**
     * # 333 -完整的本地交易构造（参考）
     * 原本流程一个交易是将信息通过 gRPC接口 在远程构建，现在使用代码在本地构建交易。
     * 好处是减少网络IO次数，更加灵活的配置交易变量
     * 需要注意的点，本地构造交易需要一个引用区块，这个区块距离最新区块高度不能超过65535，比如可以在系统中配置一个引用区块全局变量，每两个小时刷新一次，以达到复用效果。
     * <a href="https://cn.developers.tron.network/v3.7/docs/%E6%9C%AC%E5%9C%B0%E6%9E%84%E5%BB%BA%E4%BA%A4%E6%98%93">文档搜索：本地构建交易</a>
     * 1dd048b5183e0d468a7891ad8db79cce6e1046957cd218b75e4e44aed5be27b3
     */
    public void localTransfer(TransferInfo transferInfo) throws IllegalException {
        // 获取参考区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 区块高度
        long blockHeight = nowBlock.getBlockHeader().getRawData().getNumber();
        // 区块ID
        String blockId = BlockParser.parseBlockId(nowBlock);

        byte[] refBlockNum = ByteBuffer.allocate(8).putLong(blockHeight).array();
        byte[] blockHash = Hash.sha256(nowBlock.getBlockHeader().getRawData().toByteArray());

        // 当前时间
        Date now = new Date();

        // 当前时间 +8 小时 - 用于过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, 8);

        // 合约类型
        Chain.Transaction.Contract.ContractType contractType;
        // 合约信息
        GeneratedMessageV3 message;

        if (transferInfo instanceof Trc20TransferInfo trc20TransferInfo) {
            contractType = Chain.Transaction.Contract.ContractType.TriggerSmartContract;
            // 构造trc20转账函数
            Function function = new Function(
                    "transfer",
                    Arrays.asList(
                            new Address(trc20TransferInfo.getTo()),
                            new Uint256(trc20TransferInfo.getAmount().toBigInteger())),
                    List.of(new TypeReference<Bool>() {
                    })
            );
            // 编码
            String encodedHex = FunctionEncoder.encode(function);

            // 构造trc20合约信息
            message = org.tron.trident.proto.Contract.TriggerSmartContract.newBuilder()
                    .setOwnerAddress(ApiWrapper.parseAddress(transferInfo.getFrom()))
                    .setContractAddress(ApiWrapper.parseAddress(trc20TransferInfo.getContractAddress()))
                    .setData(ApiWrapper.parseHex(encodedHex))
                    .build();
        } else if (transferInfo instanceof Trc10TransferInfo trc10Transfer) {
            contractType = Chain.Transaction.Contract.ContractType.TransferAssetContract;
            // 构造trc10合约信息
            message = org.tron.trident.proto.Contract.TransferAssetContract.newBuilder()
                    .setAmount(trc10Transfer.getAmount().longValue())
                    .setOwnerAddress(ApiWrapper.parseAddress(trc10Transfer.getFrom()))
                    .setToAddress(ApiWrapper.parseAddress(trc10Transfer.getTo()))
                    .setAssetName(ByteString.copyFrom(trc10Transfer.getAssetName().toByteArray()))
                    .build();
        } else {
            contractType = Chain.Transaction.Contract.ContractType.TransferContract;
            // 构造trx转账合约
            message = org.tron.trident.proto.Contract.TransferContract.newBuilder()
                    .setAmount(transferInfo.getAmount().longValue())
                    .setOwnerAddress(ApiWrapper.parseAddress(transferInfo.getFrom()))
                    .setToAddress(ApiWrapper.parseAddress(transferInfo.getTo()))
                    .build();
        }


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
                        .setRefBlockHash(ByteString.copyFrom(subArray(blockHash, 8, 16)))
                        // 参考区块信息
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
                        .setData(ByteString.copyFromUtf8("备注一份"))
                        // trc20 手续费限制
                        .setFeeLimit(Convert.toSun(BigDecimal.TEN, Convert.Unit.TRX).longValue())
        );
        Chain.Transaction transaction = transactionBuilder.build();
        // 签名
        Chain.Transaction signedTxn = wrapper.signTransaction(transaction);
        // 广播并获取交易ID
        String tid = wrapper.broadcastTransaction(signedTxn);
        log.debug("交易ID：{}", tid);
    }

    /**
     * # 333 - 分割数组
     */
    public static byte[] subArray(byte[] input, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, end - start);
        return result;
    }


    /**
     * 将不同类型的转账封装到一个类中（初级demo级别）
     * 5cbe17671d9ac54b7ed8b58addbfc15ed4c6ed1809df55eb3d690cf60b3b2962
     */
    @Test
    public void demoLocalTransferTest() throws IllegalException {
        // 到账地址
        String fromAddress = fromAccount.getBase58CheckAddress();
        // 转账金额
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // 合约
        Trc20ContractInfo trc20ContractInfo = ContractUtils.readTrc20ContractInfo(testContractAddress, wrapper);
        // 转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(realAmount);
        // 构造trc20 转账
        Trc20TransferInfo transferInfo = new Trc20TransferInfo(fromAddress, toAddress, transferAmount, trc20ContractInfo.getAddress());
        // 获取参考区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 获取测试处理器
        DemoLocalTransferHandler handler = new DemoLocalTransferHandler();
        // 测试处理器构造本地交易
        Chain.Transaction transaction = handler.buildLocalTransfer(transferInfo, nowBlock.getBlockHeader());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
        // 广播并返回交易ID
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
    }


    /**
     * 抽象本地转账封装（中级demo级别）
     * 50a359dcf0bfbaedb4164176656b46ba1fad0fcabe0894e6347d4ac17052db3b
     */
    @Test
    public void localTransferLastTest() throws IllegalException {
        String fromAddress = fromAccount.getBase58CheckAddress();
        // 实际转账金额
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // 系统转账金额
        BigDecimal transferAmount = Convert.toSun(realAmount, Convert.Unit.TRX);
        // 构造 trx 交易
        TransferInfo transferInfo = new TransferInfo(fromAddress, toAddress, transferAmount);
        // 设置备注
        transferInfo.setMemo("大聪明");
        // 获取参考区块
        Chain.Block refBlock = wrapper.getNowBlock();
        // trx 转账处理器
        TrxTransferHandler trxTransferHandler = new TrxTransferHandler();
        // 处理器构造本地交易
        Chain.Transaction transaction = trxTransferHandler.buildLocalTransfer(transferInfo, refBlock.getBlockHeader());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
        // 广播并返回ID
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
    }

}
