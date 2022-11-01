package org.tron.easywork.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tron.easywork.enums.TransactionStatus;
import org.tron.easywork.enums.TransferType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-09-20 15:36
 */
@Getter
@Setter
@ToString
public class TransferInfo {

    /**
     * 交易哈希
     */
    private String id;

    /**
     * 转账类型
     */
    @Setter(AccessLevel.NONE)
    private TransferType transferType = TransferType.TRX;

    /**
     * 转出地址
     */
    private String from;

    /**
     * 转入地址
     */
    private String to;

    /**
     * 转账金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String memo;

    /**
     * 交易状态
     */
    private TransactionStatus status = TransactionStatus.UNKNOWN;

    /**
     * 上链时间
     */
    private Date broadcastTime;

    /**
     * 权限ID
     */
    private Integer permissionId;


    public TransferInfo() {
    }

    public TransferInfo(String from, String to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }


    /**
     * 检查合约目标（trc20合约地址|trc10资源名称）是否匹配
     * trx 对比任意目标返回true
     * 要求：子类必须重写本方法
     *
     * @param contractTarget 合约目标
     * @return 是否匹配
     */
    public boolean contractTargetEquals(Object contractTarget) {
        return true;
    }
}
