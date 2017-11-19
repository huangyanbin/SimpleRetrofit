package com.bin.david.cacheokhttp.parse;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Path;
import com.bin.david.cacheokhttp.annotation.Query;
import com.bin.david.cacheokhttp.core.Call;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by huang on 2017/11/16.
 */

public class AnnotatedMethod {


    private ExecutableElement methodElement;

    private String simpleClassName;


    private String simpleMethodName;


    private String qualifiedClassName;

    private TypeElement classElement;

    private Map<String,String> pathMap = new HashMap<>();
    private Map<String,String> queryMap = new HashMap<>();

    public AnnotatedMethod(ExecutableElement element){
        this.methodElement = element;
        simpleMethodName = element.getSimpleName().toString();
        classElement = (TypeElement) element.getEnclosingElement();
        simpleClassName = classElement.getSimpleName().toString();
        qualifiedClassName= classElement.getQualifiedName().toString();

    }

    public String getSimpleMethodName() {
        return simpleMethodName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public ExecutableElement getMethodElement() {
        return methodElement;
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public TypeElement getClassElement() {
        return classElement;
    }


    public MethodSpec generateMethodSpec(){
        ExecutableElement methodElement = getMethodElement();
        Get get = methodElement.getAnnotation(Get.class);
        String url = get.value();
        TypeName returnType = TypeName.get(methodElement.getReturnType());
        List<? extends VariableElement> params = methodElement.getParameters();
        String methodName = methodElement.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);
        StringBuffer methodFieldStr = new StringBuffer(" $T method = this.getClass().getMethod(\""+methodName+"\"");
        for(int i = 0;i < params.size();i++){
            VariableElement paramElement = params.get(i);
            String paramName = paramElement.getSimpleName().toString();
            TypeName fieldType = TypeName.get(paramElement.asType());
            methodBuilder.addParameter(fieldType,paramName);
            methodFieldStr.append(","+fieldType+".class");
            Path path = paramElement.getAnnotation(Path.class);
            if(path!= null){
                String value = path.value();
                pathMap.put(value,paramName);
            }
            Query query = paramElement.getAnnotation(Query.class);
            if(query != null){
                String value = query.value();
                queryMap.put(value,paramName);
            }
        }
        methodFieldStr.append(")");
        methodBuilder.addStatement("String url = $S",url);
        for (Map.Entry<String,String> entry : pathMap.entrySet()) {
            methodBuilder.addStatement("url =  url.replaceAll(\"\\\\{$N\\\\}\",$N)"
            ,entry.getKey(),entry.getValue());
        }
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
        String returnTypeName = returnType.toString();
        String generic = returnTypeName.substring(returnTypeName.indexOf("<"));
        methodBuilder.addStatement("url= url+paramStr.toString()");
        methodBuilder.addStatement("$T$N call = new $T$N(createCall(url))",Call.class,generic,Call.class,generic);
        methodBuilder.beginControlFlow("try");
        methodBuilder.addStatement(methodFieldStr.toString(), Method.class);
        methodBuilder.addStatement("setCallGenericReturnType(method,call)");
        methodBuilder.endControlFlow();
        methodBuilder.beginControlFlow("catch (Exception e)");
        methodBuilder.addStatement("e.printStackTrace()");
        methodBuilder.endControlFlow();
        methodBuilder.addStatement("$T convertCall = ($T)(getConverterFactory().converter(call))",returnType,returnType);
        methodBuilder.addStatement("return convertCall");
        MethodSpec methodSpec = methodBuilder.build();
        return methodSpec;
    }
}
