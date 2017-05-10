package diploma.clustering;

import diploma.clustering.clusters.StatusesCluster;
import diploma.clustering.clusters.StatusesClustering;
import diploma.clustering.tfidf.TfIdf;
import org.junit.Test;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Никита
 */
public class StatusesClusteringSerializerTest {
    @Test
    public void testStatusesClusteringSerializer() throws NoSuchFieldException, IllegalAccessException {
        StatusesClustering statusesClustering = new StatusesClustering(0.2);
        Field minSimilarityField = StatusesClustering.class.getSuperclass().getDeclaredField("minSimilarity");
        minSimilarityField.setAccessible(true);

        Field lastClusterIdField = StatusesClustering.class.getSuperclass().getDeclaredField("lastClusterId");
        lastClusterIdField.setAccessible(true);
        lastClusterIdField.set(statusesClustering, 3);

        List<StatusesCluster> clusters = new ArrayList<>();
        StatusesCluster firstCluster = new StatusesCluster(1, 0.0002);
        StatusesCluster secondCluster = new StatusesCluster(2, 0.0002);
        StatusesCluster thirdCluster = new StatusesCluster(3, 0.0002);
        clusters.add(firstCluster);
        clusters.add(secondCluster);
        clusters.add(thirdCluster);

        Field clusterIdField = StatusesCluster.class.getSuperclass().getDeclaredField("id");
        clusterIdField.setAccessible(true);

        Field sizeClusterField = StatusesCluster.class.getSuperclass().getDeclaredField("size");
        sizeClusterField.setAccessible(true);
        sizeClusterField.set(firstCluster, 10);
        sizeClusterField.set(secondCluster, 15);
        sizeClusterField.set(thirdCluster, 20);

        Field clusterCreationTimeField = StatusesCluster.class.getSuperclass().getDeclaredField("creationTime");
        clusterCreationTimeField.setAccessible(true);
        clusterCreationTimeField.set(firstCluster, 1);
        clusterCreationTimeField.set(secondCluster, 2);
        clusterCreationTimeField.set(thirdCluster,3);

        Field clusterLastUpdateField = StatusesCluster.class.getSuperclass().getDeclaredField("lastUpdateTime");
        clusterLastUpdateField.setAccessible(true);
        clusterLastUpdateField.set(firstCluster, 10);
        clusterLastUpdateField.set(secondCluster, 15);
        clusterLastUpdateField.set(thirdCluster, 20);

        Field clustersField = StatusesClustering.class.getSuperclass().getDeclaredField("clusters");
        clustersField.setAccessible(true);
        clustersField.set(statusesClustering, clusters);

        TfIdf firstTfIdf = new TfIdf();
        TfIdf secondTfIdf = new TfIdf();
        TfIdf thirdTfIdf = new TfIdf();

        Map<String, Integer> firstTermFrequencyMap = new HashMap<>();
        firstTermFrequencyMap.put("car", 10);
        firstTermFrequencyMap.put("door", 15);
        firstTermFrequencyMap.put("wall", 13);

        Map<String, Integer> secondTermFrequencyMap = new HashMap<>();
        secondTermFrequencyMap.put("car2", 11);
        secondTermFrequencyMap.put("door2", 16);
        secondTermFrequencyMap.put("wall2", 14);

        Map<String, Integer> thirdTermFrequencyMap = new HashMap<>();
        thirdTermFrequencyMap.put("car3", 12);
        thirdTermFrequencyMap.put("door3", 17);
        thirdTermFrequencyMap.put("wall3", 15);

        Map<String, Double> firstTfIdfMap = new HashMap<>();
        firstTfIdfMap.put("car", 0.5);
        firstTfIdfMap.put("door", 0.1);
        firstTfIdfMap.put("wall", 0.8);

        Map<String, Double> secondTfIdfMap = new HashMap<>();
        secondTfIdfMap.put("car2", 0.2);
        secondTfIdfMap.put("door2", 0.1);
        secondTfIdfMap.put("wall2", 0.9);

        Map<String, Double> thirdTfIdfMap = new HashMap<>();
        thirdTfIdfMap.put("car3", 1.4);
        thirdTfIdfMap.put("door3", 0.3);
        thirdTfIdfMap.put("wall3", 0.2);

        Field documentNumberField = TfIdf.class.getDeclaredField("documentNumber");
        documentNumberField.setAccessible(true);
        documentNumberField.set(firstTfIdf, 10);
        documentNumberField.set(secondTfIdf, 15);
        documentNumberField.set(thirdTfIdf, 20);

        Field termFrequencyMapField = TfIdf.class.getDeclaredField("termFrequencyMap");
        termFrequencyMapField.setAccessible(true);
        termFrequencyMapField.set(firstTfIdf, firstTermFrequencyMap);
        termFrequencyMapField.set(secondTfIdf, secondTermFrequencyMap);
        termFrequencyMapField.set(thirdTfIdf, thirdTermFrequencyMap);

        Field tfIdfMapField = TfIdf.class.getDeclaredField("tfIdfMapForAllDocuments");
        tfIdfMapField.setAccessible(true);
        tfIdfMapField.set(firstTfIdf, firstTfIdfMap);
        tfIdfMapField.set(secondTfIdf, secondTfIdfMap);
        tfIdfMapField.set(thirdTfIdf, thirdTfIdfMap);

        Field clusterTfIdfField = StatusesCluster.class.getDeclaredField("tfIdf");
        clusterTfIdfField.setAccessible(true);
        clusterTfIdfField.set(firstCluster, firstTfIdf);
        clusterTfIdfField.set(secondCluster, secondTfIdf);
        clusterTfIdfField.set(thirdCluster, thirdTfIdf);

        StatusesClustering.MapDbSerializer serializer = new StatusesClustering.MapDbSerializer();
        TfIdf.MapDbSerializer tfIdfSerializer = new TfIdf.MapDbSerializer();
        StatusesCluster.MapDbSerializer clusterSerializer = new StatusesCluster.MapDbSerializer();
        try {
            DataOutput2 dataOutput2 = new DataOutput2();
            tfIdfSerializer.serialize(dataOutput2, secondTfIdf);
            TfIdf tfIdf = tfIdfSerializer.deserialize(new DataInput2.ByteArray(dataOutput2.buf), -1);
            assertEquals(secondTfIdf, tfIdf);

            dataOutput2 = new DataOutput2();
            clusterSerializer.serialize(dataOutput2, firstCluster);
            StatusesCluster cluster = clusterSerializer.deserialize(new DataInput2.ByteArray(dataOutput2.buf), -1);
            assertEquals(firstCluster, cluster);

            dataOutput2 = new DataOutput2();
            serializer.serialize(dataOutput2, statusesClustering);
            StatusesClustering clustering = serializer.deserialize(new DataInput2.ByteArray(dataOutput2.buf), -1);
            assertEquals(statusesClustering, clustering);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
