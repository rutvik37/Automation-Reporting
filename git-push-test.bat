@echo off
echo ============================================================
echo Git: Create Branch 'Test' and Push Everything
echo ============================================================
echo.

:: Switch to or create the 'Test' branch
git checkout -b Test 2>nul
if %ERRORLEVEL% neq 0 (
    echo Branch 'Test' already exists locally. Switching to it...
    git checkout Test
)

:: Add all changes (untracked, modified, deleted)
echo Staging all changes...
git add -A

:: Commit changes
echo Committing changes...
git commit -m "Pushing everything to branch Test"

:: Push the branch to the remote origin and set upstream
echo Pushing to GitHub (origin/Test)...
git push -u origin Test

echo.
echo ============================================================
echo Push complete!
echo ============================================================
pause
