# Jex TODO List

## Current Version: 1.0.1

## v2.0.0 Architectural Refactoring

### Phase 1: Fix Current Issues
- [ ] Fix `--java-package` option for `--generate-plugin`
  - Add `--java-package` option definition to arguments.yaml in Setup.java
  - Test: `jex --generate-plugin my-plugin --java-package com.example`

### Phase 2: Refactor to Minimal Core
- [x] Rename `--setup` to `--install` throughout codebase
  - ✓ Renamed Setup.java to Install.java
  - ✓ Updated App.java (changed --setup to --install, Setup.run() to Install.run())
  - ✓ Updated Install.java createDefaultArgumentsYaml() method
  - ✓ Updated ArgumentParser.java help text (user fixed this)
  - ✓ Updated jex.sh wrapper script error message
  - ✓ Updated jex.bat wrapper script error message
  - [ ] Update CLAUDE.md documentation
  - [ ] Update README.md

- [ ] Move built-in command definitions from YAML to code
  - Define Options directly in App.java (--install, --help, --list)
  - Remove `createDefaultArgumentsYaml()` from Setup.java
  - Remove arguments.yaml file generation entirely
  - Update ArgumentParser.java to only handle plugin-specific YAML parsing

### Phase 3: Extract Generator to Plugin
- [ ] Create generator-plugin as separate Maven project
  - Move PluginGenerator.java logic to new plugin project
  - Implement Plugin interface
  - Package as generator-plugin.jar

- [ ] Update installation to bundle generator plugin
  - Copy generator-plugin.jar to plugins directory during install
  - Auto-register in plugin.yaml during install
  - Document as bundled plugin in README

- [ ] Remove PluginGenerator.java from core
  - Delete PluginGenerator.java from core codebase
  - Remove --generate-plugin handling from App.java
  - Update tests if any

### Phase 4: Testing & Documentation
- [ ] Test all functionality
  - `jex --install` works correctly
  - `jex --help` displays correct information
  - `jex --list` shows bundled generator plugin
  - `jex generate my-plugin --package com.example` works
  - Plugin loading and execution works as before

- [ ] Update documentation
  - Update README.md with new architecture
  - Update CLAUDE.md (this file)
  - Add migration guide for v1.0.0 → v2.0.0

- [ ] Version and release
  - Update pom.xml to version 2.0.0
  - Create git tag v2.0.0
  - Update CHANGELOG if it exists

## In Progress
(None currently)

## Completed
(None yet - v2.0.0 work starting)

---

## Notes
- See CLAUDE.md for full architectural discussion and rationale
- Current codebase is at v1.0.0
- Target: v2.0.0 with minimal core and plugin-based architecture