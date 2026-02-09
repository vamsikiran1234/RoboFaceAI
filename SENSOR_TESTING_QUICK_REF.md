# 🧪 SENSOR TESTING QUICK REFERENCE

## 🎯 Quick Test Commands

### Monitor All Sensor Events
```bash
adb logcat -c && adb logcat | grep -E "SensorController|RoboReducer|RoboViewModel"
```

### Monitor Proximity Only
```bash
adb logcat | grep "PROXIMITY"
```

### Monitor Shake Only
```bash
adb logcat | grep "SHAKE"
```

---

## ✅ Test Scenarios

### Test 1: Proximity → Sleep
**Action:** Cover top of phone with hand  
**Expected State:** 😴 SLEEP (Blue)  
**Expected Log:** `👋 PROXIMITY CHANGED: FAR → NEAR | 🔵 TRIGGERING SLEEP`

### Test 2: Proximity → Wake
**Action:** Remove hand from phone  
**Expected State:** 🤔 CURIOUS (Yellow)  
**Expected Log:** `👋 PROXIMITY CHANGED: NEAR → FAR | 🟢 TRIGGERING WAKE`

### Test 3: Mild Shake
**Action:** Gentle shake (wrist flick)  
**Expected State:** 🤔 CURIOUS (Yellow)  
**Expected Log:** `🔍 SHAKE DETECTED! Magnitude: 13-15 m/s²`

### Test 4: Strong Shake
**Action:** Vigorous shake (full arm)  
**Expected State:** 😠 ANGRY (Red)  
**Expected Log:** `💢 STRONG SHAKE DETECTED! Magnitude: 19+ m/s²`

---

## 🔍 Log Patterns to Watch

### ✅ GOOD Logs (Working Correctly)
```
SensorController: 👋 PROXIMITY CHANGED: FAR → NEAR | Distance: 0.0cm | 🔵 TRIGGERING SLEEP
RoboReducer: 😴 Proximity NEAR detected → Transitioning to SLEEP
RoboViewModel: ✅ STATE TRANSITION: Curious → Sleep

SensorController: 🔍 SHAKE DETECTED! Magnitude: 15.2 m/s² | Intensity: 0.40
RoboReducer: 🤔 Mild shake detected (intensity=0.40) → Transitioning to CURIOUS
RoboViewModel: ✅ STATE TRANSITION: Idle → Curious
```

### ❌ BAD Logs (Issues)
```
# Proximity not detected (sensor not working)
# No proximity logs appearing

# Shake threshold too high (not triggering)
SensorController: Shake peak: 10.5 m/s² (below threshold)

# Rapid state changes (debouncing issue)
Multiple PROXIMITY CHANGED logs < 300ms apart
```

---

## 🎨 Visual State Reference

| Emoji | State | Color | Trigger |
|-------|-------|-------|---------|
| 😴 | SLEEP | Blue | Proximity NEAR |
| 🤔 | CURIOUS | Yellow | Proximity FAR, Mild shake, Tilt |
| 😠 | ANGRY | Red | Strong shake |
| 😊 | HAPPY | Green | Manual/AI |
| 😐 | IDLE | White | Default |

---

## 📱 Sensor Thresholds

### Proximity
- **NEAR:** < 1cm (typically 0.0cm)
- **FAR:** > 1cm (typically 5-10cm)
- **Debounce:** 300ms

### Shake Detection
- **Mild:** 12-18 m/s² → Curious (Intensity: 0.3-0.6)
- **Strong:** > 18 m/s² → Angry (Intensity: 0.7-1.0)
- **Cooldown:** 800ms

---

## 🐛 Troubleshooting

### Proximity Not Working
1. Check if sensor exists: Look for `✓ Proximity: ACTIVE` in logs
2. Try covering different areas near earpiece
3. Verify device has proximity sensor (not all phones do)
4. Check `proximityInitialized` flag in logs

### Shake Not Detected
1. Shake harder (need > 12 m/s²)
2. Check for `🔔 Shake peak detected` logs
3. Verify accelerometer is active
4. Try different shake motions (up/down, side/side)

### States Not Changing
1. Check `STATE TRANSITION` logs
2. Verify RoboReducer is processing events
3. Look for `⏸️ No state change` (already in that state)
4. Check if manual state lock is active (5 sec after button press)

---

## 🚀 Quick Deploy

```bash
# Build
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.example.robofaceai/.MainActivity

# Monitor
adb logcat -c && adb logcat | grep -E "Sensor|Robo"
```

---

## ✅ Success Checklist

- [ ] Proximity sensor triggers sleep reliably
- [ ] Proximity sensor wakes from sleep reliably
- [ ] Mild shake triggers curious state
- [ ] Strong shake triggers angry state
- [ ] Visual emoji states are correct colors
- [ ] All events appear in Logcat
- [ ] No crashes or sensor errors
- [ ] Smooth transitions (no lag/flicker)

---

## 📊 Expected Behavior Summary

```
Phone Action          → Sensor Event        → State Change      → Visual
─────────────────────────────────────────────────────────────────────────
Cover sensor         → Proximity NEAR      → SLEEP             → 😴 Blue
Uncover sensor       → Proximity FAR       → CURIOUS           → 🤔 Yellow
Gentle shake         → Shake (12-18 m/s²)  → CURIOUS           → 🤔 Yellow
Strong shake         → Shake (>18 m/s²)    → ANGRY             → 😠 Red
Tilt phone           → Tilt detected       → CURIOUS           → 🤔 Yellow
Rotate phone         → Rotation detected   → Head tilt effect  → (visual)
Wait 5 sec (angry)   → Idle timeout        → IDLE              → 😐 White
```

---

**🟢 All sensors should respond within 500ms**  
**🟢 Logs should show complete event flow**  
**🟢 Visual feedback should be immediate and clear**

