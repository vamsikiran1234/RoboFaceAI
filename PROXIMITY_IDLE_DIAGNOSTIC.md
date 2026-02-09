# 🔍 PROXIMITY SENSOR & IDLE BUTTON DIAGNOSTIC GUIDE

## 📱 Issue 1: Proximity Sensor Not Working

### **Device Checks to Perform**

#### ✅ **Check 1: Does Your Device Have a Proximity Sensor?**

**How to Check:**
1. **Settings → About Phone → Hardware Info** (varies by device)
2. **Use a Sensor Test App:**
   - Install "Sensor Kinetics" or "Sensors Multitool" from Play Store
   - Look for "TYPE_PROXIMITY" sensor
   - Check if it shows distance values changing when you cover/uncover

3. **Physical Location:**
   - Usually near the **front camera** or **earpiece**
   - Look for a tiny dot/lens near the top of your screen
   - Try covering that area with your hand

**Not All Devices Have Proximity Sensors!**
- Budget phones may skip this sensor
- Some tablets don't include it
- If missing, this is normal - the app will log a warning

---

#### ✅ **Check 2: Test Proximity Sensor Manually**

**Method 1: Phone Call Test**
1. Make a phone call
2. Move phone to your ear → **Screen should turn OFF**
3. Move phone away → **Screen should turn ON**
4. If this works, your proximity sensor is functional

**Method 2: Use Sensor App**
1. Install "Sensor Test" app
2. Find "Proximity" sensor
3. Cover sensor → Should show **0 cm** or **NEAR**
4. Uncover → Should show **5-8 cm** or **FAR**

---

#### ✅ **Check 3: Check App Logs (Logcat)**

**Open Android Studio → Logcat** and filter for:
```
Tag: SensorController
```

**What to Look For:**

**IF SENSOR IS AVAILABLE:**
```
✓ Proximity: ACTIVE
👋 Proximity initialized: FAR (8.5cm) | MaxRange: 8.5cm
👋 Proximity: NEAR (distance: 1.2cm)
👋 Proximity: FAR (distance: 8.5cm)
```

**IF SENSOR IS MISSING:**
```
⚠ Proximity not available
```

**IF SENSOR EXISTS BUT NOT WORKING:**
```
✓ Proximity: FAILED
```
→ This means the sensor exists but registration failed (rare hardware issue)

---

#### ✅ **Check 4: Verify App Permissions**

**Good News:** Proximity sensor doesn't require runtime permissions!
- It's a hardware sensor (not privacy-sensitive)
- No permissions needed in AndroidManifest.xml
- Should work automatically

---

#### ✅ **Check 5: Test in the App**

**Step-by-Step Test:**
1. **Launch RoboFaceAI**
2. **Check sensor overlay** at top-left:
   - Should show "SENSORS: TRACKING" when active
3. **Cover the top front of your phone** (near earpiece/camera)
   - Wait **300ms** (debounce delay)
   - **Expected:** Robo goes to **SLEEP** (dark, dim face)
   - **State indicator** changes to "Sleep"
4. **Remove your hand**
   - **Expected:** Robo **WAKES** to "Curious" (purple glow)

**What You Might See:**
- ✅ **Working:** Clean sleep/wake transitions
- ❌ **Not Working:** No state change when covering sensor
- ⚠️ **Flickering:** State changes rapidly (debounce working, but sensor noisy)

---

### **Common Proximity Sensor Issues**

| **Issue** | **Cause** | **Solution** |
|-----------|-----------|--------------|
| **No response at all** | Sensor doesn't exist on device | Normal - not all devices have it |
| **Sensor exists but not registering** | Hardware failure | Try device restart, check for screen protector blocking sensor |
| **Flickering between states** | Noisy sensor readings | Already handled by 300ms debounce - hold hand steady |
| **Only works sometimes** | Wrong location covered | Try covering different areas near top of screen |
| **Works in phone calls, not in app** | Lifecycle issue | Make sure app is in foreground and screen is on |

---

### **Screen Protector Alert! ⚠️**
- **Thick screen protectors** can block proximity sensor
- **Cutout misalignment** can interfere with sensor
- **Test without screen protector** to rule this out

---

## 🔘 Issue 2: Idle Button Reverts Back to Curious

### **Root Cause Analysis**

This is happening because the **AI engine is constantly running** and overriding manual state changes!

**What's Happening:**
1. You press **"Idle" button** → State changes to Idle ✅
2. After **5 seconds**, the manual lock expires
3. **AI runs inference every 1 second**
4. AI detects low motion → Predicts **"idle"** with 85% confidence
5. BUT due to slight phone movement or tilting, AI might predict **"curious"**
6. State changes back to Curious ❌

**The Problem:**
```kotlin
// In RoboViewModel.kt
private val manualChangeLockDuration = 5000L  // Only 5 seconds!

// In AIManager.kt  
if (result.confidence > 0.7f && result.prediction != "sleep") {
    _aiEvent.value = RoboEvent.AIResult(result.prediction, result.confidence)
}
// AI is CONSTANTLY overriding your manual choices!
```

**The AI prediction logic favors "curious" when there's ANY movement:**
```kotlin
// In TFLiteEngine.kt
scores[1] = when {  // CURIOUS
    magnitude in 3f..12f -> 0.80f  // Wide range!
    magnitude in 12f..15f -> 0.60f
    else -> 0.15f
}

scores[0] = when {  // IDLE  
    magnitude < 3f -> 0.85f  // Narrow range
    magnitude < 5f -> 0.65f
    else -> 0.20f
}
```

---

### **Why This Happens:**

1. **Phone on desk isn't perfectly still**
   - Micro-vibrations from table
   - Slight hand movements
   - Accelerometer noise
   - → Magnitude often 3-5 m/s² (enough to trigger "curious")

2. **AI runs every second**
   - Constant predictions
   - Overrides manual state after 5 seconds
   - No way to "lock" a manual state permanently

3. **"Idle" is hard to maintain**
   - Requires magnitude < 3 m/s²
   - "Curious" range is 3-12 m/s² (4x wider!)
   - Natural motion keeps triggering curious

---

### **Solutions Available**

I'll provide you with **3 fix options** (from easiest to most control):

---

#### **SOLUTION 1: Increase Manual Lock Duration (Quick Fix)**
Extend the manual state lock from 5 seconds to 30 seconds or more.

**Files to Change:**
- `app/src/main/java/com/example/robofaceai/viewmodel/RoboViewModel.kt`

**Change:**
```kotlin
private val manualChangeLockDuration = 5000L  // 5 seconds
```
**To:**
```kotlin
private val manualChangeLockDuration = 30000L  // 30 seconds
```

**Pros:** Simple, one-line fix
**Cons:** AI still overrides eventually

---

#### **SOLUTION 2: Add AI Toggle Button (Recommended)**
Add a button to completely disable/enable AI predictions.

**Add this to the UI:**
- Toggle button: "AI Mode: ON/OFF"
- When OFF, AI predictions are ignored
- Manual states persist indefinitely

**Pros:** Full user control, AI can still be used when wanted
**Cons:** Requires UI changes

---

#### **SOLUTION 3: Make Idle More Robust (Best for Real-World Use)**
Adjust AI thresholds so "idle" is easier to maintain:
- Increase idle threshold to < 5 m/s² (instead of < 3)
- Decrease curious threshold to > 6 m/s² (instead of > 3)
- Add hysteresis to prevent bouncing

**Pros:** More realistic, better user experience
**Cons:** Requires testing to tune thresholds

---

## 🛠️ **RECOMMENDED FIXES**

I'll implement **ALL THREE** solutions for you:

### **Fix 1: Extended Manual Lock** ✅
- Increase to 30 seconds
- Quick immediate improvement

### **Fix 2: AI Toggle Button** ✅  
- Add "AI Mode" toggle
- Full control over state changes

### **Fix 3: Improved AI Thresholds** ✅
- Make "idle" more stable
- Reduce "curious" sensitivity

---

## 📊 **Verification Checklist**

After fixes are applied, test these scenarios:

### **Proximity Sensor:**
- [ ] Check Logcat for sensor availability
- [ ] Cover sensor → Sleep state (dark)
- [ ] Uncover → Curious state (purple)
- [ ] No flickering (300ms debounce working)
- [ ] If sensor missing, app logs warning but doesn't crash

### **Idle Button:**
- [ ] Press Idle button → State changes to Idle
- [ ] **With AI ON:** State stays idle if phone is still
- [ ] **With AI OFF:** State NEVER changes (manual control only)
- [ ] No automatic reversion to Curious
- [ ] Manual lock duration extended to 30s

---

## 🎯 **Next Steps**

Would you like me to:
1. ✅ **Apply all 3 fixes now** (recommended)
2. 🔧 **Apply just the quick fix** (manual lock extension)
3. 📝 **Show you the code changes first** (review before applying)

Let me know and I'll implement the fixes!

---

**Generated:** February 8, 2026  
**App:** RoboFaceAI  
**Issues:** Proximity sensor detection + Idle state AI override

