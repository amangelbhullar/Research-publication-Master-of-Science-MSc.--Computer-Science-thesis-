import java.util.Comparator;
import java.util.Objects;

public class Expert {

        public int expertId;
        public double distance;

        public static Comparator<Expert> DistComparator() {
            Comparator comp = new Comparator<Expert>() {
                public int compare(Expert s1, Expert s2) {
                    if (s1.distance > s2.distance)
                        return 1;
                    if (s1.distance < s2.distance)
                        return -1;
                    return 0;
                }
            };
            return comp;
        }


        public Expert()
        {
//        this.expertId = expertId;
//        this.distance = distance;

        }
        public int getExpertId()
        {
            return expertId;
        }
        public void setExpertId(int expertId)
        {
            this.expertId = expertId;
        }
        public double getDistance()
        {
            return distance;
        }
        public void setDistance(double distance)
        {
            this.distance = distance;
        }

        @Override public String toString()
        {
            return String.format("(%d,%f)", expertId, distance);
        }
        @Override public int hashCode()
        {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.expertId);
            hash = 79 * hash + Objects.hashCode(this.distance);
            return hash;
        }
        @Override public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Expert other = (Expert) obj;
            if (!Objects.equals(this.expertId, other.expertId)) {
                return false;
            }
            if (!Objects.equals(this.distance, other.distance)) {
                return false;
            }

            return true;
        }
}
