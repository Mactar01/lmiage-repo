package com.bank.controllers;

import com.bank.DigitalBankingApplication;
import com.bank.entities.Client;
import com.bank.entities.Compte;
import com.bank.entities.Operation;
import com.bank.services.BanqueService;
import com.bank.services.PDFReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.util.Optional;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainController {
    private final BanqueService banqueService;
    private final PDFReportService pdfReportService;

    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Long> clientIdColumn;
    @FXML private TableColumn<Client, String> clientNomColumn;
    @FXML private TableColumn<Client, String> clientPrenomColumn;
    @FXML private TableColumn<Client, String> clientEmailColumn;

    @FXML private TableView<Compte> comptesTable;
    @FXML private TableColumn<Compte, String> compteNumeroColumn;
    @FXML private TableColumn<Compte, Double> compteSoldeColumn;
    @FXML private TableColumn<Compte, String> compteDateColumn;
    @FXML private TableColumn<Compte, String> compteClientColumn;

    public MainController() {
        this.banqueService = DigitalBankingApplication.getBanqueService();
        this.pdfReportService = new PDFReportService();
    }

    @FXML
    public void initialize() {
        setupClientTable();
        setupCompteTable();
        refreshTables();
    }

    private void setupClientTable() {
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientNomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        clientPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        clientEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void setupCompteTable() {
        compteNumeroColumn.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        compteSoldeColumn.setCellValueFactory(new PropertyValueFactory<>("solde"));
        compteDateColumn.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(sdf.format(cellData.getValue().getDateCreation()));
        });
        compteClientColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClient().getNom() + " " + 
                                   cellData.getValue().getClient().getPrenom()));
    }

    private void refreshTables() {
        List<Client> clients = banqueService.listClients();
        clientsTable.setItems(FXCollections.observableArrayList(clients));
        
        List<Compte> comptes = banqueService.listComptes();
        comptesTable.setItems(FXCollections.observableArrayList(comptes));
    }

    @FXML
    private void showNewClientDialog() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Client");
        dialog.setHeaderText("Créer un nouveau client");

        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField email = new TextField();

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nom, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenom, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Client client = new Client();
                client.setNom(nom.getText());
                client.setPrenom(prenom.getText());
                client.setEmail(email.getText());
                return client;
            }
            return null;
        });

        Optional<Client> result = dialog.showAndWait();
        result.ifPresent(client -> {
            banqueService.saveClient(client);
            refreshTables();
        });
    }

    @FXML
    private void showNewAccountDialog() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", null, 
                     "Veuillez sélectionner un client pour créer un compte");
            return;
        }

        Dialog<Compte> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Compte");
        dialog.setHeaderText("Créer un nouveau compte");

        ButtonType saveButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField soldeInitial = new TextField("0.0");
        grid.add(new Label("Solde Initial:"), 0, 0);
        grid.add(soldeInitial, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double solde = Double.parseDouble(soldeInitial.getText());
                    Compte compte = new Compte();
                    compte.setSolde(solde);
                    compte.setDateCreation(new Date());
                    return compte;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, 
                             "Le solde doit être un nombre valide");
                    return null;
                }
            }
            return null;
        });

        Optional<Compte> result = dialog.showAndWait();
        result.ifPresent(compte -> {
            banqueService.saveCompte(compte, selectedClient.getId());
            refreshTables();
        });
    }

    @FXML
    private void showDepositDialog() {
        showOperationDialog("Dépôt", (compte, montant) -> banqueService.depot(compte.getNumeroCompte(), montant));
    }

    @FXML
    private void showWithdrawalDialog() {
        showOperationDialog("Retrait", (compte, montant) -> banqueService.retrait(compte.getNumeroCompte(), montant));
    }

    @FXML
    private void showTransferDialog() {
        Compte sourceCompte = comptesTable.getSelectionModel().getSelectedItem();
        if (sourceCompte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", null, 
                     "Veuillez sélectionner un compte source");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Virement");
        dialog.setHeaderText("Effectuer un virement");

        ButtonType transferButtonType = new ButtonType("Virer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(transferButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Compte> destinationCompte = new ComboBox<>();
        destinationCompte.setItems(FXCollections.observableArrayList(
            banqueService.listComptes().stream()
                .filter(c -> !c.getNumeroCompte().equals(sourceCompte.getNumeroCompte()))
                .toList()
        ));
        destinationCompte.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Compte compte, boolean empty) {
                super.updateItem(compte, empty);
                if (empty || compte == null) {
                    setText(null);
                } else {
                    setText(compte.getNumeroCompte() + " - " + 
                           compte.getClient().getNom() + " " + 
                           compte.getClient().getPrenom());
                }
            }
        });

        TextField montant = new TextField();

        grid.add(new Label("Compte Destination:"), 0, 0);
        grid.add(destinationCompte, 1, 0);
        grid.add(new Label("Montant:"), 0, 1);
        grid.add(montant, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == transferButtonType) {
                try {
                    if (destinationCompte.getValue() == null) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", null, 
                                "Veuillez sélectionner un compte destination");
                        return null;
                    }
                    return Double.parseDouble(montant.getText());
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, 
                             "Le montant doit être un nombre valide");
                    return null;
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                banqueService.virement(
                    sourceCompte.getNumeroCompte(),
                    destinationCompte.getValue().getNumeroCompte(),
                    amount
                );
                refreshTables();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, e.getMessage());
            }
        });
    }

    @FXML
    private void generateSelectedAccountStatement() {
        Compte selectedCompte = comptesTable.getSelectionModel().getSelectedItem();
        if (selectedCompte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", null, 
                     "Veuillez sélectionner un compte");
            return;
        }

        List<Operation> operations = banqueService.listOperations(
            selectedCompte.getNumeroCompte(), 0, 100).getContent();
        
        pdfReportService.generateBankStatement(
            selectedCompte.getNumeroCompte(),
            operations,
            selectedCompte.getClient().getNom() + " " + selectedCompte.getClient().getPrenom(),
            selectedCompte.getSolde()
        );

        showAlert(Alert.AlertType.INFORMATION, "Succès", null, 
                 "Le relevé a été généré avec succès");
    }

    private void showOperationDialog(String type, OperationCallback callback) {
        Compte selectedCompte = comptesTable.getSelectionModel().getSelectedItem();
        if (selectedCompte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", null, 
                     "Veuillez sélectionner un compte");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle(type);
        dialog.setHeaderText("Effectuer un " + type.toLowerCase());

        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField montant = new TextField();
        grid.add(new Label("Montant:"), 0, 0);
        grid.add(montant, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    return Double.parseDouble(montant.getText());
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, 
                             "Le montant doit être un nombre valide");
                    return null;
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                callback.execute(selectedCompte, amount);
                refreshTables();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, e.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface OperationCallback {
        void execute(Compte compte, double montant);
    }
}
