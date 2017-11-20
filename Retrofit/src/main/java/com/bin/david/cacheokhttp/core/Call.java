package com.bin.david.cacheokhttp.core;

import com.bin.david.cacheokhttp.adapter.DefaultCallAdapterFactory;
import com.bin.david.cacheokhttp.adapter.ICallAdapterFactory;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by huang on 2017/11/17.
 */

public class Call<T>{
    private okhttp3.Call call;
    private Type genericType;
    private ICallAdapterFactory callAdapterFactory;

    public Call(okhttp3.Call call){
        this.call = call;
    }

    public Request request() {
        return call.request();
    }

    public Response execute() throws IOException {
        return call.execute();
    }

    public void enqueue(Callback<T> callback) {
        callback.setGenericType(genericType);
        callback.setCallAdapterFactory(getCallAdapterFactory());
        call.enqueue(callback);
    }


    public void cancel() {
        call.cancel();

    }

    public void setGenericType(Type genericType){
        this.genericType = genericType;
    }

    public boolean isExecuted() {
        return call.isExecuted();
    }

    public boolean isCanceled() {
        return call.isCanceled();
    }

    public ICallAdapterFactory getCallAdapterFactory() {
        if(callAdapterFactory == null){
            callAdapterFactory = new DefaultCallAdapterFactory();
        }
        return callAdapterFactory;
    }

    public void setCallAdapterFactory(ICallAdapterFactory callAdapterFactory) {
        this.callAdapterFactory = callAdapterFactory;
    }

    @Override
    public okhttp3.Call clone() {
        return call.clone();
    }
}
