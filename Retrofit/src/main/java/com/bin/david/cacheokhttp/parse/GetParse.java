package com.bin.david.cacheokhttp.parse;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.QueryMap;
import com.squareup.javapoet.MethodSpec;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import okhttp3.Request;


/**
 * Created by huang on 2017/11/20.
 */

public class GetParse extends BaseAnnotatedParse {
    @Override
    public String parse(ExecutableElement methodElement,MethodSpec.Builder methodBuilder, Map<String,String> queryMap) {

        methodBuilder.addStatement("$T paramStr = new $T()",StringBuffer.class,StringBuffer.class);
        methodBuilder.addStatement("int paramCount = 0");
        for (Map.Entry<String,String> entry : queryMap.entrySet()) {
            methodBuilder.beginControlFlow("if(paramCount == 0)");
            methodBuilder.addStatement("paramStr.append(\"?\")");
            methodBuilder.endControlFlow();
            methodBuilder.beginControlFlow("else");
            methodBuilder.addStatement("paramStr.append(\"&\")");
            methodBuilder.endControlFlow();
            methodBuilder.addStatement("paramCount++");
            methodBuilder.addStatement("paramStr.append(\"$N=\"+$N)",entry.getKey(),entry.getValue());
        }
        List<? extends VariableElement> params = methodElement.getParameters();
        for (int i = 0; i < params.size(); i++) {
            VariableElement paramElement = params.get(i);
            String paramName = paramElement.getSimpleName().toString();
            QueryMap query = paramElement.getAnnotation(QueryMap.class);
            if(query != null){
                methodBuilder.beginControlFlow("for ($T<String,String> entry : $N.entrySet()) ",Map.Entry.class,paramName);
                methodBuilder.beginControlFlow("if(paramCount == 0)");
                methodBuilder.addStatement("paramStr.append(\"?\")");
                methodBuilder.endControlFlow();
                methodBuilder.beginControlFlow("else");
                methodBuilder.addStatement("paramStr.append(\"&\")");
                methodBuilder.endControlFlow();
                methodBuilder.addStatement("paramCount++");
                methodBuilder.addStatement("paramStr.append(entry.getKey()+\"=\"+entry.getValue())");
                methodBuilder.endControlFlow();
            }
        }
        methodBuilder.addStatement(" $T.Builder requestBuilder = new $T.Builder()",Request.class,Request.class);
        setHead(methodElement, methodBuilder);
        methodBuilder.addStatement("url= url+paramStr.toString()");
        String requestStateStr = "$T request = requestBuilder.url(getBaseUrl() + url)\n" +
                ".get().build()";
        methodBuilder.addStatement(requestStateStr,Request.class);
        return requestStateStr;
    }



    @Override
    public String getUrl(ExecutableElement methodElement) {
        Get get = methodElement.getAnnotation(Get.class);
        return get.value();
    }
}
