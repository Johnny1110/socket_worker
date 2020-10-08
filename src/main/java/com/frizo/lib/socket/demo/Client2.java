package com.frizo.lib.socket.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client2 {

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 8811);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        int num = 0;

        while (num < 10){

            os.write("123456789\0".getBytes());

            num++;
        }

        os.close();
        is.close();
        socket.close();
    }

}
