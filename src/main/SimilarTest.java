package main;

import info.debatty.java.stringsimilarity.*;

/**
 * Created by gcc on 17-12-23.
 */
public class SimilarTest {
    public static void main(String[] args) {
        String s1 = "36301";
        String s2 = "36301a";
        MetricLCS lcs = new MetricLCS();
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        Cosine cosine = new Cosine();
        QGram qGram = new QGram();
        JaroWinkler jw = new JaroWinkler();
        double qgR = qGram.distance(s1,s2);
        double lcsR = lcs.distance(s1, s2);
        double lsR = l.distance(s1,s2);
        double cosR = cosine.distance(s1,s2);
        double jwR = jw.similarity(s1,s2);
        System.out.println("lcs result="+lcsR);
        System.out.println("NormalizedLevenshtein result="+lcsR);
        System.out.println("cosine result="+cosR);
        System.out.println("QGram result="+qgR);
        System.out.println("JaroWinkler result="+jwR);
    }
}
