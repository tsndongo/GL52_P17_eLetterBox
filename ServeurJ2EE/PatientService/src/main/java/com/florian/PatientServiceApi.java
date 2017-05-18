package com.florian;

import com.florian.Service.mongoRepo.PersonneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Florian on 06/01/2017.
 */

//Gestion de toute les entrée sur /update
@RestController
@RequestMapping("/update")
class PatientServiceApi {

    @Autowired
    private final PersonneRepository reservationRepository;

    public PatientServiceApi(PersonneRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void write(@RequestBody String request) {
        System.out.println(request);
        List<String> a = Arrays.asList(request.split("&"));

        int debut, fin;
        String nom, prenom;
        for (int i = 0; i < a.size(); i++) {
            String str = a.get(i);
            debut = str.indexOf("=") + 1;
            str = str.substring(debut, str.length());
            System.out.println(str);
            a.set(i, str);
        }
        reservationRepository.updatetestSauvegarde(a.get(0), a.get(1));


    }
}