# Astro 
注意GameController和Coordinate是单例的。  
网络相关的Handler是空的。  
动画和音效已经不再是Handler，因为没必要。  
动画补完，做了一点优化，模拟器基本不卡了。  
实现了3种憨憨AI（尽管只用到一种）。  
现在基本可以进行本地游戏。  
2,4,6起飞，6连续行动，能跳的时候跳之前和跳之后都能撞机。  
（点数可以在Value类里改，以后想改成可以设置功能里改的）  

补充大厅界面作为开始界面，但是界面功能未实现。  
为方便调试，大厅的设置按钮用于进入游戏界面。 



## 房间界面

1. client申请连接server

```
type = VALUE.hello;
clientName = 客户端nickname;
```

返回

```
type = VALUE.hello;
clientNames = [nickname1, nickname2, nickname3, nickname4]; // 有可能有null
clientIPs = [ip1, ip2, ip3, ip4];

```

