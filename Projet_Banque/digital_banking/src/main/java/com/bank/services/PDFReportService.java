package com.bank.services;

import com.bank.entities.Operation;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

public class PDFReportService {

    public void generateBankStatement(String numeroCompte, List<Operation> operations, String clientName, double solde) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("releve_" + numeroCompte + ".pdf"));
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Relevé de Compte", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Add account information
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Numéro de compte: " + numeroCompte, normalFont));
            document.add(new Paragraph("Client: " + clientName, normalFont));
            document.add(new Paragraph("Solde actuel: " + String.format("%.2f €", solde), normalFont));
            document.add(Chunk.NEWLINE);

            // Create table for operations
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            
            // Add table headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Montant", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Solde après opération", headerFont)));

            // Add operations
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            double runningBalance = solde;
            
            for (Operation operation : operations) {
                if (operation.getClass().getSimpleName().equals("Retrait")) {
                    runningBalance += operation.getMontant();
                }
            }

            for (Operation operation : operations) {
                table.addCell(dateFormat.format(operation.getDateOperation()));
                table.addCell(operation.getClass().getSimpleName());
                
                String montant;
                if (operation.getClass().getSimpleName().equals("Retrait")) {
                    montant = String.format("-%.2f €", operation.getMontant());
                    runningBalance -= operation.getMontant();
                } else {
                    montant = String.format("+%.2f €", operation.getMontant());
                    runningBalance += operation.getMontant();
                }
                
                table.addCell(montant);
                table.addCell(String.format("%.2f €", runningBalance));
            }

            document.add(table);
            document.close();

            System.out.println("Relevé bancaire généré avec succès pour le compte: " + numeroCompte);
        } catch (Exception e) {
            System.out.println("Erreur lors de la génération du relevé bancaire pour le compte: " + numeroCompte);
            throw new RuntimeException("Erreur lors de la génération du relevé bancaire", e);
        }
    }
}
