/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package springerdownloader;

import java.io.File;

/**
 *
 * @author Olli
 */
public class Chapter {
    
    private String name;
    private String link;
    private File file;
    private int part;
    private int pageCount;
    

    public Chapter(String name, String link, int part) {
        this.name = name;
        this.link = link;
        this.part = part;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }
    public File getFile() {
        return file;
    }

    public int getPart() {
        return part;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPages(int pages) {
        this.pageCount = pages;
    }
    

    
   
    
}
