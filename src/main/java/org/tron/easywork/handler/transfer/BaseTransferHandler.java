package org.tron.easywork.handler.transfer;


import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.util.internal.StringUtil;
import org.tron.easywork.enums.TransactionStatus;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.ReferenceBlock;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.util.TransactionUtil;
import org.tron.trident.proto.Chain;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-11 09:07
 */
public abstract class BaseTransferHandler<Contract extends GeneratedMessageV3> implements TransferHandler {

    @Override
    public boolean supportContractType(Chain.Transaction.Contract.ContractType contractType) {
        return contractType == this.getContractType();
    }

    public boolean supportContractType(Chain.Transaction transaction) {
        return this.supportContractType(TransactionUtil.getFirstContractType(transaction));
    }

    /**
     * 支持处理的合约类型
     *
     * @return 合约类型
     */
    abstract protected Chain.Transaction.Contract.ContractType getContractType();


    /**
     * 创建交易原数据构造器，并进行基础配置
     * <p>
     * 本地构建交易
     * bytes ref_block_bytes = 1;   //最新块高度的第6到8（不包含）之间的字节
     * int64 ref_block_num = 3;     //区块高度，可选
     * bytes ref_block_hash = 4;    //最新块的hash的第8到16(不包含)之间的字节
     *
     * @param transfer       交易信息
     * @param referenceBlock 引用区块
     * @return 交易原数据
     */
    protected Chain.Transaction.raw.Builder initTransactionRawBuilder(Transfer transfer, ReferenceBlock referenceBlock) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, 8);

        // 备注
        ByteString memo = StringUtil.isNullOrEmpty(transfer.getMemo()) ? ByteString.empty() : ByteString.copyFromUtf8(transfer.getMemo());

        return Chain.Transaction.raw.newBuilder()
                // 创建时间
                .setTimestamp(now.getTime())
                // 过期时间
                .setExpiration(calendar.getTimeInMillis())
                // 参考区块 RefBlockHash
                .setRefBlockHash(referenceBlock.getRefBlockHash())
                // 参考区块 RefBlockBytes
                .setRefBlockBytes(referenceBlock.getRefBlockBytes())
                // 备注
                .setData(memo);
    }

    /**
     * 初始化交易建造者
     *
     * @param transfer       交易信息
     * @param referenceBlock 引用区块信息
     * @return 交易建造者
     */
    protected Chain.Transaction.Builder initTransactionBuilder(Transfer transfer, ReferenceBlock referenceBlock) {

        // 交易原数据
        Chain.Transaction.raw.Builder rawBuilder = this.initTransactionRawBuilder(transfer, referenceBlock);

        // 添加合约信息
        rawBuilder.addContract(this.contractBuilder(transfer));

        // 构造交易信息
        Chain.Transaction.Builder transactionBuilder = Chain.Transaction.newBuilder();
        // 设置原数据
        return transactionBuilder.setRawData(
                rawBuilder
        );
    }

    /**
     * 创建合约构造器，并在子类中进行合约配置
     *
     * @param transfer 交易信息
     * @return 合约构造器
     */
    protected Chain.Transaction.Contract.Builder contractBuilder(Transfer transfer) {
        // 创建合约类容
        Any parameter = this.createContractParameter(transfer);
        // 合约构造器
        return Chain.Transaction.Contract.newBuilder()
                // 设置合约类型
                .setType(this.getContractType())
                // 合约内容
                .setParameter(parameter)
                // 权限ID
                .setPermissionId(null == transfer.getPermissionId() ? 0 : transfer.getPermissionId());
    }

    @Override
    public Chain.Transaction buildLocalTransfer(Transfer transfer, ReferenceBlock referenceBlock) {
        return this.initTransactionBuilder(transfer, referenceBlock).build();
    }

    @Override
    public Transfer parse(Chain.Transaction transaction)
            throws InvalidProtocolBufferException, SmartParamDecodeException, FunctionSelectorException {

        // 检查交易是否成功
        boolean status = TransactionUtil.isTransactionSuccess(transaction);

        // 合约
        Chain.Transaction.Contract contract = transaction.getRawData().getContract(0);

        // parameter
        Any any = contract.getParameter();

        // 解包
        Contract unpack = this.unpack(any);

        // 交易ID
        String tid = TransactionUtil.getTransactionId(transaction);

        // 备注
        ByteString memoData = transaction.getRawData().getData();

        // 获取交易信息
        Transfer transfer = this.getTransferInfo(unpack);
        // 完善已知交易信息
        transfer.setId(tid);
        transfer.setStatus(status ? TransactionStatus.SUCCESS : TransactionStatus.ERROR);
        transfer.setMemo(memoData == ByteString.EMPTY ? null : memoData.toStringUtf8());
        transfer.setPermissionId(TransactionUtil.getFirstPermissionId(transaction));
        transfer.setTimestamp(transaction.getRawData().getTimestamp());
        transfer.setExpiration(transaction.getRawData().getExpiration());

        return transfer;
    }

    /**
     * 创建合约内容 - 入参数据
     *
     * @param transfer 交易信息
     * @return 合约内容
     */
    abstract protected Any createContractParameter(Transfer transfer);


    /**
     * 合约数据解包
     *
     * @param any 合约数据
     * @return 解码后的合约数据
     * @throws InvalidProtocolBufferException 数据解码异常
     */
    abstract protected Contract unpack(Any any) throws InvalidProtocolBufferException;


    /**
     * 获取交易信息
     *
     * @param contract 合约数据
     * @return 交易信息
     * @throws SmartParamDecodeException 转账解析失败
     * @throws FunctionSelectorException 智能合约 函数选择器 错误异常
     */
    abstract protected Transfer getTransferInfo(Contract contract) throws SmartParamDecodeException, FunctionSelectorException;
}
