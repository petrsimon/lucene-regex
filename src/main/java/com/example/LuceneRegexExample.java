package com.example;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.util.automaton.Operations;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.lucene.util.automaton.RegExp.*;

public class LuceneRegexExample {
    public static void main(String[] args) throws IOException {
        KeywordAnalyzer analyzer = new KeywordAnalyzer();
//        StandardAnalyzer analyzer = new StandardAnalyzer();

        Directory index = new ByteBuffersDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter writer = new IndexWriter(index, config);

        indexDocument(writer, "abc");
        indexDocument(writer, "ABC");
        indexDocument(writer, "xyz");
        indexDocument(writer, "123");
        indexDocument(writer, "ς");
        writer.close();

        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        StoredFields storedFields = reader.storedFields();

        test("[abc]+", searcher, storedFields);
        test("[ABC]+", searcher, storedFields);
        test("[a-c]+", searcher, storedFields);
        test("[A-C]+", searcher, storedFields);
        test("abc", searcher, storedFields);
        test("ABC", searcher, storedFields);
        test("A.*", searcher, storedFields);
        test("a.*", searcher, storedFields);
        test("Σ", searcher, storedFields);
        test("ς", searcher, storedFields);

        Pattern p = Pattern.compile("[a-c]+", CASE_INSENSITIVE);
        System.out.println(p.matcher("abc").matches());
        System.out.println(p.matcher("ABC").matches());
    }

    private static void test(String queryString, IndexSearcher searcher, StoredFields storedFields) throws IOException {
        Query query1 = createRegexpQuery("content", queryString);
        TopDocs topDocs = searcher.search(query1, 10);

        System.out.println("\nQuery: " + queryString);
        System.out.println("Number of documents found: " + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println("Found document: " +  storedFields.document(scoreDoc.doc).get("content"));
        }
    }

    private static void indexDocument(IndexWriter writer, String text) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("content", text, Field.Store.YES));
        writer.addDocument(doc);
    }

    private static Query createRegexpQuery(String field, String pattern) {
        Term term = new Term(field, pattern);
        return new RegexpQuery(term, ALL, ASCII_CASE_INSENSITIVE | CASE_INSENSITIVE, RegexpQuery.DEFAULT_PROVIDER,
                Operations.DEFAULT_DETERMINIZE_WORK_LIMIT,
                MultiTermQuery.CONSTANT_SCORE_BLENDED_REWRITE);
    }
}
