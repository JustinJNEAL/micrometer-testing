package com.fis.entapi.services.micrometer;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

@SpringBootApplication
public class MicrometerTestingApplication {

    public static void main(String[] args) {

        SpringApplication.run(MicrometerTestingApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(RestTemplate restTemplate) {

        return args -> restTemplate.getForObject("https://www.google.com", String.class);
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder, Tracer tracer, ObservationRegistry observationRegistry) {

        return builder
                .interceptors(new MicrometerInterceptor(tracer, observationRegistry))
                .build();
    }

}

@RequiredArgsConstructor
class MicrometerInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, @NotNull byte[] bytes,
                                        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

        var header = "testHeader";

        Optional.ofNullable(tracer.currentSpan())
                .ifPresent(currentSpan -> currentSpan.name(header));

        Optional.ofNullable(observationRegistry.getCurrentObservation())
                .ifPresent(observation -> observation.contextualName(header));

        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }

}
