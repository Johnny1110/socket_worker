package com.frizo.lib.socket.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


// ByteBuff 的操作:
// flip(): limit 變到 position 的位置，然後 position 歸 0 (讀取時使用)。
// rewind(): position 歸 0，重新讀取 buffer 時使用。
// clear(): limit 等於總容量，position 歸 0 (寫入時使用)
// compact(): 未讀取的資料移動到開頭， position 設置到資料結尾的下一個位置

public class EchoServer {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open(); // 開啟 selector
        ServerSocketChannel ssc = ServerSocketChannel.open(); //TCP 連接監聽通道

        ssc.bind(new InetSocketAddress(9999)); // 綁定本機 9999 port
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT); // ssc 只對 ACCEPT 敏感

        ByteBuffer buffer = ByteBuffer.allocate(100);

        while (true) {
            selector.select(); // 此處阻塞，當有事件時放行
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next();

                if (key.isAcceptable()){  // 有可行的連接
                    SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();  // 接通連線
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ); // channel 只對 selector 的 READ 敏感 (讓 channel 處理 READ 動作)
                    System.out.println("Connected to the client : " + channel.getRemoteAddress());
                }

                if (key.isReadable()){ // 有資料可讀
                    buffer.clear();
                    // 如果讀到 -1 就跳過
                    if (((SocketChannel) key.channel()).read(buffer) == -1) {
                        System.out.println("close connecton: " + ((SocketChannel) key.channel()).getRemoteAddress());
                        key.channel().close();
                        continue;
                    }

                    System.out.println("----------------------------------------------");

                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        byte b = buffer.get();
                        if (b == 0) { // 客户端消息結束符號 \0
                            System.out.println();
                            ByteBuffer wbuff = ByteBuffer.allocate(100);
                            wbuff.put("Hello, Client!\0".getBytes());
                            wbuff.flip();
                            while (wbuff.hasRemaining()) {
                                ((SocketChannel) key.channel()).write(wbuff);
                            }
                        }else{
                            System.out.print((char)b);
                        }
                    }
                }
                keyIter.remove();
            }
        }
    }

}
