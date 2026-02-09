# 🧪 PROXIMITY SENSOR TEST PROCEDURE

## ⚡ Quick Test (30 seconds)

### Step 1: Launch App
```
✅ Open RoboFaceAI on your Vivo device
```

### Step 2: Watch Logcat
```bash
# Via Android Studio: View → Tool Windows → Logcat
# Filter: "Proximity|SensorController"
```

### Step 3: Test Covering Sensor

**COVER the top of your phone (near front camera)**

**Expected Logs**:
```log
📡 Proximity: distance=0.0cm, shouldBeNear=true, currentState=FAR
👋 PROXIMITY STATE CHANGED: FAR → NEAR
🔵 EVENT SENT: ProximityChanged(NEAR)
✅ STATE TRANSITION: Curious → Sleep
```

**Expected Visual**: 
- 👁️ Eyes close
- 🎨 Screen shows "Sleep" state

### Step 4: Test Uncovering

**UNCOVER your hand**

**Expected Logs**:
```log
📡 Proximity: distance=5.0cm, shouldBeNear=false, currentState=NEAR
👋 PROXIMITY STATE CHANGED: NEAR → FAR
🟢 EVENT SENT: ProximityChanged(FAR)
✅ STATE TRANSITION: Sleep → Curious
```

**Expected Visual**:
- 👁️ Eyes open
- 🎨 Returns to previous state

---

## 🔍 Detailed Diagnostic

### Check 1: Sensor Registration
**Look for**:
```log
🔒 Proximity wake lock acquired (PARTIAL)
✓ Proximity: ACTIVE @ FASTEST (with wake lock)
📱 Proximity Sensor Info: name='stk3x6x Proximity', maxRange=5.0cm
```

✅ **PASS**: Sensor registered successfully
❌ **FAIL**: See "Troubleshooting" below

### Check 2: Initial Reading
**Look for**:
```log
👋 Proximity INITIALIZED: FAR (5.0cm) | MaxRange: 5.0cm
```

✅ **PASS**: Sensor providing data
❌ **FAIL**: Sensor not responding (see Check 3)

### Check 3: Health Check
**After 10 seconds, look for**:
```log
📊 PROXIMITY HEALTH CHECK: Readings=50, ValueChanges=5, Status=✓ Working
```

✅ **PASS**: Sensor is active and responding
❌ **FAIL**: `ValueChanges=0, Status=❌ SENSOR STUCK!`

---

## 🐛 Troubleshooting

### Issue 1: Sensor Not Registered
```log
❌ PRIMARY proximity registration FAILED!
```

**Solution A: Grant Permission**
1. Settings → Apps → RoboFaceAI → Permissions
2. Enable "Sensors" or "Additional Permissions"

**Solution B: Restart Device**
```
Power off → Wait 10 seconds → Power on
```

### Issue 2: Sensor Stuck at 5.0cm
```log
ValueChanges=0, CurrentValue=5.0cm, Status=❌ SENSOR STUCK!
```

**Solution A: Disable Battery Optimization**
1. Settings → Apps → RoboFaceAI → Battery
2. Select "Unrestricted" or "No restrictions"

**Solution B: Clear App Data**
1. Settings → Apps → RoboFaceAI → Storage
2. Clear Data → Relaunch app

**Solution C: Check Phone Case**
- Remove phone case (may block sensor)
- Clean top of phone (near front camera)

### Issue 3: Values Change But No State Transition
```log
📡 Proximity: distance=0.0cm, shouldBeNear=true, currentState=FAR
👋 Proximity needs 1 more NEAR readings  ← STUCK HERE
```

**Solution: Lower Consecutive Threshold**
Edit `SensorController.kt` line ~120:
```kotlin
// Change from:
private val consecutiveThreshold = 1

// To:
private val consecutiveThreshold = 0  // Immediate response
```

---

## 📊 Test Results Matrix

| Test | Expected | Actual | Status |
|------|----------|--------|--------|
| Sensor registration | ✓ ACTIVE @ FASTEST | _____ | ⬜ |
| Initial reading | 5.0cm (FAR) | _____ | ⬜ |
| Cover sensor | 0.0cm (NEAR) | _____ | ⬜ |
| State changes to Sleep | Eyes close | _____ | ⬜ |
| Uncover sensor | 5.0cm (FAR) | _____ | ⬜ |
| State wakes up | Eyes open | _____ | ⬜ |
| Health check | ValueChanges > 0 | _____ | ⬜ |

**Fill in your results and share if issues persist**

---

## 🎯 Alternative Test: Manual ADB

If you can't see logcat in Android Studio:

```bash
# 1. Enable USB Debugging on phone
# 2. Connect USB cable
# 3. Run:

adb logcat | findstr /C:"Proximity" /C:"SensorController" /C:"STATE TRANSITION"
```

---

## 📱 Device-Specific Notes

### Vivo Y-Series / X-Series
- Proximity sensor: `stk3x6x` or `ltr579`
- Binary sensor: Reports only 0.0cm or 5.0cm
- Known issue: May require app to be in foreground

### Workaround for Stubborn Sensors
If nothing works, add this test:

1. **Press SLEEP button** manually
2. **Cover sensor** (while in Sleep state)
3. **Press power button** to wake screen
4. If face stays sleeping → Sensor working, just needs different trigger

---

## ✅ Success Criteria

All of these should work:

1. ✅ Cover sensor → Eyes close (Sleep)
2. ✅ Uncover sensor → Eyes open (Wake)
3. ✅ Cover sensor multiple times → Consistent response
4. ✅ ValueChanges > 0 in health check
5. ✅ No "SENSOR STUCK" errors

If 4/5 work → Proximity sensor is functional ✅
If <3 work → Need device-specific investigation ⚠️

---

## 📝 Report Template

If you need to report issues:

```
Device: Vivo ________ (model)
Android Version: ________

Sensor Registration: ✅ / ❌
Initial Reading: ____ cm
Cover Test: ✅ / ❌
State Change: ✅ / ❌
Health Check: ValueChanges = ____

Logs:
[Paste relevant logcat here]

Screenshots:
[Attach if helpful]
```

---

**Now go ahead and test! The fix is ready.** 🚀

