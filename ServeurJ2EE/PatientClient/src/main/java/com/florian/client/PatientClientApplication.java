package com.florian.client;




import com.florian.entity.Genre;
import com.florian.entity.Personne;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.hateoas.Resources;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;



import java.util.*;

/**
 * Class permettant de faire le lien entre les appel exterrieur et les appel interne
 */

@EnableResourceServer
@EnableBinding(PatientChannels.class)
@EnableFeignClients
@EnableZuulProxy
@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication
@IntegrationComponentScan
public class  PatientClientApplication {


	public static void main(String[] args) {


		SpringApplication.run(PatientClientApplication.class, args);
	}
}








