package com.frizo.lib.socket.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client3 {

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 7854);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        int num = 0;

        while (num < 1000000000){

            os.write("hello world!\0".getBytes());

            num++;
        }

        os.close();
        is.close();
        socket.close();
    }

}
