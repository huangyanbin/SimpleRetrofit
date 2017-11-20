package com.bin.david.cacheokhttp.adapter;


import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by huang on 2017/11/17.
 */

public class DefaultCallAdapterFactory implements ICallAdapterFactory {


    @Override
    public ResponseBody converter(Response response, Type returnType) {

        return response.body();
    }
}
