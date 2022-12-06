package org.tron.easywork.util;

import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;

/**
 * 区块工具类
 *
 * @author Admin
 * @version 1.0
 * @time 2022-04-21 03:40
 */
public class BlockParser {

    /**
     * 解析区块ID（区块hash）
     *
     * @param block 块
     * @return hash
     */
    public static String parseBlockId(Chain.Block block) {
        return parseBlockId(block.getBlockHeader());
    }

    /**
     * 解析区块ID（区块hash）
     *
     * @param blockHeader 头信息
     * @return hash
     */
    public static String parseBlockId(Chain.BlockHeader blockHeader) {
        // 块高度
        long number = blockHeader.getRawData().getNumber();
        // 块header
        byte[] headerBytes = blockHeader.getRawData().toByteArray();
        byte[] bytes = Hash.sha256(headerBytes);
        String blockIdEnd = Hex.toHexString(bytes).substring(16);
        String blockStart = BaseConvert.toBase16StringWithZero(number);
        return blockStart + blockIdEnd;
    }
}
