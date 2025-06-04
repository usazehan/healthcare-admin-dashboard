package com.healthcare.appointment.model

import jakarta.persistence.*
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "appointments")
@EntityListeners(AuditingEntityListener::class)
data class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val patientId: String,

    @Column(nullable = false)
    val providerId: String,

    // split your single scheduledTime into two fields:
    @Column(nullable = false)
    val startTime: LocalDateTime,

    @Column(nullable = false)
    val endTime: LocalDateTime,

    @Column(nullable = false)
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,

    @Column(nullable = false)
    val type: AppointmentType = AppointmentType.APPOINTMENT_TYPE_CONSULTATION,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(columnDefinition = "TEXT")
    val reason: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,           // auto-set on insert

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null            // auto-set on update
) {
    // No-args constructor for JPA
    constructor() : this(
        0,
        "",
        "",
        LocalDateTime.now(),
        LocalDateTime.now(),
        AppointmentStatus.SCHEDULED,
        AppointmentType.APPOINTMENT_TYPE_CONSULTATION,
        null,
        null,
        null,
        null
    )
}

enum class AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

enum class AppointmentType {
    APPOINTMENT_TYPE_CONSULTATION,
    APPOINTMENT_TYPE_FOLLOW_UP,
    APPOINTMENT_TYPE_CHECK_UP,
    APPOINTMENT_TYPE_EMERGENCY,
    APPOINTMENT_TYPE_SPECIALIST
}
