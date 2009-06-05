/**
 * 
 */
package Bingo.index;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import Bingo.util.HTMLDocParser;

/**
 * @author zhang
 *
 */
public class IndexManager {
    //the directory that stores HTML files 
    private final String dataDir  = "c:\\dataDir";

    //the directory that is used to store a Lucene index
    private final String indexDir = "c:\\indexDir";

    /**
     * create index
     */
    public boolean createIndex() throws IOException{
        if(true == ifIndexExist()){
            return true;	
        }
        File dir = new File(dataDir);
        if(!dir.exists()){
            return false;
        }
        File[] htmls = dir.listFiles();
        //Directory fsDirectory = FSDirectory.getDirectory(indexDir, true);
        Analyzer  analyzer    = new StandardAnalyzer();
        IndexWriter indexWriter = new IndexWriter(indexDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        for(int i = 0; i < htmls.length; i++){
            String htmlPath = htmls[i].getAbsolutePath();

            if(htmlPath.endsWith(".html") || htmlPath.endsWith(".htm")){
        		addDocument(htmlPath, indexWriter);
        	}
        }
        indexWriter.optimize();
        indexWriter.close();
        return true;
    }

    /**
     * Add one document to the Lucene index
     */
    public void addDocument(String htmlPath, IndexWriter indexWriter){
        HTMLDocParser htmlParser = new HTMLDocParser(htmlPath);
        String path    = htmlParser.getPath();
        String title   = htmlParser.getTitle();
        Reader content = htmlParser.getContent();

        Document document = new Document();
        document.add(new Field("path",path,Field.Store.YES,Field.Index.NO));
        document.add(new Field("title",title,Field.Store.YES,Field.Index.ANALYZED));
        document.add(new Field("content",content));
        try {
              indexWriter.addDocument(document);
    } catch (IOException e) {
              e.printStackTrace();
          }
    }

    /**
     * judge if the index exists already
     */
    public boolean ifIndexExist(){
        File directory = new File(indexDir);
        if(0 < directory.listFiles().length){
            return true;
        }else{
            return false;
        }
    }

    public String getDataDir(){
        return this.dataDir;
    }

    public String getIndexDir(){
        return this.indexDir;
    }

}
