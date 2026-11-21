# PPSSPP-X v0.1.0-preview

First preview build of the PPSSPP-X Android fork.

## Highlights

- PPSSPP app branding changed to PPSSPP-X.
- Added a global touchscreen-button opacity slider in the touch layout editor.
- Added per-control opacity adjustment.
- Circle, Cross, Square, and Triangle face buttons can be positioned independently.
- Each face button has independent size and opacity settings.
- Existing shared face-button layouts are migrated to the independent layout on first initialization.
- Android APK GitHub Actions workflow fixed to remove an obsolete Gradle task.

## Build

- Variant: NormalOptimized
- Target branch: ppssppx-dev
- Commit: 93b4dc7e98bae6ee07f8ee0404d2c4278422ef97
- CI: GitHub Actions run 44887681507 (completed successfully)

## Known limitations

- Named control profiles and layout export/import are not included yet.
- This preview has passed the GitHub Actions build, but has not been independently tested on a physical Android device.
- This is an unofficial community fork of PPSSPP. It is not an official PPSSPP release.

## Installation

Download the Android APK from the GitHub Actions artifact named `android-NormalOptimized-build` for the build linked to this preview. Extract the artifact ZIP and install the APK on a compatible Android device.

## Credits

PPSSPP is an open-source PSP emulator. See the upstream project for its license and attribution requirements.
