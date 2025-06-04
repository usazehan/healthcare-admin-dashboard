import grpc
from concurrent import futures
import tensorflow as tf
import numpy as np
import mlflow
from typing import Dict, Any, List

# Import generated protobuf code
from healthcare.ml.v1 import ml_service_pb2
from healthcare.ml.v1 import ml_service_pb2_grpc

class MLModelService(ml_service_pb2_grpc.MLServiceServicer):
    def __init__(self):
        self.models = {}
        self.initialize_models()
        
    def initialize_models(self):
        """Initialize all ML models"""
        self.models["no_show"] = self.create_no_show_model()
        self.models["treatment_outcome"] = self.create_treatment_outcome_model()
        self.models["readmission_risk"] = self.create_readmission_risk_model()
        
    def create_no_show_model(self) -> tf.keras.Model:
        """Create the no-show prediction model"""
        model = tf.keras.Sequential([
            tf.keras.layers.Dense(64, activation='relu', input_shape=(10,)),
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
        
        return model
        
    def create_treatment_outcome_model(self) -> tf.keras.Model:
        """Create the treatment outcome prediction model"""
        # Similar structure to no-show model
        return self.create_no_show_model()
        
    def create_readmission_risk_model(self) -> tf.keras.Model:
        """Create the readmission risk prediction model"""
        # Similar structure to no-show model
        return self.create_no_show_model()
        
    def PredictNoShow(self, request, context):
        """Predict no-show probability"""
        try:
            features = self.preprocess_features(request.appointment_data)
            probability = self.models["no_show"].predict(features)[0][0]
            
            return ml_service_pb2.NoShowPrediction(
                patient_id=request.patient_id,
                probability=float(probability),
                risk_level=self.get_risk_level(probability),
                confidence=0.85  # Placeholder
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.NoShowPrediction()
            
    def PredictTreatmentOutcome(self, request, context):
        """Predict treatment outcome"""
        try:
            return ml_service_pb2.TreatmentOutcome(
                patient_id=request.patient_id,
                predicted_outcome="Positive",
                confidence=0.85,
                factors=["Age", "Previous Treatment", "Current Condition"]
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.TreatmentOutcome()
            
    def AssessReadmissionRisk(self, request, context):
        """Assess readmission risk"""
        try:
            return ml_service_pb2.ReadmissionRisk(
                patient_id=request.patient_id,
                risk_score=0.65,
                risk_level=ml_service_pb2.RISK_LEVEL_MEDIUM,
                contributing_factors=["Age", "Previous Admissions", "Chronic Conditions"]
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.ReadmissionRisk()
            
    def GetTreatmentRecommendations(self, request, context):
        """Get treatment recommendations"""
        try:
            recommendations = [
                ml_service_pb2.TreatmentRecommendation(
                    treatment="Standard Protocol A",
                    confidence=0.92,
                    rationale="Based on patient history and current condition"
                )
            ]
            
            return ml_service_pb2.TreatmentRecommendations(
                recommendations=recommendations
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.TreatmentRecommendations()
            
    def AnalyzeDrugInteractions(self, request, context):
        """Analyze drug interactions"""
        try:
            interactions = [
                ml_service_pb2.DrugInteraction(
                    medication1=request.medications[0],
                    medication2=request.medications[1],
                    risk_level=ml_service_pb2.RISK_LEVEL_MEDIUM,
                    description="Potential interaction affecting drug metabolism"
                )
            ]
            
            return ml_service_pb2.DrugInteractions(
                interactions=interactions
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.DrugInteractions()
            
    def preprocess_features(self, data: Dict[str, str]) -> np.ndarray:
        """Convert input data to feature vector"""
        # Placeholder implementation
        return np.array([[0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]])
        
    def get_risk_level(self, probability: float) -> int:
        """Convert probability to risk level"""
        if probability < 0.3:
            return ml_service_pb2.RISK_LEVEL_LOW
        elif probability < 0.6:
            return ml_service_pb2.RISK_LEVEL_MEDIUM
        else:
            return ml_service_pb2.RISK_LEVEL_HIGH

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ml_service_pb2_grpc.add_MLServiceServicer_to_server(
        MLModelService(), server
    )
    server.add_insecure_port('[::]:50051')
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve() 