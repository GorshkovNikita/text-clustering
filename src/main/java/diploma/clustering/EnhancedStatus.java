package diploma.clustering;

import twitter4j.Status;

/**
 * @author Никита
 */
public class EnhancedStatus {
    private Status status;
    private String normalizedText;

    public EnhancedStatus(Status status) {
        this.status = status;
        this.normalizedText = TextNormalizer.getInstance().normalizeToString(status.getText());
    }

    public Status getStatus() {
        return status;
    }

    public String getNormalizedText() {
        return normalizedText;
    }
}
