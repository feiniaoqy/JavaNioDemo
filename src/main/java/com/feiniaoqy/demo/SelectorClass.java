package com.feiniaoqy.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;

/**
 * Created by asus on 2016/4/14.
 */
public class SelectorClass {
    /*发送数据缓冲区*/
    private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);
    /*接受数据缓冲区*/
    private static ByteBuffer rBuffer = ByteBuffer.allocate(1024);
    /*服务器端地址*/
    private InetSocketAddress SERVER;
    private static Selector selector;
    private static SocketChannel client;
    private static String receiveText;
    private static String sendText;
    private static int count=0;

    public SelectorClass(int port){
        SERVER = new InetSocketAddress("localhost", port);
        init();
    }
    public void init(){
        try {
           /*
             * 客户端向服务器端发起建立连接请求
             */
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);//对连接状态
            socketChannel.connect(SERVER);
            /*
             * 轮询监听客户端上注册事件的发生
             */
            while (true) {
                int connectChannels = selector.select();//阻塞到至少有一个通道在你注册的事件上就绪了。
                if(connectChannels == 0) continue;
                Set<SelectionKey> keySet = selector.selectedKeys();
                for(final SelectionKey key : keySet){
                    handle(key);
                };
                keySet.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handle(SelectionKey selectionKey) throws IOException{
        if (selectionKey.isConnectable()) {
            /*
             * 连接建立事件，已成功连接至服务器
             */
            client = (SocketChannel) selectionKey.channel();
            if (client.isConnectionPending()) {
                client.finishConnect();
                System.out.println("connect success !");
                sBuffer.clear();
                sBuffer.put((new Date().toLocaleString()+" connected!").getBytes());
                sBuffer.flip();
                client.write(sBuffer);//发送信息至服务器
                /* 原文来自站长网
                 * 启动线程一直监听客户端输入，有信心输入则发往服务器端
                 * 因为输入流是阻塞的，所以单独线程监听
                 */
                /*new Thread(){
                    @Override
                    public void run() {
                        while(true){
                            try {
                                sBuffer.clear();
                                InputStreamReader input = new InputStreamReader(System.in);
                                BufferedReader br = new BufferedReader(input);
                                sendText = br.readLine();
                                *//*
                                 * 未注册WRITE事件，因为大部分时间channel都是可以写的
                                 *//*
                                sBuffer.put(sendText.getBytes());
                                sBuffer.flip();
                                client.write(sBuffer);
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    };
                }.start();*/
            }
            //注册读事件
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            /*
             * 读事件触发
             * 有从服务器端发送过来的信息，读取输出到屏幕上后，继续注册读事件
             * 监听服务器端发送信息
             */
            client = (SocketChannel) selectionKey.channel();
            rBuffer.clear();
            count=client.read(rBuffer);
            if(count>0){
                receiveText = new String( rBuffer.array(),0,count);
                System.out.println(receiveText);
                client = (SocketChannel) selectionKey.channel();
                client.register(selector, SelectionKey.OP_READ);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        SelectorClass client = new SelectorClass(8070);
    }

}
