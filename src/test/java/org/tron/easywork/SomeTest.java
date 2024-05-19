package org.tron.easywork;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.AccountInfo;
import org.tron.easywork.model.TransferFunctionParam;
import org.tron.easywork.util.AccountUtils;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.easywork.util.TronConverter;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.SignatureValidator;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-13 21:34
 */
@Slf4j
public class SomeTest extends BaseTest {

    @Test
    public void hexAddressToBase58() {
        String hexStr = "41b6cb08800483ad904d43df53cec7a8d866ed1d95";
        byte[] decode = Hex.decode(hexStr);
        String base58 = Base58Check.bytesToBase58(decode);
        log.debug(base58);
    }

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

        try {
            // 推荐
            ApiWrapper.parseAddress(address);
        } catch (Exception e) {
            log.error("地址错误");
        }

        // 非严谨
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
        AccountInfo accountInfo = new AccountInfo(key);
        log.debug(accountInfo.toString());
    }

    /**
     * 从区块ID中获取区块高度(未经大量测试)
     */
    @Test
    public void getBlockHeightFromBlockId() {
        String blockId = "00000000032f44b1684f8a72488a3e74507b3efb918cf4ac9aeeadf0917b62d2";
        String start = blockId.substring(0, blockId.length() - 48);
        long l = Long.parseLong(start, 16);
        log.debug(String.valueOf(l));
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


    // 原始授权
    // @Test
    public void approveTrc20() {
        Contract contract = wrapper.getContract(contractAddress);
        Trc20Contract trc20Contract = new Trc20Contract(contract, from, wrapper);
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
                wrapper.transfer(from, to, amount.longValue());
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
    public void verify_text() {
        String str = "hello world";

        String hexString = Hex.toHexString(str.getBytes());

        log.debug(hexString);

        // 交易id（即交易哈希，通过Transaction.rawData计算SHA256得到）
        byte[] tid = Hash.sha256(Hex.decode(hexString));

        // 交易ID Hex
        log.debug("tid-hex:{}", Hex.toHexString(tid));

        /*byte[] digest = new Keccak.Digest256().digest(Hex.decode(hexString));
        log.debug("digest:{}", Hex.toHexString(digest));*/

        // 对 tid 进行签名
        byte[] sign = KeyPair.signTransaction(tid, wrapper.keyPair);
        log.debug("sign-hex：{}", Hex.toHexString(sign));

        // 所有者
        byte[] owner = Base58Check.base58ToBytes(from);

        // 签名
        boolean verify = SignatureValidator.verify(tid, sign, owner);
        log.info("签名结果：{}", verify);
    }

    /**
     * 验证交易签名
     */
    @Test
    public void verifyTransaction() throws IllegalException {
        // trx 个数
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // sun 个数
        BigDecimal sun = Convert.toSun(realAmount, Convert.Unit.TRX);

        // 远程构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(from, to, sun.longValue());

        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transfer);

        boolean verify = SignatureValidator.verify(
                ApiWrapper.calculateTransactionHash(transfer.getTransaction()),
                signTransaction.getSignature(0).toByteArray(),
                ApiWrapper.parseAddress(wrapper.keyPair.toBase58CheckAddress()).toByteArray()
        );

        if (verify) {
            // 广播
            String tid = wrapper.broadcastTransaction(signTransaction);
            log.debug(tid);
            return;
        }
        log.error("签名错误！");
    }

    /**
     * 解析trc20合约转账数据
     */
    @Test
    public void parseData() throws SmartParamDecodeException, FunctionSelectorException {
        String data = "a9059cbb0000000000000000000000001391667f4940d9f58d2779d92357e714f5bf3ea900000000000000000000000000000000000000000000000000000000005ffab4";
        TransferFunctionParam transferFunctionParam = Trc20ContractUtil.getTransferFunctionParam(data);
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
     * trx 与 sun 转换
     */
    @Test
    public void convert() {
        // 1 trx
        BigDecimal trx = BigDecimal.ONE;
        BigDecimal sunBalance = Convert.toSun(trx, Convert.Unit.TRX);
        log.debug("1trx={}sun", sunBalance);

        // 1000000 sun
        BigDecimal sun = BigDecimal.valueOf(1000000);
        BigDecimal trxBalance = Convert.fromSun(sun, Convert.Unit.TRX);
        log.debug("1000000sun={}trx", trxBalance);
    }


    @Test
    public void usdtName() {
        String res = "000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000045553445400000000000000000000000000000000000000000000000000000000";
        String name_res = res.substring(res.length() - 32).replaceAll("0", "");
        log.debug("name_res：{}", name_res);


        log.debug("name:{}", this.toStringHex("55534454"));

        // log.debug("{}", TronConverter.hexToInt("430e1b7"));

    }

    // 转化十六进制编码为字符串
    public String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, StandardCharsets.UTF_8);//UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

}
