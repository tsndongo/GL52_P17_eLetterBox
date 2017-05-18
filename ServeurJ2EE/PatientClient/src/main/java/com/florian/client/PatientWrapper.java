package com.florian.client;

import com.florian.entity.Personne;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Florian on 06/01/2017.
 */

class PatientWrapper {

    private List<Personne> reservation;

    public PatientWrapper() {
        reservation= new LinkedList<>();
    }

    public PatientWrapper(List<Personne> reservation) {
        this.reservation = reservation;
    }

    public List<Personne> getReservatiopn() {
        return reservation;
    }

    public void setReservation(List<Personne> reservation) {
        this.reservation = reservation;
    }
}