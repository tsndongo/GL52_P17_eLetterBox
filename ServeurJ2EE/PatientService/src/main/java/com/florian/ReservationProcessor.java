package com.florian;

import com.florian.Service.mongoRepo.PersonneRepository;
import com.florian.entity.Personne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Created by Florian on 06/01/2017.
 */

/**
 * lorsque un message est dans le brooker
 */

@MessageEndpoint
class ReservationProcessor{

    @ServiceActivator(inputChannel = "input")
    public void onNewReservation(Personne msg){
        System.out.println("\n \n\n \tFrom BRooker"+msg.toString());
        this.reservationRepository.save(msg);
    }

    private PersonneRepository reservationRepository;

    @Autowired
    public ReservationProcessor(PersonneRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
}
