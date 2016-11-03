package diploma.clustering;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Никита
 */
public class TF_IDF {
    /**
     * Количество документов в корпусе
     */
    private int documentNumber = 0;
    /**
     * Карта инверсий частот, всех слов,
     * с которой они встречаются в документах корпуса
     */
    public Map<String, Double> idfMap = new HashMap<>();
    /**
     * Количество появлений каждого слова во всем корпусе
     */
//    private Map<String, Integer> globalFrequencyMap = new HashMap<>();

    /**
     * Карта, в которой ключом является слово, а значением - количество документах,
     * в которых это слово встречается
     */
    public Map<String, Integer> numberOfDocumentsWithWord = new HashMap<>();
    /**
     * Матрица количества появлений каждого слова в каждом документе
     */
    private Table<String, String, Integer> termDocumentCoOccurrenceMatrix = HashBasedTable.create();
    private TextNormalizer normalizer = new TextNormalizer();

    public TF_IDF() {
    }

    // TODO: первый параметр заменить потом на id твита
    public Map<String, Double> tf(String docName, String text) {
        this.documentNumber++;
        String normalizedText = this.normalizer.normalizeToString(text);
        String[] words = normalizedText.split(" ");
        int numberOfWords = words.length;
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String word: words) {
//            this.updateGlobalFrequencyMap(word);
            if (!frequencyMap.containsKey(word))
            if (frequencyMap.containsKey(word))
                frequencyMap.put(word, frequencyMap.get(word) + 1);
            else {
                frequencyMap.put(word, 1);
                if (this.numberOfDocumentsWithWord.containsKey(word))
                    this.numberOfDocumentsWithWord.put(word, this.numberOfDocumentsWithWord.get(word) + 1);
                else
                    this.numberOfDocumentsWithWord.put(word, 1);
                // надо обновлять все слова, даже те, которые не встречаются в текущем документе
                idfMap.put(word, Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithWord.get(word)));
            }
        }
        for (Map.Entry<String, Double> word: idfMap.entrySet()) {
            idfMap.put(word.getKey(), Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithWord.get(word.getKey())));
        }
        Map<String, Double> tfMap = new HashMap<>();
        frequencyMap.forEach((k, v) -> {
            tfMap.put(k,  ((double)v / (double)numberOfWords));
            termDocumentCoOccurrenceMatrix.put(k, docName, v);
        });
        return tfMap;
    }

    private void updateIdf(String word) {
        idfMap.put(word, Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithWord.get(word)));
//        if (idfMap.containsKey(word)) {
//
//        }
//        else {
//            idfMap.put(word, Math.log10((double) this.documentNumber / (double) this.numberOfDocumentsWithWord.get(word)));
//        }
    }

    private void updateGlobalFrequencyMap(String word) {
        if (!this.numberOfDocumentsWithWord.containsKey(word))
            this.numberOfDocumentsWithWord.put(word, this.numberOfDocumentsWithWord.get(word) + 1);
//        if (this.globalFrequencyMap.containsKey(word))
//            this.globalFrequencyMap.put(word, this.globalFrequencyMap.get(word) + 1);
//        else
//            this.globalFrequencyMap.put(word, 1);
    }
}
