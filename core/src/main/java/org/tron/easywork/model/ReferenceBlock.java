package org.tron.easywork.model;

import com.google.protobuf.ByteString;
import lombok.Getter;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;

import java.nio.ByteBuffer;

/**
 * 引用区块，要求范围最新区块 65535 以内
 * <a href="https://cn.developers.tron.network/v3.7/docs/%E6%9C%AC%E5%9C%B0%E6%9E%84%E5%BB%BA%E4%BA%A4%E6%98%93">文档搜索：本地构建交易</a>
 * <p>
 * 本地构建交易
 * bytes ref_block_bytes = 1;   //最新块高度的第6到8（不包含）之间的字节
 * int64 ref_block_num = 3;     //区块高度，可选
 * bytes ref_block_hash = 4;    //最新块的hash的第8到16(不包含)之间的字节
 *
 * @author Admin
 * @version 1.0
 * @time 2022-12-13 17:41
 */
@Getter
public class ReferenceBlock {

    /**
     * 引用区块头，要求范围最新区块 65535 以内
     */
    private final Chain.BlockHeader blockHeader;

    /**
     * 最新块的hash的第8到16(不包含)之间的字节
     */
    private final ByteString refBlockHash;

    /**
     * 最新块高度的第6到8（不包含）之间的字节
     */
    private final ByteString refBlockBytes;

    public ReferenceBlock(Chain.BlockHeader blockHeader) {
        this.blockHeader = blockHeader;

        byte[] blockHash = Hash.sha256(blockHeader.getRawData().toByteArray());
        refBlockHash = ByteString.copyFrom(blockHash, 8, 8);

        byte[] refBlockNum = ByteBuffer
                .allocate(Long.BYTES)
                .putLong(blockHeader.getRawData().getNumber())
                .array();
        refBlockBytes = ByteString.copyFrom(refBlockNum, 6, 2);
    }

}
