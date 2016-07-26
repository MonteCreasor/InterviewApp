package monte.apps.interviewapp.utils;

import java.util.function.DoubleUnaryOperator;

/**
 * Created by monte on 2016-07-24.
 */

public final class UnitUtils {
    public static final int FEET_PER_MILE = 5280;
    public static final DoubleUnaryOperator convertKmToMi = curriedConverter(5.0/8, 0);
    public static final DoubleUnaryOperator convertMiToKm = curriedConverter(8.0/5, 0);
    public static final DoubleUnaryOperator convertCtoF = curriedConverter(9.0/5, 32);
    public static final DoubleUnaryOperator convertFtoC = (double x) -> (x - 32) * 5.0/9;

    private UnitUtils() {
        throw new AssertionError();
    }

    static DoubleUnaryOperator curriedConverter(double f, double b) {
        return (double x) -> x * f + b;
    }
}
