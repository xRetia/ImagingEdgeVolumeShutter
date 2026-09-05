# SONY ImagingEdge 音量键快门 📷

让 Sony ImagingEdge 支持音量键和蓝牙遥控拍照。

## 功能

- 音量加键、音量减键触发原有拍照按钮。
- 支持通过蓝牙遥控器发送音量键进行拍照。
- 音量键由模块消费，不会继续调节 Android 系统音量。
- 原有触屏拍照功能保持不变。
- 默认静默运行，不显示 Toast，不发送通知。
- 调试时在 `/sdcard/vsdebug.xretia` 创建空文件，即可开启 Toast 和通知栏日志；删除文件后恢复静默。

## 使用方法

1. 使用新版 LSPatch，将本模块集成到原版 Sony Imaging Edge APK。
2. 在 LSPatch 中把 `SONY IEM 音量键快门` 添加到模块，并限定作用域为 `com.sony.playmemories.mobile`。
3. 重新生成并安装集成后的 APK。
4. 进入 Imaging Edge 的远程拍摄界面即可使用音量键或蓝牙遥控拍照。

请不要在已经集成过旧版本模块的 APK 上重复修补，建议从原始 APK 重新集成。

## 调试日志

默认不输出任何日志。如果需要排查问题，在设备上创建：

```text
/sdcard/vsdebug.xretia
```

重新启动 Imaging Edge 后，模块会输出 Toast 和通知栏信息。确认完成后删除该文件并重启应用。

## 兼容性

| 项目 | 支持情况 |
|---|---|
| Android | Android 8.0 及以上（API 26+） |
| Hook 框架 | LSPatch 现代模块模式、LibXposed API 102 |
| 目标应用 | Sony Imaging Edge `com.sony.playmemories.mobile` |
| 已测试版本 | Imaging Edge 7.8.5 ✅ |
| 蓝牙遥控 | 发送音量键事件的遥控器 |
| B 门 | 按住音量键，松开结束曝光 |

模块包名：`com.xretia.sonyiem.volumeshutter`

现代模块入口：`META-INF/xposed/java_init.list`
