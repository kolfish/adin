@echo off
setlocal enabledelayedexpansion
title RuntimeFabricMod - Mod to DLL Converter

echo ================================================================
echo        RuntimeFabricMod - Fabric Mod (.jar) to (.dll)
echo ================================================================
echo.

set "SCRIPT_DIR=%~dp0"
if exist "E:\mingw64\bin" set "PATH=E:\mingw64\bin;!PATH!"
set "MOD_JAR=%~1"

:: Check if a jar file was passed as argument or dragged ^& dropped
if "%MOD_JAR%"=="" (
    echo [!] No .jar file provided.
    echo.
    echo Usage:
    echo   Method 1: Drag and drop your mod .jar directly onto this file.
    echo   Method 2: Run: convert_mod.bat path\to\your_mod.jar
    echo.
    set /p "MOD_JAR=Or enter the full path to your mod .jar now: "
    if "!MOD_JAR!"=="" (
        echo [ERROR] No jar file specified. Exiting.
        pause
        exit /b 1
    )
)

:: Remove surrounding quotes
set "MOD_JAR=!MOD_JAR:"=!"

if not exist "!MOD_JAR!" (
    echo [ERROR] File not found: "!MOD_JAR!"
    pause
    exit /b 1
)

:: Extract jar name and base name
for %%F in ("!MOD_JAR!") do (
    set "JAR_NAME=%%~nxF"
    set "BASE_NAME=%%~nF"
)

echo [*] Target Mod: !JAR_NAME!
echo.

:: 1. Check for Java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'java' command was not found in your PATH!
    echo Please make sure Java 17, 21, or 25 is installed and in your PATH.
    pause
    exit /b 1
)

:: 2. Generate C++ headers from Jar
echo [*] [1/3] Extracting mod classes and generating C++ header...
java -jar "%SCRIPT_DIR%compiled_java\GenerateHeaders.jar" input-jar "!MOD_JAR!" "%SCRIPT_DIR%dll\injecting_classes\jar.h"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Failed to generate jar.h from !JAR_NAME!
    echo Please check if the jar is a valid, uncorrupted Fabric mod jar.
    pause
    exit /b 1
)
echo [OK] jar.h generated successfully.
echo.

:: 3. Compile the DLL with CMake
echo [*] [2/3] Compiling .dll with CMake...
if not exist "%SCRIPT_DIR%output" mkdir "%SCRIPT_DIR%output"

set "BUILD_DIR=%SCRIPT_DIR%dll\build"

:: Check for CMake
where cmake >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo [NOTICE] 'cmake' was not found in your PATH!
    echo.
    echo jar.h has been generated successfully in dll\injecting_classes\jar.h
    echo.
    echo To finish compiling the .dll, choose one of these options:
    echo   Option A: Install CMake (cmake.org) and MinGW-w64 / Ninja.
    echo   Option B: Open the '%SCRIPT_DIR%dll' folder in Visual Studio or CLion
    echo             and build the Release configuration.
    echo.
    pause
    exit /b 0
)

:: Configure build directory
cmake -B "%BUILD_DIR%" -S "%SCRIPT_DIR%dll"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] CMake configuration failed.
    echo Make sure you have a working C++ compiler (Visual Studio or MinGW-w64).
    pause
    exit /b 1
)

:: Build the Release DLL
cmake --build "%BUILD_DIR%" --config Release
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

:: 4. Locate and Copy the Output DLL
echo.
echo [*] [3/3] Finalizing output DLL...

set "FOUND_DLL="
if exist "%BUILD_DIR%\Release\jvmwork.dll" set "FOUND_DLL=%BUILD_DIR%\Release\jvmwork.dll"
if exist "%BUILD_DIR%\libjvmwork.dll" set "FOUND_DLL=%BUILD_DIR%\libjvmwork.dll"
if exist "%BUILD_DIR%\jvmwork.dll" set "FOUND_DLL=%BUILD_DIR%\jvmwork.dll"

if "!FOUND_DLL!"=="" (
    echo [WARNING] Built successfully, but could not auto-locate the DLL in %BUILD_DIR%.
    echo Please check the %BUILD_DIR% directory for the output .dll file.
) else (
    copy /y "!FOUND_DLL!" "%SCRIPT_DIR%output\!BASE_NAME!.dll" >nul
    echo.
    echo ================================================================
    echo   [SUCCESS] Mod successfully converted to DLL!
    echo   Saved to: output\!BASE_NAME!.dll
    echo ================================================================
)

echo.
pause
