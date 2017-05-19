package com.tittletize.expander.indexing;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EntityIndex {
    private FSDirectory index;
    private IndexWriter indexWriter;
    private IndexWriterConfig indexConfig;
    private Analyzer analyzer;

    private TrackingIndexWriter trackingIndexWriter;
    private ReferenceManager<IndexSearcher> searcherManager;
    private ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread;

    public EntityIndex() throws IOException {
        File t = new File("entities.lcn");
        if (t.exists()) {
            t.delete();
        }

        this.analyzer = new StandardAnalyzer();
        this.indexConfig = new IndexWriterConfig(this.analyzer);
        this.index = FSDirectory.open(Paths.get(new File("entities.lcn").toURI()));
        this.indexWriter = new IndexWriter(this.index, this.indexConfig);
        this.trackingIndexWriter = new TrackingIndexWriter(indexWriter);
        this.searcherManager = new SearcherManager(indexWriter, true, null);
        this.nrtReopenThread = new ControlledRealTimeReopenThread<>(trackingIndexWriter, searcherManager, 1.0, 0.1);

        nrtReopenThread.setName("NRT Reopen Thread");
        nrtReopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
        nrtReopenThread.setDaemon(true);
        nrtReopenThread.start();

    }

    public void appendDocument(String term, String entity) throws IOException {
        Document document = new Document();
        document.add(new TextField("term", term, TextField.Store.YES));
        document.add(new TextField("entity", entity, TextField.Store.YES));
        this.indexWriter.updateDocument(new Term("term", term), document);
    }

    public FSDirectory getIndex() {
        return index;
    }

    @SuppressWarnings("Duplicates")
    public String knowledgeFor(String term) throws IOException, ParseException {
        IndexSearcher searcher = searcherManager.acquire();
        QueryParser qp = new QueryParser("term", this.analyzer);
        Query query = qp.parse(term);
        TopDocs docs = searcher.search(query, 1);

        if (docs.scoreDocs.length == 0) {
            return null;
        }

        searcherManager.release(searcher);
        return searcher.doc(docs.scoreDocs[0].doc).get("entity");

    }

    public static void main(String args[]) throws IOException, ParseException, InterruptedException {
        Map<String, String> knowledge = new HashMap<>();
        knowledge.put("NY", "New York City");
        knowledge.put("NYork", "New York City");
        knowledge.put("Big Apple", "New York City");
        knowledge.put("NYC", "New York City");

        EntityIndex index = new EntityIndex();
        for (String term : knowledge.keySet()) {
            index.appendDocument(term, knowledge.get(term));
        }

        Thread.sleep(5000);

        System.out.println(index.knowledgeFor("NYC"));
    }

}
