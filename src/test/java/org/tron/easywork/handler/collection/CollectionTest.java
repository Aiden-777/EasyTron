package org.tron.easywork.handler.collection;

import lombok.extern.slf4j.Slf4j;
import org.tron.easywork.BaseTest;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-11-08 20:12
 */
@Slf4j
public class CollectionTest extends BaseTest {

    // 资金归集
    /*@Test
    public void fundCollection() throws IllegalException {
        // 私钥列表
        List<String> keys;
        // 读取需要归集的私钥 - 一行一个
        File file = new File("D:/Admin/Desktop/tron.txt");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            keys = bufferedReader.lines().filter(line -> line.length() == 64).toList();
            keys.forEach(log::debug);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Trx转账处理器
        TrxTransferHandler trxTransferHandler = new TrxTransferHandler();
        // Trc20转账处理器
        Trc20TransferHandler trc20TransferHandler = new Trc20TransferHandler();

        // 归集配置
        FundCollectionConfig config = new FundCollectionConfig(
                Trc20ContractUtil.readTrc20ContractInfo("TFd1piJ8iXmJQicTicq4zChDSNSMLPFR4w", wrapper),
                "TBkg2JK18tY2YZArQgiS9DygzhVKptFirn",
                "TBkg2JK18tY2YZArQgiS9DygzhVKptFirn",
                "TP6QorvxAJ4bXg21LterCpGi5oZ2PxybCZ",
                List.of("--------******-----"),
                BigDecimal.TEN
        );
        // 默认值为0，可忽略
        config.setHandingFeeProviderPermissionId(0);


        FundCollection collection = new FundCollection(config, trxTransferHandler, trc20TransferHandler, wrapper);

        // 引用区块
        Chain.Block nowBlock = wrapper.getNowBlock();
        ReferenceBlock referenceBlock = new ReferenceBlock(nowBlock.getBlockHeader());

        for (String key : keys) {
            try {
                log.debug("--------------------------------");
                collection.collection(key, referenceBlock);
            } catch (InterruptedException e) {
                log.error("Sleep睡眠异常");
                e.printStackTrace();
            } catch (RuntimeException e) {
                log.debug(e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                log.debug("兜底：{}", e.getMessage());
                e.printStackTrace();
            }
        }

    }*/

}
