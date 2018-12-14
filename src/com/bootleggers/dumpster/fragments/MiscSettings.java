package com.bootleggers.dumpster.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.provider.Settings;
import com.android.settings.R;
import com.bootleggers.dumpster.extra.Utils;
import com.bootleggers.dumpster.preferences.AppMultiSelectListPreference;
import com.bootleggers.dumpster.preferences.ScrollAppsViewPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;

public class MiscSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String DEVICE_CATEGORY = "device_extras_category";
    private static final String DEVICE_OMNI_CATEGORY = "device_extras_omni_category";
    private static final String DEVICE_OMNI_PACKAGE = "org.omnirom.device";
    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";
    private static final String RANDOMBOOTANIMATION_PREF = "random_bootanimations_toggle";
    private static final String RANDOMBOOTANIMATION_PERSIST_PROP = "persist.sys.random_bootanimation";
    private static final String RANDOMBOOTANIMATION_AVAILABLE_PROP = "sys.random_bootanimation_disabled";

    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;
    private SwitchPreference mRandomBootanimation;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.bootleg_dumpster_misc);

        Preference DeviceExtras = findPreference(DEVICE_CATEGORY);
        if (!getResources().getBoolean(R.bool.has_device_extras)) {
            getPreferenceScreen().removePreference(DeviceExtras);
        }

        Preference RandomBootanimation = findPreference(RANDOMBOOTANIMATION_PREF);
        String disableRandomBootanimationProp = SystemProperties.get(RANDOMBOOTANIMATION_AVAILABLE_PROP);
        mRandomBootanimation = (SwitchPreference) findPreference(RANDOMBOOTANIMATION_PREF);
        if (disableRandomBootanimationProp.equals("1")) {
            mRandomBootanimation.setVisible(false);
        } else {
            mRandomBootanimation.setOnPreferenceChangeListener(this);
        }

        if (!Utils.isPackageInstalled(getActivity(), DEVICE_OMNI_PACKAGE)) {
            getPreferenceScreen().removePreference(findPreference(DEVICE_OMNI_CATEGORY));
        }

        final PreferenceCategory aspectRatioCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
        final boolean supportMaxAspectRatio = getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
        if (!supportMaxAspectRatio) {
            getPreferenceScreen().removePreference(aspectRatioCategory);
        } else {
        mAspectRatioAppsSelect = (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
        mAspectRatioApps = (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
        final String valuesString = Settings.System.getString(getContentResolver(), Settings.System.OMNI_ASPECT_RATIO_APPS_LIST);
        List<String> valuesList = new ArrayList<String>();
        if (!TextUtils.isEmpty(valuesString)) {
             valuesList.addAll(Arrays.asList(valuesString.split(":")));
             mAspectRatioApps.setVisible(true);
             mAspectRatioApps.setValues(valuesList);
        } else {
             mAspectRatioApps.setVisible(false);
        }
        mAspectRatioAppsSelect.setValues(valuesList);
        mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAspectRatioAppsSelect) {
            Collection<String> valueList = (Collection<String>) objValue;
            mAspectRatioApps.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(), Settings.System.OMNI_ASPECT_RATIO_APPS_LIST,
                        TextUtils.join(":", valueList));
                mAspectRatioApps.setVisible(true);
                mAspectRatioApps.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(), Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, "");
            }
            return true;
        } else if (preference == mRandomBootanimation) {
            if (objValue != null) {
                int toggledInt = (Boolean) objValue ? 1 : 0;
                SystemProperties.set(RANDOMBOOTANIMATION_PERSIST_PROP, String.valueOf(toggledInt));
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BOOTLEG;
    }
}
