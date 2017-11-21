package com.bin.david.cacheokhttp.parse;

import com.bin.david.cacheokhttp.annotation.Http;
import com.squareup.javapoet.MethodSpec;

import java.util.Map;

import javax.lang.model.element.ExecutableElement;


/**
 * Created by huang on 2017/11/20.
 */

public class HttpParse extends BaseAnnotatedParse {
    private BaseAnnotatedParse parse;

    @Override
    public String parse(ExecutableElement methodElement,MethodSpec.Builder methodBuilder, Map<String,String> queryMap) {
       return parse.parse(methodElement,methodBuilder,queryMap);
    }



    @Override
    public String getUrl(ExecutableElement methodElement) {
        Http http = methodElement.getAnnotation(Http.class);
        String method = http.method().toUpperCase();
       if("POST".equals(method)){
            parse = new PostParse();
        }else{
            parse = new GetParse();
        }
        parse = new GetParse();
        return http.url();
    }
}
