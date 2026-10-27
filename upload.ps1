# CipherCore Verify - PowerShell Git Upload & Live Deployment Script
param(
    [string]$CommitMessage = "Deploy portal.html, updated landing page, and report generator"
)

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " CIPHERCORE VERIFY - GIT UPLOAD & LIVE DEPLOYMENT SCRIPT" -ForegroundColor Cyan
Write-Host " Target: origin/main and origin/gh-pages" -ForegroundColor Cyan
Write-Host " Live URL: https://nonutsayan.github.io/CipherCore-verify/" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

Write-Host "`n[1/4] Staging files..." -ForegroundColor Yellow
git add portal.html index.html frontend/ server.js backend/report_generator.py assets/ docs/ scripts/ package_project.py .vscode/

Write-Host "[2/4] Committing with message: '$CommitMessage'..." -ForegroundColor Yellow
git commit -m $CommitMessage

Write-Host "[3/4] Pushing to main branch..." -ForegroundColor Yellow
git push origin main

Write-Host "[4/4] Deploying to gh-pages branch..." -ForegroundColor Yellow
git checkout gh-pages
git merge main --no-edit
git push origin gh-pages
git checkout main

Write-Host "`n=====================================================================" -ForegroundColor Green
Write-Host " UPLOAD & DEPLOYMENT COMPLETE!" -ForegroundColor Green
Write-Host " Live Landing: https://nonutsayan.github.io/CipherCore-verify/" -ForegroundColor Green
Write-Host " Live Portal:  https://nonutsayan.github.io/CipherCore-verify/portal.html" -ForegroundColor Green
Write-Host "=====================================================================" -ForegroundColor Green
