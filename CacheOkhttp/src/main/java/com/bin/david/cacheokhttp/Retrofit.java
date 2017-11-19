package com.bin.david.cacheokhttp;

import com.bin.david.cacheokhttp.core.APIService;
import com.bin.david.cacheokhttp.adapter.ICallAdapterFactory;
import com.bin.david.cacheokhttp.converter.IConverterFactory;
import com.bin.david.cacheokhttp.exception.RetrofitException;

import okhttp3.OkHttpClient;

/**
 * Created by huang on 2017/11/16.
 */

public class Retrofit {
    private Builder builder;

   private Retrofit(Builder builder){
       this.builder = builder;
   }

    public  <T> T create(Class<T> clazz) {
        String impClazz = clazz.getName()+"Imp";
        try {
            Class childClazz = Class.forName(impClazz);
            T t = (T) childClazz.newInstance();
            APIService apiService = (APIService)t;
            apiService.setOkHttpClient(builder.client);
            apiService.setConverterFactory(builder.converterFactory);
            apiService.setBaseUrl(builder.baseUrl);
            return t;
        }catch (ClassNotFoundException e){
            throw new RetrofitException("ClassNotFoundException "+impClazz);
        } catch (IllegalAccessException e) {
            throw new RetrofitException("IllegalAccessException "+impClazz);
        } catch (InstantiationException e) {
            throw new RetrofitException("InstantiationException "+impClazz);
        }
    }

    public static class Builder{

        private OkHttpClient client;
        private String baseUrl;
        private ICallAdapterFactory callAdapterFactory;
        private IConverterFactory converterFactory;

        public Builder setClient(OkHttpClient client) {
            this.client = client;
            return this;
        }


        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setCallAdapterFactory(ICallAdapterFactory callAdapterFactory) {
            this.callAdapterFactory = callAdapterFactory;
            return this;

        }


        public Builder setConverterFactory(IConverterFactory converterFactory) {
            this.converterFactory = converterFactory;
            return this;
        }

        public Retrofit build(){
            return new Retrofit(this);
        }
    }
}
