# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app that generates random missions for the Space Alert board game. Package: `com.boarbeard`. Uses audio narration to guide players through timed mission events (threats, data transfers, phase changes). Supports both randomly generated and pre-constructed missions, including the "Double Action" variant.

## Build Commands

```bash
# Build (requires Android SDK)
./gradlew assembleDebug

# Build and deploy to connected phone via adb
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk

# Unit tests (JVM, no emulator needed - uses Robolectric)
./gradlew test

# Single test class
./gradlew testDebugUnitTest --tests "com.boarbeard.generator.beimax.TestMissionImplJvm"

# Lint (run before pushing — CI checks this)
./gradlew lint

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

After building UI changes, deploy to the user's phone for testing.

**Build targets:** compileSdk 37, minSdk 30, targetSdk 37, Java 11 bytecode. AGP 9.5 (built-in Kotlin, no `kotlin-android` plugin), Gradle 9.8, Kotlin 2.4, JDK 21.

**Release notes:** update `CHANGELOG.md` and the "What's new" strings (`about_whats_new_body`, English and German) together.

## Architecture

**Modules:** `app` only.

**Mission Generation Pipeline:**
1. `MissionType` enum defines all mission variants and acts as a factory — calls `MissionImpl.generateMission()` for random missions or returns pre-built `EventList` from `ConstructedMissions`
2. `MissionImpl` orchestrates generation using `ThreatsGenerator`, `DataOperationGenerator`, and `PhasesGenerator` to populate an `EventList`
3. `MissionPreferences` (built from `SharedPreferences`) controls difficulty, player count, threat levels, etc.

**Audio Playback:**
- `MediaPlayerMainMission` sequences audio files, updates the mission log, and syncs with `StopWatch`
- `EventListParser` (`EnglishParser` over the bundled `DefaultGrammar`) converts events into audio cue sequences. Custom voice packs were removed in 3.6.0; only English audio ships.
- `MediaPlayerSequence` (Java) is the base audio sequencing class

**UI:** Hybrid — XML layouts for activities with Jetpack Compose (`MissionCard`) embedded for the mission log display. `MissionActivity` is the main entry point.

**Language mix:** Kotlin and Java. Newer code is Kotlin; legacy generator core and parsers are Java.

## Key Conventions

- Logging via Timber (not `Log.*`)
- Coroutines for mission generation (`suspend` functions)
- `HandlerThread` for event parsing and timer operations
- Testing: JUnit 4 + Robolectric + Kotest assertions for unit tests; Espresso for instrumented tests
