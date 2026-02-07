# 🧪 SENSOR FUSION TESTING GUIDE

## 🚀 Quick Start - Testing All Sensors

This guide will help you verify that all sensor behaviors are working correctly.

---

## 📱 Before You Start

### 1. Build and Install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Launch the App
- Open RoboFaceAI on your Android device
- You should see the robo face in the center
- Green "SENSORS" box in top-left corner
- "AI STATS" box in top-right corner

---

## ✅ Test 1: Accelerometer - Tilt Detection

### What to Do
1. **Hold phone upright** (portrait mode)
2. **Slowly tilt LEFT** (lean phone to the left)
3. **Tilt RIGHT**
4. **Tilt FORWARD** (top of phone forward)
5. **Tilt BACKWARD** (top of phone back)

### Expected Behavior
- ✅ **Eyes move in the SAME direction as tilt**
  - Tilt left → Eyes move left
  - Tilt right → Eyes move right
  - Tilt forward → Eyes move up
  - Tilt backward → Eyes move down
- ✅ **Movement is SMOOTH and RESPONSIVE**
- ✅ **Eyes move dramatically** (55% of eye radius)
- ✅ **Sensor overlay shows**:
  - `Tilt X: 0.XX` (changes with left/right tilt, turns YELLOW)
  - `Tilt Y: 0.XX` (changes with forward/back tilt, turns YELLOW)
  - `🎯 TRACKING` (green) appears during movement

### Logcat Output
```
SensorController: 📊 Sensor FPS: 95.2 Hz
```

### Troubleshooting
- If eyes don't move: Check that accelerometer is available
- If movement is jerky: Normal device behavior, filters smooth it
- If movement too small: Values should reach ±0.3 to ±0.8 range

---

## ✅ Test 2: Gyroscope - Head Rotation

### What to Do
1. **Hold phone flat** (screen facing up)
2. **Rotate phone CLOCKWISE** (like turning a steering wheel right)
3. **Rotate phone COUNTER-CLOCKWISE**
4. **Return to center** (should auto-center)

### Expected Behavior
- ✅ **Entire robo head rotates** with phone rotation
- ✅ **Rotation angle** ±25° max
- ✅ **Smooth auto-centering** when you stop rotating
- ✅ **Sensor overlay shows**:
  - `Rotation: X.X°` (changes during rotation, turns YELLOW)
  - Value increases clockwise, decreases counter-clockwise

### Logcat Output
```
SensorController: ✓ Gyroscope: ACTIVE @ UI RATE
```

### Troubleshooting
- If no rotation visible: Some devices lack gyroscope
- If rotation drifts: Normal - auto-centers in 1-2 seconds
- If rotation too subtle: Look at eye rings - they rotate

---

## ✅ Test 3: Proximity Sensor - Sleep/Wake

### What to Do
1. **Cover proximity sensor** with your hand
   - Usually near the top front of phone
   - Try covering the earpiece area
2. **Remove hand** (pull away from phone)
3. **Repeat** several times

### Expected Behavior
- ✅ **Hand NEAR** (<5cm):
  - Robo goes to **SLEEP state**
  - Face darkens
  - Eyes dim
  - State indicator shows "Sleep"
- ✅ **Hand FAR** (>5cm):
  - Robo **WAKES to Curious**
  - Face brightens (purple glow)
  - Eyes become active
  - State indicator shows "Curious"

### Logcat Output
```
SensorController: 👋 Proximity: NEAR (distance: 1.2cm)
SensorController: 👋 Proximity: FAR (distance: 8.5cm)
```

### Troubleshooting
- If no response: Some devices don't have proximity sensor
- If flickers: Debouncing active - hold hand steady for 300ms
- Can't find sensor: Look for small dot near earpiece/camera

---

## ✅ Test 4: Shake Detection - Mild Shake

### What to Do
1. **Hold phone firmly**
2. **Give a GENTLE shake** (like shaking a small bottle)
3. **Not too hard** - we want mild shake first

### Expected Behavior
- ✅ **State changes to CURIOUS**
- ✅ **Purple glow** on face
- ✅ **Eyes become more active**
- ✅ **Sensor overlay shows TRACKING**

### Logcat Output
```
SensorController: 🔍 Mild shake. Magnitude: 15.2 m/s² | Intensity: 0.42
```

### Troubleshooting
- If triggers angry: You shook too hard!
- If no response: Shake harder - threshold is 14 m/s²
- If too sensitive: Threshold may need tuning for your device

---

## ✅ Test 5: Shake Detection - Strong Shake

### What to Do
1. **Hold phone firmly** (don't drop it!)
2. **Give a HARD shake** (like shaking a spray can)
3. **Quick, forceful motion**

### Expected Behavior
- ✅ **State changes to ANGRY**
- ✅ **RED face** with intense glow
- ✅ **Fast pulsing** animation
- ✅ **Eyes pulse rapidly**
- ✅ **After 5 seconds**, calms down to Idle

### Logcat Output
```
SensorController: 💢 STRONG SHAKE! Magnitude: 23.7 m/s² | Intensity: 0.88
```

### Troubleshooting
- If only triggers curious: Shake HARDER
- Magnitude needs to exceed 20 m/s²
- Try a quick wrist flick motion

---

## 📊 Monitoring Sensors in Real-Time

### Sensor Debug Overlay (Top-Left Green Box)
```
SENSORS
Tilt X: -0.23    ← Changes with left/right tilt
Tilt Y: 0.45     ← Changes with forward/back tilt
Rotation: -12.3° ← Changes with phone rotation
🎯 TRACKING      ← Green when sensors active
```

### Color Coding
- **WHITE** = Sensor at rest (< 0.1)
- **YELLOW** = Sensor active (> 0.1)
- **GREEN "TRACKING"** = Movement detected
- **GRAY "STABLE"** = No movement

---

## 🔍 Advanced Testing - Logcat Monitoring

### Enable Verbose Logging
```bash
adb shell
setprop log.tag.SensorController VERBOSE
```

### Watch Real-Time Logs
```bash
adb logcat -s SensorController:*
```

### What You'll See
```
SensorController: 🚀 Starting sensor fusion system...
SensorController: ✓ Accelerometer: ACTIVE @ UI RATE (~60-100Hz)
SensorController: ✓ Gyroscope: ACTIVE @ UI RATE
SensorController: ✓ Proximity: ACTIVE
SensorController: 🎯 Sensor fusion system ready
SensorController: 📊 Sensor FPS: 87.3 Hz
SensorController: 🔍 Mild shake. Magnitude: 15.8 m/s²
SensorController: 👋 Proximity: NEAR (distance: 0.5cm)
```

---

## 🎯 Complete Test Checklist

### Basic Functionality
- [ ] Eyes follow left tilt
- [ ] Eyes follow right tilt
- [ ] Eyes follow forward tilt
- [ ] Eyes follow backward tilt
- [ ] Head rotates with phone rotation
- [ ] Proximity sleep works
- [ ] Proximity wake works
- [ ] Mild shake triggers Curious
- [ ] Strong shake triggers Angry
- [ ] Angry times out to Idle after 5s

### Advanced Checks
- [ ] Eye movement is smooth (no jerking)
- [ ] Gyroscope auto-centers smoothly
- [ ] No sensor noise causing random movements
- [ ] Sensor overlay updates in real-time
- [ ] Colors change when sensors active
- [ ] Proximity doesn't flicker
- [ ] Shake cooldown prevents spam

### Performance
- [ ] Frame rate stays smooth (60 FPS)
- [ ] No lag between tilt and eye movement
- [ ] App doesn't consume excessive battery
- [ ] Sensors stop when app backgrounded

---

## 🐛 Common Issues & Solutions

### Issue: Eyes don't move at all
**Solution:**
1. Check logcat for "Accelerometer not available"
2. Try restarting the app
3. Grant motion sensor permissions if prompted
4. Device may not have accelerometer (rare)

### Issue: Movement is very slow/laggy
**Solution:**
1. Check sensor FPS in logcat (should be >50 Hz)
2. Device may be in power saving mode
3. Close other apps to free resources
4. Check that SENSOR_DELAY_UI is being used

### Issue: Proximity sensor doesn't work
**Solution:**
1. Many devices don't have this sensor
2. Try different areas near camera/earpiece
3. Check logcat for "Proximity not available"
4. App works fine without it

### Issue: Shake too sensitive/not sensitive enough
**Solution:**
1. Thresholds can be adjusted in SensorController.kt:
   - `shakeThresholdMild = 14f` (lower = more sensitive)
   - `shakeThresholdStrong = 20f`
2. Check magnitude in logs and adjust accordingly

### Issue: Gyroscope rotation barely visible
**Solution:**
1. Look at the eye circuit rings - they rotate
2. Try rotating phone more dramatically
3. Values in overlay should show ±10° or more
4. Some devices have low gyroscope sensitivity

---

## 📈 Performance Benchmarks

### Expected Values
- **Sensor Update Rate:** 60-100 Hz
- **Tilt Response Time:** <16ms (sub-frame)
- **Visual Update Rate:** 60 FPS
- **Memory Usage:** <5MB for sensor processing
- **Battery Impact:** Minimal (~1-2% per hour)

### Optimal Ranges
- **Tilt X/Y:** ±0.3 to ±0.8 for visible movement
- **Rotation:** ±10° to ±25° for dramatic effect
- **Shake Mild:** 14-19 m/s²
- **Shake Strong:** 20+ m/s²
- **Proximity:** <5cm for NEAR

---

## 🎓 Understanding the Sensor Values

### Tilt X (Left/Right)
- `-1.0` = Maximum LEFT tilt
- `0.0` = Upright
- `+1.0` = Maximum RIGHT tilt
- Practical range: ±0.3 to ±0.8

### Tilt Y (Forward/Back)
- `-1.0` = Maximum FORWARD tilt
- `0.0` = Upright
- `+1.0` = Maximum BACKWARD tilt
- Practical range: ±0.3 to ±0.8

### Rotation (Gyroscope)
- `-25°` = Maximum counter-clockwise
- `0°` = No rotation
- `+25°` = Maximum clockwise
- Auto-centers when rotation stops

### Shake Magnitude
- `0-13 m/s²` = No shake (gravity only)
- `14-19 m/s²` = Mild shake → Curious
- `20+ m/s²` = Strong shake → Angry

---

## 🎉 Success Criteria

Your sensor fusion is working **perfectly** if:

✅ **All movements are SMOOTH and NATURAL**  
✅ **Response is IMMEDIATE** (no lag)  
✅ **Visual feedback is DRAMATIC** (clearly visible)  
✅ **Sensor overlay updates in REAL-TIME**  
✅ **No jerky or glitchy motion**  
✅ **App feels ALIVE and RESPONSIVE**  

---

## 🚀 Next Steps

Once all tests pass:
1. ✅ Sensors are working professionally
2. ✅ Ready for demonstration
3. ✅ Ready for code review
4. ✅ Production-ready quality

**Congratulations!** You now have a **top-tier sensor fusion implementation** that demonstrates **expert-level** mobile development skills! 🏆

