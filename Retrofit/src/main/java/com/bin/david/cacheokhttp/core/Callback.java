package com.bin.david.cacheokhttp.core;


import com.bin.david.cacheokhttp.adapter.ICallAdapterFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.*;
import okhttp3.Call;

/**
 * Created by huang on 2017/11/17.
 */

public abstract class Callback<T> implements okhttp3.Callback {

    private Type genericType;
    private ICallAdapterFactory callAdapterFactory;

    @Override
    public void onResponse(Call call, Response resp) throws IOException {
        if(resp.isSuccessful()) {
            T t = callAdapterFactory.converter(resp,getType());
            if(t != null) {
                onResponse(call, t);
            }else{
                onFailure(call,resp);
            }
        }else {
            onFailure(call,resp);
        }
    }


    public void onFailure(Call call, Response resp){

    }
    public abstract void onResponse(Call call, T response) throws IOException;


    private Type getType(){
        if(genericType != null){
            return genericType;
        }else {
            Type type = String.class;
            Type mySuperClass = this.getClass().getGenericSuperclass();
            if (mySuperClass instanceof ParameterizedType) {
                type = ((ParameterizedType) mySuperClass).getActualTypeArguments()[0];
            }
            return type;
        }
    }


    public void setCallAdapterFactory(ICallAdapterFactory callAdapterFactory) {
        this.callAdapterFactory = callAdapterFactory;
    }

    public void setGenericType(Type genericType) {
        this.genericType = genericType;
    }
}
