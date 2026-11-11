# EveryConfig

A client-only NeoForge 1.21.1 companion mod that embeds other mods' config
screens into a [Hellow](https://github.com/filowxy/hellow) page.

```json
{
  "type": "configEmbed",
  "libraries": ["cloth_config", "yet_another_config_lib_v3"],
  "height": 160
}
```

Drop that into `config/hellow/pages/*.json` and the page shows the first
installed config library's screen. With none installed it renders a
"not installed" placeholder instead of crashing.

## What it does

- **Cloth Config** and **YACL** adapters, plus an **REI** settings adapter.
- Config libraries are **soft dependencies**: EveryConfig loads fine without
  them, and each adapter checks the mod list at runtime.
- **No commands, no client tick handlers** — pages drive everything.
- Purely client side; nothing is bundled or auto-downloaded.

## Building

Requires JDK 21 and the Hellow jar in `libs/` (compiled against
`hellow-1.0.0.jar`, resolved at runtime as a NeoForge mod dependency).

```bash
./gradlew build
```

The jar lands in `build/libs/everyconfig-1.0.0.jar`.

## Requirements

- Minecraft **1.21.1**
- **NeoForge 21.1.x**
- **Hellow** (required)
- Java **21+**
