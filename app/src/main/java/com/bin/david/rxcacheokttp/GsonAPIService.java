package com.bin.david.rxcacheokttp;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Head;
import com.bin.david.cacheokhttp.annotation.Path;
import com.bin.david.cacheokhttp.annotation.Query;
import com.bin.david.cacheokhttp.core.Call;

import java.util.List;

/**
 * Created by huang on 2017/11/16.
 */

public interface GsonAPIService {

    @Get("{query}/pm10.json")
    @Head("Cache-Control:max-age=640000")
    Call<List<PM25>> getWeather(@Path("query") String query, @Query("city") String city, @Query("token") String token);
}
