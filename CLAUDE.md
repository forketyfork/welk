# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Behavioral Guidelines

- Always implement complete, working code - never write stub comments instead of actual implementation
- Verify that changes don't break the build by running `./gradlew :composeApp:build`
- Run desktop tests with `./gradlew :composeApp:desktopTest` before finalizing tasks
- Plan your approach first, then execute systematically
- Stay focused on the requested task - avoid scope creep or unrelated changes

## Project Overview

Welk is a Kotlin Multiplatform flashcard application targeting iOS and Desktop platforms.
Users can view and edit cards, manage decks, and mark cards as learned or not learned through
flipping and swiping gestures. The app uses Firebase Firestore for data storage.

Key features include card creation, editing, and deletion, with helpful feedback and intuitive
card creation workflows for empty decks.

## Build Commands

### Desktop

```bash
# Build the desktop application
./gradlew :composeApp:build

# Run desktop tests
./gradlew :composeApp:desktopTest
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

## Application Workflow

Main user flow:

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

### Guidelines
- Focus unit tests on critical business logic that could break or change
- Prefer integration tests over unit tests that simply mirror implementation details
- Avoid tests that just verify method calls without testing actual behavior

### Commands
- Run desktop tests: `./gradlew :composeApp:desktopTest`
- Always run tests before finalizing any task or feature

## Development Flow

### General Process
1. **Plan**: Understand the requirements and plan your approach
2. **Implement**: Write code following the established patterns
3. **Build**: Verify the application builds with `./gradlew :composeApp:build`
4. **Test**: Run desktop tests with `./gradlew :composeApp:desktopTest`
5. **Review**: Ensure code follows project conventions

### Code Organization
- Maximize code reuse between iOS and Desktop by placing shared logic in the `shared` module
- Use Kermit logging instead of println statements for better debugging
### UI Implementation
When adding new UI functionality:
1. **Domain Layer**: Implement the data model first
2. **ViewModel Layer**: Add necessary state handling  
3. **UI Layer**: Implement platform-specific UI components

### Best Practices
- Always provide confirmation dialogs for destructive actions (like delete)
- Delay database operations to minimize unnecessary writes

### iOS Development
When making changes to iOS code:
1. **Build shared module first**: `./gradlew :shared:build`
2. **Test Swift compilation**:
   ```bash
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
   -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' build
   ```
3. **Kotlin-Swift interop considerations**:
   - Use optional casting (`as?`) and null checks for Kotlin types
   - `Pair<String, String>` exposes `first` and `second` properties as `NSString?`
   - Handle type conversions carefully

## Library Management

### Dependency Management
- All dependencies must be defined in `gradle/libs.versions.toml`
- Follow this process when adding new libraries:
  1. Add library details to `libs.versions.toml` under the appropriate section
  2. Reference the library in build files using `libs.some.library` syntax
  3. Use `version.ref = "some-version"` format for version references

## Common Patterns

### Entity Creation
- Create entities as temporary in-memory objects
- Save to Firestore only when user explicitly confirms
- Clear form fields after cancellation or successful save

### Empty State Handling
- Display appropriate empty state UI when decks contain no cards
- Provide direct actions for content creation
- Update `hasCards` state flow to trigger proper UI changes

### User Authentication
- Show login screen for unauthenticated users
- Manage authentication state through `LoginViewModel`
- Provide logout functionality in the side panel
- Redirect to login screen after logout

### Error Handling
- Wrap repository operations in try-catch blocks
- Log errors using appropriate Kermit log levels
- Implement fallback behavior for failed operations
- Provide user-facing feedback for error states

### Kotlin Guidelines
- Use file-level annotations for Kotlin opt-ins when needed

### Concurrency
- Structure code using functional reactive programming principles
- Derive state from existing flows using Kotlin's flow operations rather than creating new flows
- Use Kotlin's `StateFlow` for reactive patterns - avoid Android-specific reactive features
- Never use `delay()` or `Thread.sleep()` to handle race conditions, even in tests
- Implement proper reactive patterns that respond to specific conditions

### Comments
- Write clear, accessible, and grammatically correct comments
- Use simple language and avoid complex terminology
- Focus on explaining "why" rather than "what" - don't duplicate what's already visible in code
- Comment complex logic, business rules, and non-obvious design decisions 