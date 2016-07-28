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

package monte.apps.interviewapp.activities;

/**
 * Origin: From one of my assignments in the Coursera cloud computing MOOC.
 */

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.utils.ColorUtils;
import monte.apps.interviewapp.utils.ThemeUtils;

/**
 * This abstract class extends the Activity class and overrides lifecycle callbacks for logging
 * various lifecycle events.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements ColorUtils.GeneratePaletteCallback {

    private static final String TAG = "BaseActivity";

    private Toolbar mToolbar;

    //--------------
    // Construction
    //--------------

    /**
     * Implemented to inject this class into Dagger.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Implemented to automatically install the top level toolbar for derived classes (only if
     * toolbar id is R.id.toolbar).
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    //---------------------------
    // ActionBar support methods
    //---------------------------

    /**
     * Provided for activities that want to install a ActionBar toolbar with an id other than
     * R.id.toolbar. This method does not need to be called if the toolbar is defined with id
     * R.id.toolbar (see onCreate()).
     *
     * @param toolbar
     */
    public void setupActionBar(Toolbar toolbar) {
        setupActionBar(toolbar, 0);
    }

    /**
     * Provided for activities that want to install a ActionBar toolbar with an id other than
     * R.id.toolbar.
     *
     * @param toolbar
     */
    public void setupActionBar(Toolbar toolbar, @LayoutRes int customLayoutId) {
        if (toolbar == null || toolbar == mToolbar) {
            return;
        }

        mToolbar = toolbar;

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (customLayoutId > 0) {
                setupCustomToolbar(customLayoutId);
            }

            if (!isTaskRoot()) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
            }
        }
    }

    /**
     * Helper method to install and remove a custom toolbar layout.
     *
     * @param layoutId Layout resource id; 0 for clearing existing custom layout.
     */
    public void setupCustomToolbar(@LayoutRes int layoutId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (layoutId != 0) {
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setCustomView(layoutId);
            } else {
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setCustomView(null);
            }
        }
    }

    /**
     * Sets the ActionBar title from a string resource.
     *
     * @param resId String resource title
     */
    public void setActionBarTitle(int resId) {
        setActionBarTitle(getString(resId));
    }

    public void setActionBarHomeAsUpIndicator(Drawable drawable) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(drawable);
        }
    }

    public void setActionBarHomeAsUpIndicator(int resId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(resId);
        }
    }

    /**
     * Sets the ActionBar title from a string.
     *
     * @param title Title string.
     */
    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * Sets the ActionBar background color to a ColorDrawable.
     *
     * @param colorDrawable ColorDrawable to use for ActionBar background.
     */
    public void setActionBarColor(ColorDrawable colorDrawable) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }
    }

    /**
     * Sets the ActionBar icon.
     *
     * @param icon BitmapDrawable
     */
    public void setActionBarIcon(Drawable icon) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(icon);
        }
    }

    /**
     * ImageLoaderCallback used to set both the ActionBar and StatusBar colors to match a generated
     * palette. Currently, the MovieDetailPagedFragment class generates a palette to match its
     * contained movie poster image.
     *
     * @param sender  The class instance invoking this callback.
     * @param palette A generated palette.
     */
    @Override
    public void onPaletteGenerated(
            Object sender,
            final ColorUtils.PaletteSwatch palette) {
        //TODO                               ,float expansionFraction) {

        int actionBarColor = R.color.colorPrimary;
        if (mToolbar != null) {
            ColorDrawable colorDrawable = (ColorDrawable) mToolbar.getBackground();
            actionBarColor = colorDrawable != null ?
                             colorDrawable.getColor() :
                             mToolbar.getDrawingCacheBackgroundColor();
        }
        final int startActionBarColor = actionBarColor;

        // Set ending ActionBar color to the new primary accent.
        final int endActionBarColor = palette.primaryAccent;

        // Get starting status bar color based on its current color.
        final int startStatusBarColor = getStatusBarColor();

        // The ending status bar color is always based on a slightly
        // darker shade of the ActionBar ending color.
        final int endStatusBarColor =
                ColorUtils.adjustColorBrightnessHSV(
                        endActionBarColor,
                        getResources().getFraction(
                                R.fraction.status_bar_color_offset, 1, 1));

        // Get duration from shared preferences to stay in sync with
        // other concurrent color morphing animators run by caller.
        final int colorMorphDuration = getSharedPreferences().getInt(
                ThemeUtils.PREF_ANIMATION_COLOR_MORPH_DURATION,
                getResources().getInteger(
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
                        final float animatedFraction =
                                animation.getAnimatedFraction();

                        // Set StatusBar color on Lollipop
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            int statusBarColor = ColorUtils.blendColors(
                                    startStatusBarColor,
                                    endStatusBarColor,
                                    animatedFraction);
                            ColorUtils.setStatusBarColor(getWindow(),
                                                         statusBarColor);
                        }

                        // Set the ActionBar color (only in two-pane mode)
                        if (isTaskRoot() && mToolbar != null) {
                            int actionBarColor1 = ColorUtils.blendColors(
                                    startActionBarColor,
                                    endActionBarColor,
                                    animatedFraction);
                            mToolbar.setBackgroundColor(actionBarColor1);
                        }
                    }
                });
        animator.start();
    }

    /**
     * Should be called by base class once the activity's main image has been loaded. This will
     * trigger a background palette generation that will eventually morph the action and
     * status bar
     * colors to match the image palette.
     *
     * @param bitmap Image to extract a matching theme palette.
     */

    public void updatePalette(Bitmap bitmap) {

    }

    /**
     * Get current status bar color.
     *
     * @return Status bar color if version is LOLLIPOP or greater.
     */
    public int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getWindow().getStatusBarColor();
        } else {
            return 0;
        }
    }

    //--------------------------
    // API M Permission Helpers
    // TODO: 2016-07-21
    //--------------------------

    public SharedPreferences getSharedPreferences() {
        // TODO: 2016-07-21 fix the name here...
        return super.getSharedPreferences("app", MODE_PRIVATE);
    }
}
