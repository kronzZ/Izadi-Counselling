# Izadi — iOS

SwiftUI practice app backed by Firebase Auth + Firestore.

## Open in Xcode

1. Complete [`../../firebase/README.md`](../../firebase/README.md).
2. Replace `Izadi/GoogleService-Info.plist` with the download from Firebase (bundle ID `com.practice.app`).
3. Open `Izadi.xcodeproj`.
4. Wait for SPM to resolve **FirebaseAuth** / **FirebaseFirestore**.
5. Set your Development Team → Run (iOS 17+).

## First launch

Create an account (or sign in). Clients, sessions, and the welcome SMS template sync under `practices/{yourUid}/…`.

## Bundle ID

`com.practice.app` — keep aligned with Firebase (and Square if you use tap-to-pay).
