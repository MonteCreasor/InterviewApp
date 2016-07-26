package monte.apps.interviewapp.utils;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import monte.apps.interviewapp.R;

/**
 * Created by monte on 2016-07-24.
 */
public class LocaleUtils {
    public enum Units {
        Imperial,
        Metric
    }

    public static Units getDefault() {
        return getUnitsFrom(Locale.getDefault());
    }

    public static Units getUnitsFrom(Locale locale) {
        switch (locale.getCountry()) {
            case "US":
            case "LR":
            case "MM":
            case "GB":
                return Units.Imperial;
            default:
                return Units.Metric;
        }
    }

    public static String getPrintableDistanceFromMeters(Context context, double distance, int precision) {
        return getPrintableDistanceFromKm(context, distance / 1000, precision);
    }

    public static String getPrintableDistanceFromKm(Context context, double distance, int precision) {
        String format;

        if (Units.Metric.equals(LocaleUtils.getDefault())) {
            if (distance >= 1) {
                format = context.getString(R.string.distance_in_kilometers);
            } else {
                format = context.getString(R.string.distance_in_meters);
                precision = 0;
            }
        } else {
            distance = (float)UnitUtils.convertKmToMi.applyAsDouble(distance);

            if (distance * UnitUtils.FEET_PER_MILE > 1000) {
                format = context.getString(R.string.distance_in_miles);
            } else {
                format = context.getString(R.string.distance_in_feet);
                precision = 0;
            }
        }

        DecimalFormat decimalFormat =
                (DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault());
        decimalFormat.setMaximumFractionDigits(precision);

        return String.format(Locale.getDefault(), format, decimalFormat.format(distance));
    }
}
