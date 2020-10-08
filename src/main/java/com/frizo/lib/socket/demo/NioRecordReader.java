package com.frizo.lib.socket.demo;

import com.frizo.lib.socket.core.RecordReader;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class NioRecordReader implements RecordReader<ByteBuffer> {
    @Override
    public void processRecord(ByteBuffer buffer) {
        if (buffer != null) {
            System.out.println(Arrays.toString(buffer.array()));
            while (buffer.hasRemaining()) {
                byte b = buffer.get();

                if (b == 0) { // 客户端消息收到 \0
                    System.out.println();
                    System.out.println("after b==0: position:" + buffer.position() + " limit: " + buffer.limit());
                    return;
                }else{
                    System.out.print((char) b);
                }
            }
        }
    }
}
