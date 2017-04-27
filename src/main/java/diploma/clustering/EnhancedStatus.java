package diploma.clustering;

import twitter4j.Status;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Никита
 */
public class EnhancedStatus implements Serializable {
    private Status status;
    private String normalizedText;
    private Date creationDate;

    public EnhancedStatus(Status status) {
        this.status = status;
        this.normalizedText = TextNormalizer.getInstance().normalizeToString(status.getText());
        this.creationDate = new Date();
    }

    public Status getStatus() {
        return status;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public Date getCreationDate() {
        return creationDate;
    }
}
