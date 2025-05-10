# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Welk is a Kotlin Multiplatform project targeting iOS and Desktop platforms. It's a flashcard application that allows users to view cards, flip them, and swipe right (mark as learned) or left (mark as not learned). The app uses Firebase Firestore for storing card data.

## Build and Run Commands

### Desktop

```bash
# Build the desktop application
./gradlew :composeApp:build

# Run the desktop application
./gradlew :composeApp:run
```

### iOS

```bash
# Build the iOS application
./gradlew :shared:assembleXcframework

# Open the Xcode project and run from there
open iosApp/iosApp.xcodeproj
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

2. **ViewModel Layer**
   - `CardViewModel` - Interface for card-related operations 
   - `CommonCardViewModel` - Shared implementation of card operations
   - `CardAnimationManager` - Interface for handling card animations

3. **UI Layer**
   - Platform-specific implementations of the UI
   - Animation logic for card interactions

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
1. Cards are fetched from Firestore
2. User can flip cards to see front/back
3. User can swipe right (learned) or left (not learned)
4. Card status is updated in Firestore
5. Next card is displayed

## Dependencies

- Kotlin Multiplatform (2.1.20)
- Compose Multiplatform (1.8.0)
- Kotlinx Coroutines (1.10.2)
- Ktor (3.1.3)
- Firebase Kotlin SDK (2.1.0)
- SKIE for iOS interop (0.10.2-preview.2.1.20)