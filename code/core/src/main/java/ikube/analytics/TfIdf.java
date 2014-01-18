package ikube.analytics;

import javax.validation.constraints.NotNull;

/**
 * Wrapper object that consists all necessary information for calculating tfidf value for each term. Use
 * method getValue() to get the actual tfidf value.
 *
 * @author Aniket
 */
public class TfIdf implements Comparable<TfIdf> {

    private Number numOfOccurrences;
    private Number totalTermsInDocument;
    private Number totalDocuments;
    private Number numOfDocumentsWithTerm;

    public TfIdf(Number occ, Number totTerms, Number totDocs, Number docsWithTerms) {
        numOfOccurrences = occ;
        totalTermsInDocument = totTerms;
        totalDocuments = totDocs;
        numOfDocumentsWithTerm = docsWithTerms;
    }

    /**
     * Calculates the tf-idf value of the current term. For description of tf-idf refer to
     * <a href="http://en.wikipedia.org/wiki/Tf–idf"> wikipedia.org/Tf–idf</a> <br />. Because there can be many
     * cases where the current term is not present in any other document in the repository, Float.MIN_VALUE is added to the
     * denominator to avoid DivideByZero exception
     *
     * @return the inverse term frequency/document frequency
     */
    public Float getValue() {
        float tf = numOfOccurrences.floatValue() / (Float.MIN_VALUE + totalTermsInDocument.floatValue());
        float df = totalDocuments.floatValue() / (Float.MIN_VALUE + numOfDocumentsWithTerm.floatValue());
        float idf = (float) Math.log10(df);
        return (tf * idf);
    }

    public String toString() {
        return this.getValue().toString();
        // return "numOfOccurrences : " + this.numOfOccurrences.intValue() + "\n"
        // + "totalTermsInDocument : " + this.totalTermsInDocument.intValue() + "\n"
        // + "numOfDocumentsWithTerm : " + this.numOfDocumentsWithTerm.intValue() + "\n"
        // + "totalDocuments : " + this.totalDocuments.intValue() + "\n"
        // + "TFIDF : " + this.Value();

    }

    @Override
    public int compareTo(@NotNull final TfIdf o) {
        return (int) ((this.getValue() - o.getValue()) * 100);
    }

}
