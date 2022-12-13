package org.tron.easywork.handler.transfer;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.util.internal.StringUtil;
import org.tron.easywork.enums.TransactionStatus;
import org.tron.easywork.enums.TransferType;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.util.TransactionUtil;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-10-30 15:43
 */
public abstract class BaseTransferHandler implements LocalTransfer, TransferParser {

    @Override
    public Chain.Transaction buildLocalTransfer(TransferInfo transferInfo, Chain.BlockHeader refBlockHeader) {
        // 交易原数据
        Chain.Transaction.raw.Builder rawBuilder = this.transactionRawBuilder(transferInfo, refBlockHeader);

        // 添加合约信息
        rawBuilder.addContract(this.contractBuilder(transferInfo));
        // 设置智能合约手续费限制
        this.setFeeLimit(rawBuilder, transferInfo);

        // 构造交易信息
        Chain.Transaction.Builder transactionBuilder = Chain.Transaction.newBuilder();
        // 设置原数据
        transactionBuilder.setRawData(
                rawBuilder
        );
        return transactionBuilder.build();
    }

    /**
     * 创建交易原数据构造器，并进行基础配置
     * <p>
     * 本地构建交易
     * bytes ref_block_bytes = 1;   //最新块高度的第6到8（不包含）之间的字节
     * int64 ref_block_num = 3;     //区块高度，可选
     * bytes ref_block_hash = 4;    //最新块的hash的第8到16(不包含)之间的字节
     *
     * @param transferInfo   交易信息
     * @param refBlockHeader 引用区块
     * @return 交易原数据
     */
    private Chain.Transaction.raw.Builder transactionRawBuilder(TransferInfo transferInfo, Chain.BlockHeader refBlockHeader) {

        byte[] blockHash = Hash.sha256(refBlockHeader.getRawData().toByteArray());

        byte[] refBlockNum = ByteBuffer
                .allocate(Long.BYTES)
                .putLong(refBlockHeader.getRawData().getNumber())
                .array();

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, 8);

        // 备注
        ByteString memo = StringUtil.isNullOrEmpty(transferInfo.getMemo()) ? ByteString.empty() : ByteString.copyFromUtf8(transferInfo.getMemo());

        return Chain.Transaction.raw.newBuilder()
                // 创建时间
                .setTimestamp(now.getTime())
                // 过期时间
                .setExpiration(calendar.getTimeInMillis())
                // 参考区块
                .setRefBlockHash(ByteString.copyFrom(blockHash, 8, 8))
                // 参考区块
                .setRefBlockBytes(ByteString.copyFrom(refBlockNum, 6, 2))
                // 备注
                .setData(memo);
    }


    /**
     * 创建合约构造器，并在子类中进行合约配置
     *
     * @param transferInfo 交易信息
     * @return 合约构造器
     */
    private Chain.Transaction.Contract.Builder contractBuilder(TransferInfo transferInfo) {
        // 创建合约类容
        Any parameter = this.createContractParameter(transferInfo);
        // 合约构造器
        return Chain.Transaction.Contract.newBuilder()
                // 设置合约类型
                .setType(this.getContractType())
                // 合约内容
                .setParameter(parameter)
                // 权限ID
                .setPermissionId(null == transferInfo.getPermissionId() ? 0 : transferInfo.getPermissionId());
    }

    /**
     * 获取能处理的合约类型
     *
     * @return 合约类型
     */
    abstract public Chain.Transaction.Contract.ContractType getContractType();


    /**
     * 获取能处理的转账类型
     *
     * @return 转账类型
     */
    abstract public TransferType getTransferType();

    /**
     * 创建合约内容 - 入参数据
     *
     * @param transferInfo 交易信息
     * @return 合约内容
     */
    abstract protected Any createContractParameter(TransferInfo transferInfo);

    /**
     * 设置智能合约手续费限制，非智能合约使用空实现即可
     *
     * @param rawBuilder   交易原数据
     * @param transferInfo 转账信息
     */
    protected void setFeeLimit(Chain.Transaction.raw.Builder rawBuilder, TransferInfo transferInfo) {

    }

    /**
     * 检查并转换为子类
     *
     * @param transferInfo 交易信息
     * @return 子类交易信息
     */
    abstract protected TransferInfo checkAndTranslate(TransferInfo transferInfo);

    @Override
    public TransferInfo parse(Chain.Transaction transaction) throws InvalidProtocolBufferException, SmartParamDecodeException, FunctionSelectorException {
        // 检查交易是否成功
        boolean status = transaction.getRet(0).getContractRet().getNumber() == 1;

        // 合约
        Chain.Transaction.Contract contract = transaction.getRawData().getContract(0);

        // parameter
        Any any = contract.getParameter();

        // 解包
        GeneratedMessageV3 unpack = this.unpack(any);

        // 获取交易信息
        TransferInfo transferInfo = this.getTransferInfo(unpack);

        // 交易ID
        String tid = TransactionUtil.getTransactionId(transaction);

        // 备注
        ByteString memoData = transaction.getRawData().getData();

        // 完善已知交易信息
        transferInfo.setId(tid);
        transferInfo.setStatus(status ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        transferInfo.setMemo(memoData == ByteString.EMPTY ? null : memoData.toStringUtf8());

        return transferInfo;
    }


    /**
     * 合约数据解包
     *
     * @param any 合约数据
     * @return 解码后的合约数据
     * @throws InvalidProtocolBufferException 数据解码异常
     */
    abstract protected GeneratedMessageV3 unpack(Any any) throws InvalidProtocolBufferException;


    /**
     * 获取交易信息
     *
     * @param contract 合约数据
     * @return 交易信息
     * @throws SmartParamDecodeException 转账解析失败
     * @throws FunctionSelectorException 智能合约 函数选择器 错误异常
     */
    abstract protected TransferInfo getTransferInfo(GeneratedMessageV3 contract) throws SmartParamDecodeException, FunctionSelectorException;

}
