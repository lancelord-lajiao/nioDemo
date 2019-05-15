package bio.client;

import lombok.Cleanup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @ClassName TimeClient
 * @Description //TODO
 * @Date 2019/5/15 11:39
 * @Author jszhang@wisedu
 * @Version 1.0
 **/

public class TimeClient {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {

            }
        }

        Socket socket = null;

        BufferedReader in = null;

        PrintWriter out = null;
        try {
            socket = new Socket("127.0.0.1",port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            out.println("QUERY TIME ORDER");
            System.out.println("Send order 2 server succeed");
            String res = in.readLine();
            System.out.println("now is:"+res);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }

    }
}
