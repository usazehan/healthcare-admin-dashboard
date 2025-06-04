package com.healthcare.appointment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class AppointmentServiceApplication

fun main(args: Array<String>) {
    runApplication<AppointmentServiceApplication>(*args)
} 