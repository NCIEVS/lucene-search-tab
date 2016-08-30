package edu.stanford.protege.search.lucene.tab.engine;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class PropertyValueAbsent extends BasicQuery {

    private final Query luceneQuery;
    private final Set<OWLEntity> resultSpace;

    private final LuceneSearcher searcher;

    private final String algebraString;

    public PropertyValueAbsent(Query luceneQuery, Set<OWLEntity> resultSpace, LuceneSearcher searcher) {
        this(luceneQuery, resultSpace, searcher, luceneQuery.toString());
    }

    public PropertyValueAbsent(Query luceneQuery, Set<OWLEntity> resultSpace, LuceneSearcher searcher, String algebraString) {
        this.luceneQuery = luceneQuery;
        this.resultSpace = resultSpace;
        this.searcher = searcher;
        this.algebraString = algebraString;
    }

    @Override
    public Query getLuceneQuery() {
        return luceneQuery;
    }

    @Override
    public LuceneSearcher getSearcher() {
        return searcher;
    }

    @Override
    public String getAlgebraString() {
        return algebraString;
    }

    @Override
    public Set<OWLEntity> evaluate(SearchProgressListener listener, AtomicBoolean stopSearch) throws QueryEvaluationException {
        SearchDocumentHandler handler = new SearchDocumentHandler(searcher.getEditorKit());
        Set<Document> docs = evaluate();
        int counter = 0;
        for (Document doc : docs) {
            if (stopSearch.get()) { // if should stop
                return getResults(handler.getSearchResults());
            }
            handler.handle(doc);
            if (listener != null) {
                listener.fireSearchingProgressed((counter++*100)/docs.size());
            }
        }
        return getResults(handler.getSearchResults());
    }

    /*
     * Compute the final results using difference operation A - B
     */
    private Set<OWLEntity> getResults(Set<OWLEntity> positiveResults) {
        Set<OWLEntity> finalResults = new HashSet<>(resultSpace);
        ResultSetUtils.difference(finalResults, positiveResults);
        return finalResults;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + PropertyValueAbsent.class.getSimpleName().hashCode();
        result = prime * result + luceneQuery.hashCode();
        result = prime * result + resultSpace.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PropertyValueAbsent)) {
            return false;
        }
        PropertyValueAbsent other = (PropertyValueAbsent) obj;
        return this.luceneQuery.equals(other.luceneQuery) && this.resultSpace.equals(other.resultSpace);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Query: ").append(luceneQuery);
        return sb.toString();
    }
}
