# Firebase setup — Izadi (iOS only)

Cloud backend for the iOS app. Each counselor signs in with email/password; their data lives under their Auth UID.

## Quick path

1. Create Firebase project  
2. Enable Firestore + Email/Password auth  
3. Register the iOS app (`com.practice.app`)  
4. Drop in `GoogleService-Info.plist`  
5. Deploy security rules  
6. Run from Xcode and create your account  

Detailed steps below.

---

## 1. Create the project

1. Go to [Firebase Console](https://console.firebase.google.com/) → **Add project**.
2. Name it something like `izadi-counselling`.
3. Google Analytics is optional — you can turn it off.
4. Open the project when it’s ready.

## 2. Create Firestore

1. Left menu → **Build → Firestore Database → Create database**.
2. Choose **Start in production mode** (rules from this repo will lock it down).
3. Pick a region close to you (e.g. `australia-southeast1` for AU).
4. Confirm / enable.

You’ll see an empty database. That’s expected — the app creates documents when you sign up and add clients/sessions.

## 3. Enable Email/Password auth

1. **Build → Authentication → Get started**.
2. **Sign-in method** tab → **Email/Password** → Enable → Save.
3. Leave other providers off unless you want them later.

You can create the first user either:

- In the app (Create account on launch), or  
- In Console → **Authentication → Users → Add user** (then sign in with that email in the app).

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

6. In Xcode, confirm that file is in the **Izadi** target (Target Membership checked). It’s already wired in the project; you’re just overwriting the placeholder contents.

You do **not** need an Android app registered in Firebase.

## 5. Deploy rules and indexes

From a machine with [Firebase CLI](https://firebase.google.com/docs/cli) installed:

```bash
cd firebase
firebase login
firebase use --add          # select izadi-counselling (or your project)
firebase deploy --only firestore
```

That deploys:

- `firestore.rules` — only the signed-in user can read/write `practices/{theirUid}/…`
- `firestore.indexes.json` — composite indexes for session/client queries

**Without CLI:** paste `firestore.rules` into **Firestore → Rules → Publish**. Indexes will be suggested in the console the first time a query needs one — click the link in the error and create them.

## 6. Run the app

1. Open `ios/Izadi/Izadi.xcodeproj` in Xcode.
2. Wait for SPM to finish resolving Firebase.
3. Set your **Development Team** for signing.
4. Run on simulator or device.
5. **Create account** with your practice email + password (6+ characters).

On first sign-in the app creates:

```
practices/{yourUid}                    # settings + welcome SMS template
```

When you add data you’ll see:

```
practices/{yourUid}/clients/{clientId}
practices/{yourUid}/sessions/{sessionId}
```

## 7. Sanity-check in the console

1. Add a client in the app.
2. Firebase Console → **Firestore** → `practices` → your UID → `clients`.
3. You should see the document with firstName, surname, etc.
4. Book a session → check the `sessions` subcollection (`startsAt`, `status`, `paymentStatus`, …).

If the app fails with permission errors, rules weren’t deployed or you’re not signed in.

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

`uid` is always Firebase Auth’s user id. Rules enforce `request.auth.uid == practiceId`.

## Optional: Square later

Firestore does not take payments. Cash wrap-up is stored as session fields. For tap-to-pay, register bundle ID `com.practice.app` in the Square Developer Dashboard (same Application ID as in `SquareConfig.swift`).
