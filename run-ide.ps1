$env:JAVA_HOME = 'C:\Users\gaozhenyu\.jdks\temurin-21.0.10'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "Using Java: $env:JAVA_HOME"
.\gradlew.bat runIde --no-configuration-cache
