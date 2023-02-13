package controller;

import dao.BücherAutorDAO;
import dao.TempDAO;
import model.Autor;
import model.Buch;
import view.AlleBücherDesAutorsView;
import view.MainView;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MainController {

    private final BücherAutorDAO bücherAutorDB;
    private final MainView mainView;

    public MainController(BücherAutorDAO bücherAutorDB, MainView mainView) {
        this.bücherAutorDB = bücherAutorDB;
        this.mainView = mainView;

        mainView.setAnzeigenButtonListener( this::performAnzeigen );
        mainView.setSpeichernButtonListener( this::performSpeichern );
        mainView.setAlleBücherDesAutorsButtonListener( this::performAlleBücherDesAutors );
    }

    private void performAlleBücherDesAutors(ActionEvent actionEvent) {
        String autorName = mainView.getAutor();
        AlleBücherDesAutorsView alleBücherDesAutorsView = new AlleBücherDesAutorsView();

        DefaultListModel<Buch> bücherModel = new DefaultListModel<>();
        for (Buch buch : bücherAutorDB.getAllBücher()) {
            if (buch.getAutor().getName().equals(autorName))
                bücherModel.addElement(buch);
        }
        alleBücherDesAutorsView.setBücherListDefaultModel(bücherModel);


    }

    private void performSpeichern(ActionEvent actionEvent) {
        int buchnummer = mainView.getBuchnummer();
        if (buchnummer <= 0) return;

        Buch buch = bücherAutorDB.getBuchByID(buchnummer);
        if (buch != null) {
            // Überschreiben!?
            if ( mainView.zeigeRückfrage("Buch existiert bereits. Soll es überschrieben werden?") ) {
                Buch neuesBuch = holeBuch();
                if (bücherAutorDB.updateBuch(buchnummer, neuesBuch)) {
                    mainView.zeigeMeldung("Änderungen wurden gespeichert");
                }
                else {
                    mainView.zeigeFehlermeldung("Beim Speichern ist etwas schief gegangen!");
                }
            }
        }
        else {
            // Neu anlegen
            Buch neuesBuch = holeBuch();
            if (bücherAutorDB.insertBuch(neuesBuch)) {
                mainView.zeigeMeldung("Das neue Buch wurde in der Datenbank gespeichert");
            }
            else {
                mainView.zeigeFehlermeldung("Beim Speichern ist etwas schief gegangen!");
            }
        }
    }

    private Buch holeBuch() {
        int buchnummer = mainView.getBuchnummer();
        String buchtitel = mainView.getBuchtitel();
        String autorname = mainView.getAutor();
        double preis = mainView.getPreis();
        boolean gelesen = mainView.istGelesen();

        Autor autor;
        int autorID = bücherAutorDB.getIDByAutorName(autorname);
        if (autorID > 0) {
            autor = bücherAutorDB.getAutorByID(autorID);
        }
        else {
            autor = new Autor( bücherAutorDB.nächsteAutorID(), autorname );
            bücherAutorDB.insertAutor(autor);
        }

        return new Buch(buchnummer, buchtitel, autor, preis, gelesen);
    }

    private void performAnzeigen(ActionEvent actionEvent) {
        int buchnummer = mainView.getBuchnummer();
        if (buchnummer > 0) {
            Buch buch = bücherAutorDB.getBuchByID(buchnummer);
            if (buch != null) {
                zeigeBuch(buch);
                return;
            }
        }
        leereBuch();
    }

    private void zeigeBuch(Buch buch) {
        mainView.setBuchnummer( buch.getBuchID() );
        mainView.setBuchtitel( buch.getTitel() );
        mainView.setAutor( buch.getAutor().getName() );
        mainView.setPreis( buch.getPreis() );
        mainView.setGelesen( buch.isGelesen() );
    }

    private void leereBuch() {
        mainView.setBuchnummer( 0 );
        mainView.setBuchtitel( "" );
        mainView.setAutor( "" );
        mainView.setPreis( 0.0 );
        mainView.setGelesen( false );
    }

    public static void main(String[] args) {
        new MainController( new TempDAO(), new MainView() );
    }
}
