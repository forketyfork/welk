# Implementation Plan for Welk

## Overview
This plan outlines remaining features and improvements for the Welk flashcard application based on the current project state and the Roadmap in `README.md`.

The repository already supports authentication, dependency injection, dark mode, and basic deck and card management. Desktop UI and basic ViewModels are implemented, while iOS support is still limited. Multiple integration tests exist but fail on a clean environment because they require a configured Firebase backend.

The plan is divided into immediate tasks from the "Current focus" section and longer‑term tasks from the "Targeted features" section.

## Current Focus
1. [x] **End‑to‑End Tests for Main Functionality**
   - Extend existing Compose UI tests to cover login, card flipping, deck creation, card editing, and deletion.
   - Configure GitHub Actions to run tests against a mocked Firestore or use a local emulator so CI does not depend on external accounts.
   - Stabilize test data utilities so they create and clean up decks and cards reliably.

2. **Proper Coroutine Dispatchers for Firestore Access**
   - Inject `CoroutineDispatcher`s into `FirestoreRepository` so database calls run on `Dispatchers.IO`.
   - Provide default dispatchers via Koin modules and expose them for tests using `Unconfined` or `StandardTestDispatcher`.
   - Audit ViewModel scopes to ensure Firestore calls never use `Dispatchers.Main` directly.

3. **Catch Up iOS Functionality**
   - Implement missing features in the SwiftUI module to match the desktop UI (deck management, card editing, login screen, logout, theme switch if applicable).
   - Use the shared ViewModels through SKIE and observe their flows on the main thread with `@MainActor`.
   - Add basic UI tests using Xcode’s UITest framework when feasible.

4. **Break Down the Main ViewModel**
   - `SharedCardViewModel` currently handles deck selection, card editing, animation, and session logic.
   - Extract smaller classes: e.g., `DeckManager`, `CardEditor`, `SessionTracker`. Inject these into the main ViewModel to reduce size and improve testability.
   - Provide dedicated unit tests for each new class.

5. **Add `@Preview` Composables**
   - For each desktop Compose component (e.g., `CardPanel`, `SidePanel`, `DeckItem`), create preview functions annotated with `@Preview` showing light and dark themes.
   - Include sample state using fake ViewModels or preview data objects.

6. **Better Kermit Log Formatting**
   - Configure a custom `Logger` instance with formatters that include timestamps and log levels.
   - Provide a Koin module so each platform can customize output (e.g., colored logs on desktop, OSLog on iOS).

7. **Rewrite Database Interaction on Flows**
   - Replace callbacks and manual updates with Flow-based APIs from Firebase where possible.
   - Update repository functions to expose `Flow<List<Card>>` and `Flow<Deck>` for real-time updates.
   - Adapt ViewModels to collect these flows, ensuring they cancel appropriately when a session ends.

## Targeted Features
1. **Additional Authentication Methods**
   - Integrate Google and Apple sign-in using the firebase-kotlin-sdk. Expose sign‑in flows in `AuthService` and handle credential linking with existing accounts.

2. **Filesystem‑like Deck Management**
   - Implement a tree data structure for decks with unlimited nesting.
   - Support moving, joining, and duplicating decks. Update Firestore schema to store parent references and maintain child order.
   - Provide UI controls for expanding/collapsing nested decks on both platforms.

3. **Multiple Deck Selection for Sessions**
   - Allow users to select several decks before starting a learning session. The ViewModel should merge card streams from all chosen decks and maintain progress per deck.

4. **Spaced Repetition Scheduling**
   - Store review timestamps and success metrics for each card.
   - Implement an algorithm such as SM‑2 to schedule next review dates.
   - Update the UI to show due cards and allow the user to start review sessions based on schedule.

5. **Duplicate Search Across Decks**
   - Provide a utility that scans all cards for identical or similar text.
   - Surface potential duplicates in the UI so users can merge or delete them.

6. **Highlighting Text in Flashcards**
   - Extend the card data model to include markup (e.g., Markdown or simple spans).
   - Update rendering components on both platforms to display highlights.
   - Offer an editor with basic formatting actions for bold, italic, and color highlights.

7. **Native Apps for iOS, iPadOS, and macOS**
   - Configure compose multiplatform targets for iOS and macOS.
   - Ensure window sizes and input methods work well on each device type.
   - Prepare deployment pipelines for TestFlight and the Mac App Store.

8. **Image Search and Insertion**
   - Integrate an image search API (e.g., Unsplash) and allow users to attach images by URL or file upload.
   - Store image references in Firestore and cache files locally.

9. **Automated Definition and Example Generation**
   - Use a dictionary API to fetch word definitions and example sentences during card creation.
   - Provide a button in the editor to populate these fields automatically.

10. **Audio (TTS) and Transcription**
   - Add text‑to‑speech playback for card content using platform TTS engines.
   - Support attaching recorded audio and display transcriptions using a speech‑to‑text library.

11. **Spelling / Writing Practice Modes**
   - Implement alternative card types requiring typed input instead of flipping.
   - Track correctness and integrate with spaced repetition scheduling.

12. **Import from Other Tools**
   - Parse export formats from Anki and Mochi to create decks and cards.
   - Provide an import wizard with field mapping and progress feedback.

13. **Word Connections**
   - Allow linking cards that represent related words or phrases.
   - Display these connections in the card view and offer quick navigation between related cards.

## Development Notes
- Continue to keep shared logic in the `shared` module to maximize reuse.
- Always run `./gradlew :composeApp:build` and `./gradlew :composeApp:desktopTest` before committing changes.
- Document new public APIs and update tests as features evolve.

