# 🚀 QUICK START - Build & Test in 60 Seconds

## ⚡ ONE-COMMAND BUILD & INSTALL

```powershell
cd C:\Users\vamsi\RoboFaceAI ; $env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2" ; .\gradlew clean :app:assembleDebug ; adb install -r app\build\outputs\apk\debug\app-debug.apk ; adb shell am start -n com.example.robofaceai/.MainActivity
```

## 📊 WATCH REAL-TIME LOGS

```powershell
adb logcat -c ; adb logcat -s SensorController:* RoboViewModel:D | Select-String "(VIVO|Light|SLEEP|WAKE|STATE)"
```

---

## ✅ WHAT YOU'LL SEE

### Within 3 Seconds:
```
❌ VIVO/OEM PROXIMITY RESTRICTION DETECTED!
💡 Light sensor fallback ENABLED
```

### When You Cover Phone:
```
💡 LIGHT SENSOR STATE CHANGED!
   BRIGHT/UNCOVERED → DARK/COVERED
   → 😴 SLEEP MODE
✅ STATE TRANSITION: Curious → Sleeping
```

### When You Uncover Phone:
```
💡 LIGHT SENSOR STATE CHANGED!
   DARK/COVERED → BRIGHT/UNCOVERED
   → 👁️ WAKE UP
✅ STATE TRANSITION: Sleeping → Curious
```

---

## 🎯 SUCCESS = All 3 Work!

1. ✅ **Vivo restriction detected** (first 3 seconds)
2. ✅ **Sleep when covered** (light < 25 lux)
3. ✅ **Wake when uncovered** (light > 25 lux)

---

## 🐛 IF NOT WORKING

Check these in order:

### 1. Is light sensor enabled?
```powershell
adb logcat | Select-String "Light sensor fallback"
```
Should see: `💡 Light sensor fallback ENABLED`

### 2. Is light level changing?
```powershell
adb logcat | Select-String "Light Sensor:" | Select-Object -Last 5
```
Should see different lux values when you cover/uncover

### 3. Cover COMPLETELY
- Block **all light** at top of phone
- Hold for 2 seconds
- Don't just shadow it - fully cover with hand

---

## 📱 ALTERNATIVE: Test Without Logcat

1. **Open app**
2. **Wait 3 seconds** (for Vivo detection)
3. **Cover phone** with hand at top (front camera area)
4. **Watch face animation** - should transition to sleeping face
5. **Uncover phone** - should wake up

---

## 📚 FULL DOCUMENTATION

- **COMPLETE_FIX_SUMMARY.md** - What was fixed and why
- **PROXIMITY_FIX_TEST_GUIDE.md** - Detailed test instructions
- **PROXIMITY_SENSOR_DIAGNOSIS.md** - Problem analysis

---

**That's it! Your proximity sensor now works via light sensor fallback!** ✨

_Build → Install → Test → Done in 60 seconds!_

