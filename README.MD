
# 使用方式
```
    api 'com.arnoldx.ehr:x-ehrArchitecture:0.1.1'
    api 'com.arnoldx.ehr:x-ehrMvp:0.1.1'
    api 'com.arnoldx.ehr:x-ehrMvvm:0.1.1'
```

# 采用AndroidX进行开发

# 使用规范
1. 颜色资源文件：统一在【CommonView】用cxxx命名，例如：` <color name="cFFFFFF">#ffffff</color>`
- 每个Module都会对资源文件做统一规范，详情见Gradle文件中的 **resourcePrefix** 配置，由于这个配置只能检测XML文件，所有每个Module中的独有的图片资源文件，在文件名面前加上相应的模块名，例如：登录模块：**login_xxx**
- 控件 id 使用驼峰命名，除公共模块，id 命名都要包含自己的模块名。比如 **tvHomeOpenCart** 表示 home 模块的 TextView，用来打开购物车。
- 所有Adapter类不能写在V中，必须单独新建一个类
- 所有的bean文件，只能作为数据载体，不能在里面做任何的逻辑操作，所有的逻辑判断操作全部移到工具类里面处理，每个模块都可以有自己独立的工具类
- ~~当页面需要横屏时` android:screenOrientation="landscape"`，具体查看 [横屏适配](#autoSzie)   暂时移除~~
- 横屏异形屏适配 [异形屏适配](#barChange)


# 项目架构

<table border="1" width="100%" style="text-align: center;">
			<tr>
				<td colspan="6">
				APP<br>
				这里可以进行模块划分，具体可以参考二号人事部的项目架构</td>
			</tr>
			<tr>
                <td colspan="6">
                这里可以再建一个CommonBase库，用于管理一些资源文件，工具类等等非业务的代码
                第三方库都可以在这里进行统一管理
                </td>
            </tr>
			<tr>
				<td  colspan="3">
				CommonMvvm/CommonMvp<br>
				MVVP或者MVP模式的库，一个项目只能选择一种库进行开发
				</td>
				<td  colspan="3">
				CommonView<br>
				通用的自定义View框架,一些APP特有的view效果可以抽出独立成一个库
				</td>
			</tr>
			<tr>
				<td colspan="3">
				CommonArchitecture<br>
				通用框架库。包含BaseApplication、常用的工具类以及架构层的封装</td>
				<td colspan="3"></td>
			</tr>
			<tr>
				<td colspan="2">
				CommonNetwork<br>
				网络库，目前是okhttp + retrfoit
				</td>
				<td colspan="1">
				CommonRepository
				数据管理库
				</td>
				<td colspan="3"/>
			</tr>
		</table>

## 2号人事部项目架构

<table border="1" width="100%" style="text-align: center;">
			<tr>
				<td colspan="6">APP</td>
			</tr>
			<tr>
				<td>
				module_calendar<br>
				日历模块
				</td>
				<td>
				module_employee<br>
				员工模块
				</td>
				<td>
				module_user<br>
				用户模块
				</td>
				<td>
				module_hr<br>
				人事模块
				</td>
				<td>
				module_browser<br>
				web模块
				</td>
				<td>
				module_login<br>
				登录模块
				</td>
				<td>
				OldEhr<br>
				旧项目
				</td>
			</tr>
			<tr>
				<td  colspan="4">
				CommonSdk<br>
				2号人事部项目公共库（与业务逻辑相关，主要包含一些组件化的封装），所有的第三方框架和自定义View框架都在这里添加依赖，以方便统一管理。<br>
				是其他所有子组件必需依赖的库
				</td>
				<td colspan="2">
				CommonBaseService<br>
				公共服务，主要的作用是用于 业务层 各个模块之间的交互(自定义方法和类的调用), 包含自定义 Service 接口, 和可用于跨模块传递的自定义类z
				</td>
			</tr>
			<tr>
				<td  colspan="2">
				CommonMvvm/CommonMvp<br>
				MVVM或者MVP模式的库，一个项目只能选择一种库进行开发
				</td>
				<td  colspan="2">
				CommonView<br>
				通用的自定义View框架,一些APP特有的view效果可以抽出独立成一个库
				</td>
				<td  colspan="3">
				</td>
			</tr>
			<tr>
				<td colspan="2">
				CommonArchitecture<br>
				通用框架库。包含BaseApplication、常用的工具类以及架构层的封装</td>
				<td colspan="5"></td>
			</tr>
			<tr>
				<td colspan="1">
				CommonNetwork<br>
				网络库，目前是okhttp + retrfoit
				</td>
				<td colspan="1">
				CommonRepository
				数据管理库
				</td>
				<td colspan="4"/>
			</tr>
		</table>


## 请求全局拦截处理
自定义了`GsonConverterFactory`，对数据进行拦截处理
具体查看
```
com.arnold.common.sdk.app.GlobalHttpHandlerImpl.onHttpInterceptResponse()
```
方法实现


## 本地数据存储 CommonRepository
- 简单数据存储
    * DataHelper 类，目前采用微信的MMKV代替SharedPreferences进行简单的数据存储
    * 数据库：目前还没有定下来，可以采用GreenDao或者ROOM

## 模块特有的初始化类
实现ConfigModule，便可以进行一些单独模块的初始化，考虑到不同模块可能需要初始化的顺序优先级，采用mLoadWeight字段进行区分，数值越大初始化越先，具体使用方案请看【公共的初始化】
里面的实现

## 公共的初始化
  例如请求的BaseURL,在【CommonSdk】模块中
  ```java
  com.arnold.common.sdk.app.GlobalConfiguration
  ```
  进行配置，

## <span id="autoSzie">横屏适配</span>
实现`me.jessyan.autosize.internal.CustomAdapt` 接口

```java
@Override
     public boolean isBaseOnWidth() {
         return false;
     }
     @Override
     public float getSizeInDp() {
         return 667;
     }
```

## <span id="barChange">异形屏适配</span>
实现**onBarChange**方法监听
```java
    public void onBarChange(BarProperties barProperties) {

    }
```


## 开发模式
项目中提供了MVVM以及MVP的架构。后续的项目开发，可以其中一种模式开发也可以两种模式融合使用。**非组件化项目不建议一个项目采用两种模式开发**

## MVVM
- V: Actvity/Fragment
- VM：VM 为视图提供数据，主要方式为提供 LiveData 或者 Rx 的数据源、或者某些数据可以直接返回的，则提供对应的方法或属性供 V 直接访问。
  - LiveData 不一定就需要在类中创建实例对象，也可以作为方法的返回值直接返回。
  - LiveData 的使用不要局限于 VM 中，没有限制，只要场景适合，就可以使用。
- M：负责数据的存储和获取，统一命名为 xxxModel。
- MVVM 三者通过 Dagger2 进行绑定

## MVP
- V: Activity/Fragment
- P: 里面处理数据逻辑操作，通过注入的View对V层进行数据显示操作。
- M: 负责数据的存储和获取，统一命名为 xxxModel。

### 后续其他项目
只需要引用不同的架构模式进行开发就行
`CommonMvvm`和`CommonMvp`
