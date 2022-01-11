# Impl 相關

<br>

---

<br>

這裡主要是 SockerServer 的實作，先者要介紹 4 大類 SockerServer，其次在介紹其他的類別。

<br>

### IoSockerServer

<br>

實作 SockerServer 介面，建構式如下：

<br>

```java
public IoSocketServer(RecordReader reader, InetSocketAddress address, int bufferSize)
```

<br>

建構 `IoSocketServer` 需要 `RecordReader` `InetSocketAddress` 與 `bufferSize` 這三個參數。

<br>
<br>

`init()` 方法：

<br>

建立 ServerSocket 物件，綁定 port。

<br>
<br>

`startup()` 方法：

<br>

阻塞式 Socket 寫法，建立一個執行緒佔住一個 port。`serverSocket.accept()` 會阻塞住。當連線成功建立起來之後，會在起一個 Thread 去處理資料接收。

<br>
<br>

`restart` 方法：

<br>

先後進行 `close()` `init()` `startup()` 操作。

<br>
<br>

`close()` 方法：

<br>

關閉 serverSocker。

<br>
<br>
<br>
<br>

### NioServerSocket

<br>


實作 SockerServer 介面，建構式如下：

```java
public NioSocketServer(RecordReader reader, InetSocketAddress address, int bufferSize)
```

<br>
<br>

`init()` 方法：

<br>

建立 cacheBuffer 緩存。他是一個 `Map<String, ByteBuffer>`，用途是為每一個客戶端連線建立一個暫存資料的 bytebuffer。

<br>
<br>

`startup()` 方法：

<br>

採用 Java NIO API，開啟 ServerSocketChannel，綁定 Selector 並開始對 ACCEPT 事件進行監聽。

<br>

SelectionKey 有 2 種，一種是 Acceptable 的，另一種是 Readable 的。這兩種 key 分別交給 `processAcceptable()` `processReadable()` 方法處裡。

<br>
<br>

`processAcceptable()`

<br>

接通連線：

<br>

```java
SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
channel.configureBlocking(false); // 設定為非阻塞
channel.register(selector, SelectionKey.OP_READ);
```

<br>

accept 處裡最重要的一環是這個：

<br>

```java
cacheBuffers.put(String.valueOf(channel.getRemoteAddress()), ByteBuffer.allocate(bufferSize));
```

<br>

當連線進來之後，就幫這個客戶端建立一個專屬的 ByteBuffer，之後這個 Client 的所有傳入資料都放入他專屬的 buffer 中。

<br>
<br>

`processReadable()`

<br>

處理 Read 動作。

processReadable 的處裡邏輯稍微有一點複雜。首先我們要準備兩組 ByteBuffer：

<br>

```java
ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
String remoteAddr = String.valueOf(channel.getRemoteAddress());
ByteBuffer cacheBuffer = cacheBuffers.get(remoteAddr);
```

<br>

第一個 ByteBuffer 是直接新建出來的，第二個 "cacheBuffer" 是由先前在 processAcceptable() 階段建立的。

<br>

處裡的第一步，是把 cacheBuffer (先前沒讀完的) 資料倒入新建的 ByteBuffer 中，繼續把 channel 中的資料接續 ByteBuffer。

<br>

讀取完畢之後把 buffer 調整至讀取狀態 `buffer.flip();`。呼叫 `recordReader` 的 `processRecord(buffer)` 方法。

<br>

這裡不是 RecordReader 的介紹，所以叫稍微提一下 RecordReader 裡面的邏輯。

<br>

在 RecordReader 中，我們要做的是跟據標頭的 dataSize 長度去取出資料，當 buffer 的資料不夠 dataSize 大小時，reset buffer 游標（把資料放回去）存回　catchBuffer 中，留著下一輪 read 時接續之後的資料以供完整讀取一筆。 

<br>

recordReader 讀取完後如果 buff 裡面有剩餘就放回 cacheBuffer 中

<br>

```java
cacheBuffers.put(remoteAddr, cacheBuffer);
```

<br>
<br>

`close()` 方法：

<br>

關閉 selector 與 serverSocketChannel。

<br>
<br>
<br>
<br>

### TLSIosocketServer

<br>

這一個類別延續 IoSocketServer 的設計，多加入了 TLS(SSL) 的設計。

<br>

建構式如下：

<br>

```java
public TLSIoSocketServer(RecordReader reader, InetSocketAddress address, int bufferSize, String keyStoreName, String keyStorePassword)
```

<br>

可以看到跟 IoSocketSever 不一樣的是，這裡多了 `keyStoreName` 與 `keyStorePassword` 這兩個參數。這兩個參數都是為 SSL 做準備的。其中 `keyStoreName` 是 key 文件的位置，`keyStorePassword` 是 SSL 密碼。

<br>
<br>

`init()` 方法：

<br>

初始化階段步驟就有點繁瑣了，首先我們要載入 SSL key 文件。

<br>

建立 `KeyStore`：

<br>

```java
KeyStore keyStore = KeyStore.getInstance(SSLProperty.KS_TYPE);
```

<br>

這邊的 SSLProperty 是自己建立的一個 Interface，裡面只有幾個靜態屬性：

<br>

```java
public interface SSLProperty {

    String SSL_TYPE = "SSL";
    String KS_TYPE = "JKS";
    String X509 = "SunX509";

}
```

<br>

keystore 文件需要被載入：

<br>

```java
 InputStream kstore = new FileInputStream(new File(keyStoreName));
```

<br>

將 __key 文件__ 與 __密碼__ 載入到 keystore：

<br>

```java
keyStore.load(kstore, keyStorePassword.toCharArray());
```

<br>

然後就需要用到 `KeyManagerFactory` 與 `SSLContext` 這兩個類別。

<br>

建立 `KeyManagerFactory` 並初始化：

<br>

```java
KeyManagerFactory kmf = KeyManagerFactory.getInstance(SSLProperty.X509);
kmf.init(keyStore, keyStorePassword.toCharArray());
```

<br>

建立 `SSLContext` 並出數化：

<br>

```java
SSLContext ctx = SSLContext.getInstance(SSLProperty.SSL_TYPE);
ctx.init(kmf.getKeyManagers(), new TrustManager[]{new TrustAllManager()}, null);
```

<br>

這邊有一點特殊的地方是我們在 SSLContext 初始化階段我們需要傳入兩組陣列分別是 `KeyManager[]` 與 `TrustManager[]`。第一個 `KeyManager[]` 就是從前面建立好的 `KeyManagerFactory` 直接 get 出來。後面的 `TrustManager[]`，的作用是設定只相信哪一些憑證的連線來源。由於我們 Server 端需要接受所有連線，所以我們需要自制一個 `TrustAllManager`，來看一下 TrustAllManager 類別：

<br>

```java
public class TrustAllManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
```

<br>

`TrustAllManager` 需要實作 `X509TrustManager` 介面。一共有 3 個方法需要被實現。針對 client 端的連線憑證檢查可以在這邊做，當我們需要做到只讓某些特定持有指定憑證的 client 連入時，就需要在這邊實作檢查動作。

由於我們不需要對來源端坐任何檢查，所有來源一律放行，所以不需要做任何檢查邏輯。

<br>

最終我們需要建立出 SSLServerSocket：

<br>

```java
SSLServerSocketFactory factory = ctx.getServerSocketFactory();
serverSocket = (SSLServerSocket) factory.createServerSocket();
```

<br>
<br>

`startup()` 方法：

<br>

由於是 SSLIoSocket 所以不會很複雜。事實上就跟一般 IoSocket 一樣，關於 SSL 的設定都在 `init()` 時做好了，所以在 `stratup()` 階段只需要使用被設定好的 `serverSocket` 就可以了。餘下部分不多做說明。

<br>
<br>
<br>
<br>

### SSLNioSocketServer

<br>

這一個部份解比較複雜了。首先看到建構式部分：

<br>

```java
public SSLNioSocketServer(RecordReader<ByteBuffer> reader, InetSocketAddress address, String keyStoreName, String keyStorePassword)
```

<br>

在 NIOSockerServer 的基礎上多出了 SSL 相關參數 `keyStoreName` 與 `keyStorePassword`。

<br>
<br>

`init()` 方法：

<br>

在 init 階段需要先建立一個 `connectionCaches`，他是一個 Map 類別。具體作用是存放當前與客戶端連線所使用到的 `SSLConnectionCache`。

<br>

這個 `SSLConnectionCache` 是一個自建類別，其主要功能就是作為一個 Java Bean 存放 SSL 連線所需要的物件。

<br>

```java
public class SSLConnectionCache {

    private ByteBuffer netInData;

    private ByteBuffer appInData;

    private String ownerIpAddress;

    private SSLEngine sslEngine;

    private ByteBuffer catchBuffer;

    private Object lock;

    ...
}
```

<br>

跟一個 client 連線所需要的項目類都放在這邊方便當作參數存取以及存放進 Map 中。

<br>

建立 `processingKeys` (key 處裡列表)：

<br>

```java
this.processingKeys = new CopyOnWriteArraySet<>();
```

<br>

這是一個 `CopyOnWriteArraySet`，Thread Safe 的 Set 類別。裡面存放的就是 remoteAddress。

<br>

接下來是建立 `Socket` 與 `SSLContext` 還有 `ExecutorService`。

<br>

```java
createServerSocket();
createSSLContext();
createExecutorService();
```

<br>

稍微介紹一下這些方法：

<br>

```java
private void createServerSocket()
```

<br>

這個跟 `NioServerSocket` 一樣，並無差異。

<br>

```java
private void createSSLContext()
```

<br>

這個部分就是在 `IoSocketServer` 的 `init()` 裡面建立的 SSL 工具。

<br>

```java
KeyManagerFactory kmf = KeyManagerFactory.getInstance(SSLProperty.X509);
char[] keyPassword = keyStorePassword.toCharArray();
KeyStore serverKeyStore = KeyStore.getInstance(SSLProperty.KS_TYPE);
serverKeyStore.load(new FileInputStream(keyStoreName), keyPassword);
kmf.init(serverKeyStore, keyPassword);
sslContext = SSLContext.getInstance(SSLProperty.SSL_TYPE);
sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new TrustAllManager()}, null);
```

<br>

跟 `IoSocketServer` 一樣，這邊就不再贅述。

<br>

```java
private void createExecutorService ()
```

<br>

建立 ThreadPool，提供連線使用。

<br>

```java
executorService = Executors.newFixedThreadPool(10);
```

<br>
<br>

`startup()` 方法：

<br>

`startup()` 跟之前的 `NioSocketServer` 的架構大致一樣。不一樣的地方在 `processAcceptable()` 與 `processReadable()`。

<br>
<br>

`processAcceptable()` 方法：

<br>

當有連線進入的時候，第一個就會進入這個方法。

取出 `SocketChannel` 並設定為非阻塞：

<br>

```java
SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
sc.configureBlocking(false); 
```

<br>

綁定 `selector` 並設定對 Read 敏感。

<br>

```java
sc.register(selector, SelectionKey.OP_READ);
```

<br>

接下來就是 SSL 部分的細節了。

建立 SSLEngine 並設定 ServerMode：

<br>

```java
SSLEngine sslEngine = sslContext.createSSLEngine();
sslEngine.setUseClientMode(false);
```

<br>

為 SSLEngine 設定 SSLParameters：

<br>

```java
SSLParameters sslParams = new SSLParameters();
sslParams.setEndpointIdentificationAlgorithm("HTTPS");
sslEngine.setSSLParameters(sslParams);
```

<br>

把 SSLEngine 放入 connectionCaches：

<br>

```java
connectionCaches.put(addressStr, new SSLConnectionCache(sslEngine));
```

<br>

接下來進入 SSL HandShake：

<br>

```java
doHandShake(sc, addressStr);
```

<br>

看看 `doHandshake()` 裡面在做甚麼。

<br>

```java
private void doHandShake (SocketChannel sc, String remoteAddr)
```

<br>

取出 SSLEngine 並建立待會需要用到的 ByteBuffer：

<br>

```java
SSLConnectionCache connectionCache = connectionCaches.get(remoteAddr);

SSLEngine sslEngine = connectionCache.getSslEngine();
ByteBuffer netInData = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
ByteBuffer appInData = ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize());
ByteBuffer netOutData = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
ByteBuffer appOutData = ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize());
```

<br>

這四個 ByteBuffer 非常重要，之後會提到如何使用。

開始 handshake：

<br>

```java
sslEngine.beginHandshake();
SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
```

接下來會進入一個迴圈循環，直到握手完成為止。HandShakeStatus 主要有 5 個狀態，分別是 `FINISHED`丶`NEED_TASK`丶`NEED_UNWRAP`丶`NEED_WRAP`丶`NOT_HANDSHAKING`。

<br>

* `FINISHED` 就是握手完成。

<br>

* `NEED_TASK` 代表握手過程需要分支出一個執行緒進行任務調度，所以需要另起執行緒取出 sslEngine 的 task 執行。

* `NEED_UNWRAP` 代表客戶端傳來封包需要解封，把來源憑證認證資料解析出來。

* `NEED_WRAP` 代表有資料需要打包傳給客戶端。

* `NOT_HANDSHAKING` 這代表目前沒有在進行 handshake。


<br>

```java
while (!handshakeDone) {
    switch (hsStatus) {
        case FINISHED:
            handshakeDone = true;
            break;

        case NEED_TASK:
            hsStatus = doTask(sslEngine);
            break;

        case NEED_UNWRAP:
            netInData.clear();
            sc.read(netInData);
            netInData.flip();
            do {
                SSLEngineResult engineResult = sslEngine.unwrap(netInData, appInData);
                hsStatus = doTask(sslEngine);
            } while (hsStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && netInData.remaining() > 0);
            netInData.clear();
            break;

        case NEED_WRAP:
            SSLEngineResult engineResult = sslEngine.wrap(appOutData, netOutData);
            hsStatus = doTask(sslEngine);
            netOutData.flip();
            sc.write(netOutData);
            netOutData.clear();
            break;

        case NOT_HANDSHAKING:
            System.out.println("NOT_HANDSHAKING..");
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
            handshakeDone = true;
            break;
    }
}
```

<br>
<br>

`doTask(SslEngine sslEngine)`

<br>

這個方法用來處理 handshake 時需要額外分出 Thread 處裡 Task 時用到的。`SslEngine`有一個 `getDelegatedTask()` 方法。他會回傳一個 Runnable 物件。我們只需要把他放進一個新的 Thread 執行既可。

<br>

```java
Runnable task;
while ((task = sslEngine.getDelegatedTask()) != null) {
    new Thread(task).start();
}
```

<br>

方法的最後回傳 Status：

<br>

```java
return sslEngine.getHandshakeStatus();
```

<br>
<br>

`restart()` 方法：

<br>

```java
close();
init();
startup();
```

<br>

`close()` 方法：

<br>

關閉方法裡面我們區要把 `connectionCaches` 裡的每一個 `sslEngine` 都關閉。

<br>

```java
connectionCaches.forEach((k, v) -> {
    v.getSslEngine().closeOutbound();
});
```

<br>


關閉 `Selector` 與 `ServerSocketChannel` 還有 `executorService`：

<br>

```java
connectionCaches = null;
selector.close();
ssc.close();
executorService.shutdown();
```

<br>
<br>

`processReadable()` 方法：

<br>

處裡 read 事件的方法。

<br>

取得 SocketChannel 物件：

<br>

```java
SocketChannel sc = (SocketChannel) key.channel();
```

<br>

因為 readKey 可能一個連線會重複被接收處裡很多次，我們並不希望每一次來自同一個地方的請求都要從頭來處理，所以我們設計了一個 `processingKeys` 的一個 Thread Safe Set。他用來儲存連入的來源 IP 位置。在每次處裡新的 readKey 時，都檢查一遍 `processingKeys` 裡面是否已存在這個來源位置的請求。如果沒有就把此 IP 存入 Set 中然後繼續處裡。如果有則直接離開。

<br>

```java
if (processingKeys.contains(remoteAddr)){
    return;
}else{
    processingKeys.add(remoteAddr);
}
```

<br>

建立一個 Runnable 物件，在這個 Runnable 中，我們需要座椅下動作：

<br>

1. 取出 sslEngine 與需要使用到的 ByteBuffer：

<br>

```java
SSLConnectionCache connectionCache = connectionCaches.get(remoteAddr);
SSLEngine sslEngine = connectionCache.getSslEngine();
ByteBuffer netInData = connectionCache.getNetInData();
ByteBuffer appInData = connectionCache.getAppInData();
```

<br>

當確定握手協議完成並且狀態是 `NOT_HANDSHAKING` 時，才可以正式開始處裡資料：

<br>

```java
if (sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
    // 處裡資料
}
```

<br>

在處裡 read 方面跟 NioSocketServer 幾乎是一樣的，所以這裡就只提 SSL 特別要注意的地方。

由於資料傳輸使用 SSL 協定。所以所有傳輸資料都被加密過了。當我們把資料從 `SocketChannel` 讀取出來之後，還需要使用 `sslEngine` 進行 unwrap 動作：

<br>

```java
while (netInData.hasRemaining()) {
    SSLEngineResult result = sslEngine.unwrap(netInData, appInData);

    ...
```

<br>

這個 SSLEngineResult 有 3 種狀態分別是：

<br>

* `CLOSED`： SSL 關閉了，屬於非正常現象，一但發生需要關閉 In/Out bound。

* `BUFFER_OVERFLOW`：這個情形是　appInData 這個 byteBuffer 滿載了，此時需要 processRecord 一下，把 buffer 中的資料讀出來一些。

* `BUFFER_UNDERFLOW` : 這個情形是 netInData 資料量不足造成的，需要手動從 SocketServer 中讀取出資料。

<br>

每一種狀況像對應的處理方式這邊不全部列舉。source code 上都寫得很清楚。

<br>
<br>

以上的回圈動作基本上可以理解為 SocketServer 把資料寫入 netInData 中，此時 netInData 中的資料是加密過的資料，然後在使用 sslEngine 把 netInData 中的加密資料解密並倒入 appInData 中。當 appInData 裝滿之後就讀取資料做邏輯處裡。當 netInData 資料量不夠時，就繼續從 SocketServer 讀取。

<br>

在最後，需要把這個 read Task 交給 `executorService` 處理。

<br>

```java
executorService.submit(readTask);
```

<br>
<br>


