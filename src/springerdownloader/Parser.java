/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package springerdownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 *
 * @author Olli
 */
public class Parser extends javax.swing.SwingWorker<Book, Void> {
    
    private SpringerDownloader main;
    private JProgressBar progressBar;
    private SupportedSite siteToParse;
   

    private static final int timeout = 10000;
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2";

    public Parser(SpringerDownloader main, SupportedSite siteToParse) {
        this.main = main;
        main.setBook(null);
        this.siteToParse = siteToParse;
        progressBar = main.getProgressBar();
        this.addPropertyChangeListener(main);
        main.getChapterTable().setModel(new DefaultListModel());
    }
     
    @Override
    protected Book doInBackground() throws Exception {
        switch (siteToParse) {
            case SPRINGER :
                return parseSpringer(main.getURL());
            case DEGRUYTER:
                return parseDeGruyter(main.getURL());              
        }
        return null;
    }

    @Override
        public void done() {
        try {
            main.setBook(get());
            List<String> chapterNames = main.getBook().getChapterNames();
            main.getChapterTable().setListData(chapterNames.toArray());
            progressBar.setString("Ready to Download!");
        } catch (InterruptedException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }


    }


    public static Book parseSpringer(String url) throws Exception {
        Connection con = Jsoup.connect(url);
        con.timeout(timeout);
        con.userAgent(userAgent);
        con.cookie("MUD", "MP");
        Document doc = con.get();

        // get the element containing the pdfs
        Element ul = doc.select("ul[class=manifest primitiveManifest]").get(1);
        Elements linkList = ul.select("[class^=primitive]");
        List<Chapter> chapters = new ArrayList<>();
        boolean hasParts = false;
        int part = 0;
        int chapterNr = 1;
        boolean lastRowContainedPart = false;
        // some books have parts. Each part starts with its own frontmatter, so 
        // a book can have multiple front matters.
        if (!ul.select("li[class*=bookPart]").isEmpty()) {
            hasParts = true;
        }
        String chapterName = null;
        for (Element row : linkList) {
            // current row contains part
            if (hasParts && !row.select("li[class*=bookPart]").isEmpty()) {
                chapterName = row.select("p[class=title]").first().text();
                lastRowContainedPart = true;
                part++;
                
            } else {
                // not a part
                Element pdfResource = row.select("a[class=sprite pdf-resource-sprite]").first();
                String link = pdfResource.absUrl("href");
                Chapter chapter;
                int partTemp = 0;
                if(!lastRowContainedPart  || (lastRowContainedPart && row.select("[class*=Frontmatter]").isEmpty())) {
                    chapterName = row.select("p[class=title]").first().text();
                }
                if (lastRowContainedPart) {
                    lastRowContainedPart = false;
                    partTemp = part;
                }

                    // no chapterNr for front and backmatterr
                    if (row.select("[class*=Frontmatter]").isEmpty() && row.select("[class*=Backmatter]").isEmpty()) {             
                        chapterName = chapterNr + ". " + chapterName;
                        chapterNr++;
                    }
                

                chapter = new Chapter(chapterName, link, partTemp);
                chapters.add(chapter);
            }
        }
        Element aboutSection = doc.select("div[id=AboutSection]").first();
        Element titleElement = aboutSection.select("dd").first();
        String bookTitle = titleElement.ownText();
        return new Book(bookTitle, chapters, hasParts);
    }

    private static Book parseDeGruyter(String url) throws Exception {
        Connection con = Jsoup.connect(url);
        con.timeout(timeout);
        con.userAgent(userAgent);
        Document doc = con.get();
        String contentLink = doc.select("a[id=read-content]").first().absUrl("href");
        con.url(contentLink);
        doc = con.get();
        String bookTitle = null;
        List<Chapter> chapters = new ArrayList<>();
        boolean hasParts = false;
        // get the list containing the chapterLinks
        Element div = doc.select("div[id=contentContainer]").first();
        Element innerDiv = div.select("div[class=innerWrap]").first();
        Elements listItems = innerDiv.select("li");

        for (Element listItem : listItems) {
            // no parts
            Chapter chapter;
            if (listItem.select("span[class=unlinked]").isEmpty()) {
                Element linkElem = listItem.select("a[href]").first();
                String name = linkElem.ownText();
                String link = linkElem.absUrl("href");
                con.url(link);
                Document subDoc = con.get();
                if (bookTitle == null) {
                    bookTitle = subDoc.select("p[class=bookTitle]").first().ownText();
                }
                String pdfLink = subDoc.select("[class=pdf-Link]").first().absUrl("href");
                // the chapterName contains the chapter number, thus set chapterNumb = 0
                chapter = new Chapter(name, pdfLink, 0);

            } else { //has parts
                hasParts = true;
                String partName = listItem.select("span[class=unlinked]").first().ownText();
                chapter = new Chapter(partName, null, 0);

            }
            chapters.add(chapter);
        }
        Book book = new Book(bookTitle, chapters, hasParts);
        return book;
    }


    

}
