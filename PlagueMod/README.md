# PlagueMod

Fabric мод для **Minecraft 26.2** (Java 25, Mojang official mappings): слияние плагинов
**LeperClass** и **PalePlugin** в один jar + клиентские фичи, невозможные в плагинах.

Плагины при этом **не тронуты** и остаются рабочей серверной версией.

## Сборка

```bash
cd PlagueMod
./gradlew build --no-daemon
# jar: build/libs/plague-*.jar
```

Нужны JDK 25 и интернет (Loom скачает Minecraft + Fabric).
В CI собирается отдельной джобой `build-mod` (см. `.github/workflows/build.yml`).

Тулчейн: MC 26.2, Fabric Loader 0.19.5, Loom 1.17, Fabric API 0.160.0+26.2,
Gradle 9.4.0, `mappings loom.officialMojangMappings()` (Yarn для 26.x не существует).

## Что внутри

| Модуль | Пакет | Содержимое |
|---|---|---|
| Общее | `org.examplee.plague` | `PlagueMod` (entrypoint), `PlagueConfig` (`config/plague.json`) |
| Прокажённые | `leper/` | Заражение/класс/солнце/зонты/фонари, 12 предметов, сгусток чиха, зелья крови |
| Бледность | `pale/` | Распространение, обереги, биомы, стадии, 6 предметов + жезлы |
| Тьма | `darkness/` | **Новый блок тьмы**, рост/ветви, карта, жезл света |
| Мобы | `entity/` | Чумной доктор и мрачный охотник (AI, модели, анимация, яйца) |
| Сеть | `network/` | 7 пакетов: заражение, зоны, карта, заряд оберегов, катсцена |
| Команды | `command/` | `/plague leper…/pale…/dark…`, `/purify` |
| Клиент | `client/` | HUD (полоса заражения, заряд, бейджи зон), GUI-карта (M), виньетки, катсцена, зонт открыт/закрыт |
| Миксины | `mixin/` | Мобы игнорят прокажённых; голод x4 |

Конфиг — все числа баланса из обоих плагинов 1:1, правится в `config/plague.json`
(команды `on/off/speed/...` тоже сохраняются туда).

## Управление и команды

- **M** — карта зоны (бледность/тьма — по карте в руке).
- `/plague leper add|remove|bless|unbless|sneeze|status <игрок>`, `give <предмет> <игрок> [кол-во]`
- `/plague pale on|off|speed|info|give ...`, `/plague dark on|off|speed|growth|infectall|info|give ...`
- `/purify [игрок]` — выдать вакцину.

## Временные заглушки (v1)

- **Текстуры** — процедурные плейсхолдеры (`textures/`), заменить настоящим пиксель-артом.
- **Звуки** — `sounds.json` + 5 событий готовы, `.ogg` файлов нет (тихо, без краша).
- **3D-туман/небо** — только HUD-виньетка + ванильный эффект Darkness; настоящий туман = миксин на FogRenderer (TODO).
- **Маппинги 26.2** — код писан по каноническим Mojmap-сигнатурам; если Loom/26.2 что-то переименовал,
  первая сборка подсветит точные места (кандидаты: `MobRenderer`/`HudElementRegistry`, `SpawnEggItem`).

## Структура ресурсов

`assets/plague/`: `lang/en_us.json`, `ru_ru.json`, 30 моделей предметов (у зонтов override
`plague:open` для раскрытия), 4 блока (blockstate/модель/лут), `sounds.json`, текстуры.
Лут: великий оберег и тьма не дропаются таблицей (заряд возвращается через `playerWillDestroy`).
