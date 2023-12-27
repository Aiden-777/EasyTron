package org.tron.easywork;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

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
                // feeLimit
                Convert.toSun("50", Convert.Unit.TRX).longValue()
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


    /**
     * trc20 转账 - trident原生 - 非本地构造交易
     * 小数点转账，复制出trident源码，加以修改
     */
    @Test
    public void transferTrc20_trident_custom() {
        // 真实金额 - 单位个
        String realAmount = "2.333";

        // 根据合约地址获取合约信息
        org.tron.trident.core.contract.Contract contract = wrapper.getContract(contractAddress);
        // 构造trc20合约
        Trc20Contract trc20Contract = new Trc20Contract(contract, from, wrapper);

        // 精度
        int decimals = trc20Contract.decimals().intValue();
        // 手续费
        long feeLimit = Convert.toSun("50", Convert.Unit.TRX).longValue();
        // 实际转账金额（trc20不可分割单位）
        BigInteger transferAmount = new BigDecimal(realAmount)
                .multiply(BigDecimal.TEN.pow(decimals))
                .toBigInteger();

        Function transfer = new Function("transfer",
                Arrays.asList(new Address(to),
                        new Uint256(transferAmount)),
                Arrays.asList(new TypeReference<Bool>() {
                }));

        TransactionBuilder builder = wrapper.triggerCall(from, contractAddress, transfer);
        builder.setFeeLimit(feeLimit);
        builder.setMemo("备注");

        Chain.Transaction signedTxn = wrapper.signTransaction(builder.build());
        String tid = wrapper.broadcastTransaction(signedTxn);
        log.debug(tid);
    }

}
