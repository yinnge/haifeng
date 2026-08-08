# 海峰未来规划院 - 服务器部署指南

> 服务器配置：2核 2G（已针对低内存优化：JVM -Xmx512m、postgres 512m、redis 256m、nginx 128m）

## 目录结构（服务器上）

```
/opt/haifeng/
├── docker-compose.yml        # 从仓库拷贝
├── .env                      # 服务器环境变量（见 .env.server.example）
├── deploy/
│   └── nginx.conf            # 从仓库拷贝
└── dist/
    ├── user/                 # 用户端构建产物（本地 pnpm build 后上传）
    └── admin/                # 管理后台构建产物
```

## 一、本地准备

### 1. 前端构建（本地 Windows 机器）

```powershell
cd D:\SelfCompany\Project-HaiFeng-fronted
pnpm install
pnpm build          # 产出 apps/admin/dist 和 apps/user/dist
```

### 2. 上传产物到服务器

```powershell
# 在服务器创建目录后执行（在服务器上建好目录结构）
scp -r D:\SelfCompany\Project-HaiFeng-fronted\apps\user\dist  root@8.130.89.54:/opt/haifeng/dist/user
scp -r D:\SelfCompany\Project-HaiFeng-fronted\apps\admin\dist root@8.130.89.54:/opt/haifeng/dist/admin
```

## 二、服务器准备

```bash
# 1. 安装 docker（若未装）
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker

# 2. 上传后端仓库（任选其一）
git clone https://github.com/yinnge/haifeng.git /opt/haifeng-src
# 或 scp 本地仓库文件到 /opt/haifeng-src（排除 .git/.env/dist）

# 3. 复制 compose 和 nginx 配置
cp /opt/haifeng-src/docker-compose.yml /opt/haifeng/
cp -r /opt/haifeng-src/deploy /opt/haifeng/
# 注意：deploy/.env.server.example → 复制为 .env 并填真实值

# 4. 创建/编辑 .env（关键！下面再强调）
vim /opt/haifeng/.env

# 5. 构建并启动（在 /opt/haifeng 下）
cd /opt/haifeng
docker compose up -d --build
```

## 三、.env 关键点（与本地 .env 不同！）

- `DB_HOST=postgresql`（容器名，不是 localhost）
- `REDIS_HOST=redis`（容器名）
- `SPRING_PROFILES_ACTIVE=prod`
- **JWT_SECRET / AES_SECRET_KEY / HASH_SALT / DB_PASSWORD / REDIS_PASSWORD 全部重新生成**，不要复用仓库里的示例值
- 模板见 `deploy/.env.server.example`

## 四、防火墙 / 安全组

- 只放行 **80 端口**（http）
- 数据库 5432 / Redis 6379 **不要对外开放**（compose 已不映射公网端口）
- 保留你的 SSH 端口

## 五、验证

```bash
curl http://8.130.89.54/api/v1/app/home/...    # 用户端 API
curl http://8.130.89.54/                       # 用户端页面
curl http://8.130.89.54/admin/                 # 管理后台
docker compose ps                              # 查看容器状态
docker compose logs -f haifeng-admin           # 看启动日志（Flyway 建表）
```

## 六、常见问题（2G 内存）

| 问题 | 处理 |
|------|------|
| 构建 OOM | 服务器内存紧张时，可在本地 `mvn package -pl haifeng-admin -am -DskipTests` 打包，将 jar 传入服务器后用 `docker compose build` 跳过 Maven 阶段（或临时加 swap） |
| Flyway 建表失败 | 首次启动 admin 会自动建表，看 `docker compose logs haifeng-admin` |
| 数据导入 | 空库部署，业务数据（院校/Excel 数据）上线后到管理后台逐项导入 |

swap 建议（2G 服务器强烈建议）：
```bash
fallocate -l 2G /swapfile && chmod 600 /swapfile
mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```