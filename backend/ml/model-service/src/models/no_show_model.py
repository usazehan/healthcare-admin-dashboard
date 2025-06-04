from typing import Any, Dict
import numpy as np
import tensorflow as tf
from .base_model import BaseModel

class NoShowPredictionModel(BaseModel):
    def __init__(self):
        super().__init__("no_show_prediction")
        self.feature_columns = [
            'age', 'gender', 'day_of_week', 'time_of_day',
            'previous_no_shows', 'days_since_last_visit',
            'appointment_type', 'insurance_type',
            'distance_to_clinic', 'weather_condition'
        ]
        
    def build_model(self):
        """Build the neural network model"""
        model = tf.keras.Sequential([
            tf.keras.layers.Dense(64, activation='relu', input_shape=(len(self.feature_columns),)),
            tf.keras.layers.Dropout(0.2),
            tf.keras.layers.Dense(32, activation='relu'),
            tf.keras.layers.Dropout(0.2),
            tf.keras.layers.Dense(16, activation='relu'),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])
        
        model.compile(
            optimizer='adam',
            loss='binary_crossentropy',
            metrics=['accuracy', tf.keras.metrics.AUC()]
        )
        
        self.model = model
        return model
    
    def preprocess_data(self, data: Dict[str, Any]) -> np.ndarray:
        """Convert input data to feature vector"""
        features = []
        for column in self.feature_columns:
            if column in data:
                features.append(data[column])
            else:
                features.append(0)  # Default value for missing features
        return np.array(features).reshape(1, -1)
    
    def train(self, training_data: Dict[str, Any], validation_data: Dict[str, Any]) -> Dict[str, float]:
        """Train the model"""
        if self.model is None:
            self.build_model()
            
        X_train = np.array([self.preprocess_data(d) for d in training_data['features']])
        y_train = np.array(training_data['labels'])
        
        X_val = np.array([self.preprocess_data(d) for d in validation_data['features']])
        y_val = np.array(validation_data['labels'])
        
        history = self.model.fit(
            X_train, y_train,
            validation_data=(X_val, y_val),
            epochs=50,
            batch_size=32,
            callbacks=[
                tf.keras.callbacks.EarlyStopping(
                    monitor='val_loss',
                    patience=5,
                    restore_best_weights=True
                )
            ]
        )
        
        return {
            'accuracy': history.history['accuracy'][-1],
            'val_accuracy': history.history['val_accuracy'][-1],
            'auc': history.history['auc'][-1],
            'val_auc': history.history['val_auc'][-1]
        }
    
    def predict(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make prediction for no-show probability"""
        if self.model is None:
            raise ValueError("Model not loaded. Call load_model() first.")
            
        features = self.preprocess_data(data)
        probability = self.model.predict(features)[0][0]
        
        return {
            'no_show_probability': float(probability),
            'risk_level': self._get_risk_level(probability)
        }
    
    def _get_risk_level(self, probability: float) -> str:
        """Convert probability to risk level"""
        if probability < 0.3:
            return "Low"
        elif probability < 0.6:
            return "Medium"
        else:
            return "High" 