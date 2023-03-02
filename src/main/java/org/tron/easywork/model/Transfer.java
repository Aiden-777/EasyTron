package org.tron.easywork.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tron.easywork.enums.TransactionStatus;
import org.tron.easywork.enums.TransferType;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-12 01:00
 */
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class Transfer {


    /**
     * 交易哈希
     */
    private String id;

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
     * 转账类型
     */
    private TransferType transferType;

    /**
     * 交易状态
     */
    private TransactionStatus status;

    /**
     * 权限ID
     */
    private Integer permissionId;

    /**
     * 创建时间
     */
    private Long timestamp;

    /**
     * 过期时间
     */
    private Long expiration;

    /**
     * TRC10 资源名称
     */
    private BigInteger assetName;

    /**
     * TRC20合约地址
     */
    private String contractAddress;

    /**
     * TRC20 矿工费限制 - 单位sum
     */
    private Long feeLimit;


    /**
     * 上链时间
     */
    private Long broadcastTime;

    /**
     * 区块哈希
     */
    private String blockId;

    /**
     * 区块高度
     */
    private Long blockHeight;


    /**
     * 创建TRC20转账构建者
     *
     * @param from            来源地址/转出地址
     * @param to              到账地址
     * @param amount          金额
     * @param contractAddress 合约地址
     * @return TRC20转账构建者
     */
    public static TransferBuilder trc20TransferBuilder(String from, String to, BigDecimal amount, String contractAddress) {
        return Transfer.builder().from(from).to(to).amount(amount).transferType(TransferType.TRC20).contractAddress(contractAddress);
    }

    /**
     * 创建TRX转账构建者
     *
     * @param from   来源地址/转出地址
     * @param to     到账地址
     * @param amount 金额
     * @return TRX转账构建者
     */
    public static TransferBuilder trxTransferBuilder(String from, String to, BigDecimal amount) {
        return Transfer.builder().from(from).to(to).amount(amount).transferType(TransferType.TRX);
    }

    /**
     * 创建TRC10转账构建者
     *
     * @param from      来源地址/转出地址
     * @param to        到账地址
     * @param amount    金额
     * @param assetName TRC10 资源名称
     * @return TRC10转账构建者
     */
    public static TransferBuilder trc10TransferBuilder(String from, String to, BigDecimal amount, BigInteger assetName) {
        return Transfer.builder().from(from).to(to).amount(amount).transferType(TransferType.TRX).assetName(assetName);
    }

}
