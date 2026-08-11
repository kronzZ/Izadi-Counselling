# Firebase setup — Izadi Counselling

Shared backend for the **Android** and **iOS** apps. Data lives in Cloud Firestore under each counselor’s auth UID.

## 1. Create the Firebase project

1. Open [Firebase Console](https://console.firebase.google.com/) → **Add project** (name it e.g. `izadi-counselling`).
2. Disable Google Analytics if you don’t need it (optional).
3. In **Build → Firestore Database → Create database**:
   - Start in **production mode**
   - Pick a region close to you (e.g. `australia-southeast1`)
4. In **Build → Authentication → Sign-in method**, enable **Email/Password**.
5. (Optional) Create your counselor account under **Authentication → Users → Add user**.

## 2. Register the apps

### Android
1. **Project settings → Your apps → Add app → Android**
2. Android package name: `com.practice.app`
3. Download `google-services.json`
4. Place it at: `android/app/google-services.json`  
   (see `android/app/google-services.json.example`)

### iOS
1. **Project settings → Your apps → Add app → iOS**
2. Bundle ID: `com.practice.app` (must match Xcode)
3. Download `GoogleService-Info.plist`
4. Place it at: `ios/Izadi/Izadi/GoogleService-Info.plist`  
   (see `ios/Izadi/Izadi/GoogleService-Info.plist.example`)
5. In Xcode, confirm the plist is in the app target (Copy Bundle Resources).

## 3. Deploy rules & indexes

Install the [Firebase CLI](https://firebase.google.com/docs/cli), then from this `firebase/` folder:

```bash
firebase login
firebase use --add   # select your project
firebase deploy --only firestore
```

Or paste `firestore.rules` into **Firestore → Rules** and create the composite indexes when the console prompts you after the first queries.

## Data model

```
practices/{uid}                          # practice settings doc
  name: "Izadi Counselling"
  welcomeSmsTemplate: string
  currency: "AUD"
  estimatedFeeDollars: 130
  defaultFeeCents: 15000
  updatedAt: timestamp

practices/{uid}/clients/{clientId}
  firstName, surname, dateOfBirth, mobile
  emergencyContactName, emergencyContactNumber
  relationship: "Friend"|"Relative"|"Colleague"|"Other"|null
  isActive: bool
  createdAtEpochMs: number

practices/{uid}/sessions/{sessionId}
  clientId, clientName
  date: "yyyy-MM-dd"
  time: "HH:mm"
  startsAt: timestamp          # for sorting / queries
  endsAt: timestamp
  durationMinutes: number
  notes: string
  status: "Scheduled"|"Completed"|"Cancelled"
  paymentStatus: "Pending"|"Paid"
  paymentAmountCents: number|null
  paymentMethod: "Cash"|"Tapped"|null
  updatedAt: timestamp
```

`practiceId` is always the signed-in user’s Firebase Auth UID. Security rules enforce that.

## Square (payments)

Firebase does not process card payments. Keep using Square Point of Sale on device:

- Android: already wired (`SquareConfig.kt`)
- iOS: register the same Square Application ID + iOS bundle ID in the Square Developer Dashboard

## Local → cloud migration

Existing Android installs still have `clients.json` / `sessions.json` on device. On first successful Firebase sign-in, the Android app offers a one-time import of that local data into Firestore, then continues using the cloud as source of truth.
