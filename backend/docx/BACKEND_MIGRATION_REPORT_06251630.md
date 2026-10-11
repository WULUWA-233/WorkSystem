# Backend Migration Report - 2026-06-25 16:30

## Summary

已将后端 Maven 项目迁移到专用目录 `backend/`，根目录保留 monorepo 总览和前端项目。

## New Structure

```text
WorkSystem/
├── backend/
│   ├── pom.xml
│   ├── README.md
│   ├── src/
│   ├── docx/
│   ├── logs/
│   └── target/
├── frontend/
├── README.md
├── .gitignore
└── plan_front.md
```

## Moved Items

| Old Path | New Path |
|---|---|
| `pom.xml` | `backend/pom.xml` |
| `src/` | `backend/src/` |
| `docx/` | `backend/docx/` |
| `target/` | `backend/target/` |
| `README.md` | `backend/README.md` |

## Documentation Updates

1. 新增根目录 `README.md`，说明 `backend/` 与 `frontend/` 的启动方式。
2. 更新 `backend/README.md` 中的后端进入目录：
   - 从 `E:\AI\Claude\WorkSystem`
   - 改为 `E:\AI\Claude\WorkSystem\backend`

## Notes

根目录 `logs/crm-system.log` 未移动，因为该文件被当前系统进程占用。历史日志已进入 `backend/logs/`，后续从 `backend/` 启动后端时，Logback 的 `./logs` 会写入 `backend/logs/`。

## Verification

迁移后需要从后端目录执行 Maven：

```bash
cd backend
mvn test
```
