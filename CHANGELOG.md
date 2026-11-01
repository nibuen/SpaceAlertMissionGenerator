# Changelog

## 3.6.0 (unreleased)

- Updated for Android 17 (target SDK 37), with current AndroidX and Compose libraries
- The range sliders in Options now use Material's RangeSlider, replacing an unmaintained third-party library
- Removed storage permissions the app never needed. Notifications are now the only permission.
- About screen shows the version, what's new, and links to the source code, the issue tracker and the Play listing
- Build: AGP 9.5, Gradle 9.8, Kotlin 2.4; CI runs on JDK 21
- License is now GPL-3.0-or-later to match the generator code it is built on (it was labeled MIT before)

## 3.5.0

- Redesigned the About, Help and New Mission screens
- New Mission screen for picking a mission type and options
- Added German translations for the new screens
- Fixed crashes, leaks and deprecated API use found in code review
- CI moved from CircleCI to GitHub Actions
