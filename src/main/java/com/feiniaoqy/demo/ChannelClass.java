package com.feiniaoqy.demo;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by asus on 2016/4/14.
 */
public class ChannelClass {
    private String BASEPATH = System.getProperty("user.dir")+"/JavaNioDemo/src/reasource";
    /**
     * 读取数据到Buffer
     */
    public void useChannel(){
        RandomAccessFile aFile = null;
        try {
            aFile = new RandomAccessFile("F:/nio-data.txt", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileChannel inChannel = aFile.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(48);//为buffer分配空间

        int bytesRead = 0;
        try {
            bytesRead = inChannel.read(buf);//从通道读取数据到buffer
            //也可以通过put方法写;
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (bytesRead != -1) {
            System.out.println("Read " + bytesRead+"\n");
            buf.flip();//把索引置0 从写模式切换到读模式。set limit = position;set position = 0;
            //在读的模式下：buf.rewind()将position设回0，所以你可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）。
            while(buf.hasRemaining()){//如有余下，就继续循环
                System.out.print((char) buf.get());//一个一个的字符输出
            }
            System.out.println("\n");
            buf.clear();//清空缓存
            try {
                bytesRead = inChannel.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            aFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过Buffer向文件写入数据
     */
    public void bufferToChannel(){
        //System.getProperty("user.dir") 获取当前项目的路径
        String string = "hi,我是要从Buffer写到Channel中";
        byte [] bytes = string.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        byteBuffer.put(bytes);
        RandomAccessFile aFile = null;
        try {
            aFile = new RandomAccessFile(System.getProperty("user.dir")+"/JavaNioDemo/src/reasource"+"/nio-data.txt", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileChannel fileChannel = aFile.getChannel();
        /*try {
            Selector selector = Selector.open();
            SocketChannel socketChannel = null;
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }*/


        byteBuffer.flip();
        //因为有可能一次没有写完
        while(byteBuffer.hasRemaining()) {
            try {
                fileChannel.write(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //因为有可能从Buffer写入的数据没有及时的写入磁盘中
            fileChannel.force(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byteBuffer.clear();
        try {
            aFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通道之间的通信
     */
    public void channelTransform(){
        RandomAccessFile fromFile = null;
        try {
            fromFile = new RandomAccessFile(BASEPATH+"/fromFile.txt", "rw");
        } catch (FileNotFoundException e) {
            try {
                e.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        FileChannel fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = null;
        try {
            toFile = new RandomAccessFile(BASEPATH+"/toFile.txt", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileChannel toChannel = toFile.getChannel();

        long position = 0;
        long count = 0;
        try {
            count = fromChannel.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fromChannel.transferTo(0, count, toChannel);
            toChannel.force(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //SocketChannel
    public void socketChannel(){
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);//非阻塞模式
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8070));//这里得连接socket
            while(! socketChannel.finishConnect() ){
                //wait, or do something else...
            }
            //当连接完成
            ByteBuffer buf = ByteBuffer.allocate(1024);
            //从socketChannel读数据
            int byteRead = socketChannel.read(buf);
            while (byteRead != -1) {
                if (byteRead>0){
                    buf.flip();//把索引置0 从写模式切换到读模式。set limit = position;set position = 0;
                    String s = getString(buf);
                    System.out.println(s+"\n");
                    buf.clear();//清空缓存
                    break;
                }
                try {
                    byteRead = socketChannel.read(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            buf.clear();
            //写数据向socketChannel
            String string = "hello,服务端！！！！";
            byte[] bytes = string.getBytes("UTF-8");
            buf.put(bytes);
            buf.flip();
            while(buf.hasRemaining()) {
                try {
                    socketChannel.write(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //socketChannel.
            System.out.println(buf.get(3));
            buf.clear();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 字节码转换为String
     * @param buffer
     * @return
     */
    public String getString(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;

        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            //用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }





}
