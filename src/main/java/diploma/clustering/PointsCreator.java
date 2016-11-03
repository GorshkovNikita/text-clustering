package diploma.clustering;

import diploma.clustering.neuralgas.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Никита
 */
public class PointsCreator {
    public static List<Point> createTwoDimensionalPoints(int count) {
        List<Point> dataPoints = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Double x = 0.0, y = 0.0;
            int rnd = random.nextInt(4);
            switch (rnd) {
                case 0:
                    x = (double) 30 + random.nextInt(200);
                    y = (double) 30 + random.nextInt(200);
                    break;
                case 1:
                    x = (double) -30 - random.nextInt(200);
                    y = (double) 30 + random.nextInt(200);
                    break;
                case 2:
                    x = (double) 30 + random.nextInt(200);
                    y = (double) -30 - random.nextInt(200);
                    break;
                case 3:
                    x = (double) -30 - random.nextInt(200);
                    y = (double) -30 - random.nextInt(200);
                    break;
//                case 4:
//                    x = (double) random.nextInt(200);
//                    y = (double) random.nextInt(200);
//                    break;
//                case 5:
//                    x = (double) -250 - random.nextInt(200);
//                    y = (double) 250 + random.nextInt(200);
//                    break;
            }
            dataPoints.add(new Point(new Double[] {x, y}));
        }
        return dataPoints;
    }
}
