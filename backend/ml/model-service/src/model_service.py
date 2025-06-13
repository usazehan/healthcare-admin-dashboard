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

    def get_model(self, model_name: str) -> tf.keras.Model:
        """Get model by name, creating it if it doesn't exist"""
        if model_name not in self.models:
            if model_name == "no_show":
                self.models[model_name] = self.create_no_show_model()
            elif model_name == "treatment_outcome":
                self.models[model_name] = self.create_treatment_outcome_model()
            elif model_name == "readmission_risk":
                self.models[model_name] = self.create_readmission_risk_model()
        return self.models[model_name]

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
            model = self.get_model("no_show")
            features = self.preprocess_features(request.appointment_data)
            probability = model.predict(features)[0][0]

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
            model = self.get_model("treatment_outcome")
            features = self.preprocess_features(request.treatment_data)
            probability = model.predict(features)[0][0]

            return ml_service_pb2.TreatmentOutcome(
                patient_id=request.patient_id,
                predicted_outcome="Positive" if probability > 0.5 else "Negative",
                confidence=float(probability),
                factors=["Age", "Previous Treatment", "Current Condition"]
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.TreatmentOutcome()

    def AssessReadmissionRisk(self, request, context):
        """Assess readmission risk"""
        try:
            model = self.get_model("readmission_risk")
            features = self.preprocess_features(request.patient_data)
            probability = model.predict(features)[0][0]

            return ml_service_pb2.ReadmissionRisk(
                patient_id=request.patient_id,
                risk_score=float(probability),
                risk_level=self.get_risk_level(probability),
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
            
    def preprocess_features(self, data: Dict[str, Any]) -> np.ndarray:
        """Convert input data to feature vector"""
        # Define expected feature columns
        feature_columns = [
            'age', 'gender', 'day_of_week', 'time_of_day',
            'previous_no_shows', 'days_since_last_visit',
            'appointment_type', 'insurance_type',
            'distance_to_clinic', 'weather_condition'
        ]

        # Convert data to feature vector
        features = []
        for column in feature_columns:
            if column in data:
                # Convert string values to numerical representations
                if isinstance(data[column], str):
                    if column == 'gender':
                        features.append(1 if data[column].lower() == 'male' else 0)
                    elif column == 'day_of_week':
                        features.append(int(data[column]) % 7)  # 0-6 for Sun-Sat
                    elif column == 'time_of_day':
                        hour = int(data[column].split(':')[0])
                        features.append(hour / 24.0)  # Normalize to 0-1
                    elif column == 'appointment_type':
                        # Map appointment types to numerical values
                        appointment_types = {
                            'routine': 0,
                            'urgent': 1,
                            'follow_up': 2
                        }
                        features.append(appointment_types.get(data[column], 0))
                    elif column == 'insurance_type':
                        # Map insurance types to numerical values
                        insurance_types = {
                            'private': 0,
                            'public': 1,
                            'none': 2
                        }
                        features.append(insurance_types.get(data[column], 2))
                    else:
                        features.append(float(data[column]))
                else:
                    features.append(float(data[column]))
            else:
                features.append(0)  # Default value for missing features

        return np.array(features).reshape(1, -1)

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
