# PPSSPP-X (GameHub Edition)

## Project direction

PPSSPP-X is a focused Android-oriented fork of PPSSPP. The emulator core, rendering backends, input pipeline, and upstream compatibility must remain intact. The first milestone is to improve the touchscreen-control experience without replacing the existing emulator UI or input implementation.

## Current upstream capabilities verified

The current source already includes:
- A touchscreen layout editor with Move and Resize modes.
- A Customize screen for showing and hiding controls.
- Optional snap-to-grid positioning and configurable grid size.
- Separate portrait and landscape touch-control configurations.
- Per-game touch-control settings.
- A reset-to-default layout action.
- A global touch-button opacity setting.

These existing features should be reused rather than duplicated.

## Planned GameHub improvements

1. Named touch-control profiles with save, load, rename, and delete.
2. Export and import of control profiles.
3. A clear profile-management entry point in the touch-layout editor.
4. Per-game profile selection while retaining the existing per-game configuration behavior.
5. Android ARM64 build and device validation.

## Implementation constraints

- Preserve the upstream PSP emulation and rendering code.
- Keep existing configuration keys and defaults backward-compatible.
- Preserve portrait and landscape layouts independently.
- Avoid changing the default control layout for existing users.
- Validate configuration loading, saving, and migration before expanding the UI.
- Keep changes small enough to review and test independently.

## First implementation target

Implement named control profiles by reusing the existing `TouchControlConfig` data model and configuration serialization conventions. Profile storage must be separate from the active layout so saving or switching a profile cannot silently overwrite the user's only layout. The initial implementation should support both portrait and landscape data and should validate imported data before applying it.
