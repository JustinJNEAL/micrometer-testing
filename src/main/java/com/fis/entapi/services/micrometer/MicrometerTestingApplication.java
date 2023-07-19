package com.fis.entapi.services.micrometer;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;

@SpringBootApplication
public class MicrometerTestingApplication {

    public static void main(String[] args) {

        SpringApplication.run(MicrometerTestingApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(RestTemplate restTemplate) {

        return args -> {

            var httpHeaders = new HttpHeaders();
            httpHeaders.set("testHeader", "header");

            var httpEntity = new HttpEntity<Void>(httpHeaders);

            restTemplate.exchange("https://www.google.com", GET, httpEntity, String.class);
        };
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {

        return builder
                .build();
    }

    @Bean
    DefaultClientRequestObservationConvention defaultClientRequestObservationConvention() {

        return new ExtendedClientRequestObservationConvention();
    }

}

class ExtendedClientRequestObservationConvention extends DefaultClientRequestObservationConvention {

    @Override
    public KeyValues getLowCardinalityKeyValues(ClientRequestObservationContext context) {

        return KeyValues.of(clientName(context), exception(context), method(context), outcome(context), status(context),
                subscriber(context), uri(context));
    }

    private KeyValue subscriber(ClientRequestObservationContext context) {

        if (context.getCarrier() != null) {

            var testHeader = Optional.ofNullable(context.getCarrier().getHeaders().get("testHeader"))
                    .orElseThrow(() -> new RuntimeException("missing header"))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("unable to get header"));

            return KeyValue.of("testHeader", testHeader);

        } else {

            return KeyValue.of("testHeader", KeyValue.NONE_VALUE);
        }
    }

}
