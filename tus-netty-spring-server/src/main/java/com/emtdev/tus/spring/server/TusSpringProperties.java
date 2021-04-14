package com.emtdev.tus.spring.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tus.netty")
public class TusSpringProperties {

    private String locationPrefix;
    private String basePath;

    private int bossThreads;
    private int workerThreads;
    private boolean useLinuxNativeEpoll;
    private String hostname;
    private int port;

    private NettySocketConfig socket;

    public static class NettySocketConfig {

        private boolean tcpNoDelay = true;
        private int tcpSendBufferSize = -1;
        private int tcpReceiveBufferSize = -1;
        private boolean tcpKeepAlive = false;
        private int soLinger = -1;
        private boolean reuseAddress = false;
        private int acceptBackLog = 1024;


        public boolean isTcpNoDelay() {
            return tcpNoDelay;
        }

        public void setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public int getTcpSendBufferSize() {
            return tcpSendBufferSize;
        }

        public void setTcpSendBufferSize(int tcpSendBufferSize) {
            this.tcpSendBufferSize = tcpSendBufferSize;
        }

        public int getTcpReceiveBufferSize() {
            return tcpReceiveBufferSize;
        }

        public void setTcpReceiveBufferSize(int tcpReceiveBufferSize) {
            this.tcpReceiveBufferSize = tcpReceiveBufferSize;
        }

        public boolean isTcpKeepAlive() {
            return tcpKeepAlive;
        }

        public void setTcpKeepAlive(boolean tcpKeepAlive) {
            this.tcpKeepAlive = tcpKeepAlive;
        }

        public int getSoLinger() {
            return soLinger;
        }

        public void setSoLinger(int soLinger) {
            this.soLinger = soLinger;
        }

        public boolean isReuseAddress() {
            return reuseAddress;
        }

        public void setReuseAddress(boolean reuseAddress) {
            this.reuseAddress = reuseAddress;
        }

        public int getAcceptBackLog() {
            return acceptBackLog;
        }

        public void setAcceptBackLog(int acceptBackLog) {
            this.acceptBackLog = acceptBackLog;
        }
    }


    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public boolean isUseLinuxNativeEpoll() {
        return useLinuxNativeEpoll;
    }

    public void setUseLinuxNativeEpoll(boolean useLinuxNativeEpoll) {
        this.useLinuxNativeEpoll = useLinuxNativeEpoll;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NettySocketConfig getSocket() {
        return socket;
    }

    public void setSocket(NettySocketConfig socket) {
        this.socket = socket;
    }

    public String getLocationPrefix() {
        return locationPrefix;
    }

    public void setLocationPrefix(String locationPrefix) {
        this.locationPrefix = locationPrefix;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
