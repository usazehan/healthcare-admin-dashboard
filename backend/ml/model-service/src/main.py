from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Dict, List, Any
import mlflow
from models.no_show_model import NoShowPredictionModel

app = FastAPI(title="Healthcare ML Model Service")

# Initialize models
no_show_model = NoShowPredictionModel()

class PredictionRequest(BaseModel):
    patient_id: str
    features: Dict[str, Any]

class TrainingRequest(BaseModel):
    training_data: Dict[str, Any]
    validation_data: Dict[str, Any]
    model_version: str

@app.post("/models/no-show/predict")
async def predict_no_show(request: PredictionRequest):
    try:
        # Load the latest model version
        no_show_model.load_model("latest")
        
        # Make prediction
        prediction = no_show_model.predict(request.features)
        
        return {
            "patient_id": request.patient_id,
            "prediction": prediction
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/models/no-show/train")
async def train_no_show_model(request: TrainingRequest):
    try:
        # Train the model
        metrics = no_show_model.train(
            request.training_data,
            request.validation_data
        )
        
        # Save the model
        no_show_model.save_model(request.model_version)
        
        return {
            "model_version": request.model_version,
            "metrics": metrics
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/models/no-show/versions")
async def list_model_versions():
    try:
        versions = mlflow.search_model_versions(f"name='{no_show_model.model_name}'")
        return [{
            "version": v.version,
            "status": v.status,
            "run_id": v.run_id
        } for v in versions]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000) 