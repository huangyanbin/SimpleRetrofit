## Retrofit简易版本

---
#### **为啥要写这个？**

> 之前一直使用```OKHttp```，之前修改过鸿洋的```OKhttpUtils```增加了缓存功能。但对```Retrofit```并没有使用过，前几天按网上例子用了，感觉确实简约多了。总觉得```Retrofit```就是个注解版```OKHttp```，应该写个简易版本很容易，就是个编译时注解呗。于是没看源码写个简单版本。现在已经可以集合```Rxjava```,```Gson```。我试图去想```Retrofit```作者是咋写的。肯定有人说又造重复的轮子，放心，写完我也不用，因为真的只是demo，只是为了增加自己编程的能力。

####  **咋开始呢？**

> 我想着边写边改，于是我首先建了个```module```写了```Get```注解类，用于等下解析用。

```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Get {
    String value();
}
```
> 真是很简单吧，然后就是建了个类```HttpProcessor```继承```AbstractProcessor```类，结果发现死活导不了```AbstractProcessor```类，坑爹啊，只好百度了，原来```module```必须用```java library```。只有删了重新建。

> 接着就是写```HttpProcessor```了，肯定有人问```AbstractProcessor```类干嘛的，建议百度。因为我也是百度的，哈哈。查完就知道主要就是 ```public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv)```这个方法来干活的。就是编译的时候调用该方法，我们可以通过这个方法来自动生成代码。

> 问题又来了，咋生成代码。squareup 这个javapoet框架可以优雅生成代码。百度查下就应该会用了，比较简单。

```
 compile 'com.squareup:javapoet:1.9.0'
```

> ```Build project```还是不会调用```HttpProcessor```类呢，原来还需要我们告诉它在哪，这个时候```google```的```auto-service```上场了，不需要写啥Xml什么的，只需要

```
compile 'com.google.auto.service:auto-service:1.0-rc3'

``` 
> 在```HttpProcessor```类上增加注解

```
@AutoService(Processor.class)
```
> 还有咋```debug```编译```Build```，方便我们看我们到底生成什么鬼东西。在```gradle.properties```添加两行代码

```
org.gradle.daemon=true
org.gradle.jvmargs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

```
> 添加一个```Remote```调试，然后在终端输入```gradlew clean assembleDebug```.然后可以快乐的```debug```了，如果你还是不会，去网上看下资料就会了。



####  **第一步**

> 使用```Retrofit```我们一般都是新建接口，然后写个抽象方法，类似下面的。

```
 @Get("{query}/pm10.json")
    Call<List<PM25>> getWeather(@Path("query") String query, @Query("city")String city,@Query("token")String token);
```

> 或者这样

```
 @Get("{query}/pm10.json")
    Observable<List<PM25>> getWeather(@Path("query") String query, @Query("city") String city, @Query("token") String token);
```


> 我第一反应，应该用```HttpProcessor```拦截到```Get```，```Post```注解，然后再生成一个类，实现新建的```Http```请求接口，万事开头难，我们先在获取```Get``` 、``` Post```注解：



```
 @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        //获取到所有的Get注解
        Set<? extends Element> getSet = roundEnv.getElementsAnnotatedWith(Get.class);
        //获取到所有Post注解
        Set<? extends Element> postSet = roundEnv.getElementsAnnotatedWith(Post.class);
        //放入新的Set集合里面
        HashSet<Element> allSet = new HashSet<>();
        allSet.addAll(getSet);
        allSet.addAll(postSet);
        ...
```
> 获取到了```Get```和```Post```注解，然后就是获取注解类的包名了：

```
    //迭代
    for (Element e : allSet) {
            //判断注解在方法上面
            if (e.getKind() != ElementKind.METHOD) {
                onError("Builder annotation can only be applied to method", e);
                return false;
            }
            //获取包名
            String packageName = elementUtils.getPackageOf(e).getQualifiedName().toString();
            ...
```
> 然后我们要依次解析我们的方法。我们先建一个类```AnnotatedClass```用于放注解接口相关信息以及生成类代码，然后在建```AnnotatedMethod```类放方法相关信息以及生成方法代码。感觉很复杂？一步步来：

> 首先我们获取包名,每个方法对应一个```AnnotatedMethod```类：


```
//将element转成方法Element
   ExecutableElement element = (ExecutableElement) e;
   //创建一个方法生成类
   AnnotatedMethod annotatedMethod = new AnnotatedMethod(element);
   //获取类名（包含包名的），以便生成AnnotatedClass类
   String qualifiedClassName = annotatedMethod.getQualifiedClassName();
```
> 将类名和```AnnotatedClass```做为```key-value```放在```map```,中，保证不会重复生成类代码：

```
    AnnotatedClass annotatedClass;
    //判断是否已经有这个AnnotatedClass类了
    if(classMap.containsKey(qualifiedClassName)){
        annotatedClass = classMap.get(qualifiedClassName);
    }else{
         //生成AnnotatedClass类
         annotatedClass = new AnnotatedClass(packageName,annotatedMethod.getSimpleClassName()
        ,annotatedMethod.getClassElement());
         classMap.put(qualifiedClassName,annotatedClass);
    }
    //将方法加入annotatedClass类
     annotatedClass.addMethod(annotatedMethod);
     onNote("retrofit build ---"+element.getSimpleName()+"--- method", e);
     
```

> 迭代出来调用生成```AnnotatedClass```代码：

```
//迭代调用annotatedClass方法生成类代码
 for (Map.Entry<String, AnnotatedClass> annotatedClassEntry : classMap.entrySet()) {
            AnnotatedClass annotatedClass = annotatedClassEntry.getValue();
            annotatedClass.generateCode(elementUtils,filer);
        }
```

####  **如何生成代码（核心）**

> TypeSpec就是用于生成类信息的，采用Build方式来完成。

```
 public void generateCode(Elements elementUtils, Filer filer) {
        //获取接口名
        TypeName classType = TypeName.get(classElement.asType());
        
        TypeSpec.Builder typeBuilder =
        //类名 接口名+imp imp随便写的。
        TypeSpec.classBuilder(className+"Imp")
        //类访问权限
                    .addModifiers(Modifier.PUBLIC)
        //接口 实现我们包含Get注解的接口            
                    .addSuperinterface(classType)
                    //继承APIService类 ，这个类主要是辅助完成很多工作，等下会介绍
                    .superclass(APIService.class);
        //迭代生成方法代码            
        for (int i = 0;i < methods.size();i++) {
            AnnotatedMethod m = methods.get(i);
            MethodSpec methodSpec = m.generateMethodSpec();
            if(methodSpec !=null) {
                typeBuilder.addMethod(methodSpec);
            }
        }
        //创建一个java File
        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();
        try {
            //写java文件
            javaFile.writeTo(filer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```

> 方法生成代码复杂很多，每行都注释。```MethodSpec```就是方法生成的类，也是通过```build```方式在构造的。思路就是拼接一个方法，在里面获取出用于请求```Call```,通过```IConverterFactory```转换成我们需要返回的类型，通过```ICallAdapterFactory```将请求回掉类型转换成我们需要的类型，我没有将所有代码都通过```javapoet```生成，而是通过继承```APIService```类，因为```javapoet```写起来确实比写代码累多了。哈哈！


```
public MethodSpec generateMethodSpec() {
        ExecutableElement methodElement = getMethodElement();
        //获取一个BaseAnnotatedParse 用于Get和Post不同解析
        BaseAnnotatedParse parse = getParser();
        if (parse == null) {
            return null;
        }
        //获取注解的url
        String url = parse.getUrl(methodElement);
        //获取方法返回类型
        TypeName returnType = TypeName.get(methodElement.getReturnType());
        //获取所有方法形参
        List<? extends VariableElement> params = methodElement.getParameters();
        //获取方法名
        String methodName = methodElement.getSimpleName().toString();
        //构造一个方法
        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);
        //通过拼接可以得到对应method类，用于请求后由于泛型擦除导致无法得到Type      
        StringBuffer methodFieldStr = new StringBuffer(" $T method = this.getClass().getMethod(\"" + methodName + "\"");
        //迭代参数
        for (int i = 0; i < params.size(); i++) {
            //获取参数Element
            VariableElement paramElement = params.get(i);
            //参数名称
            String paramName = paramElement.getSimpleName().toString();
            //参数类型 包含泛型
            TypeName paramsTypeName = TypeName.get(paramElement.asType());
            //添加参数
            methodBuilder.addParameter(paramsTypeName, paramName);
            //去除泛型
            String paramsTypeStr =  paramsTypeName.toString();
            if(paramsTypeStr.contains("<")){
                paramsTypeStr = paramsTypeStr.substring(0,paramsTypeStr.indexOf("<"));
            }
            methodFieldStr.append("," + paramsTypeStr + ".class");
            //判断形参是否包含Path注解，放入pathMap中
            Path path = paramElement.getAnnotation(Path.class);
            if (path != null) {
                String value = path.value();
                pathMap.put(value, paramName);
            }
             //判断形参是否包含Query注解，放入queryMap中
            Query query = paramElement.getAnnotation(Query.class);
            if (query != null) {
                String value = query.value();
                queryMap.put(value, paramName);
            }
        }
        methodFieldStr.append(")");
        methodBuilder.addStatement("String url = $S", url);
        //替换所有的Path
        for (Map.Entry<String, String> entry : pathMap.entrySet()) {
            methodBuilder.addStatement("url =  url.replaceAll(\"\\\\{$N\\\\}\",$N)"
                    , entry.getKey(), entry.getValue());
        }
      
        String returnTypeName = returnType.toString();
          //获取返回类型的泛型
        String generic = returnTypeName.substring(returnTypeName.indexOf("<"));
        //解析head 和 query
        parse.parse(methodElement, methodBuilder, queryMap);
        //创建Call
        methodBuilder.addStatement("$T$N call = new $T$N(createCall(request))", Call.class, generic, Call.class, generic);
        //设置CallAdapterFactory
        methodBuilder.addStatement("call.setCallAdapterFactory(getCallAdapterFactory())");
        methodBuilder.beginControlFlow("try");
        methodBuilder.addStatement(methodFieldStr.toString(), Method.class);
        //设置返回类型的泛型
        methodBuilder.addStatement("setCallGenericReturnType(method,call)");
        methodBuilder.endControlFlow();
        methodBuilder.beginControlFlow("catch (Exception e)");
        methodBuilder.addStatement("e.printStackTrace()");
        methodBuilder.endControlFlow();
        //最后通过ConverterFactory()转换成返回类型
        methodBuilder.addStatement("$T convertCall = ($T)(getConverterFactory().converter(call))", returnType, returnType);
        methodBuilder.addStatement("return convertCall");
        return methodBuilder.build();

    }
```
> 我们在```APIService```里面获取返回类型的泛型，最后传给```Call```,```Call```在```enqueue(Callback calback)```传给```callback```,这样```callback```就知道它该解析成什么类型了。。。
```
 protected void setCallGenericReturnType(Method method,Call<?> call){
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType) {
            Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
            call.setGenericType(genericType);
        }
    }
```


> 设置```head``` 

```
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
```

> 还有就是如何```Converter```和```CallAdapter```，两个其实逻辑是一样的。只不过```CallAdapterFatory```需要方法返回类型的泛型，上面已经得到了。啦啦啦


```
//转换接口
public interface IConverterFactory<T> {

    <R> T converter(Call<R> call);

}

//CallAdapter 接口
public interface ICallAdapterFactory {

    <T> T converter(Response response, Type returnType);

}
```

> ```Rxjava``` 和```Okttp```结合在一起：

```
 @Override
    public <R> Observable<R> converter(final Call<R> call) {
        return Observable.create(new ObservableOnSubscribe<R>() {
            @Override
            public void subscribe(final ObservableEmitter<R> e) throws Exception {
                call.enqueue(new Callback<R>() {
                    @Override
                    public void onResponse(okhttp3.Call call, R response) {
                        e.onNext(response);
                        e.onComplete();
                    }
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e1) {
                        e.onError(e1);
                        e.onComplete();
                    }
                });
            }
        });

    }
```



> 最后通过```Retrofit``` ```create```来获取实现类的对象，虽然```Class```是一个接口，但是实际上获取的是```clazz.getName()+Imp```类，APIService这个类主要是用于设置```Retrofit```的配置，比如```baseUrl```等.

```
 public  <T> T create(Class<T> clazz) {
        String impClazz = clazz.getName()+"Imp"
        try {
            Class childClazz = Class.forName(impClazz);
            T t = (T) childClazz.newInstance();
            APIService apiService = (APIService)t;
            apiService.setOkHttpClient(builder.client);
            apiService.setConverterFactory(builder.converterFactory);
            apiService.setBaseUrl(builder.baseUrl);
            apiService.setCallAdapterFactory(builder.callAdapterFactory);
            return t;
        }catch (ClassNotFoundException e){
            throw new RetrofitException("ClassNotFoundException "+impClazz);
        } catch (IllegalAccessException e) {
            throw new RetrofitException("IllegalAccessException "+impClazz);
        } catch (InstantiationException e) {
            throw new RetrofitException("InstantiationException "+impClazz);
        }
    }
```


####  未完待续

####  **总结**
  
>   用了两天时间写这个思路实现，感觉这个最难的就是泛型，因为泛型会编译之后会被擦除，最后投机取巧了，用方法获取泛型，然后将泛型```Type```传给```Callback```。完成了```Get```, ```Post```,```Path``` ```Query```,```QuertMap```,```Head```注解,其他```Put```和```Delete```等请求就不写了。取一反三而已，还可以自定义```IConverterFactory```和```ICallAdapterFactory```.当然真正的```Retrofit```比我写的复杂多了。后续有时间把多种缓存```http cache```功能加上。
>