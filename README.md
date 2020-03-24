COcean
--
一个支持热加载的多程序容器
> 现在将程序容器化部署已成为主流，本程序基于java的类加载器和反射机制，实现了多模块（程序）分隔部署。
> 各个程序的运行框架基于IOC+AOP机制。容器支持热加载功能，开启后，当某个模块下的文件被修改，该程序的容器会自动热重启，利于调试和维护

一个程序作为一个模块部署到单独的容器中
- 支持多模块同时运行
- 模块间分隔，各子容器使用和依赖的类相互独立
    
模块的运行基于IOC、AOP机制
- IOC
> - 支持按注解自动注入
> - 支持链式注入，支持从构造器和成员变量中链式注入
> - 有效解决循环依赖

2. AOP
    
- 整个容器和子容器的运行都实现了SPI机制
- 支持热加载功能，各程序热加载互不干扰
- 支持http服务器

> 参考了tomcat和alibaba的jarslink

### 热加载 
通过
  
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

