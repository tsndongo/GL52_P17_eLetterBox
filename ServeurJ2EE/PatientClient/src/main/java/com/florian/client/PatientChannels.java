package com.florian.client;

/**
 * Created by Florian on 06/01/2017.
 */

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * utiliser par rabbitMq
 */
interface PatientChannels{

    @Output
    MessageChannel output();
}