package com.example.android.medjour.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

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

import timber.log.Timber;

public class PdfUtils {

    private static String RETURN = "\n";

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Timber.e("Directory not created");
        }
        return file;
    }

    public static void writePdf(Context ctxt, List<JournalEntry> journalEntries, String fileName) {
        if (isExternalStorageAvailable()) {

            Document doc = new Document(PageSize.LETTER);

            try {
                File dir = getPublicAlbumStorageDir("MeditationJournal");


                if (!dir.exists()) {
                    dir.mkdirs();

                    File file = new File(dir, fileName);
                    FileOutputStream fOut = new FileOutputStream(file);

                    PdfWriter.getInstance(doc, fOut);

                    //open the document
                    doc.open();
                    doc.setMargins(1.0f, 1.0f, 1.0f, 1.0f);
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
                    file.getName();
                    doc.close();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(ctxt, "The pdf could not be written, as there is no storage available.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void readPdf(final Context ctxt, String filename) {
        if (isExternalStorageAvailable()) {
            Uri file = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(file, "application/pdf");
            target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                ctxt.startActivity(target);
            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder pdfAlert = new AlertDialog.Builder(ctxt);
                pdfAlert.setTitle("Missing Pdf Reader");
                pdfAlert.setMessage("There is no pdf Reader installed on your device. Do you want to download one from Google Play?");
                pdfAlert.setPositiveButton("Go to GooglePlay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent googlePlay = new Intent(Intent.ACTION_VIEW);
                        googlePlay.setData(Uri.parse("http://play.google.com/store/search?q=pdf>&c=apps"));
                        ctxt.startActivity(googlePlay);
                    }
                });
                pdfAlert.setPositiveButton("Open browser", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent openBrowser = new Intent(Intent.ACTION_WEB_SEARCH);
                        ctxt.startActivity(openBrowser);
                    }
                });
                pdfAlert.setNegativeButton("Cancel", null);
            }
        } else {
            Toast.makeText(ctxt, "the external storage could not be read", Toast.LENGTH_SHORT).show();
        }
    }

}