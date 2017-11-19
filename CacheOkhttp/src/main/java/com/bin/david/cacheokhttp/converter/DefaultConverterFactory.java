package com.bin.david.cacheokhttp.converter;


import com.bin.david.cacheokhttp.core.Call;

import java.lang.reflect.Type;

/**
 * Created by huang on 2017/11/17.
 */

public class DefaultConverterFactory implements IConverterFactory<Call> {


    @Override
    public <R> Call converter(Call<R> call) {
        return call;
    }


}
