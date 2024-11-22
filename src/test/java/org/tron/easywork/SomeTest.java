package org.tron.easywork;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.AccountInfo;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.model.TransferFunctionParam;
import org.tron.easywork.util.AccountUtils;
import org.tron.easywork.util.TransferUtil;
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
     * 创建新账户
     */
    @Test
    public void createAccount() {
        AccountInfo newAccount = new AccountInfo(KeyPair.generate());
        log.debug(newAccount.toString());
    }

    /**
     * 私钥对应的账户详情
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
     * 交易签名过程演示
     */
    @Test
    public void signApiTransaction() throws InvalidProtocolBufferException {
        // 手动从API构造的交易 https://api.shasta.trongrid.io/wallet/createtransaction
        // 这串hex格式字符串中包含了所有交易信息
        String raw_data_hex = "0a023e9d2208290ec6ce431394c44085bcb0fab4325225e5a487e6b3a8efbc9a546875204e6f762032312031353a31363a34382043535420323032345ab001081f12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a15416b147a03de98c2d2079753875344179dbcd49db81215413dff1aae5b4979a0067b001afdb34a9758ecaa832244a9059cbb0000000000000000000000004d8239a7557fc50472586547b555ecf529f0665400000000000000000000000000000000000000000000000000000000000f424028027085d4d2ecb432900180dac409";
        // 解码
        byte[] raw_data = Hex.decode(raw_data_hex);
        // 交易id（即交易哈希，通过Transaction.rawData计算SHA256得到）
        byte[] txId = Hash.sha256(raw_data);
        String txIdHex = Hex.toHexString(txId);
        log.debug("txId:{}", txIdHex);

        // 构造raw对象
        Chain.Transaction.raw raw = Chain.Transaction.raw.parseFrom(raw_data);

        // ---↓ 如果改变原始交易数据，比如添加备注，此操作需要重新计算交易ID，否则会“签了个寂寞”
        raw = raw.toBuilder()
                // 添加备注
                .setData(ByteString.copyFromUtf8("hello"))
                .build();
        // 重新计算交易id
        txId = Hash.sha256(raw.toByteArray());
        txIdHex = Hex.toHexString(txId);
        log.debug("new txId:{}", txIdHex);
        // ---↑ 以上只是提醒你修改原始数据，交易ID也会改变，而签名签的就是交易ID

        // 对交易ID进行签名
        byte[] sign = KeyPair.signTransaction(txId, new KeyPair("your private key"));
        // 构造交易对象（设置 raw数据、签名数据 ）
        Chain.Transaction signedTransaction =
                Chain.Transaction.newBuilder()
                        .setRawData(raw)
                        // 添加签名
                        .addSignature(ByteString.copyFrom(sign))
                        .build();

        // 你也可以这样签名（在删掉 addSignature 的情况下，除非是多签）
        // 项目中一般都多用这种方式
        // Chain.Transaction signedTransaction =  wrapper.signTransaction(unsignedTransaction, new KeyPair("your private key"));

        // 广播并返回 交易ID
        String id = wrapper.broadcastTransaction(signedTransaction);
        log.debug(id);
    }


    /**
     * 对前端发来的多签交易验证并签名
     * 前端创建交易，后端验证并签名，将签名发给前端组装广播
     */
    @Test
    public void signRawDataHex() throws InvalidProtocolBufferException {
        // 将交易参数发给前端
        // ……

        // 前端根据参数构造交易，并将 raw_data_hex 发到此处
        String raw_data_hex = "0a02be69220876fa7aba2bcc89f54090ebfc9bb5325ab001081f12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a15416b147a03de98c2d2079753875344179dbcd49db81215413dff1aae5b4979a0067b001afdb34a9758ecaa832244a9059cbb0000000000000000000000004d8239a7557fc50472586547b555ecf529f0665400000000000000000000000000000000000000000000000000000000000f4240280270eaa1f99bb532900180ade204";
        // hex解码
        byte[] raw_data = Hex.decode(raw_data_hex);

        // 构造raw对象
        Chain.Transaction.raw raw = Chain.Transaction.raw.parseFrom(raw_data);

        // 开始校验前端是否按照指定交易信息构造交易
        // 合约信息
        Chain.Transaction.Contract contract = raw.getContract(0);
        // 检查权限id
        boolean permissionIdCheck = contract.getPermissionId() == 2;
        // 检查交易类型
        Chain.Transaction.Contract.ContractType contractType = contract.getType();
        boolean typeCheck = contract.getType() == Chain.Transaction.Contract.ContractType.TransferContract;

        // 解码并检查合约中的详细参数（纯演示，无具体逻辑。正常是比较复杂的，应采用策略模式或提取到不同的方法中去处理）
        Any parameter = contract.getParameter();
        if (contractType == Chain.Transaction.Contract.ContractType.TriggerSmartContract) {
            try {
                log.debug("这是智能合约交易");
                // 解码
                org.tron.trident.proto.Contract.TriggerSmartContract triggerSmartContract =
                        parameter.unpack(org.tron.trident.proto.Contract.TriggerSmartContract.class);
                // 获取交易详情
                Transfer transfer = TransferUtil.getTransferInfo(triggerSmartContract);
                log.debug("类型：{}\t到账地址：{}\t金额：{}", transfer.getTransferType(), transfer.getTo(), transfer.getAmount());
                // 检查......
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
            try {
                log.debug("这是TRX交易");
                org.tron.trident.proto.Contract.TransferContract unpack = parameter.unpack(org.tron.trident.proto.Contract.TransferContract.class);
                Transfer transferInfo = TransferUtil.getTransferInfo(unpack);
                // 检查......
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }

        // 假设检查无误，继续后续步骤

        // 交易id（即交易哈希，通过Transaction.rawData计算SHA256得到）
        byte[] txId = Hash.sha256(raw_data);
        String txIdHex = Hex.toHexString(txId);
        log.debug("txIdHex:{}", txIdHex);

        // 对 txid 进行签名
        byte[] sign = KeyPair.signTransaction(txId, new KeyPair("your private key"));

        String signHex = Hex.toHexString(sign);
        log.debug("================================");
        log.debug("签名发给前端：");
        log.debug(signHex);
        log.debug("================================");

        // 前端将签名组装后广播，得到链上答复后通知此处后端
        // 前端对我说说：“交易已经正确广播”
        // 开始监听交易id，如真实在链上发现此交易id，则代表订单已经完成
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
     * 验证交易签名（前端tronweb给的签名无法使用该方法验证，研究无果）
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
     * 解码TRC20合约转账数据
     * 到账地址 + 转账金额
     */
    @Test
    public void parseData() throws SmartParamDecodeException, FunctionSelectorException {
        String data = "a9059cbb0000000000000000000000001391667f4940d9f58d2779d92357e714f5bf3ea900000000000000000000000000000000000000000000000000000000005ffab4";
        TransferFunctionParam transferFunctionParam = Trc20ContractUtil.getTransferFunctionParam(data);
        log.debug(transferFunctionParam.toString());
    }

    /**
     * 验证trc20交易是否成功
     * 此处演示的txId均来自shasta测试网
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
            s = new String(baKeyword, StandardCharsets.UTF_8);// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

}
