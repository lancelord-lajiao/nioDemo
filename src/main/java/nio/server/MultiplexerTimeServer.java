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
            //将serverSocket注册到多路复用器上，监听accept事件
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
                //多路复用器无线循环准备就绪的key
                //selector每隔ms 激活一次 当有处于就绪的channel的时selector或会被激活 返回selectionKey
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
                //处理心得接入请求，完成tcp3次握手
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                serverSocketChannel.configureBlocking(false);
                //将新接入的客户端连接注册到selector，监听读取操作，读取客户端的网络
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            //读取数据
            if (selectionKey.isReadable()) {
                SocketChannel serverSocketChannel = (SocketChannel) selectionKey.channel();
               //首先开辟一个1mb的缓冲区
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //调用socketchannel的read读取字节流，因为socketChannel已经设置了异步非阻塞，所以read是非阻塞的，
                // 使用返回值进行判断
                int readBytes = serverSocketChannel.read(readBuffer);
                //对字节码进行解码
                if (readBytes > 0) {
                  //将limit设置为position，position设置为0，用去后续对缓冲区的读取
                    readBuffer.flip();
                    //然后根据缓冲区可读的字节个数创建字节数组
                    byte[] bytes = new byte[readBuffer.remaining()];
                    //调用ByteBuffer的get操作将缓冲区的可读字节数组复制到新创建的字节数组
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("the time server received order:" + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)?new Date(currentTimeMillis()).toString():"Bad Order";
                    doWrite(serverSocketChannel,currentTime);
                }

            }

        }


    }

    private void doWrite(SocketChannel socketChannel, String response) throws IOException {
        if(response!=null &&response.length()>0){
            byte[] bytes= response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
           //socketChannel是异步非阻塞的，不能保证一次性把需要发送的字节数组发送完，会出现半写包的问题
            //要注册写操作，不断轮询selector将没有发送完的ByteBuffer发送完，然后判断byteBuffer的hasRemain方法判断消息是否发送完成
            socketChannel.write(writeBuffer);
        }
    }
}
