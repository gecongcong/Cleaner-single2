package CDLearn.main;

/**
 * Created by gcc on 17-12-1.
 */
public class Main {

    public void rec() {

    }

    public static void main(String[] args) {
        if (args.length < 9) {
            System.out.println("Usage::./cdlearn mlnfile evidfile queryfile totaliters ibound LEARN outputmlnfile constraintsfile");
            System.out.println("Usage::./cdlearn mlnfile evidfile queryfile totaliters ibound MAR resultfilename constraints");
            return;
        }
        String mlnfile = new String(args[1]);
        String evidfile = new String(args[2]);
        String qryfile = new String(args[3]);
        int totaliters = Integer.parseInt(new String(args[4]));//总的迭代次数
        int bound = Integer.parseInt(new String(args[5]));

        MLN mln;
    }

}
