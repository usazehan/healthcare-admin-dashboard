import os
import threading
from concurrent import futures
import datetime

import grpc
from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import Dict, Any
import uvicorn
import requests

import ml_service_pb2
import ml_service_pb2_grpc
from models.no_show_model import NoShowPredictionModel

import logging
LOGGER = logging.getLogger("prediction_service")

app = FastAPI(title="Healthcare ML Prediction Service")

# ---- Load the ML model once at startup ----
MODEL_NAME = os.getenv("MODEL_NAME", "no-show")
MODEL_VERSION = os.getenv("MODEL_VERSION", "latest")
no_show_model = NoShowPredictionModel()
no_show_model.load_model(MODEL_NAME, MODEL_VERSION)


# ---- REST request/response schema ----
class PredictionRequest(BaseModel):
    patient_id: str
    features: Dict[str, Any]

# ---- Background task to report predictions to analytics ----
async def report_to_analytics(patient_id: str, probability: float, risk_level: str):
    """
    Send prediction results to an analytics service.
    This is a placeholder function; implement actual reporting logic.
    """
    url = os.getenv("ANALYTICS_URL", "http://analytics-service:6562")
    payload = {
        "appointment_id": patient_id,
        "prediction_time": datetime.utcnow().isoformat(),
        "no_show_probability": probability,
        "risk_level": risk_level,
    }
    try:
        # fire-and-forget; you could add retries here
        requests.post(f"{url}/analytics/predictions", json=payload, timeout=2)
    except Exception as e:
        # log and swallow so it doesn’t block your response
        LOGGER.warning(f"Failed to report to analytics: {e}")

# ---- REST API endpoints ----
@app.post("/predict/no-show")
async def predict_no_show(
    request: PredictionRequest,
    bg_tasks: BackgroundTasks
):
    """
    Receive JSON { patient_id, features } and return
    { patient_id, probability, risk_level, confidence }.
    """
    try:
        result = no_show_model.predict(request.features)
        # enqueue the analytics report
        bg_tasks.add_task(
            report_to_analytics,
            request.patient_id,
            result["no_show_probability"],
            result["risk_level"],
        )
        return {
            "patient_id": request.patient_id,
            "probability": result["no_show_probability"],
            "risk_level": result["risk_level"],
            "confidence": 0.85,  # or pull from result if available
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {e}")
@app.get("/health")
def health():
    # Check if the model is loaded
    model_status = "loaded" if no_show_model.is_loaded() else "not loaded"
    
    return {
        "status": "ok",
        "model_status": model_status
    }

# ---- gRPC Servicer (optional) ----
class MLServiceServicer(ml_service_pb2_grpc.MLServiceServicer):
    def PredictNoShow(self, request, context):
        try:
            # Use the same in-memory model for gRPC clients
            pred = no_show_model.predict(request.features)
            # Map your risk_level string to the protobuf enum
            risk_enum = ml_service_pb2.RiskLevel.RISK_LEVEL_LOW
            if pred["risk_level"] == "Medium":
                risk_enum = ml_service_pb2.RiskLevel.RISK_LEVEL_MEDIUM
            elif pred["risk_level"] == "High":
                risk_enum = ml_service_pb2.RiskLevel.RISK_LEVEL_HIGH

            return ml_service_pb2.NoShowPrediction(
                patient_id=request.patient_id,
                probability=float(pred["no_show_probability"]),
                risk_level=risk_enum,
                confidence=0.85,
            )
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return ml_service_pb2.NoShowPrediction()


def serve_grpc():
    grpc_port = int(os.getenv("GRPC_PORT", "50051"))
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ml_service_pb2_grpc.add_MLServiceServicer_to_server(MLServiceServicer(), server)
    server.add_insecure_port(f"[::]:{grpc_port}")
    server.start()
    LOGGER.info(f"gRPC server started on port {grpc_port}")
    server.wait_for_termination()


if __name__ == "__main__":
    # Start gRPC server in the background
    threading.Thread(target=serve_grpc, daemon=True).start()

    # Start FastAPI REST server
    rest_host = os.getenv("REST_HOST", "0.0.0.0")
    rest_port = int(os.getenv("REST_PORT", "8001"))
    uvicorn.run(app, host=rest_host, port=rest_port)
