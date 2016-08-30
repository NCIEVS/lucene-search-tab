package edu.stanford.protege.search.lucene.tab.engine;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class ResultSetUtils {

    /**
     * Performs an intersect operation given {@code set1} and {@code set2}, where the
     * result will be stored in {@code set1}
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void intersect(final Set<OWLEntity> set1, final Set<OWLEntity> set2) {
        if (set1.isEmpty() && !set2.isEmpty()) {
            set1.addAll(set2); // initialize
        }
        else {
            if (set1.size() <= set2.size()) {
                set1.retainAll(set2);
            }
            else {
                Set<OWLEntity> buffer = new HashSet<>(set2);
                buffer.retainAll(set1);
                set1.clear();
                set1.addAll(buffer);
            }
        }
    }

    /**
     * Performs a union operation given {@code set1} and {@code set2}, where the
     * result will be stored in {@code set1}
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void union(final Set<OWLEntity> set1, final Set<OWLEntity> set2) {
        set1.addAll(set2);
    }

    /**
     * Performs a difference operation given {@code set1} and {@code set2}, such that
     * the {@code set1} will have members that do not belong to {@code set2}
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void difference(final Set<OWLEntity> set1, final Set<OWLEntity> set2) {
        set1.removeAll(set2);
    }

    /**
     * Performs a complement operation given {@code set1} and {@code set2}, such that
     * the {@code set2} will have members that do not belong to {@code set1}. The end
     * result will be stored in {@code set1}.
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void complement(final Set<OWLEntity> set1, final Set<OWLEntity> set2) {
        Set<OWLEntity> cache = new HashSet<OWLEntity>(set1);
        set1.clear();
        set1.addAll(set2);
        set1.removeAll(cache);
    }
}
