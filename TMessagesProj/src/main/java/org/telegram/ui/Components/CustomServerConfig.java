package org.telegram.ui.Components;

import android.app.Activity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

/**
 * Per-account custom MTProto server configuration dialog.
 *
 * The address is stored in a dedicated preferences file so it is not wiped by
 * account logout. On the login screen the change is applied immediately, while
 * from the settings screen it only takes effect after the client is restarted.
 */
public class CustomServerConfig {

    public static void showDialog(Activity activity, int accountId, boolean applyImmediately, Runnable onSaved) {
        if (activity == null) {
            return;
        }

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(8), AndroidUtilities.dp(24), 0);

        TextView info = new TextView(activity);
        info.setText("留空地址即使用官方服务器。切换到另一台服务器后，需要重新登录该账号。");
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        info.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        info.setPadding(0, 0, 0, AndroidUtilities.dp(12));
        layout.addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView hostLabel = new TextView(activity);
        hostLabel.setText("地址");
        hostLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        hostLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        layout.addView(hostLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        EditTextBoldCursor hostField = new EditTextBoldCursor(activity);
        hostField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        hostField.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        hostField.setHintText("例如 192.168.8.101");
        hostField.setHintColor(Theme.getColor(Theme.key_dialogTextHint));
        hostField.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
        hostField.setCursorSize(AndroidUtilities.dp(20));
        hostField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        hostField.setSingleLine(true);
        hostField.setText(ConnectionsManager.getCustomServerHost(accountId));
        hostField.setSelection(hostField.length());
        layout.addView(hostField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 0, 4, 0, 12));

        TextView portLabel = new TextView(activity);
        portLabel.setText("端口");
        portLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        portLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        layout.addView(portLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        EditTextBoldCursor portField = new EditTextBoldCursor(activity);
        portField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        portField.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        portField.setHintText("2398");
        portField.setHintColor(Theme.getColor(Theme.key_dialogTextHint));
        portField.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
        portField.setCursorSize(AndroidUtilities.dp(20));
        portField.setInputType(InputType.TYPE_CLASS_NUMBER);
        portField.setSingleLine(true);
        portField.setText(String.valueOf(ConnectionsManager.getCustomServerPort(accountId)));
        portField.setSelection(portField.length());
        layout.addView(portField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 0, 4, 0, 0));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("服务器");
        builder.setView(layout);
        builder.setPositiveButton("保存", (d, which) -> {
            String host = hostField.getText().toString().trim();
            int port = parsePort(portField.getText().toString());
            boolean enabled = !TextUtils.isEmpty(host);
            ConnectionsManager.saveCustomServer(accountId, host, port, enabled);
            apply(activity, accountId, host, port, enabled, applyImmediately);
            if (onSaved != null) {
                onSaved.run();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("使用官方", (d, which) -> {
            ConnectionsManager.saveCustomServer(accountId, "", 2398, false);
            apply(activity, accountId, "", 2398, false, applyImmediately);
            if (onSaved != null) {
                onSaved.run();
            }
        });
        builder.show();
    }

    private static void apply(Activity activity, int accountId, String host, int port, boolean enabled, boolean immediately) {
        if (immediately) {
            ConnectionsManager.getInstance(accountId).switchCustomServer(host, port, enabled);
        } else {
            Toast.makeText(activity, "已保存，重启客户端后生效", Toast.LENGTH_LONG).show();
        }
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value.trim());
            if (port > 0 && port <= 65535) {
                return port;
            }
        } catch (Exception ignore) {
        }
        return 2398;
    }
}
