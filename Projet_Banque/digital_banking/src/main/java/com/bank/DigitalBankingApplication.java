package com.bank;

import com.bank.services.BanqueService;
import com.bank.services.BanqueServiceImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DigitalBankingApplication extends Application {
    private static EntityManagerFactory emf;
    private static BanqueService banqueService;

    @Override
    public void init() {
        emf = Persistence.createEntityManagerFactory("digital-banking-pu");
        banqueService = new BanqueServiceImpl(emf);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();
        
        primaryStage.setTitle("Digital Banking Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (emf != null) {
            emf.close();
        }
        Platform.exit();
    }

    public static BanqueService getBanqueService() {
        return banqueService;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
