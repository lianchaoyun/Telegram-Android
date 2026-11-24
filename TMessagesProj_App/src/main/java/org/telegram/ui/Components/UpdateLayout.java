package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.IUpdateLayout;

/**
 * Bottom "update available" bar of the self-hosted build. The release APK is
 * hosted on our own update service, so the bar opens its download URL instead
 * of using the built-in Telegram updater (which would need an uploaded
 * document).
 */
public class UpdateLayout extends IUpdateLayout {

    private final Activity activity;
    private final ViewGroup container;

    private FrameLayout layout;
    private TextView textView;

    public UpdateLayout(Activity activity, ViewGroup sideMenuContainer) {
        super(activity, sideMenuContainer);
        this.activity = activity;
        this.container = sideMenuContainer;
    }

    @Override
    public void createUpdateUI(int currentAccount) {
        if (container == null || layout != null) {
            return;
        }
        layout = new FrameLayout(activity);
        layout.setVisibility(View.INVISIBLE);
        layout.setTranslationY(dp(44));
        container.addView(layout, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, 44,
                Gravity.LEFT | Gravity.BOTTOM));

        textView = new TextView(activity);
        textView.setTextColor(0xffffffff);
        textView.setTextSize(15);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(dp(16), 0, dp(16), 0);
        layout.addView(textView, LayoutHelper.createFrameMatchParent());

        layout.setOnClickListener(v -> {
            final TLRPC.TL_help_appUpdate update = SharedConfig.pendingAppUpdate;
            if (update == null || TextUtils.isEmpty(update.url)) {
                return;
            }
            Browser.openUrlInSystemBrowser(activity, update.url);
        });
    }

    @Override
    public void updateAppUpdateViews(int currentAccount, boolean animated) {
        if (container == null) {
            return;
        }
        final TLRPC.TL_help_appUpdate update = SharedConfig.pendingAppUpdate;
        final boolean visible = update != null && !TextUtils.isEmpty(update.url);
        if (visible) {
            createUpdateUI(currentAccount);
            final String version = TextUtils.isEmpty(update.version)
                    ? ""
                    : (" " + update.version);
            textView.setText(LocaleController.getString(R.string.AppUpdate) + version);
            if (layout.getTag() == null) {
                layout.setTag(1);
                layout.setVisibility(View.VISIBLE);
                if (animated) {
                    layout.animate()
                            .translationY(0)
                            .setInterpolator(CubicBezierInterpolator.EASE_OUT)
                            .setDuration(180)
                            .start();
                } else {
                    layout.setTranslationY(0);
                }
            }
        } else if (layout != null && layout.getTag() != null) {
            layout.setTag(null);
            if (animated) {
                layout.animate()
                        .translationY(dp(44))
                        .setInterpolator(CubicBezierInterpolator.EASE_OUT)
                        .setDuration(180)
                        .withEndAction(() -> {
                            if (layout.getTag() == null) {
                                layout.setVisibility(View.INVISIBLE);
                            }
                        })
                        .start();
            } else {
                layout.setTranslationY(dp(44));
                layout.setVisibility(View.INVISIBLE);
            }
        }
    }
}
