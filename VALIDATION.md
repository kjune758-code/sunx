# 验证记录

执行日期：2026-10-25（Asia/Shanghai）。

| 项目 | 结果 | 说明 |
|---|---|---|
| Vue 生产构建 | PASS | npm run build；34 modules；Vite 7.3.7；package-lock.json 已保存 |
| Java 编译、单元测试、可执行 JAR 打包 | PASS | Maven package / Java 21；RiskCheckerTest 1 项，0 失败 |
| Python 单元测试 | PASS | Python 3.10；3 项，0 失败；确定性数据、突破窗口、风控边界 |
| Java → Python HTTP | PASS | 临时 H2 内存库；真实启动 Spring Boot 与 Python HTTP 服务 |
| Vue → Java → Python HTTP | PASS | 启动 Vite，读取页面入口，经 /api 代理执行同一完整冒烟测试 |
| 风控与人工确认 | PASS | PENDING → CONFIRMED；金额超限 BLOCKED；再次确认 409；不存在 ID 404；非法标的及数量 400 |
| 无交易副作用 | PASS | health 标记 realOrdersEnabled=false，确认后 positions 仍为空 |
| Docker/MySQL 运行 | 未执行 | 本机 PATH 未发现 docker；没有安装 Docker，也没有云端部署 |
| 浏览器可视化 / 点击操作 | 未执行 | 验证了 Vue 编译及 HTTP 入口和代理，没有进行浏览器截图或交互测试 |

源码附带可重复执行的 `scripts/smoke.py`。联调分别通过后端直连与 Vite 代理运行，测试进程已关闭，联调数据仅保存在临时内存数据库。正常本地启动默认使用 H2 文件库。

Docker 配置包含 MySQL/引擎健康检查、数据库卷、Nginx API 代理与 SPA 回退；配置经过源码检查，但容器构建、MySQL 初始化和重启持久化尚待在安装 Docker 的机器上实测。

首次依赖下载遇到沙箱缓存权限与网络限制，改用工作目录 Maven 缓存并允许下载后完成构建。这不需要用户修改系统环境变量。
