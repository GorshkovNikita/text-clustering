package diploma.clustering.tfidf;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import diploma.clustering.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс, обсепечивающий подсчет tf-idf векторов.
 * Объект {@link TfIdf} связываеться с кластером.
 * Кластером является объект класса {@link diploma.clustering.neuralgas.Neuron}.
 * @author Никита
 */
public class TfIdf {
    /**
     * Общее число документов в корпусе
     */
    private static int globalDocumentNumber = 0;
    private static Map<String, Integer> globalNumberOfDocumentsWithTermMap = new HashMap<>();
    /**
     * Количество документов, связанных с конкретным объектом TfIdf
     * В данном случае каждый объект TfIdf связан с кластером, таким образом documentNumber -
     * это количество документов в данном кластере
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
    private Map<String, Integer> numberOfDocumentsWithTermMap = new HashMap<>();
    /**
     * Матрица количества появлений каждого слова в каждом документе
     */
    private Table<String, String, Integer> termDocumentCoOccurrenceMatrix = HashBasedTable.create();
//    private TextNormalizer normalizer = new TextNormalizer();
    private Map<String, Double> tfIdfMapForAllDocuments = new HashMap<>();
    /**
     * Флаг, показывающий был ли добавлен новый документ, если нет, то tfIdfMapForAllDocuments
     * можно не пересчитывать. Если же новый документ был добавлен, то необходимо посчитать все заново
     */
    private boolean wasUpdated = true;

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
        globalDocumentNumber++;
        for (String term: terms) {
            updateFrequencyMapForTerm(this.termFrequencyMap, term);
            if (this.termDocumentCoOccurrenceMatrix.contains(term, docName))
                this.termDocumentCoOccurrenceMatrix.put(term, docName, this.termDocumentCoOccurrenceMatrix.get(term, docName) + 1);
            else {
                updateFrequencyMapForTerm(this.numberOfDocumentsWithTermMap, term);
                updateFrequencyMapForTerm(globalNumberOfDocumentsWithTermMap, term);
                this.termDocumentCoOccurrenceMatrix.put(term, docName, 1);
            }
        }
        this.wasUpdated = true;
    }

    public Double getTermIdf(String term) {
        return Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithTermMap.get(term));
    }

    public static Double getGlobalTermIdf(String term) {
        return Math.log10((double) globalDocumentNumber / (double) globalNumberOfDocumentsWithTermMap.get(term));
    }

    /**
     * Подсчет вектора tf-idf для документа с именем docName
     * То есть предполагается, что этот документ уже был добавлен в кластер
     * @param docName - имя документа (id твита)
     * @return - вектор tf-idf для заданного документа
     */
    // TODO: первый параметр заменить потом на id твита
    public Map<String, Double> getTfIdfForSpecificDocument(String docName) {
        // TODO: может быть узким местом, тк поиск по строкам в таблице быстрее
        Map<String, Integer> docTermFrequencyMap = termDocumentCoOccurrenceMatrix.column(docName);
        Map<String, Double> tfIdfMap = new HashMap<>();
        for (Map.Entry<String, Integer> term: docTermFrequencyMap.entrySet()) {
            double tf = (double) term.getValue() / (double) docTermFrequencyMap.size();
            tfIdfMap.put(term.getKey(), tf * getGlobalTermIdf(term.getKey()));
        }
        return tfIdfMap;
    }

    /**
     * Подсчет вектора tf-idf для всех слов текущего кластера
     * @return - вектор tf-idf для всех слов корпуса
     */
    public Map<String, Double> getTfIdfForAllDocuments() {
        if (wasUpdated) {
            this.tfIdfMapForAllDocuments.clear();
            for (Map.Entry<String, Integer> term : this.termFrequencyMap.entrySet()) {
                // TODO: в online-denstream в качестве tf используется только количество вхождений
                double tf = (double) term.getValue() / (double) this.termFrequencyMap.size();
                // TODO: global or not?
                this.tfIdfMapForAllDocuments.put(term.getKey(), tf * getGlobalTermIdf(term.getKey()));
            }
            this.wasUpdated = false;
        }
        return this.tfIdfMapForAllDocuments;
    }

    /**
     * Получение вектора tf-idf для нового пришедшего твита в рамках текущего кластера.
     * В этой карте будут все ключи, которые есть в экземпляре {@link diploma.clustering.tfidf.TfIdf}
     * и в пришедшем твите.
     * @param normalizedText - нормализованный текст твита
     * @return - tf-idf твита
     */
    public Map<String, Double> getTfIdfForSpecificDocumentWithContent(String normalizedText) {
        String[] terms = normalizedText.split(" ");
        int termsNumber = terms.length;
        Map<String, Integer> docTermFrequencyMap = new HashMap<>();
        for (String term: terms) updateFrequencyMapForTerm(docTermFrequencyMap, term);
        Map<String, Double> tfIdfMap = new HashMap<>();
        for (Map.Entry<String, Integer> termAndItsFrequency: this.termFrequencyMap.entrySet()) {
            if (docTermFrequencyMap.containsKey(termAndItsFrequency.getKey())) {
                double tf = (double) docTermFrequencyMap.get(termAndItsFrequency.getKey()) / (double) termsNumber;
                tfIdfMap.put(termAndItsFrequency.getKey(), tf * getGlobalTermIdf(termAndItsFrequency.getKey()));
            }
            else tfIdfMap.put(termAndItsFrequency.getKey(), 0.0);
        }
        for (String term: terms) {
            if (!tfIdfMap.containsKey(term)) {
                double tf = (double) docTermFrequencyMap.get(term) / (double) termsNumber;
                // умножаем на количество документов, тк этого терма нет в карте idf => он не встрачался еще ни разу
                tfIdfMap.put(term, tf * globalDocumentNumber);
            }
        }
        return tfIdfMap;
    }

    /**
     * Получение вектора tf-idf для нового пришедшего твита, состоящий только из термов,
     * встречающихся в кластере. Например, пришел твит с текстом: "i like to eat".
     * В текущем экземпляре {@link diploma.clustering.tfidf.TfIdf} собрались термы:
     * i, go, to, like, dog. В этом случае вернется карта tf-idf с ключами i, to, like,
     * т.к. только эти слова встречаются и в твите и в экземпляре {@link diploma.clustering.tfidf.TfIdf}
     * @param normalizedText - нормализованный текст твита
     * @return - tf-idf твита
     */
    public Map<String, Double> getTfIdfOfDocumentIntersection(String normalizedText) {
        String[] terms = normalizedText.split(" ");
        int termsNumber = terms.length;
        Map<String, Integer> docTermFrequencyMap = new HashMap<>();
        for (String term: terms) updateFrequencyMapForTerm(docTermFrequencyMap, term);
        Map<String, Double> tfIdfMap = new HashMap<>();
        for (Map.Entry<String, Integer> termAndItsFrequency: this.termFrequencyMap.entrySet()) {
            if (docTermFrequencyMap.containsKey(termAndItsFrequency.getKey())) {
                double tf = (double) docTermFrequencyMap.get(termAndItsFrequency.getKey()) / (double) termsNumber;
                tfIdfMap.put(termAndItsFrequency.getKey(), tf * getGlobalTermIdf(termAndItsFrequency.getKey()));
            }
        }
        return tfIdfMap;
    }

    /**
     * tf-idf всех документов текущего кластера, дополненный термами пришедшего твита
     * @param normalizedText - нормализованный текст твита
     * @return - дополненный tf-idf всех документов текущего кластера
     */
    public Map<String, Double> getAugmentedTfIdfForAllDocuments(String normalizedText) {
        String[] terms = normalizedText.split(" ");
        Map<String, Double> augmentedTfIdf = new HashMap<>(getTfIdfForAllDocuments());
        for (String term: terms)
            if (!augmentedTfIdf.containsKey(term))
                augmentedTfIdf.put(term, 0.0);
        return augmentedTfIdf;
    }

    private void updateFrequencyMapForTerm(Map<String, Integer> frequencyMap, String term) {
        if (frequencyMap.containsKey(term)) frequencyMap.put(term, frequencyMap.get(term) + 1);
        else frequencyMap.put(term, 1);
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public Map<String, Integer> getNumberOfDocumentsWithTermMap() {
        return numberOfDocumentsWithTermMap;
    }

    public Table<String, String, Integer> getTermDocumentCoOccurrenceMatrix() {
        return termDocumentCoOccurrenceMatrix;
    }

    public Map<String, Integer> getTermFrequencyMap() {
        return termFrequencyMap;
    }

    public void sortTermFrequencyMap() {
        termFrequencyMap = MapUtil.sortByValue(termFrequencyMap);
    }

    public static int getGlobalDocumentNumber() {
        return globalDocumentNumber;
    }

    public static Map<String, Integer> getGlobalNumberOfDocumentsWithTermMap() {
        return globalNumberOfDocumentsWithTermMap;
    }
}
