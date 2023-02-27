package org.tron.easywork;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.easywork.util.TronConverter;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

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
        String address = from;
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
}
