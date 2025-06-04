package com.healthcare.appointment.grpc

import com.healthcare.appointment.model.Appointment
import com.healthcare.appointment.model.AppointmentStatus
import com.healthcare.appointment.model.AppointmentType
import com.healthcare.appointment.v1.Appointment as AppointmentProto
import com.healthcare.appointment.v1.AppointmentStatus as AppointmentStatusProto
import com.healthcare.appointment.v1.AppointmentType as AppointmentTypeProto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Appointment.toProto(): AppointmentProto {
    return AppointmentProto.newBuilder()
            .setId(id.toString())
            .setPatientId(patientId)
            .setProviderId(providerId)
            .setStartTime(startTime.toString())
            .setEndTime(endTime.plusMinutes(30).toString())
            .setType(type.toProto())
            .setStatus(status.toProto())
            .setNotes(notes ?: "")
            .setCreatedAt(createdAt?.toString() ?: "")
            .setUpdatedAt(updatedAt?.toString() ?: "")
            .build()
}

fun AppointmentProto.toDomain(): Appointment {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    return Appointment(
            id = id.toLong(),
            patientId = patientId,
            providerId = providerId,
            startTime = LocalDateTime.parse(startTime, formatter),
            endTime = LocalDateTime.parse(endTime, formatter),
            status = status.toDomain(),
            createdAt = if (createdAt.isNotEmpty()) LocalDateTime.parse(createdAt, formatter) else null,
            updatedAt = if (updatedAt.isNotEmpty()) LocalDateTime.parse(updatedAt, formatter) else null,
            type = type.toDomain(),
            notes = notes,
            reason = reason
    )
}

fun AppointmentStatus.toProto(): AppointmentStatusProto {
    return when (this) {
        AppointmentStatus.SCHEDULED -> AppointmentStatusProto.APPOINTMENT_STATUS_SCHEDULED
        AppointmentStatus.CONFIRMED -> AppointmentStatusProto.APPOINTMENT_STATUS_CONFIRMED
        AppointmentStatus.COMPLETED -> AppointmentStatusProto.APPOINTMENT_STATUS_COMPLETED
        AppointmentStatus.CANCELLED -> AppointmentStatusProto.APPOINTMENT_STATUS_CANCELLED
        AppointmentStatus.NO_SHOW -> AppointmentStatusProto.APPOINTMENT_STATUS_NO_SHOW
    }
}

fun AppointmentStatusProto.toDomain(): AppointmentStatus {
    return when (this) {
        AppointmentStatusProto.APPOINTMENT_STATUS_SCHEDULED -> AppointmentStatus.SCHEDULED
        AppointmentStatusProto.APPOINTMENT_STATUS_CONFIRMED -> AppointmentStatus.CONFIRMED
        AppointmentStatusProto.APPOINTMENT_STATUS_COMPLETED -> AppointmentStatus.COMPLETED
        AppointmentStatusProto.APPOINTMENT_STATUS_CANCELLED -> AppointmentStatus.CANCELLED
        AppointmentStatusProto.APPOINTMENT_STATUS_NO_SHOW -> AppointmentStatus.NO_SHOW
        AppointmentStatusProto.APPOINTMENT_STATUS_UNSPECIFIED,
        AppointmentStatusProto.UNRECOGNIZED -> throw IllegalArgumentException("Invalid AppointmentStatus: $this")
    }
}

fun AppointmentType.toProto(): AppointmentTypeProto {
    return when (this) {
        AppointmentType.APPOINTMENT_TYPE_CONSULTATION -> AppointmentTypeProto.APPOINTMENT_TYPE_CONSULTATION
        AppointmentType.APPOINTMENT_TYPE_FOLLOW_UP -> AppointmentTypeProto.APPOINTMENT_TYPE_FOLLOW_UP
        AppointmentType.APPOINTMENT_TYPE_CHECK_UP -> AppointmentTypeProto.APPOINTMENT_TYPE_CHECK_UP
        AppointmentType.APPOINTMENT_TYPE_EMERGENCY -> AppointmentTypeProto.APPOINTMENT_TYPE_EMERGENCY
        AppointmentType.APPOINTMENT_TYPE_SPECIALIST -> AppointmentTypeProto.APPOINTMENT_TYPE_SPECIALIST
    }
}

fun AppointmentTypeProto.toDomain(): AppointmentType {
    return when (this) {
        AppointmentTypeProto.APPOINTMENT_TYPE_CONSULTATION -> AppointmentType.APPOINTMENT_TYPE_CONSULTATION
        AppointmentTypeProto.APPOINTMENT_TYPE_FOLLOW_UP -> AppointmentType.APPOINTMENT_TYPE_FOLLOW_UP
        AppointmentTypeProto.APPOINTMENT_TYPE_CHECK_UP -> AppointmentType.APPOINTMENT_TYPE_CHECK_UP
        AppointmentTypeProto.APPOINTMENT_TYPE_EMERGENCY -> AppointmentType.APPOINTMENT_TYPE_EMERGENCY
        AppointmentTypeProto.APPOINTMENT_TYPE_SPECIALIST -> AppointmentType.APPOINTMENT_TYPE_SPECIALIST
        AppointmentTypeProto.APPOINTMENT_TYPE_UNSPECIFIED,
        AppointmentTypeProto.UNRECOGNIZED -> throw IllegalArgumentException("Invalid AppointmentType: $this")
    }
}
