# Firebase setup — Izadi (iOS only)

Cloud backend for the iOS app. One counselor uses the app — there is **no login screen**.
The app signs in anonymously in the background; practice data lives under that Auth UID in Firestore.

## Quick path

1. Create Firebase project  
2. Enable Firestore  
3. Enable **Anonymous** auth  
4. Register the iOS app (`com.practice.app`)  
5. Drop in `GoogleService-Info.plist`  
6. Deploy security rules  
7. Run from Xcode — opens straight to Home  

Detailed steps below.

---

## 1. Create the project

1. Go to [Firebase Console](https://console.firebase.google.com/) → **Add project**.
2. Name it something like `izadi-counselling`.
3. Google Analytics is optional — you can turn it off.
4. Open the project when it’s ready.

## 2. Create Firestore

1. Left menu → **Databases & Storage → Firestore** (or search “Firestore”).
2. **Create database**.
3. Choose **Start in production mode** (rules from this repo will lock it down).
4. Pick a region close to you (e.g. `australia-southeast1` for AU).
5. Confirm / enable.

You’ll see an empty database. That’s expected — the app creates documents when it first launches and when you add clients/sessions.

## 3. Enable Anonymous auth

1. **Build → Authentication → Get started** (or search “Authentication”).
2. **Sign-in method** tab → **Anonymous** → Enable → Save.
3. Leave Email/Password off — you don’t need it.

You do **not** create a user manually in the console.

## 4. Register the iOS app

1. Project overview (gear) → **Project settings → Your apps → Add app → iOS**.
2. **Apple bundle ID:** `com.practice.app`  
   (must match Xcode → target → Signing & Capabilities / General).
3. App nickname optional (e.g. `Izadi iOS`). App Store ID can stay blank for now.
4. Download **`GoogleService-Info.plist`**.
5. Replace the placeholder file in the repo:

   ```
   ios/Izadi/Izadi/GoogleService-Info.plist
   ```

6. In Xcode: left sidebar → **Izadi** (blue) → **Izadi** (yellow) → **`GoogleService-Info.plist`**. Confirm it’s the real download (not placeholder text).

You do **not** need an Android app registered in Firebase.

## 5. Deploy rules and indexes

From a machine with [Firebase CLI](https://firebase.google.com/docs/cli) installed:

```bash
cd firebase
firebase login
firebase use --add          # select your project
firebase deploy --only firestore
```

That deploys:

- `firestore.rules` — only the signed-in (anonymous) user can read/write `practices/{theirUid}/…`
- `firestore.indexes.json` — composite indexes for session/client queries

**Without CLI:** paste `firestore.rules` into **Firestore → Rules → Publish**. Indexes will be suggested in the console the first time a query needs one — click the link in the error and create them.

## 6. Run the app

1. Open `ios/Izadi/Izadi.xcodeproj` in Xcode.
2. Wait for SPM to finish resolving Firebase.
3. Set your **Development Team** for signing.
4. Run on simulator or device.
5. App should land on **Home** with no sign-in screen.

On first launch the app creates:

```
practices/{uid}                    # settings + welcome SMS template
```

When you add data you’ll see:

```
practices/{uid}/clients/{clientId}
practices/{uid}/sessions/{sessionId}
```

## 7. Sanity-check in the console

1. Add a client in the app.
2. Firebase Console → **Firestore** → `practices` → your UID → `clients`.
3. You should see the document.

If the app shows “Couldn’t start” mentioning Anonymous, go back to step 3 and enable Anonymous auth.

If you see permission errors, rules weren’t deployed (step 5).

## Data shape (reference)

```
practices/{uid}
  name, welcomeSmsTemplate, currency, estimatedFeeDollars, defaultFeeCents, updatedAt

practices/{uid}/clients/{clientId}
  firstName, surname, dateOfBirth, mobile,
  emergencyContactName, emergencyContactNumber,
  relationship, isActive, createdAtEpochMs

practices/{uid}/sessions/{sessionId}
  clientId, clientName, date, time, startsAt, endsAt,
  durationMinutes, notes, status, paymentStatus,
  paymentAmountCents, paymentMethod, updatedAt
```

`uid` is the anonymous Auth user id for that install. Rules enforce `request.auth.uid == practiceId`.

**Note:** Deleting the app (or resetting the simulator) creates a new anonymous user and an empty practice. For your friend’s real phone, avoid deleting the app if you care about keeping that cloud data linked.

## Optional: Square later

Firestore does not take payments. Cash wrap-up is stored as session fields. For tap-to-pay, register bundle ID `com.practice.app` in the Square Developer Dashboard (same Application ID as in `SquareConfig.swift`).
