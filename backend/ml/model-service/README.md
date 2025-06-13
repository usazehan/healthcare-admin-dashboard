# Healthcare ML Model Service

This service manages the training and versioning of machine learning models. It interacts with:

- The analytics-service to receive data for model training
- The prediction-service to deploy new models or model versions
- It stores models in a model registry

## Key Interactions

- **Analytics Service to Model Service**: The analytics-service triggers model retraining in the model-service when drift or performance issues are detected
- **Model Service to Prediction Service**: The model-service deploys new models or model versions to the prediction-service

## Getting Started

### Prerequisites

- Docker
- Python 3.9

### Installation

1. Clone the repository
2. Navigate to the `backend/ml/model-service` directory
3. Build the Docker image

```bash
docker build -t healthcare-ml-model-service .
```

### Running the Service

1. Start the service using Docker

```bash
docker run -p 50051:50051 healthcare-ml-model-service
```

2. The service will be available at:
   - gRPC: `localhost:50051`

### Environment Variables

Create a `.env` file in the root of the project with the following variables:

```env
# MLflow tracking URI
MLFLOW_TRACKING_URI=http://localhost:5000

# Database connection string
DATABASE_URL=postgresql://user:password@localhost:5432/healthcare

# InfluxDB connection string
INFLUXDB_URL=http://localhost:8086
INFLUXDB_TOKEN=my-token
INFLUXDB_ORG=my-org
INFLUXDB_BUCKET=healthcare
```

### API Endpoints

#### gRPC

- `PredictNoShow`: Predict no-show probability for an appointment
- `PredictTreatmentOutcome`: Predict treatment outcome
- `AssessReadmissionRisk`: Assess readmission risk
- `GetTreatmentRecommendations`: Get treatment recommendations
- `AnalyzeDrugInteractions`: Analyze drug interactions

### Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
