from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Dict, Any
import grpc
import ml_service_pb2
import ml_service_pb2_grpc
from models.no_show_model import NoShowPredictionModel

app = FastAPI(title="Healthcare ML Prediction Service")

# Initialize models
no_show_model = NoShowPredictionModel()

# Initialize gRPC client
class GRPCClient:
    def __init__(self, host: str = "localhost", port: int = 50051):
        self.channel = grpc.insecure_channel(f"{host}:{port}")
        self.stub = ml_service_pb2_grpc.MLServiceStub(self.channel)

    def predict_no_show(self, patient_id: str, features: Dict[str, Any]) -> Dict[str, Any]:
        request = ml_service_pb2.NoShowPredictionRequest(
            patient_id=patient_id,
            features=features
        )
        response = self.stub.PredictNoShow(request)
        return {
            "patient_id": response.patient_id,
            "probability": response.probability,
            "risk_level": response.risk_level,
            "confidence": response.confidence
        }

grpc_client = GRPCClient()

class PredictionRequest(BaseModel):
    patient_id: str
    features: Dict[str, Any]

@app.post("/predict/no-show")
async def predict_no_show(request: PredictionRequest):
    try:
        result = grpc_client.predict_no_show(
            request.patient_id,
            request.features
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class MLService(ml_service_pb2_grpc.MLServiceServicer):
    def PredictNoShow(self, request, context):
        """Predict no-show probability"""
        try:
            features = request.features
            prediction = no_show_model.predict(features)

            risk_level = ml_service_pb2.RiskLevel.RISK_LEVEL_LOW
            if prediction['risk_level'] == "Medium":
                risk_level = ml_service_pb2.RiskLevel.RISK_LEVEL_MEDIUM
            elif prediction['risk_level'] == "High":
                risk_level = ml_service_pb2.RiskLevel.RISK_LEVEL_HIGH

            return ml_service_pb2.NoShowPrediction(
                patient_id=request.patient_id,
                probability=float(prediction['no_show_probability']),
                risk_level=risk_level,
                confidence=0.85  # Placeholder
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.NoShowPrediction()

if __name__ == "__main__":
    import uvicorn
    from concurrent import futures
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ml_service_pb2_grpc.add_MLServiceServicer_to_server(MLService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("gRPC server started on port 50051")
    uvicorn.run(app, host="0.0.0.0", port=8000)
