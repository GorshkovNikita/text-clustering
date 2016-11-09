package diploma.clustering;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Никита
 */
public class TfIdf {
    /**
     * Количество документов в корпусе
     */
    private int documentNumber = 0;
    /**
     * Карта инверсий частот, всех слов,
     * с которой они встречаются в документах корпуса
     * TODO: удалить, тк не нужно из-за того, что его можно посчитать напрямую для слова
     */
//    public Map<String, Double> idfMap = new HashMap<>();
    /**
     * Количество появлений каждого слова во всем корпусе
     */
    private Map<String, Integer> termFrequencyMap = new HashMap<>();
    /**
     * Карта, в которой ключом является слово, а значением - количество документов,
     * в которых это слово встречается
     * TODO: может быть отказаться от этого поля в пользу termFrequencyMap из-за того,
     * TODO: что в твитах редко бывает так, что какое-то слово в твит попадает несколько раз
     */
    private Map<String, Integer> numberOfDocumentsWithTerm = new HashMap<>();
    /**
     * Матрица количества появлений каждого слова в каждом документе
     */
    private Table<String, String, Integer> termDocumentCoOccurrenceMatrix = HashBasedTable.create();
//    private TextNormalizer normalizer = new TextNormalizer();

    public TfIdf() {}

    /**
     * Обновление idf корпуса, обновление матрицы появлений слов в документах,
     * общего количества документов, карты частоты появления слова.
     * Метод должен вызываться только при добавлении элемента в кластер.
     * @param docName - назвние документа
     * @param normalizedText - нормализованное содержание документа
     */
    public void updateForNewDocument(String docName, String normalizedText) {
//        String normalizedText = this.normalizer.normalizeToString(text);
        String[] terms = normalizedText.split(" ");
        this.documentNumber++;
        for (String term: terms) {
            if (this.termFrequencyMap.containsKey(term))
                this.termFrequencyMap.put(term, this.termFrequencyMap.get(term) + 1);
            else this.termFrequencyMap.put(term, 1);

            if (this.termDocumentCoOccurrenceMatrix.contains(term, docName))
                this.termDocumentCoOccurrenceMatrix.put(term, docName, this.termDocumentCoOccurrenceMatrix.get(term, docName) + 1);
            else {
                if (this.numberOfDocumentsWithTerm.containsKey(term))
                    this.numberOfDocumentsWithTerm.put(term, this.numberOfDocumentsWithTerm.get(term) + 1);
                else
                    this.numberOfDocumentsWithTerm.put(term, 1);
//                idfMap.put(word, Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithTerm.get(word)));
                this.termDocumentCoOccurrenceMatrix.put(term, docName, 1);
            }
        }
//        for (Map.Entry<String, Double> word: idfMap.entrySet()) {
//            // TODO: поставить условие, проверяющее, что слово не было обновлено (т.е. было в поступившем документе)
//            idfMap.put(word.getKey(), Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithTerm.get(word.getKey())));
//        }
    }

    public Double getTermIdf(String term) {
        return Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithTerm.get(term));
    }

    /**
     * Подсчет вектора tf-idf для документа с именем docName
     * @param docName - имя документа (id твита)
     * @return - вектор tf-idf для заданного документа
     */
    // TODO: первый параметр заменить потом на id твита
    public Map<String, Double> tfIdfForSpecificDocument(String docName) {
        // TODO: может быть узким местом, тк поиск по строкам в таблице быстрее
        Map<String, Integer> docTermFrequencyMap = termDocumentCoOccurrenceMatrix.column(docName);
        Map<String, Double> tfIdfMap = new HashMap<>();
        for (Map.Entry<String, Integer> term: docTermFrequencyMap.entrySet()) {
            double tf = (double) term.getValue() / (double) docTermFrequencyMap.size();
            tfIdfMap.put(term.getKey(), tf * getTermIdf(term.getKey()));
        }
        return tfIdfMap;
    }

    /**
     * Подсчет вектора tf-idf для всех слов корпуса
     * @return - вектор tf-idf для всех слов корпуса
     */
    public Map<String, Double> tfIdfForAllDocuments() {
        Map<String, Double> tfIdfMap = new HashMap<>();
        for (Map.Entry<String, Integer> term: this.termFrequencyMap.entrySet()) {
            double tf = (double) term.getValue() / (double) this.termFrequencyMap.size();
            tfIdfMap.put(term.getKey(), tf * getTermIdf(term.getKey()));
        }
        return tfIdfMap;
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public Map<String, Integer> getNumberOfDocumentsWithTerm() {
        return numberOfDocumentsWithTerm;
    }

    public Table<String, String, Integer> getTermDocumentCoOccurrenceMatrix() {
        return termDocumentCoOccurrenceMatrix;
    }

    public Map<String, Integer> getTermFrequencyMap() {
        return termFrequencyMap;
    }
}
