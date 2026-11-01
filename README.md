# Space Alert Mission Generator

[![Tests](https://github.com/nibuen/SpaceAlertMissionGenerator/actions/workflows/tests.yml/badge.svg)](https://github.com/nibuen/SpaceAlertMissionGenerator/actions/workflows/tests.yml)
[![Get it on Google Play](https://img.shields.io/badge/Google%20Play-com.boarbeard-414141?logo=googleplay)](https://play.google.com/store/apps/details?id=com.boarbeard)

An Android companion app for the [Space Alert](https://boardgamegeek.com/boardgame/38453/space-alert) board game. It generates missions and narrates them out loud (threats, incoming data, data transfers, phase changes) so the whole crew can focus on playing instead of running a soundtrack.

**Free and open source. No ads, no tracking, and no internet access.** The only permission it asks for is notifications, used for a play/pause control while a mission is running.

## Features

- **Random missions** with tunable length, threat difficulty, incoming data frequency and double threats
- **Every mission from the book**: test runs, simulations, advanced simulations, real missions, and the Double Action variants
- **Unconfirmed reports** resolved automatically for your player count, or left for you to handle with the cards
- **Compress time** for experienced crews who want less silence between events
- English and German narration

## Install

- [Google Play](https://play.google.com/store/apps/details?id=com.boarbeard)
- Signed APKs are attached to each [GitHub release](https://github.com/nibuen/SpaceAlertMissionGenerator/releases)

Requires Android 11 (API 30) or newer.

## Building

You need JDK 21 and the Android SDK (platform 37).

```bash
./gradlew assembleDebug        # build
./gradlew test                 # unit tests (Robolectric, no device needed)
./gradlew lint                 # lint, also run in CI
```

The random mission generator lives in `app/src/main/java/com/boarbeard/generator/beimax`. It is a port of Maximilian Kalus's [JSpaceAlertMissionGenerator](https://github.com/mkalus/JSpaceAlertMissionGenerator).

## Contributing

Bug reports and pull requests are welcome. Please [open an issue](https://github.com/nibuen/SpaceAlertMissionGenerator/issues) first for anything bigger than a small fix. CI runs lint and unit tests on every pull request. See [CHANGELOG.md](CHANGELOG.md) for release history.

## Credits

Designed and developed by Leif Norcott, with contributions from Thomas Arnold, Christoph König, Torbjörn Eklund, kuhrusty and Marcus Zuber. The random mission algorithm comes from Maximilian Kalus's Java generator.

Space Alert is designed by Vlaada Chvátil and published by Czech Games Edition. This project is a fan-made companion and is not affiliated with them.

More of my projects and dev write-ups are on [my blog](https://blog.iterary.com).

## License

The app code is under the [MIT License](LICENSE). Files under `generator/beimax` that are derived from JSpaceAlertMissionGenerator keep their original GPL-3.0 headers.
