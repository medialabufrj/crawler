import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class PatternFile {

	public static ArrayList<String> links_file(String site, String rawPage, ArrayList<String> patterns) {
    	
		ArrayList<String> links_look = new ArrayList<String>(50);
    	
		for (int i = 0; i < patterns.size(); i++) {
			
    		String formats = patterns.get(i);
			if ((formats.startsWith("//")) || (formats.trim().length() == 0))  //comments
				continue;
    		
    		StringTokenizer parts = new StringTokenizer(formats,";");
    		int count_tokens = parts.countTokens();
    		
    		if (count_tokens < 3)
    			continue;
    
    		String patt [] = new String [3]; //tags da pagina
    		for (int j = 0; j < 3; j++)
    			patt[j] = parts.nextToken();
    		
    		int num_extension = Integer.parseInt(parts.nextToken());
    		String patt2 [] = new String[1]; //extensoes a procurar na pagina
    		if (num_extension > 0) {
    			patt2 = new String[num_extension];
        		for (int j = 0; j < num_extension; j++)
        			patt2[j] = "." + parts.nextToken();
    		}
    		
    		String home_site = "";
    		if (parts.hasMoreTokens()) {
    			home_site = parts.nextToken();
    			
    			if ((home_site.length() != 0) && (!site.contains(home_site)))
    				continue;
    		}
    		
    		ArrayList<String> sites = find_links(site,rawPage,patt);
    		
            for (int k = 0; k < sites.size(); k++) {
            	String link = sites.get(k);
            	String sit = link.toLowerCase();
            	
            	boolean flag = false;
            	for (int z = 0; z < num_extension; z++) { //procura extensoes
            		if (sit.contains(patt2[z])) {
            			flag = true;
            			break;
            		}
            	}
            	
            	if (flag) {
            		if (!links_look.contains(link))
            			links_look.add(link);
            	}   		
            }
		}
		
		return links_look;
    }
    
    public static ArrayList<String> find_links(String site_origem, String rawPage, String patt []) {
    
    	String site = "";
    	ArrayList<String> pages = new ArrayList<String>(20);
    	
    	Document page = Jsoup.parse(rawPage);
    	Elements links = page.select(patt[0]);
	
    	if (patt[2].startsWith("abspath:")) {
    		if (!patt[1].equals(" ")) {
    			for (Element src : links) {
    				if (src.tagName().equals(patt[1])) {
    					try {
    						URL url = new URL(site_origem);
    						URI uri = url.toURI();
    						site = uri.resolve(src.attr(patt[2].substring(patt[2].indexOf(':')+1))).toString();
    					}

    					catch (Exception ex) {site = "";}

    					if (!pages.contains(site))
    						pages.add(site);
    				}  
    			}
    		}

    		else {
    			for (Element src : links) {
    				try {
    					URL url = new URL(site_origem);
    					URI uri = url.toURI();
    					site = uri.resolve(src.attr(patt[2].substring(patt[2].indexOf(':')+1))).toString();
    				}

    				catch (Exception ex) {site = "";}

    				if (!pages.contains(site))
    					pages.add(site);
    			}
    		}
    	}

    	else {

    		if (!patt[1].equals(" ")) {
    			for (Element src : links) {
    				if (src.tagName().equals(patt[1])) {
    					site = src.attr(patt[2]);
    					if (!pages.contains(site))
    						pages.add(site);
    				}  
    			}
    		}
    	
    		else {
    			for (Element src : links) {
    				site = src.attr(patt[2]);
    				if (!pages.contains(site))
    					pages.add(site);
    			}
    		}
    	}	
    	
    	return pages;
    }
    
}