package diploma.clustering;

import diploma.clustering.clusters.StatusesClustering;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * @author Никита
 */
public class MapDbDenStream extends DenStream {
    private Atomic.Var<StatusesClustering> mapDbPotentialMicroClustering;
    private Atomic.Var<StatusesClustering> mapDbOutlierMicroClustering;
    private DB db;

    public MapDbDenStream(int minPoints, int mu, double beta, double lambda, double initSimilarity) {
        super(minPoints, mu, beta, lambda, initSimilarity);
        db = DBMaker.fileDB("D:\\MSU\\diploma\\mapdb-clustering\\micro-clusters.mapdb")
                .closeOnJvmShutdown()
                .make();
        mapDbPotentialMicroClustering = db
                .atomicVar("potentialMicroClustering", new StatusesClustering.MapDbSerializer())
                .createOrOpen();
        mapDbOutlierMicroClustering = db
                .atomicVar("outlierMicroClustering", new StatusesClustering.MapDbSerializer())
                .createOrOpen();
        if (mapDbPotentialMicroClustering.get() == null)
            mapDbPotentialMicroClustering.set(this.potentialMicroClustering);
        else this.potentialMicroClustering = mapDbPotentialMicroClustering.get();
        if (mapDbOutlierMicroClustering.get() == null)
            mapDbOutlierMicroClustering.set(this.outlierMicroClustering);
        else this.outlierMicroClustering = mapDbOutlierMicroClustering.get();
    }

//    @Override
//    public StatusesClustering getPotentialMicroClustering() {
//        return potentialMicroClustering.get();
//    }
//
//    @Override
//    public StatusesClustering getOutlierMicroClustering() {
//        return outlierMicroClustering.get();
//    }
}
