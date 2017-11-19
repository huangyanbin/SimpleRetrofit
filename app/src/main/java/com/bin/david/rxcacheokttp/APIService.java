package com.bin.david.rxcacheokttp;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Path;
import com.bin.david.cacheokhttp.annotation.Query;
import com.bin.david.cacheokhttp.core.Call;

import java.util.List;

/**
 * Created by huang on 2017/11/16.
 */

public interface APIService {

    @Get("{query}/pm10.json")
    Call<List<PM25>> getWeather(@Path("query") String query, @Query("city")String city,@Query("token")String token);
}
