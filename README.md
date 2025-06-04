# Healthcare Admin Dashboard

A modern, microservices-based healthcare administration system with AI-powered no-show prediction capabilities. This system helps healthcare providers manage appointments efficiently and reduce no-show rates through predictive analytics.

## 🚀 Features

- **Appointment Management**: Create, view, and manage patient appointments
- **AI-Powered Predictions**: Predict potential no-shows using machine learning
- **Real-time Updates**: Get instant notifications for appointment changes
- **Secure Authentication**: Role-based access control for healthcare staff
- **Scalable Architecture**: Microservices-based design for easy scaling

## 🏗️ System Architecture

The system consists of the following microservices:

- **Appointment Service** (Kotlin Spring Boot)
  - Handles appointment CRUD operations
  - Manages patient scheduling
  - Integrates with ML service for predictions
  
- **ML Service** (Python FastAPI)
  - Provides no-show predictions
  - Processes historical appointment data
  - Exposes prediction API endpoints

- **PostgreSQL Database**
  - Stores appointment and patient data
  - Maintains service configurations
  - Handles data persistence

## 🛠️ Prerequisites

- Docker (version 20.10.0 or higher)
- Docker Compose (version 2.0.0 or higher)
- JDK 17
- Python 3.10
- Git

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/usazehan/healthcare-admin-dashboard.git
   cd healthcare-admin-dashboard
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Prepare data**
   ```bash
   mkdir -p data models
   # Download the No-Show Appointments dataset and place it in the data directory
   ```

4. **Build and run services**
   ```bash
   docker-compose up --build
   ```

## 📡 API Documentation

### Appointment Service (http://localhost:8080)

#### Appointment Management
- `POST /api/v1/appointments` - Create new appointment
- `GET /api/v1/appointments/{id}` - Get appointment details
- `GET /api/v1/appointments/patient/{patientId}` - Get patient's appointments
- `PUT /api/v1/appointments/{id}/status` - Update appointment status

#### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh access token

### ML Service (http://localhost:8000)

#### Predictions
- `POST /predict` - Get no-show prediction
- `GET /health` - Service health check

## 🔧 Development

### Project Structure
```
healthcare-admin-dashboard/
├── backend/
│   ├── core/
│   │   ├── appointment-service/
│   │   └── ml-service/
│   ├── shared/
│   │   └── protos/
│   └── docker/
├── data/
├── models/
└── docs/
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific service tests
./gradlew :appointment-service:test
```

### Code Style
- Kotlin: Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Python: Follow [PEP 8](https://peps.python.org/pep-0008/)

## 🔐 Security

- All sensitive data is stored in environment variables
- JWT-based authentication
- HTTPS encryption for all API endpoints
- Regular security audits and updates

## 📊 Monitoring

- Service health checks at `/health` endpoints
- Logging to `app_output.log`
- Metrics available at `/metrics` endpoints

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Your Name** - *Initial work* - [usazehan](https://github.com/usazehan)

## 🙏 Acknowledgments

- No-Show Appointments Dataset
- Spring Boot Team
- FastAPI Team
- PostgreSQL Team
