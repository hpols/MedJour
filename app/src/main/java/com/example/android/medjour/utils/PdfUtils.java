package com.example.android.medjour.utils;

import android.os.Environment;

import com.example.android.medjour.model.data.JournalEntry;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class PdfUtils {

    private String RETURN = "\n";

    public void writePdf(List<JournalEntry> journalEntries) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Document doc = new Document(PageSize.LETTER);

            try {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Dir";

                File dir = new File(path);
                if (!dir.exists())
                    dir.mkdirs();

                File file = new File(dir, "meditation_journal.pdf");
                FileOutputStream fOut = new FileOutputStream(file);

                PdfWriter.getInstance(doc, fOut);

                //open the document
                doc.open();
                doc.newPage();

                Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12);

                Paragraph spacer = new Paragraph("\n~~~~\n", font);
                spacer.setAlignment(Element.ALIGN_CENTER);

                for (int i = 0; i < journalEntries.size(); i++) {
                    JournalEntry currentEntry = journalEntries.get(i);
                    String prep = "Preparation time: " + String.valueOf(currentEntry.getPrepTime());
                    String med = "Meditation time: " + String.valueOf(currentEntry.getMedTime());
                    String rev = "Review time: " + String.valueOf(currentEntry.getRevTime());
                    String date = "Date: " + String.valueOf(currentEntry.getDate());
                    String assessment = currentEntry.getAssessment();
                    String entry = date + RETURN + prep + RETURN + med + RETURN + rev + RETURN + assessment;

                    Paragraph p = new Paragraph(entry, font);
                    p.setKeepTogether(true);
                    doc.add(p);
                    doc.add(spacer);
                }
                //close document
                doc.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
    }
}
