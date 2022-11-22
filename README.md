# healthpass-android-app
- Devs try to use Debug build more than others, since others will report crashes to Firebase, which mean QA will have to do unnecessary work to log those crashes.

# Branching
Branches are used as follows:
- `open-source-dev` > where you can merge new features.
- feature/xxxx > this will be a feature specific branch, which starts from `open-source-dev` branch. The developer can work on it and merge it with `open-source-dev` once it is done.
- bugs/xxxx > every ticket will have Fix version, try to follow it.

# Environments
- Add it to the EnvironmentHandler class, make sure to add the correct function depending on if it is a Dev env or Prod one

# Build Types
- Currently Release builds will be pointing to `Production` APIs only.
- Debug build will use `usa_dev2` as default for developers, since it has auto deployment, while rest of builds pointing to `usa_dev1` environment, but all should allow switching to different environment.
- Beta should NOT go to production as it allows showing debug screen.
- QA build may have extra images for QR codes which is maintained by QA team.

# Github Code Owners file
- For now the file is split into 2 package, one for devs and one for QA team. You can modify it to assign default reviewers for Github if needed.
- This needs to be maintained manually.

# Firebase Distribution
1- Make sure to update the build version in gradle file.
2- Make sure that `releasenotes.txt` is updated correctly.
3- If you have uploaded before in same session, skip to step 2.
- if it is your first time to upload then type `./gradlew appDistributionLogin` in terminal, you will get a link, click it and login.
- write `export FIREBASE_TOKEN=refresh_token` in your terminal, while replacing `refresh_token` with the Token you just received from the previous command.
4- Use `WalletQA` or `VerifyQA` depending on what you want to upload
write `./gradlew assembleWalletQA appDistributionUploadWalletQA`

### Release Notes files
- we keep the release notes here for each build. App has 2 flavors so make sure to add in the correct file depending on your ticket/task

#### Follow link for more details For Google integration
- https://firebase.google.com/docs/app-distribution/android/distribute-gradle?authuser=2#google-acc-gradle use this link to follow on how to upload but.

## Strings in Flavors and how it works
- The order: Flavor is the top level > build types > main package, so an example is:
  Make sure you are changing same file in same `values-` package, if you have flavor String `app_name="flavor"`, it will be overridden by Debug `app_name="buildType"` then will be overridden by `main` package `app_name="main package"`

# Building aar files for our library modules
- Click on Gradle on top right of your screen in Android Studio
- navigate to the module name (example: QRCoder)
- open it, open Tasks > Build > double click `assemble`
  https://stackoverflow.com/a/36232327
