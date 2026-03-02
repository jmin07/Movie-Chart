@echo off
setlocal

powershell -ExecutionPolicy Bypass -File "%~dp0bootrun-with-env.ps1" %*

