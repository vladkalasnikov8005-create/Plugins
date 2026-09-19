# Tactic — Paper 26.2

Плагин для **Paper/Spigot 26.2** (Java 25): маска, боевые предметы, кальян/сигареты, вирус мяуканья, ритуальный костёр, территории и **алмазный прессинг**.

## Сборка

```bash
./gradlew jar          # Linux/macOS
gradlew.bat jar        # Windows
```

Готовый файл: `build/libs/Tactic.jar` → папка `plugins` сервера **Paper 26.2**.

Требуется **Java 25** (toolchain в Gradle подтянет сам, если настроен).

## Команды

### `/tactic` (алиасы: `maskmenu`, `tactict`)

| Подкоманда | Право | Описание |
|---|---|---|
| `/tactic` / `menu` | `tactic.menu` | GUI выдачи предметов |
| `/tactic give <item> [игрок] [N]` | `tactic.give` | Выдать предмет |
| `/tactic unmask <игрок>` | `tactic.unmask` | Снять маску |
| `/tactic meowinfo \| infect \| cure \| cureall \| meow` | `tactic.meow.admin` | Вирус мяуканья |
| `/tactic bonfire mobs <on\|off>` | `tactic.reload` | Мобы от костра |
| `/tactic diamond [on\|off\|give]` | info всем; on/off — `tactic.reload` | Алмазный прессинг |
| `/tactic smokestats` | `tactic.menu` | Топ курильщиков |
| `/tactic glad on\|off` | `tactic.glad` | Защита ника Glad от чужих команд |
| `/tactic reload` | `tactic.reload` | Перечитать config.yml |

Базовый доступ к команде: **`tactic.use`** (default: true).

### `/region` (алиасы: `rg`, `territory`, `территория`, `регион`)

| Команда | Описание |
|---|---|
| `/region` | Инфо о регионе, в котором стоишь |
| `/region create <имя> [заголовок]` | Создать по выделению жезлом |
| `/region delete/info/list <имя>` | Удалить / инфо / список |
| `/region title <имя> <текст>` | Заголовок (цвета `§`) |
| `/region rules <имя> add \| set \| remove \| clear` | Правила |
| `/region coowner/member/blacklist <имя> add\|remove\|list` | Участники / ЧС |
| `/region wand` | Выдать жезл выделения |

Права: `tactic.region.use` (default true), `tactic.region.admin` (op).

## ✦ Защита Glad

`/tactic glad on|off` (`tactic.glad` / op):

- **on** — любые команды других игроков/консоли, где в аргументах есть ник `Glad`, блокируются.
- Сам **Glad** может использовать команды **на себя**.
- Сохраняется в `config.yml` (`glad-protect-enabled`).

## 🌿 Петрушевый чай

Легендарный напиток (`/tactic give parsleytea` / `чай`):

- Длинное легендарное описание (без перечисления эффектов в лоре)
- ПКМ — выпить (скрытые эффекты на 5 минут)
- Меню: Инструменты

## 🫒 Оливковый чай

Ещё более редкий легендарный напиток (`/tactic give olivetea` / `olive` / `оливковый`):

- Богатый лор «рощи, которой нет на картах» (без списка эффектов)
- ПКМ — «Сеньория оливы» **8 минут**: носитель неуязвим к урону от сущностей, PvP в радиусе **8 блоков** блокируется, урон по мобам в ауре −40%
- Скрытые долгие баффы (дольше петрушки) + короткий старт регена
- Меню: Инструменты

## 🚬 Сигареты

- В одной пачке **8** сигарет (счётчик на пачке)
- Можно **курить на ходу**; урон и движение **не сбрасывают** процесс

## Маска

- Предмет: бумага `§5Маска` (`/tactic give mask`), PDC-метка.
- ПКМ — ник → `????` на **30 минут** (config: `mask-duration-seconds`).
- Скрывается: TAB, чат, nametag (scoreboard team), join/quit/kick/death.
- Снять: ножницы разоблачения, `/tactic unmask`, истечение таймера.
- **Персист:** expire-время в PDC игрока — маска переживает **рестарт сервера**.
- `tactic.mask.protected` — нельзя сорвать ножницами.

## ◆ Гранёные алмазы

Как устроено:

1. **С алмазной руды** падают **негранёные** алмазы (не обычные ванильные «готовые»).
2. **Ванильный алмаз** (данжи, трейды и т.п.) в верстаке: `1 ванильный` → **1 негранёный**.
3. **Огранка в верстаке:** `6 негранёных + 1 бумага` → **1 гранёный алмаз**.
4. **Алмазные предметы** (броня, инструменты, блок, jukebox, enchanting table) крафтятся **только из гранёных**.
5. Обычный/негранёный алмаз в таком крафте **не принимается**.
6. Silk Touch по-прежнему даёт блок руды; печь с руды тоже выдаёт негранёный.

Конфиг:
```yaml
diamond-hard-enabled: true
diamond-uncut-per-cut: 6
```

Команды:
```
/tactic diamond
/tactic diamond on|off
/tactic diamond give cut|uncut [N]
/tactic give cutdiamond 16
/tactic give uncutdiamond 64
```

Право обхода: `tactic.diamond.bypass`.


## Прочий контент (кратко)

- **Боевые:** динамит, фаербол, дым, кассета, липучка, стан, крио, самонаводящийся лук, поводок.
- **Инструменты:** плуг, ритуальный костёр (топливо, орда, Варден), жезл территорий.
- **Кальян / табаки / сигареты** + никотиновая передозировка + smokestats.
- **Вирус мяуканья** (10 стадий, вакцина, слюни, карантин-GUI). Маска (активная) снижает шанс заражения до 1%.

## Права (plugin.yml)

| Право | default |
|---|---|
| `tactic.use` | true |
| `tactic.menu` | true |
| `tactic.give` | op |
| `tactic.unmask` | op |
| `tactic.reload` | op |
| `tactic.mask.protected` | false |
| `tactic.meow.admin` | op |
| `tactic.region.use` | true |
| `tactic.region.admin` | op |
| `tactic.diamond.bypass` | false |

## Технические заметы

- Пакет: `org.examplee.tactic`, главный класс `TacticPlugin`.
- Предметы через **PersistentDataContainer**, не только CustomModelData.
- Paper API `26.2.+`, `api-version: '26.2'`.
- Версия артефакта: **1.9**.
