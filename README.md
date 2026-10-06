# ⛰️ Summit Saver — a micro-saving Android app

Save for something you want by spending a little less every day.
Your goal sits on the top of a mountain. Every day you stay under your daily limit,
the money you didn't spend is saved, your climber walks higher up the trail, and a tree
is planted on the mountain (a bit like the *Forest* focus app).

## How it works

1. **Pick a goal** — e.g. ✈️ *Trip to Japan* or 📷 *New camera* — and how much it costs.
2. **Enter your monthly income and fixed expenses** (rent, bills, subscriptions…).
3. The money that is left is **split evenly over the days of the month** → your *daily limit*.
4. **Log what you spend** with the orange *Add spending* button.
5. At midnight the day closes:
   - spent less than the limit → the difference is saved, the climber moves up and a 🌲 is planted;
   - spent more → the extra is taken from your savings and the climber slips back a little.
6. Reach the summit = you have saved enough for your goal 🎉

The faded climber on the trail shows where you'll be tonight if you stop spending now.
Camps at 25 %, 50 % and 75 % light up as you pass them.

> **Tip:** In *Settings → Load demo data* you can fill the last three weeks with example
> spending to see the mountain, trees and history in action straight away.

## The project

| Path | What's inside |
| --- | --- |
| `app/src/main/java/com/microsaving/app/logic/BudgetCalculator.kt` | Daily limit, savings, streak and "days to goal" maths |
| `app/src/main/java/com/microsaving/app/ui/components/Mountain.kt` | The animated mountain drawing |
| `app/src/main/java/com/microsaving/app/ui/screens/` | Setup, Climb (home), History and Settings screens |
| `app/src/main/java/com/microsaving/app/data/` | Data models and on-device storage |
| `app/src/test/` | Unit tests for the budget maths |

Built with Kotlin and Jetpack Compose. All data stays on the phone.

---

## 🧪 Testing it — step by step (no experience needed)

### Option A: Download a ready-made APK (easiest)

Every time code is pushed, GitHub builds the app automatically and publishes it.

1. Open the repository on GitHub and click **Releases** (right-hand side of the main page).
2. In the newest *Summit Saver build*, click **SummitSaver.apk** under *Assets* to download it.

### Try it in your browser (Appetize.io)

1. Download **SummitSaver.apk** as above.
2. Go to <https://appetize.io>, sign up for free and upload `SummitSaver.apk`
   (the `.apk` file itself, not a `.zip`).
3. Open the link it gives you and press **Play** — the app runs on a virtual phone in your browser.

To install it on your Android phone:

1. Send the `.apk` to your phone (Google Drive, e-mail to yourself, USB cable…).
2. Tap it on the phone. Android will ask to allow installing from this source — allow it.
3. Tap **Install**, then **Open**.

### Option B: Run it on a virtual phone on your computer (Android Studio)

1. Download and install **Android Studio**: <https://developer.android.com/studio>
   (Windows, macOS or Linux; just accept the defaults in the installer).
2. Get the code: on GitHub click **Code → Download ZIP** and unzip it
   (or in Android Studio: *File → New → Project from Version Control* and paste the repo URL).
3. In Android Studio choose **Open** and select the project folder (the one containing `settings.gradle.kts`).
4. Wait until the bottom status bar stops showing "Gradle sync" (the first time takes a few minutes
   because it downloads what it needs).
5. Create a virtual phone: **Tools → Device Manager → + (Create Virtual Device)** →
   pick e.g. *Pixel 8* → choose a system image (download the one that's recommended) → **Finish**.
6. Press the green **▶ Run** button at the top. The virtual phone opens and the app starts.

You can also plug in a real Android phone with a USB cable (enable *Developer options → USB debugging*
on the phone first) and choose it next to the ▶ button.

### Building the APK yourself in Android Studio

**Build → Build App Bundle(s) / APK(s) → Build APK(s)**. When it finishes, click *locate* in the
pop-up; the file is `app/build/outputs/apk/debug/app-debug.apk`.

From a terminal the same thing is:

```bash
./gradlew assembleDebug        # build the APK
./gradlew testDebugUnitTest    # run the unit tests
```

> Before publishing on Google Play you'll need to sign the app with your own key —
> Android Studio's **Build → Generate Signed App Bundle / APK** walks you through it.
