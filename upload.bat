@echo off
setlocal enabledelayedexpansion

echo =====================================================================
echo  CIPHERCORE VERIFY - ONE-CLICK GIT UPLOAD & LIVE DEPLOYMENT SCRIPT
echo  Target: origin/main and origin/gh-pages
echo  Live URL: https://nonutsayan.github.io/CipherCore-verify/
echo =====================================================================
echo.

set MSG=Deploy portal.html, updated landing page, and report generator
set /p USER_MSG="Enter commit message (Press Enter for default): "
if not "!USER_MSG!"=="" (
    set MSG=!USER_MSG!
)

echo.
echo [1/4] Staging files...
git add portal.html index.html frontend/ server.js backend/report_generator.py assets/ docs/ scripts/ package_project.py .vscode/

echo.
echo [2/4] Creating commit: "!MSG!"...
git commit -m "!MSG!"

echo.
echo [3/4] Pushing to main branch...
git push origin main

echo.
echo [4/4] Deploying to gh-pages branch...
git checkout gh-pages
git merge main --no-edit
git push origin gh-pages
git checkout main

echo.
echo =====================================================================
echo  UPLOAD & DEPLOYMENT COMPLETE!
echo  Live Landing: https://nonutsayan.github.io/CipherCore-verify/
echo  Live Portal:  https://nonutsayan.github.io/CipherCore-verify/portal.html
echo =====================================================================
echo.
pause
