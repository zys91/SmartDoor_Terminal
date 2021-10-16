# SmartDoor_Terminal

智能门禁 | 设备终端 | 树莓派 | Android APP | 可视化门铃 | WebRTC | 视频通话 | 远程开门 | 人脸解锁

## 应用特点及食用指北

1. 人脸识别使用的虹软的人脸识别**ArcFace SDK**，教程详见<https://ai.arcsoft.com.cn/manual/docs#/140>；
2. 人脸录入、比对均在设备端完成。其中人脸录入需要登录账号确认，设备端不支持用户注册，注册需在移动用户端完成，另外人脸存储在设备端本地，人脸比对离线完成，无需联网；
3. 门禁解锁通过I/O口输出电平控制继电器实现，I/O口电平控制通过DOS命令来实现，控制的是**GPIO26**管脚；
4. 部署环境：树莓派3B+和LineageOS（多平台适配的开源三方Android系统），LineageOS 16.0 (Android 9)镜像链接<https://konstakang.com/devices/rpi3/LineageOS16.0/>；
5. 远程开门指令通过WebRTC携带信息进行发送与接收；
6. 摄像头采用的USB接口外置带红外摄像头，可实现夜间人脸识别解锁。

## 友情提醒

[此仓库](https://github.com/zys91/SmartDoor_Terminal)为智能门禁设备终端侧，需与智能门禁移动客户端配合食用，用户侧仓库[传送门](https://github.com/zys91/SmartDoor_Client)