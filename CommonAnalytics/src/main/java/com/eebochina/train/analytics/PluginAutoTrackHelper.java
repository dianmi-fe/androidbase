package com.eebochina.train.analytics;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.eebochina.train.analytics.base.IAnalytics;
import com.eebochina.train.analytics.entity.ViewNode;
import com.eebochina.train.analytics.util.AopUtil;
import com.eebochina.train.analytics.util.ViewUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PluginAutoTrackHelper {
    private static final String TAG = "Analytics";

    public static void track(final String eventName, final String properties) {

        try {
            if (TextUtils.isEmpty(eventName)) {
                return;
            }
            JSONObject pro = null;
            if (!TextUtils.isEmpty(properties)) {
                try {
                    pro = new JSONObject(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String pageRoute = (String) pro.remove("pageRoute");
            if (pageRoute == null) {
                return;
            }

            String sessionId = (String) pro.remove("sessionId");
            if (sessionId == null) {
                return;
            }

            String pagePath = (String) pro.remove("pagePath");
            if (pagePath == null) {
                return;
            }

            Map<String, Object> parameter = new HashMap<>();
            Iterator<String> keys = pro.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                parameter.put(key, pro.get(key));
            }

            DataAutoTrackHelper.INSTANCE.trackEvent(pageRoute, parameter, pagePath, sessionId, eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============track" + properties);
        }
    }


    public static void trackViewOnClick(final View view) {
        if (view == null) {
            return;
        }
        trackViewOnClick(view, view.isPressed());

    }

    public static void trackViewOnClick(final View view, final boolean isFromUser) {
        if (view == null) {
            return;
        }

        try {
            //获取所在的 Context
            Context context = view.getContext();

            //将 Context 转成 Activity
            Activity activity = AopUtil.getActivityFromContext(context);

            if (activity == null) {
                return;
            }

            // 获取 view 所在的 fragment
            Object fragment = AopUtil.getFragmentFromView(view, activity);

            if (AopUtil.isDoubleClick(view)) {
                return;
            }

            //view 被忽略
            if (AopUtil.isViewIgnored(view)) {
                return;
            }

            String pagePath = "";
            String route = "";
            String sessionId = "";
            Map<String, Object> parameter;

            if (fragment instanceof IAnalytics && ((IAnalytics) fragment).autoTrackPage()) {
                pagePath = fragment.getClass().getCanonicalName();
                route = ((IAnalytics) fragment).pageRoute();
                sessionId = ((IAnalytics) fragment).sessionId();
                parameter = ((IAnalytics) fragment).parameter();
            } else if (activity instanceof IAnalytics && ((IAnalytics) activity).autoTrackPage()) {
                pagePath = activity.getClass().getCanonicalName();
                route = ((IAnalytics) activity).pageRoute();
                sessionId = ((IAnalytics) activity).sessionId();
                parameter = ((IAnalytics) activity).parameter();
            } else {
                return;
            }

            if (parameter == null) {
                parameter = new HashMap<>();
            }

            ViewNode viewNode = ViewUtil.INSTANCE.getViewContentAndType(view);
            String viewText = viewNode.getViewContent();

           String idString =  AopUtil.getViewId(view);

            //3.获取 View 自定义属性
            HashMap<String, Object> properties = (HashMap<String, Object>) view.getTag(R.id.arnold_analytics_tag_view_properties);
            if (properties != null) {
                parameter.putAll(properties);
            }

            parameter.put("element_type", viewNode.getViewType());
            if (!TextUtils.isEmpty(idString)) {
                parameter.put("element_id", idString);
            }

            DataAutoTrackHelper.INSTANCE.trackEvent(route, parameter, pagePath, sessionId, viewText);

            if (AnalyticsDataApi.INSTANCE.getDebug()) {
                Log.d(TAG, "=============trackViewOnClick");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void trackMenuItem(final MenuItem menuItem) {
        trackMenuItem(null, menuItem);
    }

    public static void trackMenuItem(final Object object, final MenuItem menuItem) {
//        ThreadUtils.getSinglePool().execute(new Runnable() {
//            @Override
//            public void run() {
//                if (menuItem == null) {
//                    return;
//                }
//
//
//            }
//        });

        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============trackMenuItem");
        }
    }


    public static void trackListView(AdapterView<?> adapterView, View view, int position) {
        if (view == null) {
            return;
        }


        try {
            //获取所在的 Context
            Context context = view.getContext();

            //将 Context 转成 Activity
            Activity activity = AopUtil.getActivityFromContext(context);

            if (activity == null) {
                return;
            }

            //view 被忽略
            if (AopUtil.isViewIgnored(view)) {
                return;
            }

            // 获取 view 所在的 fragment
            Object fragment = AopUtil.getFragmentFromView(view, activity);

            String pagePath = "";
            String route = "";
            String sessionId = "";
            Map<String, Object> parameter;

            if (fragment instanceof IAnalytics && ((IAnalytics) fragment).autoTrackPage()) {
                pagePath = fragment.getClass().getCanonicalName();
                route = ((IAnalytics) fragment).pageRoute();
                sessionId = ((IAnalytics) fragment).sessionId();
                parameter = ((IAnalytics) fragment).parameter();
            } else if (activity instanceof IAnalytics && ((IAnalytics) activity).autoTrackPage()) {
                pagePath = activity.getClass().getCanonicalName();
                route = ((IAnalytics) activity).pageRoute();
                sessionId = ((IAnalytics) activity).sessionId();
                parameter = ((IAnalytics) activity).parameter();
            } else {
                return;
            }
            if (parameter == null) {
                parameter = new HashMap<>();
            }

            parameter.put("position", position);

            if (adapterView instanceof ListView) {
                parameter.put("element_type", "ListView");
            } else if (adapterView instanceof GridView) {
                parameter.put("element_type", "GridView");
            } else if (adapterView instanceof Spinner) {
                parameter.put("element_type", "Spinner");
            }


            String viewText = null;
            if (view instanceof ViewGroup) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    viewText = AopUtil.traverseView(stringBuilder, (ViewGroup) view);
                    if (!TextUtils.isEmpty(viewText)) {
                        viewText = viewText.substring(0, viewText.length() - 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                viewText = AopUtil.getViewText(view);
            }

            //3.获取 View 自定义属性
            HashMap<String, Object> properties = (HashMap<String, Object>) view.getTag(R.id.arnold_analytics_tag_view_properties);
            if (properties != null) {
                parameter.putAll(properties);
            }

            DataAutoTrackHelper.INSTANCE.trackEvent(route, parameter, pagePath, sessionId, viewText);
            if (AnalyticsDataApi.INSTANCE.getDebug()) {
                Log.d(TAG, "=============trackListView");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void trackRadioGroup(RadioGroup view, int checkedId) {
        if (view == null) {
            return;
        }

        try {
            View childView = view.findViewById(checkedId);
            if (childView == null || !childView.isPressed()) {
                return;
            }

            //获取所在的 Context
            Context context = view.getContext();

            //将 Context 转成 Activity
            Activity activity = AopUtil.getActivityFromContext(context);

            if (activity == null) {
                return;
            }

            //view 被忽略
            if (AopUtil.isViewIgnored(view)) {
                return;
            }

            // 获取 view 所在的 fragment
            Object fragment = AopUtil.getFragmentFromView(view, activity);

            String pagePath = "";
            String route = "";
            String sessionId = "";
            Map<String, Object> parameter;

            if (fragment instanceof IAnalytics && ((IAnalytics) fragment).autoTrackPage()) {
                pagePath = fragment.getClass().getCanonicalName();
                route = ((IAnalytics) fragment).pageRoute();
                sessionId = ((IAnalytics) fragment).sessionId();
                parameter = ((IAnalytics) fragment).parameter();
            } else if (activity instanceof IAnalytics && ((IAnalytics) activity).autoTrackPage()) {
                pagePath = activity.getClass().getCanonicalName();
                route = ((IAnalytics) activity).pageRoute();
                sessionId = ((IAnalytics) activity).sessionId();
                parameter = ((IAnalytics) activity).parameter();
            } else {
                return;
            }

            if (parameter == null) {
                parameter = new HashMap<>();
            }

            //ViewId
            String idString = AopUtil.getViewId(view);
            if (!TextUtils.isEmpty(idString)) {
                parameter.put("element_id", idString);
            }

            String viewType = "RadioButton";
            if (childView != null) {
                viewType = AopUtil.getViewType(childView.getClass().getCanonicalName(), "RadioButton");
            }
            parameter.put("element_type", viewType);


            //获取变更后的选中项的ID
            int checkedRadioButtonId = view.getCheckedRadioButtonId();
            String viewText = null;
            if (activity != null) {
                try {
                    RadioButton radioButton = activity.findViewById(checkedRadioButtonId);
                    if (radioButton != null) {
                        if (!TextUtils.isEmpty(radioButton.getText())) {
                            viewText = radioButton.getText().toString();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DataAutoTrackHelper.INSTANCE.trackEvent(route, parameter, pagePath, sessionId, viewText);
            if (AnalyticsDataApi.INSTANCE.getDebug()) {
                Log.d(TAG, "=============trackRadioGroup");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void trackDialog(DialogInterface dialogInterface, int whichButton) {
        if (dialogInterface == null) {
            return;
        }
        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============trackDialog");
        }
    }


    public static void trackExpandableListViewOnGroupClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition) {
        if (expandableListView == null) {
            return;
        }
        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============trackExpandableListViewOnGroupClick");
        }
    }

    public static void trackExpandableListViewOnChildClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition, int childPosition) {
        if (expandableListView == null) {
            return;
        }
        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============trackExpandableListViewOnChildClick");
        }
    }


    public static void trackTabHost(String tabName) {
        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============trackTabHost");
        }
    }

    public static void trackTabLayoutSelected(Object object, Object tab) {
        if (tab == null) {
            return;
        }

        if (AnalyticsDataApi.INSTANCE.getDebug()) {
            Log.d(TAG, "=============trackTabLayoutSelected");
        }
    }


}
