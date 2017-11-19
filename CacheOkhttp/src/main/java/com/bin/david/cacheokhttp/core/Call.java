package com.bin.david.cacheokhttp.core;

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

    @Override
    public okhttp3.Call clone() {
        return call.clone();
    }
}
