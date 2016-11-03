package diploma.clustering.endpoint;

import diploma.clustering.neuralgas.Point;

import java.io.Serializable;
import java.util.List;

/**
 * @author Никита
 */
public class NeuronDto implements Serializable {
    private int id;
    private Double[] coordinatesVector;
    private List<Point> points;

    public NeuronDto() {
    }

    public Double[] getCoordinatesVector() {
        return coordinatesVector;
    }

    public void setCoordinatesVector(Double[] coordinatesVector) {
        this.coordinatesVector = coordinatesVector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
