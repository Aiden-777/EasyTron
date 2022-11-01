package org.tron.easywork.util;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.TransferFunctionParam;
import org.tron.easywork.model.TransferInfo;
import org.tron.easywork.model.Trc10TransferInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.utils.Base58Check;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-01 16:52
 */
@Slf4j
public class TransactionParser {

    /**
     * 解析交易ID（交易hash）
     *
     * @param transaction 交易
     * @return hash
     */
    public static String getTransactionId(Chain.Transaction transaction) {
        /*
            byte[] rawData = transaction.getRawData().toByteArray();
            byte[] rawDataHash256 = Hash.sha256(rawData);
            return Hex.toHexString(rawDataHash256);
        */
        byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
        return Hex.toHexString(bytes);
    }

    /**
     * 获取转账信息
     *
     * @param transferContract trx合约
     * @return 转账信息
     */
    public static TransferInfo getTransferInfo(Contract.TransferContract transferContract) {
        // 到账地址
        ByteString toAddressBs = transferContract.getToAddress();
        String toAddress = Base58Check.bytesToBase58(toAddressBs.toByteArray());
        // 转账(发起人)地址
        ByteString fromAddressBs = transferContract.getOwnerAddress();
        String fromAddress = Base58Check.bytesToBase58(fromAddressBs.toByteArray());
        // 转账金额
        long amount = transferContract.getAmount();
        return new TransferInfo(fromAddress, toAddress, BigDecimal.valueOf(amount));
    }

    /**
     * 获取转账信息
     *
     * @param transferAssetContract trc10合约
     * @return 转账信息
     */
    public static Trc10TransferInfo getTransferInfo(Contract.TransferAssetContract transferAssetContract) {
        // 到账地址
        ByteString toAddressBs = transferAssetContract.getToAddress();
        String toAddress = Base58Check.bytesToBase58(toAddressBs.toByteArray());
        // 转账(发起人)地址
        ByteString fromAddressBs = transferAssetContract.getOwnerAddress();
        String fromAddress = Base58Check.bytesToBase58(fromAddressBs.toByteArray());
        // 转账金额
        long amount = transferAssetContract.getAmount();
        // tokenId
        ByteString assetNameBs = transferAssetContract.getAssetName();
        String assetName = assetNameBs.toStringUtf8();
        return new Trc10TransferInfo(fromAddress, toAddress, BigDecimal.valueOf(amount), new BigInteger(assetName));
    }


    /**
     * 获取转账信息
     *
     * @param triggerSmartContract trc20 合约
     * @return 转账信息
     */
    public static Trc20TransferInfo getTransferInfo(Contract.TriggerSmartContract triggerSmartContract) throws SmartParamDecodeException, FunctionSelectorException {
        // 转账数据：到账地址、交易金额
        TransferFunctionParam transferFunctionParam = SmartContractParser.getTransferFunctionParam(triggerSmartContract);
        // 发送人
        byte[] fromAddressBs = triggerSmartContract.getOwnerAddress().toByteArray();
        String fromAddress = Base58Check.bytesToBase58(fromAddressBs);
        // 合约地址
        byte[] contractAddressBs = triggerSmartContract.getContractAddress().toByteArray();
        String contractAddress = Base58Check.bytesToBase58(contractAddressBs);
        // 交易产生时间 ... 忽略，sdk读取时间 间歇性丢失
        return new Trc20TransferInfo(fromAddress, transferFunctionParam.getToAddress()
                , transferFunctionParam.getAmount(), contractAddress);
    }


}
