# DailyDots — How You Earn & How to Publish

## 1. The business model (simple, no server, no ads)

| | Free | Pro |
|---|---|---|
| Habits | 3 | Unlimited |
| History on card | 7 days | 30 days + best streak |
| Home-screen widget | Up to 3 habits | Up to 6 habits |
| Price | ₹0 | **Lifetime unlock** (main) + **Yearly** (optional) |

**Why this works for a habit app**
- Habit-tracker users dislike subscriptions, so the **lifetime unlock is the main product**. The yearly plan is a lower-price alternative for people who prefer it.
- The paywall appears at the moment of highest intent: when the user tries to add a **4th habit** after already using the app. That is where most conversions happen.
- No server and no ads means no running costs and almost no policy risk. All data stays on the phone.

**Suggested starting prices** (Google lets you set a price per country; start here, test, adjust):
- Lifetime: ₹349–₹499 (about $4.99 for US/EU)
- Yearly: ₹149 (about $1.99–$2.99)

## 2. Honest earning expectations

The $1.21 revenue per install figure in your note is mostly App Store data. Android users usually pay less, and India has a lower average spend than the US or Europe. Treat the numbers below as **illustrative scenarios, not promises**.

Assumptions: 3% of installs buy Pro, average payment $4 (mix of countries), Google fee 15%.

| Total installs | Buyers (3%) | Gross | After Google 15% |
|---|---|---|---|
| 2,000 | 60 | $240 | ~$204 |
| 20,000 | 600 | $2,400 | ~$2,040 |
| 100,000 | 3,000 | $12,000 | ~$10,200 |

Your income depends mainly on **how many installs you get**, so the work after launch is growth (section 6). A 1–2% conversion is also normal, so plan with a range.

## 3. How you get paid by Google

1. Create a **Google Play Developer account** (one-time registration fee, about $25). Complete identity verification.
2. In Play Console open **Setup → Payments profile** and add your bank account and tax details (India: PAN/GST details as applicable).
3. Customers pay through Google Play. Google keeps its service fee (currently 15% on the first $1M per year and on subscriptions; confirm on the Play Console fees page) and **pays the rest to your bank, monthly**, once earnings pass the minimum payout threshold.
4. Income tax and GST on this income is your responsibility. Speak to a CA once earnings start.

## 4. Publishing steps (so Google verifies and accepts it)

**A. Prepare the app**
1. Open the folder in **Android Studio** (latest stable). Let it sync.
2. Change the package name `com.yourname.dailydots` everywhere (Android Studio: Refactor → Rename on the package). Do this *before* upload; it cannot be changed later.
3. Replace the placeholder icon: File → New → Image Asset.
4. Build the release file: **Build → Generate Signed App Bundle (AAB)**. Create a keystore and **back it up safely** (but use Play App Signing when asked).
5. If Play Console asks for a newer `targetSdk`, raise it in `app/build.gradle.kts`.

**B. Play Console setup**
1. Create app → category **Health & Fitness**, free app.
2. **Store listing**: name, short and full description, 512×512 icon, feature graphic (1024×500), at least 4 screenshots (show the widget on a home screen).
3. **Privacy policy URL** (required). Free generators work; say plainly: "All data is stored on your device. We collect no personal data." Host it on a free page (GitHub Pages, Google Sites).
4. **Data safety form**: no data collected, no data shared. The app has no INTERNET permission.
5. **Content rating** questionnaire, **target audience** 13+ or 18+ (not a kids app, which avoids extra rules), **ads declaration**: no ads.
6. **Monetize → Products**:
   - In-app product, ID `lifetime_unlock`, managed (one-time) product, set price, activate.
   - Subscription, ID `premium_annual`, one yearly base plan, set price, activate.
   - These IDs must match `Products` in `BillingManager.kt` exactly.
7. **License testing**: add your Gmail under Setup → License testing and test both purchases for free before release.

**C. Testing track before production**
- New personal developer accounts must run a **closed test** with a minimum number of testers (currently about 12) for 14 days before applying for production. Confirm the current number in Play Console. Ask friends/family, or small communities, to join with their Gmail.
- Fix anything in the **pre-launch report** and Android vitals.

**D. Go to production** → submit for review (usually a few days).

## 5. Policy checklist (avoids rejection)

- Digital unlocks must use **Google Play Billing** (done in the app). Never add UPI/PayPal links for the unlock.
- Describe Pro honestly in the listing; don't claim features the app doesn't have. Don't use "#1" or "best" claims without proof.
- No medical claims. It is a habit tracker, not a health treatment tool.
- Show the real price on the paywall (the app reads it from Play, so it is always correct and localized).
- Keep a working privacy policy link live.
- Don't copy another app's name, icon or screenshots. Check that "DailyDots" is free to use as a name on Play Store before you finalize it.

## 6. Growth plan (this is what decides your income)

1. **Store listing optimization**: title with keywords, e.g. "DailyDots: Habit Tracker & Widget". Mention *widget, streak, offline, no ads* in the description.
2. **Screenshots**: lead with the home-screen widget; it is your difference from competitors.
3. **Launch** in India first, then add English-speaking countries; also consider Hindi/Marathi listing text for local search.
4. **Rating prompt**: ask for a review after a user's 7-day streak (add Play In-App Review later; ratings strongly affect ranking).
5. **Short videos** (Reels/Shorts) of the widget in use; posts in habit/productivity communities (follow each community's self-promotion rules).
6. **Track**: Play Console shows installs, conversion and revenue. Test prices (₹299 vs ₹399 vs ₹499) using Play's pricing experiments.
7. **Update regularly** with small features (more widget sizes, themes as Pro perks, reminders, backup/export). Regular updates help ranking.

## 7. Stability ("must not crash")

- Fully offline with a local Room database; no network calls except Google Play Billing.
- Purchase status is cached, so the app works without internet and billing errors never block normal use.
- Before release: test on at least a low-end phone and a recent phone, with Android 8 to the latest version; check dark mode, rotation, deleting/re-adding habits and widget behaviour after a phone restart.
- After release watch **Android vitals** (crash rate) and fix problems quickly.
- Back up the signing keystore and project. Losing them means you cannot update the app.

## 8. Next features that raise income (v1.1+)

- Daily reminder notifications (Pro or free, test which converts better)
- Extra themes and widget styles (Pro)
- CSV/JSON export and phone-to-phone backup
- Weekly summary screen
