package org.tron.easywork;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.handler.contract.TransferContractHandler;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.model.AccountInfo;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.Trc20Utils;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 多签相关
 *
 * @author Admin
 * @version 1.0
 * @time 2022-11-02 17:59
 */
@Slf4j
public class MultiSignatureTest extends BaseTest {

    /**
     * 本地多签转账
     *
     * <p>
     * 296fc6ae7c8a61c0005b64d38b51c99623fb7475277ab2bbc0439b07f7a86afe
     */
    // @Test
    public void multiSignature() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，业务所致此处未用到私钥
        AccountInfo account = new AccountInfo("");

        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   Trx转账权限
        AccountInfo account1 = new AccountInfo("");

        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   Trx转账权限
        AccountInfo account2 = new AccountInfo("");

        // 到账地址
        String fromAddress = account.getBase58CheckAddress();
        // 实际转账金额
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // 合约
        Trc20ContractInfo trc20ContractInfo = Trc20Utils.readTrc20ContractInfo(testContractAddress, wrapper);
        // 系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(realAmount);
        // trc20交易
        Trc20TransferInfo transferInfo = new Trc20TransferInfo(fromAddress, toAddress, transferAmount, trc20ContractInfo.getAddress());
        // 矿工费限制
        transferInfo.setFeeLimit(Convert.toSun(BigDecimal.valueOf(20), Convert.Unit.TRX).longValue());
        // 备注
        transferInfo.setMemo("备注：" + new Date());
        // 设置权限ID
        transferInfo.setPermissionId(2);

        // 参考区块
        Chain.Block refBlock = wrapper.getNowBlock();
        // trc20 转账处理器
        Trc20TransferHandler trc20TransferHandler = new Trc20TransferHandler();
        // 构造本地交易
        Chain.Transaction transaction = trc20TransferHandler.buildLocalTransfer(transferInfo, refBlock.getBlockHeader());
        // 账号1签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction, account1.getKeyPair());
        // 账号2签名
        signTransaction = wrapper.signTransaction(signTransaction, account2.getKeyPair());
        // 广播并返回ID
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
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


        Contract.AccountPermissionUpdateContract accountPermissionUpdateContract =
                Contract.AccountPermissionUpdateContract.newBuilder()
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
     * 多签 - trident原生 - 非本地构造交易
     */
    // @Test
    public void multiSignature_trident() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，业务所致此处未用到私钥
        AccountInfo account = new AccountInfo("");

        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   Trx转账权限
        AccountInfo account1 = new AccountInfo("");

        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   Trx转账权限
        AccountInfo account2 = new AccountInfo("");

        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, account.getHexPrivateKey(), null);


        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);

        // 构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(
                        account.getBase58CheckAddress()
                        , toAddress
                        , amount.longValue()
                );

        // 交易构造器
        Chain.Transaction.Builder transactionBuilder = transfer.getTransaction().toBuilder();
        // 使用2号活跃权限
        transactionBuilder.getRawDataBuilder().getContractBuilder(0).setPermissionId(2);

        // 交易
        Chain.Transaction transaction = transactionBuilder.build();

        // 签名
        Chain.Transaction sign1 = wrapper.signTransaction(transaction, account1.getKeyPair());
        Chain.Transaction sign2 = wrapper.signTransaction(sign1, account2.getKeyPair());

        /*byte[] rawData = transactionBuilder.getRawData().toByteArray();
        byte[] tidByte = Hash.sha256(rawData);

        byte[] sign1 = KeyPair.signTransaction(tidByte, account1.getKeyPair());
        byte[] sign2 = KeyPair.signTransaction(tidByte, account2.getKeyPair());

        Chain.Transaction transaction = transactionBuilder
                .addSignature(ByteString.copyFrom(sign1))
                .addSignature(ByteString.copyFrom(sign2))
                .build();*/

        // 广播并返回交易ID
        String id = wrapper.broadcastTransaction(sign2);
        log.debug(id);

    }

    /**
     * 封装多签 - 非本地构造交易 - 过时的
     */
    // @Test
    public void multiSignature2_trident() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，业务所致此处未用到私钥
        AccountInfo account = new AccountInfo("");

        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   Trx转账权限
        AccountInfo account1 = new AccountInfo("");

        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   Trx转账权限
        AccountInfo account2 = new AccountInfo("");

        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, account.getHexPrivateKey(), null);

        // 金额 sun
        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);
        // trx 转账信息
        TransferInfo transferInfo = new TransferInfo(account.getBase58CheckAddress(), toAddress, amount);
        transferInfo.setMemo("多签备注");
        // trx 转账处理器
        TransferContractHandler transferContractHandler = new TransferContractHandler(wrapper);
        // 多签转账
        String id = transferContractHandler.transfer(transferInfo, Arrays.asList(account1.getKeyPair(), account2.getKeyPair()), 2);

        /*// trc20 转账
        BigDecimal transferAmount = TronConverter.getTransferAmount(BigDecimal.ONE, 6);
        TransferInfo transferInfo = new Trc20TransferInfo(account.getBase58CheckAddress(),toAddress, transferAmount, "TFd1piJ8iXmJQicTicq4zChDSNSMLPFR4w");
        transferInfo.setMemo("多签备注");
        TriggerSmartContractHandler triggerSmartContractHandler = new TriggerSmartContractHandler(wrapper);
        String id = triggerSmartContractHandler.transfer(transferInfo, Arrays.asList(account2.getKeyPair(), account3.getKeyPair()), 2);*/

        log.debug(id);

    }

}
