package org.tron.easywork.constant;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;

/**
 * Tron 常量
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-03 17:39
 */
public final class TronConstants {

    /**
     * 1trx = 1000000 sun
     */
    public static final BigDecimal TRX_SUN_RATE = new BigDecimal(1000000);

    /**
     * trx 精度
     */
    public static final int TRX_DECIMAL = 6;

    /**
     * Transfer事件 - Event Log
     * <a href="https://cn.developers.tron.network/docs/event#log-%E8%A7%A3%E7%A0%81">LOG 解码</a>
     * 通过keccak256计算后的结果
     * 等于【ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef】
     */
    public static final String TRANSFER_EVENT_BY_KECCAK256 =
            Hex.toHexString(new Keccak.Digest256().digest("Transfer(address,address,uint256)".getBytes()));

    /**
     * 智能合约转账函数ID
     * 通过keccak256计算后的结果 - 前四个字节
     * 等于【a9059cbb】
     * 文档描述：用于虚拟机对函数的寻址
     */
    public static final String TRANSFER_FUNC_ID_BY_KECCAK256 =
            Hex.toHexString(
                    new Keccak.Digest256().digest("transfer(address,uint256)".getBytes())
            ).substring(0, 8);

    /**
     * Hex格式地址开头
     */
    public static final String ADDRESS_HEX_PREFIX ="41";

    /**
     * Base58格式地址开头
     */
    public static final String ADDRESS_BASE58_PREFIX ="T";

    /**
     * Hex格式地址长度
     */
    public static final int ADDRESS_HEX_LENGTH =42;

    /**
     * Base58格式地址长度
     */
    public static final int ADDRESS_BASE58_LENGTH =34;


}
