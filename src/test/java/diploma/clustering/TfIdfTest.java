package diploma.clustering;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Никита
 */
public class TfIdfTest {
    TfIdf tfIdf;

    @Before
    public void setUp() {
        tfIdf = new TfIdf();
    }

    @Test
    public void testTfIdf() {
        tfIdf.updateForNewDocument("doc1", "i have to go");
        tfIdf.updateForNewDocument("doc2", "you have to eat");
        tfIdf.updateForNewDocument("doc3", "we all need to walk");
        tfIdf.updateForNewDocument("doc4", "i need you and you");
        int vocabularySize = 11;
        Map<String, Integer> termFrequencyMap = new HashMap<>();
        termFrequencyMap.put("to", 3);
        termFrequencyMap.put("you", 3);
        termFrequencyMap.put("i", 2);
        termFrequencyMap.put("have", 2);
        termFrequencyMap.put("need", 2);
        termFrequencyMap.put("go", 1);
        termFrequencyMap.put("all", 1);
        termFrequencyMap.put("walk", 1);
        termFrequencyMap.put("eat", 1);
        termFrequencyMap.put("we", 1);
        termFrequencyMap.put("and", 1);
        Map<String, Integer> numberOfDocumentsWithTerm = new HashMap<>();
        numberOfDocumentsWithTerm.put("to", 3);
        numberOfDocumentsWithTerm.put("you", 2);
        numberOfDocumentsWithTerm.put("i", 2);
        numberOfDocumentsWithTerm.put("have", 2);
        numberOfDocumentsWithTerm.put("need", 2);
        numberOfDocumentsWithTerm.put("go", 1);
        numberOfDocumentsWithTerm.put("all", 1);
        numberOfDocumentsWithTerm.put("walk", 1);
        numberOfDocumentsWithTerm.put("eat", 1);
        numberOfDocumentsWithTerm.put("we", 1);
        numberOfDocumentsWithTerm.put("and", 1);
        Table<String, String, Integer> termDocumentCoOccurrenceMatrix = HashBasedTable.create();
        termDocumentCoOccurrenceMatrix.put("to", "doc1", 1);
        termDocumentCoOccurrenceMatrix.put("to", "doc2", 1);
        termDocumentCoOccurrenceMatrix.put("to", "doc3", 1);
        termDocumentCoOccurrenceMatrix.put("you", "doc2", 1);
        termDocumentCoOccurrenceMatrix.put("you", "doc4", 2);
        termDocumentCoOccurrenceMatrix.put("i", "doc1", 1);
        termDocumentCoOccurrenceMatrix.put("i", "doc4", 1);
        termDocumentCoOccurrenceMatrix.put("have", "doc1", 1);
        termDocumentCoOccurrenceMatrix.put("have", "doc2", 1);
        termDocumentCoOccurrenceMatrix.put("need", "doc3", 1);
        termDocumentCoOccurrenceMatrix.put("need", "doc4", 1);
        termDocumentCoOccurrenceMatrix.put("go", "doc1", 1);
        termDocumentCoOccurrenceMatrix.put("all", "doc3", 1);
        termDocumentCoOccurrenceMatrix.put("walk", "doc3", 1);
        termDocumentCoOccurrenceMatrix.put("eat", "doc2", 1);
        termDocumentCoOccurrenceMatrix.put("we", "doc3", 1);
        termDocumentCoOccurrenceMatrix.put("and", "doc4", 1);
        Map<String, Double> tfIdfForAllDocuments = new HashMap<>();
        tfIdfForAllDocuments.put("to", Math.log10(4.0 / 3.0) * (3.0 / vocabularySize));
        tfIdfForAllDocuments.put("you", Math.log10(4.0 / 2.0) * (3.0 / vocabularySize));
        tfIdfForAllDocuments.put("i", Math.log10(4.0 / 2.0) * (2.0 / vocabularySize));
        tfIdfForAllDocuments.put("have", Math.log10(4.0 / 2.0) * (2.0 / vocabularySize));
        tfIdfForAllDocuments.put("need", Math.log10(4.0 / 2.0) * (2.0 / vocabularySize));
        tfIdfForAllDocuments.put("go", Math.log10(4.0 / 1.0) * (1.0 / vocabularySize));
        tfIdfForAllDocuments.put("all", Math.log10(4.0 / 1.0) * (1.0 / vocabularySize));
        tfIdfForAllDocuments.put("walk", Math.log10(4.0 / 1.0) * (1.0 / vocabularySize));
        tfIdfForAllDocuments.put("eat", Math.log10(4.0 / 1.0) * (1.0 / vocabularySize));
        tfIdfForAllDocuments.put("we", Math.log10(4.0 / 1.0) * (1.0 / vocabularySize));
        tfIdfForAllDocuments.put("and", Math.log10(4.0 / 1.0) * (1.0 / vocabularySize));
        Map<String, Double> tfIdfForDoc1 = new HashMap<>();
        int doc1Size = 4;
        tfIdfForDoc1.put("to", Math.log10(4.0 / 3.0) * (1.0 / doc1Size));
        tfIdfForDoc1.put("i", Math.log10(4.0 / 2.0) * (1.0 / doc1Size));
        tfIdfForDoc1.put("have", Math.log10(4.0 / 2.0) * (1.0 / doc1Size));
        tfIdfForDoc1.put("go", Math.log10(4.0 / 1.0) * (1.0 / doc1Size));
        Map<String, Double> tfIdfForDoc4 = new HashMap<>();
        int doc4Size = 4;
        tfIdfForDoc4.put("i", Math.log10(4.0 / 2.0) * (1.0 / doc4Size));
        tfIdfForDoc4.put("need", Math.log10(4.0 / 2.0) * (1.0 / doc4Size));
        tfIdfForDoc4.put("you", Math.log10(4.0 / 2.0) * (2.0 / doc4Size));
        tfIdfForDoc4.put("and", Math.log10(4.0 / 1.0) * (1.0 / doc4Size));
        assertEquals(4, tfIdf.getDocumentNumber());
        assertEquals(termFrequencyMap, tfIdf.getTermFrequencyMap());
        assertEquals(numberOfDocumentsWithTerm, tfIdf.getNumberOfDocumentsWithTerm());
        assertEquals(termDocumentCoOccurrenceMatrix, tfIdf.getTermDocumentCoOccurrenceMatrix());
        assertEquals((Double) Math.log10(4.0 / 2.0), tfIdf.getTermIdf("you"));
        assertEquals((Double) Math.log10(4.0 / 3.0), tfIdf.getTermIdf("to"));
        assertEquals(tfIdfForAllDocuments, tfIdf.tfIdfForAllDocuments());
        assertEquals(tfIdfForDoc1, tfIdf.tfIdfForSpecificDocument("doc1"));
        assertEquals(tfIdfForDoc4, tfIdf.tfIdfForSpecificDocument("doc4"));
    }
}
