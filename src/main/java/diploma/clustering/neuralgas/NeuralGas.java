package diploma.clustering.neuralgas;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import diploma.clustering.VectorOperations;
import diploma.clustering.neuralgas.exceptions.NeuralGasIsNotInitialized;
import diploma.clustering.neuralgas.exceptions.NeuronAlreadyExistsException;
import diploma.clustering.neuralgas.exceptions.NeuronNotExistsException;

import java.util.*;

/**
 * @author Никита
 */
public class NeuralGas {
    private static NeuralGas instance;

    /**
     * Матрица связей нейронов. Значение -1 обозначает, что связи нет.
     * Числа большие 0 показывают возраст связи
     */
    private Table<Neuron, Neuron, Integer> connections = HashBasedTable.create();

    /**
     * Все нейроны, находящиеся в сети
     */
    private List<Neuron> neurons = new ArrayList<>();
    private int nextId;

    /**
     * Среднее расстояние от всех нейронов до центра масс всех точек набора данных
     * TODO: сделать функцию обновления
     */
    private Double meanDistance;

    private NeuralGas() {
    }

    public static NeuralGas getInstance() {
        if (instance == null)
            instance = new NeuralGas();
        return instance;
    }

    /**
     * Инициализация объекта нейронного газа с двумя начальными нейронами и связью между ними
     */
    public void init(Double[] firstVector, Double[] secondVector) {
        Neuron firstNeuron = new Neuron(firstVector);
        Neuron secondNeuron = new Neuron(secondVector);
        createNewNeuron(firstNeuron);
        createNewNeuron(secondNeuron);
        createConnectionBetweenNeurons(firstNeuron, secondNeuron);
        meanDistance = 0.0;
        this.nextId = 2;
    }

    /**
     * Определение минимального расстояния от заданного нейрона до ближайшего
     * @param neuron - заданный нейрон
     * @return - расстояние от заданного нейрона до ближайшего
     * @throws NeuronNotExistsException
     */
    public Double getDistanceBetweenGivenAndNearestNeurons(Neuron neuron) throws NeuronNotExistsException {
        if (!neurons.contains(neuron))
            throw new NeuronNotExistsException();
        Double minimumDistance = Double.MAX_VALUE;
        for (Neuron iterativeNeuron : neurons) {
            if (!iterativeNeuron.equals(neuron)) {
                Double distance = VectorOperations.euclideanDistance(neuron.getCoordinatesVector(), iterativeNeuron.getCoordinatesVector());
                if (distance < minimumDistance)
                    minimumDistance = distance;
            }
        }
        return minimumDistance;
    }

    /**
     * Создание связи между нейронами.
     * Также с помощью этого метода можно обновлять связь, сбрасывая ее возраст в 0
     * @param firstNeuron - первый нейрон
     * @param secondNeuron - второй нейрон
     * @throws NeuronNotExistsException
     */
    public void createConnectionBetweenNeurons(Neuron firstNeuron, Neuron secondNeuron) throws NeuronNotExistsException {
        if (!(neurons.contains(firstNeuron) && neurons.contains(secondNeuron)))
            throw new NeuronNotExistsException();
        connections.put(firstNeuron, secondNeuron, 0);
        // увеличивает количество необходимой памяти, зато делает таблицу симметричной
        connections.put(secondNeuron, firstNeuron, 0);
        firstNeuron.addNeighbour(secondNeuron);
        secondNeuron.addNeighbour(firstNeuron);
    }

    public void deleteConnectionBetweenNeurons(Neuron firstNeuron, Neuron secondNeuron) throws NeuronNotExistsException {
        if (!(neurons.contains(firstNeuron) && neurons.contains(secondNeuron)))
            throw new NeuronNotExistsException();
        connections.remove(firstNeuron, secondNeuron);
        connections.remove(secondNeuron, firstNeuron);
        firstNeuron.deleteNeighbour(secondNeuron);
        secondNeuron.deleteNeighbour(firstNeuron);
    }

    /**
     * Добаление нового нейрона в сеть
     * @param newNeuron - новый нейрон
     * @throws NeuronAlreadyExistsException
     */
    public void createNewNeuron(Neuron newNeuron) throws NeuronAlreadyExistsException {
        newNeuron.setId(nextId++);
        for (Neuron neuron : neurons) {
            if (newNeuron.equals(neuron))
                throw new NeuronAlreadyExistsException();
        }
        neurons.add(newNeuron);
    }

    public void insertNewNeuronBetween(Neuron firstNeuron, Neuron secondNeuron) throws NeuronNotExistsException {
        if (!neurons.contains(firstNeuron) || !neurons.contains(secondNeuron))
            throw new NeuronNotExistsException();
        Double[] coordinateVectorForNewNeuron = VectorOperations.scalarMultiplication(
                VectorOperations.addition(firstNeuron.getCoordinatesVector(), secondNeuron.getCoordinatesVector()), 0.5);
        Neuron newNeuron = new Neuron(coordinateVectorForNewNeuron);
        createNewNeuron(newNeuron);
        deleteConnectionBetweenNeurons(firstNeuron, secondNeuron);
        createConnectionBetweenNeurons(newNeuron, firstNeuron);
        createConnectionBetweenNeurons(newNeuron, secondNeuron);
        firstNeuron.setError(firstNeuron.getError() * Configuration.ERROR_REDUCTION_FACTOR_AFTER_INSERTION);
        secondNeuron.setError(secondNeuron.getError() * Configuration.ERROR_REDUCTION_FACTOR_AFTER_INSERTION);
        newNeuron.setError(firstNeuron.getError());
        // TODO: присоединить все ближайшие точки
    }

    /**
     * Поиск двух ближайших нейронов к точке из набора данных
     * @param point - точка из набора данных
     * @return - массив двух ближайших нейронов, где 0-ой нейрон - ближайший, а 1-ый второй ближайший
     */
    public Neuron[] findTwoNearestNeurons(Point point) {
        Neuron[] nearestNeurons = new Neuron[2];
        Double minimumDistance = VectorOperations.euclideanDistance(neurons.get(0).getCoordinatesVector(), point.getCoordinatesVector());
        Double secondNearestDistance = VectorOperations.euclideanDistance(neurons.get(1).getCoordinatesVector(), point.getCoordinatesVector());
        if (minimumDistance > secondNearestDistance) {
            Double temp = secondNearestDistance;
            secondNearestDistance = minimumDistance;
            minimumDistance = temp;
            nearestNeurons[0] = neurons.get(1);
            nearestNeurons[1] = neurons.get(0);
        }
        else {
            nearestNeurons[0] = neurons.get(0);
            nearestNeurons[1] = neurons.get(1);
        }
        for (int i = 2; i < neurons.size(); i++) {
            Double distance = VectorOperations.euclideanDistance(neurons.get(i).getCoordinatesVector(), point.getCoordinatesVector());
            if (distance < minimumDistance) {
                nearestNeurons[1] = nearestNeurons[0];
                nearestNeurons[0] = neurons.get(i);
                secondNearestDistance = minimumDistance;
                minimumDistance = distance;
            }
            else if (distance < secondNearestDistance) {
                nearestNeurons[1] = neurons.get(i);
                secondNearestDistance = distance;
            }
        }
        return nearestNeurons;
    }

    /**
     * Поиск ближайшего нейрона к точке набора данных
     * @param point - точка
     * @return - ближайший нейрон
     */
    public Neuron findNearestNeuron(Point point) {
        Double minimumDistance = VectorOperations.euclideanDistance(neurons.get(0).getCoordinatesVector(), point.getCoordinatesVector());
        Neuron nearestNeuron = neurons.get(0);
        for (int i = 1; i < neurons.size(); i++) {
            Double distance = VectorOperations.euclideanDistance(neurons.get(i).getCoordinatesVector(), point.getCoordinatesVector());
            if (distance < minimumDistance) {
                nearestNeuron = neurons.get(i);
                minimumDistance = distance;
            }
        }
        return nearestNeuron;
    }

    /**
     * Увеличение возраста всех соседей заданного нейрона
     * @param neuron - нейрон, возраст соседей которого нужно увеличить
     * @throws NeuronNotExistsException
     */
    public void increaseAgeOfNeuronConnections(Neuron neuron) throws NeuronNotExistsException {
        if (!neurons.contains(neuron))
            throw new NeuronNotExistsException();
        for (Map.Entry<Neuron, Integer> neighbour: connections.row(neuron).entrySet()) {
            if (!neighbour.getKey().equals(neuron) && neighbour.getValue() >= 0) {
                neighbour.setValue(neighbour.getValue() + 1);
                // обновление значения в симметричной ячейке таблицы
                connections.put(neighbour.getKey(), neuron, connections.get(neighbour.getKey(), neuron) + 1);
            }
        }
    }

    /**
     * Передвинуть ближайший нейрон и всех его соседей ближе к полученной точке
     * @param neuron - нейрон
     * @param point - точка
     */
    public void moveNeuronAndItsNeighboursToPoint(Neuron neuron, Point point) {
        Double eps = 1.0 / neuron.getPoints().size();
        Double secondaryEps = 1.0 / (100.0 * neuron.getPoints().size());
//        Double eps = 0.2;
//        Double secondaryEps = 0.006;
        neuron.setCoordinatesVector(VectorOperations.addition(neuron.getCoordinatesVector(),
                VectorOperations.scalarMultiplication(
                        VectorOperations.subtraction(point.getCoordinatesVector(), neuron.getCoordinatesVector()), eps)));
        for (Neuron neighbour: neuron.getNeighbours()) {
            neighbour.setCoordinatesVector(VectorOperations.addition(neighbour.getCoordinatesVector(),
                    VectorOperations.scalarMultiplication(
                            VectorOperations.subtraction(point.getCoordinatesVector(), neighbour.getCoordinatesVector()), secondaryEps)));
        }
    }

    /**
     * Поиск нейрона с наибольшей накопленной ошибкой
     * @return - нейрон с наибольшей ошибкой
     */
    public Neuron findNeuronWithMaximumError() {
        Double maxError = Double.MIN_VALUE;
        Neuron neuronWithMaxError = neurons.get(0);
        for (int i = 1; i < neurons.size(); i++) {
            Neuron neuron = neurons.get(i);
            Double neuronError = neuron.getError();
            if (neuronError > maxError) {
                maxError = neuronError;
                neuronWithMaxError = neuron;
            }
        }
        return neuronWithMaxError;
    }

    /**
     * Ассоцииация всех точек соседей нового нейрона с ним
     */
    public void reattachPoints() {
        for (Neuron neuron: neurons) {
            Map<Point, Neuron> needToReattachPointsWithNearestNeuron = new HashMap<>();
            for (Point point: neuron.getPoints()) {
                Neuron nearestNeuron = findNearestNeuron(point);
                if (!nearestNeuron.equals(neuron)) {
                    needToReattachPointsWithNearestNeuron.put(point, nearestNeuron);
                }
            }
            for (Map.Entry<Point, Neuron> pointWithNearestNeuron: needToReattachPointsWithNearestNeuron.entrySet()) {
                pointWithNearestNeuron.getValue().attachPoint(pointWithNearestNeuron.getKey());
                neuron.detachPoint(pointWithNearestNeuron.getKey());
            }
        }
    }

    /**
     * An Adaptive Incremental Clustering Method Based on the Growing Neural Gas Algorithm
     * https://hal.archives-ouvertes.fr/hal-00794354/document
     * @param dataPoints - набор данных
     */
    public void runAdaptiveIncrementalClustering(List<Point> dataPoints) throws NeuralGasIsNotInitialized {
        if (neurons.size() < 2)
            throw new NeuralGasIsNotInitialized();
        for (Point dataPoint: dataPoints) {
            Neuron[] nearestNeurons = findTwoNearestNeurons(dataPoint);
            // TODO: в моем случае смысла проверять 2 трешхолда нет, тк в каждом случае я создаю новое соединение между нейронами
            if (VectorOperations.euclideanDistance(
                    dataPoint.getCoordinatesVector(), nearestNeurons[0].getCoordinatesVector()) > nearestNeurons[0].getThreshold()) {
                Neuron newNeuron = new Neuron(dataPoint.getCoordinatesVector());
                createNewNeuron(newNeuron);
                newNeuron.attachPoint(dataPoint);
                // --------------------------------------
//                Double[] coordinateVectorForSecondNewNeuron = VectorOperations.scalarMultiplication(
//                        VectorOperations.addition(newNeuron.getCoordinatesVector(), nearestNeurons[0].getCoordinatesVector()), 0.5);
//                Neuron secondNewNeuron = new Neuron(coordinateVectorForSecondNewNeuron);
//                createNewNeuron(secondNewNeuron);
//                createConnectionBetweenNeurons(newNeuron, secondNewNeuron);
//                createConnectionBetweenNeurons(secondNewNeuron, nearestNeurons[0]);
//                reattachPoints();
                // TODO: присоединить все ближайшие нейроны
                // ---------------------------------------
                // TODO: создал связь, так как, если новый нейрон был очень далеко от остальных,
                // TODO: он не имел с ними связи, следовательно не имел соседей, и могло так произойти,
                // TODO: что его первоначальный threshold был равен 0, из-за этого даже точки, находящиеся близко создавали новые нейроны
                createConnectionBetweenNeurons(nearestNeurons[0], newNeuron);
            }
            else if (VectorOperations.euclideanDistance(
                    dataPoint.getCoordinatesVector(), nearestNeurons[1].getCoordinatesVector()) > nearestNeurons[1].getThreshold()) {
                Neuron newNeuron = new Neuron(dataPoint.getCoordinatesVector());
                createNewNeuron(newNeuron);
                newNeuron.attachPoint(dataPoint);
                createConnectionBetweenNeurons(nearestNeurons[0], newNeuron);
            }
            else {
                nearestNeurons[0].attachPoint(dataPoint);
                increaseAgeOfNeuronConnections(nearestNeurons[0]);
                moveNeuronAndItsNeighboursToPoint(nearestNeurons[0], dataPoint);
                createConnectionBetweenNeurons(nearestNeurons[0], nearestNeurons[1]);
                List<Neuron> needToDeleteNeurons = new ArrayList<>();
                for (Neuron neighbour: nearestNeurons[0].getNeighbours()) {
                    if (connections.get(nearestNeurons[0], neighbour) != null && connections.get(nearestNeurons[0], neighbour) > Configuration.MAX_AGE_OF_CONNECTION)
                        needToDeleteNeurons.add(neighbour);
                }
                for (Neuron neuron: needToDeleteNeurons) {
                    deleteConnectionBetweenNeurons(nearestNeurons[0], neuron);
                }
            }
        }
    }

    /**
     * Стандартный растущий нейронный газ
     * https://papers.nips.cc/paper/893-a-growing-neural-gas-network-learns-topologies.pdf
     * @param dataPoints - набор данных
     */
    public void runStandardGrowingNeuralGas(List<Point> dataPoints) throws NeuralGasIsNotInitialized {
        if (neurons.size() < 2)
            throw new NeuralGasIsNotInitialized();
        for (int i = 0; i < dataPoints.size(); i++) {
            Neuron[] nearestNeurons = findTwoNearestNeurons(dataPoints.get(i));
            nearestNeurons[0].attachPoint(dataPoints.get(i));
            increaseAgeOfNeuronConnections(nearestNeurons[0]);
            moveNeuronAndItsNeighboursToPoint(nearestNeurons[0], dataPoints.get(i));
            createConnectionBetweenNeurons(nearestNeurons[0], nearestNeurons[1]);
            List<Neuron> needToDeleteNeurons = new ArrayList<>();
            for (Neuron neighbour: nearestNeurons[0].getNeighbours()) {
                if (connections.get(nearestNeurons[0], neighbour) != null && connections.get(nearestNeurons[0], neighbour) > Configuration.MAX_AGE_OF_CONNECTION)
                    needToDeleteNeurons.add(neighbour);
            }
            for (Neuron neuron: needToDeleteNeurons) deleteConnectionBetweenNeurons(nearestNeurons[0], neuron);
            for (Neuron neuron: neurons) {
                if (neuron.getNeighbours().size() == 0) {
                    List<Point> points = neuron.getPoints();
                    neurons.remove(neuron);
                    for (Point point: points) {
                        Neuron nearestNeuron = findNearestNeuron(point);
                        nearestNeuron.attachPoint(point);
                    }
                }
            }
            if (i % Configuration.STEP_FOR_NEW_NEURON == 0) {
                Neuron neuronWithMaximumError = findNeuronWithMaximumError();
                insertNewNeuronBetween(neuronWithMaximumError, neuronWithMaximumError.findNeighbourWithMaximumError());
            }
            for (Neuron neuron: neurons) neuron.setError(neuron.getError() * Configuration.ERROR_REDUCTION_FACTOR);
        }
    }

    public Table<Neuron, Neuron, Integer> getConnections() {
        return connections;
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public Double getMeanDistance() {
        return meanDistance;
    }

    public void setMeanDistance(Double meanDistance) {
        this.meanDistance = meanDistance;
    }

    public void printInfo() {
        System.out.println("Количество нейронов = " + neurons.size());
        for (Neuron neuron: neurons) {
            System.out.println("------- Нейрон с координатами = " + Arrays.toString(neuron.getCoordinatesVector()) + " и id = " + neuron.getId() + "--------");
            System.out.println("\tКоличество точек = " + neuron.getPoints().size());
            System.out.println("\tСвязан с нейронами:");
            for (Neuron neuron1: neuron.getNeighbours())
                System.out.println("\t\t id= " + neuron1.getId());
        }
    }

    /**
     * Очищение объекта синглтона перед каждым тестом
     */
    public static void clearStateForTesting() {
        instance = null;
    }
}
