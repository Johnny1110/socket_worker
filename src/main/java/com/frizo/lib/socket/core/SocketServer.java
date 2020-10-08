package com.frizo.lib.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface SocketServer {

    void init() throws IOException;

    void startup() throws IOException;

    void restart() throws IOException;

    void close() throws IOException;

}
