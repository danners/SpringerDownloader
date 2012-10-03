/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package springerdownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Olli
 */
public class Book {

    private String title;
    private List<Chapter> chapters = new ArrayList<>();
    private boolean partioned = false;
    private Map<String, String> cookies;
    
    public Book(String title, List<Chapter> chapters, boolean hasParts) throws Exception {
        this.title = title;
        this.chapters = chapters;
        this.partioned = hasParts;

    }

    public List<String> getChapterNames() {
        List<String> names = new ArrayList();
        for (Chapter chapter : chapters) {
            names.add(chapter.getName());
        }
        return names;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPartioned() {
        return partioned;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }
    
}
