package com.florian;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * Created by Florian on 06/01/2017.
 */
// chanel de récuperation de RabbtitMQ
interface PatientChanel {

    @Input
    SubscribableChannel input();
}