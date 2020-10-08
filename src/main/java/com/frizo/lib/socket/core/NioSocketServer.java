package com.frizo.lib.socket.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class NioSocketServer implements SocketServer {

    private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private RecordReader recordReader;

    private Selector selector;

    private ServerSocketChannel ssc;

    private int bufferSize;

    private InetSocketAddress address;

    private Map<String, ByteBuffer> cacheBuffers;

    public NioSocketServer(RecordReader reader, InetSocketAddress address, int bufferSize) {
        this.address = address;
        this.recordReader = reader;
        this.bufferSize = bufferSize;
    }

    @Override
    public void init() throws IOException {
        this.cacheBuffers = new HashMap<>(); // 建立緩存 buffer
        logger.info("NioSocketServer initializined.");
    }

    @Override
    public void startup() throws IOException {
        this.selector = Selector.open(); // 開啟 selector
        this.ssc = ServerSocketChannel.open(); //TCP 連接監聽通道
        this.ssc.getLocalAddress();
        this.ssc.bind(address);
        this.ssc.configureBlocking(false);
        this.ssc.register(selector, SelectionKey.OP_ACCEPT); // ssc 只對 ACCEPT 敏感

        logger.info("NioSocketServer binding with: " + ssc.getLocalAddress());

        new Thread(() -> {
            while (selector.isOpen()) {
                try {
                    selector.select(); // 此處阻塞，當有事件時放行
                } catch (IOException e) {
                    logger.error("Selector encounter some problems.", e);
                }

                try {
                    Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                    while (keyIter.hasNext()) {
                        SelectionKey key = keyIter.next();
                        keyIter.remove();  // 事件取出後要手動移除。

                        if (key.isAcceptable()) { // 有 Accpetable 的事件
                            processAcceptable(key);
                        }

                        if (key.isReadable()) { // 有資料可讀的事件
                            processReadable(key);
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }).start();
        logger.info("NioSocetServer already started.");
    }

    @Override
    public void restart() throws IOException {
        logger.info("trying to restart NioSocketServer.");
        selector.close();
        ssc.close();
        init();
        startup();
        logger.info("NioSocketServer restart successfully.");
    }

    private void processReadable(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        String remoteAddr = String.valueOf(channel.getRemoteAddress());
        ByteBuffer cacheBuffer = cacheBuffers.get(remoteAddr);
        if (cacheBuffer.hasRemaining()) {
            cacheBuffer.flip();  // 緩存 buffer 準備讀取
            buffer.put(cacheBuffer); // 將緩存資料放入 buffer 中。
        }

        try {
            if (channel.read(buffer) == -1) { // 此處寫入資料到 buffer 中
                // 當 read 返回 -1 時，說明 client 那邊已經關掉了。(正常調用 socket.close() 離開)
                logger.info("disconnected with clinet [" + remoteAddr + "].");
                cacheBuffers.remove(remoteAddr);
                channel.close();
            }
        } catch (IOException e) {
            // 當 client 被暴力關閉時，進入以下邏輯處裡。
            logger.warn("client [" + remoteAddr + "] has been crash, due to encountered some unknown problem.");
            logger.debug("client [" + remoteAddr + "] has been crash, due to encountered some unknown problem.", e);
            cacheBuffers.remove(remoteAddr);
            channel.close();
        }

        buffer.flip(); //  buffer 調整至讀取狀態。
        recordReader.processRecord(buffer);

        cacheBuffer.clear();
        if (buffer.hasRemaining()) {
            cacheBuffer.put(buffer);
        }
        cacheBuffers.put(remoteAddr, cacheBuffer);
    }

    private void processAcceptable(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();  // 接通連線
        channel.configureBlocking(false); // 設定為非阻塞
        channel.register(selector, SelectionKey.OP_READ); // channel 只對 selector 的 READ 敏感 (讓 channel 處理 READ 動作)
        logger.info("Accepted the client connection request: " + channel.getRemoteAddress());
        cacheBuffers.put(String.valueOf(channel.getRemoteAddress()), ByteBuffer.allocate(bufferSize));
        logger.info("Created cache buffer with remoteAddress: " + channel.getRemoteAddress());
    }

    @Override
    public void close() {
        try {
            selector.close();
            ssc.close();
            logger.info("NioSocketServer closed successfully.");
        } catch (IOException e) {
            logger.error("Failed to close NioSocketServer.", e);
        }
    }
}
