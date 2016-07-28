package monte.apps.interviewapp.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import monte.apps.interviewapp.R;

/**
 * Display entries in a LinearLayout that can be expanded to show all entries.
 */
public class ExpandingEntryCardView extends CardView {

    public static final int DURATION_EXPAND_ANIMATION_CHANGE_BOUNDS = 300;
    public static final int DURATION_COLLAPSE_ANIMATION_CHANGE_BOUNDS = 300;
    private static final String TAG = "ExpandingEntryCardView";
    private static final int DURATION_EXPAND_ANIMATION_FADE_IN = 200;
    private static final int DURATION_COLLAPSE_ANIMATION_FADE_OUT = 75;
    private static final int DELAY_EXPAND_ANIMATION_FADE_IN = 100;
    private static final Property<View, Integer> VIEW_LAYOUT_HEIGHT_PROPERTY =
            new Property<View, Integer>(Integer.class, "height") {
                @Override
                public void set(View view, Integer height) {
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams)
                                    view.getLayoutParams();
                    params.height = height;
                    view.setLayoutParams(params);
                }

                @Override
                public Integer get(View view) {
                    return view.getLayoutParams().height;
                }
            };
    private final ImageView mExpandCollapseArrow;
    private final List<ImageView> mBadges;
    private final List<Integer> mBadgeIds;
    private final int mDividerLineHeightPixels;
    private View mExpandCollapseButton;
    private TextView mExpandCollapseTextView;
    private TextView mTitleTextView;
    private CharSequence mExpandButtonText;
    private CharSequence mCollapseButtonText;
    private OnClickListener mOnClickListener;
    private OnCreateContextMenuListener mOnCreateContextMenuListener;
    private boolean mIsExpanded = false;
    /**
     * The max number of entries to show in a collapsed card. If there are less entries passed in,
     * then they are all shown.
     */
    private int mCollapsedEntriesCount;
    private ExpandingEntryCardViewListener mListener;
    private List<List<Entry>> mEntries;
    private int mNumEntries = 0;
    private boolean mAllEntriesInflated = false;
    private List<List<View>> mEntryViews;
    private LinearLayout mEntriesViewGroup;
    private int mThemeColor;
    private ColorFilter mThemeColorFilter;
    /**
     * Whether to prioritize the first entry type. If prioritized, we should show at least two of
     * this entry type.
     */
    private boolean mShowFirstEntryTypeTwice;
    private boolean mIsAlwaysExpanded;
    /**
     * The ViewGroup to run the expand/collapse animation on
     */
    private ViewGroup mAnimationViewGroup;
    private LinearLayout mBadgeContainer;
    /**
     * List to hold the separators. This saves us from reconstructing every expand/collapse and
     * provides a smoother animation.
     */
    private List<View> mSeparators;
    private LinearLayout mContainer;
    private final OnClickListener mExpandCollapseButtonListener =
            new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsExpanded) {
                        collapse();
                    } else {
                        expand();
                    }
                }
            };

    public ExpandingEntryCardView(Context context) {
        this(context, null);
    }

    public ExpandingEntryCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View expandingEntryCardView =
                inflater.inflate(R.layout.expanding_entry_card_view, this);
        mEntriesViewGroup = (LinearLayout)
                expandingEntryCardView.findViewById(
                        R.id.content_area_linear_layout);
        mTitleTextView =
                (TextView) expandingEntryCardView.findViewById(R.id.title);
        mContainer = (LinearLayout) expandingEntryCardView.findViewById(
                R.id.container);

        mExpandCollapseButton = inflater.inflate(
                R.layout.expanding_entry_card_button, this, false);
        mExpandCollapseTextView =
                (TextView) mExpandCollapseButton.findViewById(R.id.text);
        mExpandCollapseArrow =
                (ImageView) mExpandCollapseButton.findViewById(R.id.arrow);
        mExpandCollapseButton.setOnClickListener(mExpandCollapseButtonListener);
        mBadgeContainer = (LinearLayout) mExpandCollapseButton.findViewById(
                R.id.badge_container);
        mDividerLineHeightPixels = getResources()
                .getDimensionPixelSize(R.dimen.divider_line_height);

        mBadges = new ArrayList<>();
        mBadgeIds = new ArrayList<>();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void initialize(
            List<List<Entry>> entries,
            int numInitialVisibleEntries,
            boolean isExpanded,
            boolean isAlwaysExpanded,
            ExpandingEntryCardViewListener listener,
            ViewGroup animationViewGroup) {
        initialize(entries, numInitialVisibleEntries, isExpanded,
                   isAlwaysExpanded,
                   listener, animationViewGroup, /* showFirstEntryTypeTwice = */
                   false);
    }

    /**
     * Sets the Entry list to display.
     *
     * @param entries The Entry list to display.
     */
    public void initialize(
            List<List<Entry>> entries,
            int numInitialVisibleEntries,
            boolean isExpanded,
            boolean isAlwaysExpanded,
            ExpandingEntryCardViewListener listener,
            ViewGroup animationViewGroup,
            boolean showFirstEntryTypeTwice) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        mIsExpanded = isExpanded;
        mIsAlwaysExpanded = isAlwaysExpanded;
        // If isAlwaysExpanded is true, mIsExpanded should be true
        mIsExpanded |= mIsAlwaysExpanded;
        mEntryViews = new ArrayList<>(entries.size());
        mEntries = entries;
        mNumEntries = 0;
        mAllEntriesInflated = false;
        mShowFirstEntryTypeTwice = showFirstEntryTypeTwice;
        for (List<Entry> entryList : mEntries) {
            mNumEntries += entryList.size();
            mEntryViews.add(new ArrayList<View>());
        }
        mCollapsedEntriesCount =
                Math.min(numInitialVisibleEntries, mNumEntries);
        // We need a separator between each list, but not after the last one
        if (entries.size() > 1) {
            mSeparators = new ArrayList<>(entries.size() - 1);
        }
        mListener = listener;
        mAnimationViewGroup = animationViewGroup;

        if (mIsExpanded) {
            updateExpandCollapseButton(getCollapseButtonText(), /* duration = */
                                       0);
            inflateAllEntries(layoutInflater);
        } else {
            updateExpandCollapseButton(getExpandButtonText(), /* duration = */
                                       0);
            inflateInitialEntries(layoutInflater);
        }
        insertEntriesIntoViewGroup();
        applyColor();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public void setOnCreateContextMenuListener(
            OnCreateContextMenuListener
                    listener) {
        mOnCreateContextMenuListener = listener;
    }

    private List<View> calculateEntriesToRemoveDuringCollapse() {
        final List<View> viewsToRemove = getViewsToDisplay(true);
        final List<View> viewsCollapsed = getViewsToDisplay(false);
        viewsToRemove.removeAll(viewsCollapsed);
        return viewsToRemove;
    }

    private void insertEntriesIntoViewGroup() {
        mEntriesViewGroup.removeAllViews();

        for (View view : getViewsToDisplay(mIsExpanded)) {
            mEntriesViewGroup.addView(view);
        }

        removeView(mExpandCollapseButton);
        if (mCollapsedEntriesCount < mNumEntries
                && mExpandCollapseButton.getParent() == null
                && !mIsAlwaysExpanded) {
            mContainer.addView(mExpandCollapseButton, -1);
        }
    }

    /**
     * Returns the list of views that should be displayed. This changes depending on whether the
     * card is expanded or collapsed.
     */
    private List<View> getViewsToDisplay(boolean isExpanded) {
        final List<View> viewsToDisplay = new ArrayList<>();
        if (isExpanded) {
            for (int i = 0; i < mEntryViews.size(); i++) {
                List<View> viewList = mEntryViews.get(i);
                if (i > 0) {
                    View separator;
                    if (mSeparators.size() <= i - 1) {
                        separator = generateSeparator(viewList.get(0));
                        mSeparators.add(separator);
                    } else {
                        separator = mSeparators.get(i - 1);
                    }
                    viewsToDisplay.add(separator);
                }
                viewsToDisplay.addAll(viewList);
            }
        } else {
            // We want to insert mCollapsedEntriesCount entries into the
            // group. extraEntries is the
            // number of entries that need to be added that are not the head
            // element of a list
            // to reach mCollapsedEntriesCount.
            int numInViewGroup = 0;
            int extraEntries = mCollapsedEntriesCount - mEntryViews.size();
            for (int i = 0;
                 i < mEntryViews.size()
                         && numInViewGroup < mCollapsedEntriesCount;
                 i++) {
                List<View> entryViewList = mEntryViews.get(i);
                if (i > 0) {
                    View separator;
                    if (mSeparators.size() <= i - 1) {
                        separator = generateSeparator(entryViewList.get(0));
                        mSeparators.add(separator);
                    } else {
                        separator = mSeparators.get(i - 1);
                    }
                    viewsToDisplay.add(separator);
                }
                viewsToDisplay.add(entryViewList.get(0));
                numInViewGroup++;

                int indexInEntryViewList = 1;
                if (mShowFirstEntryTypeTwice && i == 0
                        && entryViewList.size() > 1) {
                    viewsToDisplay.add(entryViewList.get(1));
                    numInViewGroup++;
                    extraEntries--;
                    indexInEntryViewList++;
                }

                // Insert entries in this list to hit mCollapsedEntriesCount.
                for (int j = indexInEntryViewList;
                     j < entryViewList.size()
                             && numInViewGroup < mCollapsedEntriesCount &&
                             extraEntries > 0;
                     j++) {
                    viewsToDisplay.add(entryViewList.get(j));
                    numInViewGroup++;
                    extraEntries--;
                }
            }
        }

        formatEntryIfFirst(viewsToDisplay);
        return viewsToDisplay;
    }

    private void formatEntryIfFirst(List<View> entriesViewGroup) {
        // If no title and the first entry in the group, add extra padding
        if (TextUtils.isEmpty(mTitleTextView.getText()) &&
                entriesViewGroup.size() > 0) {
            final View entry = entriesViewGroup.get(0);
            entry.setPadding(entry.getPaddingLeft(),
                             getResources().getDimensionPixelSize(
                                     R.dimen.expanding_entry_card_item_padding_top)
                                     +
                                     getResources().getDimensionPixelSize(
                                             R.dimen.expanding_entry_card_null_title_top_extra_padding),
                             entry.getPaddingRight(),
                             entry.getPaddingBottom());
        }
    }

    private View generateSeparator(View entry) {
        View separator = new View(getContext());
        Resources res = getResources();

        separator.setBackgroundColor(res.getColor(
                R.color.divider_line_color_light));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mDividerLineHeightPixels);
        // The separator is aligned with the text in the entry. This is
        // offset by a default
        // margin. If there is an icon present, the icon's width and margin
        // are added
        int marginStart = res.getDimensionPixelSize(
                R.dimen.expanding_entry_card_item_padding_start);
        ImageView entryIcon = (ImageView) entry.findViewById(R.id.icon);
        if (entryIcon.getVisibility() == View.VISIBLE) {
            int imageWidthAndMargin =
                    res.getDimensionPixelSize(
                            R.dimen.expanding_entry_card_item_icon_width) +
                            res.getDimensionPixelSize(
                                    R.dimen.expanding_entry_card_item_image_spacing);
            marginStart += imageWidthAndMargin;
        }
        layoutParams.setMarginStart(marginStart);
        separator.setLayoutParams(layoutParams);
        return separator;
    }

    private CharSequence getExpandButtonText() {
        if (!TextUtils.isEmpty(mExpandButtonText)) {
            return mExpandButtonText;
        } else {
            // Default to "See more".
            return getResources().getText(
                    R.string.expanding_entry_card_view_see_more);
        }
    }

    /**
     * Sets the text for the expand button.
     *
     * @param expandButtonText The expand button text.
     */
    public void setExpandButtonText(CharSequence expandButtonText) {
        mExpandButtonText = expandButtonText;
        if (mExpandCollapseTextView != null && !mIsExpanded) {
            mExpandCollapseTextView.setText(expandButtonText);
        }
    }

    private CharSequence getCollapseButtonText() {
        if (!TextUtils.isEmpty(mCollapseButtonText)) {
            return mCollapseButtonText;
        } else {
            // Default to "See less".
            return getResources().getText(
                    R.string.expanding_entry_card_view_see_less);
        }
    }

    /**
     * Sets the text for the expand button.
     *
     * @param expandButtonText The expand button text.
     */
    public void setCollapseButtonText(CharSequence expandButtonText) {
        mCollapseButtonText = expandButtonText;
        if (mExpandCollapseTextView != null && mIsExpanded) {
            mExpandCollapseTextView.setText(mCollapseButtonText);
        }
    }

    /**
     * Inflates the initial entries to be shown.
     */
    private void inflateInitialEntries(LayoutInflater layoutInflater) {
        // If the number of collapsed entries equals total entries, inflate all
        if (mCollapsedEntriesCount == mNumEntries) {
            inflateAllEntries(layoutInflater);
        } else {
            // Otherwise inflate the top entry from each list
            // extraEntries is used to add extra entries until
            // mCollapsedEntriesCount is reached.
            int numInflated = 0;
            int extraEntries = mCollapsedEntriesCount - mEntries.size();
            for (int i = 0;
                 i < mEntries.size() && numInflated < mCollapsedEntriesCount;
                 i++) {
                List<Entry> entryList = mEntries.get(i);
                List<View> entryViewList = mEntryViews.get(i);

                entryViewList.add(
                        createEntryView(layoutInflater, entryList.get(0),
                        /* showIcon = */ View.VISIBLE));
                numInflated++;

                int indexInEntryViewList = 1;
                if (mShowFirstEntryTypeTwice && i == 0
                        && entryList.size() > 1) {
                    entryViewList.add(
                            createEntryView(layoutInflater, entryList.get(1),
                        /* showIcon = */ View.INVISIBLE));
                    numInflated++;
                    extraEntries--;
                    indexInEntryViewList++;
                }

                // Inflate entries in this list to hit mCollapsedEntriesCount.
                for (int j = indexInEntryViewList; j < entryList.size()
                        && numInflated < mCollapsedEntriesCount
                        && extraEntries > 0; j++) {
                    entryViewList.add(
                            createEntryView(layoutInflater, entryList.get(j),
                            /* showIcon = */ View.INVISIBLE));
                    numInflated++;
                    extraEntries--;
                }
            }
        }
    }

    /**
     * Inflates all entries.
     */
    private void inflateAllEntries(LayoutInflater layoutInflater) {
        if (mAllEntriesInflated) {
            return;
        }
        for (int i = 0; i < mEntries.size(); i++) {
            List<Entry> entryList = mEntries.get(i);
            List<View> viewList = mEntryViews.get(i);
            for (int j = viewList.size(); j < entryList.size(); j++) {
                final int iconVisibility;
                final Entry entry = entryList.get(j);
                // If the entry does not have an icon, mark gone. Else if it
                // has an icon, show
                // for the first Entry in the list only
                if (entry.getIcon() == null && entry.getIconUri() == null) {
                    iconVisibility = View.GONE;
                } else if (j == 0) {
                    iconVisibility = View.VISIBLE;
                } else {
                    iconVisibility = View.INVISIBLE;
                }
                viewList.add(
                        createEntryView(layoutInflater, entry, iconVisibility));
            }
        }
        mAllEntriesInflated = true;
    }

    public void setColorAndFilter(int color, ColorFilter colorFilter) {
        mThemeColor = color;
        mThemeColorFilter = colorFilter;
        applyColor();
    }

    public void setEntryHeaderColor(int color) {
        if (mEntries != null) {
            for (List<View> entryList : mEntryViews) {
                for (View entryView : entryList) {
                    TextView header =
                            (TextView) entryView.findViewById(R.id.header);
                    if (header != null) {
                        header.setTextColor(color);
                    }
                }
            }
        }
    }

    /**
     * The ColorFilter is passed in along with the color so that a new one only needs to be created
     * once for the entire activity. 1. Title 2. Entry icons 3. Expand/Collapse Text 4.
     * Expand/Collapse Button
     */
    public void applyColor() {
        if (mThemeColor != 0 && mThemeColorFilter != null) {
            // Title
            if (mTitleTextView != null) {
                mTitleTextView.setTextColor(mThemeColor);
            }

            // Entry icons
            if (mEntries != null) {
                for (List<Entry> entryList : mEntries) {
                    for (Entry entry : entryList) {
                        if (entry.shouldApplyColor()) {
                            Drawable icon = entry.getIcon();
                            if (icon != null) {
                                icon.mutate();
                                icon.setColorFilter(mThemeColorFilter);
                            }
                        }
                        Drawable alternateIcon = entry.getAlternateIcon();
                        if (alternateIcon != null) {
                            alternateIcon.mutate();
                            alternateIcon.setColorFilter(mThemeColorFilter);
                        }
                        Drawable thirdIcon = entry.getThirdIcon();
                        if (thirdIcon != null) {
                            thirdIcon.mutate();
                            thirdIcon.setColorFilter(mThemeColorFilter);
                        }
                    }
                }
            }

            // Expand/Collapse
            mExpandCollapseTextView.setTextColor(mThemeColor);
            mExpandCollapseArrow.setColorFilter(mThemeColorFilter);
        }
    }

    private View createEntryView(
            LayoutInflater layoutInflater, final Entry entry,
            int iconVisibility) {
        final EntryView view = (EntryView) layoutInflater.inflate(
                R.layout.expanding_entry_card_item, this, false);

        view.setContextMenuInfo(entry.getEntryContextMenuInfo());
        if (!TextUtils.isEmpty(entry.getPrimaryContentDescription())) {
            view.setContentDescription(entry.getPrimaryContentDescription());
        }

        final ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setVisibility(iconVisibility);
        if (entry.getIcon() != null) {
            icon.setImageDrawable(entry.getIcon());
        } else if (entry.getIconUri() != null) {
            RequestCreator creator = Picasso.with(getContext()).load(entry.getIconUri());
            if (entry.getTransformation() != null) {
                creator.transform(entry.getTransformation());
            }
            creator.into(icon);
        }

        final TextView header = (TextView) view.findViewById(R.id.header);
        if (!TextUtils.isEmpty(entry.getHeader())) {
            header.setText(entry.getHeader());
        } else {
            header.setVisibility(View.GONE);
        }

        final TextView subHeader =
                (TextView) view.findViewById(R.id.sub_header);
        if (!TextUtils.isEmpty(entry.getSubHeader())) {
            subHeader.setText(entry.getSubHeader());
        } else {
            subHeader.setVisibility(View.GONE);
        }

        final ImageView subHeaderIcon =
                (ImageView) view.findViewById(R.id.icon_sub_header);
        if (entry.getSubHeaderIcon() != null) {
            subHeaderIcon.setImageDrawable(entry.getSubHeaderIcon());
        } else {
            subHeaderIcon.setVisibility(View.GONE);
        }

        final TextView text = (TextView) view.findViewById(R.id.text);
        if (!TextUtils.isEmpty(entry.getText())) {
            text.setText(entry.getText());
        } else {
            text.setVisibility(View.GONE);
        }

        final ImageView textIcon =
                (ImageView) view.findViewById(R.id.icon_text);
        if (entry.getTextIcon() != null) {
            textIcon.setImageDrawable(entry.getTextIcon());
        } else {
            textIcon.setVisibility(View.GONE);
        }

        if (entry.getIntent() != null) {
            view.setOnClickListener(mOnClickListener);
            view.setTag(new EntryTag(entry.getId(), entry.getIntent()));
        }

        if (entry.getIntent() == null
                && entry.getEntryContextMenuInfo() == null) {
            // Remove the click effect
            view.setBackground(null);
        }

        // If only the header is visible, add a top margin to match icon's
        // top margin.
        // Also increase the space below the header for visual comfort.
        if (header.getVisibility() == View.VISIBLE
                && subHeader.getVisibility() == View.GONE &&
                text.getVisibility() == View.GONE) {
            RelativeLayout.LayoutParams headerLayoutParams =
                    (RelativeLayout.LayoutParams) header.getLayoutParams();
            headerLayoutParams.topMargin = (int) (getResources().getDimension(
                    R.dimen.expanding_entry_card_item_header_only_margin_top));
            headerLayoutParams.bottomMargin +=
                    (int) (getResources().getDimension(
                            R.dimen.expanding_entry_card_item_header_only_margin_bottom));
            header.setLayoutParams(headerLayoutParams);
        }

        // Adjust the top padding size for entries with an invisible icon.
        // The padding depends on
        // if there is a sub header or text section
        if (iconVisibility == View.INVISIBLE &&
                (!TextUtils.isEmpty(entry.getSubHeader()) || !TextUtils.isEmpty(
                        entry.getText()))) {
            view.setPaddingRelative(view.getPaddingStart(),
                                    getResources().getDimensionPixelSize(
                                            R.dimen.expanding_entry_card_item_no_icon_margin_top),
                                    view.getPaddingEnd(),
                                    view.getPaddingBottom());
        } else if (iconVisibility == View.INVISIBLE && TextUtils.isEmpty(
                entry.getSubHeader())
                && TextUtils.isEmpty(entry.getText())) {
            view.setPaddingRelative(view.getPaddingStart(), 0,
                                    view.getPaddingEnd(),
                                    view.getPaddingBottom());
        }

        final ImageView alternateIcon =
                (ImageView) view.findViewById(R.id.icon_alternate);
        final ImageView thirdIcon =
                (ImageView) view.findViewById(R.id.third_icon);

        if (entry.getAlternateIcon() != null
                && entry.getAlternateIntent() != null) {
            alternateIcon.setImageDrawable(entry.getAlternateIcon());
            alternateIcon.setOnClickListener(mOnClickListener);
            alternateIcon.setTag(
                    new EntryTag(entry.getId(), entry.getAlternateIntent()));
            alternateIcon.setVisibility(View.VISIBLE);
            alternateIcon.setContentDescription(
                    entry.getAlternateContentDescription());
        }

        if (entry.getThirdIcon() != null
                && entry.getThirdAction() != Entry.ACTION_NONE) {
            thirdIcon.setImageDrawable(entry.getThirdIcon());
            if (entry.getThirdAction() == Entry.ACTION_INTENT) {
                thirdIcon.setOnClickListener(mOnClickListener);
                thirdIcon.setTag(
                        new EntryTag(entry.getId(), entry.getThirdIntent()));
            } else if (entry.getThirdAction() == Entry.ACTION_CALL_WITH_SUBJECT) {
                thirdIcon.setOnClickListener(mOnClickListener);
                thirdIcon.setTag(entry.getThirdExtras());
            }
            thirdIcon.setVisibility(View.VISIBLE);
            thirdIcon.setContentDescription(entry.getThirdContentDescription());
        }

        // Set a custom touch listener for expanding the extra icon touch areas
        view.setOnTouchListener(
                new EntryTouchListener(view, alternateIcon, thirdIcon));
        view.setOnCreateContextMenuListener(mOnCreateContextMenuListener);

        return view;
    }

    private void updateExpandCollapseButton(
            CharSequence buttonText,
            long duration) {
        if (mIsExpanded) {
            final ObjectAnimator animator =
                    ObjectAnimator.ofFloat(mExpandCollapseArrow,
                                           "rotation", 180);
            animator.setDuration(duration);
            animator.start();
        } else {
            final ObjectAnimator animator =
                    ObjectAnimator.ofFloat(mExpandCollapseArrow,
                                           "rotation", 0);
            animator.setDuration(duration);
            animator.start();
        }
        updateBadges();

        mExpandCollapseTextView.setText(buttonText);
    }

    private void updateBadges() {
        if (mIsExpanded) {
            mBadgeContainer.removeAllViews();
        } else {
            int numberOfMimeTypesShown = mCollapsedEntriesCount;
            if (mShowFirstEntryTypeTwice && mEntries.size() > 0
                    && mEntries.get(0).size() > 1) {
                numberOfMimeTypesShown--;
            }
            // Inflate badges if not yet created
            if (mBadges.size() < mEntries.size() - numberOfMimeTypesShown) {
                for (int i = numberOfMimeTypesShown; i < mEntries.size(); i++) {
                    Drawable badgeDrawable = mEntries.get(i).get(0).getIcon();
                    int badgeResourceId =
                            mEntries.get(i).get(0).getIconResourceId();
                    // Do not add the same badge twice
                    if (badgeResourceId != 0 && mBadgeIds.contains(
                            badgeResourceId)) {
                        continue;
                    }
                    if (badgeDrawable != null) {
                        ImageView badgeView = new ImageView(getContext());
                        LinearLayout.LayoutParams badgeViewParams =
                                new LinearLayout.LayoutParams(
                                        (int) getResources().getDimension(
                                                R.dimen.expanding_entry_card_item_icon_width),
                                        (int) getResources().getDimension(
                                                R.dimen.expanding_entry_card_item_icon_height));
                        badgeViewParams.setMarginEnd(
                                (int) getResources().getDimension(
                                        R.dimen.expanding_entry_card_badge_separator_margin));
                        badgeView.setLayoutParams(badgeViewParams);
                        badgeView.setImageDrawable(badgeDrawable);
                        mBadges.add(badgeView);
                        mBadgeIds.add(badgeResourceId);
                    }
                }
            }
            mBadgeContainer.removeAllViews();
            for (ImageView badge : mBadges) {
                mBadgeContainer.addView(badge);
            }
        }
    }

    private void expand() {
        ChangeBounds boundsTransition = new ChangeBounds();
        boundsTransition.setDuration(DURATION_EXPAND_ANIMATION_CHANGE_BOUNDS);

        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.setDuration(DURATION_EXPAND_ANIMATION_FADE_IN);
        fadeIn.setStartDelay(DELAY_EXPAND_ANIMATION_FADE_IN);

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(boundsTransition);
        transitionSet.addTransition(fadeIn);

        transitionSet.excludeTarget(R.id.text, /* exclude = */ true);

        final ViewGroup transitionViewContainer = mAnimationViewGroup == null ?
                                                  this : mAnimationViewGroup;

        transitionSet.addListener(new TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mListener.onExpand();
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mListener.onExpandDone();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(transitionViewContainer,
                                                 transitionSet);

        mIsExpanded = true;
        // In order to insert new entries, we may need to inflate them for
        // the first time
        inflateAllEntries(LayoutInflater.from(getContext()));
        insertEntriesIntoViewGroup();
        updateExpandCollapseButton(getCollapseButtonText(),
                                   DURATION_EXPAND_ANIMATION_CHANGE_BOUNDS);
    }

    private void collapse() {
        final List<View> views = calculateEntriesToRemoveDuringCollapse();

        // This animation requires layout changes, unlike the expand()
        // animation: the action bar
        // might get scrolled open in order to fill empty space. As a result,
        // we can't use
        // ChangeBounds here. Instead manually animate view height and alpha.
        // This isn't as
        // efficient as the bounds and translation changes performed by
        // ChangeBounds. Nonetheless, a
        // reasonable frame-rate is achieved collapsing a dozen elements on a
        // user Svelte N4. So the
        // performance hit doesn't justify writing a less maintainable
        // animation.
        final AnimatorSet set = new AnimatorSet();
        final List<Animator> animators = new ArrayList<>(views.size());
        int totalSizeChange = 0;
        for (View viewToRemove : views) {
            final ObjectAnimator animator =
                    ObjectAnimator.ofObject(viewToRemove,
                                            VIEW_LAYOUT_HEIGHT_PROPERTY, null,
                                            viewToRemove.getHeight(), 0);
            totalSizeChange += viewToRemove.getHeight();
            animator.setDuration(DURATION_COLLAPSE_ANIMATION_CHANGE_BOUNDS);
            animators.add(animator);
            viewToRemove.animate()
                    .alpha(0)
                    .setDuration(DURATION_COLLAPSE_ANIMATION_FADE_OUT);
        }
        set.playTogether(animators);
        set.start();
        set.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Now that the views have been animated away, actually
                // remove them from the view
                // hierarchy. Reset their appearance so that they look
                // appropriate when they
                // get added back later.
                insertEntriesIntoViewGroup();
                for (View view : views) {
                    if (view instanceof EntryView) {
                        VIEW_LAYOUT_HEIGHT_PROPERTY.set(view,
                                                        LayoutParams
                                                                .WRAP_CONTENT);
                    } else {
                        VIEW_LAYOUT_HEIGHT_PROPERTY.set(view,
                                                        mDividerLineHeightPixels);
                    }
                    view.animate().cancel();
                    view.setAlpha(1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mListener.onCollapse(totalSizeChange);
        mIsExpanded = false;
        updateExpandCollapseButton(getExpandButtonText(),
                                   DURATION_COLLAPSE_ANIMATION_CHANGE_BOUNDS);
    }

    /**
     * Returns whether the view is currently in its expanded state.
     */
    public boolean isExpanded() {
        return mIsExpanded;
    }

    /**
     * Sets the title text of this ExpandingEntryCardView.
     *
     * @param title The title to set. A null title will result in the title being removed.
     */
    public void setTitle(String title) {
        if (mTitleTextView == null) {
            Log.e(TAG, "mTitleTextView is null");
        }
        mTitleTextView.setText(title);
        mTitleTextView.setVisibility(
                TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        findViewById(R.id.title_separator).setVisibility(
                TextUtils.isEmpty(title) ?
                View.GONE : View.VISIBLE);
        // If the title is set after children have been added, reset the top
        // entry's padding to
        // the default. Else if the title is cleared after children have been
        // added, set
        // the extra top padding
        if (!TextUtils.isEmpty(title)
                && mEntriesViewGroup.getChildCount() > 0) {
            View firstEntry = mEntriesViewGroup.getChildAt(0);
            firstEntry.setPadding(firstEntry.getPaddingLeft(),
                                  getResources().getDimensionPixelSize(
                                          R.dimen.expanding_entry_card_item_padding_top),
                                  firstEntry.getPaddingRight(),
                                  firstEntry.getPaddingBottom());
        } else if (!TextUtils.isEmpty(title)
                && mEntriesViewGroup.getChildCount() > 0) {
            View firstEntry = mEntriesViewGroup.getChildAt(0);
            firstEntry.setPadding(firstEntry.getPaddingLeft(),
                                  getResources().getDimensionPixelSize(
                                          R.dimen.expanding_entry_card_item_padding_top)
                                          +
                                          getResources().getDimensionPixelSize(
                                                  R.dimen.expanding_entry_card_null_title_top_extra_padding),
                                  firstEntry.getPaddingRight(),
                                  firstEntry.getPaddingBottom());
        }
    }

    public boolean shouldShow() {
        return mEntries != null && mEntries.size() > 0;
    }

    public interface ExpandingEntryCardViewListener {
        void onCollapse(int heightDelta);

        void onExpand();

        void onExpandDone();
    }

    /**
     * Entry data.
     */
    public static final class Entry {
        // No action when clicking a button is specified.
        public static final int ACTION_NONE = 1;
        // Button action is an intent.
        public static final int ACTION_INTENT = 2;
        // Button action will open the call with subject dialog.
        public static final int ACTION_CALL_WITH_SUBJECT = 3;

        private final int mId;
        private final Drawable mIcon;
        private final String mHeader;
        private final String mSubHeader;
        private final Drawable mSubHeaderIcon;
        private final String mText;
        private final Drawable mTextIcon;
        private final Intent mIntent;
        private final Drawable mAlternateIcon;
        private final Intent mAlternateIntent;
        private final String mAlternateContentDescription;
        private final boolean mShouldApplyColor;
        private final boolean mIsEditable;
        private final EntryContextMenuInfo mEntryContextMenuInfo;
        private final Drawable mThirdIcon;
        private final Intent mThirdIntent;
        private final String mThirdContentDescription;
        private final int mIconResourceId;
        private final int mThirdAction;
        private final Bundle mThirdExtras;
        private final Uri mIconUri;
        private Spannable mPrimaryContentDescription;
        private Transformation mTransformation;

        public Entry(
                int id,
                Drawable mainIcon,
                String header,
                String subHeader,
                Drawable subHeaderIcon,
                String text,
                Drawable textIcon,
                Spannable primaryContentDescription,
                Intent intent,
                Drawable alternateIcon,
                Intent alternateIntent,
                String alternateContentDescription,
                boolean shouldApplyColor,
                boolean isEditable,
                EntryContextMenuInfo entryContextMenuInfo,
                Drawable thirdIcon,
                Intent thirdIntent,
                String thirdContentDescription,
                int thirdAction,
                Bundle thirdExtras,
                int iconResourceId,
                Uri iconUri,
                Transformation transformation) {
            mId = id;
            mIcon = mainIcon;
            mHeader = header;
            mSubHeader = subHeader;
            mSubHeaderIcon = subHeaderIcon;
            mText = text;
            mTextIcon = textIcon;
            mPrimaryContentDescription = primaryContentDescription;
            mIntent = intent;
            mAlternateIcon = alternateIcon;
            mAlternateIntent = alternateIntent;
            mAlternateContentDescription = alternateContentDescription;
            mShouldApplyColor = shouldApplyColor;
            mIsEditable = isEditable;
            mEntryContextMenuInfo = entryContextMenuInfo;
            mThirdIcon = thirdIcon;
            mThirdIntent = thirdIntent;
            mThirdContentDescription = thirdContentDescription;
            mThirdAction = thirdAction;
            mThirdExtras = thirdExtras;
            mIconResourceId = iconResourceId;
            mIconUri = iconUri;
            mTransformation = transformation;
        }

        private Entry(Builder builder) {
            mId = builder.mId;
            mIcon = builder.mIcon;
            mHeader = builder.mHeader;
            mSubHeader = builder.mSubHeader;
            mSubHeaderIcon = builder.mSubHeaderIcon;
            mText = builder.mText;
            mTextIcon = builder.mTextIcon;
            mPrimaryContentDescription = builder.mPrimaryContentDescription;
            mIntent = builder.mIntent;
            mAlternateIcon = builder.mAlternateIcon;
            mAlternateIntent = builder.mAlternateIntent;
            mAlternateContentDescription = builder.mAlternateContentDescription;
            mShouldApplyColor = builder.mShouldApplyColor;
            mIsEditable = builder.mIsEditable;
            mEntryContextMenuInfo = builder.mEntryContextMenuInfo;
            mThirdIcon = builder.mThirdIcon;
            mThirdIntent = builder.mThirdIntent;
            mThirdContentDescription = builder.mThirdContentDescription;
            mThirdAction = builder.mThirdAction;
            mThirdExtras = builder.mThirdExtras;
            mIconResourceId = builder.mIconResourceId;
            mIconUri = builder.mIconUri;
            mTransformation = builder.mTransformation;
        }

        Drawable getIcon() {
            return mIcon;
        }

        Uri getIconUri() {
            return mIconUri;
        }

        String getHeader() {
            return mHeader;
        }

        String getSubHeader() {
            return mSubHeader;
        }

        Drawable getSubHeaderIcon() {
            return mSubHeaderIcon;
        }

        public String getText() {
            return mText;
        }

        Drawable getTextIcon() {
            return mTextIcon;
        }

        Spannable getPrimaryContentDescription() {
            return mPrimaryContentDescription;
        }

        Intent getIntent() {
            return mIntent;
        }

        Drawable getAlternateIcon() {
            return mAlternateIcon;
        }

        Intent getAlternateIntent() {
            return mAlternateIntent;
        }

        String getAlternateContentDescription() {
            return mAlternateContentDescription;
        }

        boolean shouldApplyColor() {
            return mShouldApplyColor;
        }

        boolean isEditable() {
            return mIsEditable;
        }

        int getId() {
            return mId;
        }

        EntryContextMenuInfo getEntryContextMenuInfo() {
            return mEntryContextMenuInfo;
        }

        Drawable getThirdIcon() {
            return mThirdIcon;
        }

        Intent getThirdIntent() {
            return mThirdIntent;
        }

        String getThirdContentDescription() {
            return mThirdContentDescription;
        }

        int getIconResourceId() {
            return mIconResourceId;
        }

        public int getThirdAction() {
            return mThirdAction;
        }

        public Bundle getThirdExtras() {
            return mThirdExtras;
        }

        public Transformation getTransformation() {
            return mTransformation;
        }
    }

    public static final class EntryView extends RelativeLayout {
        private EntryContextMenuInfo mEntryContextMenuInfo;

        public EntryView(Context context) {
            super(context);
        }

        public EntryView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected ContextMenuInfo getContextMenuInfo() {
            return mEntryContextMenuInfo;
        }

        public void setContextMenuInfo(EntryContextMenuInfo info) {
            mEntryContextMenuInfo = info;
        }
    }

    public static final class EntryContextMenuInfo implements ContextMenuInfo {
        private final String mCopyText;
        private final String mCopyLabel;
        private final String mMimeType;
        private final long mId;
        private final boolean mIsSuperPrimary;

        public EntryContextMenuInfo(
                String copyText, String copyLabel, String mimeType, long id,
                boolean isSuperPrimary) {
            mCopyText = copyText;
            mCopyLabel = copyLabel;
            mMimeType = mimeType;
            mId = id;
            mIsSuperPrimary = isSuperPrimary;
        }

        public String getCopyText() {
            return mCopyText;
        }

        public String getCopyLabel() {
            return mCopyLabel;
        }

        public String getMimeType() {
            return mMimeType;
        }

        public long getId() {
            return mId;
        }

        public boolean isSuperPrimary() {
            return mIsSuperPrimary;
        }
    }

    public static final class EntryTag {
        private final int mId;
        private final Intent mIntent;

        public EntryTag(int id, Intent intent) {
            mId = id;
            mIntent = intent;
        }

        public int getId() {
            return mId;
        }

        public Intent getIntent() {
            return mIntent;
        }
    }

    /**
     * This custom touch listener increases the touch area for the second and third icons, if they
     * are present. This is necessary to maintain other properties on an entry view, like using a
     * top padding on entry. Based off of {@link TouchDelegate}
     */
    private static final class EntryTouchListener
            implements OnTouchListener {
        private final View mEntry;
        private final ImageView mAlternateIcon;
        private final ImageView mThirdIcon;
        /**
         * mTouchedView locks in a view on touch down
         */
        private View mTouchedView;
        /**
         * mSlop adds some space to account for touches that are just outside the hit area
         */
        private int mSlop;

        public EntryTouchListener(
                View entry,
                ImageView alternateIcon,
                ImageView thirdIcon) {
            mEntry = entry;
            mAlternateIcon = alternateIcon;
            mThirdIcon = thirdIcon;
            mSlop = ViewConfiguration.get(entry.getContext())
                    .getScaledTouchSlop();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            View touchedView = mTouchedView;
            boolean sendToTouched = false;
            boolean hit = true;
            boolean handled = false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (hitThirdIcon(event)) {
                        mTouchedView = mThirdIcon;
                        sendToTouched = true;
                    } else if (hitAlternateIcon(event)) {
                        mTouchedView = mAlternateIcon;
                        sendToTouched = true;
                    } else {
                        mTouchedView = mEntry;
                        sendToTouched = false;
                    }
                    touchedView = mTouchedView;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                    sendToTouched =
                            mTouchedView != null && mTouchedView != mEntry;
                    if (sendToTouched) {
                        final Rect slopBounds = new Rect();
                        touchedView.getHitRect(slopBounds);
                        slopBounds.inset(-mSlop, -mSlop);
                        if (!slopBounds.contains((int) event.getX(),
                                                 (int) event.getY())) {
                            hit = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    sendToTouched =
                            mTouchedView != null && mTouchedView != mEntry;
                    mTouchedView = null;
                    break;
            }
            if (sendToTouched) {
                if (hit) {
                    event.setLocation(touchedView.getWidth() / 2,
                                      touchedView.getHeight() / 2);
                } else {
                    // Offset event coordinates to be outside the target view
                    // (in case it does
                    // something like tracking pressed state)
                    event.setLocation(-(mSlop * 2), -(mSlop * 2));
                }
                handled = touchedView.dispatchTouchEvent(event);
            }
            return handled;
        }

        private boolean hitThirdIcon(MotionEvent event) {
            if (mEntry.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                return mThirdIcon.getVisibility() == View.VISIBLE &&
                        event.getX() < mThirdIcon.getRight();
            } else {
                return mThirdIcon.getVisibility() == View.VISIBLE &&
                        event.getX() > mThirdIcon.getLeft();
            }
        }

        /**
         * Should be used after checking if third icon was hit
         */
        private boolean hitAlternateIcon(MotionEvent event) {
            // LayoutParams used to add the start margin to the touch area
            final RelativeLayout.LayoutParams alternateIconParams =
                    (RelativeLayout.LayoutParams) mAlternateIcon
                            .getLayoutParams();
            if (mEntry.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                return mAlternateIcon.getVisibility() == View.VISIBLE &&
                        event.getX() < mAlternateIcon.getRight()
                                + alternateIconParams.rightMargin;
            } else {
                return mAlternateIcon.getVisibility() == View.VISIBLE &&
                        event.getX() > mAlternateIcon.getLeft()
                                - alternateIconParams.leftMargin;
            }
        }
    }

    /**
     * {@code Entry} builder static inner class.
     */
    public static final class Builder {
        private int mId;
        private Drawable mIcon;
        private String mHeader;
        private String mSubHeader;
        private Drawable mSubHeaderIcon;
        private String mText;
        private Drawable mTextIcon;
        private Spannable mPrimaryContentDescription;
        private Intent mIntent;
        private Drawable mAlternateIcon;
        private Intent mAlternateIntent;
        private String mAlternateContentDescription;
        private boolean mShouldApplyColor;
        private boolean mIsEditable;
        private EntryContextMenuInfo mEntryContextMenuInfo;
        private Drawable mThirdIcon;
        private Intent mThirdIntent;
        private String mThirdContentDescription;
        private int mThirdAction = Entry.ACTION_NONE;
        private Bundle mThirdExtras;
        private int mIconResourceId;
        private Uri mIconUri;
        private Transformation mTransformation;

        public Builder() {
        }

        @NonNull
        public Builder id(int val) {
            mId = val;
            return this;
        }

        @NonNull
        public Builder icon(@NonNull Drawable val) {
            mIcon = val;
            return this;
        }

        @NonNull
        public Builder header(@NonNull String val) {
            mHeader = val;
            return this;
        }

        @NonNull
        public Builder subHeader(@NonNull String val) {
            mSubHeader = val;
            return this;
        }

        @NonNull
        public Builder subHeaderIcon(@NonNull Drawable val) {
            mSubHeaderIcon = val;
            return this;
        }

        @NonNull
        public Builder text(@NonNull String val) {
            mText = val;
            return this;
        }

        @NonNull
        public Builder textIcon(@NonNull Drawable val) {
            mTextIcon = val;
            return this;
        }

        @NonNull
        public Builder primaryContentDescription(@NonNull Spannable val) {
            mPrimaryContentDescription = val;
            return this;
        }

        @NonNull
        public Builder intent(Intent val) {
            mIntent = val;
            return this;
        }

        @NonNull
        public Builder alternateIcon(@NonNull Drawable val) {
            mAlternateIcon = val;
            return this;
        }

        @NonNull
        public Builder alternateIntent(@NonNull Intent val) {
            mAlternateIntent = val;
            return this;
        }

        @NonNull
        public Builder alternateContentDescription(@NonNull String val) {
            mAlternateContentDescription = val;
            return this;
        }

        @NonNull
        public Builder shouldApplyColor(boolean val) {
            mShouldApplyColor = val;
            return this;
        }

        @NonNull
        public Builder isEditable(boolean val) {
            mIsEditable = val;
            return this;
        }

        @NonNull
        public Builder entryContextMenuInfo(@NonNull EntryContextMenuInfo val) {
            mEntryContextMenuInfo = val;
            return this;
        }

        @NonNull
        public Builder thirdIcon(@NonNull Drawable val) {
            mThirdIcon = val;
            return this;
        }

        @NonNull
        public Builder thirdIntent(@NonNull Intent val) {
            mThirdIntent = val;
            return this;
        }

        @NonNull
        public Builder thirdContentDescription(@NonNull String val) {
            mThirdContentDescription = val;
            return this;
        }

        @NonNull
        public Builder thirdAction(int val) {
            mThirdAction = val;
            return this;
        }

        @NonNull
        public Builder thirdExtras(@NonNull Bundle val) {
            mThirdExtras = val;
            return this;
        }

        @NonNull
        public Builder iconResourceId(int val) {
            mIconResourceId = val;
            return this;
        }

        public Builder iconUri(Uri iconUri) {
            mIconUri = iconUri;
            return this;
        }

        public Builder iconTransformation(Transformation transformation) {
            mTransformation = transformation;
            return this;
        }

        public Entry build() {
            return new Entry(
                    mId,
                    mIcon,
                    mHeader,
                    mSubHeader,
                    mSubHeaderIcon,
                    mText,
                    mTextIcon,
                    mPrimaryContentDescription,
                    mIntent,
                    mAlternateIcon,
                    mAlternateIntent,
                    mAlternateContentDescription,
                    mShouldApplyColor,
                    mIsEditable,
                    mEntryContextMenuInfo,
                    mThirdIcon,
                    mThirdIntent,
                    mThirdContentDescription,
                    mThirdAction,
                    mThirdExtras,
                    mIconResourceId,
                    mIconUri,
                    mTransformation
            );
        }
    }
}

