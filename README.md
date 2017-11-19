## Retrofit简易版本

---
> **为啥要写这个？**

> 之前一直使用OKHttp，之前修改过鸿洋的OKhttpUtils增加了缓存功能。但对Retrofit并没有使用过，前几天按网上例子用了，感觉确实简约多了。总觉得Retrofit就是个注解版OKHttp，应该写个简易版本很容易，就是个编译时注解呗。于是没看源码写个简单版本。现在已经可以集合Rxjava。我试图去想Retrofit作者是咋写的。肯定有人说又造重复的轮子，放心，写完我也不用，因为真的只是demo，只是为了增加自己编程的能力。

> **咋开始呢？**

> 我想着边写边改，于是我首先建了个module写了Get注解类，用于等下解析用。

```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Get {
    String value();
}
```
> 真是很简单吧，然后就是建了个类HttpProcessor继承AbstractProcessor类，结果发现死活导不了AbstractProcessor类，坑爹啊，只好百度了，原来module必须用java library。只有删了重新建。

> 接着就是写HttpProcessor了，肯定有人问AbstractProcessor类干嘛的，建议百度。因为我也是百度的，哈哈。查完就知道主要就是 public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv)这个方法来干活的。就是编译的时候调用该方法，我们可以通过这个方法来自动生成代码。

> 问题又来了，咋生成代码。squareup 这个javapoet框架可以优雅生成代码。百度查下就应该会用了，比较简单。

```
 compile 'com.squareup:javapoet:1.9.0'
```

> Build project还是不会调用HttpProcessor类呢，原来还需要我们告诉它在哪，这个时候google的auto-service上场了，不需要写啥Xml什么的，只需要

```
compile 'com.google.auto.service:auto-service:1.0-rc3'

```
> 在Http 类上增加注解

```
@AutoService(Processor.class)
```
> 还有咋debug编译Build，方便我们看我们到底生成什么鬼东西。在gradle.properties添加两行代码

```
org.gradle.daemon=true
org.gradle.jvmargs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

```
> 添加一个Remote调试，然后在终端输入gradlew clean assembleDebug.然后可以就是快乐的debug了，如果你还是不会，去网上看下资料就会了。



> **第一步**

> 使用Retrofit我们一般都是新建接口，然后写个抽象方法，类似下面的。

```
 @Get("{query}/pm10.json")
    Call<List<PM25>> getWeather(@Path("query") String query, @Query("city")String city,@Query("token")String token);
```

或者这样

```
 @Get("{query}/pm10.json")
    Observable<List<PM25>> getWeather(@Path("query") String query, @Query("city") String city, @Query("token") String token);
```


> 我第一反应，应该用HttpProcessor拦截到Get注解，然后再生成一个类，实现新建的接口，然后我就写了两个类：

```
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
            typeBuilder.addMethod(methodSpec);
        }

        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();
        try {
            javaFile.writeTo(filer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
> AnnotatedClass是用于生成一个Class,实现有Get注解的接口。AnnotatedMethod是用于生成每个类的方法。


```

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
        //获取方法返回的泛型类型
        StringBuffer methodFieldStr = new StringBuffer(" $T method = this.getClass().getMethod(\""+methodName+"\"");
        for(int i = 0;i < params.size();i++){
            VariableElement paramElement = params.get(i);
            String paramName = paramElement.getSimpleName().toString();
            TypeName fieldType = TypeName.get(paramElement.asType());
            //拼接方法参数
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
            //替换参数
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
```
# 未完待续

> **总结**

>   两天时间用了这个，感觉这个最难的就是泛型，因为泛型会编译之后会被擦除，最后投机取巧了，用方法获取泛型，然后将泛型Type传给Callback。还有如何支持Retrofit转换成想要的类型。和Rxjava，Gson等解耦出来，幸运的是我已经解耦了Rxjava。Gson也马上可以了。当然真正的Retrofit比我写的复杂多了。后续有时间把Cache功能加上。
