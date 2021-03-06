package com.emtdev.tus;

import com.emtdev.tus.initializer.TusChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.FutureListener;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(channelClass).childHandler(new TusChannelInitializer());

        InetSocketAddress addr = new InetSocketAddress(1080);
        //        if (configCopy.getHostname() != null) {
        //            addr = new InetSocketAddress(configCopy.getHostname(), configCopy.getPort());
        //        }

        b.bind(addr).addListener(new FutureListener() {

            @Override
            public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                System.out.println("server Starterd");
            }


        });
    }


//    public static void main(String[] args) {
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        Future<Object> objectFuture = executorService.submit(new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
//                Thread.sleep(5000);
//                System.out.println("finisgded");
//                return 1;
//            }
//
//            ;
//        });
//
//        while (true) {
//            System.out.println(objectFuture.isDone());
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
