package diploma.clustering;

import twitter4j.Status;

import java.io.Serializable;

/**
 * @author Никита
 */
public class EnhancedStatus implements Serializable {
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
