"""Static checks of mod metadata. Needs only the Python 3 standard library.

Runs in CI (_validate.yml) and locally:  python .github/scripts/check_metadata.py

Checks:
  1. Every JSON file under src/ parses.
  2. Versions in gradle.properties and fabric.mod.json agree.
  3. Mod id is the same in fabric.mod.json, McFunMod.MOD_ID and the assets folder.
  4. Every entrypoint class, mixin config and mixin class referenced from metadata exists.
  5. Every mixin source file is listed in a mixin config (an unlisted mixin silently does nothing).
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "src"
MOD_JSON = SRC / "main/resources/fabric.mod.json"
MAIN_CLASS = SRC / "main/java/com/codefee/mcfunmod/McFunMod.java"

errors: list[str] = []


def fail(path: Path, message: str) -> None:
    rel = path.relative_to(ROOT).as_posix()
    errors.append(f"::error file={rel}::{message}")


def read_properties(path: Path) -> dict[str, str]:
    props = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return props


def source_exists(class_name: str) -> bool:
    relative = class_name.replace(".", "/") + ".java"
    return any(SRC.glob(f"*/java/{relative}"))


def load_json_files() -> dict[Path, object]:
    parsed = {}
    for path in sorted(SRC.rglob("*.json")):
        if ".cache" in path.parts:
            continue
        try:
            parsed[path] = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            fail(path, f"invalid JSON: {e}")
    return parsed


def check_versions(mod: dict, props: dict[str, str]) -> None:
    mc = props.get("minecraft_version", "")
    loader = props.get("loader_version", "")
    fabric_api = props.get("fabric_api_version", "")
    depends = mod.get("depends", {})

    if depends.get("minecraft") != f"~{mc}":
        fail(MOD_JSON, f"depends.minecraft is '{depends.get('minecraft')}' but minecraft_version is '{mc}' (expected '~{mc}')")
    if depends.get("fabricloader") != f">={loader}":
        fail(MOD_JSON, f"depends.fabricloader is '{depends.get('fabricloader')}' but loader_version is '{loader}' (expected '>={loader}')")
    if not fabric_api.endswith(f"+{mc}"):
        fail(ROOT / "gradle.properties", f"fabric_api_version '{fabric_api}' is not built for minecraft_version '{mc}'")


def check_mod_id(mod: dict) -> None:
    mod_id = mod.get("id", "")
    if f'MOD_ID = "{mod_id}"' not in MAIN_CLASS.read_text(encoding="utf-8"):
        fail(MAIN_CLASS, f"MOD_ID does not match fabric.mod.json id '{mod_id}'")
    if not (SRC / f"main/resources/assets/{mod_id}").is_dir():
        fail(MOD_JSON, f"assets folder src/main/resources/assets/{mod_id} is missing")


def check_references(parsed: dict[Path, object]) -> None:
    listed_mixins: set[str] = set()

    for mod_json in sorted(SRC.glob("*/resources/fabric.mod.json")):
        mod = parsed.get(mod_json, {})

        for entries in mod.get("entrypoints", {}).values():
            for entry in entries:
                target = entry if isinstance(entry, str) else entry.get("value", "")
                class_name = target.split("::")[0]
                if not source_exists(class_name):
                    fail(mod_json, f"entrypoint class '{class_name}' not found")

        for config in mod.get("mixins", []):
            config_name = config if isinstance(config, str) else config.get("config", "")
            config_files = list(SRC.glob(f"*/resources/{config_name}"))
            if not config_files:
                fail(mod_json, f"mixin config '{config_name}' not found")
                continue
            mixin_config = parsed.get(config_files[0], {})
            package = mixin_config.get("package", "")
            for side in ("mixins", "client", "server"):
                for mixin in mixin_config.get(side, []):
                    listed_mixins.add(f"{package}.{mixin}")
                    if not source_exists(f"{package}.{mixin}"):
                        fail(config_files[0], f"mixin '{package}.{mixin}' ({side}) not found")

    for path in sorted(SRC.glob("*/java/**/mixin/**/*.java")):
        if path.name == "package-info.java":
            continue
        java_root = next(p for p in path.parents if p.name == "java")
        class_name = path.relative_to(java_root).with_suffix("").as_posix().replace("/", ".")
        if class_name not in listed_mixins:
            fail(path, f"mixin '{class_name}' is not listed in any *.mixins.json")


def main() -> int:
    parsed = load_json_files()
    if errors:
        print("\n".join(errors))
        print("Fix JSON syntax first.")
        return 1

    props = read_properties(ROOT / "gradle.properties")
    mod = parsed[MOD_JSON]
    check_versions(mod, props)
    check_mod_id(mod)
    check_references(parsed)

    if errors:
        print("\n".join(errors))
        print(f"Metadata check failed with {len(errors)} error(s).")
        return 1

    print(
        "Metadata OK (minecraft {minecraft_version}, loader {loader_version}, fabric-api {fabric_api_version}).".format(**props)
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
