package org.tron.easywork;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.model.AccountInfo;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.utils.Convert;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-11-01 12:41
 */
@Slf4j
public class BaseTest {

    protected String apiKey;

    // 转出私钥
    protected String privateKey;

    // 转出账户
    protected AccountInfo fromAccount;

    // 到账地址
    protected String toAddress;

    // ApiWrapper
    protected ApiWrapper wrapper;

    // 测试trc20合约地址
    protected String testContractAddress;

    // USDT 合约地址
    protected String usdtContractAddress;

    // 默认矿工费限制 - 单位sum - 此处为10TRX
    protected Long defaultFeeLimit = Convert.toSun(BigDecimal.TEN, Convert.Unit.TRX).longValue();

    /**
     * 变量初始化
     */
    @Before
    public void init() throws Exception {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");

        Properties properties = new Properties();
        properties.load(in);

        assert in != null;
        in.close();

        for (Object o : properties.values()) {
            String v = (String) o;
            if (StringUtil.isNullOrEmpty(v)) {
                throw new Exception("配置文件填写不完整：core/src/test/resources/config.properties");
            }
        }

        apiKey = properties.getProperty("apiKey");
        privateKey = properties.getProperty("privateKey");
        fromAccount = new AccountInfo(privateKey);
        toAddress = properties.getProperty("toAddress");
        wrapper = ApiWrapperFactory.create(ApiWrapperFactory.NetType.Shasta, privateKey, apiKey);
        testContractAddress = properties.getProperty("testContractAddress");
        usdtContractAddress = properties.getProperty("usdtContractAddress");

    }

}
