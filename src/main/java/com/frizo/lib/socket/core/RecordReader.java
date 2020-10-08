package com.frizo.lib.socket.core;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface RecordReader<T> {

    void processRecord(T t);

}
