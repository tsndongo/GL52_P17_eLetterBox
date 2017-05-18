package com.florian.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Florian on 13/11/2016.
 */
@XmlRootElement(name = "course")
@XmlType(propOrder = {"courseName", "score"})
@JsonPropertyOrder({"courseName", "score"})
@Document(collection = "persone")
public class Personne{
    @Id
    private String id;

    private String nom;

    private String prenom;

    private Genre genre;

    private String idUser;

    private String Symptome;

    private String note;

    private String noteDate;

    public Personne(String nom, String prenom, Genre genre, String idUser, String symptome, String note, String noteDate) {
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.idUser = idUser;
        Symptome = symptome;
        this.note = note;
        this.noteDate = noteDate;
    }
    public Personne(String nom, String prenom, String genre, String idUser, String symptome, String note, String noteDate) {
        this.nom = nom;
        this.prenom = prenom;
        if(genre.equals("m"))
            this.genre = Genre.MASCULIN;
        else
            this.genre = Genre.FEMMININ;
        this.idUser = idUser;
        this.Symptome = symptome;
        this.note = note;
        this.noteDate = noteDate;
    }

    public Personne(String nom,String prenom,String genre,String id){

        this.nom = nom;
        this.prenom = prenom;
        if(genre.equals("m"))
            this.genre = Genre.MASCULIN;
        else
            this.genre = Genre.FEMMININ;
        this.idUser = id;
        this.Symptome="";
        this.note="";
        this.noteDate="";
    }
    public Personne(String nom, String prenom, Genre genre, String idUser, String symptome, String note) {

        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.idUser = idUser;
        Symptome = symptome;
        this.note = note;
    }

    public Personne(String id, String nom, String prenom, Genre genre, String idUser, String symptome, String note) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.idUser = idUser;
        Symptome = symptome;
        this.note = note;
    }

    public Personne() {
    }

    public Personne(String nom) {
        this.nom = nom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "Personne{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", genre=" + genre +
                ", idUser='" + idUser + '\'' +
                ", Symptome='" + Symptome + '\'' +
                ", note='" + note + '\'' +
                ", noteDate=" + noteDate +
                '}';
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getSymptome() {
        return Symptome;
    }

    public void setSymptome(String symptome) {
        Symptome = symptome;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(String noteDate) {
        this.noteDate = noteDate;
    }
}
