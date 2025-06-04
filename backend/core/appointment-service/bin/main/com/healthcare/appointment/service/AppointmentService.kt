package com.healthcare.appointment.service

import com.healthcare.appointment.model.Appointment
import com.healthcare.appointment.model.AppointmentStatus
import com.healthcare.appointment.repository.AppointmentRepository
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppointmentService(
        private val appointmentRepository: AppointmentRepository
) {
    @Transactional
    suspend fun createAppointment(appointment: Appointment): Appointment {
        return appointmentRepository.save(appointment)
    }

    @Transactional
    suspend fun updateAppointment(id: Long, appointment: Appointment): Appointment? {
        return appointmentRepository
                .findById(id)
                .map { it.copy(
                        patientId = appointment.patientId,
                        providerId = appointment.providerId,
                        startTime = appointment.startTime,
                        endTime = appointment.endTime,
                        type = appointment.type,
                        notes = appointment.notes,
                        reason = appointment.reason
                ) }
                .map { appointmentRepository.save(it) }
                .orElse(null)
    }

    fun getAppointment(id: Long): Appointment? = appointmentRepository.findById(id).orElse(null)

    @Transactional
    fun updateAppointmentStatus(id: Long, status: AppointmentStatus): Appointment? {
        return appointmentRepository
                .findById(id)
                .map { it.copy(status = status) }
                .map { appointmentRepository.save(it) }
                .orElse(null)
    }

    fun listAppointmentsInRange(
            start: LocalDateTime,
            end: LocalDateTime,
            pageIndex: Int,
            pageSize: Int
    ): Page<Appointment> {
        val pageable = PageRequest.of(pageIndex, pageSize, Sort.by("startTime").descending())
        return appointmentRepository.findByStartTimeBetween(start, end, pageable)
    }
}
