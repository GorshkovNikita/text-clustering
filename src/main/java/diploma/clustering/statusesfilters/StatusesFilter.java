package diploma.clustering.statusesfilters;

import diploma.clustering.EnhancedStatus;

/**
 * @author Никита
 */
public interface StatusesFilter {
    public boolean filter(EnhancedStatus status);
}
