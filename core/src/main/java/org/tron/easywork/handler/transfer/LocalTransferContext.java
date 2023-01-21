package org.tron.easywork.handler.transfer;

import org.tron.easywork.enums.TransferType;
import org.tron.trident.proto.Chain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-31 12:30
 */
public class LocalTransferContext {


    private final Map<String, BaseTransferHandler> transferHandlers = new ConcurrentHashMap<>();


    public LocalTransferContext() {
    }

    public LocalTransferContext(Map<String, BaseTransferHandler> transferHandlers) {
        this.transferHandlers.putAll(transferHandlers);
    }

    public void addHandler(String name, BaseTransferHandler transferHandler) {
        transferHandlers.put(name, transferHandler);
    }

    public BaseTransferHandler getHandler(String handlerName) {
        return transferHandlers.get(handlerName);
    }

    public BaseTransferHandler getHandler(TransferType transferType) {
        return transferHandlers.values().stream().filter(e -> e.getTransferType() == transferType).findFirst().orElse(null);
    }

    public BaseTransferHandler getHandler(Chain.Transaction.Contract.ContractType contractType) {
        return transferHandlers.values().stream().filter(e -> e.getContractType() == contractType).findFirst().orElse(null);
    }

}
