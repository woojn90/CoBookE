package com.android.woojn.coursebookmarkapplication.fragment;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_FOLDER_ID;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Folder;

import io.realm.Realm;

/**
 * Created by wjn on 2017-02-07.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);

        // ListPreference Summary 추가
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int preferenceCategoryCount = preferenceScreen.getPreferenceCount();

        for (int i = 0 ; i < preferenceCategoryCount; i++) {
            Preference preferenceCategory = preferenceScreen.getPreference(i);
            int count = ((PreferenceCategory) preferenceCategory).getPreferenceCount();

            for (int j = 0 ; j < count ; j++) {
                Preference preference = ((PreferenceCategory) preferenceCategory).getPreference(j);
                if (preference instanceof ListPreference) {
                    String value = sharedPreferences.getString(preference.getKey(), "");
                    setPreferenceSummary(preference, value);
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // ListPreference Summary 변경 적용
        Preference preference = findPreference(key);
        if (preference != null) {
            if (preference instanceof ListPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    private void setPreferenceSummary(Preference preference, String value) {
        ListPreference listPreference = (ListPreference) preference;
        int prefIndex = listPreference.findIndexOfValue(value);
        if (prefIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[prefIndex]);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        Preference prefDeleteAll = findPreference(getString(R.string.settings_key_delete_all));
        Preference prefDownloadBackup= findPreference(getString(R.string.settings_key_download_backup_file));
        Preference prefUploadBackup= findPreference(getString(R.string.settings_key_upload_backup_file));
        Preference prefSendMail = findPreference(getString(R.string.settings_key_send_mail));
        Preference prefLicense = findPreference(getString(R.string.settings_key_license));
        prefDeleteAll.setOnPreferenceClickListener(this);
        prefDownloadBackup.setOnPreferenceClickListener(this);
        prefUploadBackup.setOnPreferenceClickListener(this);
        prefSendMail.setOnPreferenceClickListener(this);
        prefLicense.setOnPreferenceClickListener(this);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (getString(R.string.settings_key_delete_all).equals(preference.getKey())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.msg_delete_all_confirm);
            builder.setNegativeButton(R.string.string_cancel, null);
            builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.deleteAll();
                    realm.createObject(Folder.class, DEFAULT_FOLDER_ID);
                    realm.commitTransaction();
                    realm.close();
                    Toast.makeText(getContext(), R.string.msg_delete_all_complete, Toast.LENGTH_LONG).show();
                }
            });
            builder.show();

        } else if (getString(R.string.settings_key_download_backup_file).equals(preference.getKey())) {
            // TODO: 백업 내보내기 구현

        } else if (getString(R.string.settings_key_upload_backup_file).equals(preference.getKey())) {
            // TODO: 백업 가져오기 구현

        } else if (getString(R.string.settings_key_send_mail).equals(preference.getKey())) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.settings_developer_mail_address));
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_developer_mail_subject));
            startActivity(Intent.createChooser(sendIntent, getString(R.string.settings_title_send_mail)));

        } else if (getString(R.string.settings_key_license).equals(preference.getKey())) {

        }
        return false;
    }
}