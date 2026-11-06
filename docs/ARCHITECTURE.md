# Architecture

The mod is organized **by feature**. Everything for one piece of gameplay (its items, blocks,
events, commands, networking and rules) lives in one folder, so a feature can be read, changed or
deleted on its own. Inside a feature, the game **rules** (`logic/`) are plain Java, kept apart from the
code that talks to Minecraft, so they can be unit tested in milliseconds.

Every package has a `package-info.java` explaining what belongs there. The rules below are enforced
by [ArchitectureTest](../src/test/java/com/codefee/mcfunmod/architecture/ArchitectureTest.java),
so CI fails if they are broken.

## Layout

```
src/main/java/com/codefee/mcfunmod/
  McFunMod.java                  entry point: calls common init, then each feature's init()
  common/                        shared by more than one feature
    logic/                         pure-Java helpers (e.g. WeightedTable<T>, Cooldown)
    registry/                      register helpers, the shared creative tab (ModCreativeTab)
    config/                        server config file (ModConfig), one section per feature
  feature/
    luckycoin/                   example feature
      LuckyCoin.java               entry class: init() registers everything this feature owns
      logic/                       pure-Java rules, no Minecraft imports (only if the feature has rules)
      item/                        Item subclasses
      block/                       Block / BlockEntity subclasses
      entity/                      Entity subclasses and AI goals
      event/                       Fabric event listeners
      command/                     Brigadier commands
      network/                     payload records + server-side receivers
  mixin/
    luckycoin/                   mixins for that feature (all mixins must sit under mixin/)

src/client/java/com/codefee/mcfunmod/client/
  McFunModClient.java            client entry point: calls each feature's client init()
  feature/luckycoin/
    LuckyCoinClient.java           client entry class
    render/  screen/  keybind/  network/
  mixin/luckycoin/

src/datagen/.../datagen/provider/   one provider per output type, one section per feature
src/gametest/.../gametest/feature/luckycoin/LuckyCoinGameTests.java
src/test/.../feature/luckycoin/logic/...Test.java
```

Create only the folders a feature needs. A simple item with no rules of its own is just an entry
class and an `item/` folder.

## How the pieces talk

```
     Minecraft / Fabric
            │ calls
            ▼
  feature/<name>/item, event, command, network, mixin     read input from Minecraft,
            │ calls                                        call logic, apply the result
            ▼
  feature/<name>/logic   +   common/logic                 plain Java rules, unit tested
```

`logic/` receives plain values (numbers, strings, records, enums) and returns plain values. The
Minecraft side converts in both directions. Example: the item rolls `random.nextInt(table.totalWeight())`
and passes the number into the logic, so the logic never needs Minecraft's random generator.

If logic really needs to ask the game something while it runs (e.g. "is this player online?"), give
it a small interface inside its own `logic/` package and implement it on the Minecraft side.
Only add such an interface when plain values are not enough.

## Rules

| # | Rule | Enforced |
|---|---|---|
| 1 | `logic/` packages (feature and common) import no Minecraft, Fabric or Mixin classes, and nothing from the mod outside `logic/` | ArchitectureTest |
| 2 | A feature never uses another feature's internals. If it must use another feature, it calls that feature's **entry class** only (e.g. `LuckyCoin`, never `luckycoin.item.LuckyCoinItem`). Client and server parts of the same feature count as one feature | ArchitectureTest |
| 3 | No dependency cycles between features | ArchitectureTest |
| 4 | `common/` never imports from `feature/`. Code moves to `common/` only once a second feature needs it | ArchitectureTest |
| 5 | Nothing references a mixin class (they do not exist at runtime) | ArchitectureTest |
| 6 | Client-only code lives in `src/client`. Common code cannot see it, so a dedicated server never loads rendering classes | Compiler (separate source sets) |
| 7 | Minecraft-side folders stay thin: read input, call `logic/`, apply the result. If a listener or item method grows beyond glue, move the rule into `logic/` | Code review |
| 8 | Each feature registers its own content from its entry class. `McFunMod` only calls `init()` methods, in order | Code review |

## Adding a feature (step by step)

1. Create `feature/<name>/<Name>.java` with a `public static void init()` method.
2. Call it from `McFunMod.onInitialize()` (and `<Name>Client.init()` from `McFunModClient` if the
   feature has client parts).
3. Write the rules in `feature/<name>/logic/` as plain Java, with unit tests in
   `src/test/.../feature/<name>/logic/`. Skip this step if the feature has no rules of its own.
4. Add the Minecraft side in the folders you need (`item/`, `event/`, ...), and register it from `init()`
   using the helpers in `common/registry`. Add items to the shared tab from `init()` with
   `CreativeModeTabEvents.modifyOutputEvent(ModCreativeTab.KEY)`.
5. Add the feature's models, names, recipes and loot in its section of each datagen provider, run
   `./gradlew runDatagen`, and commit `src/main/generated`.
6. Put textures and sounds in `src/main/resources/assets/mcfunmod/` (`textures/item/<name>.png`, ...).
7. Add a game test class in `src/gametest/.../feature/<name>/` and list it under `fabric-gametest`
   in `src/gametest/resources/fabric.mod.json`.

## Where do I put...?

| I want to... | Put it in |
|---|---|
| A game rule or calculation | `feature/<name>/logic/` + a unit test in `src/test` |
| A helper several features use | `common/logic/` (pure Java) or `common/registry/` (registration) |
| An item | `feature/<name>/item/`, registered in `<Name>.init()`, added to the tab, plus datagen + texture |
| A block (with stored data) | `feature/<name>/block/` (Block + BlockEntity), registered in `<Name>.init()`, plus datagen + texture |
| A mob / entity | `feature/<name>/entity/`; its renderer in `client/feature/<name>/render/` |
| A reaction to something in game | Listener in `feature/<name>/event/`, registered in `<Name>.init()` |
| A `/command` | `feature/<name>/command/`, registered in `<Name>.init()` |
| Data sent between server and client | Payload + server receiver in `feature/<name>/network/`, client receiver in `client/feature/<name>/network/` |
| A GUI screen or key binding | `client/feature/<name>/screen/` or `keybind/` (send actions to the server via a payload) |
| A server config option | The feature's section in `common/config/ModConfig` |
| A change to vanilla code with no Fabric event | Mixin in `mixin/<name>/`, listed as `"<name>.FooMixin"` in `mcfunmod.mixins.json` |
| A test that needs the real game | `src/gametest/.../feature/<name>/` |

## Testing strategy

- **Unit tests** (`src/test`): `logic/` packages only. Fast, no Minecraft setup.
- **Architecture tests** (`src/test/.../architecture`): enforce the rules above.
- **Game tests** (`src/gametest`): a few end-to-end checks per feature in a real server.
- **Metadata check** (`.github/scripts/check_metadata.py`): versions, entrypoints and mixin configs are consistent.

## CI

`.github/workflows/ci.yml` runs on every push to `main` and every pull request:

| Workflow | What it proves |
|---|---|
| `_validate.yml` | Gradle wrapper is genuine, `gradlew` is executable, metadata is consistent |
| `_build.yml` | All source sets compile, unit + architecture tests pass, jar is built (downloadable artifact) |
| `_datagen.yml` | Committed `src/main/generated` matches what the providers produce |
| `_gametest.yml` | The mod boots on a dedicated server and all game tests pass |

Shared setup (JDK 25 + Gradle cache) lives in `.github/actions/setup-gradle-env`.
