package edu.stanford.protege.search.lucene.tab.engine;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.RemoveChangeSetHandler;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.AxiomSubjectProvider;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import edu.stanford.protege.search.lucene.tab.ui.LuceneUiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchTabRemoveChangeSetHandler extends RemoveChangeSetHandler implements OWLAxiomVisitor {

    public SearchTabRemoveChangeSetHandler(OWLEditorKit editorKit) {
        super(editorKit);
    }

    @Override
    public void visit(RemoveAxiom change) {
        OWLAxiom changeAxiom = change.getAxiom();
        changeAxiom.accept(this);
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        OWLEntity entity = axiom.getEntity();
        List<Term> terms = new ArrayList<>();
        terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
        terms.add(new Term(IndexField.ENTITY_TYPE, getType(entity)));
        removeFilters.add(terms);
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        if (axiom.getSubject() instanceof IRI) {
            List<Term> terms = new ArrayList<>();
            OWLEntity entity = getOWLEntity((IRI) axiom.getSubject());
            terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
            //terms.add(new Term(IndexField.DISPLAY_NAME, getDisplayName(entity)));
            terms.add(new Term(IndexField.ANNOTATION_IRI, getIri(axiom.getProperty())));
            //terms.add(new Term(IndexField.ANNOTATION_DISPLAY_NAME, getDisplayName(axiom.getProperty())));
            
            
            OWLAnnotationValue value = axiom.getAnnotation().getValue();
            if (value instanceof OWLLiteral) {
                OWLLiteral literal = (OWLLiteral) value;
                if (literal.getDatatype().getIRI().equals(XSDVocabulary.ANY_URI.getIRI())) {
                    terms.add(new Term(IndexField.ANNOTATION_VALUE_IRI, literal.getLiteral()));
                }
                else {
                    terms.add(new Term(IndexField.ANNOTATION_TEXT, strip(literal.getLiteral())));
                    terms.add(new Term(IndexField.ANNOTATION_FULL_TEXT, 
                    		literal.getLiteral().trim().toLowerCase()));
                }
            }
            else if (value instanceof IRI) {
                IRI iri = (IRI) value;
                terms.add(new Term(IndexField.ANNOTATION_VALUE_IRI, iri.toString()));
            }
            
            removeFilters.add(terms);
            
            for (OWLAnnotation ann : axiom.getAnnotations()) {
            	
            	terms = new ArrayList<>();
            	terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
                //terms.add(new Term(IndexField.DISPLAY_NAME, getDisplayName(entity)));
                
                terms.add(new Term(IndexField.ANNOTATION_IRI, getIri(ann.getProperty())));
                //terms.add(new Term(IndexField.ANNOTATION_DISPLAY_NAME, getDisplayName(ann.getProperty())));
               
                value = ann.getValue();
                if (value instanceof OWLLiteral) {
                    OWLLiteral literal = (OWLLiteral) value;
                    if (literal.getDatatype().getIRI().equals(XSDVocabulary.ANY_URI.getIRI())) {
                        terms.add(new Term(IndexField.ANNOTATION_VALUE_IRI, literal.getLiteral()));
                    }
                    else {
                        terms.add(new Term(IndexField.ANNOTATION_TEXT, strip(literal.getLiteral())));
                        terms.add(new Term(IndexField.ANNOTATION_FULL_TEXT, 
                        		literal.getLiteral().trim().toLowerCase()));
                    }
                }
                else if (value instanceof IRI) {
                    IRI iri = (IRI) value;
                    terms.add(new Term(IndexField.ANNOTATION_VALUE_IRI, iri.toString()));
                }
                removeFilters.add(terms);
            	
            }
            
        }
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        visitLogicalAxiom(axiom);
        if (!(axiom.getSubClass() instanceof OWLClass)) {
            return;
        }
        OWLClass cls = axiom.getSubClass().asOWLClass();
        if (axiom.getSuperClass() instanceof OWLRestriction) {
            OWLRestriction restriction = (OWLRestriction) axiom.getSuperClass();
            visitObjectRestriction(cls, restriction);
        }
        else if (axiom.getSuperClass() instanceof OWLBooleanClassExpression) {
            OWLBooleanClassExpression expr = (OWLBooleanClassExpression) axiom.getSuperClass();
            if (expr instanceof OWLObjectIntersectionOf) {
                for (OWLClassExpression ce : expr.asConjunctSet()) {
                    if (ce instanceof OWLRestriction) {
                        visitObjectRestriction(cls, (OWLRestriction) ce);
                    }
                }
            }
            else if (expr instanceof OWLObjectUnionOf) {
                for (OWLClassExpression ce : expr.asDisjunctSet()) {
                    if (ce instanceof OWLRestriction) {
                        visitObjectRestriction(cls, (OWLRestriction) ce);
                    }
                }
            }
            else if (expr instanceof OWLObjectComplementOf) {
                OWLClassExpression ce = ((OWLObjectComplementOf) expr).getObjectComplementOf();
                if (ce instanceof OWLRestriction) {
                    visitObjectRestriction(cls, (OWLRestriction) ce);
                }
            }
        }
    }

    private void visitObjectRestriction(OWLClass subclass, OWLRestriction restriction) {
        if (restriction.getProperty() instanceof OWLProperty) {
            OWLProperty property = (OWLProperty) restriction.getProperty();
            if (restriction instanceof HasFiller<?>) {
                List<Term> terms = new ArrayList<>();
                HasFiller<?> restrictionWithFiller = (HasFiller<?>) restriction;
                if (restrictionWithFiller.getFiller() instanceof OWLClass) {
                    OWLClass filler = (OWLClass) restrictionWithFiller.getFiller();
                    terms.add(new Term(IndexField.ENTITY_IRI, getIri(subclass)));
                    terms.add(new Term(IndexField.OBJECT_PROPERTY_IRI, getIri(property)));
                    terms.add(new Term(IndexField.FILLER_IRI, getIri(filler)));
                }
                else {
                    terms.add(new Term(IndexField.ENTITY_IRI, getIri(subclass)));
                    terms.add(new Term(IndexField.OBJECT_PROPERTY_IRI, getIri(property)));
                    terms.add(new Term(IndexField.FILLER_IRI, ""));
                }
                removeFilters.add(terms);
            }
        }
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        visitLogicalAxiom(axiom);
        Set<OWLSubClassOfAxiom> subClassAxioms = axiom.asOWLSubClassOfAxioms();
        for (OWLSubClassOfAxiom sc : subClassAxioms) {
            sc.accept(this);
        }
    }

    //@formatter:off
    @Override public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLAsymmetricObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLReflexiveObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDisjointClassesAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDataPropertyDomainAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLObjectPropertyDomainAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLEquivalentObjectPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDifferentIndividualsAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDisjointDataPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDisjointObjectPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLObjectPropertyRangeAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLObjectPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLFunctionalObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLSubObjectPropertyOfAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDisjointUnionAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLSymmetricObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDataPropertyRangeAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLFunctionalDataPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLEquivalentDataPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLClassAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLDataPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLTransitiveObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLSubDataPropertyOfAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLSameIndividualAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLSubPropertyChainOfAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLInverseObjectPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
    @Override public void visit(OWLHasKeyAxiom axiom) { visitLogicalAxiom(axiom); }

    //@formatter:on
    private void visitLogicalAxiom(OWLAxiom axiom) {
        List<Term> terms = new ArrayList<>();
        OWLObject subject = new AxiomSubjectProvider().getSubject(axiom);
        if (subject instanceof OWLEntity) {
            OWLEntity entity = (OWLEntity) subject;
            terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
            terms.add(new Term(IndexField.AXIOM_DISPLAY_NAME, getDisplayName(axiom)));
            terms.add(new Term(IndexField.AXIOM_TYPE, getType(axiom)));
            removeFilters.add(terms);
        }
    }

    //@formatter:off
    @Override public void visit(OWLSubAnnotationPropertyOfAxiom axiom) { doesNothing(); }
    @Override public void visit(OWLAnnotationPropertyDomainAxiom axiom) { doesNothing(); }
    @Override public void visit(OWLAnnotationPropertyRangeAxiom axiom) { doesNothing(); }
    @Override public void visit(SWRLRule rule) { doesNothing(); }
    @Override public void visit(OWLDatatypeDefinitionAxiom axiom) { doesNothing(); }

    //@formatter:on
    private void doesNothing() {
        // NO-OP
    }

    private String strip(String s) {
        return s.replaceAll("\\^\\^.*$", "") // remove datatype ending
                .replaceAll("^\"|\"$", "") // remove enclosed quotes
                .replaceAll("<[^>]+>", " ") // trim XML tags
                .replaceAll("\\s+", " ") // trim excessive white spaces
                .trim();
    }
}
