package cn.bingoogolapple.scaffolding.util;

import android.support.annotation.StringRes;

import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle.components.support.RxFragment;

import cn.bingoogolapple.scaffolding.R;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/24 下午10:11
 * 描述:
 */
public class PermissionUtil {
    /**
     * 跳转到权限设置界面的请求码
     */
    public static final int RC_PERMISSION_SETTINGS_SCREEN = 1025;
    /**
     * 访问外部存储权限
     */
    public static final int RC_PERMISSION_STORAGE = 1026;
    /**
     * 打电话权限
     */
    public static final int RC_PERMISSION_CALL_PHONE = 1027;
    /**
     * 定位权限
     */
    public static final int RC_PERMISSION_LOCATION = 1028;
    /**
     * 拍照权限
     */
    public static final int RC_PERMISSION_TAKE_PHOTO = 1029;
    /**
     * 发短信权限
     */
    public static final int RC_PERMISSION_SEND_SMS = 1030;

    private static final int NO_COMMON_PERMISSION_SETTING_DIALOG_CONTENT_RES_ID = -1;

    private PermissionUtil() {
    }

    /**
     * 请求权限
     *
     * @param object
     * @param permissionRequestTipResId
     * @param requestCode
     * @param perms
     */
    public static void requestPermissions(Object object, @StringRes int permissionRequestTipResId, int requestCode, String... perms) {
        EasyPermissions.requestPermissions(object, AppManager.getApp().getString(permissionRequestTipResId), R.string.permission_settings_confirm, R.string.permission_settings_cancel, requestCode, perms);
    }

    /**
     * 某些权限被拒绝
     *
     * @param activity    RxAppCompatActivity
     * @param delegate    Delegate
     * @param requestCode 权限请求码
     */
    public static void onPermissionsDenied(RxAppCompatActivity activity, Delegate delegate, int requestCode) {
        int contentResId = getCommonPermissionSettingDialogContentResId(requestCode);
        if (contentResId != NO_COMMON_PERMISSION_SETTING_DIALOG_CONTENT_RES_ID) {
            showOpenPermissionSettingsScreenDialog(activity, delegate, requestCode, contentResId);
        } else {
            delegate.onSomePermissionDenied(requestCode);
        }
    }

    /**
     * 显示打开权限设置界面的对话框
     *
     * @param activity     RxAppCompatActivity
     * @param delegate     Delegate
     * @param requestCode  权限请求码
     * @param contentResId 对话框提示内容
     */
    public static void showOpenPermissionSettingsScreenDialog(RxAppCompatActivity activity, Delegate delegate, int requestCode, @StringRes int contentResId) {
        new AppSettingsDialog.Builder(activity, activity.getString(contentResId, AppManager.getAppName()))
                .setTitle(activity.getString(R.string.permission_settings_title))
                .setPositiveButton(activity.getString(R.string.permission_settings_confirm))
                .setNegativeButton(activity.getString(R.string.permission_settings_cancel), (dialog, which) -> {
                    if (isNotHandleCommonPermissionsDenied(requestCode, activity)) {
                        delegate.onClickCancelOpenPermissionsSettingsScreen(requestCode);
                    }
                })
                .setRequestCode(RC_PERMISSION_SETTINGS_SCREEN)
                .build()
                .show();
    }

    /**
     * 某些权限被拒绝
     *
     * @param fragment    RxFragment
     * @param delegate    Delegate
     * @param requestCode 权限请求码
     */
    public static void onPermissionsDenied(RxFragment fragment, Delegate delegate, int requestCode) {
        int contentResId = getCommonPermissionSettingDialogContentResId(requestCode);
        if (contentResId != NO_COMMON_PERMISSION_SETTING_DIALOG_CONTENT_RES_ID) {
            showOpenPermissionSettingsScreenDialog(fragment, delegate, requestCode, contentResId);
        } else {
            delegate.onSomePermissionDenied(requestCode);
        }
    }

    /**
     * 显示打开权限设置界面的对话框
     *
     * @param fragment     RxFragment
     * @param delegate     Delegate
     * @param requestCode  权限请求码
     * @param contentResId 对话框提示内容
     */
    public static void showOpenPermissionSettingsScreenDialog(RxFragment fragment, Delegate delegate, int requestCode, @StringRes int contentResId) {
        new AppSettingsDialog.Builder(fragment, fragment.getString(contentResId, AppManager.getAppName()))
                .setTitle(fragment.getString(R.string.permission_settings_title))
                .setPositiveButton(fragment.getString(R.string.permission_settings_confirm))
                .setNegativeButton(fragment.getString(R.string.permission_settings_cancel), (dialog, which) -> {
                    if (isNotHandleCommonPermissionsDenied(requestCode, fragment)) {
                        delegate.onClickCancelOpenPermissionsSettingsScreen(requestCode);
                    }
                })
                .setRequestCode(RC_PERMISSION_SETTINGS_SCREEN)
                .build()
                .show();
    }

    /**
     * 获取公共的打开权限设置界面对话框的提示内容资源id
     *
     * @param requestCode
     * @return
     */
    private static int getCommonPermissionSettingDialogContentResId(int requestCode) {
        int contentResId = NO_COMMON_PERMISSION_SETTING_DIALOG_CONTENT_RES_ID;
        if (requestCode == RC_PERMISSION_STORAGE) {
            contentResId = R.string.permission_settings_content_storage;
        } else if (requestCode == RC_PERMISSION_TAKE_PHOTO) {
            contentResId = R.string.permission_settings_content_take_photo;
        } else if (requestCode == RC_PERMISSION_CALL_PHONE) {
            contentResId = R.string.permission_settings_content_call_phone;
        } else if (requestCode == RC_PERMISSION_LOCATION) {
            contentResId = R.string.permission_settings_content_location;
        } else if (requestCode == RC_PERMISSION_SEND_SMS) {
            contentResId = R.string.permission_settings_content_send_sms;
        }
        return contentResId;
    }

    /**
     * 是否没有处理公共的权限被拒绝
     *
     * @param requestCode
     * @param lifecycleProvider
     * @return
     */
    private static boolean isNotHandleCommonPermissionsDenied(int requestCode, LifecycleProvider lifecycleProvider) {
        boolean isNotHandled = false;
        String appName = AppManager.getAppName();
        if (requestCode == RC_PERMISSION_STORAGE) {
            ToastUtil.show(AppManager.getApp().getString(R.string.tip_permission_storage_denied, appName));
            RxUtil.runInUIThreadDelay(1500, lifecycleProvider).subscribe(aVoid -> AppManager.getInstance().exit());
        } else if (requestCode == RC_PERMISSION_TAKE_PHOTO) {
            ToastUtil.show(AppManager.getApp().getString(R.string.tip_permission_take_photo_denied, appName));
        } else if (requestCode == RC_PERMISSION_CALL_PHONE) {
            ToastUtil.show(AppManager.getApp().getString(R.string.tip_permission_call_phone_denied, appName));
        } else if (requestCode == RC_PERMISSION_LOCATION) {
            ToastUtil.show(AppManager.getApp().getString(R.string.tip_permission_location_denied, appName));
        } else if (requestCode == RC_PERMISSION_SEND_SMS) {
            ToastUtil.show(AppManager.getApp().getString(R.string.tip_permission_send_sms_denied, appName));
        } else {
            isNotHandled = true;
        }
        return isNotHandled;
    }

    public interface Delegate {
        /**
         * 点击取消打开权限设置界面
         *
         * @param requestCode
         */
        void onClickCancelOpenPermissionsSettingsScreen(int requestCode);

        /**
         * 某些权限被拒绝
         *
         * @param requestCode
         */
        void onSomePermissionDenied(int requestCode);
    }
}