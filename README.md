# welk

## About

welk is a flashcard application, currently in development. 

## Development prerequisites

For local development:
- the `GoogleService-Info.plist` file should be placed into the `iosApp/iosApp` directory.
- the `firebase.properties` file should be placed into the directory `shared/src/jvmMain/resources` with the following configuration:
  ```properties
  # Firebase configuration
  firebase.apiKey=
  firebase.authDomain=
  firebase.projectId=
  firebase.storageBucket=
  firebase.messagingSenderId=
  firebase.appId=
  ```

## TODO

### Current focus

- [ ] End-to-end tests for the main functionality
- [ ] Test instance of Firestore

### Targeted features

- [ ] Filesystem-like deck management, arbitrarily nested decks, joining and duplicating decks
- [ ] Ability to select multiple decks for a learning session
- [ ] Spaced repetition
- [ ] Search for duplicates across decks
- [ ] Easily highlight parts of the text in the flashcards
- [ ] Apps for iOS, iPadOS and macOS
- [ ] Search and insert images (both as links and as files)
- [ ] Generate definition and short example sentences
- [ ] Audio (TTS), transcription
- [ ] Spelling / writing modes
- [ ] Import from other similar tools (Anki, Mochi...)