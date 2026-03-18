# Changelog

All notable changes to this library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0] - 2026-03-18

### Added
- Element-level list diffing: individual added, removed, and changed items reported with indices
- Set diffing: added and removed elements tracked individually
- `ChangeType` enum (`CHANGED`, `ADDED`, `REMOVED`) on `Change` data class
- `DiffSummary` data class with `added`, `removed`, `changed`, and `total` counts
- `summary()` extension function on `List<Change>` and `DiffResult`
- `toPatchMap()` on `DiffResult` to extract a map of new values from changes
- Custom comparator support via `DiffConfig.comparator(path, comparator)`
- Wildcard pattern exclusion via `exclude("*.metadata")` style patterns

## [0.1.1] - 2026-03-18

- Fix CI badge and gradlew permissions

## [0.1.0] - 2026-03-17

### Added
- `diff()` function for comparing data class instances with recursive property traversal
- `DiffResult` with `hasChanges()`, `changedPaths()`, and human-readable `toString()`
- `Change` data class with dot-separated path notation for nested properties
- `DiffConfig` with `exclude()` for skipping fields during comparison
- `diffMaps()` for comparing maps with added, removed, and changed entry tracking
- `MapDiffResult` with categorized differences
