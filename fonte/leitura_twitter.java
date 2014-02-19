
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;


public class leitura_twitter
{
		
	static String OS = System.getProperty("file.separator");
	
	public static void main(String[] args) throws Exception
	{
		if (args.length != 1)
			System.out.println("É necessario informar o nome do arquivo com os dados.");
		
		else{	
			File arquivo = new File(args[0]);
			System.out.println("Processou " + texto_read(arquivo) + " linhas.");
		}
	}
		
		
	public static int texto_read(File arquivo)
	{
		
        String linha;
        String comment;
        String author;
        String saida = "";
        int num = 0;
        Vector<String> links = new Vector<String>(5,5);
        
        try
        {
            //Reader filein = new FileReader(arquivo);   //arquivo lido
        	InputStreamReader filein = new InputStreamReader(new FileInputStream(arquivo),"UTF-8"); 
            BufferedReader filein2 = new BufferedReader(filein);

            while ((linha = filein2.readLine()) != null){

            	StringTokenizer parte = new StringTokenizer(linha,"|");
            	
            	comment = parte.nextToken();
            	author = parte.nextToken();

            	//ignora outras partes
            	
            	parte = new StringTokenizer(comment," ");
            	
            	while (parte.hasMoreTokens()){
            		
            		String palavra = parte.nextToken();
            		
            		if (palavra.startsWith("http")){
            			
            			if (palavra.length() < 12)
            				continue;
            			
            			if (palavra.contains("t.co")) {
            			
            				String test = palavra.substring(palavra.lastIndexOf('/')+1);
            			
            				if (test.length() < 10)
            					continue;
            			
            				if (test.length() > 10)
            					palavra = palavra.substring(0,palavra.length() - (test.length()-10));
            			}
            			
            			links.add(new String(palavra));	
            		}
            	}
            	
            	for (int i = 0; i < links.size(); i++)
            		saida = saida.concat(author + ";" + links.elementAt(i).toString() + "\r\n");
            	
            	file_writer(saida,arquivo.getAbsolutePath().substring(0,arquivo.getAbsolutePath().lastIndexOf(OS)) + OS + "links.csv");
            	
            	saida = "";
            	links.clear();
            	
            	num++;
            }

            filein2.close();
            filein.close();
           
        }

        catch(FileNotFoundException e)
        {
        	System.out.println("Arquivo nao encontrado");
        }
        catch(IOException e)
        {
        	System.out.println("Arquivo com defeito");
        }
        
        return num;   
	}
	
	
	
	public static void file_writer(String conteudo, String filename){
		
		try{
		
			//FileWriter fw = new FileWriter(filename,true);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(filename,true), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter saida = new PrintWriter(bw);
			saida.write(conteudo);
			saida.close();
		}
		
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}

	}
	
	
}
