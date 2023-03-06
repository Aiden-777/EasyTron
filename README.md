# EasyTron
**娱乐项目**

[trident-java](https://github.com/tronprotocol/trident) 的轻量扩展，将常用解码步骤做了进一步的封装。


本库封装了 Trx、Trc10、Trc20 常用功能，本地构造交易转账、读取解码后的交易，变得十分简单。
同时，不影响使用 trident 原有的各种功能。

测试用例（包含各种方法运用思路）： core/src/test/java/org/tron/easywork

测试基类： org.tron.easywork.BaseTest

---

由于测试代码包含许多需要鉴权、有速率限制的网络API，打包时请跳过测试，调试请在 IDE 中进行。

编译依赖java17，在其他地方使用这个库，local-path形式，或jar包导入本地maven：

```xml

<dependencies>

    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-all</artifactId>
        <version>1.51.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk18on</artifactId>
        <version>1.72</version>
        <scope>provided</scope>
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
        <version>2.0</version>
    </dependency>

</dependencies>
```