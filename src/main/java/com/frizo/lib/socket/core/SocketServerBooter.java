package com.frizo.lib.socket.core;

import com.frizo.lib.socket.exception.SocketServerBooterBuilderExcepton;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocketServerBooter {

    private SocketServer server;

    private SocketServerBooter(SocketServer server){
        this.server = server;
    }

    public void boot() throws IOException {
        server.init();
        server.startup();
    }

    public void restart() throws IOException {
        server.restart();
    }

    public void close() throws IOException {
        server.close();
    }

    public static class BooterBuilder {

        private RecordReader recordReader;

        private int bufferSize;

        private InetSocketAddress address;

        private SocketType type;

        private BooterBuilder(){

        }

        public static BooterBuilder newBuilder() {
            BooterBuilder builder = new BooterBuilder();
            builder.address = new InetSocketAddress(0); // 預設 localhost:隨機
            builder.bufferSize = 1024;
            return builder;
        }

        public BooterBuilder socketType(SocketType type){
            this.type = type;
            return this;
        }

        public BooterBuilder bufferSize(int bufferSize){
            this.bufferSize = bufferSize;
            return this;
        }

        public BooterBuilder address(InetSocketAddress address){
            this.address = address;
            return this;
        }

        public BooterBuilder recordReader(RecordReader reader){
            this.recordReader = reader;
            return this;
        }

        public SocketServerBooter build(){
            if (type.equals(SocketType.NIO_SOCKET)){
                SocketServer server = new NioSocketServer(recordReader, address, bufferSize);
                return new SocketServerBooter(server);
            }else{
                throw new SocketServerBooterBuilderExcepton("Not support IO socket yet, use NIO socket please.");
            }
        }
    }

}
