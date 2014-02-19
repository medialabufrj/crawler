
// salva as paginas da internet e os indices das páginas.

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class Crawler_simples2 {
	
	private static String OS = System.getProperty("file.separator"); //separador de diretorios
	
	private static int num_img = 0; //numero de imagens baixadas
	private static int num_user = 0; //numero de usuarios com links diferentes
	private static int num_pages = 0; //numero de páginas salvas
	private static int num_lines = 0; //numero de linhas do arquivo
	private static int user_label = 1; //rotulo do usuarios
	
	private static long TAM_MIN = 15; //nao armazena imagens menores do que 15 kB
	private static int ALT_MIN = 200; //nao armazena imagens com altura menor do que 150 pixels
	private static int LAR_MIN = 200; //nao armazena imagens com largura menor do que 150 pixels
	private static int SALVE_INT = 2000;  //Numero de iterações para salvar o historico
	private static boolean EXIT_PROGRAM = false; //sai do programa ou não depois de SALVE_INT iterações
	private static long WAIT_TIME = 1000; //tempo de espera entre requisicoes
	private static boolean USE_PATTERN = false; //usa um arquivo com padroes para encontrar os links
	private static int WAIT_TIMEOUT = 100; //tempo de espera de conexao em segundos
	
	
	public static String [] fileType = {".bmp", ".jpg", ".jpeg", ".tiff"};
	
	//Listas de variaveis para evitar links repetidos
	private static ArrayList<String> home_pages = new ArrayList<String>(5000); 
	private static ArrayList<String> users = new ArrayList<String>(5000);
	private static ArrayList<String> save_fotos = new ArrayList<String>(5000);
	private static ArrayList<String> save_fotos_real = new ArrayList<String>(5000);
	private static ArrayList<String> auxiliar = new ArrayList<String>(5000);

	private static ArrayList<String> mini_url = new ArrayList<String>(5000); //armazena todas as miniurls
	private static ArrayList<String> p_url = new ArrayList<String>(5000);  //armazena todas as urls reais
	
	
	//diretorios de arquivos para ler e salvar
	private static File save_dir; //diretorio onde sao salvos os arquivos
	
	private static File arq_org = new File("links.csv"); //arquivo original
	private static File real_link = new File("links_reais.csv"); //arquivo com os links reais
	private static File user_fotos = new File("user_fotos.csv"); //arquivo com usuario e arquivos
	
	private static File hist = new File("hist"); //pasta para historico
	private static File control_file = new File (hist.toString() + OS + "control.dat"); //arquivo de historico
	
	private static String patternFile = "patterns.txt"; //diretorio de arquivo de padroes (olhar http://jsoup.org/)
	
	//inicio de tudo
	public static void main(String args[]) {
		
		switch (args.length) {
		
			case 0:
				System.out.println("java " + Crawler_simples2.class + " (arquivo de configuracao) <diretorio para salvar dados>");
				System.exit(0);
			case 1:
				if ((!read_file_config (new File(args[0]))) || (!save_dir.exists())) {
					System.out.println("java " + Crawler_simples2.class + " (arquivo de configuracao) <diretorio para salvar dados>");
					System.exit(0);
				}
				break;
			case 2:
				if (!read_file_config (new File(args[0]))) {
					System.out.println("java " + Crawler_simples2.class + " (arquivo de configuracao) <diretorio para salvar dados>");
					System.exit(0);
				}
				save_dir = new File(args[1]);
				if (!save_dir.exists()) {
					System.out.println("Diretorio nao existe!");
					System.exit(0);
				}
				break;
			default:
				System.out.println("java " + Crawler_simples2.class + " (arquivo de configuracao) <diretorio para salvar dados>");
				System.exit(0);
		}	
		
		
		//inicializa variaveis e diretorios
		int init_mini_url = 0;
		boolean flag1 = false;
		int count = 0;
		String dataPath = save_dir.toString(); 
		
		arq_org = new File(save_dir.toString() + OS + arq_org.toString());
    	real_link = new File(dataPath + OS + "links_reais.csv");
        user_fotos = new File(dataPath + OS + "user_fotos.csv");
    	hist = new File(dataPath + OS + "hist");
    	control_file = new File (hist.toString() + OS + "control.dat");
    	
		
        //cria arquivos e pastas
        if (!hist.exists())
        	hist.mkdir();
        
        //verifica se eh a primeira vez que roda ou se eh continuacao
        if (!control_file.exists()) {
		
        	if (user_fotos.exists() && user_fotos.isFile())
        		user_fotos.delete();
        
        	if (real_link.exists() && real_link.isFile())
        		real_link.delete();
        
        	save_dir = new File(dataPath + OS + "images");
        	if (!save_dir.exists())
        		save_dir.mkdir();      
        	
        	save_dir = new File(dataPath + OS + "pages");
        	if (!save_dir.exists())
        		save_dir.mkdir();      
        	
        	save_dir = new File(dataPath);
        }
        
        else { //carrega os dado salvos
        	int numeros[][] = read_historico(control_file);
        	init_mini_url = numeros[0][0]; //numero do ultimo link visitado
        	num_img = numeros[1][0];	
        	num_user = numeros[1][1];	
        	num_pages = numeros[1][2];
        	num_lines = numeros[1][3];
        	
        	if (numeros[0][0] == numeros[0][1])
        		flag1 = true;
        	
        	if (flag1)
        		estatisticas(user_fotos);
        		System.out.println("Todos os dados ja estao processados!");
        }
        
        
        if (!flag1) {
        
        	boolean flag_stop = false;
        	
        	// ler arquivo com endereços miniurls
        	ArrayList<String> links = read_file(arq_org);
       
        	//percorre todos as miniurls em busca dos links reais
        	for (count = init_mini_url; count < links.size(); count++) {
        	
        		String text = links.get(count);
        		StringTokenizer parts = new StringTokenizer(text,";");
    		
        		String user = parts.nextToken(); //nome do usuário
        		String dir_site = parts.nextToken(); //link do site
        		System.out.println(dir_site + " link " + (count+1) + " de " + links.size());
				
        		//entra na pagina e salva conteudo
        		save_page(user, dir_site);
        		
				//parada forcada do programa atraves de arquivo
	    		File stop_file = new File (hist.toString() + OS + "stop.dat");
	    		if (stop_file.exists()) {
	    			flag_stop = true;
	    			System.out.println("O programa foi forcado a terminar atraves do arquivo hist" + OS + "stop.dat.");
	    			System.out.println("Espere o programa salvar todos os arquivos.");
	    		}
	    			
        		//escreve o historico
        		if ((count > 0) && (((count % SALVE_INT) == 0) || (count+1 == links.size()) || (flag_stop))) {
        			int numeros [][] = new int [2][];
        			numeros[0] = new int [2];
        			numeros[1] = new int [4];
        			
        			numeros[0][0] = count+1; 
        			numeros[0][1] = links.size();
        			numeros[1][0] = num_img; 
        			numeros[1][1] = num_user;
        			numeros[1][2] = num_pages;
        			numeros[1][3] = num_lines;
        			
        			try {Thread.sleep(3000);}
        			catch (Exception ex){}
        			
        			write_historico(control_file,numeros);
        			
        			if (((EXIT_PROGRAM == true) && (count > 0)) || (flag_stop)) {
        				System.out.println("Programa parado. Arquivos salvos.");
        				System.exit(0);
        			}
        		}
        	}
        	
        	//gera arquivos de estatisticas
        	estatisticas(user_fotos);
        	give_usernumber(user_fotos, new File (save_dir.toString() + OS + "rel_user_photos.csv"));
			System.out.println("Todos os dados foram processados!");	
        }
	}

	
	//captura o link real de cada miniurl
    public static String crawler(String link) {
    	String page = "";
    	try {
    		page = Jsoup.connect(link).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true).timeout(WAIT_TIMEOUT*1000).get().baseUri();
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	return page;
    }
    
    
    //percorre os links reais para baixar as paginas e as imagens
    public static void save_page(String user, String miniurl) {
    	
		if (!mini_url.contains(miniurl))
		{
			String url_url = crawler(miniurl);  //captura o site real, caso seja uma miniurl

			if (!url_url.equals("")) {
				//escreve o nome do link real e o usuário associado
				write_file(real_link, user + ";" + miniurl + ";" + url_url + "\r\n", true);
				mini_url.add(miniurl);
			
		    	System.out.println("Saving page ...");	
		    	num_user++;
		    	
    			if ((!p_url.contains(url_url)) && (!home_pages.contains(url_url)))
    				save_ind_page(user,url_url);
    			
    			else //usuarios diferentes para a mesma pagina
    				tabela_correcao(user,url_url,url_url,true);
    		   			
    			p_url.add(url_url);
		    }
		}

		else { //se a mini url já foi visitada
			int ref = mini_url.indexOf(miniurl);
			String url_url = p_url.get(ref);
			
			//escreve o nome do link real e o usuário associado
			write_file(real_link, user + ";" + miniurl + ";" + url_url + "\r\n", true);
	
	    	num_user++;
	    	tabela_correcao(user,url_url,url_url,true);
		}	
    }
    
    
    //salva a página ou a imagem apontada no link real
    public static void save_ind_page(String user, String link)  {
        try {
        	
        	boolean imageType = false;
        	
        	String link2 = link.toLowerCase();
        	
        	for (int i = 0; i < fileType.length; i++) {
        		if (link2.contains(fileType[i])) {
        			imageType = true;
        			if(image_crawler(user,link,fileType[i],save_dir)) { //se o link for uma imagem, salva a imagem
            			num_img++;
            			write_file(user_fotos, user + ";"  + num_img + fileType[i] + ";" + link + ";" + link + "\r\n", true);
            			num_lines++;
        				
            			users.add(user + ":" + num_user); //usuario do link
        				save_fotos.add(num_img + fileType[i]); //imagem do link
        				save_fotos_real.add(link); //link da imagem
        				auxiliar.add(link); //link do site
        			}
        			break;
        		}
        	}
	    			
        	if (!imageType) { //se nao for imagem, salva a pagina
        		
        		//while(!isConnected (link)){} //verifica conexao com a internet
        		
        		String pageType = urlType(link);
        		
        		if (pageType.contains("text")) {
        			
        			URL my_url = new URL(link); 
        			
        		    URLConnection testConnection = my_url.openConnection();
        		    testConnection.setConnectTimeout(WAIT_TIMEOUT*1000);
        		    testConnection.setReadTimeout(WAIT_TIMEOUT*1000);
        			
        			BufferedReader br = new BufferedReader(new InputStreamReader(testConnection.getInputStream(),"UTF-8"));
        			//BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream(),"UTF-8"));
        			
        			String strTemp = "";
        			String text = "";
        			while(null != (strTemp = br.readLine()))
        				text = text.concat(strTemp + "\r\n");
        			
        			br.close();
            
        			link2 = link;
        		
        			link2 = link2.replaceAll("://","_");
        			link2 = link2.replaceAll("[/\\:*?\"<>|]","_");
        		
        			if (link2.length() >= 100) //evitar nomes muito longos
        				link2 = link2.substring(0,100);
        		
        			File file = new File(save_dir.toString() + OS  + "pages" + OS + link2 + "_" + (num_pages+1) + ".html");
            
        			num_pages++;
        		
        			write_file(file,text,false); //salva pagina da internet
        			Thread.sleep(WAIT_TIME); //tempo de espera entre requisicoes
        			
        			users.add(user + ":" + num_user);
        			save_fotos.add("");
        			save_fotos_real.add("");
        			auxiliar.add(link);
            
        			if ((patternFile.equals("")) || (!USE_PATTERN)) 
        				extractLinks(user,text,link);
        			else
        				extractLinksPatterns(user,text,link);
        		}
        	}
        }
        catch (Exception ex) {ex.printStackTrace();}
    }
	
    
    //descobre o tipo de link url
    public static String urlType(String link) {
    	String type = "";
       try {
          URL u = new URL(link);
          URLConnection uc = u.openConnection();
          type = uc.getContentType();
        }
       catch (Exception ex) {System.out.println(ex);} 
       return type;
    }
    
    
    //extrai link usando as definicoes no arquivo de patroes
    public static void extractLinksPatterns (String user, String rawPage, String link_origem) {
    	
    	ArrayList<String> patterns = read_file(new File(patternFile));
    	
    	ArrayList<String> sites = PatternFile.links_file(link_origem,rawPage,patterns);
    	
        for (int i = 0; i < sites.size(); i++) {
        	String site = sites.get(i);
        	
			if (!home_pages.contains(site)) {
				
	        	String sit = site.toLowerCase();
	        	
	        	for (int k = 0; k < fileType.length; k++) {
	        		if (sit.contains(fileType[k])) {
	        			if(image_crawler(user,site,fileType[k],save_dir)) { //se o link for uma imagem, salva a imagem
	            			num_img++;
	            			write_file(user_fotos, user + ";"  + num_img + fileType[k] + ";" + site + ";" + link_origem + "\r\n", true);
	            			num_lines++;
	            			
	            			home_pages.add(site);
	        				users.add(user + ":" + num_user);
	        				save_fotos.add(num_img + fileType[k]);
	        				save_fotos_real.add(site);
	        				auxiliar.add(site);
	        			}
	        			break;
	        		}
	        	}
			}
			else //usuarios diferentes para a mesma pagina
				tabela_correcao(user,site,link_origem,false);
    	}	
    }
    
    
    //extrai todos os links interessantes de cada pagina
    public static void extractLinks(String user, String rawPage, String link_origem) {
		
		ArrayList<String> sites = new ArrayList<String>(50);
		
		Document page = Jsoup.parse(rawPage);
		
        Elements links = page.select("a[href]");
        Elements media = page.select("[src]");
        Elements imports = page.select("link[href]");
        Elements metas = page.select("meta[property*=og:image]"); //instagram
    	Elements rel = page.select("[rel]"); //gazeta
        
        String site = "";
        
        for (Element src : imports) {
        	if (src.tagName().equals("img")) {
        		site = src.attr("abs:href");
    			if (!sites.contains(site))
    				sites.add(site);
        	}  
        }
        
        for (Element src : media) {
        	if (src.tagName().equals("img")) {
        		site = src.attr("abs:src");
    			if (!sites.contains(site))
    				sites.add(site);
        	}
        }

        for (Element src : links) {
        	if (src.tagName().equals("img")) {
        		site = src.attr("abs:href");
    			if (!sites.contains(site))
    				sites.add(site);
        	}  
        }
        
    	for (Element src : metas) {
        	site = src.attr("abs:content");
    		if (!sites.contains(site))
    			sites.add(site);
        }

        for (Element src : rel) {
        	if (src.tagName().equals("img")) {
        		site = src.attr("abs:rel");
    			if (!site.contains(site))
    				sites.add(site);
        	}
        }
        
        for (int i = 0; i < sites.size(); i++) {
        	site = sites.get(i);
        	
			if (!home_pages.contains(site)) {
        	
				String sit = site.toLowerCase();
				for (int k = 0; k < fileType.length; k++) {
					if (sit.contains(fileType[k])) {
						if(image_crawler(user,site,fileType[k],save_dir)) { //se o link for uma imagem, salva a imagem
							num_img++;
							write_file(user_fotos, user + ";"  + num_img + fileType[k] + ";" + site + ";" + link_origem + "\r\n", true);
							num_lines++;
							
							home_pages.add(site);
							users.add(user + ":" + num_user);
							save_fotos.add(num_img + fileType[k]);
							save_fotos_real.add(site);
							auxiliar.add(site);
						}
						break;
					}
				}
			}
			else //usuarios diferentes para a mesma pagina
				tabela_correcao(user,site,link_origem,false);
    	}	
    }
    
    
    //baixa a imagem presente em um link
    public static boolean image_crawler(String user, String link, String extension, File save_dir) {
    	
    	boolean validImage = false;
    	
    	try {  
    		
    		//while(!isConnected (link)){} //verifica conexao com a internet
    		
    		// 4MB de buffer  
    		final int BUFFER_SIZE = 2048 * 2048;    	
    		
    		String nomeArquivo = save_dir.toString() + OS + "images" + OS + (num_img+1) + extension;
    		
    	    URL url = new URL(link);  
    	    URLConnection testConnection = url.openConnection();
		    testConnection.setConnectTimeout(WAIT_TIMEOUT*1000);
		    testConnection.setReadTimeout(WAIT_TIMEOUT*1000);
		    
		    BufferedInputStream stream = new BufferedInputStream(testConnection.getInputStream(), BUFFER_SIZE);
    		
    		//BufferedInputStream stream = new BufferedInputStream(url.openStream(), BUFFER_SIZE);  
    		BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(nomeArquivo));  
    		byte buf[] = new byte[BUFFER_SIZE];  
    		int numBytesRead;  
    		
    		do {  
    			numBytesRead = stream.read(buf);  
    			if (numBytesRead != -1) {  
    				fos.write(buf, 0, numBytesRead);  
    			}  
    		} while (numBytesRead != -1);
    		
    		fos.flush();  
    		fos.close();  
    		stream.close();  
    		buf = null; 
    		
    		
    		File rem_img = new File(nomeArquivo);
    		do {
    			Thread.sleep(100); //0.1 second
    		}
    		while (!rem_img.exists());

    		//Remove imagens menores do que um tamanho de bits ou de dimensoes
    		BufferedImage bimg = ImageIO.read(rem_img);
    		if ((rem_img.length() < TAM_MIN*1024) || (bimg.getHeight() < ALT_MIN) || (bimg.getHeight() < LAR_MIN)){
    			while(!rem_img.delete()){
    				Thread.sleep(100);
    				if (!rem_img.exists())
    					break;
    			}
    		}
    		else {
    			validImage = true;
    			Thread.sleep(WAIT_TIME); //tempo de espera entre requisicoes
    		}
    	} 
    	    
    	catch (MalformedURLException e1) {e1.printStackTrace();}
		catch (Exception e) {e.printStackTrace();}  
    	
    	return validImage;
    } 
    
    
    //corrige a tabela user-fotos quando diferentes usuarios apontam para as mesmas imagens
    public static void tabela_correcao(String user, String link, String link_origem, boolean recursivo) {
    	
    	List<String> temp;
    	int aux, aux2;
    	String user_num; 
    	
    	if (recursivo) {
    		aux = auxiliar.indexOf(link);
    		if (aux != -1) {
    			user_num = users.get(aux);
    			
    			aux = aux2 = 0;
    			temp = users.subList(aux2,users.size());
  
    			while ((aux = temp.indexOf(user_num)) != -1) {
    				aux2 = aux2 + aux;
    				if (!save_fotos.get(aux2).isEmpty()) {
    					write_file(user_fotos, user + ";" + save_fotos.get(aux2) + ";" + save_fotos_real.get(aux2) + ";" + link_origem + "\r\n", true);
    					num_lines++;
    				}
    					
    				aux2++;
    				if (aux2 >= users.size())
    					break;
    				else
    					temp = users.subList(aux2,users.size());
    			}
    		}
    	}
    	
    	else {
    		aux = auxiliar.indexOf(link);
    		if (aux != -1) {
    			write_file(user_fotos, user + ";" + save_fotos.get(aux) + ";" + save_fotos_real.get(aux) + ";" +  link_origem + "\r\n", true);
    			num_lines++;
    		}
    	}
    }
    
    
    //ler aquivo de historico
    public static int[][] read_historico(File file) {
    
    	int aux, aux2;
    	int [][] numeros = new int [2][];
		numeros[0] = new int [2];
		numeros[1] = new int [4];
    	
    	ArrayList<String> dados = read_file(file);
    	
    	StringTokenizer parts = new StringTokenizer(dados.get(0),";");
    	numeros[0][0] = Integer.parseInt(parts.nextToken()); //numero de miniurls processados
    	numeros[0][1] = Integer.parseInt(parts.nextToken()); //numero total de miniurls
         	
     	//verfica se todos os dados foram processados
     	if ((numeros[0][0] < numeros[0][1])) {
     	
     		parts = new StringTokenizer(dados.get(1),";");
     		numeros[1][0] = Integer.parseInt(parts.nextToken()); //numero de imagens (num_img)
     		numeros[1][1] = Integer.parseInt(parts.nextToken()); //numero de usuarios (num_user)
     		numeros[1][2] = Integer.parseInt(parts.nextToken()); //numero de pages (num_pages)
     		numeros[1][3] = Integer.parseInt(parts.nextToken()); //numero de linhas (num_lines)
     		
     		aux = Integer.parseInt(dados.get(2)); //numero de home_pages
     		for (int count = 3; count < aux+3; count++)
     			home_pages.add(dados.get(count));
    	
     		aux = aux + 3;
     		aux2 = Integer.parseInt(dados.get(aux)); //numero de users
     		for (int count = aux+1; count < aux2+aux+1; count++)
     			users.add(dados.get(count));
    	
     		aux = aux2 + aux + 1;
     		aux2 = Integer.parseInt(dados.get(aux)); //numero de fotos
     		for (int count = aux+1; count < aux2+aux+1; count++) {
     			String fotos = dados.get(count); //parts.nextToken();
     			if (fotos.equals("-"))
     				save_fotos.add("");
     			else
     				save_fotos.add(fotos);
     		}
     		
     		aux = aux2 + aux + 1;
     		aux2 = Integer.parseInt(dados.get(aux)); //numero de fotos (link real)
     		for (int count = aux+1; count < aux2+aux+1; count++) {
     			String fotos = dados.get(count); //parts.nextToken();
     			if (fotos.equals("-"))
     				save_fotos_real.add("");
     			else
     				save_fotos_real.add(fotos);
     		}
    	
     		aux = aux2 + aux + 1;
     		aux2 = Integer.parseInt(dados.get(aux)); //numero de auxiliar
     		for (int count = aux+1; count < aux2+aux+1; count++)
     			auxiliar.add(dados.get(count));
     		
     		aux = aux2 + aux + 1;
     		aux2 = Integer.parseInt(dados.get(aux)); //numero de mini_url
     		for (int count = aux+1; count < aux2+aux+1; count++)
     			mini_url.add(dados.get(count));
     		
     		aux = aux2 + aux + 1;
     		aux2 = Integer.parseInt(dados.get(aux)); //numero de p_url
     		for (int count = aux+1; count < aux2+aux+1; count++)
     			p_url.add(dados.get(count));
     	}
    	return numeros;
    }
    
    
    //escreve arquivo de historico
    public static void write_historico(File file, int [][] numeros) {
    	
    	String texto = "";

    	texto = texto.concat(numeros[0][0] + ";" + numeros[0][1] + "\r\n");
    	texto = texto.concat(numeros[1][0] + ";" + numeros[1][1] + ";" + numeros[1][2] + ";" + numeros[1][3] + "\r\n");
    	write_file(file,texto,false);
    	
    	texto = "";
        texto = texto.concat(home_pages.size() + "\r\n");
        for (int count = 0; count < home_pages.size(); count++) {
        	texto = texto.concat(home_pages.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (home_pages.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}
        }
        
        texto = "";
        texto = texto.concat(users.size() + "\r\n");
        for (int count = 0; count < users.size(); count++) {
        	texto = texto.concat(users.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (users.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}
        }
        
    	
        texto = "";
        texto = texto.concat(save_fotos.size() + "\r\n");
        for (int count = 0; count < save_fotos.size(); count++) {
        	if (save_fotos.get(count).isEmpty())
        		texto = texto.concat("-\r\n");
        	else
        		texto = texto.concat(save_fotos.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (save_fotos.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}
        }
        
        texto = "";
        texto = texto.concat(save_fotos_real.size() + "\r\n");
        for (int count = 0; count < save_fotos_real.size(); count++) {
        	if (save_fotos_real.get(count).isEmpty())
        		texto = texto.concat("-\r\n");
        	else
        		texto = texto.concat(save_fotos_real.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (save_fotos_real.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}	
        }
        
        texto = "";
        texto = texto.concat(auxiliar.size() + "\r\n");
        for (int count = 0; count < auxiliar.size(); count++) {
        	texto = texto.concat(auxiliar.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (auxiliar.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}		
        }
        
        texto = "";
        texto = texto.concat(mini_url.size() + "\r\n");
        for (int count = 0; count < mini_url.size(); count++) {
        	texto = texto.concat(mini_url.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (mini_url.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}		
        }
        
        texto = "";
        texto = texto.concat(p_url.size() + "\r\n");
        for (int count = 0; count < p_url.size(); count++) {
        	texto = texto.concat(p_url.get(count) + "\r\n");
        	
        	if ((count % 100 == 0) || (count == (p_url.size()-1))) {
        		write_file(file,texto,true);
        		texto = "";
        	}	
        }
    }
    
    
    //calcula estatisticas basicas dos dados
    public static void estatisticas(File file) {
    	
    	ArrayList<String> users_num = new ArrayList<String>();
    	ArrayList<String> fotos_num = new ArrayList<String>();
    	ArrayList<String> links_num = new ArrayList<String>();
    	
    	ArrayList<Integer> users_count = new ArrayList<Integer>();
    	ArrayList<Integer> fotos_count = new ArrayList<Integer>();
    	ArrayList<Integer> links_count = new ArrayList<Integer>();
    	
    	ArrayList<String> texto = read_file(file);
    	
    	int pos_letter;
    	
    	for (int count = 0; count < texto.size(); count++) {
    		StringTokenizer parts = new StringTokenizer(texto.get(count),";");
    		
    		//nome dos usuarios
    		String part_texto = parts.nextToken();
    		if(!users_num.contains(part_texto)) {
    			users_num.add(part_texto);
    			users_count.add(1);
    		}
    		else {
    			int i = users_num.indexOf(part_texto);
    			users_count.set(i,users_count.get(i)+1);
    		}
    		
    		//nome das fotos
    		part_texto = parts.nextToken();
    		if(!fotos_num.contains(part_texto)) {
    			fotos_num.add(part_texto);
    			fotos_count.add(1);
    		}
    		else {
    			int i = fotos_num.indexOf(part_texto);
    			fotos_count.set(i,fotos_count.get(i)+1);
    		}
    		
    		//link das fotos
    		parts.nextToken();
    		
    		//link dos sites
    		part_texto = parts.nextToken();
    		
    		pos_letter = part_texto.indexOf("//");
    		if (pos_letter != -1) 
    			part_texto = part_texto.substring(pos_letter+2);
    		
    		pos_letter = part_texto.indexOf("/");
    		if (pos_letter != -1) 
    			part_texto = part_texto.substring(0,pos_letter);
 
    		if(!links_num.contains(part_texto)) {
    			links_num.add(part_texto);
    			links_count.add(1);
    		}
    		else {
    			int i = links_num.indexOf(part_texto);
    			links_count.set(i,links_count.get(i)+1);
    		}
    	}
    	
    	//escreve arquivo de estatisticas
    	List<Integer> ordena = new ArrayList<Integer>();
    	
    	for (int u = 0; u < users_count.size(); u++)
    		ordena.add(users_count.get(u));
    	Collections.sort(ordena); //ordem ascendente
    	
    	String saida = "USUARIOS QUE MAIS POSTARAM\r\n";
    	int tam = users_count.size();
    	for (int j = tam-1; j >= 0; j--) {
    		int k = users_count.indexOf(ordena.get(j));
    		saida = saida.concat(users_num.get(k) + ";" +  ordena.get(j) + "\r\n");
    		users_count.set(k, -100);
    	}
    	saida = saida.concat("\r\n\r\n");
    	File stat_file = new File(save_dir + OS + "Est_Usuarios.csv");
    	write_file(stat_file,saida,false);
    	ordena.clear();
    	
    	for (int u = 0; u < fotos_count.size(); u++)
    		ordena.add(fotos_count.get(u));
    	Collections.sort(ordena); //ordem ascendente
    	
    	saida = "IMAGENS MAIS POSTADAS\r\n";
    	tam = fotos_count.size();
    	for (int j = tam-1; j >= 0; j--) {
    		int k = fotos_count.indexOf(ordena.get(j));
    		saida = saida.concat("=hiperlink(\"images/" + fotos_num.get(k) + "\")"  + ";" + fotos_num.get(k) + ";" +  ordena.get(j) + "\r\n");
    		fotos_count.set(k, -100);
    	}
    	saida = saida.concat("\r\n\r\n");
    	stat_file = new File(save_dir + OS + "Est_Imagens.csv");
    	write_file(stat_file,saida,false);
    	ordena.clear();    	
    	
    	for (int u = 0; u < links_count.size(); u++)
    		ordena.add(links_count.get(u));
    	Collections.sort(ordena); //ordem ascendente
    	
    	saida = "SITES MAIS POSTADOS\r\n";
    	tam = links_count.size();
    	for (int j = tam-1; j >= 0; j--) {
    		int k = links_count.indexOf(ordena.get(j));
    		saida = saida.concat(links_num.get(k) + ";" +  ordena.get(j) + "\r\n");
    		links_count.set(k, -100);;
    	}
    	stat_file = new File(save_dir + OS + "Est_Sites.csv");
    	write_file(stat_file,saida,false);
    	
    }
    
    //associa numero ao nome dos usuarios
    public static void give_usernumber(File file_origem, File file_destino) {
    	
    	ArrayList<String> users_num = new ArrayList<String>(5000);
    	
    	ArrayList<String> texto = read_file(file_origem);
    	
    	for (int count = 0; count < texto.size(); count++) {
    		StringTokenizer parts = new StringTokenizer(texto.get(count),";");
    		
    		//nome dos usuarios
    		String part_texto = parts.nextToken();
    		if(!users_num.contains(part_texto))
    			users_num.add(part_texto);
    	}
    	
    	String saida;
    	for (int count = 0; count < texto.size(); count++) {
    		StringTokenizer parts = new StringTokenizer(texto.get(count),";");
    	
    		saida = (users_num.indexOf(parts.nextToken())+user_label) + ";" + texto.get(count) + "\r\n";
    		
    		if (count == 0)
    			write_file(file_destino,saida,false);
    		else
    			write_file(file_destino,saida,true);
    	} 
    }
    
    
    //ler arquivo de configuracao
    public static boolean read_file_config (File file) {
    	
    	int linha = 0;
    	boolean fileExists = false;
    	
    	if (file.exists()) {
    	
    		ArrayList<String> configs = read_file(file);
    	
    		for (int i = 0; i < configs.size(); i++) {
			
    			String formats = configs.get(i);
    			if ((formats.startsWith("//")) || (formats.trim().length() == 0))  //comments
    				continue;
    		
    			linha++;
    		
    			formats = formats.substring(formats.indexOf(':')+1).trim();
    		
    			if (formats.length() > 0) {
    		
    				switch (linha) {
    					case 1: //diretorio para armazenar os dados
    						save_dir = new File(formats);
    						break;
    					case 2: //Nome de arquivo com os links e usuarios	
    						arq_org = new File(formats);
    						break;
    					case 3: //Numero de iterações (paginas visitadas) para salvar histórico
    						SALVE_INT = Integer.parseInt(formats);
    						break;
    					case 4: //Termina programa após salvar histórico
    						EXIT_PROGRAM = Boolean.parseBoolean(formats);
    						break;
    					case 5: //Tamanho mínimo (em kB) das imagem armazenada
    						TAM_MIN = Long.parseLong(formats);
    						break;
    					case 6: //Tamanho mínimo (em pixels) das imagem armazenada
    						ALT_MIN = Integer.parseInt(formats);
    						break;
    					case 7: //Largura mínima (em pixels) das imagem armazenada
    						LAR_MIN = Integer.parseInt(formats);
    						break;
    					case 8: //Formato de imagens capturadas
    						StringTokenizer parts = new StringTokenizer(formats,";");
    						int num_parts = parts.countTokens();
    						fileType = new String[num_parts];
    						for (int k = 0; k < num_parts; k++)
    							fileType[k] = "." + parts.nextToken().toLowerCase();
    						break;
    					case 9: //Tempo de espera entre requisições (em milissegundos)
    						WAIT_TIME = Long.parseLong(formats);
    						break;
    					case 10: //Timeout de conexão
    						WAIT_TIMEOUT = Integer.parseInt(formats);
    						break;
    					case 11: //Sequencia de imagens, páginas e usuários começa a partir de
    						num_img = Integer.parseInt(formats) - 1;
    						num_pages = num_img;
    						user_label = Integer.parseInt(formats);
    						break;
    					case 12: //Usar o arquivo de padrões?
    						USE_PATTERN = Boolean.parseBoolean(formats);
    						break;
    					case 13: //nome do arquivo de padrões
    						patternFile = formats;
    				}
    			}
    		}
    		fileExists = true;
    	}
    	return fileExists;
    }
    
    
    public static boolean isConnected (String link) {
    	boolean output = false;
    	try{  
    		java.net.URL mandarMail = new java.net.URL(link);  
    		java.net.URLConnection conn = mandarMail.openConnection();  
    	  
    		java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) conn;  
    		httpConn.connect();  
    		int x = httpConn.getResponseCode(); 
    		
    		if(x == 200){output = true;} 
    		else {System.out.println("Desconectado");}
    	}  
    	catch(java.net.MalformedURLException urlmal){}  
    	catch(java.io.IOException ioexcp){}
    	return output;
    }
    
    
    //metodo para leitura de arquivo
    public static ArrayList<String> read_file(File file) {
    	
    	ArrayList<String> links = new ArrayList<String>(100);
    	try{   
    		String linha = "";
    		
    		//FileReader reader = new FileReader(file);  
    		InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"UTF-8");  
    		BufferedReader br = new BufferedReader(reader);  
    		
    		while(br.ready()){  
    			linha = br.readLine();  
    			linha = linha.replaceAll("[^\\p{ASCII}]", ""); 
    			links.add(linha);
    			//System.out.println(linha);  
    		}  
    		br.close();  
    		reader.close();
    	}
    	catch(IOException ioe) {ioe.printStackTrace();}
		return links;
    }   
	
    
    //metodo para escrita de arquivo
    public static void write_file(File file, String texto, boolean concat) {  
    	try {

            //FileWriter fileWriter = new FileWriter(file,concat);
    		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file,concat), "UTF-8");
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.print(texto);
            printWriter.flush();
 
    		//fileWriter.write(texto);
    		
            printWriter.close();
    		fileWriter.close();
        } 
    	catch (IOException e) {e.printStackTrace();}
    }  
        
}
