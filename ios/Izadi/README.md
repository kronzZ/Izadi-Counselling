# Izadi — iOS

SwiftUI port of the Android practice app, backed by the same Firebase Auth + Firestore project.

## Open in Xcode

1. Complete the Firebase console steps in [`../../firebase/README.md`](../../firebase/README.md).
2. Replace `Izadi/GoogleService-Info.plist` with the file downloaded from Firebase (iOS app, bundle ID `com.practice.app`).
3. Open `Izadi.xcodeproj` in Xcode.
4. Wait for SPM to resolve **FirebaseAuth** and **FirebaseFirestore** (Firebase iOS SDK).
5. Select your Development Team under Signing & Capabilities.
6. Run on a device or simulator (iOS 17+).

## First launch

1. Create an account (or sign in with the same email you use on Android).
2. Clients, sessions, and the welcome SMS template sync live under `practices/{yourUid}/…`.

## Feature parity

| Feature | Status |
|---------|--------|
| Clients / sessions / payments | Yes |
| Welcome SMS (`MFMessageComposeViewController`) | Yes |
| Daily quote + mint theme + Outfit fonts | Yes |
| Cash wrap-up | Yes |
| Square tap | Configured via `SquareConfig.swift` — register iOS bundle ID in Square Dashboard; Point of Sale app required on device |

## Bundle ID

`com.practice.app` — keep this aligned with Firebase and Square.
