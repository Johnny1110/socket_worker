package com.frizo.lib.socket.exception;

public class SocketServerBooterBuilderExcepton extends IllegalStateException{
    public SocketServerBooterBuilderExcepton(String msg){
        super(msg);
    }

    public SocketServerBooterBuilderExcepton(String msg, Exception ex){
        super(msg, ex);
    }
}
