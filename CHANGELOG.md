# Changelog

All notable changes to this library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-03-17

### Added
- `diff()` function for comparing data class instances with recursive property traversal
- `DiffResult` with `hasChanges()`, `changedPaths()`, and human-readable `toString()`
- `Change` data class with dot-separated path notation for nested properties
- `DiffConfig` with `exclude()` for skipping fields during comparison
- `diffMaps()` for comparing maps with added, removed, and changed entry tracking
- `MapDiffResult` with categorized differences
