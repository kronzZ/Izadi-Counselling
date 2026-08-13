# Izadi Counselling (iOS)

Practice manager for **Izadi Counselling** — clients, bookings, wrap-up payments, and welcome SMS.

The iOS app is the product. Backend is **Firebase Auth + Cloud Firestore**.

| | Path |
|--|------|
| iOS (Xcode) | [`ios/Izadi/`](ios/Izadi/) |
| Firebase rules & indexes | [`firebase/`](firebase/) |

> The `android/` folder is leftover from early prototyping and is not part of the product path.

## Setup

1. Follow **[`firebase/README.md`](firebase/README.md)** end-to-end (project → Firestore → Auth → iOS app → deploy rules).
2. Replace `ios/Izadi/Izadi/GoogleService-Info.plist` with the file from the Firebase console.
3. Open `ios/Izadi/Izadi.xcodeproj` → set signing team → run.
4. Create your account in the app.

## Sync model

Your practice data is keyed to your Auth UID:

```
practices/{uid}/clients/…
practices/{uid}/sessions/…
```

Sign in on another iPhone/iPad with the same email and you get the same live data.
