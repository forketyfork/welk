# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Behavioral Guidelines

- Do not write stub comments instead of the actual implementation
- Always verify that your changes do not break the application build
- Plan first, then execute
- Keep all of your actions in line with the initial task, don't do anything you weren't asked to do

## Project Overview

Welk is a Kotlin Multiplatform project targeting iOS and Desktop platforms. It's a flashcard
application that allows users to view and edit cards, manage card decks, flip and swipe cards
(mark as learned or not learned). The app uses Firebase Firestore for storing card data.

Users can also create, edit, and delete cards. The application provides feedback when a deck has no
cards and offers a simple way to create new cards.

## Build Commands

### Desktop

```bash
# Build the desktop application
./gradlew :composeApp:build
```

### iOS

```bash
# Build the iOS application
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' build
```

## Project Structure

- `/shared` - Common code shared between all platforms
    - Contains domain models, view models, and repository interfaces
    - Platform-specific implementations are in respective source sets

- `/composeApp` - Compose Multiplatform UI code for desktop
    - Contains UI components, animations, and desktop-specific implementation

- `/iosApp` - iOS application code
    - Contains Swift code for iOS integration

## Key Architecture Components

1. **Domain Layer**
    - `Card` - Data class representing a flashcard with front and back text
    - `CardRepository` - Interface for fetching and updating card data
    - `FirestoreRepository` - Implementation that uses Firebase Firestore
    - `Deck` - Data class representing a collection of cards
    - `DeckRepository` - Interface for fetching and updating deck data

2. **ViewModel Layer**
    - `CardViewModel` - Interface for card-related operations including logout functionality
    - `SharedCardViewModel` - Shared implementation of card operations
    - `LoginViewModel` - Interface for login-related operations
    - `SharedLoginViewModel` - Shared implementation of login operations
    - `CardAnimationManager` - Interface for handling card animations
    - State flows for tracking UI state (editing, deletion confirmation, etc.)

3. **UI Layer**
    - Platform-specific implementations of the UI
    - Animation logic for card interactions
    - Card panel with edit/delete functionality
    - Side panel with deck listing and user actions
    - Confirmation dialogs for destructive actions
    - Bottom panel in the side panel with application-wide actions like logout

4. **Service Layer**
    - `AuthService` - Interface for authentication operations

## Development Prerequisites

1. **Firebase Configuration**
    - Place `GoogleService-Info.plist` in `iosApp/iosApp/` directory
    - Create `firebase.properties` file in `shared/src/jvmMain/resources/` with the following:
   ```properties
   # Firebase configuration
   firebase.apiKey=
   firebase.authDomain=
   firebase.projectId=
   firebase.storageBucket=
   firebase.messagingSenderId=
   firebase.appId=
   ```

## Workflow

The main workflow of the application:

1. Users authenticate through the login screen
2. After successful login, cards are fetched from Firestore
3. User can flip cards to see front/back
4. User can swipe right (learned) or left (not learned)
5. User can edit card content by tapping the edit button (pencil icon)
6. User can delete cards by tapping the delete button and confirming
7. User can create new cards when a deck is empty or by clicking "Add Card"
8. Card status and content are updated in Firestore
9. Next card is displayed
10. User can log out at any time using the logout button in the side panel

For new card creation:

1. A temporary card is created in memory (not in the database)
2. The card is only saved to Firestore when the user clicks "Save"
3. If the user clicks "Cancel", no database write occurs

## Dependencies

- Kotlin
- Compose Multiplatform
- Kotlinx Coroutines
- Ktor
- GitLiveApp firebase-kotlin-sdk
- SKIE for iOS interop
- Kermit for logging
- Koin for dependency injection

## Logging

The application uses Kermit for multiplatform logging. The Logger instance is available throughout
the codebase via:

```kotlin
private val logger = Logger.withTag("ClassName")
```

Available log levels:

- `logger.v { "Verbose message" }` - Verbose
- `logger.d { "Debug message" }` - Debug
- `logger.i { "Info message" }` - Info
- `logger.w { "Warning message" }` - Warning
- `logger.e(e) { "Error message" }` - Error (also accepts Exception as a parameter)

## Testing

- It is okay to write unit tests, however, make sure you're testing the important logic of the unit
  under test that may be changed or broken; otherwise, go with an integration test.
- Do not write tests that simply mirror the code under test, this is a bad pattern.

## Development Flow

- Try to reuse as much code as possible between the iOS and Desktop by placing it into the `shared`
  module.
- After implementing changes to the shared or desktop code, check that the application builds by
  executing `./gradlew :composeApp:build`
- Use proper logging with Kermit instead of println statements for better debugging.
- When adding new UI functionality:
    - First implement the data model in the domain layer
    - Then update the ViewModel layer with necessary state handling
    - Finally implement the UI components for each platform
- When implementing destructive actions (like delete), always provide confirmation dialogs
- Delay database operations as much as possible to avoid unnecessary writes
- When making changes to iOS code:
    - First build the shared module with `./gradlew :shared:build`
    - Test the Swift compilation by running
      `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' build`
    - When working with Kotlin-Swift interop, be careful with type conversions. Use optional casting
      with `as?` and null checks for Kotlin types exposed to Swift.
    - Remember that Kotlin types like `Pair<String, String>` are exposed to Swift with properties
      named `first` and `second`, which are `NSString?` type and need proper handling.

## Library Management

- All dependencies must be defined in `gradle/libs.versions.toml` file
- When adding new libraries:
    1. First add the library details to `libs.versions.toml` under the appropriate section
    2. Then reference the library in build files using the `libs.some.library` syntax
    3. For version references, use `version.ref = "some-version"` format

## Common Patterns

### Entity Creation

- Create new entity as a temporary object in memory
- Only save to Firestore when user explicitly saves
- Clear form fields when canceling or after successful save

### Empty State Handling

- When a deck has no cards, show appropriate empty state UI
- Provide direct actions for users to add content
- Update the hasCards state flow to properly trigger UI changes

### User Authentication

- Login screen is shown when the user is not authenticated
- Authentication state is managed by the LoginViewModel
- Logout functionality is accessible from the side panel
- After logout, users are redirected back to the login screen

### Error Handling

- Use try-catch blocks around repository operations
- Log errors using appropriate Kermit log levels
- Provide fallback behavior when operations fail
- Show appropriate UI feedback for error states

### Kotlin

- If you need to add any Kotlin opt-ins, use file-level annotations

### Concurrency
- Structure the code using the principles of functional reactive programming.
- Reuse existing flows to derive state using Kotlin's flow operations instead of creating new flows.
- Implement the reactive code using Kotlin's `StateFlow`, do not rely on the Android specific
  reactive features.
- Avoid calling `delay()` or `Thread.sleep()` to wait for obscure racy conditions, even in tests.
  Always implement proper reactive patterns to react on the exact condition.

### Comments

- Write useful, accessible and grammatically correct comments. 
- Avoid words or phrases that may be hard to comprehend.
- When writing comments, don't explain obvious things or duplicate what's already visible from the code.
  Try to focus on complicated parts or intentions that may not be clear from the code itself. 