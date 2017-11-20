package com.bin.david.cacheokhttp.core;

import com.bin.david.cacheokhttp.adapter.DefaultCallAdapterFactory;
import com.bin.david.cacheokhttp.adapter.ICallAdapterFactory;
import com.bin.david.cacheokhttp.converter.IConverterFactory;
import com.bin.david.cacheokhttp.converter.DefaultConverterFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.*;


/**
 * Created by huang on 2017/11/17.
 */

public class APIService {

    private OkHttpClient okHttpClient;
    private String baseUrl;
    private IConverterFactory converterFactory;
    private ICallAdapterFactory callAdapterFactory;

    protected okhttp3.Call createCall(Request request){
        OkHttpClient client = getOkHttpClient();
       return client.newCall(request);
    }



    private OkHttpClient getOkHttpClient() {
        if(okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public IConverterFactory getConverterFactory() {
        if(converterFactory == null){
            converterFactory = new DefaultConverterFactory();
        }
        return converterFactory;
    }

    public void setConverterFactory(IConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    public Type getTypeByClassName(String className){
        try {
            Class<?> clazz = Class.forName(className);
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void setCallGenericReturnType(Method method,Call<?> call){
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType) {
            Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
            call.setGenericType(genericType);
        }
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
}
