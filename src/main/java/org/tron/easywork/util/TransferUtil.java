package org.tron.easywork.util;

import com.google.protobuf.ByteString;
import org.tron.easywork.exception.FunctionSelectorException;
import org.tron.easywork.exception.SmartParamDecodeException;
import org.tron.easywork.model.Transfer;
import org.tron.easywork.model.TransferFunctionParam;
import org.tron.trident.proto.Contract;
import org.tron.trident.utils.Base58Check;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-12 01:14
 */
public class TransferUtil {


    /**
     * 获取转账信息
     *
     * @param transferContract trx合约
     * @return 转账信息
     */
    public static Transfer getTransferInfo(Contract.TransferContract transferContract) {
        // 到账地址
        ByteString toAddressBs = transferContract.getToAddress();
        String toAddress = Base58Check.bytesToBase58(toAddressBs.toByteArray());
        // 转账(发起人)地址
        ByteString fromAddressBs = transferContract.getOwnerAddress();
        String fromAddress = Base58Check.bytesToBase58(fromAddressBs.toByteArray());
        // 转账金额
        long amount = transferContract.getAmount();
        return Transfer.trxTransferBuilder(fromAddress, toAddress, BigDecimal.valueOf(amount)).build();
    }

    /**
     * 获取转账信息
     *
     * @param transferAssetContract trc10合约
     * @return 转账信息
     */
    public static Transfer getTransferInfo(Contract.TransferAssetContract transferAssetContract) {
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
        return Transfer.trc10TransferBuilder(fromAddress, toAddress, BigDecimal.valueOf(amount), new BigInteger(assetName)).build();
    }


    /**
     * 获取转账信息
     *
     * @param triggerSmartContract trc20 合约
     * @return 转账信息
     */
    public static Transfer getTransferInfo(Contract.TriggerSmartContract triggerSmartContract) throws SmartParamDecodeException, FunctionSelectorException {
        // 转账数据：到账地址、交易金额
        TransferFunctionParam transferFunctionParam = Trc20ContractUtil.getTransferFunctionParam(triggerSmartContract);
        // 发送人
        byte[] fromAddressBs = triggerSmartContract.getOwnerAddress().toByteArray();
        String fromAddress = Base58Check.bytesToBase58(fromAddressBs);
        // 合约地址
        byte[] contractAddressBs = triggerSmartContract.getContractAddress().toByteArray();
        String contractAddress = Base58Check.bytesToBase58(contractAddressBs);
        return Transfer.trc20TransferBuilder(fromAddress, transferFunctionParam.getToAddress(),
                transferFunctionParam.getAmount(), contractAddress).build();
    }
}
