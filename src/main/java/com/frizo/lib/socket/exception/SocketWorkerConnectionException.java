package com.frizo.lib.socket.exception;

public class SocketWorkerConnectionException extends RuntimeException{

    public SocketWorkerConnectionException(String msg, Exception ex){
        super(msg, ex);
    }

    public SocketWorkerConnectionException(Exception ex){
        this("ERROR: Server encounter connection problems.", ex);
    }

}
