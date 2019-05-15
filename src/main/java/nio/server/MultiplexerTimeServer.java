package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static java.lang.System.currentTimeMillis;

/**
 * @ClassName MultiplexerTimeServer
 * @Description //TODO
 * @Date 2019/5/15 17:12
 * @Author jszhang@wisedu
 * @Version 1.0
 **/
public class MultiplexerTimeServer implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop;


    public MultiplexerTimeServer(int port) {
        try {
            //创建多路复用器
            selector = Selector.open();
            //打开ServerSocketChannel,用于监听客户端连接，他是所有客户端连接的父管道
            serverSocketChannel = ServerSocketChannel.open();
            //绑定监听端口，设置连接为非阻塞
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            //将serverSocket注册到多路复用器上，监听accept时间
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("the time server is start in " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("selector 注册异常");
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    public void run() {
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                SelectionKey selectionKey = null;
                while (selectionKeyIterator.hasNext()) {

                    selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    handleInput(selectionKey);
                    if(selectionKey!=null){
                        selectionKey.cancel();
                        if(selectionKey.channel()!=null){
                            selectionKey.channel().close();
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //当多路复用器selector被关闭以后，注册在上面的channel和pipe等资源都会被注册和关闭，不需要重复释放
        if(selector!=null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("-------------------selector关闭失败-------------------");
            }
        }
    }

    private void handleInput(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isValid()) {
            //处理新接入的请求消息
            if (selectionKey.isAcceptable()) {
                //
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                serverSocketChannel.configureBlocking(false);
                //新建和selector的连接
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            //读取数据
            if (selectionKey.isReadable()) {
                SocketChannel serverSocketChannel = (SocketChannel) selectionKey.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = serverSocketChannel.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("the time server received order:" + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)?new Date(currentTimeMillis()).toString():"Bad Order";
                    doWrite(serverSocketChannel,currentTime);
                }

            }

        }


    }

    private void doWrite(SocketChannel serverSocketChannel, String response) throws IOException {
        if(response!=null &&response.length()>0){
            byte[] bytes= response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            serverSocketChannel.write(writeBuffer);
        }
    }
}
