package com.bin.david.cacheokhttp;

import com.bin.david.cacheokhttp.annotation.Http;
import com.bin.david.cacheokhttp.annotation.Post;
import com.bin.david.cacheokhttp.parse.AnnotatedMethod;
import com.bin.david.cacheokhttp.annotation.Get;
import com.bin.david.cacheokhttp.parse.AnnotatedClass;
import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by huang on 2017/11/16.
 */
@AutoService(Processor.class)
public class HttpProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;
    private HashMap<String,AnnotatedClass> classMap;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        classMap = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        //获取到所有的Get注解
        Set<? extends Element> getSet = roundEnv.getElementsAnnotatedWith(Get.class);
        //获取到所有Post注解
        Set<? extends Element> postSet = roundEnv.getElementsAnnotatedWith(Post.class);
        //放入新的Set集合里面
        Set<? extends Element> httpSet = roundEnv.getElementsAnnotatedWith(Http.class);
        HashSet<Element> allAnnotationSet = new HashSet<>();
        allAnnotationSet.addAll(getSet);
        allAnnotationSet.addAll(postSet);
        allAnnotationSet.addAll(httpSet);
        for (Element e : allAnnotationSet) {
            if (e.getKind() != ElementKind.METHOD) {
                onError("Builder annotation can only be applied to method", e);
                return false;
            }
            String packageName = elementUtils.getPackageOf(e).getQualifiedName().toString();
            ExecutableElement element = (ExecutableElement) e;
            AnnotatedMethod annotatedMethod = new AnnotatedMethod(element);
            String qualifiedClassName = annotatedMethod.getQualifiedClassName();
            AnnotatedClass annotatedClass;
            if(classMap.containsKey(qualifiedClassName)){
              annotatedClass = classMap.get(qualifiedClassName);
            }else{
                annotatedClass = new AnnotatedClass(packageName,annotatedMethod.getSimpleClassName()
                ,annotatedMethod.getClassElement());
                classMap.put(qualifiedClassName,annotatedClass);
            }
            annotatedClass.addMethod(annotatedMethod);
            onNote("retrofit build ---"+element.getSimpleName()+"--- method", e);
        }
        for (Map.Entry<String, AnnotatedClass> annotatedClassEntry : classMap.entrySet()) {
            AnnotatedClass annotatedClass = annotatedClassEntry.getValue();
            annotatedClass.generateCode(elementUtils,filer);
        }
        return true;

    }





    private void onError(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }


    private void onNote(String message, Element element) {
        //messager.printMessage(Diagnostic.Kind.OTHER, message, element);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Get.class.getCanonicalName());
        set.add(Post.class.getCanonicalName());
        set.add(Http.class.getCanonicalName());
        return set;
    }
}
