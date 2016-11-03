package diploma.clustering.endpoint;

import diploma.clustering.neuralgas.Neuron;

/**
 * @author Никита
 */
public class NeuronConverter {
    public NeuronDto convertNeuronToDto(Neuron neuron) {
        NeuronDto neuronDto = new NeuronDto();
        neuronDto.setId(neuron.getId());
        neuronDto.setCoordinatesVector(neuron.getCoordinatesVector());
        neuronDto.setPoints(neuron.getPoints());
        return neuronDto;
    }
}
