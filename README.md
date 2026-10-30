# WIFI-Chat


<h1>基本功能</h1>

1.上线、下线

2.在线列表

3.消息发送

<h1>网络通信</h1>

核心通信代码借鉴了[kouchat](https://github.com/RUANHAOANDROID/kouchat-android/)

UDP服务端口	12425（用于监听单播或广播消息，服务端类是DatagramSocket)

UDP服务端口	12426（用于监听组播消息，服务端类是MulticastSocket)

TCP服务端口	12425

<h2>数据包</h2>

数据包内容以 ：隔开 例（协议版本:包序号:发送者用户名:命令字:消息正文）

<h1>数据库表设计</h1>

<h2>消息表</h2>

| 字段   	|     数据类型  	|  注释 	|
|     ---	|       ---	|    ---	|
|   id     	|     bigint     	|    消息类型   	|
|    type    	|   tinyint     |    消息类型 (1：文本  2：位置  3：附件通知)  	|
|   	msg_time |   	datetime   |   消息发送时间或接收时间	    |
|   	content  |   	 text      |   	消息内容    |

<h2>用户关系表</h2>

| 字段   |      数据类型      |       注释 |
|       ---|           --- |        --- |
|username	 |    varchar(128)     |	当前登录用户的用户名|
|msg_id|	bigint	|	消息id|
|dir_type	|	tinyint	|消息方向(1：当前用户发送的消息   2：当前用户收到的消息)|
|other_username|	varchar(32)	|	对方用户名(当前用户发送的消息，对方用户为消息接收者.对于当前用户收到的消息，对方用户为消息发送者)|
|other_name	|varchar(128)|		对方姓名|
|status	|tinyint|	消息发送状态，该字段只对当前用户发送的消息有效（0：发送中 1：出错2：送出）|
|is_read	|tinyint		|当前用户是否已读消息，该字段只对当前用户收到的消息有效（0：当前用户未读  1：当前用户已读）|

<h2>附件表</h2>

| 字段   	|     数据类型  	|  注释 	|
|     ---	|       ---	|    ---	|
|id|	bigint|		系统自增ID|
|msg_id	|bigint	|	消息id（这里为附件通知消息的id）|
|file_uuid|	varchar(48)	|	附件uuid|
|type	|tinyint|		附件类型（1照片2音频3视频4文件）|
|filename|	varchar(128)	|	文件名称|
|size	|bigint		|文件大小，单位：byte|
|uri	|varchar(1024)	|	文件本地存储路径。对于文件发送方，该字段为发送文件所存储在本地的路径；对于文件接收方，该字段为接收文件所保存在本地的路径。|
|status	|tinyint|		文件下载状态，对于当前用户接收文件有效 0：下载未完成  1：下载完成|

<h1>技术栈</h1>

- 语言：Kotlin（已由 Java 全量迁移）
- 数据库：[Room](https://developer.android.com/training/data-storage/room)（首次启动自动从旧 LitePal 库 `WiFIChat.db` 迁移数据）
- 异步：Kotlin Coroutines / Flow（替代 RxJava / RxAndroid）
- 权限：`ActivityResultContracts.RequestMultiplePermissions`（替代 RxPermissions）
- 目标系统：compileSdk / targetSdk 37（Android 17），minSdk 23

<h1>引用到的第三方库</h1>

[gson](https://github.com/google/gson/)

[room](https://developer.android.com/jetpack/androidx/releases/room)

[kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)

[logger](https://github.com/orhanobut/logger/)

[photoview](https://github.com/chrisbanes/PhotoView/)

[matisse](https://github.com/zhihu/Matisse/)
