package com.eebochina.train.analytics.util

import android.annotation.SuppressLint
import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.eebochina.train.analytics.entity.ViewNode
import java.lang.reflect.InvocationTargetException

object ViewUtil {
    fun getViewContentAndType(view: View): ViewNode? {
        var viewType = view.javaClass.canonicalName
        var viewText: CharSequence? = null
        var tab: Any? = null
        if (view is CheckBox) { // CheckBox
            viewType = AopUtil.getViewType(viewType, "CheckBox")
            viewText = view.text
        } else if (view is RadioButton) { // RadioButton
            viewType = AopUtil.getViewType(viewType, "RadioButton")
            viewText = view.text
        } else if (view is ToggleButton) { // ToggleButton
            viewType = AopUtil.getViewType(viewType, "ToggleButton")
            viewText = AopUtil.getCompoundButtonText(view)
        } else if (view is CompoundButton) {
            viewType = AopUtil.getViewTypeByReflect(view)
            viewText = AopUtil.getCompoundButtonText(view)
        } else if (view is Button) { // Button
            viewType = AopUtil.getViewType(viewType, "Button")
            viewText = view.text
        } else if (view is CheckedTextView) { // CheckedTextView
            viewType = AopUtil.getViewType(viewType, "CheckedTextView")
            viewText = view.text
        } else if (view is TextView) { // TextView
            viewType = AopUtil.getViewType(viewType, "TextView")
            viewText = view.text
        } else if (view is ImageView) { // ImageView
            viewType = AopUtil.getViewType(viewType, "ImageView")
            if (!TextUtils.isEmpty(view.contentDescription)) {
                viewText = view.contentDescription.toString()
            }
        } else if (view is RatingBar) {
            viewType = AopUtil.getViewType(viewType, "RatingBar")
            viewText = view.rating.toString()
        } else if (view is SeekBar) {
            viewType = AopUtil.getViewType(viewType, "SeekBar")
            viewText = view.progress.toString()
        } else if (view is Spinner) {
            viewType = AopUtil.getViewType(viewType, "Spinner")
            try {
                val stringBuilder = StringBuilder()
                viewText = AopUtil.traverseView(stringBuilder, view as ViewGroup)
                if (!TextUtils.isEmpty(viewText)) {
                    viewText = viewText.toString().substring(0, viewText!!.length - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (instanceOfTabView(view).also { tab = it } != null) {
            viewText = getTabLayoutContent(tab!!)
            viewType = AopUtil.getViewType(viewType, "TabLayout")
        } else if (instanceOfBottomNavigationItemView(view)) {
            val itemData: Any? = getItemData(view)
            if (itemData != null) {
                try {
                    val menuItemImplClass: Class<*>? = ReflectUtil.getCurrentClass(arrayOf("androidx.appcompat.view.menu.MenuItemImpl"))
                    if (menuItemImplClass != null) {
                        val title: String = ReflectUtil.findField(
                            menuItemImplClass,
                            itemData,
                            "mTitle"
                        )
                        if (!TextUtils.isEmpty(title)) {
                            viewText = title
                        }
                    }
                } catch (e: Exception) {

                }
            }
        } else if (instanceOfNavigationView(view)) {
            viewText = if (isViewSelfVisible(view)) "Open" else "Close"
            viewType = AopUtil.getViewType(viewType, "NavigationView")
        } else if (view is ViewGroup) {
            viewType = AopUtil.getViewGroupTypeByReflect(view)
            viewText = view.getContentDescription()
            if (TextUtils.isEmpty(viewText)) {
                try {
                    val stringBuilder = StringBuilder()
                    viewText = AopUtil.traverseView(stringBuilder, view)
                    if (!TextUtils.isEmpty(viewText)) {
                        viewText = viewText.toString().substring(0, viewText.length - 1)
                    }
                } catch (e: Exception) {

                }
            }
        }
        if (TextUtils.isEmpty(viewText) && view is TextView) {
            viewText = view.hint
        }
        if (TextUtils.isEmpty(viewText)) {
            viewText = view.contentDescription
        }
        if (view is EditText) {
            viewText = ""
        }
        if (TextUtils.isEmpty(viewText)) {
            viewText = ""
        }
        return ViewNode(viewText.toString(), viewType)
    }


    private fun instanceOfTabView(tabView: View): Any? {
        try {
            val currentTabViewClass: Class<*> = ReflectUtil.getCurrentClass(
                arrayOf(
                    "android.support.design.widget.TabLayout\$TabView",
                    "com.google.android.material.tabs.TabLayout\$TabView"
                )
            )
            if (currentTabViewClass != null && currentTabViewClass.isAssignableFrom(tabView.javaClass)) {
                return ReflectUtil.findField<Any>(currentTabViewClass, tabView, "mTab", "tab")
            }
        } catch (e: java.lang.Exception) {
        }
        return null
    }


    private fun instanceOfNavigationView(view: Any): Boolean {
        val clazz: Class<*> = try {
            Class.forName("android.support.design.widget.NavigationView")
        } catch (th: ClassNotFoundException) {
            try {
                Class.forName("com.google.android.material.navigation.NavigationView")
            } catch (e2: ClassNotFoundException) {
                return false
            }
        }
        return clazz.isInstance(view)
    }


    private fun getTabLayoutContent(tab: Any): String? {
        var viewText: String? = null
        var currentTabClass: Class<*>? = null
        try {
            currentTabClass = ReflectUtil.getCurrentClass(
                arrayOf(
                    "android.support.design.widget.TabLayout\$Tab",
                    "com.google.android.material.tabs.TabLayout\$Tab"
                )
            )
            if (currentTabClass != null) {
                var text: Any? = null
                text = ReflectUtil.callMethod(tab, "getText")
                if (text != null) {
                    viewText = text.toString()
                }
                val customView: View = ReflectUtil.findField(
                    currentTabClass,
                    tab,
                    "mCustomView",
                    "customView"
                )
                if (customView != null) {
                    val stringBuilder = java.lang.StringBuilder()
                    if (customView is ViewGroup) {
                        viewText = AopUtil.traverseView(stringBuilder, customView)
                        if (!TextUtils.isEmpty(viewText)) {
                            viewText = viewText.toString().substring(0, viewText.length - 1)
                        }
                    } else {
                        viewText = AopUtil.getViewText(customView)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return viewText
    }


    fun instanceOfBottomNavigationItemView(view: Any?): Boolean {
        try {
            val clazz: Class<*> = Class.forName("com.google.android.material.bottomnavigation.BottomNavigationItemView")
            return clazz.isInstance(view)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    fun getItemData(view: View): Any? {
        try {
            val method = view.javaClass.getMethod("getItemData")
            return method.invoke(view)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e2: InvocationTargetException) {
            e2.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("NewApi")
    fun isViewSelfVisible(view: View?): Boolean {
        if (view == null || view.windowVisibility == View.GONE) {
            return false
        }

        if (view.width <= 0 || view.height <= 0 || view.alpha <= 0.0f || !view.getLocalVisibleRect(Rect())) {
            return false
        }
        return !((view.visibility == View.VISIBLE || view.animation == null || !view.animation.fillAfter) && view.visibility != View.VISIBLE)
    }

}