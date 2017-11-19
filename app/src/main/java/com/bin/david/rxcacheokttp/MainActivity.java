package com.bin.david.rxcacheokttp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.bin.david.cacheokhttp.Retrofit;
import com.bin.david.cacheokhttp.converter.RxConverterFactory;
import com.bin.david.cacheokhttp.core.Callback;
import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.style.FontStyle;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by huang on 2017/11/16.
 */

public class MainActivity extends AppCompatActivity {

    private SmartTable<PM25> smartTable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smartTable = findViewById(R.id.smartTable);
        FontStyle.setDefaultTextSpSize(this,15);
       // startBaseRetrofit();
        startRxRetrofit();

    }

    private void startRxRetrofit(){
        Retrofit retrofit = new Retrofit.Builder().setBaseUrl("http://www.pm25.in/api/")
                .setClient(new OkHttpClient())
                .setConverterFactory(new RxConverterFactory()).build();
        retrofit.create(RxAPIService.class).getWeather("querys","上海","5j1znBVAsnSf5xQyNQyq&avg")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PM25>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<PM25> pm25s) {
                        Log.e("huang","size"+pm25s.size());
                        smartTable.setData(pm25s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    /**
     * 基于OkHttp cell
     */
    private void startBaseRetrofit() {
        Retrofit retrofit = new Retrofit.Builder().setBaseUrl("http://www.pm25.in/api/")
                .setClient(new OkHttpClient()).build();
        retrofit.create(APIService.class).getWeather("querys","上海","5j1znBVAsnSf5xQyNQyq&avg")
                .enqueue(new Callback<List<PM25>>() {
                    @Override
                    public void onResponse(Call call, final List<PM25> response) {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               smartTable.setData(response);
                           }
                       });
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
    }
}
