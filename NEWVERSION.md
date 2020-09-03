# How to release a new version

Currently I only know how to do it by hand via the Play console

1. Update the CHANGELOG.txt file
2. In Android Studio, run Build -> Generate Signed Bundle
3. Upload the .aab file to the Play Console, in the Release Management tab
4. Fill inn the "What's new" field by hand.

# How to update the play store listings

1. Update the images and descriptions in app/src/main/play/listings
2. Run ./gradlew publish

# GPP Documentation

https://github.com/Triple-T/gradle-play-publisher

------------

TODO: How to release a new version with GPP
This doesn't seem to work at the moment. We need to figure out how to add a signingConfig

See:
- https://developer.android.com/studio/publish/app-signing#gradle-sign
- https://github.com/Triple-T/gradle-play-publisher/issues/803

1. In build.gradle, update versionCode and versionName
2. Write the release notes in app/src/main/play/release-notes
3. Run ./gradlew publishBundle

