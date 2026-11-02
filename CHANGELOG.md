# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changes documented here do not include those from the original repository.

## [Unreleased]

- Chore: Update dependencies and build tooling (Gradle 8.14.3, AGP 8.13.0, Kotlin 1.9.25, compileSdk/targetSdk 36, androidx libraries, Room 2.6.1, WorkManager 2.10.5, play-services-location 21.3.0, gson, coroutines 1.9.0; versions capped to stay compatible with compileSdk 35 / AGP 8.7 consumers, Health Connect stays on 1.1.0-beta01).

## [1.0.1]

- Revert the Kotlin package/namespace from `io.ionic.libs.ionhealthfitnesslib` back to `com.outsystems.plugins.healthfitness` to preserve delivery of background jobs/alarms already scheduled by an app before it updates. `AlarmManagerHelper`/`ActivityTransitionReceiver` build explicit `Intent`/`PendingIntent` references that bake in the fully-qualified class name at schedule time, so a package rename would break redelivery for anything scheduled under the old package.

## [1.0.0]

- Initial release of IONHealthFitnessLib for Android under ionic-team, ported from the OutSystems Health & Fitness plugin's native Android library.
