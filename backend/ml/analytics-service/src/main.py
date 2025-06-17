from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Dict, Any
import mlflow
import logging
from models.analytics_model import AnalyticsModel

app = FastAPI(title="Healthcare Analytics Service")
LOGGER = logging.getLogger("analytics_service")

# Initialize models
analytics_model = AnalyticsModel()

class PredictionRequest(BaseModel):
    patient_id: str
    features: Dict[str, Any]

class TrainingRequest(BaseModel):
    training_data: Dict[str, Any]
    validation_data: Dict[str, Any]
    model_version: str

@app.post("/models/analytics/predict")
async def predict_analytics(request: PredictionRequest):
    try:
        # Load the latest model version
        analytics_model.load_model("latest")

        # Make prediction
        prediction = analytics_model.predict(request.features)

        return {
            "patient_id": request.patient_id,
            "prediction": prediction
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/models/analytics/train")
async def train_analytics_model(request: TrainingRequest):
    try:
        # Train the model
        metrics = analytics_model.train(
            request.training_data,
            request.validation_data
        )

        # Save the model
        analytics_model.save_model(request.model_version)

        return {
            "model_version": request.model_version,
            "metrics": metrics
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/models/analytics/versions")
async def list_model_versions():
    try:
        versions = mlflow.search_model_versions(f"name='{analytics_model.model_name}'")
        return [{
            "version": v.version,
            "status": v.status,
            "run_id": v.run_id
        } for v in versions]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


class IngestPredictionRequest(BaseModel):
    appointment_id: str
    prediction_time: str        # ISO8601 timestamp from the Prediction Service
    no_show_probability: float
    risk_level: str

@app.post("/analytics/predictions", status_code=202)
async def ingest_prediction(event: IngestPredictionRequest):
    """
    Receive a prediction event from Prediction Service and
    forward it into the AnalyticsModel (e.g. DB or time-series store).
    """
    try:
        LOGGER.info("Received prediction event", extra=event.dict())
        # Here you would call into your analytics_model to store it.
        analytics_model.record_prediction(
            appointment_id=event.appointment_id,
            prediction_time=event.prediction_time,
            no_show_probability=event.no_show_probability,
            risk_level=event.risk_level
        )
        return {"status": "accepted"}
    except Exception as e:
        LOGGER.error(f"Failed to ingest prediction: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Could not ingest prediction")
    
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
