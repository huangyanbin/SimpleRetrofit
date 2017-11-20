package com.bin.david.cacheokhttp.converter;


import com.bin.david.cacheokhttp.core.Call;

import java.lang.reflect.Type;

/**
 * Created by huang on 2017/11/17.
 */

public interface IConverterFactory<T> {

    <R> T converter(Call<R> call);

}
