# Square Point of Sale (tap payments) — what you need to do

The iOS app is wired to open **Square Point of Sale**, take a card tap, then return and mark the session **Paid · Tapped** only on success.

## One-time: Square Developer Dashboard

Do this with the **business Square account** (your friend’s seller account):

1. Open [https://developer.squareup.com/apps](https://developer.squareup.com/apps)
2. Open the existing app that has Application ID  
   `sq0idp-urZsWABGSBP75wz3nbpMvw`  
   (or create a new app and put its Application ID into `ios/Izadi/Izadi/Services/SquareConfig.swift`)
3. Left menu → **Point of Sale API**
4. Under **iOS**, enter exactly:
   - **Bundle ID:** `com.turtletech.izadicounselling`
   - **URL Scheme:** `izadicounselling`
5. Click **Save**
6. **Credentials** tab → confirm the Application ID matches `SquareConfig.swift`

You do **not** need any code or ID from her phone for this step.

## On her iPhone

1. Install **Square Point of Sale** from the App Store (she already has this)
2. Sign into Square Point of Sale with the **same seller account** that owns the Developer app above
3. Install / run **Izadi Counselling**
4. Complete a session → **Tap with Square** → Square opens → take payment → Square returns to Izadi → session shows as tapped/paid

## Testing notes

- Use a **real iPhone** (simulator can’t talk to Square POS)
- Card testing in production: charge a small amount, then refund in Square if needed
- **Cash** still works without Square
- If Square isn’t installed, Izadi offers the App Store link

## Already configured in the Xcode project

- Callback URL scheme `izadicounselling` in `Info.plist`
- `LSApplicationQueriesSchemes` includes `square-commerce-v1`
- Square Point of Sale SDK (SPM)
- Launch + callback handling in `SquarePaymentCoordinator.swift`
