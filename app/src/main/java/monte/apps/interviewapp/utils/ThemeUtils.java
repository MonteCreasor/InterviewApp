package monte.apps.interviewapp.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import monte.apps.interviewapp.R;

/**
 * Created by monte on 03/02/16.
 */
public class ThemeUtils {

    public static final String PREF_ANIMATION_IMAGE_FADE_DURATION = "image_fade_duration";
    public static final String PREF_ANIMATION_COLOR_MORPH_DURATION = "color_morph_duration";
    public static final String PREF_USE_DYNAMIC_BACKGROUND_PALETTE = "dynamic_background_palette";
    public static final String PREF_USE_DYNAMIC_TEXT_PALETTE = "dynamic_text_palette";

    public static void updateThemePalette(final Activity activity,
                                          final ColorUtils.PaletteSwatch palette,
                                          float expansionFraction) {

        final View actionBarView = getActionBarView(activity);

        int actionBarColor = R.color.colorPrimary;
        if (actionBarView != null) {
            ColorDrawable colorDrawable = (ColorDrawable) actionBarView.getBackground();
            actionBarColor = colorDrawable != null ?
                    colorDrawable.getColor() :
                    actionBarView.getDrawingCacheBackgroundColor();
        }

        final int startActionBarColor = actionBarColor;

        // Set ending ActionBar color to the new primary accent.
        final int endActionBarColor = palette.primaryAccent;

        // Get starting status bar color based on its current color.
        final int startStatusBarColor = getStatusBarColor(activity);

        // The ending status bar color is always based on a slightly
        // darker shade of the ActionBar ending color.
        final int endStatusBarColor =
                ColorUtils.adjustColorBrightnessHSV(
                        endActionBarColor,
                        activity.getResources().getFraction(
                                R.fraction.status_bar_color_offset, 1, 1));

        // Get duration from shared preferences to stay in sync with
        // other concurrent color morphing animators run by caller.
        final int colorMorphDuration =
                PreferenceManager.getDefaultSharedPreferences(activity).getInt(
                        PREF_ANIMATION_COLOR_MORPH_DURATION,
                        activity.getResources().getInteger(
                                R.integer.color_morph_duration));

        // Use a value animator to perform synchronous animation with
        // caller's animation. Using an ObjectAnimator here causes the
        // animations to be slightly out of sync (not sure why).
        ValueAnimator animator = ValueAnimator.ofInt(0, 1);
        animator.setStartDelay(0);
        animator.setDuration(colorMorphDuration);

        animator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final float animatedFraction = animation.getAnimatedFraction();

                        // Set StatusBar color on Lollipop
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            int statusBarColor = ColorUtils.blendColors(
                                    startStatusBarColor,
                                    endStatusBarColor,
                                    animatedFraction);
                            ColorUtils.setStatusBarColor(activity.getWindow(), statusBarColor);
                        }

                        // Set the ActionBar color (only in two-pane mode)
                        if (activity.isTaskRoot() && actionBarView != null) {
                            int actionBarColor = ColorUtils.blendColors(
                                    startActionBarColor,
                                    endActionBarColor,
                                    animatedFraction);
                            actionBarView.setBackgroundColor(actionBarColor);
                        }
                    }
                });
        animator.start();
    }

    public static View getActionBarView(final Activity activity) {
        if (activity instanceof IToolbarHolder) {
            return ((IToolbarHolder) activity).getToolbar();
        } else {
            final String packageName =
                    activity instanceof AppCompatActivity ? activity.getPackageName() : "android";

            final int resId = activity.getResources().getIdentifier(
                    "action_bar_container", "id", packageName);

            final View view = activity.findViewById(resId);

            return view;
        }
    }

    public interface IToolbarHolder {
        android.support.v7.widget.Toolbar getToolbar();
    }

    /**
     * Get current status bar color.
     *
     * @return Status bar color if version is LOLLIPOP or greater.
     */
    public static int getStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return activity.getWindow().getStatusBarColor();
        } else {
            return 0;
        }
    }
}
