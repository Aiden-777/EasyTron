package org.tron.easywork.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-15 21:22
 */
@Getter
@Setter
@ToString
public class BlockHeaderInfo {

    /**
     * 区块哈希
     */
    private String id;

    /**
     * 区块高度
     */
    private Long height;

    /**
     * 区块交易总数
     */
    private Integer count;

    /**
     * 创建时间
     */
    private Date createTime;

    public BlockHeaderInfo() {
    }

    public BlockHeaderInfo(String id, Long height, Integer count, Date createTime) {
        this.id = id;
        this.height = height;
        this.count = count;
        this.createTime = createTime;
    }

}
