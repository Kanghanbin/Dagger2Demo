### Dagger2使用

#### 一、@inject标注构造方法提供依赖

1.添加依赖

```groovy
// Add Dagger dependencies
dependencies {
  compile 'com.google.dagger:dagger:2.x'
  annotationProcessor 'com.google.dagger:dagger-compiler:2.x'
}
```

关于Dagger2的依赖配置就不在这里占用篇幅去描述了，大家可以到它的github主页下去查看官方教程<https://github.com/google/dagger>。接下来我们还是拿前面的Car和Engine来举例。

2.Student类是依赖需求方，依赖了Major类；因此我们需要在类变量Major上添加@Inject来告诉Dagger2来为自己提供依赖。 

```java
/**
 * 创建时间：2018/8/23
 * 编写人：kanghb
 * 功能描述：学生（依赖需求方）
 */
public class Student {

    @Inject
    Major major;

    public Student() {

    }

    @Override
    public String toString() {
        return "Student{" +
                "major=" + major +
                '}';
    }
}
```

3.Major类是依赖提供方，因此我们需要在它的构造函数上添加@Inject 

```java
/**
 * 创建时间：2018/8/23
 * 编写人：kanghb
 * 功能描述：专业（依赖提供方）
 */
public class Major {
    @Inject
    public Major() {
    }
    public void test(){
        System.out.println("我是Major的方法");
    }
}
```

4.创建一个用@Component标注的接口`StudentComponent`，这个`StudentComponent`其实就是一个注入器，这里用来将Major注入到Student中。 

```java
/**
 * 创建时间：2018/8/24
 * 编写人：kanghb
 * 功能描述：
 */
@Component
public interface StudentComponent {
    void bind(Student student);
}
```

**注意：看一些文档里这里bind方法命名都是用inject的，我不知道是不是强制必须inject，所以写了bind看看测试一下，稍后揭晓结果。**

5.上述操作完成后Build下项目，让Dagger2帮我们生成相关的Java类。

![生成三个Java类](https://Kanghanbin.github.io/blog/13.png)



6.Student的构造函数中调用Dagger2生成的`DaggerStudentComponent`来实现注入。

Student类完整代码如下

```java
/**
 * 创建时间：2018/8/23
 * 编写人：kanghb
 * 功能描述：学生（需求依赖方）
 */
public class Student {

    @Inject
    Major major;

    public Student() {
        DaggerStudentComponent.builder().build().bind(this);
    }

    @Override
    public String toString() {
        return "Student{" +
                "major=" + major +
                '}';
    }

    public Major getMajor() {
        return major;
    }

}
```

7.查看运行结果如下，证明我们前面说的inject方法不是强制的但有益于提升代码的可读性） 

```java
 Student student = new Student();
 student.getMajor().test();
```

```java
08-24 01:57:58.035 2459-2459/kanghb.com.dagger2demo I/System.out: 我是Major的方法
```



#### 二、@Module +@Provide标注构造方法提供依赖

如果创建Major的构造函数是带参数的呢？比如说制造一各专业是需要教师(teacher)的。或者Major类是我们无法修改的呢(依赖第三方，无法修改构造函数)？这时候就需要@Module和@Provide上场了。

1.在Student类的成员变量Major上加上@Inject表示自己需要Dagger2为自己提供依赖；Major类的构造函数上的@Inject也需要去掉，因为现在不需要通过构造函数上的@Inject来提供依赖了。 

```java
public class Student {

    @Inject
    Major major;

    public Student() {
        DaggerStudentComponent.builder().makeMajorModule(new 								MakeMajorModule()).build().inject(this);
    }

    @Override
    public String toString() {
        return "Student{" +
                "major=" + major +
                '}';
    }

    public Major getMajor() {
        return major;
    }

}
```

2.新建个Module类来生成依赖对象。前面介绍的@Module就是用来标准这个类的，而@Provide则是用来标注具体提供依赖对象的方法（这里有个不成文的规定，被@Provide标注的方法命名我们一般以provide开头，这并不是强制的但有益于提升代码的可读性） 

```java
@Module
public class MakeMajorModule {
    public MakeMajorModule() {
    }

    @Provides
    public Major provideMajor(){
        return new Major("kanghanbin");
    }
}
```

3.对StudentComponent进行一点点修改，之前的@Component注解是不带参数的，现在我们需要加上`modules = {MakeMajorModule.class}`，用来告诉Dagger2提供依赖的是`MakeMajorModule`这个类。 

```java
@Component(modules = {MakeMajorModule.class})
public interface StudentComponent {
    void inject(Student student);
}
```

4.Student类的构造函数我们也需要修改，相比之前多了个`markCarModule(new MarkCarModule())`方法，这就相当于告诉了注入器`DaggerStudentComponent`把`MakeMajorModule`提供的依赖注入到了Student类中 

```java

public Student() {
    DaggerStudentComponent.builder().makeMajorModule(new MakeMajorModule()).build().inject(this);
}
```

**注意：其实这里加不加 `makeMajorModule(new MakeMajorModule())`方法，都能顺利执行，为什么呢，打开`DaggerStudentComponent`一探究竟，发现就算不使用 `makeMajorModule(new MakeMajorModule())`方法，在调用build方法时，也直接 new MakeMajorModule（）。**

```java
public static final class Builder {
  private MakeMajorModule makeMajorModule;

  private Builder() {}

  public StudentComponent build() {
    if (makeMajorModule == null) {
      this.makeMajorModule = new MakeMajorModule();
    }
    return new DaggerStudentComponent(this);
  }

  public Builder makeMajorModule(MakeMajorModule makeMajorModule) {
    this.makeMajorModule = Preconditions.checkNotNull(makeMajorModule);
    return this;
  }
}
```

5.执行结果同样是

```java
08-24 01:57:58.035 2459-2459/kanghb.com.dagger2demo I/System.out: 我是Major的方法
```



#### 三、@Qualifier实现一个类中有两个相同类型不同对象

Dagger2根据返回值的类型来决定为哪个被@Inject标记了的变量赋值。但是问题来了，一旦有多个一样的返回类型Dagger2就懵逼了。@`Qulifier`的存在正式为了解决这个问题，我们使用@`Qulifier`来定义自己的注解，然后通过自定义的注解去标注提供依赖的方法和依赖需求方。

1.使用`Qulifier`定义两个注解： 

```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface QualifierA {

}
```

```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface QualifierB {

}
```

2.对依赖提供方做出修改

```java
@Module
public class MakeMajorModule {
    public MakeMajorModule() {
    }
    @QualifierA
    @Provides
    public Major provideMajorA(){
        return new Major("kanghanbin");
    }
    @QualifierB
    @Provides
    public Major provideMajorB(){
        return new Major("fenglin");
    }
}
```

3.对依赖需求方做出修改

```java
public class Student {

    @QualifierA
    @Inject
    Major major;
    @QualifierB
    @Inject
    Major major2;

    public Student() {
        DaggerStudentComponent.builder().makeMajorModule(new MakeMajorModule()).build().inject(this);
    }

    @Override
    public String toString() {
        return "Student{" +
                "major=" + major +
                '}';
    }

    public Major getMajorA() {
        return major;
    }
    public Major getMajorB() {
        return major2;
    }
```

4.添加测试代码查看运行结果

```java
  Student student = new Student();
  student.getMajorA().test();
  student.getMajorB().test();
```

```java
08-24 03:32:20.678 7632-7632/kanghb.com.dagger2demo I/System.out: 我是teacher为kanghanbinMajor的方法
08-24 03:32:20.679 7632-7632/kanghb.com.dagger2demo I/System.out: 我是teacher为fenglinMajor的方法
```

#### 四、@Scope限定作用域 

1.首先我们需要通过@Scope定义一个`StudentScope`注解： 

```java
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface StudentScope {
}

```

2.接着我们需要用这个`@StudentScope`去标记依赖提供方`MakeMajorModule`。 

```java
@StudentScope
@Provides
public Major provideMajorA(){
    return new Major("kanghanbin");
}
```

3.用`@StudentScope`去标注注入器 `StudentComponent` 

```java
@StudentScope
@Component(modules = {MakeMajorModule.class})
public interface StudentComponent {
    void inject(Student student);
}
```

4.Student类改为

```java
@Inject
Major major;
@Inject
Major major2;
```

5.Major类改为

```java
public Major(String teacher) {
    this.teacher = teacher;
    System.out.println("Create a Major");
}

public void test(){
    System.out.println("我是teacher为"+ teacher + "Major的方法");
}
```

输出结果：

```
Create a Major
```

但是如果我么不加`@StudentScope`，就打印出两次

```java
Create a Major
Create a Major
```

所以，通过@Scope实现了局部的单例。 



### dagger2原理分析

#### 1.`MakeMajorModule`和`MakeMajorModule_ProvideMajorAFactory`

> ```java
> 自己写的类
> @Module
> public class MakeMajorModule {
>     public MakeMajorModule() {
>     }
> //    @QualifierA
>     @StudentScope
>     @Provides
>     public Major provideMajorA(){
>         return new Major("kanghanbin");
>     }  
> }
> 
> 
>  Dagger2生成的工厂类
> public final class MakeMajorModule_ProvideMajorAFactory implements Factory<Major> {
>   private final MakeMajorModule module;
> 
>   public MakeMajorModule_ProvideMajorAFactory(MakeMajorModule module) {
>     assert module != null;
>     this.module = module;
>   }
> 
>   @Override
>   public Major get() {
>     return Preconditions.checkNotNull(
>         module.provideMajorA(), "Cannot return null from a non-@Nullable @Provides method");
>   }
> 
>   public static Factory<Major> create(MakeMajorModule module) {
>     return new MakeMajorModule_ProvideMajorAFactory(module);
>   }
> ```

可以看到dagger2依据我们写的类给我们提供了一个工厂类，get（）方法调用了`MakeMajorModule`的`provideMajorA`()拿到了Major，create(MakeMajorModule module)通过传进来的MakeMajorModule 创建工厂类实例。

#### 2.StudentComponent 和`DaggerStudentComponent`

> ```java
> 自己写的类
> @StudentScope
> @Component(modules = {MakeMajorModule.class})
> public interface StudentComponent {
>     void inject(Student student);
> }
> 
> 
>  Dagger2生成的StudentComponent的实现类
>  public final class DaggerStudentComponent implements StudentComponent {
>   private Provider<Major> provideMajorAProvider;
> 
>   private MembersInjector<Student> studentMembersInjector;
> 
>   private DaggerStudentComponent(Builder builder) {
>     assert builder != null;
>     initialize(builder);
>   }
> 
>   public static Builder builder() {
>     return new Builder();
>   }
> 
>   public static StudentComponent create() {
>     return builder().build();
>   }
> 
>   @SuppressWarnings("unchecked")
>   private void initialize(final Builder builder) {
> 
>     this.provideMajorAProvider =
>         DoubleCheck.provider(MakeMajorModule_ProvideMajorAFactory.create(builder.makeMajorModule));
> 
>     this.studentMembersInjector = Student_MembersInjector.create(provideMajorAProvider);
>   }
> 
>   @Override
>   public void inject(Student student) {
>     studentMembersInjector.injectMembers(student);
>   }
> 
>   public static final class Builder {
>     private MakeMajorModule makeMajorModule;
> 
>     private Builder() {}
> 
>     public StudentComponent build() {
>       if (makeMajorModule == null) {
>         this.makeMajorModule = new MakeMajorModule();
>       }
>       return new DaggerStudentComponent(this);
>     }
> 
>     public Builder makeMajorModule(MakeMajorModule makeMajorModule) {
>       this.makeMajorModule = Preconditions.checkNotNull(makeMajorModule);
>       return this;
>     }
>   }
> }
> ```

`DaggerStudentComponent`就是`StudentComponent` 的实现类，通过builder()方法返回了Builder对象，然后build创建了一个`DaggerStudentComponent`对象。在构造函数中初始化了`provideMajorAProvider` 和 `studentMembersInjector`。当调用inject时候，执行 `studentMembersInjector.injectMembers(student)`。

1. #### 分析上一步提到的Student_MembersInjector

> ```java
> public final class Student_MembersInjector implements MembersInjector<Student> {
>   private final Provider<Major> majorAndMajor2Provider;
> 
>   public Student_MembersInjector(Provider<Major> majorAndMajor2Provider) {
>     assert majorAndMajor2Provider != null;
>     this.majorAndMajor2Provider = majorAndMajor2Provider;
>   }
> 
>   public static MembersInjector<Student> create(Provider<Major> majorAndMajor2Provider) {
>     return new Student_MembersInjector(majorAndMajor2Provider);
>   }
> 
>   @Override
>   public void injectMembers(Student instance) {
>     if (instance == null) {
>       throw new NullPointerException("Cannot inject members into a null reference");
>     }
>     instance.major = majorAndMajor2Provider.get();
>     instance.major2 = majorAndMajor2Provider.get();
>   }
> 
>   public static void injectMajor(Student instance, Provider<Major> majorProvider) {
>     instance.major = majorProvider.get();
>   }
> 
>   public static void injectMajor2(Student instance, Provider<Major> major2Provider) {
>     instance.major2 = major2Provider.get();
>   }
> }
> ```

create方法在`DaggerStudentComponent`中被调用用来创建一个`Student_MembersInjector`对象，`injectMembers`（）方法也是在`DaggerStudentComponent`的inject中被调用初始化，用majorAndMajor2Provider.get()来初始化Student中的两个Major对象，从而Student依赖需求方就得到了major和major2的实例。而这里的majorAndMajor2Provider.get()就是`MakeMajorModule_ProvideMajorAFactory`里面的get方法。

#### 换一种角度去思考（结合方法调用顺序再来一波分析）

首先Major构造方法是在`provideMajor`被调用的，然后看看是谁调用了`provideMajor`这个方法，发现是被`MakeMajorModule_ProvideMajorFactory`类里的get方法调用，再看看是谁调用了get方法，看到了是由`Student_MembersInjector`的`injectMembers`方法调用，而`injectMembers`正是在`DaggerStudentComponent`的inject方法中执行的，瞬间恍然大悟，从后往前看更容易理解

```java
@Provides
public Major provideMajor(){
    return new Major("kanghanbin");
}
```

```java
  @Override
  public Major get() {
    return Preconditions.checkNotNull(
        module.provideMajor(), "Cannot return null from a non-@Nullable @Provides method");
  }
```

```java
  @Override
  public void injectMembers(Student instance) {
    if (instance == null) {
      throw new NullPointerException("Cannot inject members into a null reference");
    }
      //都调用的是
    instance.major = majorAndMajor2Provider.get();
    instance.major2 = majorAndMajor2Provider.get();
  }
```

```java
  @Override
  public void inject(Student student) {
    studentMembersInjector.injectMembers(student);
  }
```



### 总结

本文只是简单的对dagger2分析了一下，没有真正在安卓项目中去运用，在开发安卓App过程中会遇到的比这更复杂，但是相信掌握了本篇所讲内容，再去结合实际开发去用它就没有那么难了。

![最后盗个图](https://upload-images.jianshu.io/upload_images/1504173-0b81f8a57768a703.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/819/format/webp)