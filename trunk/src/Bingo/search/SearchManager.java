package Bingo.search;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import Bingo.index.IndexManager;
import Bingo.spider.VideoInfo;

public class SearchManager {
    private String searchWord;
    
    private IndexManager indexManager;
    
    private Analyzer analyzer;
    
    public SearchManager(String searchWord) throws Exception{
        this.searchWord   =  searchWord;
        this.indexManager =  new IndexManager();
        this.analyzer     =  new StandardAnalyzer();
    }
    
    /**
     * do search
     * @throws Exception 
     */
    public ArrayList<VideoInfo> search() throws Exception{
    	ArrayList<VideoInfo> searchResult = new ArrayList<VideoInfo> ();
        if(false == indexManager.ifIndexExist()){
        try {
            if(false == indexManager.createIndexFromLocal()){
                return searchResult;
            }
        } catch (IOException e) {
          e.printStackTrace();
          return searchResult;
        }
        }
    	
        IndexSearcher indexSearcher = null;

        try{
            indexSearcher = new IndexSearcher(indexManager.getIndexDir());
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        QueryParser queryParser = new QueryParser("content",analyzer);
        Query query = null;
        try {
            query = queryParser.parse(searchWord);
        } catch (ParseException e) {
          e.printStackTrace();
        }
        if(null != query && null != indexSearcher){			
            try {
                ScoreDoc[] hits = indexSearcher.search(query,null,100).scoreDocs;
                for(int i = 0; i < hits.length; i ++){
                	VideoInfo resultBean = new VideoInfo();
                    Document hitDoc = indexSearcher.doc(hits[i].doc);
                    resultBean.setUrl(hitDoc.get("path"));
                    resultBean.setTitle(hitDoc.get("title"));
                    searchResult.add(resultBean);
                }
                indexSearcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return searchResult;
    }
}
