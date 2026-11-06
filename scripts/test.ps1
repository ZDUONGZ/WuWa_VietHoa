# Script chay Unit Test voi bo ma Unicode day du tren Windows
Write-Host "Dang bien dich ma nguon va unit test..." -ForegroundColor Cyan
& .\gradlew.bat compileDebugUnitTestKotlin compileDebugKotlin

if ($LASTEXITCODE -ne 0) {
    Write-Host "Bien dich that bai!" -ForegroundColor Red
    exit 1
}

$junitJar = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\junit\junit\4.13.2" -Recurse -Filter "*.jar" | Select-Object -First 1 -ExpandProperty FullName
$hamcrestJar = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\org.hamcrest" -Recurse -Filter "*.jar" | Select-Object -First 1 -ExpandProperty FullName
$kotlinJar = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib" -Recurse -Filter "*1.9.23*.jar" | Select-Object -First 1 -ExpandProperty FullName
$androidJar = Get-ChildItem -Path "D:\android-sdk\platforms" -Recurse -Filter "android.jar" | Select-Object -First 1 -ExpandProperty FullName

$cp = "app\build\tmp\kotlin-classes\debugUnitTest;app\build\tmp\kotlin-classes\debug;$junitJar;$hamcrestJar;$kotlinJar;$androidJar"

Write-Host "Dang chay Unit Tests..." -ForegroundColor Cyan
java -cp $cp org.junit.runner.JUnitCore com.wuwa.viethoa.installer.InstallerLogicTest
