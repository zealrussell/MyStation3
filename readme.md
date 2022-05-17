# 一、项目简介

&emsp;&emsp;本项目旨在采用MAVSDK-Java开发一个“无人机Android地面站”软件，该软件能操作所有支持mavlink协议的无人机、无人车设备，以往的无人机地面站软件开发大致分两种：1. 基于QT修改QGroundControl软件 2.自行通过mavlink消息集生成java代码。 而本项目可直接使用Android Studio、SDK开发，更符合现代Android应用开发。功能有：数据显示、悬停、前进固定距离、后退、左移、右移、上升、下降、图传。

本项目结构如下：

```
com.zeal.mystation3
│  MainActivity.java
│
├─application
│      MyApplication.java
│
├─entity
│      DeviceInfo.java
│      MyPosition.java
│
├─utils
│      AlertCustomDialog.java
│      CrashHandler.java
│      FileUtils.java
│      LogUtil.java
│
└─view
        AboutActivity.java
        MapActivity.java
        SplashActivity.java
        USBCameraActivity.java
```

# 二、使用方法

&emsp;&emsp;本地面站APP需结合硬件使用，待操作的四旋翼无人机需要使用支持MAVLink协议的飞控、WiFi数传；图传功能为一单独模块，使用UVC摄像头，发送端放置在无人机上，接收端通过OTG连接到手机端。

&emsp;&emsp;控制软件时需要先点“连接”，然后点“起飞”，即可移动。



# 三、环境配置

## 1.百度地图SDK集成

参照[Android地图SDK | 百度地图API SDK (baidu.com)](https://lbsyun.baidu.com/index.php?title=androidsdk)

## 2.MAVSDK集成

项目地址[Introduction · MAVSDK Guide (mavlink.io)](https://mavsdk.mavlink.io/main/en/index.html)

建议先参考快速入门体验其用法，再根据java版体会RxJava的用户，最后上手Android。

### 2.1 MAVSDK官方文档介绍

### 2.2 代码使用

#### 1. 安卓导入gradle

```java
//mavsdk
implementation 'io.mavsdk:mavsdk:1.1.1'
//https://mvnrepository.com/artifact/io.mavsdk/mavsdk-server
implementation 'io.mavsdk:mavsdk-server:1.1.1'
```

#### 2. docker镜像使用模拟器

``` shell
 docker run --name mygazebo --rm -it jonasvautherin/px4-gazebo-headless:1.12.3 <ip.your.phone>
```

注：mavsdk-java采用C/S架构，其server端集成在安卓中，通过`mavserver.run("ADDRESS")`方法运行，而px4仿真飞控监听指定IP的14540端口，因此必须主要安卓端IP正确，否则server无法连接。该IP在AVD上有问题，建议在真机上调试。

#### 3. 代码

参照本项目`MapActivity.java`，核心思想为RxJava。以起飞功能为例介绍下其用法。

简单版：

```java
drone.getAction().takeoff().subscribe();
```

升级版：

```java
drone.getAction()
    .takeoff()
    .doOnError(throwable -> {
        Log.d("error:");
    })
    .doOnComplete(()->{
        Log.d("success");
    })
    .subscribe(() -> {
       	Log.d("subscribe");
    });
```

## 3. 图传集成

项目来源：[Android直播开发之旅(10)：AndroidUSBCamera，UVCCamera开发通用库_无名之辈FTER的博客-CSDN博客_uvccamera](https://blog.csdn.net/andrexpert/article/details/78324181)

1. 在github上下载该项目，选中`libusbcamera`模块编译，在该模块build->output->aar下找到编译好的`libusbcamera-release.aar`包[^1]
2. 将该aar包以及libs目录下的`libusbcommon_v4.1.1.aar`包复制到自己项目的libs目录下，在moudle-build.gradle下加上`implementation(fileTree("libs"))`即可使用
3. 参照`USBCameraAvtivity`的代码调用API，即可使用[^2]
4. 更多用法参照根项目[saki4510t/UVCCamera](https://github.com/saki4510t/UVCCamera)



# 四、硬件设备清单



|      |                                                              |
| ---- | ------------------------------------------------------------ |
| 数传 | [PW-Link数传 · PW-Link 文档 (cuav.net)](https://doc.cuav.net/data-transmission/pw-link/zh-hans/) |
| 飞控 | [PixHack v3X全功能飞控 · pixhack (cuav.net)](https://doc.cuav.net/flight-controller/pixhack/zh-hans/pixhack-v3.html) |
| 图传 | [模拟图传 · plane (cuav.net)](https://doc.cuav.net/tutorial/plane/optional-hardware/image-transmission.html) |





---



[^1]:要想使用原项目代码，注意检查manifest文件配置
[^2]: 网上很多代码中的该库都有问题，在安卓9中黑屏，因此最好自己打包



