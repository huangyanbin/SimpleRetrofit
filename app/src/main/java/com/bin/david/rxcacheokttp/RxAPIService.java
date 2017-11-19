package com.bin.david.rxcacheokttp;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Path;
import com.bin.david.cacheokhttp.annotation.Query;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by huang on 2017/11/16.
 */

public interface RxAPIService {

    @Get("{query}/pm10.json")
    Observable<List<PM25>> getWeather(@Path("query") String query, @Query("city") String city, @Query("token") String token);
}
