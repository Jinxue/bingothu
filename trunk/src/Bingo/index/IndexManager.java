/**
 * 
 */
package Bingo.index;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import Bingo.spider.VideoInfo;
import Bingo.util.HTMLDocParser;

/**
 * @author zhang
 *
 */
public class IndexManager {
    //the directory that stores HTML files 
    private String dataDir  = IndexConstant.dataDir;

    //the directory that is used to store a Lucene index
    private String indexDir = IndexConstant.indexDir;
    
    // The index writer
    private IndexWriter indexWriter;
    
    //The index directory 
    public IndexManager() throws Exception{
    	initIndexManager();
    }
    
    //The index directory 
    public IndexManager(String indexDir) throws Exception{
    	this.indexDir = indexDir;
    	initIndexManager();
    }
    
    private void initIndexManager() throws Exception{
    	if(ifIndexExist() == true){
    		return;
    	}
    	FSDirectory dir = FSDirectory.getDirectory(new File(indexDir));
        Analyzer  analyzer    = new ChineseAnalyzer();
        indexWriter = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
    }

    // Insert the index to index DB
    public void addIndex(VideoInfo videoInfo) throws CorruptIndexException, IOException{
        Document document = new Document();
        document.add(new Field("keyWord",videoInfo.getKeyWord(),Field.Store.YES,Field.Index.ANALYZED));
        document.add(new Field("title",videoInfo.getTitle(),Field.Store.YES,Field.Index.ANALYZED));
        document.add(new Field("description",videoInfo.getDescription(),Field.Store.YES,Field.Index.ANALYZED));
        document.add(new Field("url", videoInfo.getUrl(), Field.Store.YES, Field.Index.NO));
        document.add(new Field("imgUrl", videoInfo.getImgUrl(), Field.Store.YES, Field.Index.NO));
        
        try {
              indexWriter.addDocument(document);
        } catch (IOException e) {
              e.printStackTrace();
        }
        indexWriter.optimize();
    }
    
    // Close the Index manager
    public void closeIndex() throws CorruptIndexException, IOException{
    	indexWriter.close();
    }

    /**
     * create index form local directory
     * @throws Exception 
     */
    public boolean createIndexFromLocal() throws Exception{
        if(true == ifIndexExist()){
            return true;	
        }
        File dir = new File(dataDir);
        if(!dir.exists())
			throw new Exception(" Data directory is not existed!");

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
     * @throws Exception 
     */
    public boolean ifIndexExist() throws Exception{
        File directory = new File(indexDir);
        System.out.println(System.getProperty("user.dir"));
        if (!directory.exists())
			throw new Exception(" Index directory is not existed!");

        // We must exclude the .svn directory
        if(1 < directory.listFiles().length){
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
