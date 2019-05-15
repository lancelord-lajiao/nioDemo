package maskbio.server;

import bio.server.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName TimeServer
 * @Description //TODO
 * @Date 2019/5/15 13:33
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
            TimeServerHandlerExecutePool timeServerHandlerExecutePool = new TimeServerHandlerExecutePool(50,1000);
            while (true) {
                System.out.println("进入while循环");
                socket = server.accept();
                System.out.println("接收到客户端信息");
                timeServerHandlerExecutePool.execute(new TimeServerHandler(socket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
