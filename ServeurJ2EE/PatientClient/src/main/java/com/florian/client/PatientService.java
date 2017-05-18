package com.florian.client;

/**
 * Created by Florian on 06/01/2017.
 */

import com.florian.entity.Personne;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * pour communiquer avec la methode  get
 * Fait comme appel au microService portant le nom PatientService
 */
@FeignClient("Patient-service")
interface PatientService {

    @RequestMapping(method = RequestMethod.GET,value = "/personnes")
    Resources<Personne> read();
}