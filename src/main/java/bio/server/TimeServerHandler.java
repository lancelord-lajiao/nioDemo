package bio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import static java.lang.System.*;

/**
 * @ClassName TimeServerHandler
 * @Description //TODO
 * @Date 2019/5/15 11:13
 * @Author jszhang@wisedu
 * @Version 1.0
 **/
public class TimeServerHandler implements Runnable{
    private Socket socket;
    public TimeServerHandler(Socket socket){
        this.socket = socket;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out  = null;
        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(this.socket.getOutputStream(),true);
            String currentTime = null;
            String body = null;
            while (true){
                body = in.readLine();
                if(body == null){
                    break;
                }
                System.out.println("the time server receive order :"+body);
                currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)?new Date(currentTimeMillis()).toString():"Bad Order";
                out.println(currentTime);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(in != null ){
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if(out!=null){
                out.close();
                out=null;
            }
            if (socket !=null){
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            socket=null;
        }
    }
}
