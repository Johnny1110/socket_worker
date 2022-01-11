# RecordReader

<br>

---

<br>

Reader 的實作只有 2 個，它們都是 `RecordReader` 的實現類別：

* `IoRecordReader`

* `NioRecordReader`

<br>
<br>

## IoRecordReader

<br>

普通 IO RecordReader。實現方法 `processRecord(BufferedInputStream bis)`

<br>

```java
@Override
public void processRecord(BufferedInputStream bis) {
    try {
        byte b1;
        int offset = 0;
        while ((b1 = (byte) bis.read()) != -1) {
            Integer dataSize = ByteLenConverter.getDataSize(b1, bis);
            if (dataSize == null) {
                continue;
            }
            byte[] dataBuffer = new byte[dataSize];
            bis.read(dataBuffer, offset, dataSize);
            output(dataBuffer);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void output(byte[] dataBuffer) {
    try {
        byte[] uncompressed = GZIPStringUtils.uncompress(dataBuffer);
        String result = new String(uncompressed);
        System.out.println("Data:" + result);
        System.out.println("------------------------------");
    }catch (Exception ex){
        System.out.println("uncompress error");
    }
}
```

<br>

總結一下這個實作類的處裡資料流程。接收到的資料是 BufferedInputStream 類型，將資料讀取出來第一個 `byte` 然後交由 `ByteLenConverter` 算出此筆資料長度。

然後藉由這個資料長度建立出一個 `byte[]`。把 `BufferedInputStream` 中的資料讀取出對應大小的單位資料。這筆資料還需要通過解壓縮的方式來進行讀取，具體方式寫在 `output()` 方法中。關於壓縮套件 `GZIPStringUtils`，在 utils 單元有介紹。

<br>
<br>
<br>
<br>

## NioRecordReader

<br>

```java
@Override
public void processRecord(ByteBuffer buffer) {
    try {
        byte b1;
        int offset = 0;
        while (buffer.hasRemaining()) {
            buffer.mark();
            b1 = buffer.get();
            Integer dataSize = ByteLenConverter.getDataSize(b1, buffer);
            if (dataSize == null) {
                continue;
            }      
            if((buffer.limit() - buffer.position()) < (dataSize)) {
                buffer.reset();
                return;
            }
            byte[] dataBuffer = new byte[dataSize];
            buffer.get(dataBuffer, offset, dataSize);
            output(dataBuffer);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
public void output(byte[] dataBuffer) {
    try {
        byte[] uncompressed = GZIPStringUtils.uncompress(dataBuffer);
        String result = new String(uncompressed);
        System.out.println("Data:" + result);
        System.out.println("------------------------------");
    }catch (Exception ex){
        System.out.println("uncompress error");
    }
}
```

<br>

NioRecordReader 的接收資料是 ByteBuffer，這一點跟 IoRecordReader 不一樣。一開始讀取第一個 byte 並傳給 `ByteLenConverter` 解析出此筆資料的單位長度。 

<br>
<br>

----

<br>

說明補充一下 Nio 的 byteBuffer：

### Capacity, Limit, Position, and Mark

<br>

了解 Java NIO buffer 的關鍵, 在於了解 buffer state 以及 flipping. 這邊會先介紹四個關於 buffer state 的主要特性 (properties): capacity, limit, position 以及 mark.

<br>

### Capacity

<br>

Capacity 是一個 buffer 可以持有的資料的最大上限. 舉個例子, 如果你透過 new byte[10] 這個陣列來建立一個 buffer, 那這個 buffer 的 capacity 就是10 bytes. Capacity 在 buffer 建立後就不會再改變了.

<br>

### Limit

<br>

Limit 是一個 zero-based 的索引, 用來辨別第一個 ”不應該” 被讀取/寫入的資料. Limit 可以用來決定資料可否從 buffer 中被讀取. 介於索引 0 與 limit 之間(不包含) 的資料是可以被讀取的, 介於索引 limit (包含) 與 capacity 的資料則是垃圾 (不會被讀到的).

<br>

### Position

<br>

Position 是一個 zero-based 的索引, 用來辨別下一個資料是否可以被讀取/寫入. 當資料從 buffer 中被讀取或是被寫入 buffer 時, position 索引會遞增.

<br>

### Mark

<br>

Mark 是一個被記憶住的位置, 當呼叫 mark() 方法時, 會把 position 的值設定給 mark, 呼叫 reset() 方法時, 會把 mark 的值設定給 position. Mark 的值要設定才會有.

<br>

---

<br>
<br>

  了解上面介紹的 ByteBuffer 特性之後再來看線面這一段 code：

```java
if((buffer.limit() - buffer.position()) < (dataSize)) {
    buffer.reset();
    return;
}
```

<br>

`limit - position` 意思是可供讀取的資料大小，當可供讀取的資料大小 < dataSize 時， buffer 讀取動作做一次 reset（回復讀取第一單位作為 dataSize 的動作）。這樣一來就可以重新補齊一整筆資料（完整 dataSize 大小）然後在下一次進到 RecordReader 裡面讀取。

<br>

