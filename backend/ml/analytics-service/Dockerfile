FROM python:3.9-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application
COPY . .

# Generate gRPC code
RUN python -m grpc_tools.protoc \
    -I../../shared/protos \
    --python_out=. \
    --grpc_python_out=. \
    ../../shared/protos/analytics/v1/analytics.proto

# Expose the gRPC port
EXPOSE 50052

# Run the service
CMD ["python", "src/analytics_service.py"]
