# HelpCacheMeet-Installer
HelpCacheMeetDatapackInstaller 是HelpCacheMeet最初版的精简版本，一个简单 Minecraft 数据包安装和管理工具。它基于defination.yml解析数据包依赖关系，按照依赖顺序进行拓扑排序，修改 level.dat 的 NBT 结构以完成启用顺序的修改，并智能重命名数据包文件使其更加简洁直观。

## 核心功能

- ✅ **自动环境检测**: 验证软件是否放置在正确的 Minecraft 存档目录中
- ✅ **依赖关系解析**: 解析数据包的 `defination.yml` 配置文件中的依赖关系
- ✅ **拓扑排序**: 使用 Kahn 算法对数据包进行依赖关系排序
- ✅ **NBT 文件修改**: 安全地修改 `level.dat` 文件的 NBT 结构
- ✅ **智能重命名**: 将数据包重命名为 "项目名 版本号" 格式
- ✅ **用户界面**: 提供协议确认和消息提示窗口
- ✅ **日志记录**: 完整的日志系统记录所有操作
