# Architecture

This mod uses a small "ports and adapters" (clean architecture) layout adapted to Fabric.
The goal: game rules are plain Java that can be tested in milliseconds, and Minecraft-specific
code is a thin layer around them that is easy to update when Minecraft changes.

Every package has a `package-info.java` explaining what belongs there. The dependency rules
below are enforced by [ArchitectureTest](../src/test/java/com/codefee/mcfunmod/architecture/ArchitectureTest.java),
so CI fails if they are broken.

## Layers

```
                 ┌────────────────────────────────────────────────────┐
  Minecraft  ──▶ │ event · command · network · mixin · content        │  entry points: Minecraft calls these
  / Fabric       └───────────────┬────────────────────────────────────┘
                                 │ call
                 ┌───────────────▼────────────────────────────────────┐
                 │ core.service  (use cases, game rules)              │  plain Java
                 │ core.model    (records, enums, value objects)      │  no Minecraft imports
                 │ core.port     (interfaces core needs)              │
                 └───────────────▲────────────────────────────────────┘
                                 │ implement
                 ┌───────────────┴────────────────────────────────────┐
                 │ adapter  (ports backed by Minecraft APIs)          │
                 └────────────────────────────────────────────────────┘

  registry: declares content (items, blocks, ...)      McFunMod: wires everything together at startup
```

| Package | Responsibility | May depend on |
|---|---|---|
| `core.model` | Immutable domain data | JDK only |
| `core.port` | Interfaces for what core needs from outside (players, world, config, time) | `core.model` |
| `core.service` | Game rules and use cases | `core.model`, `core.port` |
| `adapter` | Implement `core.port` using Minecraft/Fabric | `core`, Minecraft |
| `content` | `Block`/`Item`/`Entity`/`BlockEntity` subclasses; delegate decisions to services | `core`, `registry`, Minecraft |
| `registry` | All `Registry.register` calls, one class per registry type | `content`, Minecraft |
| `event` | Fabric event listeners; translate event, call a service | `core`, `adapter`, Minecraft |
| `command` | Brigadier commands; parse args, call a service, reply | `core`, `adapter`, Minecraft |
| `network` | Payload definitions (`network.payload`) and server receivers | `core`, `adapter`, Minecraft |
| `config` | Load/save `config/mcfunmod.json` | `core`, Minecraft |
| `mixin` | Last-resort hooks into Minecraft code; forward only | anything above |
| `util` | Stateless helpers with no game meaning | JDK, Minecraft |
| `McFunMod` | Composition root: create adapters, inject into services, call `init`s | everything |
| `client.*` | Rendering, screens, key bindings, client networking | common code, Minecraft client |

### Rules

1. **Dependencies point inward.** `core` imports nothing from Minecraft, Fabric, Mixin or the
   rest of the mod. Outer layers call core; core never calls them.
2. **Core talks to the world through ports.** Need the player's position in a rule? Add a method
   to a port interface, implement it in `adapter`, and pass the adapter into the service constructor.
3. **Only `adapter` implements ports** (tests may use fakes).
4. **Entry points stay thin.** Events, commands, payload receivers, content classes and mixins
   translate Minecraft input to core calls. If one grows beyond a few lines of glue, move the
   logic into a service.
5. **No one references a mixin class.** They do not exist as normal classes at runtime.
6. **Registries only declare.** No logic, no references to events, commands or networking.
7. **No package cycles** between top-level packages.
8. **Client code stays in `src/client`.** Common code cannot see it (separate source set), so a
   dedicated server never loads rendering classes.
9. **Wiring happens in one place.** Objects are created in `McFunMod` / `McFunModClient` and passed
   down through constructors or `init(...)` parameters. No static service locators.

## Source sets

| Folder | Runs in | Contains |
|---|---|---|
| `src/main` | Server and client | Everything the server needs: core, content, registries, events, commands, networking |
| `src/client` | Client only | Renderers, screens, key bindings, client packet receivers, client mixins |
| `src/datagen` | `./gradlew runDatagen` | Providers that generate recipes, loot tables, tags, models, lang into `src/main/generated` |
| `src/gametest` | `./gradlew runGameTest` | Tests that run inside a headless dedicated server |
| `src/test` | `./gradlew test` | Plain JUnit tests for `core` and the architecture rules |

## Where do I put...?

| I want to... | Files to touch |
|---|---|
| Add a game rule / calculation | `core/service/` (+ records in `core/model/`), unit test in `src/test/.../core/` |
| Let core read/change the world | Interface in `core/port/`, implementation in `adapter/`, wire in `McFunMod` |
| Add an item | Class in `content/item/` (if custom), constant in `registry/ModItems`, add to `registry/ModItemGroups`, model + recipe + lang in `src/datagen/`, texture in `assets/mcfunmod/textures/item/` |
| Add a block | Class in `content/block/`, constant in `registry/ModBlocks`, blockstate/model/loot/lang in `src/datagen/`, texture in `assets/mcfunmod/textures/block/` |
| Add a block with stored data | Above + `content/blockentity/` + `registry/ModBlockEntities` (+ renderer in `client/render/` if it draws something) |
| Add a mob / entity | `content/entity/`, `registry/ModEntities`, renderer + model in `client/render/` |
| Add a sound | `registry/ModSounds`, `assets/mcfunmod/sounds/` + `sounds.json` |
| React to something happening in game | Listener in `event/ModEvents` that calls a service |
| Add a `/command` | Class in `command/`, register in `command/ModCommands` |
| Send data between server and client | Payload record in `network/payload/`, server receiver in `network/ModNetworking`, client receiver in `client/network/ClientNetworking` |
| Add a GUI screen | `client/screen/` (send actions to the server via a payload) |
| Add a key binding | `client/keybind/ModKeyBindings` |
| Add a server config option | `config/ModConfig` |
| Change Minecraft behavior with no Fabric event for it | Mixin in `mixin/` (or `client/mixin/`), listed in `mcfunmod.mixins.json` / `mcfunmod.client.mixins.json` |
| Test behavior that needs the real game | `src/gametest/`, register the class in `src/gametest/resources/fabric.mod.json` |

## Testing strategy

- **Unit tests** (`src/test`): fast, no Minecraft. Cover `core` thoroughly using fake ports.
- **Architecture tests** (`src/test/.../architecture`): enforce the rules above.
- **Game tests** (`src/gametest`): a few end-to-end checks per feature in a real server
  (blocks placed, commands executed, mixins applied).
- **Metadata check** (`.github/scripts/check_metadata.py`): versions, entrypoints and mixin
  configs are consistent.

## CI

`.github/workflows/ci.yml` runs on every push to `main` and every pull request:

| Workflow | What it proves |
|---|---|
| `_validate.yml` | Gradle wrapper is genuine, `gradlew` is executable, metadata is consistent |
| `_build.yml` | All source sets compile, unit + architecture tests pass, jar is built (downloadable artifact) |
| `_datagen.yml` | Committed `src/main/generated` matches what the providers produce |
| `_gametest.yml` | The mod boots on a dedicated server and all game tests pass |

Shared setup (JDK 25 + Gradle cache) lives in `.github/actions/setup-gradle-env`.
