# Healthcare Analytics Service

This service is responsible for analyzing data related to the model and predictions. It handles tasks such as:

- Feature pipeline processing (transforming raw data into features suitable for the model)
- Detecting model drift (changes in model performance over time)
- Calculating metrics related to model performance

## Getting Started

### Prerequisites

- Docker
- Python 3.9

### Installation

1. Clone the repository
2. Navigate to the `backend/ml/analytics-service` directory
3. Build the Docker image

```bash
docker build -t healthcare-analytics-service .
```

### Running the Service

1. Start the service using Docker

```bash
docker run -p 50052:50052 -p 8001:8001 healthcare-analytics-service
```

2. The service will be available at:
   - gRPC: `localhost:50052`
   - REST API: `localhost:8001`

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

- `GetPatientRiskPredictions`: Get patient risk predictions
- `GetResourceUtilizationPredictions`: Get resource utilization predictions
- `GetAppointmentNoShowPredictions`: Get appointment no-show predictions

#### REST API

- `POST /models/analytics/predict`: Make a prediction
- `POST /models/analytics/train`: Train the model
- `GET /models/analytics/versions`: List model versions

### Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
