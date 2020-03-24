COcean
--
一个支持热加载的多功能程序容器
> 现在将程序容器化部署已成为主流，本程序基于java的类加载器机制以及IOC + AOP框架，实现了多模块（程序）分隔部署。
> 
> 容器支持热加载功能，开启后，当某个模块下的文件被修改，该程序会自动热重启，利于调试和维护。
> 
> 此外，还支持跨模块的事件总线等功能，并提供了一个内置的http server模块用于快速部署web项目

#### 功能：
- 一个程序作为一个模块部署到单独的容器中
    > 模块是单独的程序，和容器本身几乎完全解耦，通过SPI进行关联
- 支持多模块同时运行
- 模块间分隔，各子容器使用和依赖的类相互独立
- 支持热加载，当模块的文件发生变化时，对应的子容器会重载
    > 基于类加载器的重载，故只能做到单个模块整体重启，无法部分重启
    >
    > 热加载是模块独立的，一个模块的重启不会影响到其他模块的运行
- 提供一个内置的 HTTP Web 服务器模块
    > 基于netty实现的轻便web框架（http）https://github.com/cai2yy/cjhttp

- 支持外部容器和子容器的通信，提供了一条事件总线
    > 子容器只需要引入一个依赖类，就可以解耦地通过这条事件总线来激发事件，使外部容器做出响应。
    
#### 核心机制
1. 容器化
    
    通过自定义类加载器实现容器化，将每个程序作为一个模块封装到一个单独的类加载器中，
    使容器之间相互隔绝，类、依赖等互不影响。并由此实现了模块热加载功能。

2. IOC
    - 支持按注解自动注入
    - 有效解决循环依赖问题
    - 支持分类注入，实现单接口-多实现类的注入
    - 支持链式注入，可对构造器入参和成员变量进行链式注入

3. AOP
    - ...

> 参考了tomcat和alibaba-jarslink的设计理念
>
> 使用反射绕开类型转换，实现跨类加载器通信

  
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

