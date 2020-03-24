# mPaaS Sync Demo

## 介绍

本代码用于指导如何集成mPaaS数据同步组件，详细文档请参考[官网](https://help.aliyun.com/document_detail/50970.html?spm=a2c4g.11186623.6.757.1bf448b8EQmVOi)。


## 编译

本代码出于发布便捷性考虑，将inside和aar集成方式合并在一个工程中，您可通过修改配置来编译不同的目标类型。

### Inside编译
1. gradle.properties里mPaasBuildType设置为inside
2. gradle-wrapper.properties里distributionUrl=https\://services.gradle.org/distributions/gradle-4.4-all.zip

### AAR编译
1. gradle.properties里mPaasBuildType设置为aar
2. gradle-wrapper.properties里distributionUrl=https\://services.gradle.org/distributions/gradle-5.5-all.zip
