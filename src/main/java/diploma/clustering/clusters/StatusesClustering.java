package diploma.clustering.clusters;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.EnhancedStatus;
import diploma.clustering.tfidf.TfIdf;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Никита
 */
public class StatusesClustering extends Clustering<StatusesCluster, EnhancedStatus> implements Serializable {
    public StatusesClustering() {
        super();
    }

    public StatusesClustering(Double minSimilarity) {
        super(minSimilarity);
    }

    @Override
    public StatusesCluster findNearestCluster(EnhancedStatus point) {
        StatusesCluster nearestCluster = null;
        Double maxSimilarity = 0.0;
        for (StatusesCluster cluster: getClusters()) {
            Map<String, Double> tfIdfForAllDocuments = cluster.getTfIdf().getTfIdfForAllDocuments();
            Map<String, Double> tfIdfOfDocumentIntersection = cluster.getTfIdf().getTfIdfOfDocumentIntersection(point.getNormalizedText());
            Double similarity = CosineSimilarity.cosineSimilarity(tfIdfForAllDocuments, tfIdfOfDocumentIntersection);
            if (similarity > minSimilarity && similarity > maxSimilarity) {
                nearestCluster = cluster;
                maxSimilarity = similarity;
            }
        }
        return nearestCluster;
    }

    public static class MapDbSerializer implements Serializer<StatusesClustering>, Serializable {
        @Override
        public void serialize(DataOutput2 out, StatusesClustering value) throws IOException {
            out.writeDouble(value.minSimilarity);
            out.writeInt(value.lastClusterId);
            out.writeInt(TfIdf.getGlobalDocumentNumber());
            out.writeInt(TfIdf.getGlobalNumberOfDocumentsWithTermMap().size());
            for (Map.Entry<String, Integer> entry : TfIdf.getGlobalNumberOfDocumentsWithTermMap().entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue());
            }
            out.writeInt(value.clusters.size());
            for (StatusesCluster cluster : value.clusters)
                new StatusesCluster.MapDbSerializer().serialize(out, cluster);
        }

        @Override
        public StatusesClustering deserialize(DataInput2 input, int available) throws IOException {
            StatusesClustering statusesClustering = new StatusesClustering(input.readDouble());
            statusesClustering.lastClusterId = input.readInt();
            try {
                Field globalDocumentNumberField = TfIdf.class.getDeclaredField("globalDocumentNumber");
                globalDocumentNumberField.setAccessible(true);
                globalDocumentNumberField.set(null, input.readInt());

                int globalNumberOfDocumentsWithTermMapSize = input.readInt();
                Field globalNumberOfDocumentsWithTermMapField = TfIdf.class.getDeclaredField("globalNumberOfDocumentsWithTermMap");
                globalNumberOfDocumentsWithTermMapField.setAccessible(true);
                Map<String, Integer> globalNumberOfDocumentsWithTermMap = new HashMap<>();
                for (int i = 0; i < globalNumberOfDocumentsWithTermMapSize; i++)
                    globalNumberOfDocumentsWithTermMap.put(input.readUTF(), input.readInt());
                globalNumberOfDocumentsWithTermMapField.set(null, globalNumberOfDocumentsWithTermMap);
            }
            catch (NoSuchFieldException | IllegalAccessException ignored) {}
            int numberOfClusters = input.readInt();
            StatusesCluster.MapDbSerializer serializer = new StatusesCluster.MapDbSerializer();
            for (int i = 0; i < numberOfClusters; i++)
                statusesClustering.clusters.add(serializer.deserialize(input, -1));
            return statusesClustering;
        }
    }
}
