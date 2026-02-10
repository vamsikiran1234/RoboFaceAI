#!/usr/bin/env python3
"""
TensorFlow Lite Model Generator for RoboFaceAI
==============================================

This script generates a simple TensorFlow Lite model for gesture/emotion classification.

Model Specifications:
- Input: 30 float values (10 xyz sensor triplets from accelerometer/gyroscope)
- Output: 5 classes (idle, curious, happy, angry, sleep)
- Architecture: Simple feedforward neural network

Requirements:
    pip install tensorflow numpy

Usage:
    python generate_tflite_model.py

Output:
    app/src/main/assets/gesture_model.tflite
"""

import os
import numpy as np

try:
    import tensorflow as tf
    from tensorflow import keras
    print(f"✓ TensorFlow version: {tf.__version__}")
except ImportError:
    print("❌ ERROR: TensorFlow not installed!")
    print("Please install: pip install tensorflow")
    exit(1)


def create_gesture_model():
    """
    Create a simple gesture classification model.
    
    Architecture:
    - Input layer: 30 features (sensor data)
    - Dense layer 1: 64 neurons, ReLU
    - Dropout: 0.3
    - Dense layer 2: 32 neurons, ReLU
    - Dropout: 0.2
    - Output layer: 5 classes, Softmax
    """
    print("\n🔨 Building model architecture...")
    
    model = keras.Sequential([
        # Input layer
        keras.layers.Input(shape=(30,), name='sensor_input'),
        
        # Hidden layer 1
        keras.layers.Dense(64, activation='relu', name='dense1'),
        keras.layers.Dropout(0.3, name='dropout1'),
        
        # Hidden layer 2
        keras.layers.Dense(32, activation='relu', name='dense2'),
        keras.layers.Dropout(0.2, name='dropout2'),
        
        # Output layer (5 emotion classes)
        keras.layers.Dense(5, activation='softmax', name='output')
    ])
    
    # Compile model
    model.compile(
        optimizer='adam',
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )
    
    print("✓ Model architecture created")
    model.summary()
    
    return model


def generate_synthetic_training_data():
    """
    Generate synthetic training data for demonstration.
    
    In production, replace this with real sensor data collected from the app.
    
    Returns:
        X_train: Training features (1000 samples x 30 features)
        y_train: Training labels (1000 samples x 5 classes)
    """
    print("\n📊 Generating synthetic training data...")
    
    num_samples = 1000
    
    # Features: simulated sensor readings
    X_train = np.random.randn(num_samples, 30).astype(np.float32)
    
    # Labels: 5 classes (idle=0, curious=1, happy=2, angry=3, sleep=4)
    y_train = np.zeros((num_samples, 5), dtype=np.float32)
    
    # Simulate different gesture patterns
    for i in range(num_samples):
        magnitude = np.linalg.norm(X_train[i])
        
        # Classification logic based on magnitude (matches TFLiteEngine rules)
        if magnitude < 1.0:
            y_train[i, 4] = 1.0  # Sleep
        elif magnitude < 3.0:
            y_train[i, 0] = 1.0  # Idle
        elif magnitude < 12.0:
            y_train[i, 1] = 1.0  # Curious
        elif magnitude < 18.0:
            if np.random.random() > 0.5:
                y_train[i, 2] = 1.0  # Happy
            else:
                y_train[i, 1] = 1.0  # Curious
        else:
            y_train[i, 3] = 1.0  # Angry
    
    print(f"✓ Generated {num_samples} training samples")
    print(f"  - Class distribution:")
    for i, label in enumerate(['idle', 'curious', 'happy', 'angry', 'sleep']):
        count = np.sum(y_train[:, i])
        print(f"    {label}: {int(count)} samples")
    
    return X_train, y_train


def train_model(model, X_train, y_train):
    """Train the model with synthetic data."""
    print("\n🎯 Training model...")
    
    # Split into train/validation
    split_idx = int(0.8 * len(X_train))
    X_val = X_train[split_idx:]
    y_val = y_train[split_idx:]
    X_train = X_train[:split_idx]
    y_train = y_train[:split_idx]
    
    # Train
    history = model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=20,
        batch_size=32,
        verbose=1
    )
    
    # Evaluate
    val_loss, val_acc = model.evaluate(X_val, y_val, verbose=0)
    print(f"\n✓ Training complete!")
    print(f"  - Validation accuracy: {val_acc * 100:.2f}%")
    print(f"  - Validation loss: {val_loss:.4f}")
    
    return model


def convert_to_tflite(model, output_path):
    """
    Convert Keras model to TensorFlow Lite format.
    
    Optimizations:
    - Default optimization (size and latency)
    - Float16 quantization for smaller size
    """
    print("\n🔄 Converting to TensorFlow Lite...")
    
    # Create TFLite converter
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Apply optimizations
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    # Optional: Float16 quantization (smaller size, slightly faster)
    converter.target_spec.supported_types = [tf.float16]
    
    # Convert
    tflite_model = converter.convert()
    
    # Save to file
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    model_size_kb = len(tflite_model) / 1024
    print(f"✓ TFLite model saved to: {output_path}")
    print(f"  - Model size: {model_size_kb:.2f} KB")
    
    return tflite_model


def verify_tflite_model(tflite_model):
    """Verify the TFLite model can run inference."""
    print("\n🧪 Verifying TFLite model...")
    
    # Load interpreter
    interpreter = tf.lite.Interpreter(model_content=tflite_model)
    interpreter.allocate_tensors()
    
    # Get input/output details
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    print("✓ Model verification:")
    print(f"  - Input shape: {input_details[0]['shape']}")
    print(f"  - Input type: {input_details[0]['dtype']}")
    print(f"  - Output shape: {output_details[0]['shape']}")
    print(f"  - Output type: {output_details[0]['dtype']}")
    
    # Test inference
    test_input = np.random.randn(1, 30).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], test_input)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]['index'])
    
    predicted_class = np.argmax(output[0])
    class_names = ['idle', 'curious', 'happy', 'angry', 'sleep']
    
    print(f"\n✓ Test inference successful!")
    print(f"  - Test input magnitude: {np.linalg.norm(test_input):.2f}")
    print(f"  - Predicted class: {class_names[predicted_class]}")
    print(f"  - Confidence: {output[0][predicted_class] * 100:.2f}%")
    print(f"  - All probabilities: {output[0]}")


def main():
    """Main execution function."""
    print("=" * 60)
    print("TensorFlow Lite Model Generator for RoboFaceAI")
    print("=" * 60)
    
    # Output path
    output_path = os.path.join('app', 'src', 'main', 'assets', 'gesture_model.tflite')
    
    # Step 1: Create model
    model = create_gesture_model()
    
    # Step 2: Generate training data
    X_train, y_train = generate_synthetic_training_data()
    
    # Step 3: Train model
    model = train_model(model, X_train, y_train)
    
    # Step 4: Convert to TFLite
    tflite_model = convert_to_tflite(model, output_path)
    
    # Step 5: Verify
    verify_tflite_model(tflite_model)
    
    print("\n" + "=" * 60)
    print("✅ SUCCESS! TFLite model is ready for use!")
    print("=" * 60)
    print(f"\n📁 Model location: {output_path}")
    print("\n📋 Next steps:")
    print("  1. The model file is ready in app/src/main/assets/")
    print("  2. Rebuild your Android app (gradle sync)")
    print("  3. Run the app - AI inference will now use the TFLite model")
    print("  4. Check AI Stats overlay for real-time performance metrics")
    print("\n💡 Tips:")
    print("  - To improve accuracy, collect real sensor data from the app")
    print("  - Replace synthetic data with actual gesture recordings")
    print("  - Retrain with more epochs for better performance")
    print("\n")


if __name__ == '__main__':
    main()
