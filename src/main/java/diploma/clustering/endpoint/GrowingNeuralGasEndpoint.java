package diploma.clustering.endpoint;

import diploma.clustering.PointsCreator;
import diploma.clustering.neuralgas.NeuralGas;
import diploma.clustering.neuralgas.Neuron;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
@Singleton
@Path("/")
public class GrowingNeuralGasEndpoint {
    private NeuralGas neuralGas;
    private NeuronConverter neuronConverter;

    public GrowingNeuralGasEndpoint() {
    }

    @GET
    @Path("{count}")
    @Produces(MediaType.APPLICATION_JSON)
    public String start(@PathParam("count") int count) {
        NeuralGas.clearStateForTesting();
        neuronConverter = new NeuronConverter();
        neuralGas = NeuralGas.getInstance();
        neuralGas.init(new Double[] {1000.0, 1000.0}, new Double[] {1001.0, 10001.0});
        neuralGas.runAdaptiveIncrementalClustering(PointsCreator.createTwoDimensionalPoints(count));
//        neuralGas.runStandardGrowingNeuralGas(PointsCreator.createTwoDimensionalPoints(count));
        neuralGas.printInfo();
        return "ready";
    }

    @GET
    @Path("json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NeuronDto> getNextBatch() {
        List<NeuronDto> result = new ArrayList<>();
        for (Neuron neuron: neuralGas.getNeurons()) {
            result.add(neuronConverter.convertNeuronToDto(neuron));
        }
        return result;
    }
}
