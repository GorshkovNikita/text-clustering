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
        normalizer = new TextNormalizer();
    }

    @Test
    public void testisVerbOrNoun() {
        String verb = "VB";
        String noun = "NN";
        String adjective = "JJ";
        assertEquals(true, normalizer.isVerbOrNoun(verb));
        assertEquals(true, normalizer.isVerbOrNoun(noun));
        assertEquals(false, normalizer.isVerbOrNoun(adjective));
    }
}
