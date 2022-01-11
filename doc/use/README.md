# 使用

<br>

---

<br>

## IO Server/Client 啟動範例

<br>

### Server：

<br>

```java
SocketServerBooter booter = SocketServerBooter.BooterBuilder.newBuilder()
            .address(new InetSocketAddress(8811))
            .bufferSize(1024000)
            .recordReader(new IoRecordReader())
            .socketType(SocketType.IO_SOCKET)
            .build();
booter.boot();
```

<br>

### Client：

<br>

```java
Socket socket = new Socket("localhost", 8811);
InputStream is = socket.getInputStream();
OutputStream oos = socket.getOutputStream();

while (true){
    String data = "1,2020-10-08 14:26:31,Norman,test";
    byte[] byteData = data.getBytes();
    if (byteData == null) {
        oos.write(ByteLenConverter.NULL_BYTE);
    } else {
        byteData = GZIPStringUtils.compress(byteData);
        byte[] header = ByteLenConverter.intToByte(byteData.length);
        oos.write(header);
        oos.write(byteData);
        oos.flush();
    }
}
```

<br>
<br>
<br>
<br>

## SSL IO Server/Client 啟動範例：

<br>

### Server：

<br>


```java
SocketServerBooter booter = SocketServerBooter.BooterBuilder.newBuilder()
        .address(new InetSocketAddress(8811))
        .bufferSize(1024000)
        .recordReader(new IoRecordReader())
        .keyStoreName("./cfg/ssl_keys/tomcat.jks")
        .keyStorePassword("tomcat")
        .socketType(SocketType.TLS_IO_SOCKET)
        .build();

booter.boot();
```


<br>

### Client：

<br>

```java
String host = "127.0.0.1";
int port = 8811;
String tlsVersion = "TLSv1.2";

String keyStoreName = "./cfg/ssl_keys/client.jks";
String keyStorePassword = "client";


KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
InputStream kstore = new FileInputStream(new File(keyStoreName));
keyStore.load(kstore, keyStorePassword.toCharArray());
KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
kmf.init(keyStore, keyStorePassword.toCharArray());
SSLContext ctx = SSLContext.getInstance("TLS");
ctx.init(kmf.getKeyManagers(), new TrustManager[]{new TrustAllManager()}, null);

            
InetSocketAddress address = new InetSocketAddress(host,port);
SocketFactory factory = ctx.getSocketFactory();
SSLSocket socket = (SSLSocket) factory.createSocket();
socket.connect(address);
socket.setEnabledProtocols(new String[] { tlsVersion });


OutputStream oos = socket.getOutputStream();

int num = 0;

while (num < 100) {
    String message = "1,2020-10-08 14:26:31,Norman,test";
    if (message == null) {
        oos.write(ByteLenConverter.NULL_BYTE);
        oos.flush();
    } else {
        byte[] byteData = message.getBytes();
        byteData = GZIPStringUtils.compress(byteData);
        byte[] header = ByteLenConverter.intToByte(byteData.length);
        oos.write(header);
        oos.write(byteData);
        oos.flush();
    }
    num++;
}

oos.close();
socket.close();
```

<br>
<br>
<br>
<br>

## NIO Server/Client Socket

<br>

### Server：

<br>

```java
SocketServerBooter booter = SocketServerBooter.BooterBuilder.newBuilder()
    .address(new InetSocketAddress(8811))
    .bufferSize(1024000)
    .recordReader(new NioRecordReader())
    .keyStoreName("./cfg/ssl_keys/tomcat.jks")
    .keyStorePassword("tomcat")
    .socketType(SocketType.TLS_NIO_SOCKET)
    .build();

booter.boot();
```

<br>

### Client：

<br>

```java
Socket socket = new Socket("localhost", 8811);
InputStream is = socket.getInputStream();
OutputStream oos = socket.getOutputStream();

while (true){
    String data = "1,2020-10-08 14:26:31,Norman,test";
    byte[] byteData = data.getBytes();
    if (byteData == null) {
        oos.write(ByteLenConverter.NULL_BYTE);
    } else {
        byteData = GZIPStringUtils.compress(byteData);
        byte[] header = ByteLenConverter.intToByte(byteData.length);
        oos.write(header);
        oos.write(byteData);
        oos.flush();
    }
}
```

<br>
<br>
<br>
<br>

## SSL NIO Socket Server/Client

<br>

### Server

<br>

```java
SocketServerBooter booter = SocketServerBooter.BooterBuilder.newBuilder()
    .address(new InetSocketAddress(8811))
    .bufferSize(1024000)
    .recordReader(new NioRecordReader())
    .keyStoreName("./cfg/ssl_keys/tomcat.jks")
    .keyStorePassword("tomcat")
    .socketType(SocketType.TLS_NIO_SOCKET)
    .build();

booter.boot();
```

<br>
<br>

### Client

<br>

```java
String host = "127.0.0.1";
int port = 8811;
String tlsVersion = "TLSv1.2";

String keyStoreName = "./cfg/ssl_keys/client.jks";
String keyStorePassword = "client";


KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
InputStream kstore = new FileInputStream(new File(keyStoreName));
keyStore.load(kstore, keyStorePassword.toCharArray());
KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
kmf.init(keyStore, keyStorePassword.toCharArray());
SSLContext ctx = SSLContext.getInstance("TLS");
ctx.init(kmf.getKeyManagers(), new TrustManager[]{new TrustAllManager()}, null);

            
InetSocketAddress address = new InetSocketAddress(host,port);
SocketFactory factory = ctx.getSocketFactory();
SSLSocket socket = (SSLSocket) factory.createSocket();
socket.connect(address);
socket.setEnabledProtocols(new String[] { tlsVersion });


OutputStream oos = socket.getOutputStream();

int num = 0;

while (num < 100) {
    String message = "1,2020-10-08 14:26:31,Norman,test";
    if (message == null) {
        oos.write(ByteLenConverter.NULL_BYTE);
        oos.flush();
    } else {
        byte[] byteData = message.getBytes();
        byteData = GZIPStringUtils.compress(byteData);
        byte[] header = ByteLenConverter.intToByte(byteData.length);
        oos.write(header);
        oos.write(byteData);
        oos.flush();
    }
    num++;
}

oos.close();
socket.close();
```


