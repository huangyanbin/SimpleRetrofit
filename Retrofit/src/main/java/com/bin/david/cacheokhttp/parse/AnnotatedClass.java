package com.bin.david.cacheokhttp.parse;

import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.annotation.Path;
import com.bin.david.cacheokhttp.core.APIService;
import com.bin.david.cacheokhttp.core.Call;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;




/**
 * Created by huang on 2017/11/16.
 */

public class AnnotatedClass {

    private String className;

    private String packageName;

    private List<AnnotatedMethod> methods = new LinkedList<>();

    private TypeElement classElement;



    public AnnotatedClass(String packageName, String generateClassName,TypeElement classElement) {
        this.className = generateClassName;
        this.packageName = packageName;
        this.classElement = classElement;
    }



    public void addMethod(AnnotatedMethod annotatedMethod) {
        methods.add(annotatedMethod);
    }


    public void generateCode(Elements elementUtils, Filer filer) {
        TypeName classType = TypeName.get(classElement.asType());
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className+"Imp")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(classType)
                    .superclass(APIService.class);
        for (int i = 0;i < methods.size();i++) {
            AnnotatedMethod m = methods.get(i);
            MethodSpec methodSpec = m.generateMethodSpec();
            if(methodSpec !=null) {
                typeBuilder.addMethod(methodSpec);
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();
        try {
            javaFile.writeTo(filer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
