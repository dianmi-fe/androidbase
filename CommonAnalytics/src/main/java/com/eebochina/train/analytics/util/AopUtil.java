/*
 * Created by wangzhuozhou on 2016/12/2.
 * Copyright 2015－2021 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eebochina.train.analytics.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.LruCache;
import android.util.TimeUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eebochina.train.analytics.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class AopUtil {

    private static LruCache<String, Object> sLruCache;

    // 采集 viewType 忽略以下包内 view 直接返回对应的基础控件 viewType
    private static ArrayList<String> sOSViewPackage = new ArrayList<String>() {{
        add("android##widget");
        add("android##support##v7##widget");
        add("android##support##design##widget");
        add("android##support##text##emoji##widget");
        add("androidx##appcompat##widget");
        add("androidx##emoji##widget");
        add("androidx##cardview##widget");
        add("com##google##android##material");
    }};

    public static Activity getActivityFromContext(Context context) {
        Activity activity = null;
        try {
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {

        }
        return activity;
    }

    /**
     * 根据 Fragment 获取对应的 Activity
     *
     * @param fragment，Fragment
     * @return Activity or null
     */
    public static Activity getActivityFromFragment(Object fragment) {
        Activity activity = null;
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                Method getActivityMethod = fragment.getClass().getMethod("getActivity");
                if (getActivityMethod != null) {
                    activity = (Activity) getActivityMethod.invoke(fragment);
                }
            } catch (Exception e) {
                //ignored
            }
        }
        return activity;
    }


    /**
     * 获取点击 view 的 fragment 对象
     *
     * @param view 点击的 view
     * @return object 这里是 fragment 实例对象
     */
    public static Object getFragmentFromView(View view) {
        return getFragmentFromView(view, null);
    }

    /**
     * 获取点击 view 的 fragment 对象
     *
     * @param view     点击的 view
     * @param activity Activity
     * @return object 这里是 fragment 实例对象
     */
    @SuppressLint("NewApi")
    public static Object getFragmentFromView(View view, Activity activity) {
        try {
            if (view != null) {
                String fragmentName = (String) view.getTag(R.id.arnold_analytics_tag_view_fragment_name);

                if (TextUtils.isEmpty(fragmentName)) {
                    if (activity == null) {
                        //获取所在的 Context
                        Context context = view.getContext();
                        //将 Context 转成 Activity
                        activity = AopUtil.getActivityFromContext(context);
                    }
                    if (activity != null) {
                        Window window = activity.getWindow();
                        if (window != null) {
                            Object tag = window.getDecorView().getRootView().getTag(R.id.arnold_analytics_tag_view_fragment_name);
                            if (tag != null) {
                                fragmentName = traverseParentViewTag(view);
                            }
                        }
                    }
                }

                if (!TextUtils.isEmpty(fragmentName)) {
                    if (sLruCache == null) {
                        sLruCache = new LruCache<>(10);
                    }
                    Object object = sLruCache.get(fragmentName);
                    if (object != null) {
                        return object;
                    }
                    object = Class.forName(fragmentName).newInstance();
                    sLruCache.put(fragmentName, object);
                    return object;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 采集 View 的 $element_type 主要区分继承系统 View 和继承系统 View 的自定义 View
     *
     * @param viewName        View.getCanonicalName（）返回的 name
     * @param defaultTypeName 默认的 typeName
     * @return typeName
     */
    public static String getViewType(String viewName, String defaultTypeName) {
        if (TextUtils.isEmpty(viewName)) {
            return defaultTypeName;
        }
        if (TextUtils.isEmpty(defaultTypeName)) {
            return viewName;
        }

        if (isOSViewByPackage(viewName)) {
            return defaultTypeName;
        }

        return viewName;
    }

    /**
     * 通过反射判断类的类型
     *
     * @param view 判断类型的 viewGroup
     * @return viewType
     */
    public static String getViewGroupTypeByReflect(View view) {
        Class<?> compatClass;
        String viewType = view.getClass().getCanonicalName();
        compatClass = getClassByName("android.support.v7.widget.CardView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "CardView");
        }
        compatClass = getClassByName("androidx.cardview.widget.CardView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "CardView");
        }
        compatClass = getClassByName("android.support.design.widget.NavigationView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "NavigationView");
        }
        compatClass = getClassByName("com.google.android.material.navigation.NavigationView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "NavigationView");
        }
        return viewType;
    }

    /**
     * 通过反射判断类的类型
     *
     * @param view 判断类型的 view
     * @return viewType
     */
    public static String getViewTypeByReflect(View view) {
        Class<?> compatClass;
        String viewType = view.getClass().getCanonicalName();
        compatClass = getClassByName("android.widget.Switch");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "Switch");
        }
        compatClass = getClassByName("android.support.v7.widget.SwitchCompat");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "SwitchCompat");
        }
        compatClass = getClassByName("androidx.appcompat.widget.SwitchCompat");
        if (compatClass != null && compatClass.isInstance(view)) {
            return getViewType(viewType, "SwitchCompat");
        }
        return viewType;
    }

    private static Class<?> getClassByName(String name) {
        Class<?> compatClass;
        try {
            compatClass = Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return compatClass;
    }


    /**
     * 判断是否是系统 view
     *
     * @param viewName view 的名称（包含包名）
     * @return 是否是系统 view  true:是  false: 否
     */
    private static boolean isOSViewByPackage(String viewName) {
        if (TextUtils.isEmpty(viewName)) {
            return false;
        }
        String viewNameTemp = viewName.replace(".", "##");
        for (String OSViewPackage : sOSViewPackage) {
            if (viewNameTemp.startsWith(OSViewPackage)) {
                return true;
            }
        }
        return false;
    }


    private static String traverseParentViewTag(View view) {
        try {
            ViewParent parentView = view.getParent();
            String fragmentName = null;
            while (TextUtils.isEmpty(fragmentName) && parentView instanceof View) {
                fragmentName = (String) ((View) parentView).getTag(R.id.arnold_analytics_tag_view_fragment_name);
                parentView = parentView.getParent();
            }
            return fragmentName;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


    /**
     * 是否是连续点击
     *
     * @param view view
     * @return Boolean
     */
    public static boolean isDoubleClick(View view) {
        if (view == null) {
            return false;
        }
        try {
            long currentOnClickTimestamp = System.currentTimeMillis();
            String tag = (String) view.getTag(R.id.arnold_analytics_tag_view_onclick_timestamp);
            if (!TextUtils.isEmpty(tag)) {

                long lastOnClickTimestamp = Long.parseLong(tag);
                if ((currentOnClickTimestamp - lastOnClickTimestamp) < 500) {
                    return true;
                }
            }
            view.setTag(R.id.arnold_analytics_tag_view_onclick_timestamp, String.valueOf(currentOnClickTimestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取 CompoundButton text
     *
     * @param view view
     * @return CompoundButton 显示的内容
     */
    public static String getCompoundButtonText(View view) {
        try {
            CompoundButton switchButton = (CompoundButton) view;
            Method method;
            if (switchButton.isChecked()) {
                method = view.getClass().getMethod("getTextOn");
            } else {
                method = view.getClass().getMethod("getTextOff");
            }
            return (String) method.invoke(view);
        } catch (Exception ex) {
            return "UNKNOWN";
        }
    }


    public static String traverseView(StringBuilder stringBuilder, ViewGroup root) {
        try {
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
            }

            if (root == null) {
                return stringBuilder.toString();
            }

            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);

                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }

                if (child instanceof ViewGroup) {
                    traverseView(stringBuilder, (ViewGroup) child);
                } else {

                    String viewText = getViewText(child);
                    if (!TextUtils.isEmpty(viewText)) {
                        stringBuilder.append(viewText);
                        stringBuilder.append("-");
                    }
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return stringBuilder != null ? stringBuilder.toString() : "";
        }
    }


    public static String getViewText(View child) {
        if (child instanceof EditText) {
            return "";
        }
        try {
            Class<?> switchCompatClass = null;
            try {
                switchCompatClass = Class.forName("android.support.v7.widget.SwitchCompat");
            } catch (Exception e) {
                //ignored
            }

            if (switchCompatClass == null) {
                try {
                    switchCompatClass = Class.forName("androidx.appcompat.widget.SwitchCompat");
                } catch (Exception e) {
                    //ignored
                }
            }

            CharSequence viewText = null;

            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                viewText = checkBox.getText();
            } else if (switchCompatClass != null && switchCompatClass.isInstance(child)) {
                CompoundButton switchCompat = (CompoundButton) child;
                Method method;
                if (switchCompat.isChecked()) {
                    method = child.getClass().getMethod("getTextOn");
                } else {
                    method = child.getClass().getMethod("getTextOff");
                }
                viewText = (String) method.invoke(child);
            } else if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                viewText = radioButton.getText();
            } else if (child instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) child;
                boolean isChecked = toggleButton.isChecked();
                if (isChecked) {
                    viewText = toggleButton.getTextOn();
                } else {
                    viewText = toggleButton.getTextOff();
                }
            } else if (child instanceof Button) {
                Button button = (Button) child;
                viewText = button.getText();
            } else if (child instanceof CheckedTextView) {
                CheckedTextView textView = (CheckedTextView) child;
                viewText = textView.getText();
            } else if (child instanceof TextView) {
                TextView textView = (TextView) child;
                viewText = textView.getText();
            } else if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;
                if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                    viewText = imageView.getContentDescription().toString();
                }
            } else {
                viewText = child.getContentDescription();
            }
            if (TextUtils.isEmpty(viewText) && child instanceof TextView) {
                viewText = ((TextView) child).getHint();
            }
            if (!TextUtils.isEmpty(viewText)) {
                return viewText.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public void setViewProperties(View view, HashMap<String, Object> properties) {
        if (view == null || properties == null) {
            return;
        }

        view.setTag(R.id.arnold_analytics_tag_view_properties, properties);
    }


    public static String getViewId(View view) {
        String idString = null;
        try {
            if (view.getId() != View.NO_ID) {
                idString = view.getContext().getResources().getResourceEntryName(view.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idString;
    }

    /**
     * 判断 View 是否被忽略
     *
     * @param view View
     * @return 是否被忽略
     */
    public static boolean isViewIgnored(View view) {
        try {
            //基本校验
            if (view == null) {
                return true;
            }

            //View 被忽略
            return "1".equals(view.getTag(R.id.arnold_analytics_tag_view_ignored));

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }


    public static void ignoreView(View view) {
        if (view != null) {
            view.setTag(R.id.arnold_analytics_tag_view_ignored, "1");
        }
    }

    public static void ignoreView(View view, boolean ignore) {
        if (view != null) {
            view.setTag(R.id.arnold_analytics_tag_view_ignored, ignore ? "1" : "0");
        }
    }
}