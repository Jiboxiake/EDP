package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Parser;

public class ParserTest {
    public static void main(String[] args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        int a = -116243353;
        int b = 43617889;

        int top = 43618006;
        int horizontal = 43617898;
        int bot = 43617791;
        int left = -116243354;
        int vertical = -116243260;
        int right = -116243165;

        int newa = Parser.normalizeLat(b);
        int newb = Parser.normalizeLon(a);

        int newtop = Parser.normalizeLat(top);
        int newhor = Parser.normalizeLat(horizontal);
        int newbot = Parser.normalizeLat(bot);
        int newleft = Parser.normalizeLon(left);
        int newver = Parser.normalizeLon(vertical);
        int newright = Parser.normalizeLon(right);
        System.out.println(newa);
        System.out.println(newb);
        System.out.println();
        System.out.println(newtop);
        System.out.println(newhor);
        System.out.println(newbot);
        System.out.println(newleft);
        System.out.println(newver);
        System.out.println(newright);
    }
}
