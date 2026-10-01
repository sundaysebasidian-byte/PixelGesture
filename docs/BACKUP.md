# Configuration backup

Manual backup is a development-source feature added after the published 0.6.0 release. It does not depend on Android Auto Backup or any account.

1. In settings, select **Export settings** and choose an on-device location in the system document picker.
2. Keep the `.pgbackup` file private. It is readable text containing all 36 bindings, selected app package names, edge width, haptics, language and format/configuration versions. It contains no screen content, installed-app inventory, permissions or credentials.
3. On the receiving device running a source build with backup support, select **Restore settings**, choose the file and confirm replacement. Cancel leaves preferences unchanged.
4. Restore pauses the master switch. Review app targets and gesture/navigation conflicts before manually resuming. Apps must be installed separately; a missing target uses the existing unavailable-app feedback. Accessibility authorization is never copied or enabled.

Backups use Java properties syntax, format version 1 and configuration version 2. Import requires the complete known configuration, rejects duplicate/unknown keys, invalid values, unsupported versions and files larger than 64 KiB before changing preferences. Old app-target entries absent from the backup are removed. Unsupported future versions fail explicitly; keep the original file for the version that produced it.

The app receives temporary access only to the selected document and requests no broad storage permission. A cloud document provider chosen in the system picker may upload the file independently of Pixel Gesture. Prefer local storage for an offline workflow. Uninstalling does not remove exported documents; delete them yourself when no longer needed.

## 简体中文

手动备份是官方 0.6.0 发布之后的源码开发功能，无需账号或 Android 自动备份。

在设置中导出 `.pgbackup` 到设备本地存储；在支持备份的源码版本中选择恢复文件并确认替换。取消不改配置，恢复后总开关保持暂停，检查应用目标和导航冲突后再手动开启。目标应用需要另行安装，无障碍授权不会复制或自动开启。

文件是明文，包含 36 个绑定、所选应用包名、热区宽度、振动、语言及版本号；不含屏幕内容、已安装应用清单、授权或凭据。解析完成前不修改设置；拒绝超过 64 KiB、缺失/重复/未知字段、非法值和不支持的版本。备份中不存在的旧应用目标会清除。

只临时访问用户选择的文件，不申请广泛存储权限。云端文件提供方可能自行上传，请选择本地存储并妥善保管；卸载应用不会删除已导出的文件。
