/**
 * Copyright 2016 bingoogolapple
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.bingoogolapple.scaffolding.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewStubCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle.components.support.RxFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.bingoogolapple.scaffolding.R;
import cn.bingoogolapple.scaffolding.util.AppManager;
import cn.bingoogolapple.scaffolding.util.KeyboardUtil;
import cn.bingoogolapple.titlebar.BGATitleBar;
import pub.devrel.easypermissions.EasyPermissions;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/9/2 下午10:57
 * 描述:
 */
public abstract class MvcFragment extends RxFragment implements EasyPermissions.PermissionCallbacks, BGATitleBar.Delegate {
    protected View mContentView;
    protected MvcActivity mActivity;

    protected boolean mIsLoadedData = false;

    protected BGATitleBar mTitleBar;
    protected Toolbar mToolbar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MvcActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 避免多次从xml中加载布局文件
        if (mContentView == null) {
            initContentView();
            setListener();
            processLogic(savedInstanceState);
        } else {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            if (parent != null) {
                parent.removeView(mContentView);
            }
        }
        return mContentView;
    }

    protected void initContentView() {
        if (getTopBarType() == TopBarType.None) {
            mContentView = LayoutInflater.from(getActivity()).inflate(getRootLayoutResID(), null);
        } else if (getTopBarType() == TopBarType.TitleBar) {
            initTitleBarContentView();
        } else if (getTopBarType() == TopBarType.Toolbar) {
            initToolbarContentView();
        }
    }

    protected void initTitleBarContentView() {
        mContentView = LayoutInflater.from(getActivity()).inflate(isLinear() ? R.layout.rootlayout_linear : R.layout.rootlayout_frame, null);

        ViewStubCompat toolbarVs = getViewById(R.id.toolbarVs);
        toolbarVs.setLayoutResource(R.layout.inc_titlebar);
        toolbarVs.inflate();

        mTitleBar = getViewById(R.id.titleBar);
        mTitleBar.setDelegate(this);

        ViewStubCompat viewStub = getViewById(R.id.contentVs);
        viewStub.setLayoutResource(getRootLayoutResID());
        viewStub.inflate();
    }

    @Override
    public void onClickLeftCtv() {
    }

    @Override
    public void onClickRightCtv() {
    }

    @Override
    public void onClickRightSecondaryCtv() {
    }

    @Override
    public void onClickTitleCtv() {
    }

    protected void initToolbarContentView() {
        mContentView = LayoutInflater.from(getActivity()).inflate(isLinear() ? R.layout.rootlayout_linear : R.layout.rootlayout_frame, null);

        ViewStubCompat toolbarVs = getViewById(R.id.toolbarVs);
        toolbarVs.setLayoutResource(R.layout.inc_toolbar);
        toolbarVs.inflate();
        mToolbar = getViewById(R.id.toolbar);

        ViewStubCompat viewStub = getViewById(R.id.contentVs);
        viewStub.setLayoutResource(getRootLayoutResID());
        viewStub.inflate();

        setHasOptionsMenu(true);
    }

    /**
     * 有 TitleBar 或者 Toolbar 时，是否为线性布局
     *
     * @return
     */
    protected boolean isLinear() {
        return true;
    }

    protected TopBarType getTopBarType() {
        return TopBarType.None;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed()) {
            handleOnVisibilityChangedToUser(isVisibleToUser);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            handleOnVisibilityChangedToUser(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            handleOnVisibilityChangedToUser(false);
        }
    }

    /**
     * 处理对用户是否可见
     *
     * @param isVisibleToUser
     */
    private void handleOnVisibilityChangedToUser(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            // 对用户可见
            if (!mIsLoadedData) {
                Logger.d(this.getClass().getSimpleName() + " 懒加载一次");
                mIsLoadedData = true;
                onLazyLoadOnce();
            }
            Logger.d(this.getClass().getSimpleName() + " 对用户可见");
            MobclickAgent.onPageStart(this.getClass().getSimpleName());
            onVisibleToUser();
        } else {
            // 对用户不可见
            Logger.d(this.getClass().getSimpleName() + " 对用户不可见");
            MobclickAgent.onPageEnd(this.getClass().getSimpleName());
            onInvisibleToUser();
        }
    }

    /**
     * 懒加载一次。如果只想在对用户可见时才加载数据，并且只加载一次数据，在子类中重写该方法
     */
    protected void onLazyLoadOnce() {
    }

    /**
     * 对用户可见时触发该方法。如果只想在对用户可见时才加载数据，在子类中重写该方法
     */
    protected void onVisibleToUser() {
    }

    /**
     * 对用户不可见时触发该方法
     */
    protected void onInvisibleToUser() {
    }

    /**
     * 获取布局文件根视图
     *
     * @return
     */
    protected abstract
    @LayoutRes
    int getRootLayoutResID();

    /**
     * 初始化View控件
     */
    protected void initView(Bundle savedInstanceState) {
    }

    /**
     * 给View控件添加事件监听器
     */
    protected abstract void setListener();

    /**
     * 处理业务逻辑，状态恢复等操作
     *
     * @param savedInstanceState
     */
    protected abstract void processLogic(Bundle savedInstanceState);

    /**
     * 跳转到下一个Activity，并且销毁当前Activity
     *
     * @param cls 下一个Activity的Class
     */
    public void forwardAndFinish(Class<?> cls) {
        forward(cls);
        mActivity.finish();
    }

    /**
     * 跳转到下一个Activity，不销毁当前Activity
     *
     * @param cls 下一个Activity的Class
     */
    public void forward(Class<?> cls) {
        KeyboardUtil.closeKeyboard(mActivity);
        startActivity(new Intent(mActivity, cls));
        mActivity.executeForwardAnim();
    }

    public void forward(Class<?> cls, int requestCode) {
        forward(new Intent(mActivity, cls), requestCode);
    }

    public void forwardAndFinish(Intent intent) {
        forward(intent);
        mActivity.finish();
    }

    public void forward(Intent intent) {
        KeyboardUtil.closeKeyboard(mActivity);
        startActivity(intent);
        mActivity.executeForwardAnim();
    }

    public void forward(Intent intent, int requestCode) {
        KeyboardUtil.closeKeyboard(mActivity);
        startActivityForResult(intent, requestCode);
        mActivity.executeForwardAnim();
    }

    /**
     * 回到上一个Activity，并销毁当前Activity
     */
    public void backward() {
        KeyboardUtil.closeKeyboard(mActivity);
        mActivity.finish();
        mActivity.executeBackwardAnim();
    }

    /**
     * 回到上一个Activity，并销毁当前Activity（应用场景：欢迎、登录、注册这三个界面）
     *
     * @param cls 上一个Activity的Class
     */
    public void backwardAndFinish(Class<?> cls) {
        KeyboardUtil.closeKeyboard(mActivity);
        startActivity(new Intent(mActivity, cls));
        mActivity.executeBackwardAnim();
        mActivity.finish();
    }

    /**
     * 设置点击事件，并防止重复点击
     *
     * @param id
     * @param action
     */
    protected void setOnClick(@IdRes int id, Action1 action) {
        setOnClick(getViewById(id), action);
    }

    /**
     * 设置点击事件，并防止重复点击
     *
     * @param view
     * @param action
     */
    protected void setOnClick(View view, Action1 action) {
        RxView.clicks(view).throttleFirst(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(action);
    }

    /**
     * 查找View
     *
     * @param id   控件的id
     * @param <VT> View类型
     * @return
     */
    protected <VT extends View> VT getViewById(@IdRes int id) {
        return (VT) mContentView.findViewById(id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().refWatcherWatchFragment(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }
}