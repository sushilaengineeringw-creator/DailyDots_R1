# DailyDots — habit tracker with a home-screen widget

Minimalist, fully offline habit tracker for Android with streaks, XP levels and a one-tap home-screen widget. Free tier + Pro (lifetime unlock or yearly) through Google Play Billing. No server, no ads, no account.

**Stack:** Kotlin · Jetpack Compose (Material 3) · Room · Jetpack Glance (widget) · Play Billing 7

## Features
- Habits with daily check-ins, streaks, 7/30-day history
- Gamification: 10 XP per check-in, levels, streak milestones (🌱 3 · 🏅 7 · 🏆 30 days)
- One-tap starter habits for a frictionless first launch
- Home-screen widget: tap a row to tick a habit
- Free: 3 habits · Pro: unlimited habits, 30-day history, best streak, bigger widget
- Works offline; premium status cached locally

## Build and run
1. Install Android Studio (latest stable) and open this folder. Gradle sync takes a few minutes the first time.
2. Run on a device or emulator (Android 8.0 / API 26 or newer).
3. Long-press the home screen → Widgets → DailyDots.

Or push to GitHub: the **Android CI** workflow builds a debug APK on every push (Actions tab → latest run → Artifacts → `DailyDots-debug-apk`). Check that the first run is green; if it fails, the log shows the exact error.

## Before publishing to Google Play
- [ ] Rename package `com.yourname.dailydots` (cannot be changed after upload)
- [ ] Replace the placeholder icon (Android Studio → New → Image Asset)
- [ ] Create products `lifetime_unlock` (in-app) and `premium_annual` (subscription) in Play Console
- [ ] Add privacy policy URL and Data safety form (no data collected)
- [ ] Build a signed bundle (Build → Generate Signed App Bundle) and keep the keystore backed up; never commit it
- [ ] Run the closed test required for new developer accounts

Full walkthrough: [EARNING_AND_PUBLISHING_GUIDE.md](EARNING_AND_PUBLISHING_GUIDE.md) · Store text: [docs/STORE_LISTING.md](docs/STORE_LISTING.md) · Strategy: [docs/STRATEGY.md](docs/STRATEGY.md)

## Project layout
```
app/src/main/java/com/yourname/dailydots/
  Data.kt            Room entities, DAO, database
  HabitViewModel.kt  streaks, XP, add/toggle/delete
  MainActivity.kt    Compose UI, level card, paywall
  HabitWidget.kt     Glance home-screen widget
  BillingManager.kt  Play Billing + cached premium state
```

## Put it on GitHub
```bash
cd DailyDots
git init
git add .
git commit -m "Initial commit: DailyDots habit tracker"
git branch -M main
git remote add origin https://github.com/<your-username>/DailyDots.git
git push -u origin main
```

## License
MIT, see [LICENSE](LICENSE).
