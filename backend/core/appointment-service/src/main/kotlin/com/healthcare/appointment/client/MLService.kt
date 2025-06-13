package com.healthcare.appointment.client

import com.healthcare.ml.v1.MLServiceGrpc
import com.healthcare.ml.v1.PredictNoShowRequest
import com.healthcare.ml.v1.NoShowPrediction
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MLService(
    @Value("\${ml.service.url}") private val mlServiceUrl: String,
    @Value("\${ml.service.port}") private val mlServicePort: Int
) {

    private val channel = ManagedChannelBuilder.forAddress(mlServiceUrl, mlServicePort)
        .usePlaintext()
        .build()

    private val stub = MLServiceGrpc.newBlockingStub(channel)

    fun predictNoShow(patientId: String, features: Map<String, String>): NoShowPrediction {
        val request = PredictNoShowRequest.newBuilder()
            .setPatientId(patientId)
            .putAllAdditionalData(features)
            .build()

        return stub.predictNoShow(request)
    }
}
