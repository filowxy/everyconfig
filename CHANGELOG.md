# Changelog

All notable changes to EveryConfig are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] — Initial standalone release

### Added

- Client-only companion mod that embeds third-party config screens inside a
  Hellow element tree.
- `ConfigAdapter` / `ConfigAdapterRegistry` with lazy registration and
  `firstInstalledIn(...)` preference ordering.
- Adapters for **Cloth Config** and **YACL**, plus an **REI** settings adapter.
- `configEmbed` element factory for file-driven Hellow pages
  (`config/hellow/pages/*.json`):
  - `library` picks one library id,
  - `libraries` lists ids in preference order and uses the first installed one,
  - `height` sets the embed height,
  - when no supported library is installed the element renders a
    "not installed" placeholder instead of failing.
- Soft dependency gating: every adapter checks the mod list at runtime, so
  EveryConfig works with none of the supported config libraries present.
- No commands and no client events — the mod activates purely through pages.

### Notes

- Hellow is a required mod dependency, compiled against
  `libs/hellow-1.0.0.jar` and resolved at runtime through `neoforge.mods.toml`.
- Configured / FzzyConfig / ResourcefulConfig adapters are not implemented yet;
  the adapter structure is the same, so they can be added without changes to the
  registry.
