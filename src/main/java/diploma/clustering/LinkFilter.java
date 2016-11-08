package diploma.clustering;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.FeatureSequenceWithBigrams;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * @author Nikita
 */
public class LinkFilter extends Pipe {
    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();
        TokenSequence ret = new TokenSequence ();
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            if (!t.getText().startsWith("https://"))
                ret.add (t);
        }
        carrier.setData(ret);
        return carrier;
    }
}
