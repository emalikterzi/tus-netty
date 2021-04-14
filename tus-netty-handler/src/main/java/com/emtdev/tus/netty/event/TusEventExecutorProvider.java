package com.emtdev.tus.netty.event;

import java.util.concurrent.ExecutorService;

public interface TusEventExecutorProvider {

    ExecutorService getExecutorService();

}
