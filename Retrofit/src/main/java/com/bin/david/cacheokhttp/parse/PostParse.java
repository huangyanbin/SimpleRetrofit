package com.bin.david.cacheokhttp.parse;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Post;
import com.bin.david.cacheokhttp.annotation.QueryMap;
import com.squareup.javapoet.MethodSpec;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import okhttp3.FormBody;
import okhttp3.Request;


/**
 * Created by huang on 2017/11/20.
 */

public class PostParse extends BaseAnnotatedParse {
    @Override
    public String parse(ExecutableElement methodElement,MethodSpec.Builder methodBuilder, Map<String,String> queryMap) {

        methodBuilder.addStatement("$T.Builder formBuilder = new $T.Builder()",FormBody.class,FormBody.class);
        for (Map.Entry<String,String> entry : queryMap.entrySet()) {
            methodBuilder.addStatement("formBuilder.add(\"$N\",$N)",entry.getKey(),entry.getValue());
        }
        List<? extends VariableElement> params = methodElement.getParameters();
        for (int i = 0; i < params.size(); i++) {
            VariableElement paramElement = params.get(i);
            String paramName = paramElement.getSimpleName().toString();
            QueryMap query = paramElement.getAnnotation(QueryMap.class);
            if (query != null) {
                methodBuilder.beginControlFlow("for ($T<String,String> entry : $N.entrySet())", Map.Entry.class, paramName);
                methodBuilder.addStatement("formBuilder.add(entry.getKey(),entry.getValue())");
                methodBuilder.endControlFlow();
            }
        }
        methodBuilder.addStatement(" $T.Builder requestBuilder = new $T.Builder()",Request.class,Request.class);
        setHead(methodElement, methodBuilder);
        String requestStateStr = "$T request = requestBuilder.url(getBaseUrl() + url)\n" +
                ".post(formBuilder.build()).build()";
        methodBuilder.addStatement(requestStateStr,Request.class);
        return requestStateStr;
    }



    @Override
    public String getUrl(ExecutableElement methodElement) {
        Post post = methodElement.getAnnotation(Post.class);
        return post.value();
    }
}
