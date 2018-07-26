package com.example.android.medjour.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.example.android.medjour.R;
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
import java.io.OutputStream;
import java.util.List;

import timber.log.Timber;

public class PdfUtils {

    private static String RETURN = "\n";

    private Context ctxt;

    public PdfUtils(Context ctxt) {
        this.ctxt = ctxt;
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    //see: http://valokafor.com/android-itext-pdf-example/
    public void writePdf(List<JournalEntry> journalEntries, String fileName)
            throws FileNotFoundException, DocumentException {

        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), ctxt.getString(R.string.medJour_directory_name));
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
            Timber.i("Pdf Directory created");
        }

        File newPdf = new File(pdfFolder.getAbsoluteFile() + File.separator + fileName);

        OutputStream output = new FileOutputStream(newPdf);

        Document doc = new Document(PageSize.LETTER);

        PdfWriter.getInstance(doc, output);

        //open the document
        doc.open();
        doc.setMargins(1.0f, 1.0f, 1.0f, 1.0f);

        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12);

        Paragraph spacer = new Paragraph("\n~~~~\n", font);
        spacer.setAlignment(Element.ALIGN_CENTER);

        for (int i = 0; i < journalEntries.size(); i++) {
            JournalEntry currentEntry = journalEntries.get(i);
            String prep = "Preparation time: " + JournalUtils.toMinutes(currentEntry.getPrepTime());
            String med = "Meditation time: " + JournalUtils.toMinutes((currentEntry.getMedTime()));
            String rev = "Review time: " + JournalUtils.toMinutes((currentEntry.getRevTime()));
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
    }

    public void readPdf(String fileName) {
        if (isExternalStorageAvailable()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            File file = new File(new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    ctxt.getString(R.string.medJour_directory_name)), fileName);
            Uri uri = FileProvider.getUriForFile(ctxt, "com.example.android.medjour.provider", file);

            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                ctxt.startActivity(intent);
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