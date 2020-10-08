package com.frizo.lib.socket;

import com.frizo.lib.socket.core.*;
import com.frizo.lib.socket.demo.NioRecordReader;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketServerBooter booter = SocketServerBooter.BooterBuilder.newBuilder()
                .address(new InetSocketAddress("localhost", 7854))
                .bufferSize(64)
                .recordReader(new NioRecordReader())
                .socketType(SocketType.NIO_SOCKET)
                .build();

        booter.boot();

        Thread.sleep(4000L);

        booter.restart();

        Thread.sleep(4000L);

        booter.close();
    }
}
