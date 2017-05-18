package com.florian.client;

import com.florian.entity.Genre;
import com.florian.entity.Personne;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Florian on 06/01/2017.
 */

/// changer le nom du mapping (reliquat de tests)
@RestController
@RequestMapping("/reservations")
class PatientApiGateway {

    private final PatientService reservationReader;
    private final PatientWriter reservationWriter;
    @Autowired
    public PatientApiGateway(PatientService reservationReader, PatientWriter reservationWriter) {
        this.reservationReader = reservationReader;
        this.reservationWriter = reservationWriter;
    }
/*
	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Personne personne){
		System.out.println("==================");
		System.out.println(personne.toString());
		this.reservationWriter.write(personne);
	}*/


    /**
     * Request post d'ajout d'une personne
     * @param request
     */
    @RequestMapping(method = RequestMethod.POST,value="/test")
    public void write(@RequestBody String request ){
        List<String> a = Arrays.asList(request.split("&"));
        int debut,fin;
        String nom,prenom;
        for(int i=0;i<a.size();i++){
            String str=a.get(i);
            debut =str.indexOf("=")+1;
            str=str.substring(debut,str.length());
            System.out.println(str);
            a.set(i,str);
        }
        Genre g;
        if(a.get(2).equals("m"))
            g = Genre.MASCULIN;
        else
            g = Genre.FEMMININ;
        Personne p = new Personne(a.get(0),a.get(1),g,a.get(3),a.get(4),a.get(5));
        System.out.println(p.toString());
        this.reservationWriter.write(p);

    }

    /**
     *
     * si le microService PatientService est down
     * @return
     */
    public PatientWrapper fallBack(){
        System.out.println("requete down utilisation par default");
        return new PatientWrapper();
    }


    /**
     * mapping pour les methode get
     * @return un JSon contenant tous les Patient en BDD
     */
    @HystrixCommand(fallbackMethod = "fallBack")
    @RequestMapping(method = RequestMethod.GET, value="/names",produces =  "application/json")
    public PatientWrapper names(){
        System.out.println("utilisation par default");
        List<Personne>  resa= new LinkedList<>();
        this.reservationReader.read().forEach(reservation -> {resa.add(reservation);});
        System.out.println("=========>"+resa);
        PatientWrapper w = new PatientWrapper(resa);
        return w;
    }
}