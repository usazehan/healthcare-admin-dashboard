package com.healthcare.appointment.validation

import com.healthcare.appointment.grpc.toDomain
import com.healthcare.appointment.model.Appointment
import com.healthcare.appointment.v1.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object AppointmentValidator {

    fun CreateAppointmentRequest.toDomainValidated(): Appointment {
        AppointmentValidator.validateCreateRequest(this)
        return this.toDomain()
    }

    fun CreateAppointmentRequest.toDomain(): Appointment {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val start = OffsetDateTime.parse(this.startTime, formatter).toLocalDateTime()
        val end = OffsetDateTime.parse(this.endTime, formatter).toLocalDateTime()
        return Appointment(
                patientId = this.patientId,
                providerId = this.providerId,
                startTime = start,
                endTime = end,
                type = this.type.toDomain(),
                notes = this.notes.takeIf { it.isNotBlank() },
                reason = this.reason.takeIf { it.isNotBlank() }
        )
    }

    fun validateCreateRequest(request: CreateAppointmentRequest) {
        require(request.patientId.isNotBlank()) { "Patient ID is required." }
        require(request.providerId.isNotBlank()) { "Provider ID is required." }
        require(request.type != AppointmentType.APPOINTMENT_TYPE_UNSPECIFIED) {
            "Appointment type is required."
        }

        val startTime = parseDateTime(request.startTime, "Start time")
        val endTime = parseDateTime(request.endTime, "End time")

        require(endTime.isAfter(startTime)) { "End time must be after start time." }
        require(startTime.isAfter(OffsetDateTime.now())) { "Start time must be in the future." }
        require(endTime.isAfter(OffsetDateTime.now())) { "End time must be in the future." }

        if (request.reason.length > 500) {
            throw IllegalArgumentException("Reason is too long.")
        }
        if (request.notes.length > 1000) {
            throw IllegalArgumentException("Notes are too long.")
        }
    }
    

    fun validateUpdateRequest(request: UpdateAppointmentRequest) {
        require(request.id.isNotBlank()) { "Appointment ID is required." }
        require(request.type != AppointmentType.APPOINTMENT_TYPE_UNSPECIFIED) {
            "Appointment type is required."
        }

        val startTime = parseDateTime(request.startTime, "Start time")
        val endTime = parseDateTime(request.endTime, "End time")

        require(endTime.isAfter(startTime)) { "End time must be after start time." }
        require(startTime.isAfter(OffsetDateTime.now())) { "Start time must be in the future." }
        require(endTime.isAfter(OffsetDateTime.now())) { "End time must be in the future." }

        if (request.reason.length > 500) {
            throw IllegalArgumentException("Reason is too long.")
        }
        if (request.notes.length > 1000) {
            throw IllegalArgumentException("Notes are too long.")
        }
    }

    fun validateCancelRequest(request: CancelAppointmentRequest) {
        require(request.id.isNotBlank()) { "Appointment ID is required." }
        require(request.cancellationReason.isNotBlank()) { "Cancellation reason is required." }
        if (request.cancellationReason.length > 500) {
            throw IllegalArgumentException("Cancellation reason is too long.")
        }
    }

    fun validateGetRequest(request: GetAppointmentRequest) {
        require(request.id.isNotBlank()) { "Appointment ID is required." }
    }

    fun validateListRequest(request: ListAppointmentsRequest) {
        require(request.patientId.isNotBlank() || request.providerId.isNotBlank()) {
            "Either Patient ID or Provider ID is required."
        }
        if (request.pagination.pageSize <= 0) {
            throw IllegalArgumentException("Page size must be greater than zero.")
        }
    }

    private fun parseDateTime(dateTimeStr: String, fieldName: String): OffsetDateTime {
        try {
            return OffsetDateTime.parse(dateTimeStr)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("$fieldName is not a valid ISO date-time.")
        }
    }
}
