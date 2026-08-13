# Izadi Counselling — agent handoff

Use this when working **locally in Cursor** on a Mac (not the cloud GitHub agent). Product owner: Andrew (designer). Tester / practitioner uses TestFlight.

## What this app is

iOS SwiftUI practice app: clients, bookings, wrap-up payments (cash + Square tap), welcome SMS, courtesy SMS reminders, Face ID lock.

- Xcode project: `ios/Izadi/Izadi.xcodeproj`
- Bundle ID: `com.turtletech.izadicounselling`
- Display name: Izadi Counselling
- Backend: Firebase Auth + Firestore (`practices/{uid}/…`)
- Branch we’ve been on: `cursor/ios-firebase-port-fb2d` (PR into `main`)

## Version / build (App Store Connect)

- **Version** = user-facing (`MARKETING_VERSION` / `CFBundleShortVersionString` via `$(MARKETING_VERSION)` in Info.plist).
- **Build** = every upload (`CURRENT_PROJECT_VERSION` / `$(CURRENT_PROJECT_VERSION)`). Must be **higher than any previously uploaded build**. Never reset to 1.
- Latest uploaded: **1.1 (6)**. Next TestFlight upload: keep **1.1**, bump **build to 7** (then 8, 9…). Do **not** bump build on every small local change — only when archiving for TestFlight.
- Identity in Xcode is the source of truth now. Do not hardcode version in Info.plist.

## Courtesy SMS (shipped)

Local notifications, not auto-send SMS.

- Start **2 calendar days before** session, **9am**, every **2 hours**, last ping **5pm**.
- Continues day-before and day-of until the **exact 3 hours before** slot (inclusive), then stop forever for that session.
- Late bookings: start from the **next valid slot** until cutoff.
- Banner: `Courtesy SMS due for {clientName}`.
- Home upcoming row: butter highlight + **!** (wrap-up takes priority if both apply).
- Sheet: **Open SMS** · **Mark as complete** · **Edit template**. Sending is optional; only **Mark as complete** stops alerts.
- Default template: `Hey {firstName}!, Confirming our appointment for {when}. Looking forward to seeing you!`
- `{when}`: today at … / tomorrow at … / Weekday at … based on when you action it.
- First name is always present — no “there” fallback needed.
- Editing session date/time clears `courtesySmsCompleted` so reminders reschedule.
- Manage Sessions `!` not built yet (home first).

## Session edit (shipped)

Scheduled sessions: **Edit** top-right on session detail → change date/time → **Save changes**. Duration not editable yet. Unsaved edits warn on back.

## Face ID / app lock

- Locks on launch and after background. Home stays **mounted** behind overlay so nav/forms survive.
- Skip Face ID once when returning from Square POS.
- Layout: lock overlay hides home (`opacity 0`) while locked; glow pinned full-width. If logo still jumps, check lock screen vs home hero separately.

## Known Mac gotchas

- Xcode rewrites `ios/Izadi/Izadi.xcodeproj/project.pbxproj` (`DEVELOPMENT_TEAM`). Before `git pull`, run:
  `git checkout -- ios/Izadi/Izadi.xcodeproj/project.pbxproj`
- Never overwrite local `ios/Izadi/Izadi/GoogleService-Info.plist` with the repo placeholder — real Firebase plist is required or Auth shows internal error.
- Signing team is set locally in Xcode; cloud copies won’t have Seane’s team.

## What is not built

- Chronological “what’s next” task list (attempted then fully reverted).
- Courtesy SMS on Manage Sessions.
- Session duration edit / full reschedule beyond date+time.

## Local workflow (no GitHub required)

1. Open the `Izadi-Counselling` folder in **Cursor Desktop**.
2. Use **local Agent** on that workspace.
3. Build/run/archive in **Xcode**.
4. Commit locally if you want history. Push to GitHub only for backup or sharing.

## Pull command (if still using git)

```bash
git checkout -- ios/Izadi/Izadi.xcodeproj/project.pbxproj
git pull origin cursor/ios-firebase-port-fb2d
```
