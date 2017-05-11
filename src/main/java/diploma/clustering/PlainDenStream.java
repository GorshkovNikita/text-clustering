package diploma.clustering;

import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;
import diploma.clustering.clusters.StatusesCluster;
import diploma.clustering.dbscan.Dbscan;
import diploma.clustering.dbscan.points.DbscanPoint;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;
import diploma.clustering.dbscan.points.SimplifiedDbscanStatusesCluster;
import diploma.clustering.statusesfilters.SportsBetsFilter;
import diploma.clustering.statusesfilters.StatusesFilter;
import diploma.clustering.statusesfilters.TweetLengthFilter;
import diploma.statistics.MacroClusteringStatistics;
import diploma.statistics.dao.MacroClusteringStatisticsDao;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Никита
 */
public class PlainDenStream {
    private DenStream denStream;
    private Dbscan dbscan;
    private MacroClusteringStatisticsDao macroClusteringStatisticsDao;
    private int numberOfDocuments = 0;
    private int numberOfDocumentsIgnored = 0;
    private int minNumberOfCommonTerms = 6;
    private StatusesFilter sportsBetsFilter;
    private StatusesFilter tweetLengthFilter;
    private long totalTime;

    public PlainDenStream() {
        this.dbscan = new Dbscan(0, 0.6);
        this.denStream = new DenStream(10, 20, 10.0, 0.000001, 0.2);
        this.macroClusteringStatisticsDao = new MacroClusteringStatisticsDao();
        this.sportsBetsFilter = new SportsBetsFilter();
        this.tweetLengthFilter = new TweetLengthFilter();
        this.totalTime = 0;
    }

    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                totalTime += 30000;
                List<DbscanStatusesCluster> incomingPoints = new ArrayList<>();
                for (StatusesCluster cluster : PlainDenStream.this.denStream.getPotentialMicroClustering().getClusters()) {
                    if (cluster.getTfIdf().getTermFrequencyMap().size() > 100)
                        cluster.getTfIdf().setTermFrequencyMap(MapUtil.putFirstEntries(75, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap())));
                    incomingPoints.add(new SimplifiedDbscanStatusesCluster(cluster, minNumberOfCommonTerms, cluster.getMacroClusterId()));
                }

                for (StatusesCluster cluster : PlainDenStream.this.denStream.getOutlierMicroClustering().getClusters()) {
                    if (cluster.getTfIdf().getTermFrequencyMap().size() > 100)
                        cluster.getTfIdf().setTermFrequencyMap(MapUtil.putFirstEntries(75, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap())));
                }
                PlainDenStream.this.dbscan.run(incomingPoints);

                // сбор кластеров по id
                Clustering<Cluster<StatusesCluster>, StatusesCluster> macroClustering = new Clustering<>();
                for (DbscanPoint point: incomingPoints) { //statefulDbscan.getAllPoints()) {
                    if (point.isAssigned()) {
                        StatusesCluster statusesCluster = ((SimplifiedDbscanStatusesCluster) point).getStatusesCluster();
                        if (macroClustering.findClusterById(point.getClusterId()) == null) {
//                                if (statusesCluster.getMacroClusterId() == 0)
                            statusesCluster.setMacroClusterId(point.getClusterId());
                            Cluster<StatusesCluster> cluster = new Cluster<>(point.getClusterId(), PlainDenStream.this.denStream.lambda);
                            cluster.assignPoint(statusesCluster);
                            macroClustering.addCluster(cluster);
                        } else {
                            Cluster<StatusesCluster> cluster = macroClustering.findClusterById(point.getClusterId());
                            statusesCluster.setMacroClusterId(point.getClusterId());
                            cluster.assignPoint(statusesCluster);
                        }
                    }
                }

                Timestamp time = new Timestamp(new Date().getTime());
                for (Cluster<StatusesCluster> cluster : macroClustering.getClusters())
                    PlainDenStream.this.macroClusteringStatisticsDao.saveStatistics(PlainDenStream.this.denStream.getClusterStatistics(cluster, time,
                            PlainDenStream.this.numberOfDocuments - PlainDenStream.this.numberOfDocumentsIgnored,
                            PlainDenStream.this.numberOfDocuments / (double) totalTime * 1000,
                            PlainDenStream.this.denStream.getPotentialMicroClustering().getClusters().size(),
                            numberOfDocumentsIgnored));

                for (StatusesCluster cluster : denStream.getPotentialMicroClustering().getClusters())
                    cluster.resetProcessedPerTimeUnit();

                for (StatusesCluster cluster : denStream.getOutlierMicroClustering().getClusters())
                    cluster.resetProcessedPerTimeUnit();


            }
        }, 30000, 30000);

        process();
    }

    private void process() {
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\MSU\\diploma\\tweets-sets\\2017-04-09-sport-events-400000.txt"))) {
            String line = null;
            int i = 0;
            do {
                long start = System.currentTimeMillis();
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    EnhancedStatus status = null;
                    try {
                        this.numberOfDocuments++;
                        status = new EnhancedStatus(TwitterObjectFactory.createStatus(line));
                        if (this.tweetLengthFilter.filter(status) && this.sportsBetsFilter.filter(status))
                            denStream.processNext(status);
                        else this.numberOfDocumentsIgnored++;
                    } catch (TwitterException ignored) {}
                }
                else break;
//                }
            } while (line != null);
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PlainDenStream plainDenStream = new PlainDenStream();
        plainDenStream.run();
    }
}
