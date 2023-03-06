package org.tron.easywork.model;

import lombok.Getter;
import lombok.ToString;
import org.tron.trident.core.key.KeyPair;

/**
 * Tron 账户模型
 *
 * @author Admin
 * @version 1.0
 * @time 2022-03-29 14:37
 */
@Getter
@ToString
public class AccountInfo {

    /**
     * 私钥
     */
    private final String hexPrivateKey;
    /**
     * 公钥
     */
    private final String publicKey;
    /**
     * base58地址
     */
    private final String base58CheckAddress;
    /**
     * hex地址
     */
    private final String hexAddress;

    private final KeyPair keyPair;

    public AccountInfo(String hexPrivateKey) {
        this(new KeyPair(hexPrivateKey));
    }

    public AccountInfo(KeyPair keyPair) {
        this.keyPair = keyPair;
        this.hexPrivateKey = keyPair.toPrivateKey();
        this.publicKey = keyPair.toPublicKey();
        this.base58CheckAddress = keyPair.toBase58CheckAddress();
        this.hexAddress = keyPair.toHexAddress();
    }

}
