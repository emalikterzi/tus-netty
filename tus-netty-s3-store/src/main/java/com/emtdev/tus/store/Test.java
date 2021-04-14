package com.emtdev.tus.store;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;

public class Test {

    private final static String bucket = "test-bucket-tus";

    public static void main(String[] args) throws Exception {
//        AmazonS3ClientBuilder builder
//                = AmazonS3Client.builder();
//        builder
//                .setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIA55YSRGOJNNOCXM6B", "p5t+/Rkobou37AUYjInyLBLUKkendXUuW8P+83CJ")));
//        builder.setRegion("eu-west-1");
//        AmazonS3 amazonS3 = builder.build();
//
//        amazonS3.putObject("test-bucket-tus", "t1", new File("/Users/enes.terzi/test.json"));
        final UnpooledByteBufAllocator unpooledByteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
        ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.directBuffer();

        final TestInputStream testInputStream = new TestInputStream(byteBuf);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < 100; i++) {
                    System.out.println("writing byte");
                    testInputStream.writeByte(unpooledByteBufAllocator.directBuffer().writeByte(i));
                }
            }
        });
        t1.start();

        while (true) {
            byte[] a1 = new byte[50];
            System.out.println(testInputStream.read(a1));
            System.out.println("");
        }
    }

    public static class TestInputStream extends InputStream {

        private final ByteBuf byteBuf;
        private final Object __locker = new Object();
        private boolean __locked = false;
        private boolean __closed = false;

        public TestInputStream(ByteBuf byteBuf) {
            this.byteBuf = byteBuf;
        }

        @Override
        public void close() throws IOException {
            synchronized (this.__locker) {
                this.__closed = true;
                this.__locked = false;
                this.__locker.notifyAll();
            }
        }

        @Override
        public int read() throws IOException {
            /**
             * waits for bytes
             */
            synchronized (this.__locker) {
                if (!byteBuf.isReadable() && !__closed && !__locked) {
                    try {
                        System.out.println("wating for byte");
                        __locked = true;
                        this.__locker.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (byteBuf.isReadable()) {
                    return byteBuf.readByte();
                }


            }
        }

        public void writeByte(ByteBuf value) {
            synchronized (__locker) {
                if (__locked) {
                    __locked = false;
                    __locker.notifyAll();
                }

                byteBuf.writeBytes(value);
            }
        }

        public void writeByte(int value) {
            synchronized (__locker) {
                if (__locked) {
                    __locked = false;
                    __locker.notifyAll();
                }

                byteBuf.writeByte(value);
            }
        }

        private boolean isReadable() {
            return true;
        }
    }
}
