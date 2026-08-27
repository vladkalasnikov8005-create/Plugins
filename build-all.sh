#!/bin/bash
# Сборка всех плагинов без IDEA - просто запусти этот файл
# На Linux/Mac: ./build-all.sh
# На Windows через Git Bash: bash build-all.sh

set -e

echo "=== Сборка всех плагинов ==="
echo ""

for dir in Smakenchant Tactic dvarf guardianClassPlugin vampirest untitled LeperClass PalePlugin PetMonsters; do
  if [ -d "$dir" ]; then
    echo ">>> Собираю $dir..."
    cd "$dir"
    chmod +x gradlew
    ./gradlew build --no-daemon
    echo "Готово: $dir/build/libs/"
    ls -lh build/libs/*.jar 2>/dev/null || echo "  (jar не найден, проверь build.gradle.kts)"
    cd ..
    echo ""
  else
    echo "Папка $dir не найдена, пропускаю"
  fi
done

echo "=== Все готово! ==="
echo "Все jar лежат в:"
find . -path "*/build/libs/*.jar" -type f | sort
