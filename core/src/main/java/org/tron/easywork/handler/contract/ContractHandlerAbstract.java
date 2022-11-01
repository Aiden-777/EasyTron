package org.tron.easywork.handler.contract;

import org.tron.trident.core.ApiWrapper;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-23 10:03
 */
@Deprecated
public abstract class ContractHandlerAbstract implements ContractHandlerInterface {

    protected final ApiWrapper wrapper;

    public ContractHandlerAbstract(ApiWrapper wrapper) {
        this.wrapper = wrapper;
    }
}
