package edu.stanford.protege.search.lucene.tab.engine;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/06/2016
 */
public abstract class ComplexQuery implements SearchTabQuery {

    public abstract boolean isMatchAll();
}
