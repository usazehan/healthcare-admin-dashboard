from abc import ABC, abstractmethod
from typing import Any, Dict, List
import mlflow
import tensorflow as tf
import numpy as np

class BaseModel(ABC):
    def __init__(self, model_name: str):
        self.model_name = model_name
        self.model = None
        self.mlflow_client = mlflow.tracking.MlflowClient()

    @abstractmethod
    def preprocess_data(self, data: Dict[str, Any]) -> np.ndarray:
        """Preprocess input data for model prediction"""
        pass

    @abstractmethod
    def train(self, training_data: Dict[str, Any], validation_data: Dict[str, Any]) -> Dict[str, float]:
        """Train the model and return metrics"""
        pass

    @abstractmethod
    def predict(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make predictions using the model"""
        pass

    def save_model(self, version: str):
        """Save model to MLflow"""
        with mlflow.start_run(run_name=f"{self.model_name}_v{version}"):
            mlflow.tensorflow.log_model(self.model, self.model_name)

    def load_model(self, version: str):
        """Load model from MLflow"""
        model_uri = f"models:/{self.model_name}/{version}"
        self.model = mlflow.tensorflow.load_model(model_uri)

    def evaluate(self, test_data: Dict[str, Any]) -> Dict[str, float]:
        """Evaluate model performance"""
        pass
