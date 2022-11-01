package org.tron.easywork.factory;

import lombok.NonNull;
import org.tron.trident.core.ApiWrapper;

/**
 * @author Admin
 * @version 1.0
 * @time 2022-04-18 09:24
 */
public class ApiWrapperFactory {

    public static ApiWrapper create(@NonNull NetType netType, @NonNull String privateKey, String apiKey) {
        if (NetType.Mainnet == netType) {
            if (null == apiKey || "".equals(apiKey.trim())) {
                throw new NullPointerException("主网 apiKey 参数不能为空！");
            }
            return ApiWrapper.ofMainnet(privateKey, apiKey);
        } else if (NetType.Shasta == netType) {
            return ApiWrapper.ofShasta(privateKey);
        } else if (NetType.Nile == netType) {
            return ApiWrapper.ofNile(privateKey);
        }
        throw new IllegalArgumentException("不支持的网络类型！");
    }

    public static ApiWrapper create(String grpcEndpoint, String grpcEndpointSolidity,
                                    String hexPrivateKey, String apiKey) {
        if (null != apiKey) {
            return new ApiWrapper(grpcEndpoint, grpcEndpointSolidity, hexPrivateKey, apiKey);
        }
        return new ApiWrapper(grpcEndpoint, grpcEndpointSolidity, hexPrivateKey);
    }


    /**
     * Tron 网络类型
     */
    public enum NetType {
        /**
         * 主网
         */
        Mainnet,
        /**
         * 水龙头测试网
         */
        Shasta,
        /**
         * 尼罗测试网
         */
        Nile
    }

}
