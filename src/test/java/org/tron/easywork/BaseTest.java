package org.tron.easywork;

import lombok.extern.slf4j.Slf4j;
import org.tron.easywork.handler.transfer.TransferHandlerContext;
import org.tron.easywork.handler.transfer.Trc10TransferHandler;
import org.tron.easywork.handler.transfer.Trc20TransferHandler;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-12 08:56
 */
@Slf4j
public class BaseTest {

    /**
     * 私钥
     */
    protected String key = "";
    /**
     * trident API 包装器
     */
    protected ApiWrapper wrapper = ApiWrapper.ofShasta(key);
    /**
     * 转出地址
     */
    protected String from = wrapper.keyPair.toBase58CheckAddress();
    /**
     * 到账地址
     */
    protected String to = "TVvSx8zgvqcc8nWuYArLfdkqerAWHibf78";
    /**
     * TRC20合约地址
     */
    protected String contractAddress = "TFd1piJ8iXmJQicTicq4zChDSNSMLPFR4w";


    /**
     * 获取引用区块
     *
     * @param wrapper trident api
     * @return 引用区块信息
     * @throws IllegalException 参数错误
     */
    protected ReferenceBlock getReferenceBlock(ApiWrapper wrapper) throws IllegalException {
        // api 获取最新块
        Chain.Block nowBlock = wrapper.getNowBlock();
        return new ReferenceBlock(nowBlock.getBlockHeader());
    }


    /**
     * 创建TRC20转账
     */
    protected Transfer createTrc20Transfer(String from, String to, BigDecimal amount) {
        // 手续费限制，单位sum
        long feeLimit = Convert.toSun(BigDecimal.valueOf(50), Convert.Unit.TRX).longValue();
        return Transfer.trc20TransferBuilder(from, to, amount, contractAddress).feeLimit(feeLimit).memo("备注：TRC20转账").build();
    }

    /**
     * 发送构造好的交易
     *
     * @param transaction 交易
     */
    protected void sendTransaction(Chain.Transaction transaction) {
        // 签名
        Chain.Transaction signTransaction = wrapper.signTransaction(transaction);
        // 广播
        String tid = wrapper.broadcastTransaction(signTransaction);
        // 打印交易ID
        log.debug("交易ID：{}", tid);
    }

    /**
     * 转账处理器上下文
     */
    protected TransferHandlerContext createTransferHandlerContext() {
        TransferHandlerContext context = new TransferHandlerContext();
        context.addHandler(TrxTransferHandler.class.getName(), new TrxTransferHandler());
        context.addHandler(Trc20TransferHandler.class.getName(), new Trc20TransferHandler());
        context.addHandler(Trc10TransferHandler.class.getName(), new Trc10TransferHandler());
        return context;
    }
}
