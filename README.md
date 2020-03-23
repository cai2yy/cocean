CJIoc
--
通过JDK反射和注解机制实现的简单IOC框架和单例仓库
- IOC自动注入
- 接口分类注入
- 支持多线程读写，是线程安全的
- 可用来实现MVC框架或辅助开发简化代码逻辑
- 对单例和分类单例对象的缓存,可作为环境容器的基础


完整的MVC框架案例：
- armiot: https://github.com/cai2yy/armot
    >一个轻便的IOT管理和控制平台

功能
---
1. 通过注解(如@Inject)完成参数的自动注入
    - 实现MVC等框架
    
2. 创造和获取全局单例对象
    - 在构建的过程中自动完成相关的依赖注入
    - 单例和命名单例的缓存
 
3. 创造普通对象
    - 在构建的过程中自动完成相关的依赖注入

知识点
---
JDK反射和注解机制

使用了JDK8和JDK10的一些新特性
- lambda, Consumer (JDK8)
- var (JDK10)

使用要点
---
接口的分类注入：
- 需在初始化时完成全项目扫描（默认）或后续手动添加匹配

连锁注入:
- 通过getInstance()函数或连锁注入被注入的对象，其构造方法的参数也将被注入
- 构造类的参数符合注入条件的（@Inject)

两种注入对象将被缓存:
- @Singleton，1个类->1个单例
- @Named("xx")，1个类（通常为接口)->N个名称->N个单例

获取缓存对象(单例):
- `injector.getInstance(XX.class)`


JSR-330规范
--
@Singleton: 单例标记
- 出现在类的上方 -> 标记为单例，支持缓存
  
@Inject: 标记为“注入”，相当于Spring里面的AutoWired
- 出现在成员变量上方 -> 连锁注入

@Qualifier: 限定器，用于分门别类，最常用的是名称限定器
- @Named是该规范下的一种，本项目支持基于@Qualifier注解的扩展开发

@Named: 基于 String 的限定器，也就是名称限定器
- 出现在类上方 -> 标记该类的名称
- 成员变量上方或构造方法参数前 -> 连锁注入
- 通常用来指定注入对象为接口时，初始化的对应类
  
Cai2yy
---
Java, Python, Node.js, Love 

https://github.com/cai2yy

- ArmOT: 边缘计算IOT软件+数据上云web端管理平台
> https://github.com/cai2yy/armot
- CJHttp: 基于netty实现的轻便web框架（http）
> https://github.com/cai2yy/cjhttp
- CJIoc：多功能的轻量级IOC框架
> https://github.com/cai2yy/cjioc
- CJEviter: 模仿node.js中eventEmitter类的JAVA实现
> https://github.com/cai2yy/cjeviter

