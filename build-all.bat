@echo off
REM Сборка всех плагинов без IDEA - просто запусти этот файл двойным кликом
REM Нужна Java 21+ установленная на компе

echo === Сборка всех плагинов ===
echo.

for %%D in (Smakenchant Tactic dvarf guardianClassPlugin vampirest untitled) do (
    if exist %%D (
        echo ^>^>^> Собираю %%D...
        cd %%D
        call gradlew.bat build --no-daemon
        echo Готово: %%D\build\libs\
        dir build\libs\*.jar 2>nul
        cd ..
        echo.
    ) else (
        echo Папка %%D не найдена, пропускаю
    )
)

echo === Все готово! ===
echo Все jar лежат в папках build\libs\
pause
