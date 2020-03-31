# 日志调试框架

> [点击此处下载Demo](https://raw.githubusercontent.com/getActivity/Logcat/master/Logcat.apk)

![](picture/logo.png)

#### 集成步骤

    dependencies {
        debugImplementation 'com.hjq:logcat:6.0'
    }

#### 使用方式

* 无需调用，直接运行，然后授予悬浮窗权限即可

* 在 debug 模式下运行即可，在 release 正式打包的时不会集成本库，尽管放心

#### 截图欣赏

![](picture/0.jpg)

![](picture/1.jpg)

![](picture/2.jpg)

![](picture/3.jpg)

![](picture/4.jpg)

![](picture/5.jpg)

![](picture/6.jpg)

![](picture/7.jpg)

#### 日志颜色个性化

> 在项目的 `values/color.xml` 中加入你喜欢的配色，例如

    <color name="logcat_level_verbose_color">#FFBBBBBB</color>
    <color name="logcat_level_debug_color">#FF33B5E5</color>
    <color name="logcat_level_info_color">#FF99CC00</color>
    <color name="logcat_level_warn_color">#FFFFBB33</color>
    <color name="logcat_level_error_color">#FFFF4444</color>
    <color name="logcat_level_other_color">#FFFFFFFF</color>

#### 框架亮点

* 只需集成，无需调用

* 日志长按可复制分享

* 支持将日志保存到本地

* 长日志点击可收缩展开显示

* 日志搜索结果支持文本高亮

* 多个相同 TAG 日志自动合并显示

* 仅在 Debug 下集成，无需手动初始化

#### 作者的其他开源项目

* 架构工程：[AndroidProject](https://github.com/getActivity/AndroidProject)

* 网络框架：[EasyHttp](https://github.com/getActivity/EasyHttp)

* 权限框架：[XXPermissions](https://github.com/getActivity/XXPermissions)

* 吐司框架：[ToastUtils](https://github.com/getActivity/ToastUtils)

* 标题栏框架：[TitleBar](https://github.com/getActivity/TitleBar)

* 国际化框架：[MultiLanguages](https://github.com/getActivity/MultiLanguages)

* 悬浮窗框架：[XToast](https://github.com/getActivity/XToast)

#### Android技术讨论Q群：78797078

#### 如果您觉得我的开源库帮你节省了大量的开发时间，请扫描下方的二维码随意打赏，要是能打赏个 10.24 :monkey_face:就太:thumbsup:了。您的支持将鼓励我继续创作:octocat:

![](https://raw.githubusercontent.com/getActivity/Donate/master/picture/pay_ali.png) ![](https://raw.githubusercontent.com/getActivity/Donate/master/picture/pay_wechat.png)

#### [点击查看捐赠列表](https://github.com/getActivity/Donate)

## License

```text
Copyright 2020 Huang JinQun

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```