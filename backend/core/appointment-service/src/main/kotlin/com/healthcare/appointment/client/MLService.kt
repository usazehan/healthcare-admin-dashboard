package com.healthcare.appointment.client

import com.healthcare.appointment.v1.AppointmentType
import com.healthcare.ml.v1.MLServiceGrpc
import com.healthcare.ml.v1.NoShowPrediction
import com.healthcare.ml.v1.PredictNoShowRequest
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MLService(
        @Value("\${ml.service.url}") private val mlServiceUrl: String,
        @Value("\${ml.service.port}") private val mlServicePort: Int
) {

    private val channel =
            ManagedChannelBuilder.forAddress(mlServiceUrl, mlServicePort).usePlaintext().build()

    private val stub = MLServiceGrpc.newBlockingStub(channel)

    fun predictNoShow(
            patientId: String,
            providerId: String,
            appointmentId: String,
            startTime: String,
            appointmentType: AppointmentType,
            features: Map<String, String>
    ): NoShowPrediction {
        val request =
                PredictNoShowRequest.newBuilder()
                        .setPatientId(patientId)
                        .setAppointmentId(appointmentId)
                        .setStartTime(startTime)
                        .setProviderId(providerId)
                        .setType(appointmentType)
                        .putAllAdditionalData(features)
                        .build()

        return stub.predictNoShow(request)
    }
}
