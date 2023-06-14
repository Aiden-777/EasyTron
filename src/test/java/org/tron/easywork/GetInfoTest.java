package org.tron.easywork;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.util.TransactionUtil;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.easywork.util.TronConverter;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 从链上获取信息
 *
 * @author Admin
 * @version 1.0
 * @time 2022-11-02 18:54
 */
@Slf4j
public class GetInfoTest extends BaseTest {


    /**
     * # ApiWrapper原装 - 获取trc20余额
     */
    @Test
    public void simple_balanceOfTrc20() {
        // 查询的地址
        String address = "TKjPqKq77777FPKUdLRMPNUWtU4jNEpUQF";
        // 获取合约信息
        Contract contract = wrapper.getContract(contractAddress);
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
     * 获取trc20余额
     */
    @Test
    public void balanceOfTrc20() {
        // 地址
        String address = "TP6QorvxAJ4bXg21LterCpGi5oZ2PxybCZ";
        BigDecimal transferAmount = Trc20ContractUtil.trc20BalanceOf(contractAddress, address, wrapper);
        log.debug("Trc20余额:{}", transferAmount);

        // 合约信息
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(contractAddress, wrapper);
        BigDecimal realAmount = trc20ContractInfo.getRealAmount(transferAmount).stripTrailingZeros();
        log.debug("Trc20真实余额：{}个", realAmount);
    }

    /**
     * # ApiWrapper原装 - 获取trx余额
     */
    @Test
    public void simple_balanceOfTrx() {
        // 地址
        String address = from;
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
     * 检查账户是否具有权限
     */
    @Test
    public void accountPermissionUpdateContract() throws IllegalException, InvalidProtocolBufferException {
        String checkAddress="TBjxJTNwZeaKrbHyDum5Rwj1xU99999n8Z";
        Chain.Transaction transaction = wrapper.getTransactionById("3ccabcef02f4c0679f811a1600ad3c4ac1977859d7fed85a81371f48032df274");
        // 检查交易是否成功
        boolean status = TransactionUtil.isTransactionSuccess(transaction);
        if (!status) {
            return;
        }
        // 合约
        Chain.Transaction.Contract contract = transaction.getRawData().getContract(0);
        // 合约类型
        Chain.Transaction.Contract.ContractType contractType = contract.getType();
        // parameter
        Any parameter = contract.getParameter();
        if (contractType == Chain.Transaction.Contract.ContractType.AccountPermissionUpdateContract) {

            log.warn("合约类型：{}", contractType.name());
            // 解码

            org.tron.trident.proto.Contract.AccountPermissionUpdateContract accountPermissionUpdateContract = parameter.unpack(org.tron.trident.proto.Contract.AccountPermissionUpdateContract.class);

            // 发送人
            byte[] fromAddressBs = accountPermissionUpdateContract.getOwnerAddress().toByteArray();
            String fromAddress = Base58Check.bytesToBase58(fromAddressBs);

            // 所有者权限
            Common.Permission owner = accountPermissionUpdateContract.getOwner();
            // 拥有者权限所需权重
            long thresholdOfOwner = owner.getThreshold();

            // 拥有者
            List<Common.Key> keysList = owner.getKeysList();

            log.debug("开始检查拥有者权限");
            for (Common.Key key : keysList) {
                // 是否包含当前账户
                if (Base58Check.bytesToBase58(key.getAddress().toByteArray()).equals(checkAddress)) {
                    long weight = key.getWeight();
                    if (weight >= thresholdOfOwner) {
                        log.debug("权限到账，拥有者权限，权重：{}，可完全支配", weight);
                    }
                    else {
                        log.warn("收到拥有者权限指定，但权重不足，所需权重{}，目前拥有：{}", thresholdOfOwner, weight);
                    }
                    return;
                }
            }
            log.debug("开始检查活跃权限");
            List<Common.Permission> activesList = accountPermissionUpdateContract.getActivesList();
            for (Common.Permission permission : activesList) {
                log.debug("--活跃权限指定，id:{}", permission.getId());
                // 当前活跃权限所需权重
                long thresholdOfActive = permission.getThreshold();
                // 当前活跃权限用户列表
                List<Common.Key> keyList = permission.getKeysList();

                // 权限可以操作的功能列表operations
                byte[] operations = permission.getOperations().toByteArray();

                for (Common.Key key : keyList) {
                    // 是否包含当前账户
                    if (Base58Check.bytesToBase58(key.getAddress().toByteArray()).equals(checkAddress)) {
                        // 权重
                        long weight = key.getWeight();


                        if (weight >= thresholdOfActive) {
                            log.debug("收到活跃权限指定，id:{}，权重：{}，可部分支配", permission.getId(), weight);
                            log.debug("权限名：{}，权限ID：{}", permission.getPermissionName(), permission.getId());

                            // 检查权限列表是否包含【触发智能合约】、【TRX转账】
                            List<Integer> contractIdList = new ArrayList<>();

                            for (int i = 0; i < operations.length; i++) {
                                byte operation = operations[i];
                                for (int j = 0; j < 8; j++) {
                                    if ((operation & (1 << j)) != 0) {
                                        contractIdList.add(i * 8 + j);
                                    }
                                }
                            }
                            log.debug(contractIdList.toString());
                            if (contractIdList.size() > 0) {
                                for (Integer value : contractIdList) {
                                    if (value == 1) {
                                        log.debug("TRX转账");
                                    }
                                    else if (value == 31) {
                                        log.debug("Smart Contract Trigger (TRC20/TRC721/TRC1155 Transfer)");
                                    }
                                }
                            }
                        }
                        else {
                            log.warn("收到活跃权限指定，id:{}，但权重不足，所需权重{}，目前拥有：{}",
                                    permission.getId(), thresholdOfActive, weight);
//                            return;
                        }
                    }
                }
            }
        }
    }
}
