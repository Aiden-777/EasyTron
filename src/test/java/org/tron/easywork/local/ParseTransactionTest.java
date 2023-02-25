package org.tron.easywork.local;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.handler.transfer.TransferHandler;
import org.tron.easywork.handler.transfer.TransferHandlerContext;
import org.tron.easywork.handler.transfer.TrxTransferHandler;
import org.tron.easywork.model.Transfer;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;

/**
 * 解析转账交易内容
 *
 * @author Admin
 * @version 1.0
 * @time 2023-02-12 09:02
 */
@Slf4j
public class ParseTransactionTest extends BaseTest {


    /**
     * 解析转账交易信息(通过Context)
     *
     * @throws IllegalException               api参数错误
     * @throws FunctionSelectorException      TRC20转账函数选择器错误
     * @throws InvalidProtocolBufferException unpack解包异常
     * @throws SmartParamDecodeException      ABI解码错误
     */
    @Test
    public void parseTransactionByContext() throws IllegalException, FunctionSelectorException, InvalidProtocolBufferException, SmartParamDecodeException {
        // 转账处理器上下文
        TransferHandlerContext context = this.createTransferHandlerContext();

        // 交易ID
        String tid = "c2cc785e07a75b6d62d6f6b0b3bb69369dbbce1c9d780163740a48cb8efa9393";
        // 交易
        Chain.Transaction transaction = wrapper.getTransactionById(tid);
        // 获取转账处理器
        TransferHandler handler = context.getHandler(transaction);
        if (handler == null) {
            return;
        }
        // 转账信息
        Transfer transfer = handler.parse(transaction);
        // 打印转账信息
        log.debug(transfer.toString());


        // 交易ID
        tid = "4b29d9ad2aef43a4ec772f68b4b91e342e083d2a6492fa151ead8553bab63357";
        // 交易
        transaction = wrapper.getTransactionById(tid);
        // 获取转账处理器
        handler = context.getHandler(transaction);
        if (handler == null) {
            return;
        }
        // 转账信息
        transfer = handler.parse(transaction);
        // 打印转账信息
        log.debug(transfer.toString());
    }

    /**
     * 指定解析转账交易，不推荐使用，除非业务需求仅处理某一种类型的转账。
     */
    @Test
    public void parseTransaction() throws IllegalException, FunctionSelectorException, InvalidProtocolBufferException, SmartParamDecodeException {
        // 系统中定义了一个单例TRX转账处理器
        TrxTransferHandler handler = new TrxTransferHandler();
        // 交易ID
        String tid = "220e268edc6dbca2b100cb63d2cab0bf2f0f36ad7cb746fee699985c0d1f438a";
        // 查询交易
        Chain.Transaction transaction = wrapper.getTransactionById(tid);
        // 检查这个交易是否被 TRX转账处理器 所支持
        if (handler.supportContractType(transaction)) {
            Transfer transfer = handler.parse(transaction);
            log.debug(transfer.toString());
        }
        // 如果此处要搞 else if 建议查看上面使用Context的例子
        else {
            log.warn("仅支持 TRX转账 ！");
        }
    }

}
