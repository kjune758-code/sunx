# Quant Trading · 港股 / 美股半自动量化 MVP

这是可独立运行的教学模拟项目。Vue 3 → Spring Boot → Python 引擎；信号持久化至 H2（本地）或 MySQL（Docker）。**没有券商 SDK、真实行情、真实下单或自动交易代码。** 人工确认仅把 PENDING 改为 CONFIRMED，不创建订单或持仓。

## 目录

- frontend/src/views：Dashboard、StockPool、Strategy、Backtest、Signals、Positions；router、api、components。
- backend/src/main/java/com/quant：controller、service、repository、entity、config、risk。
- quant-engine：data、strategies/breakout.py、mean_reversion.py、backtest、signals、risk。
- database/schema.sql：MySQL 初始表。
- deploy：Compose、Nginx、环境变量样例及前端容器构建文件。
- scripts/smoke.py：启动服务后的接口联调测试。

## Windows 本地开发

需要 JDK 21、Maven 3.9+、Node.js 22.12+ 或 24、Python 3.10+（建议 3.12）。本地不需要 Docker/MySQL。检查 `java -version`、`mvn.cmd -v`、`node -v`、`npm.cmd -v`、`python --version`。本机 Python 3.10 可运行引擎，代码仅使用标准库。

解压后在 quant-trading 根目录打开三个 PowerShell 终端。下列命令各在独立终端执行；使用 npm.cmd 避免 PowerShell 脚本执行策略问题。

终端 1：
```powershell
cd quant-engine
python -m unittest -v
python app.py
```

终端 2：
```powershell
cd backend
mvn.cmd test
mvn.cmd spring-boot:run
```
首次需联网下载 Maven 依赖。如果默认 Maven 缓存没有写权限，命令中增加 `"-Dmaven.repo.local=$PWD/.m2-cache"`。后端自动创建 `backend/data/quant.mv.db`，重启保留信号，不要同时用多个进程打开同一 H2 文件。

终端 3：
```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```
首次源码交付应包含 package-lock.json；若无锁文件，先执行 `npm.cmd install`。访问 http://localhost:5173。Vite 将 /api 转发到 localhost:8080；引擎在 8000。

另开根目录终端：
```powershell
python scripts/smoke.py
```
测试会写入少量模拟信号，可重复执行。前端验证命令 `npm.cmd run build`；后端 `mvn.cmd package`。后端也可使用 `java -jar target/quant-backend-0.1.0.jar`。

## Docker Compose（Linux 云服务器 / Docker Desktop Linux 容器）

需要 Docker Engine + Compose v2；Windows 使用 Docker Desktop / WSL2。根目录执行：
```powershell
cd deploy
Copy-Item .env.example .env
# 编辑 .env，将两个密码替换为不同随机值（不要提交 .env）
docker compose --env-file .env config
docker compose --env-file .env up --build -d
docker compose ps
docker compose logs -f backend
```
Linux 首次复制用 `cp .env.example .env`。访问 http://localhost:8088；首次拉镜像及 Java 构建需联网。后端初次启动期间页面可能短暂返回 502，查看日志并稍后刷新。只对宿主机 127.0.0.1 暴露前端端口；数据库、后端、引擎没有宿主机端口。云端建议通过 SSH 隧道访问：
```text
ssh -L 8088:127.0.0.1:8088 user@your-server
```
随后本机访问 http://localhost:8088。此 MVP 无认证，不应直接公开到互联网；正式多人使用前需认证、HTTPS、审计和备份。

容器联调在项目根目录运行：
```powershell
$env:API_URL='http://localhost:8088/api'
python scripts/smoke.py
```
`docker compose down` 保留数据库卷；不要为普通重启使用 `down -v`。schema.sql 仅在首次空数据库初始化时运行。Hibernate update 用于 MVP，正式环境应改为版本化迁移。更换 .env 密码不会修改已有卷中的数据库用户密码。

## 使用流程与约束

1. 股票池查看 AAPL / MSFT（USD）及 00700.HK（HKD）；标的列表为固定示例。
2. 回测选择策略和股票。120 根确定性合成价格，无日期及真实交易日历；AAPL 最后一根特意设置突破，以便稳定演示 PENDING → CONFIRMED。
3. 信号页面用 AAPL / breakout / 10 生成待审核信号，点击确认并接受浏览器确认框。
4. 数量改为 1000，可演示金额超限 BLOCKED；HOLD 同样 BLOCKED。
5. 确认不创建持仓，Positions 返回空数组；没有订单端点。

风控：仅允许 BUY、正数且有限的价格、数量 1..100000；单笔名义金额不超过固定模拟权益 100000 的 10%；确认时重新检查且信号 24 小时后失效。数据库乐观锁阻止并发重复确认；重复确认返回 409。每种报价货币独立模拟，**不把 USD 与 HKD 合计**，尚无组合风险、累积敞口、现金预留、卖出、止损、汇率换算及港股整手校验。这些必须在真实交易接入前补齐。

突破：当前价格超过此前 20 根最高收盘价；均值回归：当前价格低于最近 20 根均价的 98%。回测仅做多，每次使用当前现金 10%，信号在 t 收盘计算，t+1 合成收盘价成交，持有 3 根，每边费用 10bp；期末持仓按市价估值。没有复权、股息、滑点、流动性、时区、税费及港美市场微观结构模拟，不能作为实盘效果证据。暂不包含定时任务，生成信号由人工按钮触发。

Python risk 模块供独立引擎演示；最终信号与确认的权威风控在 Java RiskChecker。所有引擎调用有超时；引擎不可用时后端返回 502，不回退为虚假成功。

## API

| Method | Path | 用途 |
|---|---|---|
| GET | /api/health | 模拟模式 / 后端健康 |
| GET | /api/dashboard | 信号数量与模式 |
| GET | /api/stocks | 固定股票池 |
| GET | /api/strategies | 策略列表 |
| GET | /api/backtest?symbol=AAPL&strategy=breakout | 调用 Python 回测 |
| GET | /api/signals | 已保存信号 |
| POST | /api/signals | JSON: symbol, strategy, quantity |
| POST | /api/signals/{id}/confirm | 人工确认；空 JSON 对象 |
| GET | /api/positions | 空模拟持仓 |

Python 内部接口：GET /health、/signal?symbol=AAPL&strategy=breakout、/backtest（相同参数）。不要将引擎直接暴露公网。

## 兼容性参考

- Spring Boot 3.5 / Java：https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Vite / Node：https://vite.dev/guide/

实际执行验证见 VALIDATION.md。此项目是基础源码交付，不代表已部署云服务器或接通券商账户。
