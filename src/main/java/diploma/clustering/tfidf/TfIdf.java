package diploma.clustering.tfidf;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import diploma.clustering.MapUtil;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Класс, обсепечивающий подсчет tf-idf векторов.
 * Объект {@link TfIdf} связываеться с кластером.
 * Кластером является объект класса {@link diploma.clustering.neuralgas.Neuron}.
 * @author Никита
 */
public class TfIdf implements Serializable {
    /**
     * Общее число документов в корпусе
     */
    private static int globalDocumentNumber = 0;
    private static Map<String, Integer> globalNumberOfDocumentsWithTermMap = new HashMap<>();
    private static final Object lock = new Object();
    /**
     * Количество документов, связанных с конкретным объектом TfIdf
     * В данном случае каждый объект TfIdf связан с кластером, таким образом documentNumber -
     * это количество документов в данном кластере
     */
    private int documentNumber = 0;

    /**
     * Количество термов, связанных с конкретным объектов TfIdf, то есть
     * это количество слов в кластере
     */
    private int termNumber = 0;

    /**
     * Количество появлений каждого слова во всем корпусе текущего кластера
     */
    private Map<String, Integer> termFrequencyMap = new HashMap<>();
    /**
     * Карта, в которой ключом является слово, а значением - количество документов этого кластера,
     * в которых это слово встречается
     */
    private Map<String, Integer> numberOfDocumentsWithTermMap = new HashMap<>();
    /**
     * Матрица количества появлений каждого слова в каждом документе
     */
    private Table<String, String, Integer> termDocumentCoOccurrenceMatrix = HashBasedTable.create();

    /**
     * Вектор tf-idf для документов данного кластера
     */
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
        this.termNumber++;
        globalDocumentNumber++;
        HashSet<String> passedTerms = new HashSet<>();
        for (String term: terms) {
            updateFrequencyMapForTerm(this.termFrequencyMap, term);
//            if (this.termDocumentCoOccurrenceMatrix.contains(term, docName))
//                this.termDocumentCoOccurrenceMatrix.put(term, docName, this.termDocumentCoOccurrenceMatrix.get(term, docName) + 1);
//            else {
            // если в этом документе уже был такой терм, то не увеличиваем количество документов, в которых он встречается
            if (!passedTerms.contains(term)) {
                updateFrequencyMapForTerm(this.numberOfDocumentsWithTermMap, term);
//                synchronized (lock) {
                    updateFrequencyMapForTerm(globalNumberOfDocumentsWithTermMap, term);
//                }
                if (globalNumberOfDocumentsWithTermMap.get(term) == null && this.numberOfDocumentsWithTermMap.get(term) != null)
                    System.out.println("Бред");
                passedTerms.add(term);
            }
//                this.termDocumentCoOccurrenceMatrix.put(term, docName, 1);
//            }
        }

        // TEMP: test 20 terms
        if (this.termFrequencyMap.size() > 100)
            this.termFrequencyMap = (MapUtil.putFirstEntries(75, MapUtil.sortByValue(this.termFrequencyMap)));
        this.wasUpdated = true;
    }

    public Double getTermIdf(String term) {
//        long start = System.currentTimeMillis();
        Double idf = Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithTermMap.get(term));
//        long end = System.currentTimeMillis() - start;
//        System.out.println("local term idf completed in " + end);
        return idf;
    }

    public synchronized static Double getGlobalTermIdf(String term) {
//        long start = System.currentTimeMillis();
        Double idf = 0.0;
        try {
            idf = Math.log10((double) globalDocumentNumber / (double) globalNumberOfDocumentsWithTermMap.get(term));
        }
        catch (NullPointerException ex) {
            System.out.println(ex.getCause());
        }
//        long end = System.currentTimeMillis() - start;
//        System.out.println("global term idf completed in " + end);
        return idf;
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
                // было неверно, тк это не общее количество слов, а количество уникальных слов
                double tf = (double) term.getValue() / (double) this.termNumber; // this.termFrequencyMap.size();
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
                // TODO: попробовать умножать на 1, что логичнее
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
//                double tf = (double) docTermFrequencyMap.get(termAndItsFrequency.getKey()) / (double) termsNumber;
                double tf = 1.0;
                double idfInCluster = this.getTermIdf(termAndItsFrequency.getKey());
                tfIdfMap.put(termAndItsFrequency.getKey(), tf);// * getGlobalTermIdf(termAndItsFrequency.getKey()));
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

    public void setTermFrequencyMap(Map<String, Integer> termFrequencyMap) {
        // необходимо, чтобы при подсчете tfIdf для кластера, использовалась последняя карта частот
        this.wasUpdated = true;
        this.termFrequencyMap = termFrequencyMap;
    }

    public void sortTermFrequencyMap() {
        termFrequencyMap = MapUtil.sortByValue(termFrequencyMap);
    }

    public void limitTermFrequencyMap(int numberOfTerms) {
        termFrequencyMap = MapUtil.putFirstEntries(numberOfTerms, termFrequencyMap);
    }

    public static int getGlobalDocumentNumber() {
        return globalDocumentNumber;
    }

    public static Map<String, Integer> getGlobalNumberOfDocumentsWithTermMap() {
        return globalNumberOfDocumentsWithTermMap;
    }

    public static Map<String, Double> getTfIdfForSpecificText(String text) {
        String[] terms = text.split(" ");
        Map<String, Double> tfIdf = new HashMap<>();
        for (String term : terms)
            tfIdf.put(term, 1.0);
        return tfIdf;
    }

    public static class MapDbSerializer implements Serializer<TfIdf>, Serializable {
        @Override
        public void serialize(DataOutput2 out, TfIdf value) throws IOException {
            out.writeInt(value.documentNumber);
//            out.writeInt(value.termNumber);
            out.writeInt(value.termFrequencyMap.size());
            for (Map.Entry<String, Integer> entry : value.termFrequencyMap.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue());
            }
            out.writeInt(value.tfIdfMapForAllDocuments.size());
            for (Map.Entry<String, Double> entry : value.tfIdfMapForAllDocuments.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeDouble(entry.getValue());
            }
            out.writeInt(value.numberOfDocumentsWithTermMap.size());
            for (Map.Entry<String, Integer> entry : value.getNumberOfDocumentsWithTermMap().entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue());
            }
        }

        @Override
        public TfIdf deserialize(DataInput2 input, int available) throws IOException {
            TfIdf tfIdf = new TfIdf();
            tfIdf.documentNumber = input.readInt();
            Integer termFrequencyMapSize = input.readInt();
            for (int i = 0; i < termFrequencyMapSize; i++)
                tfIdf.termFrequencyMap.put(input.readUTF(), input.readInt());
            Integer tfIdfMapSize = input.readInt();
            for (int i = 0; i < tfIdfMapSize; i++)
                tfIdf.tfIdfMapForAllDocuments.put(input.readUTF(), input.readDouble());
            Integer numberOfDocumentsWithTermMapSize = input.readInt();
            for (int i = 0; i < numberOfDocumentsWithTermMapSize; i++)
                tfIdf.numberOfDocumentsWithTermMap.put(input.readUTF(), input.readInt());
            return tfIdf;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TfIdf)) return false;

        TfIdf tfIdf = (TfIdf) o;

        if (documentNumber != tfIdf.documentNumber) return false;
        if (!termFrequencyMap.equals(tfIdf.termFrequencyMap)) return false;
        if (!tfIdfMapForAllDocuments.equals(tfIdf.tfIdfMapForAllDocuments)) return false;

        return true;
    }
}
