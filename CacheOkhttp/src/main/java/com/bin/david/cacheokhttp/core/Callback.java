package com.bin.david.cacheokhttp.core;


import com.google.gson.Gson;

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


    @Override
    public void onResponse(Call call, Response resp) throws IOException {
        if(resp.isSuccessful()) {
            String response = null;
            T t = null;
            try {
                response = resp.body().string();
                if (response != null) {
                    Gson gson = new Gson();
                    Type type = getType();
                    if (type == String.class || type == Object.class) {
                        t = (T) response;
                    } else {
                        t = gson.fromJson(response, type);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            onResponse(call, t);
        }
    }


    public abstract void onResponse(Call call, T response);


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

    public void setGenericType(Type genericType) {
        this.genericType = genericType;
    }
}
