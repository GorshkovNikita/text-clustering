package diploma.clustering.statusesfilters;

import diploma.clustering.EnhancedStatus;

import java.util.Arrays;
import java.util.List;

/**
 * Фильтр твитов от аккаунтов со ставками.
 * @author Никита
 */
public class SportsBetsFilter implements StatusesFilter {
    @Override
    public boolean filter(EnhancedStatus status) {
        String userScreenName = status.getStatus().getUser().getScreenName().toLowerCase();
        String retweetedUserScreenName = "";
        if (status.getStatus().getRetweetedStatus() != null)
            retweetedUserScreenName = status.getStatus().getRetweetedStatus().getUser().getScreenName().toLowerCase();
        List<String> ignoredUsers = Arrays.asList("petebetnow", "paddyspower1", "bingobestodds", "highrisklife1", "paddyspower2",
                "mufcfergie", "kim_feeney1", "roadtoprofituk", "olbg", "earnathomeuk");
        return !(ignoredUsers.contains(userScreenName) || ignoredUsers.contains(retweetedUserScreenName));
    }
}
