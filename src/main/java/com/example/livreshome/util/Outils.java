package com.example.livreshome.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Outils
{

    Node node;
// cette fonction vous permet de charger d'autre ou faire la redirection
    private void loadPage(ActionEvent event, String titre, String pageacharger) throws IOException {


        ((Node)event.getSource()).getScene().getWindow().hide();
        Parent root= FXMLLoader.load(getClass().getResource(pageacharger));
        Scene scene=new Scene(root);
        Stage stage =new Stage();
        stage.setScene(scene);
        stage.setTitle(titre);

        stage.show();

    }

    // cette fonction fait appel a notre methode redirection pour gerer les pages a afficher
    public static  void load(ActionEvent event,String Titre,String url) throws IOException {
        new Outils().loadPage(event,Titre,url);
    }

    private void loadAndWaitPage(ActionEvent event, String titre, String pageacharger) throws IOException {


     //   ((Node)event.getSource()).getScene().getWindow().hide();
        Parent root= FXMLLoader.load(getClass().getResource(pageacharger));
        Scene scene=new Scene(root);
        Stage stage =new Stage();
        stage.setScene(scene);
        stage.setTitle(titre);

        stage.show();

    }
    public static  void loadandwait(ActionEvent event,String Titre,String url) throws IOException {
        new Outils().loadAndWaitPage(event,Titre,url);
    }
}
