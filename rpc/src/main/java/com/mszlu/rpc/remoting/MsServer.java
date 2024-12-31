package com.mszlu.rpc.remoting;

import com.mszlu.rpc.server.MsServiceProvider;

public interface MsServer {
    
    void run();

    void stop();

    void setMsServiceProvider(MsServiceProvider msServiceProvider);

    boolean isRunning();
}