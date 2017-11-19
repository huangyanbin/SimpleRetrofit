package com.bin.david.rxcacheokttp;

import java.text.MessageFormat;

/**
 * Created by huang on 2017/11/17.
 */

public class Test {

    public static  void main(String[] arg){
        String url = "{query}/{pm}.json?city=%E4%B8%8A%E6%B5%B7&token=5j1znBVAsnSf5xQyNQyq&avg";
        url.replaceAll("\\u007Bquery}","querys");
    }
}
