package com.bin.david.cacheokhttp.parse;

import com.bin.david.cacheokhttp.annotation.Head;
import com.squareup.javapoet.MethodSpec;

import java.util.Map;

import javax.lang.model.element.ExecutableElement;

import okhttp3.Headers;

/**
 * Created by huang on 2017/11/20.
 */

public abstract class BaseAnnotatedParse {

    abstract String parse(ExecutableElement methodElement,MethodSpec.Builder builder, Map<String,String> queryMap);

    abstract String getUrl(ExecutableElement methodElement);

    public void setHead(ExecutableElement methodElement, MethodSpec.Builder methodBuilder) {
        if(methodElement.getAnnotation(Head.class) != null){
            Head header =  methodElement.getAnnotation(Head.class);
            String[] headerStr = header.value();
            methodBuilder.addStatement("$T.Builder headBuilder = new $T.Builder()", Headers.class,Headers.class);
            for (String headStr : headerStr) {
                methodBuilder.addStatement("headBuilder.add(\"$N\")", headStr);
            }
            methodBuilder.addStatement("requestBuilder.headers(headBuilder.build())");
        }
    }
}
