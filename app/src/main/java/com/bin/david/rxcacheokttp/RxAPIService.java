package com.bin.david.rxcacheokttp;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Path;
import com.bin.david.cacheokhttp.annotation.Post;
import com.bin.david.cacheokhttp.annotation.Query;
import com.bin.david.cacheokhttp.annotation.QueryMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by huang on 2017/11/16.
 */

public interface RxAPIService {

    @Get("{query}/pm10.json")
    Observable<List<PM25>> getWeather(@Path("query") String query, @QueryMap Map<String,String> params);
}
