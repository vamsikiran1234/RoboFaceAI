# 🔬 PROXIMITY SENSOR DIAGNOSTICS & TROUBLESHOOTING GUIDE

## ✅ Changes Made (Feb 8, 2026)

### 1. Enhanced Proximity Sensor Diagnostics
- **More frequent logging**: Health check every 20 readings (was 50)
- **Value change tracking**: Logs every time sensor value changes
- **Detailed state change logging**: Ultra-verbose proximity state transitions
- **Stuck sensor detection**: Alerts if sensor doesn't change values

### 2. Improved Proximity Detection Algorithm
- **Ultra-sensitive detection**: Now uses `distance < maxRange` instead of fixed 5cm threshold
- **Better compatibility**: Works with all binary proximity sensors
- **More logging**: Every reading, state change, and debounce logged

### 3. Fixed "Idle Button Reverting to Curious" Issue
- **Extended AI lock**: Increased from 30s to **60 seconds** (1 minute)
- **Better logging**: Shows when AI events are blocked and remaining lock time
- **Clear feedback**: "AI events LOCKED for 60s" message on manual button press

### 4. Added Diagnostic Tools
- **New method**: `getProximityDiagnostics()` - Shows sensor health, readings, changes
- **Sensor stats**: Detailed information about your proximity sensor hardware

---

## 🔍 Understanding Your Proximity Sensor

### What the Logs Tell You

**When you run the app, watch for these logs:**

```
📡 RAW Proximity #1: distance=5.0cm, maxRange=5.0cm
🔍 PROXIMITY LOGIC: distance=5.0cm, maxRange=5.0cm, shouldBeNear=false
👋 Proximity INITIALIZED: FAR (5.0cm) | MaxRange: 5.0cm | Threshold: <5.0cm = NEAR
```

**This tells you:**
- ✅ Sensor is working and sending data
- Your sensor reports **5.0cm as the maximum range**
- Currently reading **5.0cm** (FAR state)

**If you cover the sensor, you should see:**

```
🔄 PROXIMITY VALUE CHANGED: 5.0cm → 0.0cm (Change #1)
⚡ PROXIMITY STATE CHANGE DETECTED: FAR → NEAR | Debounce: 150ms / 100ms
👋 ✅ PROXIMITY STATE CHANGED: FAR → NEAR | Distance: 0.0cm
🔵 EVENT SENT: ProximityChanged(NEAR) - SHOULD TRIGGER SLEEP
```

---

## 🧪 Testing Your Proximity Sensor

### Test 1: Does Your Sensor Report Different Values?

1. **Start the app**
2. **Watch logcat** (filter for "SensorController")
3. **Cover the top of your phone** (where front camera is)
4. **Look for this log:**
   ```
   🔄 PROXIMITY VALUE CHANGED: 5.0cm → 0.0cm (Change #1)
   ```

**Expected Results:**
- ✅ **WORKING**: You see value change from 5.0cm to 0.0cm (or similar)
- ❌ **NOT WORKING**: Value stays at 5.0cm even when covered

### Test 2: Health Check (After ~20 readings)

**Look for:**
```
📊 PROXIMITY HEALTH CHECK #1: 
Readings=20, ValueChanges=5, CurrentValue=5.0cm, MaxRange=5.0cm, 
Status=✓ Working
```

**What it means:**
- **ValueChanges > 0**: ✅ Sensor is working
- **ValueChanges = 0**: ❌ Sensor stuck or not responding

### Test 3: Call Test (Your Test)

You mentioned:
> "In my mobile I set the time to show when shake of mobile. When time shows, I cover the top right corner of front camera, then the time doesn't show. This confirms my mobile proximity sensor is perfect."

**This is CORRECT!** ✅ Your proximity sensor **DOES WORK**.

---

## 🤔 Why Might RoboFaceAI Not Detect Proximity?

### Possible Issues:

#### 1. **Sensor Location Confusion**
- **Your test**: Top right corner near front camera ✅
- **Where to cover for RoboFaceAI**: Same location
- **Tip**: Try covering a larger area (top half of phone)

#### 2. **Binary Sensor Behavior**
- Most proximity sensors are **binary** (ON/OFF, not gradual)
- They report **only 2 values**: 0.0cm (NEAR) or 5.0cm (FAR)
- No values in between!

#### 3. **App Permissions**
- RoboFaceAI doesn't need special permissions for proximity
- But some manufacturers block sensor access
- **Check**: Settings → Apps → RoboFaceAI → Permissions

#### 4. **Sensor Wake Lock**
- Some phones put sensors to sleep aggressively
- **Solution**: Keep screen on (app already does this)

---

## 🛠️ Diagnostic Steps

### Step 1: Check Sensor Availability

**Look for this log on app start:**
```
📱 Sensor Availability:
├─ Accelerometer: ✓
├─ Gyroscope: ✓
└─ Proximity: ✓
```

If Proximity shows ❌, your phone doesn't expose it to apps.

### Step 2: Monitor Raw Readings

**Filter logcat for**: `📡 RAW Proximity`

**Expected pattern (working sensor):**
```
📡 RAW Proximity #1: distance=5.0cm, maxRange=5.0cm    (uncovered)
📡 RAW Proximity #2: distance=5.0cm, maxRange=5.0cm    (uncovered)
📡 RAW Proximity #3: distance=0.0cm, maxRange=5.0cm    (COVERED!)
📡 RAW Proximity #4: distance=0.0cm, maxRange=5.0cm    (covered)
📡 RAW Proximity #5: distance=5.0cm, maxRange=5.0cm    (uncovered again)
```

### Step 3: Check Value Changes

**Filter logcat for**: `PROXIMITY VALUE CHANGED`

**If sensor is working:**
```
🔄 PROXIMITY VALUE CHANGED: 5.0cm → 0.0cm (Change #1)
🔄 PROXIMITY VALUE CHANGED: 0.0cm → 5.0cm (Change #2)
```

**If sensor is stuck:**
```
(No logs appear - sensor always reports 5.0cm)
```

### Step 4: Check Health Status

**After 20 readings, look for:**
```
📊 PROXIMITY HEALTH CHECK #1: 
Status=✓ Working          ← GOOD!
-- or --
Status=❌ SENSOR STUCK!    ← PROBLEM!
```

---

## 🎯 Testing Procedure

### Complete Test Sequence:

1. **Launch RoboFaceAI app**
2. **Open Logcat** (filter for `SensorController`)
3. **Wait 2 seconds** (let sensors initialize)
4. **Cover top of phone** (front camera area) with your hand
5. **Hold for 3 seconds**
6. **Uncover**
7. **Repeat 3-5 times**

### What to Look For:

**✅ WORKING:**
```
👋 ✅ PROXIMITY STATE CHANGED: FAR → NEAR
🔵 EVENT SENT: ProximityChanged(NEAR) - SHOULD TRIGGER SLEEP
(Robo face should turn dark/dim - Sleep state)
```

**❌ NOT WORKING:**
```
📌 Proximity STABLE: FAR (distance=5.0cm)
(No state change, Robo face stays same)
```

---

## 🔧 If Proximity Doesn't Work

### Option 1: Check Sensor Location
- **Try different positions**: Top middle, top left, top right
- **Cover larger area**: Entire top half of screen
- **Hold longer**: 3-5 seconds (debounce is 100ms)

### Option 2: Check Phone Settings
- **Developer Options**: Disable any sensor throttling
- **Battery Saver**: Turn OFF (can throttle sensors)
- **Screen**: Keep awake (app does this automatically)

### Option 3: Use a Sensor App
- Install **"Sensor Kinetics"** from Play Store
- Open it → Find "Proximity" sensor
- **Watch the value** as you cover/uncover
- **Expected**: 0.0 (near) ↔ 5.0 (far)

### Option 4: Check for App Issues
**Run this command in Android Studio Terminal:**
```bash
adb logcat -s SensorController:D RoboViewModel:D
```

This shows ONLY sensor-related logs.

---

## 📊 Understanding the Logs

### Log Prefix Guide:

| Emoji | Meaning |
|-------|---------|
| 📡 | Raw sensor reading |
| 🔍 | Detection logic |
| 👋 | Proximity state change |
| 🔄 | Value change detected |
| ⚡ | State change being processed |
| ✅ | State changed successfully |
| 🔵 | Event sent (NEAR = Sleep) |
| 🟢 | Event sent (FAR = Wake) |
| 📊 | Health check report |
| ❌ | Error or stuck sensor |
| ⏱️ | Debouncing in progress |
| 📌 | State stable (no change) |

---

## 🎮 Manual State Change (Idle Button Issue)

### Why Idle Reverts to Curious?

**FIXED!** The issue was:
- AI runs every **1 second**
- AI sees tilted phone → predicts **"Curious"**
- After **30 seconds**, AI overrides your manual **Idle** choice

### Solution Applied:

1. **Extended lock time**: 30s → **60 seconds** (1 minute)
2. **Added logging**: Shows when AI is blocked
3. **Clear feedback**: "AI events LOCKED for 60s" message

### How It Works Now:

1. **Press "Idle" button**
   ```
   🔘 Manual button pressed: Idle | AI events LOCKED for 60s
   ```

2. **AI tries to change state** (blocked for 60s)
   ```
   🔒 AI event BLOCKED (manual lock active for 58s more) - Prediction: curious
   🔒 AI event BLOCKED (manual lock active for 57s more) - Prediction: curious
   ```

3. **After 60 seconds**, AI resumes
   ```
   🔄 Processing event: AIResult
   ✅ STATE TRANSITION: Idle → Curious
   ```

### Tips:

- **Want to keep Idle longer?** Press Idle button again (resets 60s timer)
- **Want AI to take over?** Just wait 60 seconds
- **Want to disable AI completely?** Not currently supported (always-on design)

---

## 📱 About Your Sensor (From Your Test)

You said:
> "For call my mobile proximity is working, but for this RoboFaceAI it's not working"

**Analysis:**

1. **Call proximity** (screen off during call):
   - Uses **wake lock** to keep sensor active
   - Direct hardware access
   - OS-level priority

2. **RoboFaceAI proximity**:
   - Uses standard Android **SensorManager API**
   - Same hardware, different access method
   - May be throttled by manufacturer

**Your sensor DOES work!** The question is:
- Does it report value changes to third-party apps?
- Or is it restricted to system apps only?

---

## ✅ Verification Checklist

Use this to verify your sensor:

- [ ] App shows "✓ Proximity: ACTIVE" on startup
- [ ] Logcat shows `📡 RAW Proximity` logs
- [ ] Covering sensor changes value (5.0 → 0.0)
- [ ] Health check shows "✓ Working" with ValueChanges > 0
- [ ] State changes from FAR to NEAR in logs
- [ ] Robo face enters Sleep state (dark/dim)

**If all checked:** ✅ Sensor is working perfectly!
**If some unchecked:** See troubleshooting section above

---

## 🆘 Still Not Working?

### Get Diagnostic Report:

**In Android Studio:**
1. Run app
2. Cover/uncover proximity sensor 10 times
3. **Copy all logs** that contain "Proximity" or "SensorController"
4. **Share logs** so I can analyze

### Quick Command:
```bash
adb logcat -d | findstr /i "proximity sensorcontroller"
```

This dumps all proximity-related logs.

---

## 📝 Summary

### What Was Changed:

1. ✅ **More diagnostic logging** (every 20 readings, not 50)
2. ✅ **Value change detection** (alerts when sensor value changes)
3. ✅ **Ultra-sensitive detection** (uses maxRange threshold)
4. ✅ **Better state logging** (shows every state transition)
5. ✅ **Fixed Idle button issue** (60s AI lock instead of 30s)
6. ✅ **Added diagnostic tools** (getProximityDiagnostics method)

### Next Steps:

1. **Rebuild the app**
2. **Run it on your phone**
3. **Watch the logs** while covering/uncovering sensor
4. **Report back** what you see in logs

### Expected Outcome:

- **IF sensor works for calls**: It SHOULD work for app
- **IF logs show value changes**: Proximity IS working
- **IF logs show stuck sensor**: Hardware/driver limitation

---

**Let me know what you see in the logs after running this version!** 🔬📊

