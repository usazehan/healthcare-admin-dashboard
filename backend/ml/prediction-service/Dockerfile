FROM python:3.9-slim

WORKDIR /app

# System deps
RUN apt-get update && apt-get install -y build-essential && \
    rm -rf /var/lib/apt/lists/*

# Python deps
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy code
COPY . .

# Generate gRPC code
RUN python -m grpc_tools.protoc \
    -I../../shared/protos \
    --python_out=. \
    --grpc_python_out=. \
    ../../shared/protos/ml/v1/ml_service.proto

    
# Expose ports for the service
EXPOSE 8001
EXPOSE 50051

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8001"]