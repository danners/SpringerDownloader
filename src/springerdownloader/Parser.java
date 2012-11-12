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
import java.util.prefs.BackingStoreException;

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
		case SPRINGER:
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
			Logger.getLogger(Parser.class.getName())
					.log(Level.SEVERE, null, ex);
		}

	}

	public static Book parseSpringer(String url ) throws Exception {
    	
    	Connection con = Jsoup.connect(url);
        con.timeout(timeout);
        con.userAgent(userAgent);
        con.cookie("MUD", "MP");
        Document doc = con.get();
        
        
        List<Chapter> chapters = new ArrayList<>();
        
        int number = 1;
        
        Elements backmatteritem = null;
        Elements frontmatteritem = null;
        Element noOfPages = doc.select(".number-of-pages").first();
        if(noOfPages!=null)
        	number = Integer.parseInt(noOfPages.text());
      
       
        for(int i = 1;i <= number; i++)
        {
        	if(i>1)
        	{
        		url = url.substring(0, url.length()-1)+i;
        		con = Jsoup.connect(url);
                con.timeout(timeout);
                con.userAgent(userAgent);
                con.cookie("MUD", "MP");
                doc = con.get();
        		
        		
        	}
        	
        	
        	
        Elements newelements = doc.select(".pdf-link");
        backmatteritem = doc.select ("li.back-matter-item > div > span > a");
        frontmatteritem = doc.select ("li.front-matter-item > div > span > a");
        
        
        Elements chaptertitles = doc.select("li > p.title > a");
         
        
        for(Element e:frontmatteritem){
     	   String link = e.absUrl("href");
     	   String title = "Front Matter";
     	   Chapter back = new Chapter(title,link,0);
     	   chapters.add(back);
        }
        
        
        for(int j = 0;j<newelements.size();j++)
        {
        	 Element alink = newelements.get(j);
        	 Element ctitle = chaptertitles.get(j);
        	 String link = alink.absUrl("href");
        	 String title =  ctitle.text();
        	 
        	 
        	 Chapter chapter = new Chapter(title, link, 0);
             chapters.add(chapter);
            
        }
        
        }

        
       for(Element e:backmatteritem){
    	   String link = e.absUrl("href");
    	   String title = "Back Matter";
    	   Chapter back = new Chapter(title,link,0);
    	   chapters.add(back);
       }
       
        Element titleElement = doc.select("#abstract-about-title").first();
       
        
        String bookTitle = titleElement.ownText();
        return new Book(bookTitle, chapters, false);
    	
    	
    	
    }

	private static Book parseDeGruyter(String url) throws Exception {
		Connection con = Jsoup.connect(url);
		con.timeout(timeout);
		con.userAgent(userAgent);
		Document doc = con.get();
		String contentLink = doc.select("a[id=read-content]").first()
				.absUrl("href");
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
					bookTitle = subDoc.select("p[class=bookTitle]").first()
							.ownText();
				}
				String pdfLink = subDoc.select("[class=pdf-Link]").first()
						.absUrl("href");
				// the chapterName contains the chapter number, thus set
				// chapterNumb = 0
				chapter = new Chapter(name, pdfLink, 0);

			} else { // has parts
				hasParts = true;
				String partName = listItem.select("span[class=unlinked]")
						.first().ownText();
				chapter = new Chapter(partName, null, 0);

			}
			chapters.add(chapter);
		}
		Book book = new Book(bookTitle, chapters, hasParts);
		return book;
	}

}
