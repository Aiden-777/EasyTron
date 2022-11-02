# EasyTron
依赖 [trident-java](https://github.com/tronprotocol/trident) 的轻量扩展封装，区块解码将变得十分简单

测试用例（包含各种方法运用思路）： core/src/test/java/org/tron/easywork

测试配置文件： core/src/test/resources/config.properties

Donate：TTTTTtczA5UZM65QJpncXUsH8KwgJTHyXw

---

由于测试代码包含许多需要鉴权、有速率限制的网络API，打包时请跳过测试

在其他地方使用这个库，必要依赖：

```xml
<dependencies>
    
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-all</artifactId>
        <version>1.50.2</version>
    </dependency>

    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk18on</artifactId>
        <version>1.72</version>
    </dependency>

    <dependency>
        <groupId>org.tron.trident</groupId>
        <artifactId>abi</artifactId>
        <version>0.3.0</version>
    </dependency>

    <dependency>
        <groupId>org.tron.trident</groupId>
        <artifactId>utils</artifactId>
        <version>0.3.0</version>
    </dependency>
    <dependency>
        <groupId>org.tron.trident</groupId>
        <artifactId>core</artifactId>
        <version>0.3.0</version>
    </dependency>

    <dependency>
        <groupId>org.tron.easywork</groupId>
        <artifactId>core</artifactId>
        <version>1.2.1</version>
    </dependency>
    <dependency>
        <groupId>org.tron.easywork</groupId>
        <artifactId>common</artifactId>
        <version>1.2.1</version>
    </dependency>
    
</dependencies>
```