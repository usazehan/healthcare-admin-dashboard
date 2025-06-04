package com.healthcare.appointment.config

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy
import com.healthcare.appointment.grpc.AppointmentGrpcService

@Configuration
class GrpcServerConfig {

    @Value("\${grpc.server.port:9090}")
    private val grpcPort: Int = 9090

    private var server: Server? = null

    @Bean
    fun grpcServer(appointmentGrpcService: AppointmentGrpcService): Server {
        val server = ServerBuilder.forPort(grpcPort)
            .addService(appointmentGrpcService)
            .addService(ProtoReflectionService.newInstance())
            .build()
            .start()
        
        println("gRPC Server started, listening on port $grpcPort")
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down gRPC server")
            server.shutdown()
        })
        
        this.server = server
        return server
    }

    @PreDestroy
    fun stopServer() {
        server?.shutdown()
    }
} 