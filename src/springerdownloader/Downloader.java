/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package springerdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.util.PDFMergerUtility;

/**
 *
 * @author Olli
 */
public class Downloader extends javax.swing.SwingWorker<Void, Void> {

    private SpringerDownloader main;

    public Downloader(SpringerDownloader main) {
        this.main = main;
        this.addPropertyChangeListener(main);
    }

    @Override
    protected Void doInBackground() throws Exception {
        downloadAndMerge();
        return null;
    }

    @Override
    protected void done() {
        main.getProgressBar().setString("PDF Created!");
    }
    
    

    public void downloadAndMerge() throws Exception {
        Book book = main.getBook();
        File downloadFolder = main.getDownloadFolder();
        List<Chapter> chapters = book.getChapters();
        int i = 0;
        int progress = 0;
        setProgress(0);
        //Download PDFs
        for (Chapter chapter : chapters) {
           File file = File.createTempFile("pdf_merger_temp" + i, ".pdf");
           // File file = new File(downloadFolder+"/"+fixFileName(chapter.getName())+".pdf");
          //  file.deleteOnExit();
            download(chapter.getLink(), file,null);
            chapter.setFile(file);
            PDDocument doc = PDDocument.load(file);
            int numberOfPages = doc.getNumberOfPages();
            chapter.setPages(numberOfPages);
            doc.close();
            i++;
            progress += 100 / (chapters.size() + 1);
            setProgress(progress);
        }

        //Merge PDFs
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        PDDocument outputDoc = new PDDocument();
        PDDocumentOutline outline = new PDDocumentOutline();
        PDOutlineItem bookmark;
        PDPage targetPage;

        String fixedTitle = fixFileName(book.getTitle());
        File mergedPDF = new File(downloadFolder.getAbsolutePath() + File.separator + fixedTitle + ".pdf");

        if (book.isPartioned()) {
            for (Chapter chapter : chapters) {
                if (chapter.getFile() != null) {
                    pdfMerger.addSource(chapter.getFile());
                }
            }
            pdfMerger.setDestinationFileName(mergedPDF.getAbsolutePath());
            pdfMerger.mergeDocuments();
            outputDoc = PDDocument.load(mergedPDF);
            outputDoc.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineNode root = outline;
            int page = 0;

            for (Chapter chapter : chapters) {
                // create the bookmark to add
                bookmark = new PDOutlineItem();
                targetPage = (PDPage) outputDoc.getDocumentCatalog().getAllPages().get(page);
                bookmark.setDestination(targetPage);
                if (chapter == chapters.get(chapters.size())) {
                    root = outline;
                }
                if (chapter.getPart() == 0) {
                    bookmark.setTitle(chapter.getName());
                    root.appendChild(bookmark);
                } else {
                    bookmark.setTitle(chapter.getName());
                    outline.appendChild(bookmark);
                    bookmark.openNode();
                    root = bookmark;
                }
                page += chapter.getPageCount();
            }

        } else {

            for (Chapter chapter : chapters) {
                outline = new PDDocumentOutline();
                PDDocument currentDoc = PDDocument.load(chapter.getFile());
                currentDoc.getDocumentCatalog().setDocumentOutline(outline);

                // create the bookmark to add
                bookmark = new PDOutlineItem();
                String title = chapter.getName();
                bookmark.setTitle(title);
                targetPage = (PDPage) currentDoc.getDocumentCatalog().getAllPages().get(0);
                bookmark.setDestination(targetPage);
                // append the bookmark
                outline.appendChild(bookmark);
                pdfMerger.appendDocument(outputDoc, currentDoc);
                currentDoc.close();
            }
        }
        outputDoc.save(mergedPDF.getAbsolutePath());
        outputDoc.close();
    }

    public static void downloadAndMerge(Book book, File downloadFolder) throws Exception {
        List<Chapter> chapters = book.getChapters();
        int i = 0;
        //Download PDFs
        for (Chapter chapter : chapters) {
            File file = File.createTempFile("pdf_merger_temp" + i, ".pdf");
            //  File file = new File(downloadFolder+"/"+fixFileName(chapter.getName())+".pdf");
            //   file.deleteOnExit();
            download(chapter.getLink(), file, null);
            chapter.setFile(file);
            PDDocument doc = PDDocument.load(file);
            int numberOfPages = doc.getNumberOfPages();
            chapter.setPages(numberOfPages);
            doc.close();
            i++;
        }

        //Merge PDFs
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        PDDocument outputDoc = new PDDocument();
        PDDocumentOutline outline = new PDDocumentOutline();
        PDOutlineItem bookmark;
        PDPage targetPage;

        String fixedTitle = fixFileName(book.getTitle());
        File mergedPDF = new File(downloadFolder.getAbsolutePath() + File.separator + fixedTitle + ".pdf");

        if (book.isPartioned()) {
            for (Chapter chapter : chapters) {
                if (chapter.getFile() != null) {
                    pdfMerger.addSource(chapter.getFile());
                }
            }
            pdfMerger.setDestinationFileName(mergedPDF.getAbsolutePath());
            pdfMerger.mergeDocuments();
            outputDoc = PDDocument.load(mergedPDF);
            outputDoc.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineNode root = outline;
            int page = 0;

            for (Chapter chapter : chapters) {
                // create the bookmark to add
                bookmark = new PDOutlineItem();
                targetPage = (PDPage) outputDoc.getDocumentCatalog().getAllPages().get(page);
                bookmark.setDestination(targetPage);
                if (chapter == chapters.get(chapters.size()-1)) {
                    root = outline;
                }
                if (chapter.getPart() == 0) {
                    bookmark.setTitle(chapter.getName());
                    root.appendChild(bookmark);
                } else {
                    bookmark.setTitle(chapter.getName());
                    outline.appendChild(bookmark);
                    bookmark.openNode();
                    root = bookmark;
                }
                page += chapter.getPageCount();
            }

        } else {

            for (Chapter chapter : chapters) {
                outline = new PDDocumentOutline();
                PDDocument currentDoc = PDDocument.load(chapter.getFile());
                currentDoc.getDocumentCatalog().setDocumentOutline(outline);

                // create the bookmark to add
                bookmark = new PDOutlineItem();
                String title = chapter.getName();
                bookmark.setTitle(title);
                targetPage = (PDPage) currentDoc.getDocumentCatalog().getAllPages().get(0);
                bookmark.setDestination(targetPage);
                // append the bookmark
                outline.appendChild(bookmark);
                pdfMerger.appendDocument(outputDoc, currentDoc);
                currentDoc.close();
            }
        }
        outputDoc.save(mergedPDF.getAbsolutePath());
        outputDoc.close();
    }

    public static void download(String url, File file, Map<String, String> cookies) throws IOException {
        URL downloadURL = new URL(url);
        URLConnection con = downloadURL.openConnection();
        if (cookies != null) {
            for (String s : cookies.keySet()) {
                con.setRequestProperty(s, cookies.get(s));
            }
        }
        con.addRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        rbc.close();
        fos.close();
    }

    public static String fixFileName(String filename) {
        return filename.replaceAll("[\\/:\"*?<>|]+", "-");
    }
    
    public static void main (String[] args) throws Exception {
        Book book = Parser.parseSpringer("http://www.springerlink.com/content/978-3-540-85075-5/contents/");
        Downloader.downloadAndMerge(book, new File("C:/testing/"));
        
    }
};