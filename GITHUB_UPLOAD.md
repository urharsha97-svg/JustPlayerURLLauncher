# GitHub upload

Upload the **contents of this ZIP** into the repository root. Do not upload this ZIP as a file inside the repository.

Required root files/directories:

- `.github/workflows/build.yml`
- `settings.gradle`
- `build.gradle`
- `gradle.properties`
- `tv/`
- `sender/`
- `relay/`

The project deliberately has no bootstrap/unpacking code in `settings.gradle`. The previous `projectDir` error came from that bootstrap code running while Gradle evaluated the settings script.

GitHub Actions uses Java 17, Gradle 8.7, and builds both debug APKs.
