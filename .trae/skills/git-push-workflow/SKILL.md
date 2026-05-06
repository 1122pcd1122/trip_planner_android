---
name: "git-push-workflow"
description: "Handles git commit and push workflow for both Android client and backend Agent projects. Invoke when user asks to commit and push code changes to remote repositories."
---

# Git Push Workflow

This skill handles the git commit and push workflow for both the Android client and backend Agent projects.

## Project Information

| Item | Android Client | Backend Agent |
|------|----------------|---------------|
| Path | `c:\Users\pcd11\Desktop\trip-planner\Android` | `c:\Users\pcd11\Desktop\trip-planner\Agent` |
| Repo | `https://github.com/1122pcd1122/trip_planner_android.git` | `https://github.com/1122pcd1122/trip_planer.git` |
| Branch | `main` | `master` |

## Push Workflow

### Step 1: Check Status

```powershell
cd c:\Users\pcd11\Desktop\trip-planner\Android; git status
cd c:\Users\pcd11\Desktop\trip-planner\Agent; git status
```

### Step 2: Stage and Commit

Stage all changes and commit:

```powershell
cd c:\Users\pcd11\Desktop\trip-planner\Android; git add -A; git commit -m "commit message"
cd c:\Users\pcd11\Desktop\trip-planner\Agent; git add -A; git commit -m "commit message"
```

### Step 3: Push to Remote

```powershell
cd c:\Users\pcd11\Desktop\trip-planner\Android; git push -u origin main
cd c:\Users\pcd11\Desktop\trip-planner\Agent; git push -u origin master
```

### Step 4: Handle Issues

If push fails:
- Check if network is available (`ping github.com`)
- If token authentication fails, ask user for a new token
- If secret scanning blocks push, check for tokens in committed files
- Remove sensitive files from commit if detected by GitHub secret scanning

## Important Notes

- Always check git status first to see what changes need to be committed
- Use meaningful commit messages describing the changes
- Push Android client first, then backend
- Report success or failure to user with details
- If GitHub secret scanning blocks push, identify and remove the sensitive file from the commit
