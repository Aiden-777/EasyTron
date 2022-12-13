package org.tron.easywork;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.handler.transfer.Trc10TransferHandler;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc10TransferInfo;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.easywork.util.TronConverter;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
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
     * 获取trx 转账信息
     */
    @Test
    public void getTrxTransferInfo() throws IllegalException {
        // 交易ID
        String tid = "a3d24f7aa01ec16bde36370f8a446bde7b5e1595cf6826fae7a4850126509f60";
        // 此处使用主网内容
        wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Mainnet, privateKey, apiKey);
        // 获取交易
        Chain.Transaction transaction = wrapper.getTransactionById(tid);
        // 处理器
        TrxTransferHandler handler = new TrxTransferHandler();

        try {
            // 解析交易
            TransferInfo transferInfo = handler.parse(transaction);
            log.debug(transferInfo.toString());
        } catch (InvalidProtocolBufferException e) {
            // 原始数据解包异常
            throw new RuntimeException(e);
        } catch (SmartParamDecodeException e) {
            // 只能合约参数解码异常
            throw new RuntimeException(e);
        } catch (FunctionSelectorException e) {
            // 函数选择器 错误异常，这个可以忽略，程序无需处理
            throw new RuntimeException(e);
        } catch (Exception e) {
            // 兜底异常，如果出现异常，请及时处理，并提交 issue
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取trc20 转账信息
     */
    @Test
    public void getTrc20TransferInfo() throws IllegalException, FunctionSelectorException, InvalidProtocolBufferException, SmartParamDecodeException {
        // 交易ID
        String tid = "45a28c9e00bb84bf40fc913ecaa8e88739bf419996fbb9f8cfaeb2f0ac91b791";
        // 此处使用主网内容
        wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Mainnet, privateKey, apiKey);
        // 获取交易
        Chain.Transaction transaction = wrapper.getTransactionById(tid);
        // 处理器
        Trc20TransferHandler handler = new Trc20TransferHandler();
        // 解析交易
        Trc20TransferInfo transferInfo = (Trc20TransferInfo) handler.parse(transaction);
        log.debug(transferInfo.toString());
    }

    /**
     * 获取trc10 转账信息
     */
    @Test
    public void getTrc10TransferInfo() throws IllegalException, FunctionSelectorException, InvalidProtocolBufferException, SmartParamDecodeException {
        // 交易ID
        String tid = "4debbf7da091e6c03557c494278a199643221566def90c33344bd2627cffda46";
        // 此处使用主网内容
        wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Mainnet, privateKey, apiKey);
        // 获取交易
        Chain.Transaction transaction = wrapper.getTransactionById(tid);
        // 处理器
        Trc10TransferHandler handler = new Trc10TransferHandler();
        // 解析交易
        Trc10TransferInfo transferInfo = (Trc10TransferInfo) handler.parse(transaction);
        log.debug(transferInfo.toString());
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
     * 获取trc20余额
     */
    @Test
    public void balanceOfTrc20() {
        // 地址
        String address = "TP6QorvxAJ4bXg21LterCpGi5oZ2PxybCZ";
        BigDecimal transferAmount = Trc20ContractUtil.trc20BalanceOf(testContractAddress, address, wrapper);
        log.debug("Trc20余额:{}", transferAmount);

        // 合约信息
        Trc20ContractInfo trc20ContractInfo = Trc20ContractUtil.readTrc20ContractInfo(testContractAddress, wrapper);
        BigDecimal realAmount = trc20ContractInfo.getRealAmount(transferAmount).stripTrailingZeros();
        log.debug("Trc20真实余额：{}个", realAmount);
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
}
