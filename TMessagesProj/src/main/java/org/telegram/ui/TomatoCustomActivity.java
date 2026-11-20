package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

/**
 * 自定义界面示例页：展示客户端版本、当前服务器与账号，并提供检查更新与下载页入口。
 * 从这里可以直观看到客户端版本更新后的界面变化。
 */
public class TomatoCustomActivity extends BaseFragment {

    private static final int COLOR_TOMATO = 0xFFE5484D;
    private static final int COLOR_TOMATO_LIGHT = 0xFFFF7A45;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("实验室");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ScrollView scrollView = new ScrollView(context);
        scrollView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scrollView.addView(root, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        root.addView(createHeroCard(context), cardParams(0));

        LinearLayout infoCard = createCard(context);
        addInfoRow(context, infoCard, "客户端版本", BuildVars.BUILD_VERSION_STRING, true);
        addInfoRow(context, infoCard, "版本号", String.valueOf(SharedConfig.buildVersion()), true);
        String server = ConnectionsManager.getCustomServerSummary(currentAccount);
        addInfoRow(context, infoCard, "当前服务器", server == null || server.isEmpty() ? "未配置" : server, true);
        addInfoRow(context, infoCard, "当前账号", currentAccountName(), false);
        root.addView(infoCard, cardParams(12));

        TextView checkButton = createButton(context, "检查更新", true);
        checkButton.setOnClickListener(v -> {
            if (getParentActivity() instanceof LaunchActivity) {
                ((LaunchActivity) getParentActivity()).checkAppUpdate(true, null);
                Toast.makeText(getParentActivity(), "正在检查更新…", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(checkButton, cardParams(9));

        TextView downloadButton = createButton(context, "打开下载页", false);
        downloadButton.setOnClickListener(v -> {
            if (getParentActivity() != null) {
                Browser.openUrlInSystemBrowser(getParentActivity(), "http://192.168.8.101/apps/download/");
            }
        });
        root.addView(downloadButton, cardParams(0));

        TextView footer = new TextView(context);
        footer.setText("该页面随客户端版本更新，用于验证自建更新链路。");
        footer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        footer.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        footerParams.topMargin = AndroidUtilities.dp(24);
        root.addView(footer, footerParams);

        fragmentView = scrollView;
        return fragmentView;
    }

    private String currentAccountName() {
        TLRPC.User user = getUserConfig().getCurrentUser();
        if (user == null) {
            return "未登录";
        }
        if (user.username != null && !user.username.isEmpty()) {
            return "@" + user.username;
        }
        return user.first_name != null ? user.first_name : "已登录";
    }

    private View createHeroCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{COLOR_TOMATO, COLOR_TOMATO_LIGHT});
        background.setCornerRadius(AndroidUtilities.dp(20));
        card.setBackground(background);
        card.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20));

        TextView title = new TextView(context);
        title.setText("tomato");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        card.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("自建 Telegram 生态 · 自定义界面");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setTextColor(0xCCFFFFFF);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = AndroidUtilities.dp(6);
        card.addView(subtitle, subtitleParams);

        TextView build = new TextView(context);
        build.setText("v" + BuildVars.BUILD_VERSION_STRING + " (" + SharedConfig.buildVersion() + ")");
        build.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        build.setTextColor(0xB3FFFFFF);
        LinearLayout.LayoutParams buildParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buildParams.topMargin = AndroidUtilities.dp(12);
        card.addView(build, buildParams);

        return card;
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        background.setCornerRadius(AndroidUtilities.dp(16));
        card.setBackground(background);
        card.setClipToOutline(false);
        return card;
    }

    private void addInfoRow(Context context, LinearLayout card, String label, String value, boolean divider) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));

        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        labelView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        row.addView(labelView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView valueView = new TextView(context);
        valueView.setText(value);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        valueView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        valueView.setGravity(Gravity.END);
        row.addView(valueView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        card.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (divider) {
            View line = new View(context);
            line.setBackgroundColor(Theme.getColor(Theme.key_divider));
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, AndroidUtilities.dp(1)));
            lineParams.leftMargin = AndroidUtilities.dp(16);
            card.addView(line, lineParams);
        }
    }

    private TextView createButton(Context context, String text, boolean primary) {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(AndroidUtilities.dp(48));
        button.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(13), AndroidUtilities.dp(16), AndroidUtilities.dp(13));
        button.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

        GradientDrawable background;
        if (primary) {
            background = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{COLOR_TOMATO, COLOR_TOMATO_LIGHT});
            button.setTextColor(0xFFFFFFFF);
        } else {
            background = new GradientDrawable();
            background.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            background.setStroke(Math.max(1, AndroidUtilities.dp(1)), 0x1F000000);
            button.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        }
        background.setCornerRadius(AndroidUtilities.dp(14));
        button.setBackground(background);
        button.setClickable(true);
        return button;
    }

    private LinearLayout.LayoutParams cardParams(int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(topMarginDp);
        return params;
    }
}
