# 一、项目简介

&emsp;&emsp;本项目旨在采用MAVSDK-Java开发一个“无人机Android地面站”软件，该软件能操作所有支持mavlink协议的无人机、无人车设备，以往的无人机地面站软件开发大致分两种：`1. 基于QT修改QGroundControl软件` `2.自行通过mavlink消息集生成java代码`。 而本项目可直接使用Android Studio、SDK开发，更符合现代Android应用开发功能有：数据显示、悬停、前进固定距离、后退、左移、右移、上升、下降、图传。
地址：[MyStation](https://github.com/zealrussell/MyStation3)。
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

​	本地面站APP需结合硬件使用，待操作的四旋翼无人机需要使用支持MAVLink协议的飞控、WiFi数传；图传功能为一单独模块，使用UVC摄像头，发送端放置在无人机上，接收端通过OTG连接到手机端。注意连接无人机功能不支持x86，所以最好用真机。

​	仿真环境下使用：

1. 使用前请到[app->libs]([MyStation3/app/libs at master · zealrussell/MyStation3 (github.com)](https://github.com/zealrussell/MyStation3/tree/master/app/libs))下载aar包到自己的libs目录下，打包编译apk

2. 参照[docker镜像使用](#2. docker镜像使用模拟器)

3. 控制软件时需要先点“连接”，然后点“起飞”，即可移动。

   本项目与QGC类似，基于MAVSDK能实现QGC所有功能，可以先使用QGC体会无人机地面站的用法

<img src="https://zeal-picture.oss-cn-chengdu.aliyuncs.com/img/Screenshot_20220505_224103_com.zeal.mavsdk.jpg" alt="app" style="zoom: 25%;" /><img src="https://zeal-picture.oss-cn-chengdu.aliyuncs.com/img/Screenshot_20220509_142519_com.zeal.mystation3.jpg" alt="图传模块" style="zoom:25%;" />





# 三、环境配置

## 1.百度地图SDK集成

参照[Android地图SDK | 百度地图API SDK (baidu.com)](https://lbsyun.baidu.com/index.php?title=androidsdk)

## 2.MAVSDK集成

项目地址[Introduction · MAVSDK Guide (mavlink.io)](https://mavsdk.mavlink.io/main/en/index.html)

建议先参考快速入门体验其用法，再根据java版体会RxJava的用法，最后上手Android。

### 2.1 MAVSDK官方文档介绍

该SDK提供了各种接口，方便开发者实现地面站功能：

| 类          | 介绍                                 |
| ----------- | ------------------------------------ |
| Action      | 执行飞行动作，包括起飞、降落、移动等 |
| Calibration | 校准                                 |
| Camera      | 操作摄像机                           |
| Failure     | sdk自定义的错误类                    |
| FollowMe    | 跟随                                 |
| Ftp         | 通过ftp升级固件                      |
| Geofence    | 设置禁飞区等                         |
| Gimbal      | 操作云台                             |
| Info        | 获取机架信息                         |
| LogFiles    | 从飞控中获取飞行日志                 |
| Mission     | 设置飞行任务                         |
| Offboard    |                                      |
| Shell       | 通过shell发送指令                    |
| Telemetry   | 获取无人机的遥感信息，如经、纬、高度 |

本项目主要用到了Action类、Telemetry类。Action类比较简单，直接使用即可，这里介绍下Telemetry

| 函数                 | 介绍                                              |
| -------------------- | ------------------------------------------------- |
| getPosition()        | 获取无人机当前位置:经度、纬度、绝对高度、相对高度 |
| getRawGps()          | 获取gps信息：经度、纬度、绝对高度等各种           |
| getGroundTruth()     | 获取gps信息                                       |
| getGpsGlobalOrigin() | 还是gps                                           |
| getGpsInfo()         | 获取gps自身的信息，包括gps个数等                  |
| getArmed()           | 是否arm                                           |
| getHealthAllOk()     | 是否所有设备一切正常                              |
| getInAir()           | 是否在空中                                        |
| getBattery()         | 获取电池信息：剩余电量、电压                      |
| getHeading()         | 获取头朝向                                        |
| getAttitudeEuler()   | 获取xyz轴信息：roll pitch yaw                     |
| getFlightMode()      | 获取飞行状态，结果为一个枚举类                    |
| getLandedState()     | 获取降落状态                                      |
|                      |                                                   |

其实参考QGC很好理解：

![QGC界面](https://zeal-picture.oss-cn-chengdu.aliyuncs.com/img/api.png)

### 2.2 代码使用

#### 1. 安卓导入gradle

采用C/S模式，因此要导两个

```java
//mavsdk
implementation 'io.mavsdk:mavsdk:1.1.1'
implementation 'io.mavsdk:mavsdk-server:1.1.1'
```

#### 2. docker镜像使用模拟器

采用px4飞控的docker镜像可以节省开发时间，该镜像提供了`UDP://14550`端口监听QGC，`UDP://14540`端口监听我们的mavsdk-server，设置好ip后run就可以使用，地面站软件会自动连接。也可以在该镜像的shell中使用`commander takeoff`等命令直接操作无人机，更多用法[gazebo-px4-dcoker](https://github.com/JonasVautherin/px4-gazebo-headless)。

``` shell
 docker run --name mygazebo --rm -it jonasvautherin/px4-gazebo-headless:1.12.3 <ip.your.phone>
```

注意：mavsdk-java采用C/S架构，其server端集成在安卓中，通过`mavserver.run("ADDRESS")`方法运行，而px4仿真飞控监听指定IP的14540端口，因此必须注意安卓端IP正确，否则server无法连接。该IP在AVD上有问题，建议在真机上调试。

#### 3. 代码

参照本项目`MapActivity.java`，核心思想为RxJava。以起飞功能为例介绍下其用法，该类是一个Com

简单版：

```java
drone.getAction().takeoff().subscribe();
```

升级版：

```java
drone.getAction()
    .takeoff()
    .doOnError(throwable -> {
        Log.d("error");
    })
    .doOnComplete(()->{
        Log.d("success");
    })
    .subscribe(() -> {
       	Log.d("subscribe");
    });
```

解释：代码执行顺序为 先subscri后error/complete，被订阅者会根据情况发送error/complete二者之一。



## 3. 图传集成

​	项目来源：[Android直播开发之旅(10)：AndroidUSBCamera，UVCCamera开发通用库_无名之辈FTER的博客-CSDN博客_uvccamera](https://blog.csdn.net/andrexpert/article/details/78324181)。本项目已经集成了需要的aar包，可以直接下载使用，集成的方法如下：

1. 在[github]([jiangdongguo/AndroidUSBCamera: Universal UVCCamera library,supporting recording,pushing,etc. // USB摄像头(UVC)视频录制和图片抓拍，支持边推流边录像 (github.com)](https://github.com/jiangdongguo/AndroidUSBCamera))上下载该项目，在Android Studio选中`libusbcamera`模块编译，在该模块build->output->aar下找到编译好的`libusbcamera-release.aar`包[^1]
2. 将该aar包以及libs目录下的`libusbcommon_v4.1.1.aar`包复制到自己项目的libs目录下，在moudle-build.gradle下加上`implementation(fileTree("libs"))`
3. 参照`USBCameraAvtivity.java`的代码调用API，即可使用[^2]
4. 更多用法参照根项目[saki4510t/UVCCamera](https://github.com/saki4510t/UVCCamera)



# 四、硬件设备清单

​	本项目在真机上进行了测试，验证了软件可用，所用的设备清单如下。其中，数传必须为WiFi版，根据实际修改连接的地址[^3]。飞控最好用px4，测试过apm飞控也可使用，但需要修改下起飞代码，px4更简单。

|      |                                                              |
| ---- | ------------------------------------------------------------ |
| 数传 | [PW-Link数传 · PW-Link 文档 (cuav.net)](https://doc.cuav.net/data-transmission/pw-link/zh-hans/) |
| 飞控 | [PixHack v3X全功能飞控 · pixhack (cuav.net)](https://doc.cuav.net/flight-controller/pixhack/zh-hans/pixhack-v3.html) |
| 图传 | [模拟图传 · plane (cuav.net)](https://doc.cuav.net/tutorial/plane/optional-hardware/image-transmission.html) |





---



[^1]:想要二次开发原项目代码，注意检查manifest文件配置
[^2]: 网上很多代码中的该库都有问题，在安卓9中黑屏，因此最好自己打包
[^3]: mavsdk-java暂不支持串口模式，更多讨论可用参照issue

