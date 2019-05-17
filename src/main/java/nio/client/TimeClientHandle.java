package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName AsyncTimeClientHandle
 * @Description //TODO
 * @Date 2019/5/16 12:19
 * @Author jszhang@wisedu
 * @Version 1.0
 **/
public class TimeClientHandle implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClientHandle(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            // 打开一个socketChannel
            socketChannel = SocketChannel.open();
            //设置为非阻塞模式
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            System.out.println(
                    "client初始化异常"
            );
        }
    }


    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //循环获取准备就绪的key
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    handleInput(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {

        if (key.isValid()) {
        SocketChannel sc = (SocketChannel) key.channel();
        if(key.isConnectable()){
            if(sc.finishConnect()){
                //注册读事件到多路复用器
                sc.register(selector,SelectionKey.OP_READ);
                doWrite(sc);
            }else {
                System.exit(1);
            }
        }
        if(key.isReadable()){
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int readBytes = sc.read(readBuffer);
            if(readBytes>0){
                readBuffer.flip();
                byte[] bytes = new byte[readBuffer.remaining()];
                readBuffer.get(bytes);
                String body = new String (bytes,"UTF-8");
                System.out.println("new is:"+body);
                this.stop = true;
            }else if(readBytes<0){
                key.cancel();
                sc.close();
            }else {

            }

        }

        }
    }

    private void doConnect() throws IOException {
        //异步连接客户端，若连接成功，直接注册读状态位到多路复用器，
        // 如果没有连接成功
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            //向复用器注册连接状态位，监听服务ack应答
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        if (!writeBuffer.hasRemaining()) {
            System.out.println("send order 2 server succeed.");
        }
    }


}
