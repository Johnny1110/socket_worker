# Core

<br>

---

<br>

核心部分主要有 2 個 interface，2 個 class，1 個 enum。

<br>
<br>


## Interface

<br>

### SockerServer

<br>

SockerServer 一共有 4 個需要被實現的方法，所有種類的 SocketServer 都必須要實作這個類別。

<br>

```java
void init() throws Exception;
```

<br>

```java
void startup() throws IOException;
```

<br>

```java
void restart() throws Exception;
```

<br>

```java
void close() throws IOException;
```

<br>
<br>

### RecordReader

<br>

RecordReader 作為實現資料讀取的規範，只定義一個需要被實現的方法：

<br>

```java
void processRecord(T t);
```

<br>
<br>

## Class

<br>

### SocketServerBooter

<br>

這個類別是 SocketServer 的啟動類別，建構時需要傳入 SocketServer 實作類別。

這個 class 的建立需要大量參數，在設計時不想在建構函式傳入大量參數，所以使用 builder 設計模式。

以下列舉建構 SocketServerBooter 所需要的參數：

<br>

* `SocketType`：SocketType 是一個列舉，有 4 種 type 可以選擇 `NIO_SOCKET, IO_SOCKET, TLS_IO_SOCKET, TLS_NIO_SOCKET`。

* `bufferSize`：定義 buffer 緩衝區初始話的大小。

* `address`：address 需要傳入一個 InetSocketAddress 類別，可以供指定一個 port，讓 socket 監聽。

* `recordReader` 需要傳入一個 `RecordReader` 的實現類別，他是定義了 socket 收到資料後要如何處理資料。

* `keyStoreName` 傳入 SSL 憑證的檔案位置 (只有 SSL 相關的 SocketServer 才需要)。

* `keyStorePassword` 傳入 SSL 憑證密碼 (只有 SSL 相關的 SocketServer 才需要)。

<br>
<br>

## 建構方法

<br>

當參數輸入完成後，就可以使用 `build` 方法建構出 SocketServerBooter。

<br>

```java
public SocketServerBooter build();
```

<br>
<br>

### SocketTLSClient

<br>

這個類別是一個 SSL client 的 demo，做為測試使用的，他使用 SSL 協定去連接 SSL Socket Server。

<br>
<br>

### SocketTLSClient

<br>

這是一個列舉類，這裡一共有 4 個種類 Socket 供選擇：

<br>

```java
NIO_SOCKET, IO_SOCKET, TLS_IO_SOCKET, TLS_NIO_SOCKET
```

<br>

