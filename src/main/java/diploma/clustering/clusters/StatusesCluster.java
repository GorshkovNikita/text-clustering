package diploma.clustering.clusters;

import diploma.clustering.TextNormalizer;
import diploma.clustering.tfidf.TfIdf;
import twitter4j.Status;

/**
 * @author Никита
 */
public class StatusesCluster extends Cluster<Status> {
    private TfIdf tfIdf;

    public StatusesCluster() {
        super();
        tfIdf = new TfIdf();
    }

    @Override
    public void assignPoint(Status point) {
        super.assignPoint(point);
        tfIdf.updateForNewDocument(Long.toString(point.getId()), TextNormalizer.getInstance().normalizeToString(point.getText()));
    }

    public TfIdf getTfIdf() {
        return tfIdf;
    }
}
