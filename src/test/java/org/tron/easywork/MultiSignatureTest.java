package org.tron.easywork;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.model.AccountInfo;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
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
 * <p>
 * 创建活跃权限后，getAccount查询账户信息，可以获得permissionId。
 * 如果是拥有者权限，无需改permissionId，直接多签。
 *
 * @author Admin
 * @version 1.0
 * @time 2022-11-02 17:59
 */
@Slf4j
public class MultiSignatureTest extends BaseTest {

    /**
     * 多签TRX转账 - trident原生 - 非本地构造交易
     */
    @Test
    public void multiSignature_trident() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，业务所致此处未用到私钥
        AccountInfo account = new AccountInfo("");

        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   具有某些活动权限
        AccountInfo account1 = new AccountInfo("");

        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   具有某些活动权限
        AccountInfo account2 = new AccountInfo("");

        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, account.getHexPrivateKey(), null);


        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);

        // 构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(
                        account.getBase58CheckAddress()
                        , to
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
     * 活跃权限多签
     * @throws IllegalException
     */
    @Test
    public void multiSign_trident() throws IllegalException {
        // 拥有者
        String ownerAddress = "TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF";
        // 拥有者 此单元测试没用到拥有者私钥
        // 仅用于构造ApiWrapper，随意使用任何私钥即可|此测试留空无碍
        KeyPair keyPair_owner = new KeyPair("");

        // 活跃权限账户
        KeyPair keyPair_active = new KeyPair("..................3f567257e188335b0db6a0fb970f6db5e3f9c");

        String toAddress = "TEczEK6uzD88888QhstH6QDwB167ZsXPrb";
        String contractAddress = "TFd1piJ8iXmJQicTicq4zChDSNSMLPFR4w";


        ApiWrapper apiWrapper = ApiWrapperFactory
                .create(ApiWrapperFactory.NetType.Shasta, keyPair_owner.toPrivateKey(), null);

        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(contractAddress, apiWrapper);

        BigDecimal amount = Convert.toSun("1", Convert.Unit.TRX);

        // 构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(
                        ownerAddress, toAddress, amount.longValue()
                );

        // 交易构造器
        Chain.Transaction.Builder transactionBuilder = transfer.getTransaction().toBuilder();
        // 使用3号活跃权限 | 活跃权限按顺序从2开始 | 保险可调 getAccount Api 查阅
        transactionBuilder.getRawDataBuilder().getContractBuilder(0).setPermissionId(3);

        // 交易
        Chain.Transaction transaction = transactionBuilder.build();

        // 签名
        Chain.Transaction signedTransaction = wrapper.signTransaction(transaction, keyPair_active);

        // 广播并返回交易ID
        String id = wrapper.broadcastTransaction(signedTransaction);
        log.debug(id);

    }

    /**
     * 更新账户权限，目前需要花费100trx
     */
    @Test
    public void permissionUpdate() throws IllegalException {
        //TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF
        String privateKey = "e9144533f0edf1a9f59025847db267572a741bf7604ed73d4ec156c12e474886";

        // 分配给 1
        String permissionAddress1 = "TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z";
        // 分配给 2
        String permissionAddress2 = "TEczEK6uzD88888QhstH6QDwB167ZsXPrb";

        ApiWrapper wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, privateKey, null);

        // 账户信息
        AccountInfo account = new AccountInfo(privateKey);
        // 我的地址 ByteString 格式
        ByteString ownerAddress = ByteString.copyFrom(Base58Check.base58ToBytes(account.getBase58CheckAddress()));

        // 指定权限列表，trident已经封装枚举类
        /*Integer[] contractId = {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 31,
                32, 33, 41, 42, 43, 44, 45};*/
        Integer[] contractId = {1, Chain.Transaction.Contract.ContractType.TriggerSmartContract_VALUE};

        List<Integer> list = new ArrayList<>(Arrays.asList(contractId));
        byte[] operations = new byte[32];
        list.forEach(e -> operations[e / 8] |= (1 << e % 8));

        // 构造权限分配信息
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
                                        // 需要权重满足才执行操作
                                        .setThreshold(10)
                                        // 添加权限、指定权重
                                        .addKeys(Common.Key.newBuilder().setAddress(ByteString.copyFrom(Base58Check.base58ToBytes(permissionAddress1))).setWeight(5))
                                        // 添加权限、指定权重
                                        .addKeys(Common.Key.newBuilder().setAddress(ByteString.copyFrom(Base58Check.base58ToBytes(permissionAddress2))).setWeight(5))
                                        .setOperations(ByteString.copyFrom(operations))
                        ).addActives(
                                org.tron.trident.proto.Common.Permission.newBuilder()
                                        .setType(Common.Permission.PermissionType.Active)
                                        .setPermissionName("active-demo")
                                        // 需要权重满足才执行操作
                                        .setThreshold(10)
                                        // 添加权限、指定权重
                                        .addKeys(Common.Key.newBuilder().setAddress(ByteString.copyFrom(Base58Check.base58ToBytes(permissionAddress1))).setWeight(10))
                                        .setOperations(ByteString.copyFrom(operations))
                        )
                        .build();

        // 发给节点，构造本次交易
        Response.TransactionExtention transactionExtention = wrapper.accountPermissionUpdate(accountPermissionUpdateContract);
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transactionExtention);
        // 广播
        String id = wrapper.broadcastTransaction(signTransaction);
        log.debug(id);
    }

    /**
     * 本地多签转账
     *
     * <p>
     * 296fc6ae7c8a61c0005b64d38b51c99623fb7475277ab2bbc0439b07f7a86afe
     */
    @Test
    public void multiSignature() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号，业务所致此处未用到私钥
        AccountInfo account = new AccountInfo("");

        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   具有某些活动权限
        AccountInfo account1 = new AccountInfo("");

        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   具有某些活动权限
        AccountInfo account2 = new AccountInfo("");

        // 到账地址
        String fromAddress = account.getBase58CheckAddress();
        // 实际转账金额
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // 合约
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(contractAddress, wrapper);
        // 系统转账金额
        BigDecimal transferAmount = trc20ContractInfo.getTransferAmount(realAmount);
        // TRC20转账
        Transfer transfer =
                Transfer.trc20TransferBuilder(fromAddress, to, transferAmount, trc20ContractInfo.getAddress())
                        // 矿工费限制
                        .feeLimit(Convert.toSun(BigDecimal.valueOf(20), Convert.Unit.TRX).longValue())
                        // 备注
                        .memo("备注：" + new Date())
                        // 设置权限ID
                        .permissionId(2)
                        .build();

        // 参考区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        ReferenceBlock referenceBlock = new ReferenceBlock(nowBlock.getBlockHeader());
        // trc20 转账处理器
        Trc20TransferHandler trc20TransferHandler = new Trc20TransferHandler();
        // 构造本地交易
        Chain.Transaction transaction = trc20TransferHandler.buildLocalTransfer(transfer, referenceBlock);
        // 账号1签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction, account1.getKeyPair());
        // 账号2签名
        signTransaction = wrapper.signTransaction(signTransaction, account2.getKeyPair());
        // 广播并返回ID
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
    }


    /**
     * 多签质押演示
     */
    @Test
    public void freezeBalance() throws IllegalException {
        // TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF   主账号(此处仅用到地址)
        AccountInfo account = new AccountInfo("");
        // TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z   具有某些活动权限
        AccountInfo account1 = new AccountInfo("");
        // TEczEK6uzD88888QhstH6QDwB167ZsXPrb   具有某些活动权限
        AccountInfo account2 = new AccountInfo("");

        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal transferAmount = Convert.toSun(amount, Convert.Unit.TRX);

        // 请求远程构造交易
        Response.TransactionExtention transactionExtention = wrapper.freezeBalance(
                account.getBase58CheckAddress(),
                transferAmount.longValue(),
                3,
                Common.ResourceCode.ENERGY_VALUE,
                "TBB3jfSew1ygkwhFf4Fjqq3LLSys77777P"
        );
        // 获取交易构造器
        Chain.Transaction.Builder builder = transactionExtention.getTransaction().toBuilder();
        // 设置权限ID
        builder.getRawDataBuilder().getContractBuilder(0).setPermissionId(2);
        // 构造交易
        Chain.Transaction transaction = builder.build();

        // B 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction, account1.getKeyPair());
        // C 签名
        signTransaction = wrapper.signTransaction(signTransaction, account2.getKeyPair());
        // 广播
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
    }

}
