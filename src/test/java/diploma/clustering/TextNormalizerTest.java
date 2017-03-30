package diploma.clustering;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Никита
 */
public class TextNormalizerTest {
    TextNormalizer normalizer;

    @Before
    public void setUp() {
        normalizer = TextNormalizer.getInstance();
    }

    @Test
    public void testisVerbOrNoun() {
        String verb = "VB";
        String noun = "NN";
        String adjective = "JJ";
        assertEquals(false, normalizer.isNoun(verb));
        assertEquals(true, normalizer.isNoun(noun));
        assertEquals(false, normalizer.isNoun(adjective));
    }
}
