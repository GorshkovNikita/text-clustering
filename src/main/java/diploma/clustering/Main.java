package diploma.clustering;

import diploma.clustering.neuralgas.NeuralGas;
import diploma.clustering.neuralgas.Point;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Никита
 */
public class Main {
    public static final double FRACTION_OF_TOP_EXCLUDED_WORDS = 0.01;
    public static final double FRACTION_OF_BOTTOM_EXCLUDED_WORDS = 0.15;
    public static final int NUMBER_OF_SIGNIFICANT_WORDS = 100;
    public static Map<String, Map<String, Double>> tfTable = new HashMap<>();

    public static Map<String, Double[]> process(Path filePath) {
        TfIdf tfIdf = new TfIdf();
        List<String> tweets = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line = null;
            do {
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    String[] lines = line.split("=");
                    Map<String, Double> tf = tfIdf.tfIdfForSpecificDocument(lines[0]);//, lines[1]);
                    tfTable.put(lines[1], tf);
                }
            } while (line != null);
            List<String> words = new ArrayList<>();
            for (Map.Entry<String, Integer> entry: tfIdf.getNumberOfDocumentsWithTerm().entrySet()) {
                if (entry.getValue() > 5)
                    words.add(entry.getKey());
            }
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        Map<String, Integer> sortedMap = MapUtil.sortByValue(tfIdf.getNumberOfDocumentsWithTerm());
//        Map<String, Double> sortedMapTwo = MapUtil.sortByValue(tf_idf.idfMap);
        String[] significantWordsVector = significantWordsVector(sortedMap.keySet());
        Map<String, Double[]> vectorModels = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> tweetAndItsTf: tfTable.entrySet()) {
            String tweet = tweetAndItsTf.getKey();
            Double[] vectorModel = new Double[NUMBER_OF_SIGNIFICANT_WORDS];
            int i = 0;
            Map<String, Double> tf = tweetAndItsTf.getValue();
            for (int j = 0; j < NUMBER_OF_SIGNIFICANT_WORDS; j++) {
                if (tf.containsKey(significantWordsVector[j]))
                    vectorModel[j] = tf.get(significantWordsVector[j]) * tfIdf.getTermIdf(significantWordsVector[j]);
                else vectorModel[j] = 0.0;
            }
//            for (Map.Entry<String, Double> tfIdfForSpecificDocument: tweetAndItsTf.getValue().entrySet()) {
//                while (!tfIdfForSpecificDocument.getKey().equals(significantWordsVector[i])) {
//                    vectorModel[i] = 0.0;
//                    i++;
//                }
//                vectorModel[i] = tfIdfForSpecificDocument.getValue() * tf_idf.idfMap.get(tfIdfForSpecificDocument.getKey());
//                i++;
//            }
            vectorModels.put(tweet, vectorModel);
        }
        return vectorModels;
    }

    public static List<Point> convertVectorModelsToPoints(Map<String, Double[]> vectorModels) {
        List<Point> points = new ArrayList<>();
        for (Map.Entry<String, Double[]> vectorModel: vectorModels.entrySet()) {
            points.add(new Point(vectorModel.getValue(), vectorModel.getKey()));
        }
        return points;
    }

    public static String[] significantWordsVector(Set<String> uniqueWords) {
        int numberOfUniqueWords = uniqueWords.size();
        int first = (int) Math.floor(numberOfUniqueWords * FRACTION_OF_TOP_EXCLUDED_WORDS);
        String[] words = new String[NUMBER_OF_SIGNIFICANT_WORDS];
        int i = 0;
        Iterator<String> itr = uniqueWords.iterator();
        while (itr.hasNext()) {
            String next = itr.next();
            if (i > first && (i < NUMBER_OF_SIGNIFICANT_WORDS + first + 1))
                words[i - first - 1] = next;
            else if (i > first) break;
            i++;
        }
        return words;
    }

    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) {
        NeuralGas neuralGas = NeuralGas.getInstance();
        Double[] first = new Double[NUMBER_OF_SIGNIFICANT_WORDS];
        Double[] second = new Double[NUMBER_OF_SIGNIFICANT_WORDS];
        for (int i = 0; i < NUMBER_OF_SIGNIFICANT_WORDS; i++) {
            first[i] = 0.0;
            second[i] = 0.1;
        }
        neuralGas.init(first, second);
        neuralGas.runAdaptiveIncrementalClustering(
                convertVectorModelsToPoints(
                        process(Paths.get(Main.class.getClassLoader().getResource("sample-tweets-champions-league-first-1000.txt").getFile()
                                .substring(1)))));
//        neuralGas.init(new Double[] { 0.0, 0.0}, new Double[] {0.1, 0.1});
//        neuralGas.runAdaptiveIncrementalClustering(PointsCreator.createTwoDimensionalPoints(100000));
        neuralGas.printInfo();
//        TF_IDF tf_idf = new TF_IDF();
    }
}
