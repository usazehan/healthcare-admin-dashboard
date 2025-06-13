# Healthcare ML Prediction Service

This service is responsible for making predictions based on input data. It uses machine learning models trained by the model-service. It communicates with the Spring Boot FHIR API (through gRPC or REST) to receive prediction requests and sends predictions back to the Spring Boot FHIR API to be used by the frontend. It also sends data to the analytics-service to track model performance, drift, etc.

## Key Interactions

- **Prediction Service to Model Service**: The prediction-service requests models or model predictions from the model-service.
- **Prediction Service to FHIR API**: The prediction-service sends predictions to the FHIR API to be used by the frontend.
- **Prediction Service to Analytics Service**: The prediction-service sends data to the analytics-service to track model performance, drift, etc.

## Getting Started

### Prerequisites

- Docker
- Python 3.9

### Installation

1. Clone the repository
2. Navigate to the `backend/ml/prediction-service` directory
3. Build the Docker image

```bash
docker build -t healthcare-ml-prediction-service .
```

### Running the Service

1. Start the service using Docker

```bash
docker run -p 8001:8001 healthcare-ml-prediction-service
```

2. The service will be available at:
   - REST: `localhost:8001`

### Environment Variables

Create a `.env` file in the root of the project with the following variables:

```env
# Model service connection string
MODEL_SERVICE_URL=http://localhost:50051

# FHIR API connection string
FHIR_API_URL=http://localhost:8080

# Analytics service connection string
ANALYTICS_SERVICE_URL=http://localhost:8002
```

### API Endpoints

#### REST

- `POST /predict/no-show`: Predict no-show probability for an appointment
- `POST /predict/treatment-outcome`: Predict treatment outcome
- `POST /predict/readmission-risk`: Assess readmission risk

### Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
