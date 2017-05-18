package com.florian.client;

/**
 * Created by Florian on 06/01/2017.
 */

import com.florian.entity.Personne;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

/**
 * pour envoyer un message dans le chanel RabbitMQ
 */

@MessagingGateway
interface PatientWriter {
    @Gateway(requestChannel = "output")
    void write(Personne rn);
}
