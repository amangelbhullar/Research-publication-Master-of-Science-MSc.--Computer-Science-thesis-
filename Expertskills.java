import java.util.Comparator;

public class Expertskills {
    public int[] experts;

    public String skills;
    public int totconn;
    public int expertId;
    public double distance;

    public static Comparator<Expertskills> DegreeComparator() {
        Comparator comp = new Comparator<Expertskills>() {
            public int compare(Expertskills s1, Expertskills s2) {
                if (s1.totconn > s2.totconn)
                    return 1;
                if (s1.totconn < s2.totconn)
                    return -1;
                return 0;
            }
        };
        return comp;
    }


}
