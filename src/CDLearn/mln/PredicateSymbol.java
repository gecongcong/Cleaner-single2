package CDLearn.mln;

import java.util.ArrayList;

/**
 * Created by gcc on 17-12-1.
 */
public class PredicateSymbol {
    int id;
    String symbol;
    ArrayList<Integer> domsizes;
    ArrayList<Integer> cumulativesz;

    void setcsz() {
        cumulativesz = new ArrayList<Integer>(domsizes.size());
        for (int i = 0; i < domsizes.size(); i++) {
            int mlt = 1;
            for (int j = i + 1; j < domsizes.size(); j++) {
                mlt *= domsizes.get(j);
            }
            cumulativesz.set(i, mlt);
        }
    }
}
