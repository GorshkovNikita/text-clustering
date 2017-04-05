package diploma.clustering.dbscan.points;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class SimplifiedDbscanStatusesClusterTest extends TestCase {

    public void testHasCommonTopTerms() throws Exception {
        SimplifiedDbscanStatusesCluster cluster = new SimplifiedDbscanStatusesCluster();
        Map<String, Integer> map1 = new HashMap<>();
        Map<String, Integer> map2 = new HashMap<>();
        map1.put("term", 10);
        map1.put("moscow", 12);
        map1.put("saint", 10);
        map1.put("hover", 12);
        map1.put("some", 31);
        map1.put("123", 12);

        map2.put("term", 10);
        map2.put("moscow", 12);
        map2.put("saint", 10);
        map2.put("hover", 12);
        map2.put("some", 1);

        assertEquals(true, cluster.hasCommonTopTerms(map1, map2));
    }
}