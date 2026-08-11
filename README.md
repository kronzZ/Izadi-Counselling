# Izadi Counselling

Practice manager for **Izadi Counselling** — clients, bookings, wrap-up payments, and welcome SMS.

| Platform | Path | Storage |
|----------|------|---------|
| Android | [`android/`](android/) | Firebase Auth + Firestore (with one-time import from old on-device JSON) |
| iOS | [`ios/Izadi/`](ios/Izadi/) | Same Firebase project |
| Backend | [`firebase/`](firebase/) | Security rules + indexes |

## What you need to do once (Firebase + Xcode)

### 1. Firebase project
Follow [`firebase/README.md`](firebase/README.md):

1. Create the project, enable **Firestore** and **Email/Password** auth.
2. Register Android (`com.practice.app`) and iOS (`com.practice.app`).
3. Download configs and **replace the placeholders**:
   - `android/app/google-services.json`
   - `ios/Izadi/Izadi/GoogleService-Info.plist`
4. Deploy rules: `cd firebase && firebase deploy --only firestore`

### 2. iOS in Xcode
1. Open `ios/Izadi/Izadi.xcodeproj`
2. Let SPM fetch Firebase
3. Set your signing team
4. Run

### 3. Android
1. Sync Gradle with the real `google-services.json`
2. Run on device/emulator
3. Sign in with the **same** email as iOS
4. If the phone still has old local data, accept the **Import to cloud** prompt

## Shared data model

Everything for a counselor lives under their Auth UID:

```
practices/{uid}
practices/{uid}/clients/{clientId}
practices/{uid}/sessions/{sessionId}
```

Both apps listen in real time — book on iPhone, see it on Android (and the reverse).

## Square payments

Cash wrap-up works offline. Tap-to-pay still uses Square Point of Sale on device (same Application ID as before). Register the iOS bundle ID in the Square Developer Dashboard alongside the Android package.
