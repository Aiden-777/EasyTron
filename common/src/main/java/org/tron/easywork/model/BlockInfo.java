package org.tron.easywork.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-09-20 16:05
 */
@Getter
@Setter
@ToString
public class BlockInfo {

    /**
     * 区块信息头
     */
    private BlockHeaderInfo header;

    /**
     * 转账交易信息（仅包含转账交易）
     */
    List<TransferInfo> transfers;

    public BlockInfo() {
    }

    public BlockInfo(BlockHeaderInfo blockHeaderInfo) {
        this.header = blockHeaderInfo;
    }

    public BlockInfo(BlockHeaderInfo blockHeaderInfo, List<TransferInfo> transfers) {
        this.header = blockHeaderInfo;
        this.transfers = transfers;
    }
}
