package com.healthcare.appointment.repository

import com.healthcare.appointment.model.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
interface AppointmentRepository : JpaRepository<Appointment, Long> {

    fun findByStartTimeBetween(
        start: LocalDateTime,
        end: LocalDateTime,
        pageable: Pageable
    ): Page<Appointment>
}
