# MDM

##### 使用方法
> 1.依赖VPNClient后，注意修改主工程下的settings.gradle，添加Library以及项目中的build.gradle中添加Library引入。
> 2.把vpn.properties配置文件添加到自己项目的assets下

##### 开启vpn服务
	VPNClient.initVPNClient(Context context, OnVPNInitCallback callback);
> 参数介绍
>    @ param context       上下文
>    @ param callback  (注册vpn服务的回调)

使用实例:

	VPNClient.initVPNClient(getApplicationContext(), new VPNClient.OnVPNInitCallback() {
	            @Override
            public void onVPNInitSuccess() {
                Toast.makeText(getApplicationContext(),"Login success!!!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVPNInitFail(String err) {
                Toast.makeText(getApplicationContext(),err,Toast.LENGTH_LONG).show();
            }
        });

##### 关闭vpn服务：
	VPNClient.loginOutVPN();



#### 注：
>vpn的注册需要跟用户交互，产生用户确认页面，所以在activity中进行，并且注册的过程是异步的，所以如果app需要启动后立刻使用vpn发起请求，一定要在OnVPNInitCallback回调成功后再进行后续操作，否则注册完成前进行的请求不会经过vpn。

