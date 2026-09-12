# Stream Link — Separate Sender + TV Receiver

This repository contains two independent Android apps:

- **Stream Link Sender** — phone app, package `com.streamlink.sender`.
- **Stream Link** — Android TV receiver / Just Player launcher, package `com.justplayer.urllauncher`.

## Build

The included GitHub Actions workflow uses Java 17 and Gradle 8.7 and builds:

- `:tv:assembleDebug`
- `:sender:assembleDebug`

The generated artifacts are `Stream-Link-TV.apk` and `Stream-Link-Sender.apk`.

## Important

Upload the **contents of this archive** to the repository root. Do not upload the archive itself and do not keep another ZIP/bootstrap project nested inside the repository.

## Internet relay

The `relay/` directory contains the Cloudflare Durable Object worker used for Internet pairing and URL delivery.
