package diploma.clustering.statusesfilters;

import diploma.clustering.EnhancedStatus;

/**
 * @author Никита
 */
public class TweetLengthFilter implements StatusesFilter {
    @Override
    public boolean filter(EnhancedStatus status) {
        return status.getNormalizedText().split(" ").length >= 4;
    }
}
