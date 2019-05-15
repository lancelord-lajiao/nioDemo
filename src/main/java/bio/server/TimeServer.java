package bio.server;

import bio.server.TimeServerHandler;
import lombok.Cleanup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName TimeServer
 * @Description //TODO
 * @Date 2019/5/15 13:12
 * @Author jszhang@wisedu
 * @Version 1.0
 **/
public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {

            }
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("time server is start in: " + port);
            Socket socket = null;
            while (true) {
                System.out.println("进入while循环");
                socket = server.accept();
                System.out.println("接收到客户端信息");
                new Thread(new TimeServerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
