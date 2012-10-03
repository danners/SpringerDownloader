/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package springerdownloader;

/**
 *
 * @author Olli
 */
public enum SupportedSite {
    SPRINGER ("(http://)?(www.)?springerlink.com/content/.*?/contents(/)?"), 
    DEGRUYTER ("(http://)?www.degruyter.com/view/product/\\d*");
    
    private  final String urlPattern;

    private SupportedSite(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getUrlPattern() {
        return urlPattern;
    }
    
    public static SupportedSite checkURL(String urlString) {
        for (SupportedSite s : SupportedSite.values()) {
            if (urlString.matches(s.getUrlPattern())) {
                return s;
            }
        }
        return null;
    }
    


    
    
    
    
    
}
