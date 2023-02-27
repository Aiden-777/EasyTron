package org.tron.easywork;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-11-02 17:47
 */
@Slf4j
public class TransferTest extends BaseTest {

    /**
     * trc20 转账 - trident原生 - 非本地构造交易
     */
    @Test
    public void transferTrc20_trident() {
        // 真实金额 - 单位个
        long realAmount = 11;

        // 根据合约地址获取合约信息
        org.tron.trident.core.contract.Contract contract = wrapper.getContract(contractAddress);
        // 构造trc20合约
        Trc20Contract trc20Contract = new Trc20Contract(contract, from, wrapper);

        // trc20 合约转账
        String tid = trc20Contract.transfer(
                to,
                realAmount,
                // 精度
                trc20Contract.decimals().intValue(),
                "备注",
                Convert.toSun("10", Convert.Unit.TRX).longValue()
        );
        log.debug(tid);
    }

    /**
     * trx 转账 - trident原生 - 非本地构造交易
     */
    @Test
    public void transferTrx_trident() throws IllegalException {
        // trx 个数
        BigDecimal realAmount = BigDecimal.valueOf(1);
        // sun 个数
        BigDecimal sun = Convert.toSun(realAmount, Convert.Unit.TRX);

        // 远程构造交易
        Response.TransactionExtention transfer =
                wrapper.transfer(from, to, sun.longValue());
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transfer);
        // 广播
        String tid = wrapper.broadcastTransaction(signTransaction);
        log.debug(tid);
    }

}
