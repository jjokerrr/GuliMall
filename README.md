## 项目简介

商城项目是一套基于微服务架构的电商系统，包括前台商城系统和后台管理系统。该项目采用Spring Cloud、Spring Cloud Alibaba、MyBatis Plus等技术实现，涵盖了用户登录注册、商品搜索、购物车、订单处理、秒杀活动、系统管理、商品管理、优惠营销、库存管理、订单管理、用户管理和内容管理等功能。

**技术栈**：

SpringBoot、Spring Cloud、SpringCloud gateway、Spring Security、MybatisPlus、Mysql、 Nacos、ElasticSearch、Redis、RabbitMq、Feign、Nginx、Sentinel、OSS、Docker

## 项目结构

```
gulimall
├── gulimall-common -- 工具类及通用代码
├── renren-generator -- 人人开源项目的代码生成器
├── gulimall-auth-server -- 认证中心（社交登录、OAuth2.0）
├── gulimall-cart -- 购物车服务
├── gulimall-coupon -- 优惠卷服务
├── gulimall-gateway -- 统一配置网关
├── gulimall-order -- 订单服务
├── gulimall-product -- 商品服务
├── gulimall-search -- 检索服务
├── gulimall-seckill -- 秒杀服务
├── gulimall-third-party -- 第三方服务（对象存储、短信）
├── gulimall-ware -- 仓储服务
└── gulimall-member -- 会员服务
```

## 系统架构

![mall系统架构](https://camo.githubusercontent.com/ccb4fa3c1dbe0f35be3c0b0ac87caad40589c875ae25d097bfd2a1c5a163e01f/68747470733a2f2f692e6c6f6c692e6e65742f323032312f30322f31382f7a4d7253576141666271596f4634742e706e67)

## 业务架构

![业务架构](https://camo.githubusercontent.com/eace78b97127938606c43fc68b02707d153d71b2c84d042dcdfbf23e8d9fae01/68747470733a2f2f692e6c6f6c692e6e65742f323032312f30322f31382f79426a6c717673436770566b454e632e706e67)

## 开发环境

| 工具          | 版本号 | 下载                                                         |
| ------------- | ------ | ------------------------------------------------------------ |
| JDK           | 1.8    | https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html |
| Mysql         | 8.0    | [https://www.mysql.com](https://www.mysql.com/)              |
| Redis         | Redis  | https://redis.io/download                                    |
| Elasticsearch | 7.12.1 | https://www.elastic.co/downloads                             |
| Kibana        | 7.12.1 | https://www.elastic.co/cn/kibana                             |
| RabbitMQ      | 3.13.0 | http://www.rabbitmq.com/download.html                        |
| Nginx         | 1.12.5 | http://nginx.org/en/download.html                            |

## 项目运行

### 导入sql文件

1. 创建数据库mall_oms(订单)、mall_pms(商品)、mall_ums(用户)、mall_sms(优惠)、mall_wms(库存)、mall_admin(M端后台)
2. 导入sql文件，按照sql文件名称在相应的数据库中执行sql文件

### 部署中间件

项目需要部署中间件和相关数据库工具支持，相关的中间件和工具部署参考响应文档

| Mysql         | 8.0    | [https://www.mysql.com](https://www.mysql.com/) |
| ------------- | ------ | ----------------------------------------------- |
| Redis         | Redis  | https://redis.io/download                       |
| Elasticsearch | 7.12.1 | https://www.elastic.co/downloads                |
| Kibana        | 7.12.1 | https://www.elastic.co/cn/kibana                |
| RabbitMQ      | 3.13.0 | http://www.rabbitmq.com/download.html           |
| Nginx         | 1.12.5 | http://nginx.org/en/download.html               |

### 修改配置文件

> 前置条件，在每一个微服务中，都存在这一个bootstrap.yml启动配置文件，该配置文件会在项目启动前加载，需要在该配置文件中配置nacos地址、项目名称和nacos配置命名空间，这个是全部微服务统一的，后文不再赘述

#### mall-auth:application.yml

![mall-auth](https://github.com/jjokerrr/GuliMall/assets/96131582/58af483e-c246-400e-9299-6a5c46a6e89f)


#### mall-cart:application.yml

![mall-cart](https://github.com/jjokerrr/GuliMall/assets/96131582/ef96d34f-39de-408e-b1b9-13e02bc540fa)


#### mall-coupon:application.yml

![mall-coupon](https://github.com/jjokerrr/GuliMall/assets/96131582/d038466e-067b-4032-be50-d5970e103426)


#### mall-gateway:application.yml

![mall-gateway](https://github.com/jjokerrr/GuliMall/assets/96131582/28aeb6f4-92b5-44fe-926f-f8f92cfd790e)


#### mall-member:application.yml

![mall-member](https://github.com/jjokerrr/GuliMall/assets/96131582/1d59f0dd-9ac5-40f6-aaf3-58be7608d2e4)


#### mall-order:application.yml

![mall-order1](https://github.com/jjokerrr/GuliMall/assets/96131582/3be6a63f-1c60-4fa4-9c88-2241b0f4ea3d)


![mall-order2](https://github.com/jjokerrr/GuliMall/assets/96131582/77c16c43-5fc0-4a3b-b286-2ba119613950)


#### mall-product:application.yml

![mall-product1](https://github.com/jjokerrr/GuliMall/assets/96131582/a2bc6a39-f6a5-4a77-9772-e082fc42c741)


![mall-product2](https://github.com/jjokerrr/GuliMall/assets/96131582/c83af13f-638b-47ca-97c2-19a779123078)


#### mall-search:application.yml

![mall-search](https://github.com/jjokerrr/GuliMall/assets/96131582/3b124210-836d-4b70-a997-22f7cd05117e)


#### mall-seckill:application.yml

![mall-seckill](https://github.com/jjokerrr/GuliMall/assets/96131582/edaa10dc-1594-4553-a3c1-6b5213667c96)


#### mall-third-party:application.yml

![mall-thirdpartypng](https://github.com/jjokerrr/GuliMall/assets/96131582/8c2b1c3a-c521-42c2-bd1c-ebd74ae7a56c)


#### mall-ware:application.yml

![mall-ware](https://github.com/jjokerrr/GuliMall/assets/96131582/baf08cdd-a9f8-44c5-8e97-3ca6d64c9737)


#### renren-fast:application-dev.yml

![image-20240623162908649](https://github.com/jjokerrr/GuliMall/assets/96131582/799049b3-ba4f-4f9e-a5b6-47e00b5b6b06)
![image-20240623162941608](https://github.com/jjokerrr/GuliMall/assets/96131582/f214e401-b282-48e9-bc13-18cd396901ce)


### 启动各微服务

建议使用idea，启动各微服务的项目地址

![mall](https://camo.githubusercontent.com/e724c56a3b0a2aceeb3784de3a4ea63c9ad3098d075c4613b2efc1f1c2f4c51d/68747470733a2f2f692e6c6f6c692e6e65742f323032312f30322f31382f4850657a7353366f59784a357952492e706e67)

