package monte.apps.interviewapp.utils;


/**
 * Created by monte on 2016-07-24.
 */

public final class UnitUtils {
    public static final int FEET_PER_MILE = 5280;

    private UnitUtils() {
        throw new AssertionError();
    }

    static double convertKmToMi(double x) {
        return  x * 5.0 / 8.0;
    }
}
