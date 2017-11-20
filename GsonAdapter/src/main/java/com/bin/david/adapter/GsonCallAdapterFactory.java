package com.bin.david.adapter;


import com.bin.david.cacheokhttp.adapter.ICallAdapterFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by huang on 2017/11/17.
 */

public class GsonCallAdapterFactory implements ICallAdapterFactory {


    @Override
    public <T> T converter(Response response, Type returnType) {
        T t = null;
        try {
            ResponseBody body = response.body();
            if(body != null) {
                String respStr = body.string();
                if (respStr != null) {
                    Gson gson = new Gson();
                    if (returnType == String.class || returnType == Object.class) {
                        t = (T) respStr;
                    } else {
                        t = gson.fromJson(respStr, returnType);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }
}
