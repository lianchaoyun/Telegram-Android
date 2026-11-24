package org.telegram.messenger;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.regular.BuildConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.UpdateLayout;
import org.telegram.ui.IUpdateLayout;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    // Self-hosted builds must poll the server's help.getAppUpdate endpoint;
    // LaunchActivity only checks for updates on beta or standalone builds.
    @Override
    protected boolean isBeta() {
        return true;
    }

    // Automatic update notices use the bottom bar, like the stock client.
    // A manual check (force = true) still shows a dialog for immediate feedback.
    @Override
    public boolean showUpdateAppPopup(Context context, TLRPC.TL_help_appUpdate update, int account, boolean forced) {
        return forced && showUpdateAppPopup(context, update, account);
    }

    // The regular flavor ships no built-in updater, so surface the server's
    // release and hand the APK link to the system browser for downloading.
    @Override
    public boolean showUpdateAppPopup(Context context, TLRPC.TL_help_appUpdate update, int account) {
        if (update == null || TextUtils.isEmpty(update.url)) {
            return false;
        }
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            String title = LocaleController.getString(R.string.Update);
            builder.setTitle(title);
            builder.setMessage(update.text);
            builder.setPositiveButton(title, (dialog, which) -> Browser.openUrlInSystemBrowser(context, update.url));
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            builder.show();
            return true;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    @Override
    public IUpdateLayout takeUpdateLayout(Activity activity, ViewGroup sideMenuContainer) {
        return new UpdateLayout(activity, sideMenuContainer);
    }
}
