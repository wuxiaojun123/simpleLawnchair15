# 左一屏 Fragment 接入文档

本文说明如何把一个业务 `Fragment` 接入 Lawnchair 桌面左一屏，以及接入时需要注意的生命周期、`ActivityResult`、权限申请和外部相机/相册等问题。

## 当前结构

左一屏由 `LeftScreenOverlay` 管理，核心文件：

- `lawnchair/src/app/lawnchair/overlay/LeftScreenOverlay.kt`
  - 创建左一屏容器 `R.id.left_screen_fragment_container`。
  - 把容器挂到 Launcher 根布局 `R.id.launcher`。
  - 通过 `launcher.supportFragmentManager` 挂载业务 Fragment。
  - 使用固定 tag：`left_screen_fragment`。

- `lawnchair/src/app/lawnchair/LeftScreenFragmentResolver.kt`
  - 负责决定左一屏实际展示哪个业务 Fragment。
  - 当前返回的是 `com.simplepdf.pdfeditor.Fragment.HomeFragment()`。

- `src/com/android/launcher3/Launcher.java`
  - 必须在 `onActivityResult()` 里调用 `super.onActivityResult(requestCode, resultCode, data)`，否则业务 Fragment 收不到相册、相机、文件选择器等外部 Activity 返回结果。

## 快速接入一个业务 Fragment

修改 `LeftScreenFragmentResolver.kt`：

```kotlin
package app.lawnchair

import androidx.fragment.app.Fragment
import com.nice.screebkub.OverlayStateFileLogger
import your.package.YourBusinessFragment

object LeftScreenFragmentResolver {
    private const val TAG = "LeftScreenResolver"

    fun createFragment(launcher: LawnchairLauncher): Fragment {
        val resolved = YourBusinessFragment()
        OverlayStateFileLogger.log(
            launcher,
            TAG,
            "createFragment resolved=${resolved.javaClass.name}",
        )
        return resolved
    }
}
```

如果后续要动态切换业务，可以在 `createFragment()` 里根据配置、开关、渠道、登录状态等返回不同 Fragment，但不要在这里做耗时初始化。

## 业务 Fragment 要求

业务 Fragment 必须是普通 AndroidX `Fragment`，例如：

```kotlin
class YourBusinessFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_your_business, container, false)
    }
}
```

注意事项：

- 不要假设左一屏 Fragment 只创建一次；左一屏容器可能 detach/attach，Fragment 可能被移除后重建。
- 外部 Activity 返回前后，`onPause()`、`onResume()`、`onSaveInstanceState()` 都可能发生。
- 需要跨外部 Activity 保存的临时数据，例如拍照输出路径，必须写入 `savedInstanceState` 或持久化存储，不能只放内存字段。
- View 相关引用要在 `onDestroyView()` 释放，避免左一屏重建后引用旧 View。

## ActivityResult 接入规范

优先使用 AndroidX Activity Result API：

```kotlin
private val pickerLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent(),
) { uri: Uri? ->
    if (uri == null) return@registerForActivityResult
    // handle uri
}
```

或者 Java：

```java
private final ActivityResultLauncher<String> pickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            // handle uri
        });
```

关键前提：`Launcher.onActivityResult()` 必须调用 `super.onActivityResult(...)`。本项目已经补过：

```java
@Override
public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mPendingActivityRequestCode = -1;
    handleActivityResult(requestCode, resultCode, data);
}
```

如果不调用 `super`，现象是：

- `registerForActivityResult()` 回调不触发。
- Fragment 的传统 `onActivityResult()` 也不触发。
- 页面从相册/相机回来后只有 `onResume()`，没有结果回调。

## 传统 onActivityResult 兜底

部分旧业务或第三方库还在用 `startActivityForResult()`。可以保留兜底：

```java
private static final int REQUEST_GALLERY_IMAGE = 2001;

startActivityForResult(intent, REQUEST_GALLERY_IMAGE);

@Override
public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_GALLERY_IMAGE) {
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        // handle uri
    }
}
```

传统方式同样依赖 `Launcher.onActivityResult()` 调用 `super`。

## 权限申请

Fragment 内可以直接调用：

```java
requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
```

并在 Fragment 里接收：

```java
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_CAMERA_PERMISSION) {
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            openCamera();
        }
    }
}
```

建议：

- 先检查 `grantResults.length`，不要直接访问 `grantResults[0]`。
- 权限被拒绝时必须关闭 loading 或恢复 UI 状态。
- Android 13+ 图片选择优先使用系统 Photo Picker 或 `GetContent`，不一定需要读存储权限。
- Android 11+ 文件管理权限不要默认申请，除非业务确实要扫描全盘文件。

## 相册选择注意事项

相册返回通常是一个 `Uri`：

```java
Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
intent.setType("image/*");
intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
```

处理时不要直接把 `Uri.getPath()` 当文件路径。应该通过 `ContentResolver` 打开：

```java
ParcelFileDescriptor pfd = requireContext().getContentResolver().openFileDescriptor(uri, "r");
Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
```

失败路径必须收敛 loading：取消、Uri 为空、解码失败、转换失败都要关闭进度条。

## 拍照注意事项

拍照和相册不同。使用 `MediaStore.EXTRA_OUTPUT` 时，相机 OK 后返回的 `Intent data` 经常是 `null`，业务必须提前创建输出文件并保存路径：

```java
File imageFile = File.createTempFile("tempIMG", ".jpg", new File(AppConstants.getTempDirectoryPath()));
String cameraImagePath = imageFile.getAbsolutePath();
Uri outputUri = FileProvider.getUriForFile(
    requireContext(),
    requireContext().getPackageName() + ".provider",
    imageFile
);

Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
intent.setClipData(ClipData.newUri(requireContext().getContentResolver(), "camera_output", outputUri));
intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
startActivityForResult(intent, REQUEST_CAMERA_IMAGE);
```

必须保存 `cameraImagePath`：

```java
@Override
public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("camera_image_path", cameraImagePath);
}
```

恢复：

```java
if (savedInstanceState != null) {
    cameraImagePath = savedInstanceState.getString("camera_image_path");
    if (!TextUtils.isEmpty(cameraImagePath)) {
        imageFile = new File(cameraImagePath);
    }
}
```

返回后检查：

```java
if (imageFile == null || !imageFile.exists() || imageFile.length() <= 0) {
    // 相机没有写入图片，关闭 loading 并提示
    return;
}
```

常见坑：

- `data == null` 是正常的，不代表拍照失败。
- 只把 `imageFile` 放内存字段不可靠，Fragment 重建后会丢。
- `FileProvider` 的 authorities 必须和 Manifest 里一致。
- 需要给相机 Uri 写权限，`EXTRA_OUTPUT`、`ClipData`、grant flags 都建议加上。

## FileProvider

PDF 模块当前 provider 在 `lib/library_pdf/src/main/AndroidManifest.xml`：

```xml
<provider
    android:name="com.simplepdf.pdfeditor.PdfFileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

代码里使用：

```java
FileProvider.getUriForFile(context, requireContext().getPackageName() + ".provider", file)
```

如果新业务模块自己带 FileProvider：

- authorities 不要和现有 provider 冲突。
- library module 里推荐使用 `${applicationId}.xxx`。
- `file_paths.xml` 要覆盖业务实际输出目录，例如 files、cache、external-files。

## Loading/进度条规范

外部 Activity 打开前可以显示 loading，但必须保证所有返回路径都关闭：

- 用户取消。
- 外部 Activity 打不开。
- 权限拒绝。
- resultCode 非 OK。
- data/Uri 为空。
- 文件不存在或文件长度为 0。
- 解码失败。
- 后续异步任务失败。

建议保留一个兜底：

```java
private boolean waitingForExternalResult = false;

@Override
public void onResume() {
    super.onResume();
    if (waitingForExternalResult) {
        mainHandler.postDelayed(() -> {
            if (waitingForExternalResult) {
                waitingForExternalResult = false;
                hideLoading();
            }
        }, 700L);
    }
}
```

这个兜底只能防止 UI 卡死，不能替代真正的结果处理。看到兜底日志时，应该继续查 ActivityResult 是否被宿主吞掉。

## 左一屏生命周期注意事项

`LeftScreenOverlay` 会在以下时机 attach/verify：

- `onActivityStarted()`：attach 左一屏容器和 Fragment。
- `onActivityResumed()`：验证 Fragment 是否还挂在当前容器。
- `onAttachedToWindow()`：重新 attach 并验证。
- `onDetachedFromWindow()` / `onActivityDestroyed()`：detach 左一屏容器。

业务 Fragment 需要注意：

- 不要依赖构造函数参数传 Activity 或 View。
- 需要 Activity 时用 `requireActivity()` / `requireContext()`，并注意 Fragment 是否已添加。
- 异步任务回调里先判断 `binding != null` 或 `isAdded()`。
- 长任务建议在 `onDestroyView()` cancel/dispose。

## 排查日志建议

左一屏接入问题优先看这些日志：

- `LeftScreenOverlay`
  - `attachFragment existing=... attachedToCurrent=...`
  - `verifyLeftScreenAttachment reason=... fragmentAdded=... attachedToCurrent=...`

- `LeftScreenResolver`
  - `createFragment resolved=...`

- 业务 Fragment
  - 打开外部 Activity 前：`progress VISIBLE: open gallery picker` / `open camera`
  - 结果回调：`gallery legacy result resultCode=...` / `camera legacy result resultCode=...`
  - Uri/文件：`gallery uri=...` / `camera image file exists=..., length=...`
  - 兜底：`external image result callback not received after resume`

如果只看到打开日志和兜底日志，没有 result 日志，优先检查宿主 Activity 的 `onActivityResult()` 是否调用了 `super`。

## 编译验证

修改左一屏或业务 Fragment 后，至少跑：

```powershell
$env:JAVA_HOME='E:/jdks/jbrsdk-17'
$env:Path='E:/jdks/jbrsdk-17/bin;'+$env:Path
.\gradlew.bat compileDebugJavaWithJavac :lib:library_pdf:compileDebugJavaWithJavac --no-daemon
```

如果只改 PDF 模块：

```powershell
$env:JAVA_HOME='E:/jdks/jbrsdk-17'
$env:Path='E:/jdks/jbrsdk-17/bin;'+$env:Path
.\gradlew.bat :lib:library_pdf:compileDebugJavaWithJavac --no-daemon
```

## 接入检查清单

- `LeftScreenFragmentResolver.createFragment()` 返回了目标业务 Fragment。
- 业务 Fragment 没有依赖一次性内存状态完成外部 Activity 流程。
- `Launcher.onActivityResult()` 调用了 `super.onActivityResult(...)`。
- 权限申请有拒绝路径处理。
- 相册 Uri 使用 `ContentResolver` 读取。
- 拍照保存了输出文件路径，并处理 `data == null`。
- 所有 loading 显示路径都有关闭路径。
- `onDestroyView()` 清理 binding、handler callback、disposable 或协程任务。
- 编译通过，左一屏滑出/收起/返回桌面流程正常。
