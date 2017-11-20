package com.bin.david.converter;


import com.bin.david.cacheokhttp.converter.IConverterFactory;
import com.bin.david.cacheokhttp.core.Call;
import com.bin.david.cacheokhttp.core.Callback;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by huang on 2017/11/17.
 */

public class RxConverterFactory implements IConverterFactory<Observable> {


    @Override
    public <R> Observable<R> converter(final Call<R> call) {
        return Observable.create(new ObservableOnSubscribe<R>() {
            @Override
            public void subscribe(final ObservableEmitter<R> e) throws Exception {
                call.enqueue(new Callback<R>() {
                    @Override
                    public void onResponse(okhttp3.Call call, R response) {
                        e.onNext(response);
                        e.onComplete();
                    }
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e1) {
                        e.onError(e1);
                        e.onComplete();
                    }
                });
            }
        });


    }

}
