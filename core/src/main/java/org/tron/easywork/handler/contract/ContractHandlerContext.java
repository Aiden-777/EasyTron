package org.tron.easywork.handler.contract;

import lombok.extern.slf4j.Slf4j;
import org.tron.trident.proto.Chain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-02 15:05
 */
@Slf4j
@Deprecated
public class ContractHandlerContext {

    private final Map<String, ContractHandlerInterface> contractHandlers = new ConcurrentHashMap<>();

    public ContractHandlerContext() {
    }

    public ContractHandlerContext(Map<String, ContractHandlerInterface> contractHandlers) {
        this.contractHandlers.putAll(contractHandlers);
    }

    public ContractHandlerInterface getHandler(Chain.Transaction.Contract.ContractType contractType) {
        String handlerName = contractType.name() + "Handler";
        return contractHandlers.get(handlerName);
    }

    public void addHandler(String name, ContractHandlerInterface contractHandlerInterface) {
        contractHandlers.put(name, contractHandlerInterface);
    }

}
