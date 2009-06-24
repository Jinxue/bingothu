package Bingo.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.store.FSDirectory;

import Bingo.index.IndexManager;
import Bingo.spider.VideoInfo;

public class SearchManager {
    private String searchWord;
    
    private IndexManager indexManager;
    
    private Analyzer analyzer;
    
    public SearchManager(String searchWord) throws Exception{
        this.searchWord   =  searchWord;
        this.indexManager =  new IndexManager();
        this.analyzer     =  new ChineseAnalyzer();
    }
    
    /**
     * do search
     * @throws Exception 
     */
    public ArrayList<VideoInfo> search() throws Exception{
    	ArrayList<VideoInfo> searchResult = new ArrayList<VideoInfo> ();
//        if(false == indexManager.ifIndexExist()){
//        try {
//            if(false == indexManager.createIndexFromLocal()){
//                return searchResult;
//            }
//        } catch (IOException e) {
//          e.printStackTrace();
//          return searchResult;
//        }
//        }
    	
    	if(false == indexManager.ifIndexExist()){
			throw new Exception(" There is no record indexed!");
    	}
    	
        IndexSearcher indexSearcher = null;

        try{
        	FSDirectory dir = FSDirectory.getDirectory(new File(indexManager.getIndexDir()));
            indexSearcher = new IndexSearcher(dir);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        QueryParser queryParser = new QueryParser("keyWord",analyzer);
        Query query = null;
        try {
            query = queryParser.parse(searchWord);
        } catch (ParseException e) {
          e.printStackTrace();
        }
//        query = new QueryParser("description", analyzer).parse(searchWord);
        if(null != query && null != indexSearcher){			
            try {
            	//Hits aHits = indexSearcher.search(query);
                ScoreDoc[] hits = indexSearcher.search(query,100).scoreDocs;
                for(int i = 0; i < hits.length; i ++){
                	VideoInfo resultBean = new VideoInfo();
                    Document hitDoc = indexSearcher.doc(hits[i].doc);
                    resultBean.setTitle(hitDoc.get("title"));
                    resultBean.setUrl(hitDoc.get("url"));
                    resultBean.setImgUrl(hitDoc.get("imgUrl"));
                    resultBean.setDescription(hitDoc.get("description"));
                    resultBean.setSource(hitDoc.get("source"));
                    searchResult.add(resultBean);
                }
                
                // Another search method
//                TopDocCollector collector = new TopDocCollector(100);
//                indexSearcher.search(query, collector);
//                int total = collector.topDocs().totalHits;
//                hits = collector.topDocs().scoreDocs;
                
                indexSearcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return searchResult;
    }
}
