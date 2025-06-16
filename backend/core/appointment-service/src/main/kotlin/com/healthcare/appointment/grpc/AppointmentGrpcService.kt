package com.healthcare.appointment.grpc

import com.healthcare.appointment.client.MLService
import com.healthcare.appointment.model.Appointment
import com.healthcare.appointment.model.AppointmentStatus
import com.healthcare.appointment.service.AppointmentService
import com.healthcare.appointment.util.DEFAULT_PAGE_SIZE
import com.healthcare.appointment.util.PaginationUtils
import com.healthcare.appointment.v1.*
import com.healthcare.appointment.v1.Appointment as ProtoAppointment
import com.healthcare.appointment.v1.AppointmentServiceGrpc
import com.healthcare.common.v1.PaginationResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

/**
 * gRPC service implementation for appointment management. Handles appointment creation and other
 * appointment-related operations.
 */
@Service
class AppointmentGrpcService(
        private val appointmentService: AppointmentService,
        private val MLService: MLService
) : AppointmentServiceGrpc.AppointmentServiceImplBase() {

    /** Creates a new appointment based on the provided request. */
    override fun createAppointment(
            request: CreateAppointmentRequest,
            responseObserver: StreamObserver<ProtoAppointment>
    ) {
        try {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val start = LocalDateTime.parse(request.startTime, formatter)
            val end = LocalDateTime.parse(request.endTime, formatter)
            val domainAppointment =
                    Appointment(
                            patientId = request.patientId,
                            providerId = request.providerId,
                            startTime = start,
                            endTime = end,
                            type = request.type.toDomain(),
                            notes = request.notes,
                            reason = request.reason,
                            status = AppointmentStatus.SCHEDULED,
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now(),
                    )

            val result = runBlocking { appointmentService.createAppointment(domainAppointment) }
            responseObserver.onNext(result.toProto())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error creating appointment: ${e.message}")
                            .asRuntimeException()
            )
        }
    }

    override fun getAppointment(
            request: GetAppointmentRequest,
            responseObserver: StreamObserver<ProtoAppointment>
    ) {
        try {
            val appointment = appointmentService.getAppointment(request.id.toLong())

            if (appointment == null) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Appointment not found with ID: ${request.id}")
                                .asRuntimeException()
                )
                return
            }

            responseObserver.onNext(appointment.toProto())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error fetching appointment: ${e.message}")
                            .asRuntimeException()
            )
        }
    }

    override fun listAppointments(
            request: ListAppointmentsRequest,
            responseObserver: StreamObserver<ListAppointmentsResponse>
    ) {
        try {
            // 1) Parse dates
            val start =
                    LocalDateTime.parse(
                            request.dateRange.startDate,
                            DateTimeFormatter.ISO_DATE_TIME
                    )
            val end =
                    LocalDateTime.parse(request.dateRange.endDate, DateTimeFormatter.ISO_DATE_TIME)

            // 2) Parse pageToken/pageSize
            val pageSize = request.pagination.pageSize.takeIf { it > 0 } ?: DEFAULT_PAGE_SIZE
            val pageIndex = PaginationUtils.decodePageToken(request.pagination.pageToken)

            // 3) Delegate to domain service
            val page = appointmentService.listAppointmentsInRange(start, end, pageIndex, pageSize)

            // 4) Build PaginationResponse
            val paginationProto =
                    PaginationResponse.newBuilder()
                            .setTotalCount(page.totalElements.toInt())
                            .setNextPageToken(PaginationUtils.encodePageToken(pageIndex + 1))
                            .setHasMore(page.hasNext())
                            .build()

            // 5) Map domain → proto and return
            val response =
                    ListAppointmentsResponse.newBuilder()
                            .addAllAppointments(page.content.map { it.toProto() })
                            .setPagination(paginationProto)
                            .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error listing appointments: ${e.message}")
                            .asRuntimeException()
            )
        }
    }

    override fun updateAppointment(
            request: UpdateAppointmentRequest,
            responseObserver: StreamObserver<ProtoAppointment>
    ) {
        try {
            val existingAppointment = appointmentService.getAppointment(request.id.toLong())

            if (existingAppointment == null) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Appointment not found with ID: ${request.id}")
                                .asRuntimeException()
                )
                return
            }

            val updatedAppointment =
                    existingAppointment.copy(
                            startTime =
                                    LocalDateTime.parse(
                                            request.startTime,
                                            DateTimeFormatter.ISO_DATE_TIME
                                    ),
                            endTime =
                                    LocalDateTime.parse(
                                            request.endTime,
                                            DateTimeFormatter.ISO_DATE_TIME
                                    ),
                            type = request.type.toDomain(),
                            notes = request.notes,
                            reason = request.reason,
                            status = AppointmentStatus.SCHEDULED,
                            updatedAt = LocalDateTime.now(),
                    )

            val result = runBlocking { appointmentService.createAppointment(updatedAppointment) }
            responseObserver.onNext(result.toProto())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error updating appointment: ${e.message}")
                            .asRuntimeException()
            )
        }
    }

    override fun cancelAppointment(
            request: CancelAppointmentRequest,
            responseObserver: StreamObserver<ProtoAppointment>
    ) {
        try {
            val appointment = appointmentService.getAppointment(request.id.toLong())

            if (appointment == null) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Appointment not found with ID: ${request.id}")
                                .asRuntimeException()
                )
                return
            }

            val updatedAppointment =
                    appointmentService.updateAppointmentStatus(
                            id = appointment.id,
                            status = AppointmentStatus.CANCELLED
                    )

            if (updatedAppointment == null) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("Failed to cancel appointment")
                                .asRuntimeException()
                )
                return
            }

            responseObserver.onNext(updatedAppointment.toProto())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error cancelling appointment: ${e.message}")
                            .asRuntimeException()
            )
        }
    }

    override fun changeAppointmentStatus(
            request: ChangeAppointmentStatusRequest,
            responseObserver: StreamObserver<ProtoAppointment>
    ) {
        try {
            val appointment = appointmentService.getAppointment(request.id.toLong())

            if (appointment == null) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Appointment not found with ID: ${request.id}")
                                .asRuntimeException()
                )
                return
            }

            val updatedAppointment =
                    appointmentService.updateAppointmentStatus(
                            id = appointment.id,
                            status = request.status.toDomain()
                    )

            if (updatedAppointment == null) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("Failed to change appointment status")
                                .asRuntimeException()
                )
                return
            }

            responseObserver.onNext(updatedAppointment.toProto())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error changing appointment status: ${e.message}")
                            .asRuntimeException()
            )
        }
    }

    override fun predictNoShow(
            request: PredictNoShowRequest,
            responseObserver: StreamObserver<PredictNoShowResponse>
    ) {
        try {
            // 1) Validate & load the appointment
            val appointment =
                    appointmentService.getAppointment(request.appointmentId.toLong())
                            ?: return responseObserver.onError(
                                    Status.NOT_FOUND
                                            .withDescription(
                                                    "Appointment not found: ${request.appointmentId}"
                                            )
                                            .asRuntimeException()
                            )

            // 2. Call ML service:
            val mlResp =
                    MLService.predictNoShow(
                            patientId = appointment.patientId,
                            providerId = appointment.providerId,
                            appointmentId = appointment.id.toString(),
                            startTime = appointment.startTime.toString(),
                            appointmentType = appointment.type.toProto(),
                            features =
                                    mapOf(
                                            "dayOfWeek" to appointment.startTime.dayOfWeek.name,
                                            "timeOfDay" to
                                                    appointment.startTime.toLocalTime().toString()
                                    )
                    )

            // 4) Map the ML response to your RPC response
            val grpcResp =
                    PredictNoShowResponse.newBuilder()
                            .setNoShowProbability(mlResp.probability)
                            .setRiskLevel(mlResp.riskLevel) // assuming same enum
                            .setRecommendation(mlResp.recommendation)
                            .putAllRiskFactors(mlResp.riskFactorsMap) // if you expose them
                            .build()

            // 5) Return it
            responseObserver.onNext(grpcResp)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            // Unexpected: wrap as INTERNAL
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error predicting no-show: ${e.message}")
                            .asRuntimeException()
            )
        }
    }
}
