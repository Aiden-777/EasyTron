package org.tron.easywork.handler.transfer;

import org.tron.easywork.util.TransactionUtil;
import org.tron.trident.proto.Chain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-12 07:30
 */
public class TransferHandlerContext {

    private final HashMap<String, TransferHandler> handlers = new HashMap<>();

    public TransferHandlerContext() {
    }

    public TransferHandlerContext(Map<String, TransferHandler> handlers) {
        this.handlers.putAll(handlers);
    }

    public void addHandler(String name, TransferHandler handler) {
        handlers.put(name, handler);
    }

    public TransferHandler getHandler(String name) {
        return handlers.get(name);
    }

    public TransferHandler getHandler(Chain.Transaction.Contract.ContractType contractType) {
        return handlers.values().stream().filter(handler -> handler.supportContractType(contractType)).findFirst().orElse(null);
    }

    public TransferHandler getHandler(Chain.Transaction transaction) {
        Chain.Transaction.Contract.ContractType contractType = TransactionUtil.getFirstContractType(transaction);
        return handlers.values().stream().filter(handler -> handler.supportContractType(contractType)).findFirst().orElse(null);
    }

}
