package diploma.clustering.neuralgas;

import java.util.Arrays;

/**
 * @author Никита
 */
public class Point {
    private long id;
    private String tweetText;
    private Double[] coordinatesVector;
    private static long globalCounter = 0;

    public Point(Double[] coordinatesVector) {
        this.coordinatesVector = coordinatesVector;
        this.id = ++globalCounter;
    }

    public Point(Double[] coordinatesVector, String tweetText) {
        this.coordinatesVector = coordinatesVector;
        this.id = ++globalCounter;
        this.tweetText = tweetText;
    }

    public Double[] getCoordinatesVector() {
        return coordinatesVector;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        if (id != point.id) return false;
        if (!Arrays.equals(coordinatesVector, point.coordinatesVector)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + Arrays.hashCode(coordinatesVector);
        return result;
    }
}
