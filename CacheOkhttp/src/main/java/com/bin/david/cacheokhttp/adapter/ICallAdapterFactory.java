package com.bin.david.cacheokhttp.adapter;


import com.bin.david.cacheokhttp.core.Call;

import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by huang on 2017/11/17.
 */

public interface ICallAdapterFactory<T> {

    T converter(Callback callback);
}
