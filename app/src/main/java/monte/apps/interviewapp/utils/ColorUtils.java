/*
 * Copyright (c) 2015. Monte Creasor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package monte.apps.interviewapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;

/**
 * Created by Monte on 2015-11-23.
 */
public class ColorUtils {
    public static PaletteGenerator generatePalette(
            Bitmap bitmap,
            int maxDimension,
            GeneratePaletteCallback callback) {
        PaletteGenerator paletteGenerator = new PaletteGenerator(callback);
        paletteGenerator.execute(bitmap, maxDimension);
        return paletteGenerator;
    }

    public static PaletteSwatch generateSwatch(Palette palette) {
        // try to get vibrant colors first.
        Palette.Swatch primary = palette.getVibrantSwatch();
        Palette.Swatch secondary = palette.getDarkVibrantSwatch();
        Palette.Swatch tertiary = palette.getLightVibrantSwatch();

        // Set muted colors only if vibrant colors are missing.
        if (primary == null) {
            primary = palette.getMutedSwatch();
        }
        if (secondary == null) {
            secondary = palette.getDarkMutedSwatch();
        }
        if (tertiary == null) {
            tertiary = palette.getLightMutedSwatch();
        }

        // Setup and return colorSwatch to passed callback.
        PaletteSwatch colorSwatch = null;
        if (primary != null && secondary != null) {
            colorSwatch = new PaletteSwatch(
                    primary.getRgb(),
                    secondary.getRgb(),
                    tertiary == null ? 0 : tertiary.getRgb(),
                    primary.getTitleTextColor(),
                    primary.getBodyTextColor());
        }

        return colorSwatch;
    }

    /**
     * Returns lighter/darker version of specified <code>color</code>. Source
     * http://stackoverflow.com/questions/4928772/android-color-darker
     */
    public static int adjustColorBrightnessRGB(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                          Math.max((int) (r * factor), 0),
                          Math.max((int) (g * factor), 0),
                          Math.max((int) (b * factor), 0));
    }

    public static int adjustColorBrightnessHSV(int color, float factor) {
        float hsv[] = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    /**
     * Sets the status bar color for API >= LOLLIPOP
     *
     * @param window
     * @param color
     */
    public static void setStatusBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(color);
        }
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
     *              1.0 will return {@code color2}.
     *              <p>
     *              Source: Android/android-sdk\sources\android-23\android\support\design\widget
     *              \CollapsingTextHelper.java
     */
    public static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * Source: Android/android-sdk\sources\android-23\android\support\design\widget
     * \CollapsingTextHelper.java
     */
    private static float lerp(
            float startValue, float endValue, float fraction,
            Interpolator interpolator) {
        if (interpolator != null) {
            fraction = interpolator.getInterpolation(fraction);
        }

        return lerp(startValue, endValue, fraction);
    }

    /**
     * Source: Android/android-sdk\sources\android-23\android\support\design\widget
     * \AnimationUtils.java
     */
    static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    /**
     * Source: Android/android-sdk\sources\android-23\android\support\design\widget
     * \AnimationUtils.java
     */
    static int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (endValue - startValue));
    }

    static public Drawable getTintedDrawable(
            Context context,
            @DrawableRes int drawableResId,
            @ColorRes int colorResId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        int color = ContextCompat.getColor(context, colorResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    /**
     * Interface that can be used by any class to receive notifications from a sender who generates
     * a new palette. Currently this interface is used by both MainActivity and
     * MovieDetailPagedActivity who receive palette notifications from MovieDetailPagedFragment.
     */
    public interface GeneratePaletteCallback {
        void onPaletteGenerated(Object sender, PaletteSwatch colorSwatch);
    }

    public static class PaletteSwatch implements Parcelable {
        public static final Creator<PaletteSwatch> CREATOR =
                new Creator<PaletteSwatch>() {
                    public PaletteSwatch createFromParcel(Parcel source) {
                        return new PaletteSwatch(source);
                    }

                    public PaletteSwatch[] newArray(int size) {
                        return new PaletteSwatch[size];
                    }
                };
        @ColorInt
        public final int primaryAccent;
        @ColorInt
        public final int secondaryAccent;
        @ColorInt
        public final int tertiaryAccent;
        @ColorInt
        public final int primaryText;
        @ColorInt
        public final int secondaryText;

        public PaletteSwatch(
                int primaryAccent,
                int secondaryAccent,
                int tertiaryAccent,
                int primaryText,
                int secondaryText) {
            this.primaryAccent = primaryAccent;
            this.secondaryAccent = secondaryAccent;
            this.tertiaryAccent = tertiaryAccent;
            this.primaryText = primaryText;
            this.secondaryText = secondaryText;
        }

        protected PaletteSwatch(Parcel in) {
            this.primaryAccent = in.readInt();
            this.secondaryAccent = in.readInt();
            this.tertiaryAccent = in.readInt();
            this.primaryText = in.readInt();
            this.secondaryText = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.primaryAccent);
            dest.writeInt(this.secondaryAccent);
            dest.writeInt(this.tertiaryAccent);
            dest.writeInt(this.primaryText);
            dest.writeInt(this.secondaryText);
        }
    }

    /**
     * Asynchronously get primary, secondary, and tertiary color swatches that best match the passed
     * bitmap. Where possible, vibrant swatches are preferred over muted.
     */
    public static class PaletteGenerator {
        private final WeakReference<GeneratePaletteCallback> mCallbackRef;
        private Palette.PaletteAsyncListener mListener;

        protected PaletteGenerator(GeneratePaletteCallback callback) {
            mCallbackRef = new WeakReference<>(callback);
        }

        /**
         * Terminates the asynchronous palette generation and prevents any pending callback from
         * occurring.
         */
        public void terminate() {
            mListener = null;
            mCallbackRef.clear();
        }

        public void execute(Bitmap bitmap, int maxDimension) {
            Palette.Builder builder = new Palette.Builder(bitmap);
            if (maxDimension > 0) {
                builder.resizeBitmapArea(maxDimension);
            }
            mListener =  new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    // Only make the callback if it hasn't been garbage collected.
                    GeneratePaletteCallback cb = mCallbackRef.get();
                    if (cb != null) {
                        cb.onPaletteGenerated(cb, generateSwatch(palette));
                    }

                    terminate();
                }
            };

            builder.generate(mListener);
        }
    }
}
