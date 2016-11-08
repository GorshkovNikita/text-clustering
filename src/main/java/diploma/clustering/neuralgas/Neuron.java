package diploma.clustering.neuralgas;

import diploma.clustering.VectorOperations;

import java.util.*;

/**
 * @author Никита
 */
public class Neuron {
    private Double[] coordinatesVector;
    /**
     * Все нейроны, имеющие связь с текущим
     * Переделал на Set, чтобы можно было не проверять наличие связи при создании/обновлении
     */
    private Set<Neuron> neighbours;
    /**
     * Данные, ассоциированные с этим нейроном
     */
    private List<Point> points;
    /**
     * Сумма расстояний от текущего нейрона до всех данных (точек),
     * ассоциированных с ним
     */
    private Double totalDistanceBetweenNeuronAndItsPoints;
    /**
     * Накапливающаяся ошибка (для стандартного алгоритма)
     */
    private Double error;
    /**
     * Id нейрона для проверки уникальности и методов hashCode и equals
     * Id получается при помощи класса {@link diploma.clustering.neuralgas.NeuralGas},
     * в котором хранится глобальный счетчик нейронов, увеличивающийся с каждым добавленным
     * Увеличивается при вызове метода {@link diploma.clustering.neuralgas.NeuralGas#createNewNeuron(Neuron)}
     */
    private int id = -1;

    public Neuron(Double[] coordinatesVector) {
        this.coordinatesVector = coordinatesVector;
        neighbours = new HashSet<>();
        points = new ArrayList<>();
        totalDistanceBetweenNeuronAndItsPoints = 0.0;
        error = 0.0;
    }

    /**
     * Порог, по которому определяется создавать новый нейрон или нет
     * TODO: желательно написать формулу
     */
    public Double getThreshold() {
        Double threshold;
        final boolean[] neighboursHavePoints = new boolean[1];
            neighbours.forEach((node) -> neighboursHavePoints[0] = node.getPoints().size() != 0);
        //TODO: условие для новых нейронов без соседей
        if (neighbours.size() == 0 && points.size() <= 100) {
            threshold = NeuralGas.getInstance().getDistanceBetweenGivenAndNearestNeurons(this);
        }
        else if ((neighbours.size() != 0 && neighboursHavePoints[0]) || points.size() != 0) {
            Double totalDistanceBetweenNeuronAndAllNeighbours = 0.0;
            int totalNumberOfPointsOfAllNeighbours = 0;
            for (Neuron neighbour: neighbours) {
                int numberOfPointsOfNeighbour = neighbour.getPoints().size();
                totalDistanceBetweenNeuronAndAllNeighbours += numberOfPointsOfNeighbour * VectorOperations.euclideanDistance
                        (coordinatesVector, neighbour.getCoordinatesVector());
                totalNumberOfPointsOfAllNeighbours += numberOfPointsOfNeighbour;
            }
            threshold = ((Math.pow(totalDistanceBetweenNeuronAndItsPoints, 2) + Math.pow(totalDistanceBetweenNeuronAndAllNeighbours, 2)) / (points
                    .size() + totalNumberOfPointsOfAllNeighbours));
        }
        else {
            threshold = NeuralGas.getInstance().getDistanceBetweenGivenAndNearestNeurons(this);
        }
        return threshold;
    }

    public Neuron findNeighbourWithMaximumError() {
        if (!(getNeighbours().size() >= 1))
            return null;
        Iterator<Neuron> neurons = getNeighbours().iterator();
        Neuron neuronWithMaxError = neurons.next();
        Double maxError = neuronWithMaxError.getError();
        while (neurons.hasNext()) {
            Neuron neuron = neurons.next();
            Double neuronError = neuron.getError();
            if (neuronError > maxError) {
                maxError = neuronError;
                neuronWithMaxError = neuron;
            }
        }
        return neuronWithMaxError;
    }

    /**
     * Ассоцировать точку с нейроном
     * @param point - точка из набора данных
     */
    public void attachPoint(Point point) {
        points.add(point);
        totalDistanceBetweenNeuronAndItsPoints += VectorOperations.euclideanDistance(this.getCoordinatesVector(), point.getCoordinatesVector());
        error += Math.pow(VectorOperations.euclideanDistance(this.getCoordinatesVector(), point.getCoordinatesVector()), 2);
    }

    public void detachPoint(Point point) {
        points.remove(point);
        totalDistanceBetweenNeuronAndItsPoints -= VectorOperations.euclideanDistance(this.getCoordinatesVector(), point.getCoordinatesVector());
    }

    /**
     * Добавить соседний нейрон после создания связи между ними
     * @param neuron - новый соседний нейрон
     */
    public void addNeighbour(Neuron neuron) {
        neighbours.add(neuron);
    }

    /**
     * Удалить соседний нейрон после разрыва связи между ними
     * @param neuron - удаляемый нейрон
     */
    public void deleteNeighbour(Neuron neuron) {
        neighbours.remove(neuron);
    }

    public Double[] getCoordinatesVector() {
        return coordinatesVector;
    }

    public void setCoordinatesVector(Double[] coordinatesVector) {
        this.coordinatesVector = coordinatesVector;
    }

    public Set<Neuron> getNeighbours() {
        return neighbours;
    }

    public List<Point> getPoints() {
        return points;
    }

    public Double getTotalDistanceBetweenNeuronAndItsPoints() {
        return totalDistanceBetweenNeuronAndItsPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getError() {
        return error;
    }

    public void setError(Double error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Neuron)) return false;

        Neuron neuron = (Neuron) o;

        if (id != neuron.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
