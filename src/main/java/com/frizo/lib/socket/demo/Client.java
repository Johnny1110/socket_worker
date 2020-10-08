package com.frizo.lib.socket.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 8811);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        int num = 0;

        while (num < 5){
            Thread.sleep(2000L);
            os.write("Hello, Server!\0".getBytes());
            num += 1;
//            int b;
//            while ((b = is.read()) != 0) {
//                System.out.print((char) b);
//            }

            System.out.println("已送出消息。" + num);

        }


        socket.close();

    }

}
