package org.tron.easywork;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.handler.contract.TransferContractHandler;
import org.tron.easywork.handler.contract.TriggerSmartContractHandler;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.*;
import org.tron.easywork.util.*;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.SignatureValidator;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Contract.AccountPermissionUpdateContract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-13 21:34
 */
@Slf4j
public class TransactionTest extends BaseTest {

    /**
     * 地址格式转换
     */
    @Test
    public void toHex() {
        String addr = "TYUFU6WtuwyMEGZg9c241z6NymnVTzg3WU";
        byte[] bs = Base58Check.base58ToBytes(addr);
        String hex = Hex.toHexString(bs);
        log.debug(hex);
    }

    /**
     * 是否为Tron地址
     */
    @Test
    public void isTronAddress() {
        String address = "TP6QorvxAJ4bXg21LterCpGi5oZ2PxybCZ";
        boolean isAddress = AccountUtils.isTronAddress(address);
        log.debug("是否为正确的Tron地址：{}", isAddress);
    }

    /**
     * 创建账号
     */
    @Test
    public void createAccount() {
        AccountInfo newAccount = new AccountInfo(KeyPair.generate());
        log.debug(newAccount.toString());
    }

    /**
     * 导入账号
     */
    @Test
    public void importAccount() {
        AccountInfo accountInfo = new AccountInfo(privateKey);
        log.debug(accountInfo.toString());
    }

    /**
     * 解析trc10 资源名称
     */
    @Test
    public void parseAsset_name() {
        String assetName = "31303030393835";
        Integer n = TronConverter.hexToInt(assetName);
        log.debug(n.toString());
    }

    /**
     * # ApiWrapper原装 - 获取trc20余额
     */
    @Test
    public void simple_balanceOfTrc20() {
        // 地址
        String address = fromAccount.getBase58CheckAddress();
        // 获取合约信息
        Contract contract = wrapper.getContract(testContractAddress);
        // 构造trc20合约信息
        Trc20Contract trc20Contract = new Trc20Contract(contract, address, wrapper);
        // 合约精度
        BigInteger decimals = trc20Contract.decimals();
        // 余额
        BigInteger balance = trc20Contract.balanceOf(address);
        // 真实余额 单位 个
        BigDecimal result = TronConverter.getRealAmount(new BigDecimal(balance), decimals.intValue());
        log.debug("剩余数量：{}个", result);
    }

    /**
     * # ApiWrapper原装 - 获取trx余额
     */
    @Test
    public void simple_balanceOfTrx() {
        // 地址
        String address = fromAccount.getBase58CheckAddress();
        // 获取账户信息
        Response.Account account = wrapper.getAccount(address);
        // 余额
        long balance = account.getBalance();
        // 真实余额
        BigDecimal trx = Convert.fromSun(new BigDecimal(balance), Convert.Unit.TRX);
        log.debug("trx余额：{}", trx.toString());

        long sum = account.getFrozenList().stream().mapToLong(Response.Account.Frozen::getFrozenBalance).sum();
        long frozenBalance = account.getAccountResource().getFrozenBalanceForEnergy().getFrozenBalance();
        long frozen = sum + frozenBalance;
        BigDecimal frozenTrx = Convert.fromSun(new BigDecimal(frozen), Convert.Unit.TRX);
        log.debug("trx质押：{}", frozenTrx);

        log.debug("trx 总余额：{}", trx.add(frozenTrx));
    }

    /**
     * # ApiWrapper原装 - trc20 转账 - 非本地构造交易
     */
    @Test
    public void simple_transfer_trc20() {
        // 真实金额 - 单位个
        long realAmount = 11;

        // 根据合约地址获取合约信息
        org.tron.trident.core.contract.Contract contract = wrapper.getContract(testContractAddress);
        // 构造trc20合约
        Trc20Contract trc20Contract = new Trc20Contract(contract, fromAccount.getBase58CheckAddress(), wrapper);

        // trc20 合约转账
        String tid = trc20Contract.transfer(
                toAddress,
                realAmount,
                // 精度
                trc20Contract.decimals().intValue(),
                "备注",
                Convert.toSun("10", Convert.Unit.TRX).longValue()
        );
        log.debug(tid);
    }


    /**
     * trx 转账 - 非本地构造交易 - 过时的
     */
    @Test
    public void transferTrx() throws IllegalException {
        // 转账金额 单位 sum
        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        // 构造转账交易
        TransferInfo transferInfo = new TransferInfo(fromAccount.getBase58CheckAddress(), toAddress, amount);
        // 备注
        transferInfo.setMemo("一个备注");
        // trx 转账处理器
        TransferContractHandler handler = new TransferContractHandler(wrapper);
        // 转账并返回交易ID
        String id = handler.transfer(transferInfo, null);
        log.debug(id);
    }

    /**
     * trc20 转账- 非本地构造交易 - 过时的
     */
    @Test
    public void transferTrc20() {
        // 金额
        BigDecimal amount = BigDecimal.valueOf(1);
        // trc20 合约信息
        Trc20ContractInfo trc20ContractInfo = ContractUtils.readTrc20ContractInfo(testContractAddress, wrapper);
        // 获取系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(amount);
        // 转账交易信息
        Trc20TransferInfo trc20TransferInfo = new Trc20TransferInfo(
                fromAccount.getBase58CheckAddress(),
                toAddress,
                transferAmount,
                trc20ContractInfo.getAddress()
        );
        // 备注
        trc20TransferInfo.setMemo("备注：trc20 转账");
        // trc20 处理器
        TriggerSmartContractHandler handler = new TriggerSmartContractHandler(wrapper);
        // 转账并返回交易ID，不用自定义私钥
        String id = handler.transfer(trc20TransferInfo, null);
        log.debug("交易ID：{}", id);
    }

    /**
     * trx 转账 - 本地构造交易
     */
    @Test
    public void transferTrxLocal() throws IllegalException {
        // 引用区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 转账金额 单位 sum
        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        // 构造转账交易
        TransferInfo transferInfo = new TransferInfo(fromAccount.getBase58CheckAddress(), toAddress, amount);
        // 备注
        transferInfo.setMemo("一个备注");
        // trx 转账处理器
        TrxTransferHandler handler = new TrxTransferHandler();
        // 转账并返回交易ID
        Chain.Transaction transaction = handler.buildLocalTransfer(transferInfo, nowBlock.getBlockHeader());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
        // 广播
        String id = wrapper.broadcastTransaction(signTransaction);
        log.debug(id);
    }

    /**
     * trc20 转账- 本地构造交易
     */
    @Test
    public void transferTrc20Local() throws IllegalException {
        // 引用区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 金额
        BigDecimal amount = BigDecimal.valueOf(1);
        // trc20 合约信息
        Trc20ContractInfo trc20ContractInfo = ContractUtils.readTrc20ContractInfo(testContractAddress, wrapper);
        // 获取系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(amount);
        // 转账交易信息
        Trc20TransferInfo trc20TransferInfo = new Trc20TransferInfo(
                fromAccount.getBase58CheckAddress(),
                toAddress,
                transferAmount,
                trc20ContractInfo.getAddress()
        );
        // 备注
        trc20TransferInfo.setMemo("备注：trc20 转账");
        // trc20 转账处理器
        Trc20TransferHandler handler = new Trc20TransferHandler();
        // 转账并返回交易ID
        Chain.Transaction transaction = handler.buildLocalTransfer(trc20TransferInfo, nowBlock.getBlockHeader());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
        // 广播
        String id = wrapper.broadcastTransaction(signTransaction);
        log.debug(id);
    }


    // 原始授权
    // @Test
    public void approveTrc20() {
        Contract contract = wrapper.getContract(testContractAddress);
        Trc20Contract trc20Contract = new Trc20Contract(contract, fromAccount.getBase58CheckAddress(), wrapper);
        // rc20Contract.approve()
    }

    /**
     * 交易签名思路
     * 1.从远程获取交易信息 - hex 格式
     * 2.获取交易ID
     * 3.如果改变原交易数据，重新计算交易ID
     * 4.对交易ID签名
     * 5.广播
     */
    // @Test
    public void signApiTransaction() throws InvalidProtocolBufferException {
        // API 提供的交易 https://api.shasta.trongrid.io/wallet/createtransaction
        String raw_data_hex = "0a02c2282208445e76cd19765ba740c89df3ab9a305a67080112630a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412320a15411391667f4940d9f58d2779d92357e714f5bf3ea91215418ff66721e871b4ae9aa673fc15154f9c0deac27418c0843d70eaddefab9a30";

        byte[] raw_data = Hex.decode(raw_data_hex);

        // 交易id（即交易哈希，通过Transaction.rawData计算SHA256得到）
        byte[] txId = Hash.sha256(raw_data);
        // 交易id_hex
        log.debug("txId_hex:{}", Hex.toHexString(txId));


        // 构造raw对象
        Chain.Transaction.raw raw = Chain.Transaction.raw.parseFrom(raw_data);

        // ---↓ 改变原始数据，后期添加备注，注意此操作需要重新计算交易ID
        raw = raw.toBuilder()
                .setData(ByteString.copyFromUtf8("hello"))
                .build();
        txId = Hash.sha256(raw.toByteArray());
        log.debug("newId_hex:{}", Hex.toHexString(txId));
        // ---↑ 如果前期就添加了、或无需备注，则忽略以上块

        // 对 txid 进行签名
        byte[] sign = KeyPair.signTransaction(txId, wrapper.keyPair);

        // 构造transaction对象（设置 raw数据、-签名数据- ）
        Chain.Transaction signTransaction = Chain.Transaction.newBuilder()
                .setRawData(raw).addSignature(ByteString.copyFrom(sign)).build();

        // 签名交易(如果该交易已经签名不能重复签，除非用到不同privateKey多签)
        // Chain.Transaction signTransaction = wrapper.signTransaction(transaction);

        // 交易bytes
        byte[] signTransactionBytes = signTransaction.toByteArray();
        // 交易 hex
        String signTransaction_hex = Hex.toHexString(signTransactionBytes);
        log.debug("signTransaction_hex:{}", signTransaction_hex);

        // 广播并返回 交易id
        String id = wrapper.broadcastTransaction(signTransaction);
        log.debug(id);
    }

    /**
     * 计算交易带宽
     */
    @Test
    public void estimateBandwidth() throws IllegalException {
        // 转账金额
        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        // 构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(
                        fromAccount.getBase58CheckAddress()
                        , toAddress
                        , amount.longValue()
                );
        // 签名交易
        Chain.Transaction transaction = wrapper.signTransaction(transfer);

        // 估计带宽
        long bandwidth = transaction.toBuilder().clearRet().build().getSerializedSize() + 64;
        log.info("带宽估计：{}", bandwidth);
        // 广播交易并返回ID
        String tid = wrapper.broadcastTransaction(transaction);
        log.info(tid);
    }

    /**
     * 字符串签名验证
     */
    @Test
    public void verifyTransaction() {
        String str = "hello world";

        String hexString = Hex.toHexString(str.getBytes());

        log.debug(hexString);

        // 交易id（即交易哈希，通过Transaction.rawData计算SHA256得到）
        byte[] txId = Hash.sha256(Hex.decode(hexString));

        // 交易id_hex
        log.debug("txId_hex:{}", Hex.toHexString(txId));

        byte[] digest = new Keccak.Digest256().digest(Hex.decode(hexString));
        log.debug("digest:{}", Hex.toHexString(digest));

        // 对 txid 进行签名
        byte[] sign = KeyPair.signTransaction(digest, fromAccount.getKeyPair());
        log.debug("签名：{}", Hex.toHexString(sign));
        // 所有者
        byte[] owner = Base58Check.base58ToBytes(fromAccount.getBase58CheckAddress());
        // 签名
        boolean verify = SignatureValidator.verify(digest, sign, owner);
        log.debug("签名结果：{}", verify);

    }

    /**
     * 解析trc20合约转账数据
     */
    @Test
    public void parseData() throws SmartParamDecodeException, FunctionSelectorException {
        String data = "a9059cbb0000000000000000000000001391667f4940d9f58d2779d92357e714f5bf3ea900000000000000000000000000000000000000000000000000000000005ffab4";
        TransferFunctionParam transferFunctionParam = SmartContractParser.getTransferFunctionParam(data);
        log.debug(transferFunctionParam.toString());
    }

    /**
     * 验证trc20交易是否成功
     */
    @Test
    public void isSuccess() throws IllegalException {
        String tid = "218414bb71d49037de6d49009fb6e4f49834aea8bda11037dc347130b6c88dbf";
        Chain.Transaction transaction = wrapper.getTransactionById(tid);
        boolean status = transaction.getRet(0).getContractRet().getNumber() == 1;
        log.debug("{},{}", status ? "成功" : "失败", tid);

        tid = "17ac1d482f373752a094adf5632c61e55c807f91aa79cd794106c7d811dae8e8";
        transaction = wrapper.getTransactionById(tid);
        status = transaction.getRet(0).getContractRet().getNumber() == 1;
        log.debug("{},{}", status ? "成功" : "失败", tid);
    }

    /**
     * 获取区块中的交易信息 （demo级别）
     */
    @Test
    public void blockRead() throws IllegalException {
        // 获取最新区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        // 区块ID
        String blockId = BlockParser.parseBlockId(nowBlock);
        log.info("区块ID：{}", blockId);
        if (nowBlock.getTransactionsCount() <= 0) {
            log.debug("交易数量为0");
            return;
        }
        // 区块中的所有交易
        List<Chain.Transaction> transactionsList = nowBlock.getTransactionsList();
        // 遍历
        transactionsList.forEach(
                transaction -> {
                    // 交易ID
                    String transactionId = TransactionParser.getTransactionId(transaction);
                    log.info("交易ID：{}", transactionId);
                    // 交易状态
                    boolean status = transaction.getRet(0).getContractRet().getNumber() == 1;
                    log.debug("交易状态：{}", status ? "成功" : "失败");
                    if (!status) {
                        return;
                    }
                    // 合约
                    Chain.Transaction.Contract contract = transaction.getRawData().getContract(0);
                    // 合约类型
                    Chain.Transaction.Contract.ContractType contractType = contract.getType();
                    // parameter - 数据|入参
                    Any parameter = contract.getParameter();
                    // 根据合约类型使用不同的工具进行解码
                    // 如果是trc20合约
                    if (contractType == Chain.Transaction.Contract.ContractType.TriggerSmartContract) {
                        try {
                            // 解码
                            org.tron.trident.proto.Contract.TriggerSmartContract triggerSmartContract =
                                    parameter.unpack(org.tron.trident.proto.Contract.TriggerSmartContract.class);
                            // 获取交易详情
                            Trc20TransferInfo transferInfo = TransactionParser.getTransferInfo(triggerSmartContract);
                            // ......
                        } catch (InvalidProtocolBufferException e) {
                            log.debug("unpack解包异常");
                            e.printStackTrace();
                        } catch (SmartParamDecodeException e) {
                            log.debug("智能合约 转账参数 数据解析异常");
                            e.printStackTrace();
                        } catch (FunctionSelectorException e) {
                            // 函数选择器错误
                        } catch (Exception e) {
                            log.error("兜底异常：{}", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    // 如果是trx
                    else if (contractType == Chain.Transaction.Contract.ContractType.TransferContract) {
                        log.debug("trx");
                    }
                }
        );
    }

    /**
     * 更新账户权限
     */
    // @Test
    public void permissionUpdate() throws IllegalException {
        //TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF
        String privateKey = "";

        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, privateKey, null);

        AccountInfo account = new AccountInfo(privateKey);

        ByteString ownerAddress = ByteString.copyFrom(Base58Check.base58ToBytes(account.getBase58CheckAddress()));

        // 指定权限列表
        /*Integer[] contractId = {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 31,
                32, 33, 41, 42, 43, 44, 45};*/
        Integer[] contractId = {1, Chain.Transaction.Contract.ContractType.TriggerSmartContract_VALUE};

        List<Integer> list = new ArrayList<>(Arrays.asList(contractId));
        byte[] operations = new byte[32];
        list.forEach(e -> operations[e / 8] |= (1 << e % 8));

        log.debug(String.valueOf(operations.length));


        AccountPermissionUpdateContract accountPermissionUpdateContract =
                AccountPermissionUpdateContract.newBuilder()
                        .setOwnerAddress(ownerAddress)
                        .setOwner(
                                org.tron.trident.proto.Common.Permission.newBuilder()
                                        .setType(Common.Permission.PermissionType.Owner)
                                        .setPermissionName("owner-test")
                                        .setThreshold(1)
                                        /*.addKeys(
                                                Common.Key.newBuilder()
                                                .setAddress(ByteString.copyFrom(Base58Check.base58ToBytes("TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z")))
                                                .setWeight(5)
                                        )*/
                                        .addKeys(Common.Key.newBuilder().setAddress(ownerAddress).setWeight(1))
                        )
                        .addActives(
                                org.tron.trident.proto.Common.Permission.newBuilder()
                                        .setType(Common.Permission.PermissionType.Active)
                                        .setPermissionName("test")
                                        .setThreshold(10)
                                        .addKeys(Common.Key.newBuilder().setAddress(ByteString.copyFrom(Base58Check.base58ToBytes("TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z"))).setWeight(5))
                                        .addKeys(Common.Key.newBuilder().setAddress(ByteString.copyFrom(Base58Check.base58ToBytes("TEczEK6uzD88888QhstH6QDwB167ZsXPrb"))).setWeight(5))
                                        .setOperations(ByteString.copyFrom(operations))
                        )
                        .build();


        Response.TransactionExtention transactionExtention = wrapper.accountPermissionUpdate(accountPermissionUpdateContract);

        Chain.Transaction signTransaction = wrapper.signTransaction(transactionExtention);

        String id = wrapper.broadcastTransaction(signTransaction);
        log.debug(id);

    }

    /**
     * 多签 - 非本地构造交易
     */
    // @Test
    public void multiSignature() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，此处未用到
        String privateKey1 = "";
        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   Trx转账权限
        String privateKey2 = "";
        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   Trx转账权限
        String privateKey3 = "";

        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, privateKey2, null);

        String from = "TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF";
        String to = "TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z";

        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);

        Response.TransactionExtention transfer =
                wrapper.transfer(
                        from
                        , to
                        , 1000000
                );

        Chain.Transaction.Builder transactionBuilder = transfer.getTransaction().toBuilder();

        // 使用2号活跃权限
        transactionBuilder.getRawDataBuilder().getContractBuilder(0).setPermissionId(2);
        // ----------------------------------------------------------------

        Chain.Transaction transaction = transactionBuilder.build();

        Chain.Transaction sign1 = wrapper.signTransaction(transaction, new KeyPair(privateKey2));
        Chain.Transaction sign2 = wrapper.signTransaction(sign1, new KeyPair(privateKey3));

        String id = wrapper.broadcastTransaction(sign2);

        /*byte[] rawData = transactionBuilder.getRawData().toByteArray();
        byte[] tidByte = Hash.sha256(rawData);

        byte[] sign1 = KeyPair.signTransaction(tidByte, new KeyPair(privateKey2));
        byte[] sign2 = KeyPair.signTransaction(tidByte, new KeyPair(privateKey3));

        Chain.Transaction transaction = transactionBuilder
                .addSignature(ByteString.copyFrom(sign1))
                .addSignature(ByteString.copyFrom(sign2))
                .build();

        String id = wrapper.broadcastTransaction(transaction);*/

        log.debug(id);

    }

    /**
     * 封装多签 - 非本地构造交易 - 过时的
     */
    // @Test
    public void multiSignature2() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，此处未用到
        AccountInfo account1 = new AccountInfo("");

        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   Trx转账权限
        AccountInfo account2 = new AccountInfo("");

        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   Trx转账权限
        AccountInfo account3 = new AccountInfo("");


        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, account1.getHexPrivateKey(), null);

        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        TransferInfo transferInfo = new TransferInfo(account1.getBase58CheckAddress(), account2.getBase58CheckAddress(), amount);
        transferInfo.setMemo("多签备注");
        TransferContractHandler transferContractHandler = new TransferContractHandler(wrapper);
        String id = transferContractHandler.transfer(transferInfo, Arrays.asList(account2.getKeyPair(), account3.getKeyPair()), 2);

        /*BigDecimal amount = TronConverter.decimalsTrc20Balance(BigDecimal.ONE, 6);
        TransferInfo transferInfo = new Trc20TransferInfo(account1.getBase58CheckAddress(), account2.getBase58CheckAddress(), amount, "TFd1piJ8iXmJQicTicq4zChDSNSMLPFR4w");
        transferInfo.setMemo("多签备注");

        TriggerSmartContractHandler triggerSmartContractHandler = new TriggerSmartContractHandler(wrapper);
        String id = triggerSmartContractHandler.transfer(transferInfo, Arrays.asList(account2.getKeyPair(), account3.getKeyPair()), 2);*/

        log.debug(id);

    }

}
