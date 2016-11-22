package diploma.clustering.tfidf;

import diploma.clustering.TextNormalizer;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class Cluster {
    private TfIdf tfIdf;
    private List<Status> statuses;

    public Cluster() {
        tfIdf = new TfIdf();
        statuses = new ArrayList<>();
    }

    public void assignStatus(Status status) {
        statuses.add(status);
        tfIdf.updateForNewDocument(Long.toString(status.getId()), TextNormalizer.getInstance().normalizeToString(status.getText()));
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public TfIdf getTfIdf() {
        return tfIdf;
    }
}
