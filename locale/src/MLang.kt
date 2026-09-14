package dev.oom_wg.purejoy.mlang

import android.content.Context
import android.content.res.Configuration
import com.amamiyakokoro.box.core.locale.R
import java.util.Locale

object MLang {
    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var activeLocale: Locale = Locale.getDefault()

    @Volatile
    private var localizedContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        refreshLocalizedContext()
    }

    fun updateLocale(locale: Locale) {
        activeLocale = locale
        refreshLocalizedContext()
    }

    private fun refreshLocalizedContext() {
        val base = appContext ?: return
        val configuration = Configuration(base.resources.configuration).apply {
            setLocale(activeLocale)
            setLayoutDirection(activeLocale)
        }
        localizedContext = base.createConfigurationContext(configuration)
    }

    private fun text(key: String): String {
        val resourceId = RESOURCE_IDS[key] ?: return key
        return localizedContext?.getString(resourceId) ?: key
    }

    private val RESOURCE_IDS = mapOf(
        "About.App.Description" to R.string.about_app_description,
        "About.App.VersionFailed" to R.string.about_app_version_failed,
        "About.App.VersionLoading" to R.string.about_app_version_loading,
        "About.Copyright" to R.string.about_copyright,
        "About.License.AgplDescription" to R.string.about_license_agpl_description,
        "About.License.AgplName" to R.string.about_license_agpl_name,
        "About.License.CheckUpdate" to R.string.about_license_check_update,
        "About.License.CheckUpdateSummary" to R.string.about_license_check_update_summary,
        "About.License.Libraries" to R.string.about_license_libraries,
        "About.License.LibrariesSummary" to R.string.about_license_libraries_summary,
        "About.Section.License" to R.string.about_section_license,
        "About.Section.ProjectLinks" to R.string.about_section_project_links,
        "About.Title" to R.string.about_title,
        "About.Update.Available" to R.string.about_update_available,
        "About.Update.BrowserDownload" to R.string.about_update_browser_download,
        "About.Update.Checking" to R.string.about_update_checking,
        "About.Update.ContinueInstall" to R.string.about_update_continue_install,
        "About.Update.Download" to R.string.about_update_download,
        "About.Update.Downloading" to R.string.about_update_downloading,
        "About.Update.InAppDownload" to R.string.about_update_in_app_download,
        "About.Update.InAppDownloadSummary" to R.string.about_update_in_app_download_summary,
        "About.Update.InstallPermissionRequired" to R.string.about_update_install_permission_required,
        "About.Update.Installed" to R.string.about_update_installed,
        "About.Update.InvalidResponse" to R.string.about_update_invalid_response,
        "About.Update.NetworkError" to R.string.about_update_network_error,
        "About.Update.NoApk" to R.string.about_update_no_apk,
        "About.Update.NoBrowser" to R.string.about_update_no_browser,
        "About.Update.NoRelease" to R.string.about_update_no_release,
        "About.Update.Ok" to R.string.about_update_ok,
        "About.Update.OpenInstallSettings" to R.string.about_update_open_install_settings,
        "About.Update.OpenRelease" to R.string.about_update_open_release,
        "About.Update.PreparingInstall" to R.string.about_update_preparing_install,
        "About.Update.RateLimited" to R.string.about_update_rate_limited,
        "About.Update.Retry" to R.string.about_update_retry,
        "About.Update.UnknownVersion" to R.string.about_update_unknown_version,
        "About.Update.UpToDate" to R.string.about_update_up_to_date,
        "About.Update.UpdateFailed" to R.string.about_update_update_failed,
        "About.Update.Verifying" to R.string.about_update_verifying,
        "About.Update.WaitingForInstallConfirmation" to R.string.about_update_waiting_for_install_confirmation,
        "AccessControl.AppList.Loading" to R.string.access_control_app_list_loading,
        "AccessControl.AppList.Title" to R.string.access_control_app_list_title,
        "AccessControl.Button.Cancel" to R.string.access_control_button_cancel,
        "AccessControl.Button.Confirm" to R.string.access_control_button_confirm,
        "AccessControl.Search.Empty" to R.string.access_control_search_empty,
        "AccessControl.Search.Placeholder" to R.string.access_control_search_placeholder,
        "AccessControl.Settings.BatchOperation" to R.string.access_control_settings_batch_operation,
        "AccessControl.Settings.ChinaApps" to R.string.access_control_settings_china_apps,
        "AccessControl.Settings.DescendingOrder" to R.string.access_control_settings_descending_order,
        "AccessControl.Settings.DeselectAll" to R.string.access_control_settings_deselect_all,
        "AccessControl.Settings.Export" to R.string.access_control_settings_export,
        "AccessControl.Settings.ExportSuccess" to R.string.access_control_settings_export_success,
        "AccessControl.Settings.Import" to R.string.access_control_settings_import,
        "AccessControl.Settings.ImportExport" to R.string.access_control_settings_import_export,
        "AccessControl.Settings.ImportFailed" to R.string.access_control_settings_import_failed,
        "AccessControl.Settings.ImportSuccess" to R.string.access_control_settings_import_success,
        "AccessControl.Settings.Invert" to R.string.access_control_settings_invert,
        "AccessControl.Settings.OverseasApps" to R.string.access_control_settings_overseas_apps,
        "AccessControl.Settings.RegionQuickSelect" to R.string.access_control_settings_region_quick_select,
        "AccessControl.Settings.RegionSelectResult" to R.string.access_control_settings_region_select_result,
        "AccessControl.Settings.SelectAction" to R.string.access_control_settings_select_action,
        "AccessControl.Settings.SelectAll" to R.string.access_control_settings_select_all,
        "AccessControl.Settings.SelectedFirst" to R.string.access_control_settings_selected_first,
        "AccessControl.Settings.ShowSystemApps" to R.string.access_control_settings_show_system_apps,
        "AccessControl.Settings.SortMode" to R.string.access_control_settings_sort_mode,
        "AccessControl.Settings.SortModeCurrent" to R.string.access_control_settings_sort_mode_current,
        "AccessControl.Settings.Title" to R.string.access_control_settings_title,
        "AccessControl.SortMode.InstallTime" to R.string.access_control_sort_mode_install_time,
        "AccessControl.SortMode.Label" to R.string.access_control_sort_mode_label,
        "AccessControl.SortMode.PackageName" to R.string.access_control_sort_mode_package_name,
        "AccessControl.SortMode.UpdateTime" to R.string.access_control_sort_mode_update_time,
        "AccessControl.Title" to R.string.access_control_title,
        "AppDataManagement.GeoFiles.CacheItemSummary" to R.string.app_data_management_geo_files_cache_item_summary,
        "AppDataManagement.GeoFiles.DeleteComplete" to R.string.app_data_management_geo_files_delete_complete,
        "AppDataManagement.GeoFiles.DeleteConfirmMessage" to R.string.app_data_management_geo_files_delete_confirm_message,
        "AppDataManagement.GeoFiles.DeleteConfirmTitle" to R.string.app_data_management_geo_files_delete_confirm_title,
        "AppDataManagement.GeoFiles.EmptyHistory" to R.string.app_data_management_geo_files_empty_history,
        "AppDataManagement.GeoFiles.EmptyHistorySummary" to R.string.app_data_management_geo_files_empty_history_summary,
        "AppDataManagement.GeoFiles.HistorySummary" to R.string.app_data_management_geo_files_history_summary,
        "AppDataManagement.GeoFiles.HistoryTitle" to R.string.app_data_management_geo_files_history_title,
        "AppDataManagement.Logs.DeleteComplete" to R.string.app_data_management_logs_delete_complete,
        "AppDataManagement.Logs.DeleteConfirmMessage" to R.string.app_data_management_logs_delete_confirm_message,
        "AppDataManagement.Logs.DeleteConfirmTitle" to R.string.app_data_management_logs_delete_confirm_title,
        "AppDataManagement.Logs.EmptyLogContent" to R.string.app_data_management_logs_empty_log_content,
        "AppDataManagement.Logs.EmptyLogContentSummary" to R.string.app_data_management_logs_empty_log_content_summary,
        "AppDataManagement.Logs.EmptyLogs" to R.string.app_data_management_logs_empty_logs,
        "AppDataManagement.Logs.EmptyLogsSummary" to R.string.app_data_management_logs_empty_logs_summary,
        "AppDataManagement.Logs.LogItemSummary" to R.string.app_data_management_logs_log_item_summary,
        "AppDataManagement.Logs.LogLineTitle" to R.string.app_data_management_logs_log_line_title,
        "AppDataManagement.Logs.ManagementSummary" to R.string.app_data_management_logs_management_summary,
        "AppDataManagement.Logs.ManagementTitle" to R.string.app_data_management_logs_management_title,
        "AppDataManagement.Logs.RecordingFileTitle" to R.string.app_data_management_logs_recording_file_title,
        "AppDataManagement.Logs.ViewerLimitHint" to R.string.app_data_management_logs_viewer_limit_hint,
        "AppDataManagement.Logs.ViewerLimitSummary" to R.string.app_data_management_logs_viewer_limit_summary,
        "AppDataManagement.Logs.ViewerTitle" to R.string.app_data_management_logs_viewer_title,
        "AppDataManagement.Section.GeoFiles" to R.string.app_data_management_section_geo_files,
        "AppDataManagement.Section.Logs" to R.string.app_data_management_section_logs,
        "AppDataManagement.Title" to R.string.app_data_management_title,
        "AppSettings.Backup.ExportFailed" to R.string.app_settings_backup_export_failed,
        "AppSettings.Backup.ExportFailedDetail" to R.string.app_settings_backup_export_failed_detail,
        "AppSettings.Backup.ExportSuccess" to R.string.app_settings_backup_export_success,
        "AppSettings.Backup.ExportSummary" to R.string.app_settings_backup_export_summary,
        "AppSettings.Backup.ExportTitle" to R.string.app_settings_backup_export_title,
        "AppSettings.Backup.ImportFailedDetail" to R.string.app_settings_backup_import_failed_detail,
        "AppSettings.Backup.ImportReadFailed" to R.string.app_settings_backup_import_read_failed,
        "AppSettings.Backup.ImportSuccess" to R.string.app_settings_backup_import_success,
        "AppSettings.Backup.ImportSummary" to R.string.app_settings_backup_import_summary,
        "AppSettings.Backup.ImportTitle" to R.string.app_settings_backup_import_title,
        "AppSettings.Behavior.AutoStartSummary" to R.string.app_settings_behavior_auto_start_summary,
        "AppSettings.Behavior.AutoStartTitle" to R.string.app_settings_behavior_auto_start_title,
        "AppSettings.Behavior.AutoUpdateOnStartSummary" to R.string.app_settings_behavior_auto_update_on_start_summary,
        "AppSettings.Behavior.AutoUpdateOnStartTitle" to R.string.app_settings_behavior_auto_update_on_start_title,
        "AppSettings.Behavior.AutomaticUpdateCheckSummary" to R.string.app_settings_behavior_automatic_update_check_summary,
        "AppSettings.Behavior.AutomaticUpdateCheckTitle" to R.string.app_settings_behavior_automatic_update_check_title,
        "AppSettings.Behavior.UpdateChannelNightly" to R.string.app_settings_behavior_update_channel_nightly,
        "AppSettings.Behavior.UpdateChannelStable" to R.string.app_settings_behavior_update_channel_stable,
        "AppSettings.Behavior.UpdateChannelSummary" to R.string.app_settings_behavior_update_channel_summary,
        "AppSettings.Behavior.UpdateChannelTitle" to R.string.app_settings_behavior_update_channel_title,
        "AppSettings.Behavior.UpdateInstallMethodRoot" to R.string.app_settings_behavior_update_install_method_root,
        "AppSettings.Behavior.UpdateInstallMethodShizuku" to R.string.app_settings_behavior_update_install_method_shizuku,
        "AppSettings.Behavior.UpdateInstallMethodSummary" to R.string.app_settings_behavior_update_install_method_summary,
        "AppSettings.Behavior.UpdateInstallMethodSystem" to R.string.app_settings_behavior_update_install_method_system,
        "AppSettings.Behavior.UpdateInstallMethodTitle" to R.string.app_settings_behavior_update_install_method_title,
        "AppSettings.Button.Apply" to R.string.app_settings_button_apply,
        "AppSettings.Experimental.AcgHomeSummary" to R.string.app_settings_experimental_acg_home_summary,
        "AppSettings.Experimental.AcgHomeTitle" to R.string.app_settings_experimental_acg_home_title,
        "AppSettings.Experimental.AcgSidebarExpandedSummary" to R.string.app_settings_experimental_acg_sidebar_expanded_summary,
        "AppSettings.Experimental.AcgSidebarExpandedTitle" to R.string.app_settings_experimental_acg_sidebar_expanded_title,
        "AppSettings.Experimental.HealthCheckConcurrencySummary" to R.string.app_settings_experimental_health_check_concurrency_summary,
        "AppSettings.Experimental.HealthCheckConcurrencyTitle" to R.string.app_settings_experimental_health_check_concurrency_title,
        "AppSettings.Experimental.ResetWallpaperSuccess" to R.string.app_settings_experimental_reset_wallpaper_success,
        "AppSettings.Experimental.ResetWallpaperSummary" to R.string.app_settings_experimental_reset_wallpaper_summary,
        "AppSettings.Experimental.ResetWallpaperTitle" to R.string.app_settings_experimental_reset_wallpaper_title,
        "AppSettings.Experimental.WallpaperSummary" to R.string.app_settings_experimental_wallpaper_summary,
        "AppSettings.Experimental.WallpaperTitle" to R.string.app_settings_experimental_wallpaper_title,
        "AppSettings.Interface.AutoHideNavbarSummary" to R.string.app_settings_interface_auto_hide_navbar_summary,
        "AppSettings.Interface.AutoHideNavbarTitle" to R.string.app_settings_interface_auto_hide_navbar_title,
        "AppSettings.Interface.ColorThemeAcgWallpaperSummary" to R.string.app_settings_interface_color_theme_acg_wallpaper_summary,
        "AppSettings.Interface.ColorThemeCodeLabel" to R.string.app_settings_interface_color_theme_code_label,
        "AppSettings.Interface.ColorThemeCustomSummary" to R.string.app_settings_interface_color_theme_custom_summary,
        "AppSettings.Interface.ColorThemeDynamicSummary" to R.string.app_settings_interface_color_theme_dynamic_summary,
        "AppSettings.Interface.ColorThemeModeAcgWallpaper" to R.string.app_settings_interface_color_theme_mode_acg_wallpaper,
        "AppSettings.Interface.ColorThemeModeCustom" to R.string.app_settings_interface_color_theme_mode_custom,
        "AppSettings.Interface.ColorThemeModeMonet" to R.string.app_settings_interface_color_theme_mode_monet,
        "AppSettings.Interface.ColorThemeModeSummary" to R.string.app_settings_interface_color_theme_mode_summary,
        "AppSettings.Interface.ColorThemeModeTitle" to R.string.app_settings_interface_color_theme_mode_title,
        "AppSettings.Interface.ColorThemePickerTitle" to R.string.app_settings_interface_color_theme_picker_title,
        "AppSettings.Interface.ColorThemeTitle" to R.string.app_settings_interface_color_theme_title,
        "AppSettings.Interface.HomeControlFabSummary" to R.string.app_settings_interface_home_control_fab_summary,
        "AppSettings.Interface.HomeControlFabTitle" to R.string.app_settings_interface_home_control_fab_title,
        "AppSettings.Interface.LanguageChinese" to R.string.app_settings_interface_language_chinese,
        "AppSettings.Interface.LanguageEnglish" to R.string.app_settings_interface_language_english,
        "AppSettings.Interface.LanguageSummary" to R.string.app_settings_interface_language_summary,
        "AppSettings.Interface.LanguageSystem" to R.string.app_settings_interface_language_system,
        "AppSettings.Interface.LanguageTitle" to R.string.app_settings_interface_language_title,
        "AppSettings.Interface.LanguageTraditionalChinese" to R.string.app_settings_interface_language_traditional_chinese,
        "AppSettings.Interface.LegacyNavbarStyleSummary" to R.string.app_settings_interface_legacy_navbar_style_summary,
        "AppSettings.Interface.LegacyNavbarStyleTitle" to R.string.app_settings_interface_legacy_navbar_style_title,
        "AppSettings.Interface.PageScaleDialogSummary" to R.string.app_settings_interface_page_scale_dialog_summary,
        "AppSettings.Interface.PageScaleSummary" to R.string.app_settings_interface_page_scale_summary,
        "AppSettings.Interface.PageScaleTitle" to R.string.app_settings_interface_page_scale_title,
        "AppSettings.Interface.ThemeColorPolarityInvertSummary" to R.string.app_settings_interface_theme_color_polarity_invert_summary,
        "AppSettings.Interface.ThemeColorPolarityInvertTitle" to R.string.app_settings_interface_theme_color_polarity_invert_title,
        "AppSettings.Interface.ThemeModeDark" to R.string.app_settings_interface_theme_mode_dark,
        "AppSettings.Interface.ThemeModeLight" to R.string.app_settings_interface_theme_mode_light,
        "AppSettings.Interface.ThemeModeSummary" to R.string.app_settings_interface_theme_mode_summary,
        "AppSettings.Interface.ThemeModeSystem" to R.string.app_settings_interface_theme_mode_system,
        "AppSettings.Interface.ThemeModeTitle" to R.string.app_settings_interface_theme_mode_title,
        "AppSettings.Privacy.BiometricDialogTitleDisable" to R.string.app_settings_privacy_biometric_dialog_title_disable,
        "AppSettings.Privacy.BiometricDialogTitleEnable" to R.string.app_settings_privacy_biometric_dialog_title_enable,
        "AppSettings.Privacy.BiometricExitButton" to R.string.app_settings_privacy_biometric_exit_button,
        "AppSettings.Privacy.BiometricPromptMessage" to R.string.app_settings_privacy_biometric_prompt_message,
        "AppSettings.Privacy.BiometricPromptTitle" to R.string.app_settings_privacy_biometric_prompt_title,
        "AppSettings.Privacy.BiometricRetryButton" to R.string.app_settings_privacy_biometric_retry_button,
        "AppSettings.Privacy.BiometricUnavailableHwUnavailable" to R.string.app_settings_privacy_biometric_unavailable_hw_unavailable,
        "AppSettings.Privacy.BiometricUnavailableMessage" to R.string.app_settings_privacy_biometric_unavailable_message,
        "AppSettings.Privacy.BiometricUnavailableNoDeviceCredential" to R.string.app_settings_privacy_biometric_unavailable_no_device_credential,
        "AppSettings.Privacy.BiometricUnavailableNoHardware" to R.string.app_settings_privacy_biometric_unavailable_no_hardware,
        "AppSettings.Privacy.BiometricUnavailableNoneEnrolled" to R.string.app_settings_privacy_biometric_unavailable_none_enrolled,
        "AppSettings.Privacy.BiometricUnavailableTitle" to R.string.app_settings_privacy_biometric_unavailable_title,
        "AppSettings.Privacy.BiometricUnlockSummary" to R.string.app_settings_privacy_biometric_unlock_summary,
        "AppSettings.Privacy.BiometricUnlockTitle" to R.string.app_settings_privacy_biometric_unlock_title,
        "AppSettings.Privacy.HideFromRecentsSummary" to R.string.app_settings_privacy_hide_from_recents_summary,
        "AppSettings.Privacy.HideFromRecentsTitle" to R.string.app_settings_privacy_hide_from_recents_title,
        "AppSettings.Privacy.HideIconSummary" to R.string.app_settings_privacy_hide_icon_summary,
        "AppSettings.Privacy.HideIconTitle" to R.string.app_settings_privacy_hide_icon_title,
        "AppSettings.Privacy.ScreenshotDialogTitleDisable" to R.string.app_settings_privacy_screenshot_dialog_title_disable,
        "AppSettings.Privacy.ScreenshotDialogTitleEnable" to R.string.app_settings_privacy_screenshot_dialog_title_enable,
        "AppSettings.Privacy.ScreenshotProtectionSummary" to R.string.app_settings_privacy_screenshot_protection_summary,
        "AppSettings.Privacy.ScreenshotProtectionTitle" to R.string.app_settings_privacy_screenshot_protection_title,
        "AppSettings.Section.Backup" to R.string.app_settings_section_backup,
        "AppSettings.Section.Behavior" to R.string.app_settings_section_behavior,
        "AppSettings.Section.Experimental" to R.string.app_settings_section_experimental,
        "AppSettings.Section.Interface" to R.string.app_settings_section_interface,
        "AppSettings.Section.Privacy" to R.string.app_settings_section_privacy,
        "AppSettings.Section.Service" to R.string.app_settings_section_service,
        "AppSettings.ServiceSection.BatteryOptimizationTitle" to R.string.app_settings_service_section_battery_optimization_title,
        "AppSettings.ServiceSection.ExitUiWhenBackgroundSummary" to R.string.app_settings_service_section_exit_ui_when_background_summary,
        "AppSettings.ServiceSection.ExitUiWhenBackgroundTitle" to R.string.app_settings_service_section_exit_ui_when_background_title,
        "AppSettings.ServiceSection.SingleNodeTestSummary" to R.string.app_settings_service_section_single_node_test_summary,
        "AppSettings.ServiceSection.SingleNodeTestTitle" to R.string.app_settings_service_section_single_node_test_title,
        "AppSettings.ServiceSection.TrafficNotificationSummary" to R.string.app_settings_service_section_traffic_notification_summary,
        "AppSettings.ServiceSection.TrafficNotificationTitle" to R.string.app_settings_service_section_traffic_notification_title,
        "AppSettings.Title" to R.string.app_settings_title,
        "AppSettings.WarningDialog.HideIconMsg1" to R.string.app_settings_warning_dialog_hide_icon_msg1,
        "AppSettings.WarningDialog.HideIconMsg2" to R.string.app_settings_warning_dialog_hide_icon_msg2,
        "AppSettings.WarningDialog.Title" to R.string.app_settings_warning_dialog_title,
        "Component.BottomBar.Config" to R.string.component_bottom_bar_config,
        "Component.BottomBar.Home" to R.string.component_bottom_bar_home,
        "Component.BottomBar.Proxy" to R.string.component_bottom_bar_proxy,
        "Component.BottomBar.Setting" to R.string.component_bottom_bar_setting,
        "Component.Button.Cancel" to R.string.component_button_cancel,
        "Component.Button.Clear" to R.string.component_button_clear,
        "Component.Button.Confirm" to R.string.component_button_confirm,
        "Component.Button.Copy" to R.string.component_button_copy,
        "Component.Button.Delete" to R.string.component_button_delete,
        "Component.Button.Ok" to R.string.component_button_ok,
        "Component.ConfigInput.CountItems" to R.string.component_config_input_count_items,
        "Component.ConfigInput.PortLabel" to R.string.component_config_input_port_label,
        "Component.Editor.Action.Add" to R.string.component_editor_action_add,
        "Component.Editor.Action.Delete" to R.string.component_editor_action_delete,
        "Component.Editor.Action.Reset" to R.string.component_editor_action_reset,
        "Component.Editor.Action.Search" to R.string.component_editor_action_search,
        "Component.Editor.CountItems" to R.string.component_editor_count_items,
        "Component.Editor.Dialog.AddTitle" to R.string.component_editor_dialog_add_title,
        "Component.Editor.Dialog.EditTitle" to R.string.component_editor_dialog_edit_title,
        "Component.Editor.Dialog.ResetMessage" to R.string.component_editor_dialog_reset_message,
        "Component.Editor.Dialog.ResetTitle" to R.string.component_editor_dialog_reset_title,
        "Component.Editor.Empty.Hint" to R.string.component_editor_empty_hint,
        "Component.Editor.Empty.Title" to R.string.component_editor_empty_title,
        "Component.Editor.Error.KeyEmpty" to R.string.component_editor_error_key_empty,
        "Component.Editor.Error.KeyExists" to R.string.component_editor_error_key_exists,
        "Component.Editor.Rule.Content" to R.string.component_editor_rule_content,
        "Component.Editor.Rule.ErrorContentRequired" to R.string.component_editor_rule_error_content_required,
        "Component.Editor.Rule.ErrorTargetRequired" to R.string.component_editor_rule_error_target_required,
        "Component.Editor.Rule.NoResolve" to R.string.component_editor_rule_no_resolve,
        "Component.Editor.Rule.Src" to R.string.component_editor_rule_src,
        "Component.Editor.Rule.Target" to R.string.component_editor_rule_target,
        "Component.Editor.Rule.TargetDirect" to R.string.component_editor_rule_target_direct,
        "Component.Editor.Rule.TargetMatch" to R.string.component_editor_rule_target_match,
        "Component.Editor.Rule.TargetReject" to R.string.component_editor_rule_target_reject,
        "Component.Editor.Rule.Type" to R.string.component_editor_rule_type,
        "Component.Flag.ContentDescription" to R.string.component_flag_content_description,
        "Component.Loading.Starting" to R.string.component_loading_starting,
        "Component.Message.Confirm" to R.string.component_message_confirm,
        "Component.Message.Error" to R.string.component_message_error,
        "Component.Message.Hint" to R.string.component_message_hint,
        "Component.Message.Success" to R.string.component_message_success,
        "Component.Navigation.Back" to R.string.component_navigation_back,
        "Component.Navigation.Refresh" to R.string.component_navigation_refresh,
        "Component.ProfileCard.ClickToUpdate" to R.string.component_profile_card_click_to_update,
        "Component.ProfileCard.DaysAgo" to R.string.component_profile_card_days_ago,
        "Component.ProfileCard.Delete" to R.string.component_profile_card_delete,
        "Component.ProfileCard.Edit" to R.string.component_profile_card_edit,
        "Component.ProfileCard.ExpireAt" to R.string.component_profile_card_expire_at,
        "Component.ProfileCard.ExpireToday" to R.string.component_profile_card_expire_today,
        "Component.ProfileCard.Expired" to R.string.component_profile_card_expired,
        "Component.ProfileCard.Export" to R.string.component_profile_card_export,
        "Component.ProfileCard.HoursAgo" to R.string.component_profile_card_hours_ago,
        "Component.ProfileCard.JustNow" to R.string.component_profile_card_just_now,
        "Component.ProfileCard.LocalConfig" to R.string.component_profile_card_local_config,
        "Component.ProfileCard.LocalFile" to R.string.component_profile_card_local_file,
        "Component.ProfileCard.MinutesAgo" to R.string.component_profile_card_minutes_ago,
        "Component.ProfileCard.RemoteSubscription" to R.string.component_profile_card_remote_subscription,
        "Component.ProfileCard.Traffic" to R.string.component_profile_card_traffic,
        "Component.ProfileCard.Update" to R.string.component_profile_card_update,
        "Component.ProfileCard.UsedTraffic" to R.string.component_profile_card_used_traffic,
        "Component.Selector.Append" to R.string.component_selector_append,
        "Component.Selector.Disable" to R.string.component_selector_disable,
        "Component.Selector.Enable" to R.string.component_selector_enable,
        "Component.Selector.Merge" to R.string.component_selector_merge,
        "Component.Selector.NotModify" to R.string.component_selector_not_modify,
        "Component.Selector.Prepend" to R.string.component_selector_prepend,
        "Component.Selector.Replace" to R.string.component_selector_replace,
        "Component.Update.Action.DownloadNow" to R.string.component_update_action_download_now,
        "Component.Update.Message.Available" to R.string.component_update_message_available,
        "Component.Update.Message.CheckFailed" to R.string.component_update_message_check_failed,
        "Component.Update.Message.Checking" to R.string.component_update_message_checking,
        "Component.Update.Message.Close" to R.string.component_update_message_close,
        "Component.Update.Message.CoverDesc" to R.string.component_update_message_cover_desc,
        "Component.Update.Message.CurrentVersion" to R.string.component_update_message_current_version,
        "Component.Update.Message.DownloadAlreadyRunning" to R.string.component_update_message_download_already_running,
        "Component.Update.Message.DownloadErrorWithCode" to R.string.component_update_message_download_error_with_code,
        "Component.Update.Message.DownloadReady" to R.string.component_update_message_download_ready,
        "Component.Update.Message.Downloading" to R.string.component_update_message_downloading,
        "Component.Update.Message.DownloadingWithProgress" to R.string.component_update_message_downloading_with_progress,
        "Component.Update.Message.Error" to R.string.component_update_message_error,
        "Component.Update.Message.Finished" to R.string.component_update_message_finished,
        "Component.Update.Message.InstallFailed" to R.string.component_update_message_install_failed,
        "Component.Update.Message.InstallPromptOpened" to R.string.component_update_message_install_prompt_opened,
        "Component.Update.Message.MissingReleaseMetadata" to R.string.component_update_message_missing_release_metadata,
        "Component.Update.Message.NoCompatibleAsset" to R.string.component_update_message_no_compatible_asset,
        "Component.Update.Message.NoUpdate" to R.string.component_update_message_no_update,
        "Component.Update.Message.Preparing" to R.string.component_update_message_preparing,
        "Component.Update.Message.RemoteVersion" to R.string.component_update_message_remote_version,
        "Component.Update.Message.Updating" to R.string.component_update_message_updating,
        "Component.Update.Message.VerifyFailed" to R.string.component_update_message_verify_failed,
        "Component.Update.Message.Verifying" to R.string.component_update_message_verifying,
        "Component.Update.Message.Waiting" to R.string.component_update_message_waiting,
        "Component.Update.Title.Available" to R.string.component_update_title_available,
        "Component.WebView.InvalidUrl" to R.string.component_web_view_invalid_url,
        "Connection.ChainCount" to R.string.connection_chain_count,
        "Connection.Detail.Action.Interrupt" to R.string.connection_detail_action_interrupt,
        "Connection.Detail.Action.Interrupting" to R.string.connection_detail_action_interrupting,
        "Connection.Detail.Label.Content" to R.string.connection_detail_label_content,
        "Connection.Detail.Label.DestinationAddress" to R.string.connection_detail_label_destination_address,
        "Connection.Detail.Label.Download" to R.string.connection_detail_label_download,
        "Connection.Detail.Label.Duration" to R.string.connection_detail_label_duration,
        "Connection.Detail.Label.Process" to R.string.connection_detail_label_process,
        "Connection.Detail.Label.Protocol" to R.string.connection_detail_label_protocol,
        "Connection.Detail.Label.SourceAddress" to R.string.connection_detail_label_source_address,
        "Connection.Detail.Label.Type" to R.string.connection_detail_label_type,
        "Connection.Detail.Label.Upload" to R.string.connection_detail_label_upload,
        "Connection.Detail.Section.Info" to R.string.connection_detail_section_info,
        "Connection.Detail.Section.Rule" to R.string.connection_detail_section_rule,
        "Connection.Empty" to R.string.connection_empty,
        "Connection.Loading" to R.string.connection_loading,
        "Connection.NoResults" to R.string.connection_no_results,
        "Connection.RelativeTime.Date" to R.string.connection_relative_time_date,
        "Connection.RelativeTime.DaysAgo" to R.string.connection_relative_time_days_ago,
        "Connection.RelativeTime.HoursAgo" to R.string.connection_relative_time_hours_ago,
        "Connection.RelativeTime.JustNow" to R.string.connection_relative_time_just_now,
        "Connection.RelativeTime.MinutesAgo" to R.string.connection_relative_time_minutes_ago,
        "Connection.Search" to R.string.connection_search,
        "Connection.SearchHint" to R.string.connection_search_hint,
        "Connection.Sort.Download" to R.string.connection_sort_download,
        "Connection.Sort.Host" to R.string.connection_sort_host,
        "Connection.Sort.Time" to R.string.connection_sort_time,
        "Connection.Sort.Upload" to R.string.connection_sort_upload,
        "Connection.SortBy" to R.string.connection_sort_by,
        "Connection.Summary" to R.string.connection_summary,
        "Connection.Tab.Active" to R.string.connection_tab_active,
        "Connection.Tab.Closed" to R.string.connection_tab_closed,
        "Connection.Title" to R.string.connection_title,
        "Editor.Action.Discard" to R.string.editor_action_discard,
        "Editor.Action.Format" to R.string.editor_action_format,
        "Editor.Action.Save" to R.string.editor_action_save,
        "Editor.Common.ConfigPreviewTitle" to R.string.editor_common_config_preview_title,
        "Editor.Common.EditConfigTitle" to R.string.editor_common_edit_config_title,
        "Editor.Common.EditOverrideConfigTitle" to R.string.editor_common_edit_override_config_title,
        "Editor.Common.EditProfileConfigTitle" to R.string.editor_common_edit_profile_config_title,
        "Editor.Common.JsonSubtitle" to R.string.editor_common_json_subtitle,
        "Editor.Diagnostic.DuplicateKey" to R.string.editor_diagnostic_duplicate_key,
        "Editor.Diagnostic.Expected" to R.string.editor_diagnostic_expected,
        "Editor.Diagnostic.JsonFormatError" to R.string.editor_diagnostic_json_format_error,
        "Editor.Diagnostic.JsonMustStartWithObjectOrArray" to R.string.editor_diagnostic_json_must_start_with_object_or_array,
        "Editor.Diagnostic.JsonSyntaxError" to R.string.editor_diagnostic_json_syntax_error,
        "Editor.Diagnostic.NoValue" to R.string.editor_diagnostic_no_value,
        "Editor.Diagnostic.Unknown" to R.string.editor_diagnostic_unknown,
        "Editor.Diagnostic.Unterminated" to R.string.editor_diagnostic_unterminated,
        "Editor.Dialog.DiscardTitle" to R.string.editor_dialog_discard_title,
        "Editor.Dialog.UnsavedChangesMessage" to R.string.editor_dialog_unsaved_changes_message,
        "Editor.Dialog.UnsavedChangesTitle" to R.string.editor_dialog_unsaved_changes_title,
        "Editor.Toast.FormatFailedOrUnchanged" to R.string.editor_toast_format_failed_or_unchanged,
        "Editor.Toast.FormatSuccess" to R.string.editor_toast_format_success,
        "Editor.Toast.SaveFailed" to R.string.editor_toast_save_failed,
        "Editor.Toast.SyntaxError" to R.string.editor_toast_syntax_error,
        "Feature.Node.HealthCheckConcurrencySummary" to R.string.feature_node_health_check_concurrency_summary,
        "Feature.Node.HealthCheckConcurrencyTitle" to R.string.feature_node_health_check_concurrency_title,
        "Feature.Node.Section" to R.string.feature_node_section,
        "Feature.RuntimeConfig.Empty" to R.string.feature_runtime_config_empty,
        "Feature.RuntimeConfig.NotReady" to R.string.feature_runtime_config_not_ready,
        "Feature.RuntimeConfig.NotRunning" to R.string.feature_runtime_config_not_running,
        "Feature.RuntimeConfig.PreviewTitle" to R.string.feature_runtime_config_preview_title,
        "Feature.RuntimeConfig.RuntimeChanged" to R.string.feature_runtime_config_runtime_changed,
        "Feature.RuntimeConfig.Section" to R.string.feature_runtime_config_section,
        "Feature.RuntimeConfig.Summary" to R.string.feature_runtime_config_summary,
        "Feature.RuntimeConfig.Title" to R.string.feature_runtime_config_title,
        "Feature.RuntimeConfig.Unavailable" to R.string.feature_runtime_config_unavailable,
        "Feature.RuntimeConfig.UnknownProfile" to R.string.feature_runtime_config_unknown_profile,
        "Feature.SpeedTest.Cancel" to R.string.feature_speed_test_cancel,
        "Feature.SpeedTest.DataUsage" to R.string.feature_speed_test_data_usage,
        "Feature.SpeedTest.Download" to R.string.feature_speed_test_download,
        "Feature.SpeedTest.EdgeLocation" to R.string.feature_speed_test_edge_location,
        "Feature.SpeedTest.Error" to R.string.feature_speed_test_error,
        "Feature.SpeedTest.Jitter" to R.string.feature_speed_test_jitter,
        "Feature.SpeedTest.Latency" to R.string.feature_speed_test_latency,
        "Feature.SpeedTest.LocationUnknown" to R.string.feature_speed_test_location_unknown,
        "Feature.SpeedTest.Preparing" to R.string.feature_speed_test_preparing,
        "Feature.SpeedTest.PrivacyNotice" to R.string.feature_speed_test_privacy_notice,
        "Feature.SpeedTest.Section" to R.string.feature_speed_test_section,
        "Feature.SpeedTest.Start" to R.string.feature_speed_test_start,
        "Feature.SpeedTest.Summary" to R.string.feature_speed_test_summary,
        "Feature.SpeedTest.TestingDownload" to R.string.feature_speed_test_testing_download,
        "Feature.SpeedTest.TestingLatency" to R.string.feature_speed_test_testing_latency,
        "Feature.SpeedTest.TestingUpload" to R.string.feature_speed_test_testing_upload,
        "Feature.SpeedTest.Title" to R.string.feature_speed_test_title,
        "Feature.SpeedTest.Upload" to R.string.feature_speed_test_upload,
        "Feature.Title" to R.string.feature_title,
        "Home.Control.HintAddProfile" to R.string.home_control_hint_add_profile,
        "Home.Control.HintEnableProfile" to R.string.home_control_hint_enable_profile,
        "Home.Control.HintProfilesLoading" to R.string.home_control_hint_profiles_loading,
        "Home.Control.Start" to R.string.home_control_start,
        "Home.Control.Stop" to R.string.home_control_stop,
        "Home.IpInfo.ExitIp" to R.string.home_ip_info_exit_ip,
        "Home.Message.ConfigSwitchFailed" to R.string.home_message_config_switch_failed,
        "Home.Message.ConfigSwitched" to R.string.home_message_config_switched,
        "Home.Message.ControlBusy" to R.string.home_message_control_busy,
        "Home.Message.Preparing" to R.string.home_message_preparing,
        "Home.Message.StartFailed" to R.string.home_message_start_failed,
        "Home.Message.StopFailed" to R.string.home_message_stop_failed,
        "Home.Message.WaitingForVpnPermission" to R.string.home_message_waiting_for_vpn_permission,
        "Home.NodeInfo.Delay" to R.string.home_node_info_delay,
        "Home.NodeInfo.DelayValue" to R.string.home_node_info_delay_value,
        "Home.NodeInfo.Node" to R.string.home_node_info_node,
        "Home.NodeInfo.Unknown" to R.string.home_node_info_unknown,
        "Home.ProxyMode.Http" to R.string.home_proxy_mode_http,
        "Home.ProxyMode.Tun" to R.string.home_proxy_mode_tun,
        "Home.ProxyMode.Vpn" to R.string.home_proxy_mode_vpn,
        "Home.Status.Connecting" to R.string.home_status_connecting,
        "Home.Status.Disconnecting" to R.string.home_status_disconnecting,
        "Home.Status.Running" to R.string.home_status_running,
        "Home.Status.TapFabToStart" to R.string.home_status_tap_fab_to_start,
        "Home.Status.TapToStart" to R.string.home_status_tap_to_start,
        "Home.Title" to R.string.home_title,
        "Home.Traffic.DownShort" to R.string.home_traffic_down_short,
        "Home.Traffic.NoProfile" to R.string.home_traffic_no_profile,
        "Home.Traffic.UpShort" to R.string.home_traffic_up_short,
        "Log.Action.Save" to R.string.log_action_save,
        "Log.Action.StartRecording" to R.string.log_action_start_recording,
        "Log.Action.StopRecording" to R.string.log_action_stop_recording,
        "Log.Detail.WaitingLog" to R.string.log_detail_waiting_log,
        "Log.Detail.WillShowWhenGenerated" to R.string.log_detail_will_show_when_generated,
        "Log.Empty.NoLogs" to R.string.log_empty_no_logs,
        "Log.Empty.StartRecordingHint" to R.string.log_empty_start_recording_hint,
        "Log.Title" to R.string.log_title,
        "MetaFeature.AgeKey.DerivePublicKey" to R.string.meta_feature_age_key_derive_public_key,
        "MetaFeature.AgeKey.Generate" to R.string.meta_feature_age_key_generate,
        "MetaFeature.AgeKey.HybridTitle" to R.string.meta_feature_age_key_hybrid_title,
        "MetaFeature.AgeKey.PublicKey" to R.string.meta_feature_age_key_public_key,
        "MetaFeature.AgeKey.SecretKey" to R.string.meta_feature_age_key_secret_key,
        "MetaFeature.AgeKey.Section" to R.string.meta_feature_age_key_section,
        "MetaFeature.AgeKey.X25519Title" to R.string.meta_feature_age_key_x25519_title,
        "MetaFeature.CustomRules.AddRule" to R.string.meta_feature_custom_rules_add_rule,
        "MetaFeature.CustomRules.BackToKokoroSettings" to R.string.meta_feature_custom_rules_back_to_kokoro_settings,
        "MetaFeature.CustomRules.Cancel" to R.string.meta_feature_custom_rules_cancel,
        "MetaFeature.CustomRules.Confirm" to R.string.meta_feature_custom_rules_confirm,
        "MetaFeature.CustomRules.ConflictMessage" to R.string.meta_feature_custom_rules_conflict_message,
        "MetaFeature.CustomRules.ConflictTitle" to R.string.meta_feature_custom_rules_conflict_title,
        "MetaFeature.CustomRules.DeleteRule" to R.string.meta_feature_custom_rules_delete_rule,
        "MetaFeature.CustomRules.DiscardMessage" to R.string.meta_feature_custom_rules_discard_message,
        "MetaFeature.CustomRules.DiscardTitle" to R.string.meta_feature_custom_rules_discard_title,
        "MetaFeature.CustomRules.EditRule" to R.string.meta_feature_custom_rules_edit_rule,
        "MetaFeature.CustomRules.Empty" to R.string.meta_feature_custom_rules_empty,
        "MetaFeature.CustomRules.ErrorLoad" to R.string.meta_feature_custom_rules_error_load,
        "MetaFeature.CustomRules.ErrorNotFound" to R.string.meta_feature_custom_rules_error_not_found,
        "MetaFeature.CustomRules.ErrorRateLimited" to R.string.meta_feature_custom_rules_error_rate_limited,
        "MetaFeature.CustomRules.ErrorRequest" to R.string.meta_feature_custom_rules_error_request,
        "MetaFeature.CustomRules.ErrorUnknown" to R.string.meta_feature_custom_rules_error_unknown,
        "MetaFeature.CustomRules.ErrorValidation" to R.string.meta_feature_custom_rules_error_validation,
        "MetaFeature.CustomRules.ErrorValidationGeneral" to R.string.meta_feature_custom_rules_error_validation_general,
        "MetaFeature.CustomRules.KeepLocal" to R.string.meta_feature_custom_rules_keep_local,
        "MetaFeature.CustomRules.Loading" to R.string.meta_feature_custom_rules_loading,
        "MetaFeature.CustomRules.MatchPayloadHint" to R.string.meta_feature_custom_rules_match_payload_hint,
        "MetaFeature.CustomRules.MoveDown" to R.string.meta_feature_custom_rules_move_down,
        "MetaFeature.CustomRules.MoveUp" to R.string.meta_feature_custom_rules_move_up,
        "MetaFeature.CustomRules.Payload" to R.string.meta_feature_custom_rules_payload,
        "MetaFeature.CustomRules.Provider" to R.string.meta_feature_custom_rules_provider,
        "MetaFeature.CustomRules.Refresh" to R.string.meta_feature_custom_rules_refresh,
        "MetaFeature.CustomRules.Retry" to R.string.meta_feature_custom_rules_retry,
        "MetaFeature.CustomRules.Rules" to R.string.meta_feature_custom_rules_rules,
        "MetaFeature.CustomRules.Save" to R.string.meta_feature_custom_rules_save,
        "MetaFeature.CustomRules.Saved" to R.string.meta_feature_custom_rules_saved,
        "MetaFeature.CustomRules.Target" to R.string.meta_feature_custom_rules_target,
        "MetaFeature.CustomRules.Title" to R.string.meta_feature_custom_rules_title,
        "MetaFeature.CustomRules.Type" to R.string.meta_feature_custom_rules_type,
        "MetaFeature.CustomRules.UseRemote" to R.string.meta_feature_custom_rules_use_remote,
        "MetaFeature.Download.DialogTitle" to R.string.meta_feature_download_dialog_title,
        "MetaFeature.Download.DownloadComplete" to R.string.meta_feature_download_download_complete,
        "MetaFeature.Download.ImportFailed" to R.string.meta_feature_download_import_failed,
        "MetaFeature.Download.ImportSuccess" to R.string.meta_feature_download_import_success,
        "MetaFeature.Download.LastUpdate" to R.string.meta_feature_download_last_update,
        "MetaFeature.Download.LastUpdateNever" to R.string.meta_feature_download_last_update_never,
        "MetaFeature.Download.LastUpdateSourceLocal" to R.string.meta_feature_download_last_update_source_local,
        "MetaFeature.Download.LastUpdateSourceOnline" to R.string.meta_feature_download_last_update_source_online,
        "MetaFeature.Download.LocalDialogTitle" to R.string.meta_feature_download_local_dialog_title,
        "MetaFeature.Download.ProgressDetail" to R.string.meta_feature_download_progress_detail,
        "MetaFeature.Download.ProgressDetailUnknownTotal" to R.string.meta_feature_download_progress_detail_unknown_total,
        "MetaFeature.Download.ProgressFailed" to R.string.meta_feature_download_progress_failed,
        "MetaFeature.Download.ProgressSuccess" to R.string.meta_feature_download_progress_success,
        "MetaFeature.Download.ProgressSummary" to R.string.meta_feature_download_progress_summary,
        "MetaFeature.Download.ProgressTitle" to R.string.meta_feature_download_progress_title,
        "MetaFeature.Download.ProgressWaiting" to R.string.meta_feature_download_progress_waiting,
        "MetaFeature.Download.SelectFiles" to R.string.meta_feature_download_select_files,
        "MetaFeature.Download.StatusDownloading" to R.string.meta_feature_download_status_downloading,
        "MetaFeature.Download.StatusFailed" to R.string.meta_feature_download_status_failed,
        "MetaFeature.Download.StatusPending" to R.string.meta_feature_download_status_pending,
        "MetaFeature.Download.StatusSuccess" to R.string.meta_feature_download_status_success,
        "MetaFeature.Download.StatusValidating" to R.string.meta_feature_download_status_validating,
        "MetaFeature.GeoX.LocalUpdateSummary" to R.string.meta_feature_geo_x_local_update_summary,
        "MetaFeature.GeoX.LocalUpdateTitle" to R.string.meta_feature_geo_x_local_update_title,
        "MetaFeature.GeoX.OnlineUpdateSummary" to R.string.meta_feature_geo_x_online_update_summary,
        "MetaFeature.GeoX.OnlineUpdateTitle" to R.string.meta_feature_geo_x_online_update_title,
        "MetaFeature.GeoX.RuntimeHomeInfo" to R.string.meta_feature_geo_x_runtime_home_info,
        "MetaFeature.Section.ConnectionAndTraffic" to R.string.meta_feature_section_connection_and_traffic,
        "MetaFeature.Section.GeoXUpdate" to R.string.meta_feature_section_geo_xupdate,
        "MetaFeature.Title" to R.string.meta_feature_title,
        "NetworkSettings.Error.RootRequired" to R.string.network_settings_error_root_required,
        "NetworkSettings.Error.VpnDenied" to R.string.network_settings_error_vpn_denied,
        "NetworkSettings.Experimental.AntiPollutionDnsSummary" to R.string.network_settings_experimental_anti_pollution_dns_summary,
        "NetworkSettings.Experimental.AntiPollutionDnsTitle" to R.string.network_settings_experimental_anti_pollution_dns_title,
        "NetworkSettings.Network.CustomUserAgentSummaryDefault" to R.string.network_settings_network_custom_user_agent_summary_default,
        "NetworkSettings.Network.CustomUserAgentTitle" to R.string.network_settings_network_custom_user_agent_title,
        "NetworkSettings.Network.UserAgentDialogTitle" to R.string.network_settings_network_user_agent_dialog_title,
        "NetworkSettings.ProxyOptions.AccessControlModeTitle" to R.string.network_settings_proxy_options_access_control_mode_title,
        "NetworkSettings.ProxyOptions.AllowAll" to R.string.network_settings_proxy_options_allow_all,
        "NetworkSettings.ProxyOptions.AllowSelected" to R.string.network_settings_proxy_options_allow_selected,
        "NetworkSettings.ProxyOptions.ManageAccessControlSummary" to R.string.network_settings_proxy_options_manage_access_control_summary,
        "NetworkSettings.ProxyOptions.ManageAccessControlTitle" to R.string.network_settings_proxy_options_manage_access_control_title,
        "NetworkSettings.ProxyOptions.RejectSelected" to R.string.network_settings_proxy_options_reject_selected,
        "NetworkSettings.ProxyOptions.TunStackTitle" to R.string.network_settings_proxy_options_tun_stack_title,
        "NetworkSettings.RootTun.AutoRedirectSummary" to R.string.network_settings_root_tun_auto_redirect_summary,
        "NetworkSettings.RootTun.AutoRedirectTitle" to R.string.network_settings_root_tun_auto_redirect_title,
        "NetworkSettings.RootTun.AutoRouteSummary" to R.string.network_settings_root_tun_auto_route_summary,
        "NetworkSettings.RootTun.AutoRouteTitle" to R.string.network_settings_root_tun_auto_route_title,
        "NetworkSettings.RootTun.DnsModeFakeIp" to R.string.network_settings_root_tun_dns_mode_fake_ip,
        "NetworkSettings.RootTun.DnsModeRedirHost" to R.string.network_settings_root_tun_dns_mode_redir_host,
        "NetworkSettings.RootTun.DnsModeSummary" to R.string.network_settings_root_tun_dns_mode_summary,
        "NetworkSettings.RootTun.DnsModeTitle" to R.string.network_settings_root_tun_dns_mode_title,
        "NetworkSettings.RootTun.FakeIpRange6Summary" to R.string.network_settings_root_tun_fake_ip_range6_summary,
        "NetworkSettings.RootTun.FakeIpRange6Title" to R.string.network_settings_root_tun_fake_ip_range6_title,
        "NetworkSettings.RootTun.FakeIpRangeSummary" to R.string.network_settings_root_tun_fake_ip_range_summary,
        "NetworkSettings.RootTun.FakeIpRangeTitle" to R.string.network_settings_root_tun_fake_ip_range_title,
        "NetworkSettings.RootTun.IfNameSummary" to R.string.network_settings_root_tun_if_name_summary,
        "NetworkSettings.RootTun.IfNameTitle" to R.string.network_settings_root_tun_if_name_title,
        "NetworkSettings.RootTun.MtuSummary" to R.string.network_settings_root_tun_mtu_summary,
        "NetworkSettings.RootTun.MtuTitle" to R.string.network_settings_root_tun_mtu_title,
        "NetworkSettings.RootTun.StrictRouteSummary" to R.string.network_settings_root_tun_strict_route_summary,
        "NetworkSettings.RootTun.StrictRouteTitle" to R.string.network_settings_root_tun_strict_route_title,
        "NetworkSettings.Section.Experimental" to R.string.network_settings_section_experimental,
        "NetworkSettings.Section.Network" to R.string.network_settings_section_network,
        "NetworkSettings.Section.ProxyOptions" to R.string.network_settings_section_proxy_options,
        "NetworkSettings.Section.VpnOptions" to R.string.network_settings_section_vpn_options,
        "NetworkSettings.Section.VpnService" to R.string.network_settings_section_vpn_service,
        "NetworkSettings.Title" to R.string.network_settings_title,
        "NetworkSettings.VpnOptions.AllowBypassSummary" to R.string.network_settings_vpn_options_allow_bypass_summary,
        "NetworkSettings.VpnOptions.AllowBypassTitle" to R.string.network_settings_vpn_options_allow_bypass_title,
        "NetworkSettings.VpnOptions.BypassPrivateSummary" to R.string.network_settings_vpn_options_bypass_private_summary,
        "NetworkSettings.VpnOptions.BypassPrivateTitle" to R.string.network_settings_vpn_options_bypass_private_title,
        "NetworkSettings.VpnOptions.DnsHijackSummary" to R.string.network_settings_vpn_options_dns_hijack_summary,
        "NetworkSettings.VpnOptions.DnsHijackTitle" to R.string.network_settings_vpn_options_dns_hijack_title,
        "NetworkSettings.VpnOptions.EnableIpv6Summary" to R.string.network_settings_vpn_options_enable_ipv6_summary,
        "NetworkSettings.VpnOptions.EnableIpv6Title" to R.string.network_settings_vpn_options_enable_ipv6_title,
        "NetworkSettings.VpnOptions.SystemProxySummary" to R.string.network_settings_vpn_options_system_proxy_summary,
        "NetworkSettings.VpnOptions.SystemProxyTitle" to R.string.network_settings_vpn_options_system_proxy_title,
        "NetworkSettings.VpnService.RootTunMode" to R.string.network_settings_vpn_service_root_tun_mode,
        "NetworkSettings.VpnService.RouteTrafficSummary" to R.string.network_settings_vpn_service_route_traffic_summary,
        "NetworkSettings.VpnService.RouteTrafficTitle" to R.string.network_settings_vpn_service_route_traffic_title,
        "NetworkSettings.VpnService.SystemProxy" to R.string.network_settings_vpn_service_system_proxy,
        "NetworkSettings.VpnService.VpnMode" to R.string.network_settings_vpn_service_vpn_mode,
        "Onboarding.Finish.Subtitle" to R.string.onboarding_finish_subtitle,
        "Onboarding.Finish.Title" to R.string.onboarding_finish_title,
        "Onboarding.Navigation.Back" to R.string.onboarding_navigation_back,
        "Onboarding.Navigation.Enter" to R.string.onboarding_navigation_enter,
        "Onboarding.Navigation.Next" to R.string.onboarding_navigation_next,
        "Onboarding.Navigation.Start" to R.string.onboarding_navigation_start,
        "Onboarding.Permission.AppList.SummaryNeed" to R.string.onboarding_permission_app_list_summary_need,
        "Onboarding.Permission.AppList.Title" to R.string.onboarding_permission_app_list_title,
        "Onboarding.Permission.Common.Granted" to R.string.onboarding_permission_common_granted,
        "Onboarding.Permission.Notification.SummaryNeed" to R.string.onboarding_permission_notification_summary_need,
        "Onboarding.Permission.Notification.SummaryNotRequired" to R.string.onboarding_permission_notification_summary_not_required,
        "Onboarding.Permission.Notification.Title" to R.string.onboarding_permission_notification_title,
        "Onboarding.Permission.Subtitle" to R.string.onboarding_permission_subtitle,
        "Onboarding.Permission.Title" to R.string.onboarding_permission_title,
        "Onboarding.Personalize.Subtitle" to R.string.onboarding_personalize_subtitle,
        "Onboarding.Personalize.Title" to R.string.onboarding_personalize_title,
        "Onboarding.Privacy.Accept.Title" to R.string.onboarding_privacy_accept_title,
        "Onboarding.Privacy.PolicyLink" to R.string.onboarding_privacy_policy_link,
        "Onboarding.Privacy.Privacy.Title" to R.string.onboarding_privacy_privacy_title,
        "Onboarding.Privacy.RichTextLead" to R.string.onboarding_privacy_rich_text_lead,
        "Onboarding.Privacy.RichTextPrefix" to R.string.onboarding_privacy_rich_text_prefix,
        "Onboarding.Privacy.RichTextSuffix" to R.string.onboarding_privacy_rich_text_suffix,
        "Onboarding.Privacy.Subtitle" to R.string.onboarding_privacy_subtitle,
        "Onboarding.Privacy.Title" to R.string.onboarding_privacy_title,
        "Onboarding.Sheet.LoadFailed" to R.string.onboarding_sheet_load_failed,
        "Onboarding.Sheet.PrivacyPolicyTitle" to R.string.onboarding_sheet_privacy_policy_title,
        "OpenSourceLicenses.LicenseSheet.NoContent" to R.string.open_source_licenses_license_sheet_no_content,
        "OpenSourceLicenses.Title" to R.string.open_source_licenses_title,
        "Override.Action.Create" to R.string.override_action_create,
        "Override.Action.Import" to R.string.override_action_import,
        "Override.Action.ImportFile" to R.string.override_action_import_file,
        "Override.Action.New" to R.string.override_action_new,
        "Override.Card.Copy" to R.string.override_card_copy,
        "Override.Card.Delete" to R.string.override_card_delete,
        "Override.Card.DeleteButton" to R.string.override_card_delete_button,
        "Override.Card.Edit" to R.string.override_card_edit,
        "Override.Card.EditButton" to R.string.override_card_edit_button,
        "Override.Card.Export" to R.string.override_card_export,
        "Override.Card.NoDescription" to R.string.override_card_no_description,
        "Override.Dialog.Button.Cancel" to R.string.override_dialog_button_cancel,
        "Override.Dialog.Button.Delete" to R.string.override_dialog_button_delete,
        "Override.Dialog.Create.Description" to R.string.override_dialog_create_description,
        "Override.Dialog.Create.ImportHint" to R.string.override_dialog_create_import_hint,
        "Override.Dialog.Create.Name" to R.string.override_dialog_create_name,
        "Override.Dialog.Create.Title" to R.string.override_dialog_create_title,
        "Override.Dialog.Delete.InUseMessage" to R.string.override_dialog_delete_in_use_message,
        "Override.Dialog.Delete.Message" to R.string.override_dialog_delete_message,
        "Override.Dialog.Delete.Title" to R.string.override_dialog_delete_title,
        "Override.Dialog.EditOptions.CodeEditor" to R.string.override_dialog_edit_options_code_editor,
        "Override.Dialog.EditOptions.Title" to R.string.override_dialog_edit_options_title,
        "Override.Dialog.EditOptions.VisualEditor" to R.string.override_dialog_edit_options_visual_editor,
        "Override.Dns.AppendSystem" to R.string.override_dns_append_system,
        "Override.Dns.Default" to R.string.override_dns_default,
        "Override.Dns.DefaultHint" to R.string.override_dns_default_hint,
        "Override.Dns.EnhancedDisable" to R.string.override_dns_enhanced_disable,
        "Override.Dns.EnhancedFakeip" to R.string.override_dns_enhanced_fakeip,
        "Override.Dns.EnhancedMapping" to R.string.override_dns_enhanced_mapping,
        "Override.Dns.EnhancedMode" to R.string.override_dns_enhanced_mode,
        "Override.Dns.EnhancedNotModify" to R.string.override_dns_enhanced_not_modify,
        "Override.Dns.FakeipBlacklist" to R.string.override_dns_fakeip_blacklist,
        "Override.Dns.FakeipFilter" to R.string.override_dns_fakeip_filter,
        "Override.Dns.FakeipFilterHint" to R.string.override_dns_fakeip_filter_hint,
        "Override.Dns.FakeipFilterMode" to R.string.override_dns_fakeip_filter_mode,
        "Override.Dns.FakeipWhitelist" to R.string.override_dns_fakeip_whitelist,
        "Override.Dns.Fallback" to R.string.override_dns_fallback,
        "Override.Dns.FallbackDomain" to R.string.override_dns_fallback_domain,
        "Override.Dns.FallbackDomainHint" to R.string.override_dns_fallback_domain_hint,
        "Override.Dns.FallbackGeoip" to R.string.override_dns_fallback_geoip,
        "Override.Dns.FallbackGeoipCode" to R.string.override_dns_fallback_geoip_code,
        "Override.Dns.FallbackGeoipCodeHint" to R.string.override_dns_fallback_geoip_code_hint,
        "Override.Dns.FallbackHint" to R.string.override_dns_fallback_hint,
        "Override.Dns.FallbackIpcidr" to R.string.override_dns_fallback_ipcidr,
        "Override.Dns.FallbackIpcidrHint" to R.string.override_dns_fallback_ipcidr_hint,
        "Override.Dns.Ipv6" to R.string.override_dns_ipv6,
        "Override.Dns.Listen" to R.string.override_dns_listen,
        "Override.Dns.ListenHint" to R.string.override_dns_listen_hint,
        "Override.Dns.NameserverPolicy" to R.string.override_dns_nameserver_policy,
        "Override.Dns.NameserverPolicyKey" to R.string.override_dns_nameserver_policy_key,
        "Override.Dns.NameserverPolicyValue" to R.string.override_dns_nameserver_policy_value,
        "Override.Dns.Policy" to R.string.override_dns_policy,
        "Override.Dns.PolicyForceEnable" to R.string.override_dns_policy_force_enable,
        "Override.Dns.PolicyNotModify" to R.string.override_dns_policy_not_modify,
        "Override.Dns.PolicyUseBuiltin" to R.string.override_dns_policy_use_builtin,
        "Override.Dns.PreferH3" to R.string.override_dns_prefer_h3,
        "Override.Dns.Servers" to R.string.override_dns_servers,
        "Override.Dns.ServersHint" to R.string.override_dns_servers_hint,
        "Override.Dns.UseHosts" to R.string.override_dns_use_hosts,
        "Override.Draft.AddExtraField" to R.string.override_draft_add_extra_field,
        "Override.Draft.AddHealthCheckField" to R.string.override_draft_add_health_check_field,
        "Override.Draft.AddOverrideField" to R.string.override_draft_add_override_field,
        "Override.Draft.Apply" to R.string.override_draft_apply,
        "Override.Draft.BasicIdentity" to R.string.override_draft_basic_identity,
        "Override.Draft.BasicInfo" to R.string.override_draft_basic_info,
        "Override.Draft.BasicRouting" to R.string.override_draft_basic_routing,
        "Override.Draft.BooleanOptions" to R.string.override_draft_boolean_options,
        "Override.Draft.ClickToAddExtraField" to R.string.override_draft_click_to_add_extra_field,
        "Override.Draft.ConfigDescription" to R.string.override_draft_config_description,
        "Override.Draft.ConfigName" to R.string.override_draft_config_name,
        "Override.Draft.ConfigSections" to R.string.override_draft_config_sections,
        "Override.Draft.CoreSource" to R.string.override_draft_core_source,
        "Override.Draft.DeleteExtraField" to R.string.override_draft_delete_extra_field,
        "Override.Draft.DoubleValue" to R.string.override_draft_double_value,
        "Override.Draft.EditExtraField" to R.string.override_draft_edit_extra_field,
        "Override.Draft.EditHealthCheckField" to R.string.override_draft_edit_health_check_field,
        "Override.Draft.EditOverrideField" to R.string.override_draft_edit_override_field,
        "Override.Draft.EditSubRules" to R.string.override_draft_edit_sub_rules,
        "Override.Draft.ExtraFields" to R.string.override_draft_extra_fields,
        "Override.Draft.ExtraFieldsConfigured" to R.string.override_draft_extra_fields_configured,
        "Override.Draft.FallbackRegionGroupTitle" to R.string.override_draft_fallback_region_group_title,
        "Override.Draft.GroupTypeFallback" to R.string.override_draft_group_type_fallback,
        "Override.Draft.GroupTypeTitle" to R.string.override_draft_group_type_title,
        "Override.Draft.GroupTypeUrlTest" to R.string.override_draft_group_type_url_test,
        "Override.Draft.HeaderHint" to R.string.override_draft_header_hint,
        "Override.Draft.HealthCheckFields" to R.string.override_draft_health_check_fields,
        "Override.Draft.HealthCheckSwitch" to R.string.override_draft_health_check_switch,
        "Override.Draft.IntValue" to R.string.override_draft_int_value,
        "Override.Draft.JsonFragment" to R.string.override_draft_json_fragment,
        "Override.Draft.KeyNameEmpty" to R.string.override_draft_key_name_empty,
        "Override.Draft.Name" to R.string.override_draft_name,
        "Override.Draft.NameRequired" to R.string.override_draft_name_required,
        "Override.Draft.NetworkAuth" to R.string.override_draft_network_auth,
        "Override.Draft.NoRules" to R.string.override_draft_no_rules,
        "Override.Draft.Object" to R.string.override_draft_object,
        "Override.Draft.OfficialMrs" to R.string.override_draft_official_mrs,
        "Override.Draft.OfficialMrsSummary" to R.string.override_draft_official_mrs_summary,
        "Override.Draft.OverrideFields" to R.string.override_draft_override_fields,
        "Override.Draft.OverrideSwitch" to R.string.override_draft_override_switch,
        "Override.Draft.PresetApplySummary" to R.string.override_draft_preset_apply_summary,
        "Override.Draft.PresetTemplate" to R.string.override_draft_preset_template,
        "Override.Draft.RegionalAutoGroup" to R.string.override_draft_regional_auto_group,
        "Override.Draft.RuleList" to R.string.override_draft_rule_list,
        "Override.Draft.RulesConfigured" to R.string.override_draft_rules_configured,
        "Override.Draft.Save" to R.string.override_draft_save,
        "Override.Draft.ServiceRouting" to R.string.override_draft_service_routing,
        "Override.Draft.StringValue" to R.string.override_draft_string_value,
        "Override.Draft.SubRuleGroup" to R.string.override_draft_sub_rule_group,
        "Override.Draft.UrlTestRegionGroupTitle" to R.string.override_draft_url_test_region_group_title,
        "Override.Draft.ValueType" to R.string.override_draft_value_type,
        "Override.Draft.ValueTypeMismatch" to R.string.override_draft_value_type_mismatch,
        "Override.Edit.Button.Cancel" to R.string.override_edit_button_cancel,
        "Override.Edit.Button.Discard" to R.string.override_edit_button_discard,
        "Override.Edit.EmptyName.Summary" to R.string.override_edit_empty_name_summary,
        "Override.Edit.EmptyName.Title" to R.string.override_edit_empty_name_title,
        "Override.Edit.PresetApplied" to R.string.override_edit_preset_applied,
        "Override.Edit.TitleEdit" to R.string.override_edit_title_edit,
        "Override.Edit.TitleNew" to R.string.override_edit_title_new,
        "Override.Editor.AddCustom" to R.string.override_editor_add_custom,
        "Override.Editor.AddItem" to R.string.override_editor_add_item,
        "Override.Editor.AddObject" to R.string.override_editor_add_object,
        "Override.Editor.AddSubRuleGroup" to R.string.override_editor_add_sub_rule_group,
        "Override.Editor.AdditionalParams" to R.string.override_editor_additional_params,
        "Override.Editor.ArrayItems" to R.string.override_editor_array_items,
        "Override.Editor.BasicConnection" to R.string.override_editor_basic_connection,
        "Override.Editor.CancelDelete" to R.string.override_editor_cancel_delete,
        "Override.Editor.Clear" to R.string.override_editor_clear,
        "Override.Editor.ClearCurrentMode" to R.string.override_editor_clear_current_mode,
        "Override.Editor.ClearDialog.Summary" to R.string.override_editor_clear_dialog_summary,
        "Override.Editor.ClearDialog.Title" to R.string.override_editor_clear_dialog_title,
        "Override.Editor.ClearMode" to R.string.override_editor_clear_mode,
        "Override.Editor.ClearSubRules" to R.string.override_editor_clear_sub_rules,
        "Override.Editor.Confirm" to R.string.override_editor_confirm,
        "Override.Editor.ContentEmpty" to R.string.override_editor_content_empty,
        "Override.Editor.Copy" to R.string.override_editor_copy,
        "Override.Editor.CustomMatchResult" to R.string.override_editor_custom_match_result,
        "Override.Editor.CustomMember" to R.string.override_editor_custom_member,
        "Override.Editor.CustomProxyGroupTarget" to R.string.override_editor_custom_proxy_group_target,
        "Override.Editor.CustomSubRuleTarget" to R.string.override_editor_custom_sub_rule_target,
        "Override.Editor.Delete" to R.string.override_editor_delete,
        "Override.Editor.DeleteLastItem" to R.string.override_editor_delete_last_item,
        "Override.Editor.DeleteSelected" to R.string.override_editor_delete_selected,
        "Override.Editor.DeleteSelectedRules" to R.string.override_editor_delete_selected_rules,
        "Override.Editor.DragToSort" to R.string.override_editor_drag_to_sort,
        "Override.Editor.Edit" to R.string.override_editor_edit,
        "Override.Editor.EditItem" to R.string.override_editor_edit_item,
        "Override.Editor.EditProxyGroup" to R.string.override_editor_edit_proxy_group,
        "Override.Editor.EditProxyNode" to R.string.override_editor_edit_proxy_node,
        "Override.Editor.EditRule" to R.string.override_editor_edit_rule,
        "Override.Editor.EditSubRule" to R.string.override_editor_edit_sub_rule,
        "Override.Editor.EditSubRuleGroup" to R.string.override_editor_edit_sub_rule_group,
        "Override.Editor.EmptyString" to R.string.override_editor_empty_string,
        "Override.Editor.EnterDeleteMode" to R.string.override_editor_enter_delete_mode,
        "Override.Editor.ExtraParamsHint" to R.string.override_editor_extra_params_hint,
        "Override.Editor.HealthCheckAndFilter" to R.string.override_editor_health_check_and_filter,
        "Override.Editor.JsonBlockSubtitle" to R.string.override_editor_json_block_subtitle,
        "Override.Editor.KeyName" to R.string.override_editor_key_name,
        "Override.Editor.List" to R.string.override_editor_list,
        "Override.Editor.LogicalRuleHint" to R.string.override_editor_logical_rule_hint,
        "Override.Editor.MatchResult" to R.string.override_editor_match_result,
        "Override.Editor.MemberSource" to R.string.override_editor_member_source,
        "Override.Editor.Mode.Title" to R.string.override_editor_mode_title,
        "Override.Editor.MoveDown" to R.string.override_editor_move_down,
        "Override.Editor.MoveUp" to R.string.override_editor_move_up,
        "Override.Editor.NetworkAndRoute" to R.string.override_editor_network_and_route,
        "Override.Editor.New" to R.string.override_editor_new,
        "Override.Editor.NewProvider" to R.string.override_editor_new_provider,
        "Override.Editor.NewProxyGroup" to R.string.override_editor_new_proxy_group,
        "Override.Editor.NewProxyNode" to R.string.override_editor_new_proxy_node,
        "Override.Editor.NewRule" to R.string.override_editor_new_rule,
        "Override.Editor.NewSubRuleGroup" to R.string.override_editor_new_sub_rule_group,
        "Override.Editor.NoRules" to R.string.override_editor_no_rules,
        "Override.Editor.ObjectFallbackTitle" to R.string.override_editor_object_fallback_title,
        "Override.Editor.ObjectFieldCount" to R.string.override_editor_object_field_count,
        "Override.Editor.ObjectFieldHint" to R.string.override_editor_object_field_hint,
        "Override.Editor.ObjectFields" to R.string.override_editor_object_fields,
        "Override.Editor.ObjectJsonPlaceholder" to R.string.override_editor_object_json_placeholder,
        "Override.Editor.ObjectListHint" to R.string.override_editor_object_list_hint,
        "Override.Editor.OneItemPerLine" to R.string.override_editor_one_item_per_line,
        "Override.Editor.OtherExtraParams" to R.string.override_editor_other_extra_params,
        "Override.Editor.Payload" to R.string.override_editor_payload,
        "Override.Editor.PayloadEmpty" to R.string.override_editor_payload_empty,
        "Override.Editor.PortEmptyHint" to R.string.override_editor_port_empty_hint,
        "Override.Editor.ProviderMapHint" to R.string.override_editor_provider_map_hint,
        "Override.Editor.ProxyGroup" to R.string.override_editor_proxy_group,
        "Override.Editor.ProxyGroupTarget" to R.string.override_editor_proxy_group_target,
        "Override.Editor.ProxyNode" to R.string.override_editor_proxy_node,
        "Override.Editor.RuleBody" to R.string.override_editor_rule_body,
        "Override.Editor.RuleEdit" to R.string.override_editor_rule_edit,
        "Override.Editor.RulePlaceholder" to R.string.override_editor_rule_placeholder,
        "Override.Editor.RuleProviderInputHint" to R.string.override_editor_rule_provider_input_hint,
        "Override.Editor.RuleType" to R.string.override_editor_rule_type,
        "Override.Editor.RuleTypeEmpty" to R.string.override_editor_rule_type_empty,
        "Override.Editor.Rules" to R.string.override_editor_rules,
        "Override.Editor.RulesConfiguredInline" to R.string.override_editor_rules_configured_inline,
        "Override.Editor.SaveProxyGroup" to R.string.override_editor_save_proxy_group,
        "Override.Editor.SaveProxyNode" to R.string.override_editor_save_proxy_node,
        "Override.Editor.SaveRule" to R.string.override_editor_save_rule,
        "Override.Editor.SelectMatchResult" to R.string.override_editor_select_match_result,
        "Override.Editor.SelectProxyGroupMember" to R.string.override_editor_select_proxy_group_member,
        "Override.Editor.SelectProxyGroupTarget" to R.string.override_editor_select_proxy_group_target,
        "Override.Editor.SelectRuleProvider" to R.string.override_editor_select_rule_provider,
        "Override.Editor.SelectSubRuleTarget" to R.string.override_editor_select_sub_rule_target,
        "Override.Editor.SubRuleGroupHint" to R.string.override_editor_sub_rule_group_hint,
        "Override.Editor.SubRuleName" to R.string.override_editor_sub_rule_name,
        "Override.Editor.SubRuleTarget" to R.string.override_editor_sub_rule_target,
        "Override.Editor.TargetEmpty" to R.string.override_editor_target_empty,
        "Override.Editor.TypeEmpty" to R.string.override_editor_type_empty,
        "Override.Editor.Unnamed" to R.string.override_editor_unnamed,
        "Override.Editor.UnnamedProvider" to R.string.override_editor_unnamed_provider,
        "Override.Editor.UnnamedProxyGroup" to R.string.override_editor_unnamed_proxy_group,
        "Override.Editor.UnnamedProxyNode" to R.string.override_editor_unnamed_proxy_node,
        "Override.Editor.UnnamedRule" to R.string.override_editor_unnamed_rule,
        "Override.Editor.UnnamedSubRuleGroup" to R.string.override_editor_unnamed_sub_rule_group,
        "Override.Empty.Hint" to R.string.override_empty_hint,
        "Override.Empty.Title" to R.string.override_empty_title,
        "Override.Export.Failed" to R.string.override_export_failed,
        "Override.Export.Success" to R.string.override_export_success,
        "Override.Form.AdvancedJson" to R.string.override_form_advanced_json,
        "Override.Form.AllowPrivateNetwork" to R.string.override_form_allow_private_network,
        "Override.Form.AllowedIPs" to R.string.override_form_allowed_ips,
        "Override.Form.ApiSecret" to R.string.override_form_api_secret,
        "Override.Form.AutoDetectInterface" to R.string.override_form_auto_detect_interface,
        "Override.Form.AutoRedirect" to R.string.override_form_auto_redirect,
        "Override.Form.AutoRoute" to R.string.override_form_auto_route,
        "Override.Form.AutoUpdateGeo" to R.string.override_form_auto_update_geo,
        "Override.Form.BasicPolicy" to R.string.override_form_basic_policy,
        "Override.Form.BindAddress" to R.string.override_form_bind_address,
        "Override.Form.CacheLimit" to R.string.override_form_cache_limit,
        "Override.Form.ConfigPersistence" to R.string.override_form_config_persistence,
        "Override.Form.ConnectionNetwork" to R.string.override_form_connection_network,
        "Override.Form.ControllerCors" to R.string.override_form_controller_cors,
        "Override.Form.DirectFollowPolicy" to R.string.override_form_direct_follow_policy,
        "Override.Form.DisableIcmpForward" to R.string.override_form_disable_icmp_forward,
        "Override.Form.DisallowedIPs" to R.string.override_form_disallowed_ips,
        "Override.Form.DnsBasicParams" to R.string.override_form_dns_basic_params,
        "Override.Form.DnsBasicSwitch" to R.string.override_form_dns_basic_switch,
        "Override.Form.DnsFakeIpRange" to R.string.override_form_dns_fake_ip_range,
        "Override.Form.DnsHijack" to R.string.override_form_dns_hijack,
        "Override.Form.DnsPolicyMode" to R.string.override_form_dns_policy_mode,
        "Override.Form.DnsUpstream" to R.string.override_form_dns_upstream,
        "Override.Form.DnsUpstreamServers" to R.string.override_form_dns_upstream_servers,
        "Override.Form.EnableGso" to R.string.override_form_enable_gso,
        "Override.Form.EndpointIndependentNat" to R.string.override_form_endpoint_independent_nat,
        "Override.Form.ExcludePackage" to R.string.override_form_exclude_package,
        "Override.Form.ExternalControl" to R.string.override_form_external_control,
        "Override.Form.ExternalController" to R.string.override_form_external_controller,
        "Override.Form.ExternalControllerHttps" to R.string.override_form_external_controller_https,
        "Override.Form.ExternalDoH" to R.string.override_form_external_do_h,
        "Override.Form.FakeIpIpv6Range" to R.string.override_form_fake_ip_ipv6_range,
        "Override.Form.FakeIpMode" to R.string.override_form_fake_ip_mode,
        "Override.Form.FakeIpParams" to R.string.override_form_fake_ip_params,
        "Override.Form.FallbackFilter" to R.string.override_form_fallback_filter,
        "Override.Form.FallbackParams" to R.string.override_form_fallback_params,
        "Override.Form.FallbackSwitch" to R.string.override_form_fallback_switch,
        "Override.Form.FilterList" to R.string.override_form_filter_list,
        "Override.Form.GeoResources" to R.string.override_form_geo_resources,
        "Override.Form.GeoUpdateInterval" to R.string.override_form_geo_update_interval,
        "Override.Form.GeodataMode" to R.string.override_form_geodata_mode,
        "Override.Form.GeoipUrl" to R.string.override_form_geoip_url,
        "Override.Form.GeositeMatcher" to R.string.override_form_geosite_matcher,
        "Override.Form.GeositeUrl" to R.string.override_form_geosite_url,
        "Override.Form.GlobalClientFingerprint" to R.string.override_form_global_client_fingerprint,
        "Override.Form.Hours" to R.string.override_form_hours,
        "Override.Form.HttpPorts" to R.string.override_form_http_ports,
        "Override.Form.IncludePackage" to R.string.override_form_include_package,
        "Override.Form.Ipv6Timeout" to R.string.override_form_ipv6_timeout,
        "Override.Form.ItemsConfigured" to R.string.override_form_items_configured,
        "Override.Form.LanAccess" to R.string.override_form_lan_access,
        "Override.Form.LanAddress" to R.string.override_form_lan_address,
        "Override.Form.MmdbUrl" to R.string.override_form_mmdb_url,
        "Override.Form.NameserverPolicySection" to R.string.override_form_nameserver_policy_section,
        "Override.Form.NetworkPerfParams" to R.string.override_form_network_perf_params,
        "Override.Form.NetworkPerfSwitch" to R.string.override_form_network_perf_switch,
        "Override.Form.NotModify" to R.string.override_form_not_modify,
        "Override.Form.OpenAdvancedEdit" to R.string.override_form_open_advanced_edit,
        "Override.Form.OpenAdvancedEditSummary" to R.string.override_form_open_advanced_edit_summary,
        "Override.Form.OutboundInterface" to R.string.override_form_outbound_interface,
        "Override.Form.ProcessMode" to R.string.override_form_process_mode,
        "Override.Form.ProxyGroups" to R.string.override_form_proxy_groups,
        "Override.Form.ProxyGroupsHint" to R.string.override_form_proxy_groups_hint,
        "Override.Form.ProxyNodes" to R.string.override_form_proxy_nodes,
        "Override.Form.ProxyNodesHint" to R.string.override_form_proxy_nodes_hint,
        "Override.Form.ProxyPorts" to R.string.override_form_proxy_ports,
        "Override.Form.ProxyProviders" to R.string.override_form_proxy_providers,
        "Override.Form.ProxyProvidersAdvanced" to R.string.override_form_proxy_providers_advanced,
        "Override.Form.ProxyProvidersHint" to R.string.override_form_proxy_providers_hint,
        "Override.Form.ProxyServerNameserverPolicy" to R.string.override_form_proxy_server_nameserver_policy,
        "Override.Form.QuicPorts" to R.string.override_form_quic_ports,
        "Override.Form.RouteAddress" to R.string.override_form_route_address,
        "Override.Form.RouteExcludeAddress" to R.string.override_form_route_exclude_address,
        "Override.Form.RoutingMark" to R.string.override_form_routing_mark,
        "Override.Form.RuleChain" to R.string.override_form_rule_chain,
        "Override.Form.RuleChainNotSet" to R.string.override_form_rule_chain_not_set,
        "Override.Form.RuleProviders" to R.string.override_form_rule_providers,
        "Override.Form.RuleProvidersAdvanced" to R.string.override_form_rule_providers_advanced,
        "Override.Form.RuleProvidersHint" to R.string.override_form_rule_providers_hint,
        "Override.Form.RunAndLog" to R.string.override_form_run_and_log,
        "Override.Form.RunAndLogExtra" to R.string.override_form_run_and_log_extra,
        "Override.Form.SaveFakeIpMapping" to R.string.override_form_save_fake_ip_mapping,
        "Override.Form.SaveGroupSelection" to R.string.override_form_save_group_selection,
        "Override.Form.Seconds" to R.string.override_form_seconds,
        "Override.Form.SkipAndForce" to R.string.override_form_skip_and_force,
        "Override.Form.SkipAuthIPs" to R.string.override_form_skip_auth_ips,
        "Override.Form.SkipDstAddress" to R.string.override_form_skip_dst_address,
        "Override.Form.SkipSrcAddress" to R.string.override_form_skip_src_address,
        "Override.Form.SnifferForceDomain" to R.string.override_form_sniffer_force_domain,
        "Override.Form.SnifferOverride" to R.string.override_form_sniffer_override,
        "Override.Form.SnifferParsePureIp" to R.string.override_form_sniffer_parse_pure_ip,
        "Override.Form.SnifferPorts" to R.string.override_form_sniffer_ports,
        "Override.Form.SnifferSkipDomain" to R.string.override_form_sniffer_skip_domain,
        "Override.Form.SnifferSwitch" to R.string.override_form_sniffer_switch,
        "Override.Form.Stack" to R.string.override_form_stack,
        "Override.Form.StrictRoute" to R.string.override_form_strict_route,
        "Override.Form.StructuredEdit" to R.string.override_form_structured_edit,
        "Override.Form.SubRules" to R.string.override_form_sub_rules,
        "Override.Form.SubRulesAdvanced" to R.string.override_form_sub_rules_advanced,
        "Override.Form.SubRulesHint" to R.string.override_form_sub_rules_hint,
        "Override.Form.TcpConcurrent" to R.string.override_form_tcp_concurrent,
        "Override.Form.TlsPorts" to R.string.override_form_tls_ports,
        "Override.Form.TunBasicSwitch" to R.string.override_form_tun_basic_switch,
        "Override.Form.TunRouteAndApps" to R.string.override_form_tun_route_and_apps,
        "Override.Form.UnifiedDelay" to R.string.override_form_unified_delay,
        "Override.Form.UserAuth" to R.string.override_form_user_auth,
        "Override.General.AllowLan" to R.string.override_general_allow_lan,
        "Override.General.HttpPort" to R.string.override_general_http_port,
        "Override.General.Ipv6" to R.string.override_general_ipv6,
        "Override.General.LogLevel" to R.string.override_general_log_level,
        "Override.General.MixedPort" to R.string.override_general_mixed_port,
        "Override.General.ProxyMode" to R.string.override_general_proxy_mode,
        "Override.General.RedirectPort" to R.string.override_general_redirect_port,
        "Override.General.SocksPort" to R.string.override_general_socks_port,
        "Override.General.TproxyPort" to R.string.override_general_tproxy_port,
        "Override.Import.Failed" to R.string.override_import_failed,
        "Override.Import.FileError" to R.string.override_import_file_error,
        "Override.Import.ReadError" to R.string.override_import_read_error,
        "Override.Import.Success" to R.string.override_import_success,
        "Override.Import.SuccessDefault" to R.string.override_import_success_default,
        "Override.Label.CacheAlgorithm" to R.string.override_label_cache_algorithm,
        "Override.Label.Enable" to R.string.override_label_enable,
        "Override.Label.FakeIpRange" to R.string.override_label_fake_ip_range,
        "Override.Label.ForceDnsMapping" to R.string.override_label_force_dns_mapping,
        "Override.Label.ForceDomain" to R.string.override_label_force_domain,
        "Override.Label.HttpOverride" to R.string.override_label_http_override,
        "Override.Label.KeepAliveIdle" to R.string.override_label_keep_alive_idle,
        "Override.Label.KeepAliveInterval" to R.string.override_label_keep_alive_interval,
        "Override.Label.OverrideDestination" to R.string.override_label_override_destination,
        "Override.Label.ParsePureIp" to R.string.override_label_parse_pure_ip,
        "Override.Label.QuicOverride" to R.string.override_label_quic_override,
        "Override.Label.RespectRules" to R.string.override_label_respect_rules,
        "Override.Label.RulesReplace" to R.string.override_label_rules_replace,
        "Override.Label.SkipDomain" to R.string.override_label_skip_domain,
        "Override.Label.TlsOverride" to R.string.override_label_tls_override,
        "Override.Label.UseSystemHosts" to R.string.override_label_use_system_hosts,
        "Override.Modifier.End" to R.string.override_modifier_end,
        "Override.Modifier.Force" to R.string.override_modifier_force,
        "Override.Modifier.ItemsCount" to R.string.override_modifier_items_count,
        "Override.Modifier.Merge" to R.string.override_modifier_merge,
        "Override.Modifier.NoChanges" to R.string.override_modifier_no_changes,
        "Override.Modifier.NotModified" to R.string.override_modifier_not_modified,
        "Override.Modifier.Replace" to R.string.override_modifier_replace,
        "Override.Modifier.Start" to R.string.override_modifier_start,
        "Override.ProxyGroup.Field.DisableUdp" to R.string.override_proxy_group_field_disable_udp,
        "Override.ProxyGroup.Field.ExcludeFilter" to R.string.override_proxy_group_field_exclude_filter,
        "Override.ProxyGroup.Field.ExcludeType" to R.string.override_proxy_group_field_exclude_type,
        "Override.ProxyGroup.Field.ExpectedStatus" to R.string.override_proxy_group_field_expected_status,
        "Override.ProxyGroup.Field.Filter" to R.string.override_proxy_group_field_filter,
        "Override.ProxyGroup.Field.Hidden" to R.string.override_proxy_group_field_hidden,
        "Override.ProxyGroup.Field.Icon" to R.string.override_proxy_group_field_icon,
        "Override.ProxyGroup.Field.IncludeAll" to R.string.override_proxy_group_field_include_all,
        "Override.ProxyGroup.Field.IncludeAllProviders" to R.string.override_proxy_group_field_include_all_providers,
        "Override.ProxyGroup.Field.IncludeAllProxies" to R.string.override_proxy_group_field_include_all_proxies,
        "Override.ProxyGroup.Field.InterfaceName" to R.string.override_proxy_group_field_interface_name,
        "Override.ProxyGroup.Field.Interval" to R.string.override_proxy_group_field_interval,
        "Override.ProxyGroup.Field.Lazy" to R.string.override_proxy_group_field_lazy,
        "Override.ProxyGroup.Field.MaxFailedTimes" to R.string.override_proxy_group_field_max_failed_times,
        "Override.ProxyGroup.Field.Proxies" to R.string.override_proxy_group_field_proxies,
        "Override.ProxyGroup.Field.RoutingMark" to R.string.override_proxy_group_field_routing_mark,
        "Override.ProxyGroup.Field.Timeout" to R.string.override_proxy_group_field_timeout,
        "Override.ProxyGroup.Field.Url" to R.string.override_proxy_group_field_url,
        "Override.ProxyGroup.Field.Use" to R.string.override_proxy_group_field_use,
        "Override.ProxyGroup.Field.UseHint" to R.string.override_proxy_group_field_use_hint,
        "Override.Rule.EmptyWarning" to R.string.override_rule_empty_warning,
        "Override.Rule.InvalidFormatWarning" to R.string.override_rule_invalid_format_warning,
        "Override.Rule.MissingTargetWarning" to R.string.override_rule_missing_target_warning,
        "Override.Save.ApplyFailed" to R.string.override_save_apply_failed,
        "Override.Save.Failed" to R.string.override_save_failed,
        "Override.Save.ImportDefaultName" to R.string.override_save_import_default_name,
        "Override.Save.ImportEmpty" to R.string.override_save_import_empty,
        "Override.Save.PresetNotModifiable" to R.string.override_save_preset_not_modifiable,
        "Override.Section.Dns.Summary" to R.string.override_section_dns_summary,
        "Override.Section.Dns.Title" to R.string.override_section_dns_title,
        "Override.Section.General.Summary" to R.string.override_section_general_summary,
        "Override.Section.General.Title" to R.string.override_section_general_title,
        "Override.Section.Inbound.Summary" to R.string.override_section_inbound_summary,
        "Override.Section.Inbound.Title" to R.string.override_section_inbound_title,
        "Override.Section.Proxies.Summary" to R.string.override_section_proxies_summary,
        "Override.Section.Proxies.Title" to R.string.override_section_proxies_title,
        "Override.Section.ProxyGroups.Summary" to R.string.override_section_proxy_groups_summary,
        "Override.Section.ProxyGroups.Title" to R.string.override_section_proxy_groups_title,
        "Override.Section.ProxyProviders.Summary" to R.string.override_section_proxy_providers_summary,
        "Override.Section.ProxyProviders.Title" to R.string.override_section_proxy_providers_title,
        "Override.Section.RuleProviders.Summary" to R.string.override_section_rule_providers_summary,
        "Override.Section.RuleProviders.Title" to R.string.override_section_rule_providers_title,
        "Override.Section.Rules.Summary" to R.string.override_section_rules_summary,
        "Override.Section.Rules.Title" to R.string.override_section_rules_title,
        "Override.Section.Sniffer.Summary" to R.string.override_section_sniffer_summary,
        "Override.Section.Sniffer.Title" to R.string.override_section_sniffer_title,
        "Override.Section.SubRules.Summary" to R.string.override_section_sub_rules_summary,
        "Override.Section.SubRules.Title" to R.string.override_section_sub_rules_title,
        "Override.Section.Tun.Summary" to R.string.override_section_tun_summary,
        "Override.Section.Tun.Title" to R.string.override_section_tun_title,
        "Override.Status.InUse" to R.string.override_status_in_use,
        "Override.Status.NotInUse" to R.string.override_status_not_in_use,
        "Override.Structured.Proxies.EmptyHint" to R.string.override_structured_proxies_empty_hint,
        "Override.Structured.Proxies.ItemLabel" to R.string.override_structured_proxies_item_label,
        "Override.Structured.Proxies.Title" to R.string.override_structured_proxies_title,
        "Override.Structured.ProxyGroups.EmptyHint" to R.string.override_structured_proxy_groups_empty_hint,
        "Override.Structured.ProxyGroups.ItemLabel" to R.string.override_structured_proxy_groups_item_label,
        "Override.Structured.ProxyGroups.Title" to R.string.override_structured_proxy_groups_title,
        "Override.Structured.ProxyProviders.ItemLabel" to R.string.override_structured_proxy_providers_item_label,
        "Override.Structured.ProxyProviders.Title" to R.string.override_structured_proxy_providers_title,
        "Override.Structured.RuleProviders.ItemLabel" to R.string.override_structured_rule_providers_item_label,
        "Override.Structured.RuleProviders.Title" to R.string.override_structured_rule_providers_title,
        "Override.Structured.SubRules.ItemLabel" to R.string.override_structured_sub_rules_item_label,
        "Override.Structured.SubRules.Title" to R.string.override_structured_sub_rules_title,
        "Override.Title" to R.string.override_title,
        "ProfilesPage.Action.AddProfile" to R.string.profiles_page_action_add_profile,
        "ProfilesPage.Action.UpdateAll" to R.string.profiles_page_action_update_all,
        "ProfilesPage.Button.Cancel" to R.string.profiles_page_button_cancel,
        "ProfilesPage.Button.Confirm" to R.string.profiles_page_button_confirm,
        "ProfilesPage.DeleteDialog.Confirm" to R.string.profiles_page_delete_dialog_confirm,
        "ProfilesPage.DeleteDialog.Message" to R.string.profiles_page_delete_dialog_message,
        "ProfilesPage.DeleteDialog.Title" to R.string.profiles_page_delete_dialog_title,
        "ProfilesPage.EditDialog.Title" to R.string.profiles_page_edit_dialog_title,
        "ProfilesPage.Empty.Hint" to R.string.profiles_page_empty_hint,
        "ProfilesPage.Empty.NoProfiles" to R.string.profiles_page_empty_no_profiles,
        "ProfilesPage.Input.NewProfile" to R.string.profiles_page_input_new_profile,
        "ProfilesPage.Input.ProfileName" to R.string.profiles_page_input_profile_name,
        "ProfilesPage.Input.SelectFile" to R.string.profiles_page_input_select_file,
        "ProfilesPage.Input.SubscriptionUrl" to R.string.profiles_page_input_subscription_url,
        "ProfilesPage.Input.SubscriptionUserAgent" to R.string.profiles_page_input_subscription_user_agent,
        "ProfilesPage.Kokoro.Account" to R.string.profiles_page_kokoro_account,
        "ProfilesPage.Kokoro.AvatarDescription" to R.string.profiles_page_kokoro_avatar_description,
        "ProfilesPage.Kokoro.BandwidthLimit" to R.string.profiles_page_kokoro_bandwidth_limit,
        "ProfilesPage.Kokoro.CheckFailed" to R.string.profiles_page_kokoro_check_failed,
        "ProfilesPage.Kokoro.CheckFailedDetail" to R.string.profiles_page_kokoro_check_failed_detail,
        "ProfilesPage.Kokoro.Checking" to R.string.profiles_page_kokoro_checking,
        "ProfilesPage.Kokoro.DecreaseUpdateHours" to R.string.profiles_page_kokoro_decrease_update_hours,
        "ProfilesPage.Kokoro.DefaultProfileName" to R.string.profiles_page_kokoro_default_profile_name,
        "ProfilesPage.Kokoro.Direct" to R.string.profiles_page_kokoro_direct,
        "ProfilesPage.Kokoro.Disabled" to R.string.profiles_page_kokoro_disabled,
        "ProfilesPage.Kokoro.Enabled" to R.string.profiles_page_kokoro_enabled,
        "ProfilesPage.Kokoro.Expires" to R.string.profiles_page_kokoro_expires,
        "ProfilesPage.Kokoro.Fallback" to R.string.profiles_page_kokoro_fallback,
        "ProfilesPage.Kokoro.FinalRoute" to R.string.profiles_page_kokoro_final_route,
        "ProfilesPage.Kokoro.IncreaseUpdateHours" to R.string.profiles_page_kokoro_increase_update_hours,
        "ProfilesPage.Kokoro.InvalidUpdateHours" to R.string.profiles_page_kokoro_invalid_update_hours,
        "ProfilesPage.Kokoro.Isp" to R.string.profiles_page_kokoro_isp,
        "ProfilesPage.Kokoro.IspAuto" to R.string.profiles_page_kokoro_isp_auto,
        "ProfilesPage.Kokoro.IspCm" to R.string.profiles_page_kokoro_isp_cm,
        "ProfilesPage.Kokoro.IspCt" to R.string.profiles_page_kokoro_isp_ct,
        "ProfilesPage.Kokoro.IspCu" to R.string.profiles_page_kokoro_isp_cu,
        "ProfilesPage.Kokoro.IspOther" to R.string.profiles_page_kokoro_isp_other,
        "ProfilesPage.Kokoro.KeepFallback" to R.string.profiles_page_kokoro_keep_fallback,
        "ProfilesPage.Kokoro.LoggedIn" to R.string.profiles_page_kokoro_logged_in,
        "ProfilesPage.Kokoro.LoggedInAs" to R.string.profiles_page_kokoro_logged_in_as,
        "ProfilesPage.Kokoro.LoggedOut" to R.string.profiles_page_kokoro_logged_out,
        "ProfilesPage.Kokoro.Login" to R.string.profiles_page_kokoro_login,
        "ProfilesPage.Kokoro.LoginFailed" to R.string.profiles_page_kokoro_login_failed,
        "ProfilesPage.Kokoro.LoginHint" to R.string.profiles_page_kokoro_login_hint,
        "ProfilesPage.Kokoro.LoginRequired" to R.string.profiles_page_kokoro_login_required,
        "ProfilesPage.Kokoro.Logout" to R.string.profiles_page_kokoro_logout,
        "ProfilesPage.Kokoro.Mirror" to R.string.profiles_page_kokoro_mirror,
        "ProfilesPage.Kokoro.Mode" to R.string.profiles_page_kokoro_mode,
        "ProfilesPage.Kokoro.NoSubscription" to R.string.profiles_page_kokoro_no_subscription,
        "ProfilesPage.Kokoro.Origin" to R.string.profiles_page_kokoro_origin,
        "ProfilesPage.Kokoro.Plan" to R.string.profiles_page_kokoro_plan,
        "ProfilesPage.Kokoro.ProfileUpdate" to R.string.profiles_page_kokoro_profile_update,
        "ProfilesPage.Kokoro.Protocol" to R.string.profiles_page_kokoro_protocol,
        "ProfilesPage.Kokoro.Proxy" to R.string.profiles_page_kokoro_proxy,
        "ProfilesPage.Kokoro.Relay" to R.string.profiles_page_kokoro_relay,
        "ProfilesPage.Kokoro.Retry" to R.string.profiles_page_kokoro_retry,
        "ProfilesPage.Kokoro.Routing" to R.string.profiles_page_kokoro_routing,
        "ProfilesPage.Kokoro.RuleProviderAutoUpdate" to R.string.profiles_page_kokoro_rule_provider_auto_update,
        "ProfilesPage.Kokoro.RuleProviderAutoUpdateSummary" to R.string.profiles_page_kokoro_rule_provider_auto_update_summary,
        "ProfilesPage.Kokoro.RuleSource" to R.string.profiles_page_kokoro_rule_source,
        "ProfilesPage.Kokoro.RuleUpdate" to R.string.profiles_page_kokoro_rule_update,
        "ProfilesPage.Kokoro.SecureTokenSession" to R.string.profiles_page_kokoro_secure_token_session,
        "ProfilesPage.Kokoro.SignInFromSettings" to R.string.profiles_page_kokoro_sign_in_from_settings,
        "ProfilesPage.Kokoro.Subscription" to R.string.profiles_page_kokoro_subscription,
        "ProfilesPage.Kokoro.SubscriptionAutoUpdate" to R.string.profiles_page_kokoro_subscription_auto_update,
        "ProfilesPage.Kokoro.SubscriptionAutoUpdateSummary" to R.string.profiles_page_kokoro_subscription_auto_update_summary,
        "ProfilesPage.Kokoro.SubscriptionNumber" to R.string.profiles_page_kokoro_subscription_number,
        "ProfilesPage.Kokoro.Traffic" to R.string.profiles_page_kokoro_traffic,
        "ProfilesPage.Kokoro.TrafficUsed" to R.string.profiles_page_kokoro_traffic_used,
        "ProfilesPage.Kokoro.Unlimited" to R.string.profiles_page_kokoro_unlimited,
        "ProfilesPage.Kokoro.UpdateCustom" to R.string.profiles_page_kokoro_update_custom,
        "ProfilesPage.Kokoro.UpdateHours" to R.string.profiles_page_kokoro_update_hours,
        "ProfilesPage.Kokoro.UpdateHoursRange" to R.string.profiles_page_kokoro_update_hours_range,
        "ProfilesPage.Kokoro.UpdateHoursValue" to R.string.profiles_page_kokoro_update_hours_value,
        "ProfilesPage.Kokoro.UpdateOff" to R.string.profiles_page_kokoro_update_off,
        "ProfilesPage.Kokoro.UpdateOn" to R.string.profiles_page_kokoro_update_on,
        "ProfilesPage.Kokoro.Updates" to R.string.profiles_page_kokoro_updates,
        "ProfilesPage.Kokoro.VmessRelayOnly" to R.string.profiles_page_kokoro_vmess_relay_only,
        "ProfilesPage.LinkSettings.AddLink" to R.string.profiles_page_link_settings_add_link,
        "ProfilesPage.LinkSettings.Close" to R.string.profiles_page_link_settings_close,
        "ProfilesPage.LinkSettings.DefaultLink" to R.string.profiles_page_link_settings_default_link,
        "ProfilesPage.LinkSettings.DefaultLinkSummary" to R.string.profiles_page_link_settings_default_link_summary,
        "ProfilesPage.LinkSettings.EditLink" to R.string.profiles_page_link_settings_edit_link,
        "ProfilesPage.LinkSettings.Name" to R.string.profiles_page_link_settings_name,
        "ProfilesPage.LinkSettings.OpenMode" to R.string.profiles_page_link_settings_open_mode,
        "ProfilesPage.LinkSettings.OpenModeExternal" to R.string.profiles_page_link_settings_open_mode_external,
        "ProfilesPage.LinkSettings.OpenModeInApp" to R.string.profiles_page_link_settings_open_mode_in_app,
        "ProfilesPage.LinkSettings.Title" to R.string.profiles_page_link_settings_title,
        "ProfilesPage.LinkSettings.Url" to R.string.profiles_page_link_settings_url,
        "ProfilesPage.LinkSettings.Validation.EnterName" to R.string.profiles_page_link_settings_validation_enter_name,
        "ProfilesPage.LinkSettings.Validation.EnterUrl" to R.string.profiles_page_link_settings_validation_enter_url,
        "ProfilesPage.LinkSettings.Validation.InvalidUrl" to R.string.profiles_page_link_settings_validation_invalid_url,
        "ProfilesPage.Message.UnknownFile" to R.string.profiles_page_message_unknown_file,
        "ProfilesPage.Misc.Complete" to R.string.profiles_page_misc_complete,
        "ProfilesPage.Misc.Error" to R.string.profiles_page_misc_error,
        "ProfilesPage.Progress.Downloading" to R.string.profiles_page_progress_downloading,
        "ProfilesPage.QrScanner.NeedCamera" to R.string.profiles_page_qr_scanner_need_camera,
        "ProfilesPage.QrScanner.NeedPermission" to R.string.profiles_page_qr_scanner_need_permission,
        "ProfilesPage.QrScanner.RecognizeError" to R.string.profiles_page_qr_scanner_recognize_error,
        "ProfilesPage.QrScanner.RecognizeFailed" to R.string.profiles_page_qr_scanner_recognize_failed,
        "ProfilesPage.QrScanner.RecognizeSuccess" to R.string.profiles_page_qr_scanner_recognize_success,
        "ProfilesPage.QrScanner.ScanSuccess" to R.string.profiles_page_qr_scanner_scan_success,
        "ProfilesPage.QrScanner.SelectFromAlbum" to R.string.profiles_page_qr_scanner_select_from_album,
        "ProfilesPage.SettingsDialog.ChangeLink" to R.string.profiles_page_settings_dialog_change_link,
        "ProfilesPage.SettingsDialog.ConfigMissing" to R.string.profiles_page_settings_dialog_config_missing,
        "ProfilesPage.SettingsDialog.EditProfile" to R.string.profiles_page_settings_dialog_edit_profile,
        "ProfilesPage.SettingsDialog.EditSettings" to R.string.profiles_page_settings_dialog_edit_settings,
        "ProfilesPage.SettingsDialog.NoDescription" to R.string.profiles_page_settings_dialog_no_description,
        "ProfilesPage.SettingsDialog.OpenConfig" to R.string.profiles_page_settings_dialog_open_config,
        "ProfilesPage.SettingsDialog.SaveFailed" to R.string.profiles_page_settings_dialog_save_failed,
        "ProfilesPage.SettingsDialog.SystemPreset" to R.string.profiles_page_settings_dialog_system_preset,
        "ProfilesPage.SettingsDialog.SystemPresetSummary" to R.string.profiles_page_settings_dialog_system_preset_summary,
        "ProfilesPage.SettingsDialog.Title" to R.string.profiles_page_settings_dialog_title,
        "ProfilesPage.ShareDialog.ImportedConfigMissing" to R.string.profiles_page_share_dialog_imported_config_missing,
        "ProfilesPage.ShareDialog.NoLink" to R.string.profiles_page_share_dialog_no_link,
        "ProfilesPage.ShareDialog.ShareFile" to R.string.profiles_page_share_dialog_share_file,
        "ProfilesPage.ShareDialog.ShareLink" to R.string.profiles_page_share_dialog_share_link,
        "ProfilesPage.ShareDialog.Title" to R.string.profiles_page_share_dialog_title,
        "ProfilesPage.Sheet.AddTitle" to R.string.profiles_page_sheet_add_title,
        "ProfilesPage.Sheet.EditTitle" to R.string.profiles_page_sheet_edit_title,
        "ProfilesPage.Title" to R.string.profiles_page_title,
        "ProfilesPage.Type.Kokoro" to R.string.profiles_page_type_kokoro,
        "ProfilesPage.Type.LocalFile" to R.string.profiles_page_type_local_file,
        "ProfilesPage.Type.QrScan" to R.string.profiles_page_type_qr_scan,
        "ProfilesPage.Type.Subscription" to R.string.profiles_page_type_subscription,
        "ProfilesPage.Type.Title" to R.string.profiles_page_type_title,
        "ProfilesPage.Validation.EnterUrl" to R.string.profiles_page_validation_enter_url,
        "ProfilesPage.Validation.SelectFile" to R.string.profiles_page_validation_select_file,
        "ProfilesPage.Validation.YamlOnly" to R.string.profiles_page_validation_yaml_only,
        "ProfilesVM.Error.ProfileNotExist" to R.string.profiles_vm_error_profile_not_exist,
        "ProfilesVM.Message.AddFailed" to R.string.profiles_vm_message_add_failed,
        "ProfilesVM.Message.DeleteFailed" to R.string.profiles_vm_message_delete_failed,
        "ProfilesVM.Message.ImportFailed" to R.string.profiles_vm_message_import_failed,
        "ProfilesVM.Message.ProfileAdded" to R.string.profiles_vm_message_profile_added,
        "ProfilesVM.Message.ProfileAddedAndActivated" to R.string.profiles_vm_message_profile_added_and_activated,
        "ProfilesVM.Message.ProfileDeleted" to R.string.profiles_vm_message_profile_deleted,
        "ProfilesVM.Message.ProfileImported" to R.string.profiles_vm_message_profile_imported,
        "ProfilesVM.Message.ProfileUpdated" to R.string.profiles_vm_message_profile_updated,
        "ProfilesVM.Message.ToggleFailed" to R.string.profiles_vm_message_toggle_failed,
        "ProfilesVM.Message.UpdateFailed" to R.string.profiles_vm_message_update_failed,
        "ProfilesVM.Progress.ImportComplete" to R.string.profiles_vm_progress_import_complete,
        "ProfilesVM.Progress.ImportPreparing" to R.string.profiles_vm_progress_import_preparing,
        "ProfilesVM.Progress.Preparing" to R.string.profiles_vm_progress_preparing,
        "ProfilesVM.Progress.Verifying" to R.string.profiles_vm_progress_verifying,
        "Providers.Action.Operation" to R.string.providers_action_operation,
        "Providers.Action.Update" to R.string.providers_action_update,
        "Providers.Action.UpdateAll" to R.string.providers_action_update_all,
        "Providers.Action.Upload" to R.string.providers_action_upload,
        "Providers.Empty.NoProviders" to R.string.providers_empty_no_providers,
        "Providers.Empty.NoProvidersHint" to R.string.providers_empty_no_providers_hint,
        "Providers.Empty.NotRunning" to R.string.providers_empty_not_running,
        "Providers.Empty.NotRunningHint" to R.string.providers_empty_not_running_hint,
        "Providers.InfoSummary" to R.string.providers_info_summary,
        "Providers.InfoTitle" to R.string.providers_info_title,
        "Providers.Message.AllUpdated" to R.string.providers_message_all_updated,
        "Providers.Message.FetchFailed" to R.string.providers_message_fetch_failed,
        "Providers.Message.UpdateFailed" to R.string.providers_message_update_failed,
        "Providers.Message.UpdateSuccess" to R.string.providers_message_update_success,
        "Providers.Message.UploadFailed" to R.string.providers_message_upload_failed,
        "Providers.Message.UploadSuccess" to R.string.providers_message_upload_success,
        "Providers.ProviderPath" to R.string.providers_provider_path,
        "Providers.Title" to R.string.providers_title,
        "Providers.Type.ProxyProviders" to R.string.providers_type_proxy_providers,
        "Providers.Type.RuleProviders" to R.string.providers_type_rule_providers,
        "Providers.VehicleType.Compatible" to R.string.providers_vehicle_type_compatible,
        "Providers.VehicleType.File" to R.string.providers_vehicle_type_file,
        "Providers.VehicleType.Http" to R.string.providers_vehicle_type_http,
        "Providers.VehicleType.Inline" to R.string.providers_vehicle_type_inline,
        "Proxy.Action.Sort" to R.string.proxy_action_sort,
        "Proxy.Action.Test" to R.string.proxy_action_test,
        "Proxy.DisplayMode.DoubleDetailed" to R.string.proxy_display_mode_double_detailed,
        "Proxy.DisplayMode.DoubleSimple" to R.string.proxy_display_mode_double_simple,
        "Proxy.DisplayMode.SingleDetailed" to R.string.proxy_display_mode_single_detailed,
        "Proxy.DisplayMode.SingleSimple" to R.string.proxy_display_mode_single_simple,
        "Proxy.Empty.Hint" to R.string.proxy_empty_hint,
        "Proxy.Empty.NoNodes" to R.string.proxy_empty_no_nodes,
        "Proxy.Mode.Direct" to R.string.proxy_mode_direct,
        "Proxy.Mode.Global" to R.string.proxy_mode_global,
        "Proxy.Mode.Rule" to R.string.proxy_mode_rule,
        "Proxy.Mode.SwitchFailed" to R.string.proxy_mode_switch_failed,
        "Proxy.Mode.Switched" to R.string.proxy_mode_switched,
        "Proxy.Mode.Unknown" to R.string.proxy_mode_unknown,
        "Proxy.Node.Count" to R.string.proxy_node_count,
        "Proxy.Node.Timeout" to R.string.proxy_node_timeout,
        "Proxy.Selection.Error" to R.string.proxy_selection_error,
        "Proxy.Selection.Failed" to R.string.proxy_selection_failed,
        "Proxy.Selection.Switched" to R.string.proxy_selection_switched,
        "Proxy.SortMode.ByLatency" to R.string.proxy_sort_mode_by_latency,
        "Proxy.SortMode.ByName" to R.string.proxy_sort_mode_by_name,
        "Proxy.SortMode.Default" to R.string.proxy_sort_mode_default,
        "Proxy.Testing.All" to R.string.proxy_testing_all,
        "Proxy.Testing.Failed" to R.string.proxy_testing_failed,
        "Proxy.Testing.Group" to R.string.proxy_testing_group,
        "Proxy.Testing.InProgress" to R.string.proxy_testing_in_progress,
        "Proxy.Testing.RequestSent" to R.string.proxy_testing_request_sent,
        "Proxy.Title" to R.string.proxy_title,
        "Proxy.Type.Compatible" to R.string.proxy_type_compatible,
        "Proxy.Type.Direct" to R.string.proxy_type_direct,
        "Proxy.Type.Fallback" to R.string.proxy_type_fallback,
        "Proxy.Type.LoadBalance" to R.string.proxy_type_load_balance,
        "Proxy.Type.Pass" to R.string.proxy_type_pass,
        "Proxy.Type.Reject" to R.string.proxy_type_reject,
        "Proxy.Type.RejectDrop" to R.string.proxy_type_reject_drop,
        "Proxy.Type.Relay" to R.string.proxy_type_relay,
        "Proxy.Type.Selector" to R.string.proxy_type_selector,
        "Proxy.Type.Smart" to R.string.proxy_type_smart,
        "Proxy.Type.Unknown" to R.string.proxy_type_unknown,
        "Proxy.Type.UrlTest" to R.string.proxy_type_url_test,
        "Service.AutoRestart.ChannelDescription" to R.string.service_auto_restart_channel_description,
        "Service.AutoRestart.ChannelName" to R.string.service_auto_restart_channel_name,
        "Service.AutoRestart.Checking" to R.string.service_auto_restart_checking,
        "Service.Notification.Running" to R.string.service_notification_running,
        "Service.Notification.SpeedFormat" to R.string.service_notification_speed_format,
        "Service.Notification.TodayTrafficFormat" to R.string.service_notification_today_traffic_format,
        "Service.Notification.TrafficFormat" to R.string.service_notification_traffic_format,
        "Service.Notification.UnknownProfile" to R.string.service_notification_unknown_profile,
        "Service.Tile.ClickToOpen" to R.string.service_tile_click_to_open,
        "Service.Tile.ClickToStartProxy" to R.string.service_tile_click_to_start_proxy,
        "Service.Tile.ClickToStopProxy" to R.string.service_tile_click_to_stop_proxy,
        "Service.Tile.Connecting" to R.string.service_tile_connecting,
        "Service.Tile.Disconnecting" to R.string.service_tile_disconnecting,
        "Settings.DataSettings.AppDataManagement" to R.string.settings_data_settings_app_data_management,
        "Settings.DataSettings.AppDataManagementSummary" to R.string.settings_data_settings_app_data_management_summary,
        "Settings.DataSettings.ExportBackup" to R.string.settings_data_settings_export_backup,
        "Settings.DataSettings.ExportBackupSummary" to R.string.settings_data_settings_export_backup_summary,
        "Settings.DataSettings.ImportBackup" to R.string.settings_data_settings_import_backup,
        "Settings.DataSettings.ImportBackupSummary" to R.string.settings_data_settings_import_backup_summary,
        "Settings.Error.WebviewFailed" to R.string.settings_error_webview_failed,
        "Settings.Kokoro.CustomRules" to R.string.settings_kokoro_custom_rules,
        "Settings.Kokoro.CustomRulesSummary" to R.string.settings_kokoro_custom_rules_summary,
        "Settings.Kokoro.Summary" to R.string.settings_kokoro_summary,
        "Settings.Kokoro.Title" to R.string.settings_kokoro_title,
        "Settings.More.About" to R.string.settings_more_about,
        "Settings.More.AboutSummary" to R.string.settings_more_about_summary,
        "Settings.More.Logs" to R.string.settings_more_logs,
        "Settings.More.LogsSummary" to R.string.settings_more_logs_summary,
        "Settings.NetworkSettings.Lab" to R.string.settings_network_settings_lab,
        "Settings.NetworkSettings.LabSummary" to R.string.settings_network_settings_lab_summary,
        "Settings.NetworkSettings.MetaFeatures" to R.string.settings_network_settings_meta_features,
        "Settings.NetworkSettings.MetaFeaturesSummary" to R.string.settings_network_settings_meta_features_summary,
        "Settings.NetworkSettings.Network" to R.string.settings_network_settings_network,
        "Settings.NetworkSettings.NetworkSummary" to R.string.settings_network_settings_network_summary,
        "Settings.NetworkSettings.Override" to R.string.settings_network_settings_override,
        "Settings.NetworkSettings.OverrideSummary" to R.string.settings_network_settings_override_summary,
        "Settings.Section.DataSettings" to R.string.settings_section_data_settings,
        "Settings.Section.Kokoro" to R.string.settings_section_kokoro,
        "Settings.Section.More" to R.string.settings_section_more,
        "Settings.Section.NetworkSettings" to R.string.settings_section_network_settings,
        "Settings.Section.UiSettings" to R.string.settings_section_ui_settings,
        "Settings.Title" to R.string.settings_title,
        "Settings.UiSettings.App" to R.string.settings_ui_settings_app,
        "Settings.UiSettings.AppSummary" to R.string.settings_ui_settings_app_summary,
        "TrafficStatistics.Action.Clear" to R.string.traffic_statistics_action_clear,
        "TrafficStatistics.Action.ClearConfirmMessage" to R.string.traffic_statistics_action_clear_confirm_message,
        "TrafficStatistics.Action.ClearSuccess" to R.string.traffic_statistics_action_clear_success,
        "TrafficStatistics.Chart.Daily" to R.string.traffic_statistics_chart_daily,
        "TrafficStatistics.Chart.Hourly" to R.string.traffic_statistics_chart_hourly,
        "TrafficStatistics.Compare.LessThanYesterday" to R.string.traffic_statistics_compare_less_than_yesterday,
        "TrafficStatistics.Compare.MoreThanYesterday" to R.string.traffic_statistics_compare_more_than_yesterday,
        "TrafficStatistics.Compare.SameAsYesterday" to R.string.traffic_statistics_compare_same_as_yesterday,
        "TrafficStatistics.Compare.WeekStats" to R.string.traffic_statistics_compare_week_stats,
        "TrafficStatistics.Donut.Other" to R.string.traffic_statistics_donut_other,
        "TrafficStatistics.EntrySummary" to R.string.traffic_statistics_entry_summary,
        "TrafficStatistics.Metric.Download" to R.string.traffic_statistics_metric_download,
        "TrafficStatistics.Metric.Upload" to R.string.traffic_statistics_metric_upload,
        "TrafficStatistics.Metric.UsageLine" to R.string.traffic_statistics_metric_usage_line,
        "TrafficStatistics.Section.EmptyApps" to R.string.traffic_statistics_section_empty_apps,
        "TrafficStatistics.Section.TopApps" to R.string.traffic_statistics_section_top_apps,
        "TrafficStatistics.Section.Traffic" to R.string.traffic_statistics_section_traffic,
        "TrafficStatistics.Summary.TodayTraffic" to R.string.traffic_statistics_summary_today_traffic,
        "TrafficStatistics.Summary.WeekTraffic" to R.string.traffic_statistics_summary_week_traffic,
        "TrafficStatistics.TimeRange.Today" to R.string.traffic_statistics_time_range_today,
        "TrafficStatistics.TimeRange.Week" to R.string.traffic_statistics_time_range_week,
        "TrafficStatistics.Title" to R.string.traffic_statistics_title,
        "Util.Error.UnknownError" to R.string.util_error_unknown_error,
    )

    object About {
        object App {
            val Description: String get() = text("About.App.Description")
            val VersionFailed: String get() = text("About.App.VersionFailed")
            val VersionLoading: String get() = text("About.App.VersionLoading")
        }
        val Copyright: String get() = text("About.Copyright")
        object License {
            val AgplDescription: String get() = text("About.License.AgplDescription")
            val AgplName: String get() = text("About.License.AgplName")
            val CheckUpdate: String get() = text("About.License.CheckUpdate")
            val CheckUpdateSummary: String get() = text("About.License.CheckUpdateSummary")
            val Libraries: String get() = text("About.License.Libraries")
            val LibrariesSummary: String get() = text("About.License.LibrariesSummary")
        }
        object Section {
            val License: String get() = text("About.Section.License")
            val ProjectLinks: String get() = text("About.Section.ProjectLinks")
        }
        val Title: String get() = text("About.Title")
        object Update {
            val Available: String get() = text("About.Update.Available")
            val BrowserDownload: String get() = text("About.Update.BrowserDownload")
            val Checking: String get() = text("About.Update.Checking")
            val ContinueInstall: String get() = text("About.Update.ContinueInstall")
            val Download: String get() = text("About.Update.Download")
            val Downloading: String get() = text("About.Update.Downloading")
            val InAppDownload: String get() = text("About.Update.InAppDownload")
            val InAppDownloadSummary: String get() = text("About.Update.InAppDownloadSummary")
            val InstallPermissionRequired: String get() = text("About.Update.InstallPermissionRequired")
            val Installed: String get() = text("About.Update.Installed")
            val InvalidResponse: String get() = text("About.Update.InvalidResponse")
            val NetworkError: String get() = text("About.Update.NetworkError")
            val NoApk: String get() = text("About.Update.NoApk")
            val NoBrowser: String get() = text("About.Update.NoBrowser")
            val NoRelease: String get() = text("About.Update.NoRelease")
            val Ok: String get() = text("About.Update.Ok")
            val OpenInstallSettings: String get() = text("About.Update.OpenInstallSettings")
            val OpenRelease: String get() = text("About.Update.OpenRelease")
            val PreparingInstall: String get() = text("About.Update.PreparingInstall")
            val RateLimited: String get() = text("About.Update.RateLimited")
            val Retry: String get() = text("About.Update.Retry")
            val UnknownVersion: String get() = text("About.Update.UnknownVersion")
            val UpToDate: String get() = text("About.Update.UpToDate")
            val UpdateFailed: String get() = text("About.Update.UpdateFailed")
            val Verifying: String get() = text("About.Update.Verifying")
            val WaitingForInstallConfirmation: String get() = text("About.Update.WaitingForInstallConfirmation")
        }
    }
    object AccessControl {
        object AppList {
            val Loading: String get() = text("AccessControl.AppList.Loading")
            val Title: String get() = text("AccessControl.AppList.Title")
        }
        object Button {
            val Cancel: String get() = text("AccessControl.Button.Cancel")
            val Confirm: String get() = text("AccessControl.Button.Confirm")
        }
        object Search {
            val Empty: String get() = text("AccessControl.Search.Empty")
            val Placeholder: String get() = text("AccessControl.Search.Placeholder")
        }
        object Settings {
            val BatchOperation: String get() = text("AccessControl.Settings.BatchOperation")
            val ChinaApps: String get() = text("AccessControl.Settings.ChinaApps")
            val DescendingOrder: String get() = text("AccessControl.Settings.DescendingOrder")
            val DeselectAll: String get() = text("AccessControl.Settings.DeselectAll")
            val Export: String get() = text("AccessControl.Settings.Export")
            val ExportSuccess: String get() = text("AccessControl.Settings.ExportSuccess")
            val Import: String get() = text("AccessControl.Settings.Import")
            val ImportExport: String get() = text("AccessControl.Settings.ImportExport")
            val ImportFailed: String get() = text("AccessControl.Settings.ImportFailed")
            val ImportSuccess: String get() = text("AccessControl.Settings.ImportSuccess")
            val Invert: String get() = text("AccessControl.Settings.Invert")
            val OverseasApps: String get() = text("AccessControl.Settings.OverseasApps")
            val RegionQuickSelect: String get() = text("AccessControl.Settings.RegionQuickSelect")
            val RegionSelectResult: String get() = text("AccessControl.Settings.RegionSelectResult")
            val SelectAction: String get() = text("AccessControl.Settings.SelectAction")
            val SelectAll: String get() = text("AccessControl.Settings.SelectAll")
            val SelectedFirst: String get() = text("AccessControl.Settings.SelectedFirst")
            val ShowSystemApps: String get() = text("AccessControl.Settings.ShowSystemApps")
            val SortMode: String get() = text("AccessControl.Settings.SortMode")
            val SortModeCurrent: String get() = text("AccessControl.Settings.SortModeCurrent")
            val Title: String get() = text("AccessControl.Settings.Title")
        }
        object SortMode {
            val InstallTime: String get() = text("AccessControl.SortMode.InstallTime")
            val Label: String get() = text("AccessControl.SortMode.Label")
            val PackageName: String get() = text("AccessControl.SortMode.PackageName")
            val UpdateTime: String get() = text("AccessControl.SortMode.UpdateTime")
        }
        val Title: String get() = text("AccessControl.Title")
    }
    object AppDataManagement {
        object GeoFiles {
            val CacheItemSummary: String get() = text("AppDataManagement.GeoFiles.CacheItemSummary")
            val DeleteComplete: String get() = text("AppDataManagement.GeoFiles.DeleteComplete")
            val DeleteConfirmMessage: String get() = text("AppDataManagement.GeoFiles.DeleteConfirmMessage")
            val DeleteConfirmTitle: String get() = text("AppDataManagement.GeoFiles.DeleteConfirmTitle")
            val EmptyHistory: String get() = text("AppDataManagement.GeoFiles.EmptyHistory")
            val EmptyHistorySummary: String get() = text("AppDataManagement.GeoFiles.EmptyHistorySummary")
            val HistorySummary: String get() = text("AppDataManagement.GeoFiles.HistorySummary")
            val HistoryTitle: String get() = text("AppDataManagement.GeoFiles.HistoryTitle")
        }
        object Logs {
            val DeleteComplete: String get() = text("AppDataManagement.Logs.DeleteComplete")
            val DeleteConfirmMessage: String get() = text("AppDataManagement.Logs.DeleteConfirmMessage")
            val DeleteConfirmTitle: String get() = text("AppDataManagement.Logs.DeleteConfirmTitle")
            val EmptyLogContent: String get() = text("AppDataManagement.Logs.EmptyLogContent")
            val EmptyLogContentSummary: String get() = text("AppDataManagement.Logs.EmptyLogContentSummary")
            val EmptyLogs: String get() = text("AppDataManagement.Logs.EmptyLogs")
            val EmptyLogsSummary: String get() = text("AppDataManagement.Logs.EmptyLogsSummary")
            val LogItemSummary: String get() = text("AppDataManagement.Logs.LogItemSummary")
            val LogLineTitle: String get() = text("AppDataManagement.Logs.LogLineTitle")
            val ManagementSummary: String get() = text("AppDataManagement.Logs.ManagementSummary")
            val ManagementTitle: String get() = text("AppDataManagement.Logs.ManagementTitle")
            val RecordingFileTitle: String get() = text("AppDataManagement.Logs.RecordingFileTitle")
            val ViewerLimitHint: String get() = text("AppDataManagement.Logs.ViewerLimitHint")
            val ViewerLimitSummary: String get() = text("AppDataManagement.Logs.ViewerLimitSummary")
            val ViewerTitle: String get() = text("AppDataManagement.Logs.ViewerTitle")
        }
        object Section {
            val GeoFiles: String get() = text("AppDataManagement.Section.GeoFiles")
            val Logs: String get() = text("AppDataManagement.Section.Logs")
        }
        val Title: String get() = text("AppDataManagement.Title")
    }
    object AppSettings {
        object Backup {
            val ExportFailed: String get() = text("AppSettings.Backup.ExportFailed")
            val ExportFailedDetail: String get() = text("AppSettings.Backup.ExportFailedDetail")
            val ExportSuccess: String get() = text("AppSettings.Backup.ExportSuccess")
            val ExportSummary: String get() = text("AppSettings.Backup.ExportSummary")
            val ExportTitle: String get() = text("AppSettings.Backup.ExportTitle")
            val ImportFailedDetail: String get() = text("AppSettings.Backup.ImportFailedDetail")
            val ImportReadFailed: String get() = text("AppSettings.Backup.ImportReadFailed")
            val ImportSuccess: String get() = text("AppSettings.Backup.ImportSuccess")
            val ImportSummary: String get() = text("AppSettings.Backup.ImportSummary")
            val ImportTitle: String get() = text("AppSettings.Backup.ImportTitle")
        }
        object Behavior {
            val AutoStartSummary: String get() = text("AppSettings.Behavior.AutoStartSummary")
            val AutoStartTitle: String get() = text("AppSettings.Behavior.AutoStartTitle")
            val AutoUpdateOnStartSummary: String get() = text("AppSettings.Behavior.AutoUpdateOnStartSummary")
            val AutoUpdateOnStartTitle: String get() = text("AppSettings.Behavior.AutoUpdateOnStartTitle")
            val AutomaticUpdateCheckSummary: String get() = text("AppSettings.Behavior.AutomaticUpdateCheckSummary")
            val AutomaticUpdateCheckTitle: String get() = text("AppSettings.Behavior.AutomaticUpdateCheckTitle")
            val UpdateChannelNightly: String get() = text("AppSettings.Behavior.UpdateChannelNightly")
            val UpdateChannelStable: String get() = text("AppSettings.Behavior.UpdateChannelStable")
            val UpdateChannelSummary: String get() = text("AppSettings.Behavior.UpdateChannelSummary")
            val UpdateChannelTitle: String get() = text("AppSettings.Behavior.UpdateChannelTitle")
            val UpdateInstallMethodRoot: String get() = text("AppSettings.Behavior.UpdateInstallMethodRoot")
            val UpdateInstallMethodShizuku: String get() = text("AppSettings.Behavior.UpdateInstallMethodShizuku")
            val UpdateInstallMethodSummary: String get() = text("AppSettings.Behavior.UpdateInstallMethodSummary")
            val UpdateInstallMethodSystem: String get() = text("AppSettings.Behavior.UpdateInstallMethodSystem")
            val UpdateInstallMethodTitle: String get() = text("AppSettings.Behavior.UpdateInstallMethodTitle")
        }
        object Button {
            val Apply: String get() = text("AppSettings.Button.Apply")
        }
        object Experimental {
            val AcgHomeSummary: String get() = text("AppSettings.Experimental.AcgHomeSummary")
            val AcgHomeTitle: String get() = text("AppSettings.Experimental.AcgHomeTitle")
            val AcgSidebarExpandedSummary: String get() = text("AppSettings.Experimental.AcgSidebarExpandedSummary")
            val AcgSidebarExpandedTitle: String get() = text("AppSettings.Experimental.AcgSidebarExpandedTitle")
            val HealthCheckConcurrencySummary: String get() = text("AppSettings.Experimental.HealthCheckConcurrencySummary")
            val HealthCheckConcurrencyTitle: String get() = text("AppSettings.Experimental.HealthCheckConcurrencyTitle")
            val ResetWallpaperSuccess: String get() = text("AppSettings.Experimental.ResetWallpaperSuccess")
            val ResetWallpaperSummary: String get() = text("AppSettings.Experimental.ResetWallpaperSummary")
            val ResetWallpaperTitle: String get() = text("AppSettings.Experimental.ResetWallpaperTitle")
            val WallpaperSummary: String get() = text("AppSettings.Experimental.WallpaperSummary")
            val WallpaperTitle: String get() = text("AppSettings.Experimental.WallpaperTitle")
        }
        object Interface {
            val AutoHideNavbarSummary: String get() = text("AppSettings.Interface.AutoHideNavbarSummary")
            val AutoHideNavbarTitle: String get() = text("AppSettings.Interface.AutoHideNavbarTitle")
            val ColorThemeAcgWallpaperSummary: String get() = text("AppSettings.Interface.ColorThemeAcgWallpaperSummary")
            val ColorThemeCodeLabel: String get() = text("AppSettings.Interface.ColorThemeCodeLabel")
            val ColorThemeCustomSummary: String get() = text("AppSettings.Interface.ColorThemeCustomSummary")
            val ColorThemeDynamicSummary: String get() = text("AppSettings.Interface.ColorThemeDynamicSummary")
            val ColorThemeModeAcgWallpaper: String get() = text("AppSettings.Interface.ColorThemeModeAcgWallpaper")
            val ColorThemeModeCustom: String get() = text("AppSettings.Interface.ColorThemeModeCustom")
            val ColorThemeModeMonet: String get() = text("AppSettings.Interface.ColorThemeModeMonet")
            val ColorThemeModeSummary: String get() = text("AppSettings.Interface.ColorThemeModeSummary")
            val ColorThemeModeTitle: String get() = text("AppSettings.Interface.ColorThemeModeTitle")
            val ColorThemePickerTitle: String get() = text("AppSettings.Interface.ColorThemePickerTitle")
            val ColorThemeTitle: String get() = text("AppSettings.Interface.ColorThemeTitle")
            val HomeControlFabSummary: String get() = text("AppSettings.Interface.HomeControlFabSummary")
            val HomeControlFabTitle: String get() = text("AppSettings.Interface.HomeControlFabTitle")
            val LanguageChinese: String get() = text("AppSettings.Interface.LanguageChinese")
            val LanguageEnglish: String get() = text("AppSettings.Interface.LanguageEnglish")
            val LanguageSummary: String get() = text("AppSettings.Interface.LanguageSummary")
            val LanguageSystem: String get() = text("AppSettings.Interface.LanguageSystem")
            val LanguageTitle: String get() = text("AppSettings.Interface.LanguageTitle")
            val LanguageTraditionalChinese: String get() = text("AppSettings.Interface.LanguageTraditionalChinese")
            val LegacyNavbarStyleSummary: String get() = text("AppSettings.Interface.LegacyNavbarStyleSummary")
            val LegacyNavbarStyleTitle: String get() = text("AppSettings.Interface.LegacyNavbarStyleTitle")
            val PageScaleDialogSummary: String get() = text("AppSettings.Interface.PageScaleDialogSummary")
            val PageScaleSummary: String get() = text("AppSettings.Interface.PageScaleSummary")
            val PageScaleTitle: String get() = text("AppSettings.Interface.PageScaleTitle")
            val ThemeColorPolarityInvertSummary: String get() = text("AppSettings.Interface.ThemeColorPolarityInvertSummary")
            val ThemeColorPolarityInvertTitle: String get() = text("AppSettings.Interface.ThemeColorPolarityInvertTitle")
            val ThemeModeDark: String get() = text("AppSettings.Interface.ThemeModeDark")
            val ThemeModeLight: String get() = text("AppSettings.Interface.ThemeModeLight")
            val ThemeModeSummary: String get() = text("AppSettings.Interface.ThemeModeSummary")
            val ThemeModeSystem: String get() = text("AppSettings.Interface.ThemeModeSystem")
            val ThemeModeTitle: String get() = text("AppSettings.Interface.ThemeModeTitle")
        }
        object Privacy {
            val BiometricDialogTitleDisable: String get() = text("AppSettings.Privacy.BiometricDialogTitleDisable")
            val BiometricDialogTitleEnable: String get() = text("AppSettings.Privacy.BiometricDialogTitleEnable")
            val BiometricExitButton: String get() = text("AppSettings.Privacy.BiometricExitButton")
            val BiometricPromptMessage: String get() = text("AppSettings.Privacy.BiometricPromptMessage")
            val BiometricPromptTitle: String get() = text("AppSettings.Privacy.BiometricPromptTitle")
            val BiometricRetryButton: String get() = text("AppSettings.Privacy.BiometricRetryButton")
            val BiometricUnavailableHwUnavailable: String get() = text("AppSettings.Privacy.BiometricUnavailableHwUnavailable")
            val BiometricUnavailableMessage: String get() = text("AppSettings.Privacy.BiometricUnavailableMessage")
            val BiometricUnavailableNoDeviceCredential: String get() = text("AppSettings.Privacy.BiometricUnavailableNoDeviceCredential")
            val BiometricUnavailableNoHardware: String get() = text("AppSettings.Privacy.BiometricUnavailableNoHardware")
            val BiometricUnavailableNoneEnrolled: String get() = text("AppSettings.Privacy.BiometricUnavailableNoneEnrolled")
            val BiometricUnavailableTitle: String get() = text("AppSettings.Privacy.BiometricUnavailableTitle")
            val BiometricUnlockSummary: String get() = text("AppSettings.Privacy.BiometricUnlockSummary")
            val BiometricUnlockTitle: String get() = text("AppSettings.Privacy.BiometricUnlockTitle")
            val HideFromRecentsSummary: String get() = text("AppSettings.Privacy.HideFromRecentsSummary")
            val HideFromRecentsTitle: String get() = text("AppSettings.Privacy.HideFromRecentsTitle")
            val HideIconSummary: String get() = text("AppSettings.Privacy.HideIconSummary")
            val HideIconTitle: String get() = text("AppSettings.Privacy.HideIconTitle")
            val ScreenshotDialogTitleDisable: String get() = text("AppSettings.Privacy.ScreenshotDialogTitleDisable")
            val ScreenshotDialogTitleEnable: String get() = text("AppSettings.Privacy.ScreenshotDialogTitleEnable")
            val ScreenshotProtectionSummary: String get() = text("AppSettings.Privacy.ScreenshotProtectionSummary")
            val ScreenshotProtectionTitle: String get() = text("AppSettings.Privacy.ScreenshotProtectionTitle")
        }
        object Section {
            val Backup: String get() = text("AppSettings.Section.Backup")
            val Behavior: String get() = text("AppSettings.Section.Behavior")
            val Experimental: String get() = text("AppSettings.Section.Experimental")
            val Interface: String get() = text("AppSettings.Section.Interface")
            val Privacy: String get() = text("AppSettings.Section.Privacy")
            val Service: String get() = text("AppSettings.Section.Service")
        }
        object ServiceSection {
            val BatteryOptimizationTitle: String get() = text("AppSettings.ServiceSection.BatteryOptimizationTitle")
            val ExitUiWhenBackgroundSummary: String get() = text("AppSettings.ServiceSection.ExitUiWhenBackgroundSummary")
            val ExitUiWhenBackgroundTitle: String get() = text("AppSettings.ServiceSection.ExitUiWhenBackgroundTitle")
            val SingleNodeTestSummary: String get() = text("AppSettings.ServiceSection.SingleNodeTestSummary")
            val SingleNodeTestTitle: String get() = text("AppSettings.ServiceSection.SingleNodeTestTitle")
            val TrafficNotificationSummary: String get() = text("AppSettings.ServiceSection.TrafficNotificationSummary")
            val TrafficNotificationTitle: String get() = text("AppSettings.ServiceSection.TrafficNotificationTitle")
        }
        val Title: String get() = text("AppSettings.Title")
        object WarningDialog {
            val HideIconMsg1: String get() = text("AppSettings.WarningDialog.HideIconMsg1")
            val HideIconMsg2: String get() = text("AppSettings.WarningDialog.HideIconMsg2")
            val Title: String get() = text("AppSettings.WarningDialog.Title")
        }
    }
    object Component {
        object BottomBar {
            val Config: String get() = text("Component.BottomBar.Config")
            val Home: String get() = text("Component.BottomBar.Home")
            val Proxy: String get() = text("Component.BottomBar.Proxy")
            val Setting: String get() = text("Component.BottomBar.Setting")
        }
        object Button {
            val Cancel: String get() = text("Component.Button.Cancel")
            val Clear: String get() = text("Component.Button.Clear")
            val Confirm: String get() = text("Component.Button.Confirm")
            val Copy: String get() = text("Component.Button.Copy")
            val Delete: String get() = text("Component.Button.Delete")
            val Ok: String get() = text("Component.Button.Ok")
        }
        object ConfigInput {
            val CountItems: String get() = text("Component.ConfigInput.CountItems")
            val PortLabel: String get() = text("Component.ConfigInput.PortLabel")
        }
        object Editor {
            object Action {
                val Add: String get() = text("Component.Editor.Action.Add")
                val Delete: String get() = text("Component.Editor.Action.Delete")
                val Reset: String get() = text("Component.Editor.Action.Reset")
                val Search: String get() = text("Component.Editor.Action.Search")
            }
            val CountItems: String get() = text("Component.Editor.CountItems")
            object Dialog {
                val AddTitle: String get() = text("Component.Editor.Dialog.AddTitle")
                val EditTitle: String get() = text("Component.Editor.Dialog.EditTitle")
                val ResetMessage: String get() = text("Component.Editor.Dialog.ResetMessage")
                val ResetTitle: String get() = text("Component.Editor.Dialog.ResetTitle")
            }
            object Empty {
                val Hint: String get() = text("Component.Editor.Empty.Hint")
                val Title: String get() = text("Component.Editor.Empty.Title")
            }
            object Error {
                val KeyEmpty: String get() = text("Component.Editor.Error.KeyEmpty")
                val KeyExists: String get() = text("Component.Editor.Error.KeyExists")
            }
            object Rule {
                val Content: String get() = text("Component.Editor.Rule.Content")
                val ErrorContentRequired: String get() = text("Component.Editor.Rule.ErrorContentRequired")
                val ErrorTargetRequired: String get() = text("Component.Editor.Rule.ErrorTargetRequired")
                val NoResolve: String get() = text("Component.Editor.Rule.NoResolve")
                val Src: String get() = text("Component.Editor.Rule.Src")
                val Target: String get() = text("Component.Editor.Rule.Target")
                val TargetDirect: String get() = text("Component.Editor.Rule.TargetDirect")
                val TargetMatch: String get() = text("Component.Editor.Rule.TargetMatch")
                val TargetReject: String get() = text("Component.Editor.Rule.TargetReject")
                val Type: String get() = text("Component.Editor.Rule.Type")
            }
        }
        object Flag {
            val ContentDescription: String get() = text("Component.Flag.ContentDescription")
        }
        object Loading {
            val Starting: String get() = text("Component.Loading.Starting")
        }
        object Message {
            val Confirm: String get() = text("Component.Message.Confirm")
            val Error: String get() = text("Component.Message.Error")
            val Hint: String get() = text("Component.Message.Hint")
            val Success: String get() = text("Component.Message.Success")
        }
        object Navigation {
            val Back: String get() = text("Component.Navigation.Back")
            val Refresh: String get() = text("Component.Navigation.Refresh")
        }
        object ProfileCard {
            val ClickToUpdate: String get() = text("Component.ProfileCard.ClickToUpdate")
            val DaysAgo: String get() = text("Component.ProfileCard.DaysAgo")
            val Delete: String get() = text("Component.ProfileCard.Delete")
            val Edit: String get() = text("Component.ProfileCard.Edit")
            val ExpireAt: String get() = text("Component.ProfileCard.ExpireAt")
            val ExpireToday: String get() = text("Component.ProfileCard.ExpireToday")
            val Expired: String get() = text("Component.ProfileCard.Expired")
            val Export: String get() = text("Component.ProfileCard.Export")
            val HoursAgo: String get() = text("Component.ProfileCard.HoursAgo")
            val JustNow: String get() = text("Component.ProfileCard.JustNow")
            val LocalConfig: String get() = text("Component.ProfileCard.LocalConfig")
            val LocalFile: String get() = text("Component.ProfileCard.LocalFile")
            val MinutesAgo: String get() = text("Component.ProfileCard.MinutesAgo")
            val RemoteSubscription: String get() = text("Component.ProfileCard.RemoteSubscription")
            val Traffic: String get() = text("Component.ProfileCard.Traffic")
            val Update: String get() = text("Component.ProfileCard.Update")
            val UsedTraffic: String get() = text("Component.ProfileCard.UsedTraffic")
        }
        object Selector {
            val Append: String get() = text("Component.Selector.Append")
            val Disable: String get() = text("Component.Selector.Disable")
            val Enable: String get() = text("Component.Selector.Enable")
            val Merge: String get() = text("Component.Selector.Merge")
            val NotModify: String get() = text("Component.Selector.NotModify")
            val Prepend: String get() = text("Component.Selector.Prepend")
            val Replace: String get() = text("Component.Selector.Replace")
        }
        object Update {
            object Action {
                val DownloadNow: String get() = text("Component.Update.Action.DownloadNow")
            }
            object Message {
                val Available: String get() = text("Component.Update.Message.Available")
                val CheckFailed: String get() = text("Component.Update.Message.CheckFailed")
                val Checking: String get() = text("Component.Update.Message.Checking")
                val Close: String get() = text("Component.Update.Message.Close")
                val CoverDesc: String get() = text("Component.Update.Message.CoverDesc")
                val CurrentVersion: String get() = text("Component.Update.Message.CurrentVersion")
                val DownloadAlreadyRunning: String get() = text("Component.Update.Message.DownloadAlreadyRunning")
                val DownloadErrorWithCode: String get() = text("Component.Update.Message.DownloadErrorWithCode")
                val DownloadReady: String get() = text("Component.Update.Message.DownloadReady")
                val Downloading: String get() = text("Component.Update.Message.Downloading")
                val DownloadingWithProgress: String get() = text("Component.Update.Message.DownloadingWithProgress")
                val Error: String get() = text("Component.Update.Message.Error")
                val Finished: String get() = text("Component.Update.Message.Finished")
                val InstallFailed: String get() = text("Component.Update.Message.InstallFailed")
                val InstallPromptOpened: String get() = text("Component.Update.Message.InstallPromptOpened")
                val MissingReleaseMetadata: String get() = text("Component.Update.Message.MissingReleaseMetadata")
                val NoCompatibleAsset: String get() = text("Component.Update.Message.NoCompatibleAsset")
                val NoUpdate: String get() = text("Component.Update.Message.NoUpdate")
                val Preparing: String get() = text("Component.Update.Message.Preparing")
                val RemoteVersion: String get() = text("Component.Update.Message.RemoteVersion")
                val Updating: String get() = text("Component.Update.Message.Updating")
                val VerifyFailed: String get() = text("Component.Update.Message.VerifyFailed")
                val Verifying: String get() = text("Component.Update.Message.Verifying")
                val Waiting: String get() = text("Component.Update.Message.Waiting")
            }
            object Title {
                val Available: String get() = text("Component.Update.Title.Available")
            }
        }
        object WebView {
            val InvalidUrl: String get() = text("Component.WebView.InvalidUrl")
        }
    }
    object Connection {
        val ChainCount: String get() = text("Connection.ChainCount")
        object Detail {
            object Action {
                val Interrupt: String get() = text("Connection.Detail.Action.Interrupt")
                val Interrupting: String get() = text("Connection.Detail.Action.Interrupting")
            }
            object Label {
                val Content: String get() = text("Connection.Detail.Label.Content")
                val DestinationAddress: String get() = text("Connection.Detail.Label.DestinationAddress")
                val Download: String get() = text("Connection.Detail.Label.Download")
                val Duration: String get() = text("Connection.Detail.Label.Duration")
                val Process: String get() = text("Connection.Detail.Label.Process")
                val Protocol: String get() = text("Connection.Detail.Label.Protocol")
                val SourceAddress: String get() = text("Connection.Detail.Label.SourceAddress")
                val Type: String get() = text("Connection.Detail.Label.Type")
                val Upload: String get() = text("Connection.Detail.Label.Upload")
            }
            object Section {
                val Info: String get() = text("Connection.Detail.Section.Info")
                val Rule: String get() = text("Connection.Detail.Section.Rule")
            }
        }
        val Empty: String get() = text("Connection.Empty")
        val Loading: String get() = text("Connection.Loading")
        val NoResults: String get() = text("Connection.NoResults")
        object RelativeTime {
            val Date: String get() = text("Connection.RelativeTime.Date")
            val DaysAgo: String get() = text("Connection.RelativeTime.DaysAgo")
            val HoursAgo: String get() = text("Connection.RelativeTime.HoursAgo")
            val JustNow: String get() = text("Connection.RelativeTime.JustNow")
            val MinutesAgo: String get() = text("Connection.RelativeTime.MinutesAgo")
        }
        val Search: String get() = text("Connection.Search")
        val SearchHint: String get() = text("Connection.SearchHint")
        object Sort {
            val Download: String get() = text("Connection.Sort.Download")
            val Host: String get() = text("Connection.Sort.Host")
            val Time: String get() = text("Connection.Sort.Time")
            val Upload: String get() = text("Connection.Sort.Upload")
        }
        val SortBy: String get() = text("Connection.SortBy")
        val Summary: String get() = text("Connection.Summary")
        object Tab {
            val Active: String get() = text("Connection.Tab.Active")
            val Closed: String get() = text("Connection.Tab.Closed")
        }
        val Title: String get() = text("Connection.Title")
    }
    object Editor {
        object Action {
            val Discard: String get() = text("Editor.Action.Discard")
            val Format: String get() = text("Editor.Action.Format")
            val Save: String get() = text("Editor.Action.Save")
        }
        object Common {
            val ConfigPreviewTitle: String get() = text("Editor.Common.ConfigPreviewTitle")
            val EditConfigTitle: String get() = text("Editor.Common.EditConfigTitle")
            val EditOverrideConfigTitle: String get() = text("Editor.Common.EditOverrideConfigTitle")
            val EditProfileConfigTitle: String get() = text("Editor.Common.EditProfileConfigTitle")
            val JsonSubtitle: String get() = text("Editor.Common.JsonSubtitle")
        }
        object Diagnostic {
            val DuplicateKey: String get() = text("Editor.Diagnostic.DuplicateKey")
            val Expected: String get() = text("Editor.Diagnostic.Expected")
            val JsonFormatError: String get() = text("Editor.Diagnostic.JsonFormatError")
            val JsonMustStartWithObjectOrArray: String get() = text("Editor.Diagnostic.JsonMustStartWithObjectOrArray")
            val JsonSyntaxError: String get() = text("Editor.Diagnostic.JsonSyntaxError")
            val NoValue: String get() = text("Editor.Diagnostic.NoValue")
            val Unknown: String get() = text("Editor.Diagnostic.Unknown")
            val Unterminated: String get() = text("Editor.Diagnostic.Unterminated")
        }
        object Dialog {
            val DiscardTitle: String get() = text("Editor.Dialog.DiscardTitle")
            val UnsavedChangesMessage: String get() = text("Editor.Dialog.UnsavedChangesMessage")
            val UnsavedChangesTitle: String get() = text("Editor.Dialog.UnsavedChangesTitle")
        }
        object Toast {
            val FormatFailedOrUnchanged: String get() = text("Editor.Toast.FormatFailedOrUnchanged")
            val FormatSuccess: String get() = text("Editor.Toast.FormatSuccess")
            val SaveFailed: String get() = text("Editor.Toast.SaveFailed")
            val SyntaxError: String get() = text("Editor.Toast.SyntaxError")
        }
    }
    object Feature {
        object Node {
            val HealthCheckConcurrencySummary: String get() = text("Feature.Node.HealthCheckConcurrencySummary")
            val HealthCheckConcurrencyTitle: String get() = text("Feature.Node.HealthCheckConcurrencyTitle")
            val Section: String get() = text("Feature.Node.Section")
        }
        object RuntimeConfig {
            val Empty: String get() = text("Feature.RuntimeConfig.Empty")
            val NotReady: String get() = text("Feature.RuntimeConfig.NotReady")
            val NotRunning: String get() = text("Feature.RuntimeConfig.NotRunning")
            val PreviewTitle: String get() = text("Feature.RuntimeConfig.PreviewTitle")
            val RuntimeChanged: String get() = text("Feature.RuntimeConfig.RuntimeChanged")
            val Section: String get() = text("Feature.RuntimeConfig.Section")
            val Summary: String get() = text("Feature.RuntimeConfig.Summary")
            val Title: String get() = text("Feature.RuntimeConfig.Title")
            val Unavailable: String get() = text("Feature.RuntimeConfig.Unavailable")
            val UnknownProfile: String get() = text("Feature.RuntimeConfig.UnknownProfile")
        }
        object SpeedTest {
            val Cancel: String get() = text("Feature.SpeedTest.Cancel")
            val DataUsage: String get() = text("Feature.SpeedTest.DataUsage")
            val Download: String get() = text("Feature.SpeedTest.Download")
            val EdgeLocation: String get() = text("Feature.SpeedTest.EdgeLocation")
            val Error: String get() = text("Feature.SpeedTest.Error")
            val Jitter: String get() = text("Feature.SpeedTest.Jitter")
            val Latency: String get() = text("Feature.SpeedTest.Latency")
            val LocationUnknown: String get() = text("Feature.SpeedTest.LocationUnknown")
            val Preparing: String get() = text("Feature.SpeedTest.Preparing")
            val PrivacyNotice: String get() = text("Feature.SpeedTest.PrivacyNotice")
            val Section: String get() = text("Feature.SpeedTest.Section")
            val Start: String get() = text("Feature.SpeedTest.Start")
            val Summary: String get() = text("Feature.SpeedTest.Summary")
            val TestingDownload: String get() = text("Feature.SpeedTest.TestingDownload")
            val TestingLatency: String get() = text("Feature.SpeedTest.TestingLatency")
            val TestingUpload: String get() = text("Feature.SpeedTest.TestingUpload")
            val Title: String get() = text("Feature.SpeedTest.Title")
            val Upload: String get() = text("Feature.SpeedTest.Upload")
        }
        val Title: String get() = text("Feature.Title")
    }
    object Home {
        object Control {
            val HintAddProfile: String get() = text("Home.Control.HintAddProfile")
            val HintEnableProfile: String get() = text("Home.Control.HintEnableProfile")
            val HintProfilesLoading: String get() = text("Home.Control.HintProfilesLoading")
            val Start: String get() = text("Home.Control.Start")
            val Stop: String get() = text("Home.Control.Stop")
        }
        object IpInfo {
            val ExitIp: String get() = text("Home.IpInfo.ExitIp")
        }
        object Message {
            val ConfigSwitchFailed: String get() = text("Home.Message.ConfigSwitchFailed")
            val ConfigSwitched: String get() = text("Home.Message.ConfigSwitched")
            val ControlBusy: String get() = text("Home.Message.ControlBusy")
            val Preparing: String get() = text("Home.Message.Preparing")
            val StartFailed: String get() = text("Home.Message.StartFailed")
            val StopFailed: String get() = text("Home.Message.StopFailed")
            val WaitingForVpnPermission: String get() = text("Home.Message.WaitingForVpnPermission")
        }
        object NodeInfo {
            val Delay: String get() = text("Home.NodeInfo.Delay")
            val DelayValue: String get() = text("Home.NodeInfo.DelayValue")
            val Node: String get() = text("Home.NodeInfo.Node")
            val Unknown: String get() = text("Home.NodeInfo.Unknown")
        }
        object ProxyMode {
            val Http: String get() = text("Home.ProxyMode.Http")
            val Tun: String get() = text("Home.ProxyMode.Tun")
            val Vpn: String get() = text("Home.ProxyMode.Vpn")
        }
        object Status {
            val Connecting: String get() = text("Home.Status.Connecting")
            val Disconnecting: String get() = text("Home.Status.Disconnecting")
            val Running: String get() = text("Home.Status.Running")
            val TapFabToStart: String get() = text("Home.Status.TapFabToStart")
            val TapToStart: String get() = text("Home.Status.TapToStart")
        }
        val Title: String get() = text("Home.Title")
        object Traffic {
            val DownShort: String get() = text("Home.Traffic.DownShort")
            val NoProfile: String get() = text("Home.Traffic.NoProfile")
            val UpShort: String get() = text("Home.Traffic.UpShort")
        }
    }
    object Log {
        object Action {
            val Save: String get() = text("Log.Action.Save")
            val StartRecording: String get() = text("Log.Action.StartRecording")
            val StopRecording: String get() = text("Log.Action.StopRecording")
        }
        object Detail {
            val WaitingLog: String get() = text("Log.Detail.WaitingLog")
            val WillShowWhenGenerated: String get() = text("Log.Detail.WillShowWhenGenerated")
        }
        object Empty {
            val NoLogs: String get() = text("Log.Empty.NoLogs")
            val StartRecordingHint: String get() = text("Log.Empty.StartRecordingHint")
        }
        val Title: String get() = text("Log.Title")
    }
    object MetaFeature {
        object AgeKey {
            val DerivePublicKey: String get() = text("MetaFeature.AgeKey.DerivePublicKey")
            val Generate: String get() = text("MetaFeature.AgeKey.Generate")
            val HybridTitle: String get() = text("MetaFeature.AgeKey.HybridTitle")
            val PublicKey: String get() = text("MetaFeature.AgeKey.PublicKey")
            val SecretKey: String get() = text("MetaFeature.AgeKey.SecretKey")
            val Section: String get() = text("MetaFeature.AgeKey.Section")
            val X25519Title: String get() = text("MetaFeature.AgeKey.X25519Title")
        }
        object CustomRules {
            val AddRule: String get() = text("MetaFeature.CustomRules.AddRule")
            val BackToKokoroSettings: String get() = text("MetaFeature.CustomRules.BackToKokoroSettings")
            val Cancel: String get() = text("MetaFeature.CustomRules.Cancel")
            val Confirm: String get() = text("MetaFeature.CustomRules.Confirm")
            val ConflictMessage: String get() = text("MetaFeature.CustomRules.ConflictMessage")
            val ConflictTitle: String get() = text("MetaFeature.CustomRules.ConflictTitle")
            val DeleteRule: String get() = text("MetaFeature.CustomRules.DeleteRule")
            val DiscardMessage: String get() = text("MetaFeature.CustomRules.DiscardMessage")
            val DiscardTitle: String get() = text("MetaFeature.CustomRules.DiscardTitle")
            val EditRule: String get() = text("MetaFeature.CustomRules.EditRule")
            val Empty: String get() = text("MetaFeature.CustomRules.Empty")
            val ErrorLoad: String get() = text("MetaFeature.CustomRules.ErrorLoad")
            val ErrorNotFound: String get() = text("MetaFeature.CustomRules.ErrorNotFound")
            val ErrorRateLimited: String get() = text("MetaFeature.CustomRules.ErrorRateLimited")
            val ErrorRequest: String get() = text("MetaFeature.CustomRules.ErrorRequest")
            val ErrorUnknown: String get() = text("MetaFeature.CustomRules.ErrorUnknown")
            val ErrorValidation: String get() = text("MetaFeature.CustomRules.ErrorValidation")
            val ErrorValidationGeneral: String get() = text("MetaFeature.CustomRules.ErrorValidationGeneral")
            val KeepLocal: String get() = text("MetaFeature.CustomRules.KeepLocal")
            val Loading: String get() = text("MetaFeature.CustomRules.Loading")
            val MatchPayloadHint: String get() = text("MetaFeature.CustomRules.MatchPayloadHint")
            val MoveDown: String get() = text("MetaFeature.CustomRules.MoveDown")
            val MoveUp: String get() = text("MetaFeature.CustomRules.MoveUp")
            val Payload: String get() = text("MetaFeature.CustomRules.Payload")
            val Provider: String get() = text("MetaFeature.CustomRules.Provider")
            val Refresh: String get() = text("MetaFeature.CustomRules.Refresh")
            val Retry: String get() = text("MetaFeature.CustomRules.Retry")
            val Rules: String get() = text("MetaFeature.CustomRules.Rules")
            val Save: String get() = text("MetaFeature.CustomRules.Save")
            val Saved: String get() = text("MetaFeature.CustomRules.Saved")
            val Target: String get() = text("MetaFeature.CustomRules.Target")
            val Title: String get() = text("MetaFeature.CustomRules.Title")
            val Type: String get() = text("MetaFeature.CustomRules.Type")
            val UseRemote: String get() = text("MetaFeature.CustomRules.UseRemote")
        }
        object Download {
            val DialogTitle: String get() = text("MetaFeature.Download.DialogTitle")
            val DownloadComplete: String get() = text("MetaFeature.Download.DownloadComplete")
            val ImportFailed: String get() = text("MetaFeature.Download.ImportFailed")
            val ImportSuccess: String get() = text("MetaFeature.Download.ImportSuccess")
            val LastUpdate: String get() = text("MetaFeature.Download.LastUpdate")
            val LastUpdateNever: String get() = text("MetaFeature.Download.LastUpdateNever")
            val LastUpdateSourceLocal: String get() = text("MetaFeature.Download.LastUpdateSourceLocal")
            val LastUpdateSourceOnline: String get() = text("MetaFeature.Download.LastUpdateSourceOnline")
            val LocalDialogTitle: String get() = text("MetaFeature.Download.LocalDialogTitle")
            val ProgressDetail: String get() = text("MetaFeature.Download.ProgressDetail")
            val ProgressDetailUnknownTotal: String get() = text("MetaFeature.Download.ProgressDetailUnknownTotal")
            val ProgressFailed: String get() = text("MetaFeature.Download.ProgressFailed")
            val ProgressSuccess: String get() = text("MetaFeature.Download.ProgressSuccess")
            val ProgressSummary: String get() = text("MetaFeature.Download.ProgressSummary")
            val ProgressTitle: String get() = text("MetaFeature.Download.ProgressTitle")
            val ProgressWaiting: String get() = text("MetaFeature.Download.ProgressWaiting")
            val SelectFiles: String get() = text("MetaFeature.Download.SelectFiles")
            val StatusDownloading: String get() = text("MetaFeature.Download.StatusDownloading")
            val StatusFailed: String get() = text("MetaFeature.Download.StatusFailed")
            val StatusPending: String get() = text("MetaFeature.Download.StatusPending")
            val StatusSuccess: String get() = text("MetaFeature.Download.StatusSuccess")
            val StatusValidating: String get() = text("MetaFeature.Download.StatusValidating")
        }
        object GeoX {
            val LocalUpdateSummary: String get() = text("MetaFeature.GeoX.LocalUpdateSummary")
            val LocalUpdateTitle: String get() = text("MetaFeature.GeoX.LocalUpdateTitle")
            val OnlineUpdateSummary: String get() = text("MetaFeature.GeoX.OnlineUpdateSummary")
            val OnlineUpdateTitle: String get() = text("MetaFeature.GeoX.OnlineUpdateTitle")
            val RuntimeHomeInfo: String get() = text("MetaFeature.GeoX.RuntimeHomeInfo")
        }
        object Section {
            val ConnectionAndTraffic: String get() = text("MetaFeature.Section.ConnectionAndTraffic")
            val GeoXUpdate: String get() = text("MetaFeature.Section.GeoXUpdate")
        }
        val Title: String get() = text("MetaFeature.Title")
    }
    object NetworkSettings {
        object Error {
            val RootRequired: String get() = text("NetworkSettings.Error.RootRequired")
            val VpnDenied: String get() = text("NetworkSettings.Error.VpnDenied")
        }
        object Experimental {
            val AntiPollutionDnsSummary: String get() = text("NetworkSettings.Experimental.AntiPollutionDnsSummary")
            val AntiPollutionDnsTitle: String get() = text("NetworkSettings.Experimental.AntiPollutionDnsTitle")
        }
        object Network {
            val CustomUserAgentSummaryDefault: String get() = text("NetworkSettings.Network.CustomUserAgentSummaryDefault")
            val CustomUserAgentTitle: String get() = text("NetworkSettings.Network.CustomUserAgentTitle")
            val UserAgentDialogTitle: String get() = text("NetworkSettings.Network.UserAgentDialogTitle")
        }
        object ProxyOptions {
            val AccessControlModeTitle: String get() = text("NetworkSettings.ProxyOptions.AccessControlModeTitle")
            val AllowAll: String get() = text("NetworkSettings.ProxyOptions.AllowAll")
            val AllowSelected: String get() = text("NetworkSettings.ProxyOptions.AllowSelected")
            val ManageAccessControlSummary: String get() = text("NetworkSettings.ProxyOptions.ManageAccessControlSummary")
            val ManageAccessControlTitle: String get() = text("NetworkSettings.ProxyOptions.ManageAccessControlTitle")
            val RejectSelected: String get() = text("NetworkSettings.ProxyOptions.RejectSelected")
            val TunStackTitle: String get() = text("NetworkSettings.ProxyOptions.TunStackTitle")
        }
        object RootTun {
            val AutoRedirectSummary: String get() = text("NetworkSettings.RootTun.AutoRedirectSummary")
            val AutoRedirectTitle: String get() = text("NetworkSettings.RootTun.AutoRedirectTitle")
            val AutoRouteSummary: String get() = text("NetworkSettings.RootTun.AutoRouteSummary")
            val AutoRouteTitle: String get() = text("NetworkSettings.RootTun.AutoRouteTitle")
            val DnsModeFakeIp: String get() = text("NetworkSettings.RootTun.DnsModeFakeIp")
            val DnsModeRedirHost: String get() = text("NetworkSettings.RootTun.DnsModeRedirHost")
            val DnsModeSummary: String get() = text("NetworkSettings.RootTun.DnsModeSummary")
            val DnsModeTitle: String get() = text("NetworkSettings.RootTun.DnsModeTitle")
            val FakeIpRange6Summary: String get() = text("NetworkSettings.RootTun.FakeIpRange6Summary")
            val FakeIpRange6Title: String get() = text("NetworkSettings.RootTun.FakeIpRange6Title")
            val FakeIpRangeSummary: String get() = text("NetworkSettings.RootTun.FakeIpRangeSummary")
            val FakeIpRangeTitle: String get() = text("NetworkSettings.RootTun.FakeIpRangeTitle")
            val IfNameSummary: String get() = text("NetworkSettings.RootTun.IfNameSummary")
            val IfNameTitle: String get() = text("NetworkSettings.RootTun.IfNameTitle")
            val MtuSummary: String get() = text("NetworkSettings.RootTun.MtuSummary")
            val MtuTitle: String get() = text("NetworkSettings.RootTun.MtuTitle")
            val StrictRouteSummary: String get() = text("NetworkSettings.RootTun.StrictRouteSummary")
            val StrictRouteTitle: String get() = text("NetworkSettings.RootTun.StrictRouteTitle")
        }
        object Section {
            val Experimental: String get() = text("NetworkSettings.Section.Experimental")
            val Network: String get() = text("NetworkSettings.Section.Network")
            val ProxyOptions: String get() = text("NetworkSettings.Section.ProxyOptions")
            val VpnOptions: String get() = text("NetworkSettings.Section.VpnOptions")
            val VpnService: String get() = text("NetworkSettings.Section.VpnService")
        }
        val Title: String get() = text("NetworkSettings.Title")
        object VpnOptions {
            val AllowBypassSummary: String get() = text("NetworkSettings.VpnOptions.AllowBypassSummary")
            val AllowBypassTitle: String get() = text("NetworkSettings.VpnOptions.AllowBypassTitle")
            val BypassPrivateSummary: String get() = text("NetworkSettings.VpnOptions.BypassPrivateSummary")
            val BypassPrivateTitle: String get() = text("NetworkSettings.VpnOptions.BypassPrivateTitle")
            val DnsHijackSummary: String get() = text("NetworkSettings.VpnOptions.DnsHijackSummary")
            val DnsHijackTitle: String get() = text("NetworkSettings.VpnOptions.DnsHijackTitle")
            val EnableIpv6Summary: String get() = text("NetworkSettings.VpnOptions.EnableIpv6Summary")
            val EnableIpv6Title: String get() = text("NetworkSettings.VpnOptions.EnableIpv6Title")
            val SystemProxySummary: String get() = text("NetworkSettings.VpnOptions.SystemProxySummary")
            val SystemProxyTitle: String get() = text("NetworkSettings.VpnOptions.SystemProxyTitle")
        }
        object VpnService {
            val RootTunMode: String get() = text("NetworkSettings.VpnService.RootTunMode")
            val RouteTrafficSummary: String get() = text("NetworkSettings.VpnService.RouteTrafficSummary")
            val RouteTrafficTitle: String get() = text("NetworkSettings.VpnService.RouteTrafficTitle")
            val SystemProxy: String get() = text("NetworkSettings.VpnService.SystemProxy")
            val VpnMode: String get() = text("NetworkSettings.VpnService.VpnMode")
        }
    }
    object Onboarding {
        object Finish {
            val Subtitle: String get() = text("Onboarding.Finish.Subtitle")
            val Title: String get() = text("Onboarding.Finish.Title")
        }
        object Navigation {
            val Back: String get() = text("Onboarding.Navigation.Back")
            val Enter: String get() = text("Onboarding.Navigation.Enter")
            val Next: String get() = text("Onboarding.Navigation.Next")
            val Start: String get() = text("Onboarding.Navigation.Start")
        }
        object Permission {
            object AppList {
                val SummaryNeed: String get() = text("Onboarding.Permission.AppList.SummaryNeed")
                val Title: String get() = text("Onboarding.Permission.AppList.Title")
            }
            object Common {
                val Granted: String get() = text("Onboarding.Permission.Common.Granted")
            }
            object Notification {
                val SummaryNeed: String get() = text("Onboarding.Permission.Notification.SummaryNeed")
                val SummaryNotRequired: String get() = text("Onboarding.Permission.Notification.SummaryNotRequired")
                val Title: String get() = text("Onboarding.Permission.Notification.Title")
            }
            val Subtitle: String get() = text("Onboarding.Permission.Subtitle")
            val Title: String get() = text("Onboarding.Permission.Title")
        }
        object Personalize {
            val Subtitle: String get() = text("Onboarding.Personalize.Subtitle")
            val Title: String get() = text("Onboarding.Personalize.Title")
        }
        object Privacy {
            object Accept {
                val Title: String get() = text("Onboarding.Privacy.Accept.Title")
            }
            val PolicyLink: String get() = text("Onboarding.Privacy.PolicyLink")
            object Privacy {
                val Title: String get() = text("Onboarding.Privacy.Privacy.Title")
            }
            val RichTextLead: String get() = text("Onboarding.Privacy.RichTextLead")
            val RichTextPrefix: String get() = text("Onboarding.Privacy.RichTextPrefix")
            val RichTextSuffix: String get() = text("Onboarding.Privacy.RichTextSuffix")
            val Subtitle: String get() = text("Onboarding.Privacy.Subtitle")
            val Title: String get() = text("Onboarding.Privacy.Title")
        }
        object Sheet {
            val LoadFailed: String get() = text("Onboarding.Sheet.LoadFailed")
            val PrivacyPolicyTitle: String get() = text("Onboarding.Sheet.PrivacyPolicyTitle")
        }
    }
    object OpenSourceLicenses {
        object LicenseSheet {
            val NoContent: String get() = text("OpenSourceLicenses.LicenseSheet.NoContent")
        }
        val Title: String get() = text("OpenSourceLicenses.Title")
    }
    object Override {
        object Action {
            val Create: String get() = text("Override.Action.Create")
            val Import: String get() = text("Override.Action.Import")
            val ImportFile: String get() = text("Override.Action.ImportFile")
            val New: String get() = text("Override.Action.New")
        }
        object Card {
            val Copy: String get() = text("Override.Card.Copy")
            val Delete: String get() = text("Override.Card.Delete")
            val DeleteButton: String get() = text("Override.Card.DeleteButton")
            val Edit: String get() = text("Override.Card.Edit")
            val EditButton: String get() = text("Override.Card.EditButton")
            val Export: String get() = text("Override.Card.Export")
            val NoDescription: String get() = text("Override.Card.NoDescription")
        }
        object Dialog {
            object Button {
                val Cancel: String get() = text("Override.Dialog.Button.Cancel")
                val Delete: String get() = text("Override.Dialog.Button.Delete")
            }
            object Create {
                val Description: String get() = text("Override.Dialog.Create.Description")
                val ImportHint: String get() = text("Override.Dialog.Create.ImportHint")
                val Name: String get() = text("Override.Dialog.Create.Name")
                val Title: String get() = text("Override.Dialog.Create.Title")
            }
            object Delete {
                val InUseMessage: String get() = text("Override.Dialog.Delete.InUseMessage")
                val Message: String get() = text("Override.Dialog.Delete.Message")
                val Title: String get() = text("Override.Dialog.Delete.Title")
            }
            object EditOptions {
                val CodeEditor: String get() = text("Override.Dialog.EditOptions.CodeEditor")
                val Title: String get() = text("Override.Dialog.EditOptions.Title")
                val VisualEditor: String get() = text("Override.Dialog.EditOptions.VisualEditor")
            }
        }
        object Dns {
            val AppendSystem: String get() = text("Override.Dns.AppendSystem")
            val Default: String get() = text("Override.Dns.Default")
            val DefaultHint: String get() = text("Override.Dns.DefaultHint")
            val EnhancedDisable: String get() = text("Override.Dns.EnhancedDisable")
            val EnhancedFakeip: String get() = text("Override.Dns.EnhancedFakeip")
            val EnhancedMapping: String get() = text("Override.Dns.EnhancedMapping")
            val EnhancedMode: String get() = text("Override.Dns.EnhancedMode")
            val EnhancedNotModify: String get() = text("Override.Dns.EnhancedNotModify")
            val FakeipBlacklist: String get() = text("Override.Dns.FakeipBlacklist")
            val FakeipFilter: String get() = text("Override.Dns.FakeipFilter")
            val FakeipFilterHint: String get() = text("Override.Dns.FakeipFilterHint")
            val FakeipFilterMode: String get() = text("Override.Dns.FakeipFilterMode")
            val FakeipWhitelist: String get() = text("Override.Dns.FakeipWhitelist")
            val Fallback: String get() = text("Override.Dns.Fallback")
            val FallbackDomain: String get() = text("Override.Dns.FallbackDomain")
            val FallbackDomainHint: String get() = text("Override.Dns.FallbackDomainHint")
            val FallbackGeoip: String get() = text("Override.Dns.FallbackGeoip")
            val FallbackGeoipCode: String get() = text("Override.Dns.FallbackGeoipCode")
            val FallbackGeoipCodeHint: String get() = text("Override.Dns.FallbackGeoipCodeHint")
            val FallbackHint: String get() = text("Override.Dns.FallbackHint")
            val FallbackIpcidr: String get() = text("Override.Dns.FallbackIpcidr")
            val FallbackIpcidrHint: String get() = text("Override.Dns.FallbackIpcidrHint")
            val Ipv6: String get() = text("Override.Dns.Ipv6")
            val Listen: String get() = text("Override.Dns.Listen")
            val ListenHint: String get() = text("Override.Dns.ListenHint")
            val NameserverPolicy: String get() = text("Override.Dns.NameserverPolicy")
            val NameserverPolicyKey: String get() = text("Override.Dns.NameserverPolicyKey")
            val NameserverPolicyValue: String get() = text("Override.Dns.NameserverPolicyValue")
            val Policy: String get() = text("Override.Dns.Policy")
            val PolicyForceEnable: String get() = text("Override.Dns.PolicyForceEnable")
            val PolicyNotModify: String get() = text("Override.Dns.PolicyNotModify")
            val PolicyUseBuiltin: String get() = text("Override.Dns.PolicyUseBuiltin")
            val PreferH3: String get() = text("Override.Dns.PreferH3")
            val Servers: String get() = text("Override.Dns.Servers")
            val ServersHint: String get() = text("Override.Dns.ServersHint")
            val UseHosts: String get() = text("Override.Dns.UseHosts")
        }
        object Draft {
            val AddExtraField: String get() = text("Override.Draft.AddExtraField")
            val AddHealthCheckField: String get() = text("Override.Draft.AddHealthCheckField")
            val AddOverrideField: String get() = text("Override.Draft.AddOverrideField")
            val Apply: String get() = text("Override.Draft.Apply")
            val BasicIdentity: String get() = text("Override.Draft.BasicIdentity")
            val BasicInfo: String get() = text("Override.Draft.BasicInfo")
            val BasicRouting: String get() = text("Override.Draft.BasicRouting")
            val BooleanOptions: String get() = text("Override.Draft.BooleanOptions")
            val ClickToAddExtraField: String get() = text("Override.Draft.ClickToAddExtraField")
            val ConfigDescription: String get() = text("Override.Draft.ConfigDescription")
            val ConfigName: String get() = text("Override.Draft.ConfigName")
            val ConfigSections: String get() = text("Override.Draft.ConfigSections")
            val CoreSource: String get() = text("Override.Draft.CoreSource")
            val DeleteExtraField: String get() = text("Override.Draft.DeleteExtraField")
            val DoubleValue: String get() = text("Override.Draft.DoubleValue")
            val EditExtraField: String get() = text("Override.Draft.EditExtraField")
            val EditHealthCheckField: String get() = text("Override.Draft.EditHealthCheckField")
            val EditOverrideField: String get() = text("Override.Draft.EditOverrideField")
            val EditSubRules: String get() = text("Override.Draft.EditSubRules")
            val ExtraFields: String get() = text("Override.Draft.ExtraFields")
            val ExtraFieldsConfigured: String get() = text("Override.Draft.ExtraFieldsConfigured")
            val FallbackRegionGroupTitle: String get() = text("Override.Draft.FallbackRegionGroupTitle")
            val GroupTypeFallback: String get() = text("Override.Draft.GroupTypeFallback")
            val GroupTypeTitle: String get() = text("Override.Draft.GroupTypeTitle")
            val GroupTypeUrlTest: String get() = text("Override.Draft.GroupTypeUrlTest")
            val HeaderHint: String get() = text("Override.Draft.HeaderHint")
            val HealthCheckFields: String get() = text("Override.Draft.HealthCheckFields")
            val HealthCheckSwitch: String get() = text("Override.Draft.HealthCheckSwitch")
            val IntValue: String get() = text("Override.Draft.IntValue")
            val JsonFragment: String get() = text("Override.Draft.JsonFragment")
            val KeyNameEmpty: String get() = text("Override.Draft.KeyNameEmpty")
            val Name: String get() = text("Override.Draft.Name")
            val NameRequired: String get() = text("Override.Draft.NameRequired")
            val NetworkAuth: String get() = text("Override.Draft.NetworkAuth")
            val NoRules: String get() = text("Override.Draft.NoRules")
            val Object: String get() = text("Override.Draft.Object")
            val OfficialMrs: String get() = text("Override.Draft.OfficialMrs")
            val OfficialMrsSummary: String get() = text("Override.Draft.OfficialMrsSummary")
            val OverrideFields: String get() = text("Override.Draft.OverrideFields")
            val OverrideSwitch: String get() = text("Override.Draft.OverrideSwitch")
            val PresetApplySummary: String get() = text("Override.Draft.PresetApplySummary")
            val PresetTemplate: String get() = text("Override.Draft.PresetTemplate")
            val RegionalAutoGroup: String get() = text("Override.Draft.RegionalAutoGroup")
            val RuleList: String get() = text("Override.Draft.RuleList")
            val RulesConfigured: String get() = text("Override.Draft.RulesConfigured")
            val Save: String get() = text("Override.Draft.Save")
            val ServiceRouting: String get() = text("Override.Draft.ServiceRouting")
            val StringValue: String get() = text("Override.Draft.StringValue")
            val SubRuleGroup: String get() = text("Override.Draft.SubRuleGroup")
            val UrlTestRegionGroupTitle: String get() = text("Override.Draft.UrlTestRegionGroupTitle")
            val ValueType: String get() = text("Override.Draft.ValueType")
            val ValueTypeMismatch: String get() = text("Override.Draft.ValueTypeMismatch")
        }
        object Edit {
            object Button {
                val Cancel: String get() = text("Override.Edit.Button.Cancel")
                val Discard: String get() = text("Override.Edit.Button.Discard")
            }
            object EmptyName {
                val Summary: String get() = text("Override.Edit.EmptyName.Summary")
                val Title: String get() = text("Override.Edit.EmptyName.Title")
            }
            val PresetApplied: String get() = text("Override.Edit.PresetApplied")
            val TitleEdit: String get() = text("Override.Edit.TitleEdit")
            val TitleNew: String get() = text("Override.Edit.TitleNew")
        }
        object Editor {
            val AddCustom: String get() = text("Override.Editor.AddCustom")
            val AddItem: String get() = text("Override.Editor.AddItem")
            val AddObject: String get() = text("Override.Editor.AddObject")
            val AddSubRuleGroup: String get() = text("Override.Editor.AddSubRuleGroup")
            val AdditionalParams: String get() = text("Override.Editor.AdditionalParams")
            val ArrayItems: String get() = text("Override.Editor.ArrayItems")
            val BasicConnection: String get() = text("Override.Editor.BasicConnection")
            val CancelDelete: String get() = text("Override.Editor.CancelDelete")
            val Clear: String get() = text("Override.Editor.Clear")
            val ClearCurrentMode: String get() = text("Override.Editor.ClearCurrentMode")
            object ClearDialog {
                val Summary: String get() = text("Override.Editor.ClearDialog.Summary")
                val Title: String get() = text("Override.Editor.ClearDialog.Title")
            }
            val ClearMode: String get() = text("Override.Editor.ClearMode")
            val ClearSubRules: String get() = text("Override.Editor.ClearSubRules")
            val Confirm: String get() = text("Override.Editor.Confirm")
            val ContentEmpty: String get() = text("Override.Editor.ContentEmpty")
            val Copy: String get() = text("Override.Editor.Copy")
            val CustomMatchResult: String get() = text("Override.Editor.CustomMatchResult")
            val CustomMember: String get() = text("Override.Editor.CustomMember")
            val CustomProxyGroupTarget: String get() = text("Override.Editor.CustomProxyGroupTarget")
            val CustomSubRuleTarget: String get() = text("Override.Editor.CustomSubRuleTarget")
            val Delete: String get() = text("Override.Editor.Delete")
            val DeleteLastItem: String get() = text("Override.Editor.DeleteLastItem")
            val DeleteSelected: String get() = text("Override.Editor.DeleteSelected")
            val DeleteSelectedRules: String get() = text("Override.Editor.DeleteSelectedRules")
            val DragToSort: String get() = text("Override.Editor.DragToSort")
            val Edit: String get() = text("Override.Editor.Edit")
            val EditItem: String get() = text("Override.Editor.EditItem")
            val EditProxyGroup: String get() = text("Override.Editor.EditProxyGroup")
            val EditProxyNode: String get() = text("Override.Editor.EditProxyNode")
            val EditRule: String get() = text("Override.Editor.EditRule")
            val EditSubRule: String get() = text("Override.Editor.EditSubRule")
            val EditSubRuleGroup: String get() = text("Override.Editor.EditSubRuleGroup")
            val EmptyString: String get() = text("Override.Editor.EmptyString")
            val EnterDeleteMode: String get() = text("Override.Editor.EnterDeleteMode")
            val ExtraParamsHint: String get() = text("Override.Editor.ExtraParamsHint")
            val HealthCheckAndFilter: String get() = text("Override.Editor.HealthCheckAndFilter")
            val JsonBlockSubtitle: String get() = text("Override.Editor.JsonBlockSubtitle")
            val KeyName: String get() = text("Override.Editor.KeyName")
            val List: String get() = text("Override.Editor.List")
            val LogicalRuleHint: String get() = text("Override.Editor.LogicalRuleHint")
            val MatchResult: String get() = text("Override.Editor.MatchResult")
            val MemberSource: String get() = text("Override.Editor.MemberSource")
            object Mode {
                val Title: String get() = text("Override.Editor.Mode.Title")
            }
            val MoveDown: String get() = text("Override.Editor.MoveDown")
            val MoveUp: String get() = text("Override.Editor.MoveUp")
            val NetworkAndRoute: String get() = text("Override.Editor.NetworkAndRoute")
            val New: String get() = text("Override.Editor.New")
            val NewProvider: String get() = text("Override.Editor.NewProvider")
            val NewProxyGroup: String get() = text("Override.Editor.NewProxyGroup")
            val NewProxyNode: String get() = text("Override.Editor.NewProxyNode")
            val NewRule: String get() = text("Override.Editor.NewRule")
            val NewSubRuleGroup: String get() = text("Override.Editor.NewSubRuleGroup")
            val NoRules: String get() = text("Override.Editor.NoRules")
            val ObjectFallbackTitle: String get() = text("Override.Editor.ObjectFallbackTitle")
            val ObjectFieldCount: String get() = text("Override.Editor.ObjectFieldCount")
            val ObjectFieldHint: String get() = text("Override.Editor.ObjectFieldHint")
            val ObjectFields: String get() = text("Override.Editor.ObjectFields")
            val ObjectJsonPlaceholder: String get() = text("Override.Editor.ObjectJsonPlaceholder")
            val ObjectListHint: String get() = text("Override.Editor.ObjectListHint")
            val OneItemPerLine: String get() = text("Override.Editor.OneItemPerLine")
            val OtherExtraParams: String get() = text("Override.Editor.OtherExtraParams")
            val Payload: String get() = text("Override.Editor.Payload")
            val PayloadEmpty: String get() = text("Override.Editor.PayloadEmpty")
            val PortEmptyHint: String get() = text("Override.Editor.PortEmptyHint")
            val ProviderMapHint: String get() = text("Override.Editor.ProviderMapHint")
            val ProxyGroup: String get() = text("Override.Editor.ProxyGroup")
            val ProxyGroupTarget: String get() = text("Override.Editor.ProxyGroupTarget")
            val ProxyNode: String get() = text("Override.Editor.ProxyNode")
            val RuleBody: String get() = text("Override.Editor.RuleBody")
            val RuleEdit: String get() = text("Override.Editor.RuleEdit")
            val RulePlaceholder: String get() = text("Override.Editor.RulePlaceholder")
            val RuleProviderInputHint: String get() = text("Override.Editor.RuleProviderInputHint")
            val RuleType: String get() = text("Override.Editor.RuleType")
            val RuleTypeEmpty: String get() = text("Override.Editor.RuleTypeEmpty")
            val Rules: String get() = text("Override.Editor.Rules")
            val RulesConfiguredInline: String get() = text("Override.Editor.RulesConfiguredInline")
            val SaveProxyGroup: String get() = text("Override.Editor.SaveProxyGroup")
            val SaveProxyNode: String get() = text("Override.Editor.SaveProxyNode")
            val SaveRule: String get() = text("Override.Editor.SaveRule")
            val SelectMatchResult: String get() = text("Override.Editor.SelectMatchResult")
            val SelectProxyGroupMember: String get() = text("Override.Editor.SelectProxyGroupMember")
            val SelectProxyGroupTarget: String get() = text("Override.Editor.SelectProxyGroupTarget")
            val SelectRuleProvider: String get() = text("Override.Editor.SelectRuleProvider")
            val SelectSubRuleTarget: String get() = text("Override.Editor.SelectSubRuleTarget")
            val SubRuleGroupHint: String get() = text("Override.Editor.SubRuleGroupHint")
            val SubRuleName: String get() = text("Override.Editor.SubRuleName")
            val SubRuleTarget: String get() = text("Override.Editor.SubRuleTarget")
            val TargetEmpty: String get() = text("Override.Editor.TargetEmpty")
            val TypeEmpty: String get() = text("Override.Editor.TypeEmpty")
            val Unnamed: String get() = text("Override.Editor.Unnamed")
            val UnnamedProvider: String get() = text("Override.Editor.UnnamedProvider")
            val UnnamedProxyGroup: String get() = text("Override.Editor.UnnamedProxyGroup")
            val UnnamedProxyNode: String get() = text("Override.Editor.UnnamedProxyNode")
            val UnnamedRule: String get() = text("Override.Editor.UnnamedRule")
            val UnnamedSubRuleGroup: String get() = text("Override.Editor.UnnamedSubRuleGroup")
        }
        object Empty {
            val Hint: String get() = text("Override.Empty.Hint")
            val Title: String get() = text("Override.Empty.Title")
        }
        object Export {
            val Failed: String get() = text("Override.Export.Failed")
            val Success: String get() = text("Override.Export.Success")
        }
        object Form {
            val AdvancedJson: String get() = text("Override.Form.AdvancedJson")
            val AllowPrivateNetwork: String get() = text("Override.Form.AllowPrivateNetwork")
            val AllowedIPs: String get() = text("Override.Form.AllowedIPs")
            val ApiSecret: String get() = text("Override.Form.ApiSecret")
            val AutoDetectInterface: String get() = text("Override.Form.AutoDetectInterface")
            val AutoRedirect: String get() = text("Override.Form.AutoRedirect")
            val AutoRoute: String get() = text("Override.Form.AutoRoute")
            val AutoUpdateGeo: String get() = text("Override.Form.AutoUpdateGeo")
            val BasicPolicy: String get() = text("Override.Form.BasicPolicy")
            val BindAddress: String get() = text("Override.Form.BindAddress")
            val CacheLimit: String get() = text("Override.Form.CacheLimit")
            val ConfigPersistence: String get() = text("Override.Form.ConfigPersistence")
            val ConnectionNetwork: String get() = text("Override.Form.ConnectionNetwork")
            val ControllerCors: String get() = text("Override.Form.ControllerCors")
            val DirectFollowPolicy: String get() = text("Override.Form.DirectFollowPolicy")
            val DisableIcmpForward: String get() = text("Override.Form.DisableIcmpForward")
            val DisallowedIPs: String get() = text("Override.Form.DisallowedIPs")
            val DnsBasicParams: String get() = text("Override.Form.DnsBasicParams")
            val DnsBasicSwitch: String get() = text("Override.Form.DnsBasicSwitch")
            val DnsFakeIpRange: String get() = text("Override.Form.DnsFakeIpRange")
            val DnsHijack: String get() = text("Override.Form.DnsHijack")
            val DnsPolicyMode: String get() = text("Override.Form.DnsPolicyMode")
            val DnsUpstream: String get() = text("Override.Form.DnsUpstream")
            val DnsUpstreamServers: String get() = text("Override.Form.DnsUpstreamServers")
            val EnableGso: String get() = text("Override.Form.EnableGso")
            val EndpointIndependentNat: String get() = text("Override.Form.EndpointIndependentNat")
            val ExcludePackage: String get() = text("Override.Form.ExcludePackage")
            val ExternalControl: String get() = text("Override.Form.ExternalControl")
            val ExternalController: String get() = text("Override.Form.ExternalController")
            val ExternalControllerHttps: String get() = text("Override.Form.ExternalControllerHttps")
            val ExternalDoH: String get() = text("Override.Form.ExternalDoH")
            val FakeIpIpv6Range: String get() = text("Override.Form.FakeIpIpv6Range")
            val FakeIpMode: String get() = text("Override.Form.FakeIpMode")
            val FakeIpParams: String get() = text("Override.Form.FakeIpParams")
            val FallbackFilter: String get() = text("Override.Form.FallbackFilter")
            val FallbackParams: String get() = text("Override.Form.FallbackParams")
            val FallbackSwitch: String get() = text("Override.Form.FallbackSwitch")
            val FilterList: String get() = text("Override.Form.FilterList")
            val GeoResources: String get() = text("Override.Form.GeoResources")
            val GeoUpdateInterval: String get() = text("Override.Form.GeoUpdateInterval")
            val GeodataMode: String get() = text("Override.Form.GeodataMode")
            val GeoipUrl: String get() = text("Override.Form.GeoipUrl")
            val GeositeMatcher: String get() = text("Override.Form.GeositeMatcher")
            val GeositeUrl: String get() = text("Override.Form.GeositeUrl")
            val GlobalClientFingerprint: String get() = text("Override.Form.GlobalClientFingerprint")
            val Hours: String get() = text("Override.Form.Hours")
            val HttpPorts: String get() = text("Override.Form.HttpPorts")
            val IncludePackage: String get() = text("Override.Form.IncludePackage")
            val Ipv6Timeout: String get() = text("Override.Form.Ipv6Timeout")
            val ItemsConfigured: String get() = text("Override.Form.ItemsConfigured")
            val LanAccess: String get() = text("Override.Form.LanAccess")
            val LanAddress: String get() = text("Override.Form.LanAddress")
            val MmdbUrl: String get() = text("Override.Form.MmdbUrl")
            val NameserverPolicySection: String get() = text("Override.Form.NameserverPolicySection")
            val NetworkPerfParams: String get() = text("Override.Form.NetworkPerfParams")
            val NetworkPerfSwitch: String get() = text("Override.Form.NetworkPerfSwitch")
            val NotModify: String get() = text("Override.Form.NotModify")
            val OpenAdvancedEdit: String get() = text("Override.Form.OpenAdvancedEdit")
            val OpenAdvancedEditSummary: String get() = text("Override.Form.OpenAdvancedEditSummary")
            val OutboundInterface: String get() = text("Override.Form.OutboundInterface")
            val ProcessMode: String get() = text("Override.Form.ProcessMode")
            val ProxyGroups: String get() = text("Override.Form.ProxyGroups")
            val ProxyGroupsHint: String get() = text("Override.Form.ProxyGroupsHint")
            val ProxyNodes: String get() = text("Override.Form.ProxyNodes")
            val ProxyNodesHint: String get() = text("Override.Form.ProxyNodesHint")
            val ProxyPorts: String get() = text("Override.Form.ProxyPorts")
            val ProxyProviders: String get() = text("Override.Form.ProxyProviders")
            val ProxyProvidersAdvanced: String get() = text("Override.Form.ProxyProvidersAdvanced")
            val ProxyProvidersHint: String get() = text("Override.Form.ProxyProvidersHint")
            val ProxyServerNameserverPolicy: String get() = text("Override.Form.ProxyServerNameserverPolicy")
            val QuicPorts: String get() = text("Override.Form.QuicPorts")
            val RouteAddress: String get() = text("Override.Form.RouteAddress")
            val RouteExcludeAddress: String get() = text("Override.Form.RouteExcludeAddress")
            val RoutingMark: String get() = text("Override.Form.RoutingMark")
            val RuleChain: String get() = text("Override.Form.RuleChain")
            val RuleChainNotSet: String get() = text("Override.Form.RuleChainNotSet")
            val RuleProviders: String get() = text("Override.Form.RuleProviders")
            val RuleProvidersAdvanced: String get() = text("Override.Form.RuleProvidersAdvanced")
            val RuleProvidersHint: String get() = text("Override.Form.RuleProvidersHint")
            val RunAndLog: String get() = text("Override.Form.RunAndLog")
            val RunAndLogExtra: String get() = text("Override.Form.RunAndLogExtra")
            val SaveFakeIpMapping: String get() = text("Override.Form.SaveFakeIpMapping")
            val SaveGroupSelection: String get() = text("Override.Form.SaveGroupSelection")
            val Seconds: String get() = text("Override.Form.Seconds")
            val SkipAndForce: String get() = text("Override.Form.SkipAndForce")
            val SkipAuthIPs: String get() = text("Override.Form.SkipAuthIPs")
            val SkipDstAddress: String get() = text("Override.Form.SkipDstAddress")
            val SkipSrcAddress: String get() = text("Override.Form.SkipSrcAddress")
            val SnifferForceDomain: String get() = text("Override.Form.SnifferForceDomain")
            val SnifferOverride: String get() = text("Override.Form.SnifferOverride")
            val SnifferParsePureIp: String get() = text("Override.Form.SnifferParsePureIp")
            val SnifferPorts: String get() = text("Override.Form.SnifferPorts")
            val SnifferSkipDomain: String get() = text("Override.Form.SnifferSkipDomain")
            val SnifferSwitch: String get() = text("Override.Form.SnifferSwitch")
            val Stack: String get() = text("Override.Form.Stack")
            val StrictRoute: String get() = text("Override.Form.StrictRoute")
            val StructuredEdit: String get() = text("Override.Form.StructuredEdit")
            val SubRules: String get() = text("Override.Form.SubRules")
            val SubRulesAdvanced: String get() = text("Override.Form.SubRulesAdvanced")
            val SubRulesHint: String get() = text("Override.Form.SubRulesHint")
            val TcpConcurrent: String get() = text("Override.Form.TcpConcurrent")
            val TlsPorts: String get() = text("Override.Form.TlsPorts")
            val TunBasicSwitch: String get() = text("Override.Form.TunBasicSwitch")
            val TunRouteAndApps: String get() = text("Override.Form.TunRouteAndApps")
            val UnifiedDelay: String get() = text("Override.Form.UnifiedDelay")
            val UserAuth: String get() = text("Override.Form.UserAuth")
        }
        object General {
            val AllowLan: String get() = text("Override.General.AllowLan")
            val HttpPort: String get() = text("Override.General.HttpPort")
            val Ipv6: String get() = text("Override.General.Ipv6")
            val LogLevel: String get() = text("Override.General.LogLevel")
            val MixedPort: String get() = text("Override.General.MixedPort")
            val ProxyMode: String get() = text("Override.General.ProxyMode")
            val RedirectPort: String get() = text("Override.General.RedirectPort")
            val SocksPort: String get() = text("Override.General.SocksPort")
            val TproxyPort: String get() = text("Override.General.TproxyPort")
        }
        object Import {
            val Failed: String get() = text("Override.Import.Failed")
            val FileError: String get() = text("Override.Import.FileError")
            val ReadError: String get() = text("Override.Import.ReadError")
            val Success: String get() = text("Override.Import.Success")
            val SuccessDefault: String get() = text("Override.Import.SuccessDefault")
        }
        object Label {
            val CacheAlgorithm: String get() = text("Override.Label.CacheAlgorithm")
            val Enable: String get() = text("Override.Label.Enable")
            val FakeIpRange: String get() = text("Override.Label.FakeIpRange")
            val ForceDnsMapping: String get() = text("Override.Label.ForceDnsMapping")
            val ForceDomain: String get() = text("Override.Label.ForceDomain")
            val HttpOverride: String get() = text("Override.Label.HttpOverride")
            val KeepAliveIdle: String get() = text("Override.Label.KeepAliveIdle")
            val KeepAliveInterval: String get() = text("Override.Label.KeepAliveInterval")
            val OverrideDestination: String get() = text("Override.Label.OverrideDestination")
            val ParsePureIp: String get() = text("Override.Label.ParsePureIp")
            val QuicOverride: String get() = text("Override.Label.QuicOverride")
            val RespectRules: String get() = text("Override.Label.RespectRules")
            val RulesReplace: String get() = text("Override.Label.RulesReplace")
            val SkipDomain: String get() = text("Override.Label.SkipDomain")
            val TlsOverride: String get() = text("Override.Label.TlsOverride")
            val UseSystemHosts: String get() = text("Override.Label.UseSystemHosts")
        }
        object Modifier {
            val End: String get() = text("Override.Modifier.End")
            val Force: String get() = text("Override.Modifier.Force")
            val ItemsCount: String get() = text("Override.Modifier.ItemsCount")
            val Merge: String get() = text("Override.Modifier.Merge")
            val NoChanges: String get() = text("Override.Modifier.NoChanges")
            val NotModified: String get() = text("Override.Modifier.NotModified")
            val Replace: String get() = text("Override.Modifier.Replace")
            val Start: String get() = text("Override.Modifier.Start")
        }
        object ProxyGroup {
            object Field {
                val DisableUdp: String get() = text("Override.ProxyGroup.Field.DisableUdp")
                val ExcludeFilter: String get() = text("Override.ProxyGroup.Field.ExcludeFilter")
                val ExcludeType: String get() = text("Override.ProxyGroup.Field.ExcludeType")
                val ExpectedStatus: String get() = text("Override.ProxyGroup.Field.ExpectedStatus")
                val Filter: String get() = text("Override.ProxyGroup.Field.Filter")
                val Hidden: String get() = text("Override.ProxyGroup.Field.Hidden")
                val Icon: String get() = text("Override.ProxyGroup.Field.Icon")
                val IncludeAll: String get() = text("Override.ProxyGroup.Field.IncludeAll")
                val IncludeAllProviders: String get() = text("Override.ProxyGroup.Field.IncludeAllProviders")
                val IncludeAllProxies: String get() = text("Override.ProxyGroup.Field.IncludeAllProxies")
                val InterfaceName: String get() = text("Override.ProxyGroup.Field.InterfaceName")
                val Interval: String get() = text("Override.ProxyGroup.Field.Interval")
                val Lazy: String get() = text("Override.ProxyGroup.Field.Lazy")
                val MaxFailedTimes: String get() = text("Override.ProxyGroup.Field.MaxFailedTimes")
                val Proxies: String get() = text("Override.ProxyGroup.Field.Proxies")
                val RoutingMark: String get() = text("Override.ProxyGroup.Field.RoutingMark")
                val Timeout: String get() = text("Override.ProxyGroup.Field.Timeout")
                val Url: String get() = text("Override.ProxyGroup.Field.Url")
                val Use: String get() = text("Override.ProxyGroup.Field.Use")
                val UseHint: String get() = text("Override.ProxyGroup.Field.UseHint")
            }
        }
        object Rule {
            val EmptyWarning: String get() = text("Override.Rule.EmptyWarning")
            val InvalidFormatWarning: String get() = text("Override.Rule.InvalidFormatWarning")
            val MissingTargetWarning: String get() = text("Override.Rule.MissingTargetWarning")
        }
        object Save {
            val ApplyFailed: String get() = text("Override.Save.ApplyFailed")
            val Failed: String get() = text("Override.Save.Failed")
            val ImportDefaultName: String get() = text("Override.Save.ImportDefaultName")
            val ImportEmpty: String get() = text("Override.Save.ImportEmpty")
            val PresetNotModifiable: String get() = text("Override.Save.PresetNotModifiable")
        }
        object Section {
            object Dns {
                val Summary: String get() = text("Override.Section.Dns.Summary")
                val Title: String get() = text("Override.Section.Dns.Title")
            }
            object General {
                val Summary: String get() = text("Override.Section.General.Summary")
                val Title: String get() = text("Override.Section.General.Title")
            }
            object Inbound {
                val Summary: String get() = text("Override.Section.Inbound.Summary")
                val Title: String get() = text("Override.Section.Inbound.Title")
            }
            object Proxies {
                val Summary: String get() = text("Override.Section.Proxies.Summary")
                val Title: String get() = text("Override.Section.Proxies.Title")
            }
            object ProxyGroups {
                val Summary: String get() = text("Override.Section.ProxyGroups.Summary")
                val Title: String get() = text("Override.Section.ProxyGroups.Title")
            }
            object ProxyProviders {
                val Summary: String get() = text("Override.Section.ProxyProviders.Summary")
                val Title: String get() = text("Override.Section.ProxyProviders.Title")
            }
            object RuleProviders {
                val Summary: String get() = text("Override.Section.RuleProviders.Summary")
                val Title: String get() = text("Override.Section.RuleProviders.Title")
            }
            object Rules {
                val Summary: String get() = text("Override.Section.Rules.Summary")
                val Title: String get() = text("Override.Section.Rules.Title")
            }
            object Sniffer {
                val Summary: String get() = text("Override.Section.Sniffer.Summary")
                val Title: String get() = text("Override.Section.Sniffer.Title")
            }
            object SubRules {
                val Summary: String get() = text("Override.Section.SubRules.Summary")
                val Title: String get() = text("Override.Section.SubRules.Title")
            }
            object Tun {
                val Summary: String get() = text("Override.Section.Tun.Summary")
                val Title: String get() = text("Override.Section.Tun.Title")
            }
        }
        object Status {
            val InUse: String get() = text("Override.Status.InUse")
            val NotInUse: String get() = text("Override.Status.NotInUse")
        }
        object Structured {
            object Proxies {
                val EmptyHint: String get() = text("Override.Structured.Proxies.EmptyHint")
                val ItemLabel: String get() = text("Override.Structured.Proxies.ItemLabel")
                val Title: String get() = text("Override.Structured.Proxies.Title")
            }
            object ProxyGroups {
                val EmptyHint: String get() = text("Override.Structured.ProxyGroups.EmptyHint")
                val ItemLabel: String get() = text("Override.Structured.ProxyGroups.ItemLabel")
                val Title: String get() = text("Override.Structured.ProxyGroups.Title")
            }
            object ProxyProviders {
                val ItemLabel: String get() = text("Override.Structured.ProxyProviders.ItemLabel")
                val Title: String get() = text("Override.Structured.ProxyProviders.Title")
            }
            object RuleProviders {
                val ItemLabel: String get() = text("Override.Structured.RuleProviders.ItemLabel")
                val Title: String get() = text("Override.Structured.RuleProviders.Title")
            }
            object SubRules {
                val ItemLabel: String get() = text("Override.Structured.SubRules.ItemLabel")
                val Title: String get() = text("Override.Structured.SubRules.Title")
            }
        }
        val Title: String get() = text("Override.Title")
    }
    object ProfilesPage {
        object Action {
            val AddProfile: String get() = text("ProfilesPage.Action.AddProfile")
            val UpdateAll: String get() = text("ProfilesPage.Action.UpdateAll")
        }
        object Button {
            val Cancel: String get() = text("ProfilesPage.Button.Cancel")
            val Confirm: String get() = text("ProfilesPage.Button.Confirm")
        }
        object DeleteDialog {
            val Confirm: String get() = text("ProfilesPage.DeleteDialog.Confirm")
            val Message: String get() = text("ProfilesPage.DeleteDialog.Message")
            val Title: String get() = text("ProfilesPage.DeleteDialog.Title")
        }
        object EditDialog {
            val Title: String get() = text("ProfilesPage.EditDialog.Title")
        }
        object Empty {
            val Hint: String get() = text("ProfilesPage.Empty.Hint")
            val NoProfiles: String get() = text("ProfilesPage.Empty.NoProfiles")
        }
        object Input {
            val NewProfile: String get() = text("ProfilesPage.Input.NewProfile")
            val ProfileName: String get() = text("ProfilesPage.Input.ProfileName")
            val SelectFile: String get() = text("ProfilesPage.Input.SelectFile")
            val SubscriptionUrl: String get() = text("ProfilesPage.Input.SubscriptionUrl")
            val SubscriptionUserAgent: String get() = text("ProfilesPage.Input.SubscriptionUserAgent")
        }
        object Kokoro {
            val Account: String get() = text("ProfilesPage.Kokoro.Account")
            val AvatarDescription: String get() = text("ProfilesPage.Kokoro.AvatarDescription")
            val BandwidthLimit: String get() = text("ProfilesPage.Kokoro.BandwidthLimit")
            val CheckFailed: String get() = text("ProfilesPage.Kokoro.CheckFailed")
            val CheckFailedDetail: String get() = text("ProfilesPage.Kokoro.CheckFailedDetail")
            val Checking: String get() = text("ProfilesPage.Kokoro.Checking")
            val DecreaseUpdateHours: String get() = text("ProfilesPage.Kokoro.DecreaseUpdateHours")
            val DefaultProfileName: String get() = text("ProfilesPage.Kokoro.DefaultProfileName")
            val Direct: String get() = text("ProfilesPage.Kokoro.Direct")
            val Disabled: String get() = text("ProfilesPage.Kokoro.Disabled")
            val Enabled: String get() = text("ProfilesPage.Kokoro.Enabled")
            val Expires: String get() = text("ProfilesPage.Kokoro.Expires")
            val Fallback: String get() = text("ProfilesPage.Kokoro.Fallback")
            val FinalRoute: String get() = text("ProfilesPage.Kokoro.FinalRoute")
            val IncreaseUpdateHours: String get() = text("ProfilesPage.Kokoro.IncreaseUpdateHours")
            val InvalidUpdateHours: String get() = text("ProfilesPage.Kokoro.InvalidUpdateHours")
            val Isp: String get() = text("ProfilesPage.Kokoro.Isp")
            val IspAuto: String get() = text("ProfilesPage.Kokoro.IspAuto")
            val IspCm: String get() = text("ProfilesPage.Kokoro.IspCm")
            val IspCt: String get() = text("ProfilesPage.Kokoro.IspCt")
            val IspCu: String get() = text("ProfilesPage.Kokoro.IspCu")
            val IspOther: String get() = text("ProfilesPage.Kokoro.IspOther")
            val KeepFallback: String get() = text("ProfilesPage.Kokoro.KeepFallback")
            val LoggedIn: String get() = text("ProfilesPage.Kokoro.LoggedIn")
            val LoggedInAs: String get() = text("ProfilesPage.Kokoro.LoggedInAs")
            val LoggedOut: String get() = text("ProfilesPage.Kokoro.LoggedOut")
            val Login: String get() = text("ProfilesPage.Kokoro.Login")
            val LoginFailed: String get() = text("ProfilesPage.Kokoro.LoginFailed")
            val LoginHint: String get() = text("ProfilesPage.Kokoro.LoginHint")
            val LoginRequired: String get() = text("ProfilesPage.Kokoro.LoginRequired")
            val Logout: String get() = text("ProfilesPage.Kokoro.Logout")
            val Mirror: String get() = text("ProfilesPage.Kokoro.Mirror")
            val Mode: String get() = text("ProfilesPage.Kokoro.Mode")
            val NoSubscription: String get() = text("ProfilesPage.Kokoro.NoSubscription")
            val Origin: String get() = text("ProfilesPage.Kokoro.Origin")
            val Plan: String get() = text("ProfilesPage.Kokoro.Plan")
            val ProfileUpdate: String get() = text("ProfilesPage.Kokoro.ProfileUpdate")
            val Protocol: String get() = text("ProfilesPage.Kokoro.Protocol")
            val Proxy: String get() = text("ProfilesPage.Kokoro.Proxy")
            val Relay: String get() = text("ProfilesPage.Kokoro.Relay")
            val Retry: String get() = text("ProfilesPage.Kokoro.Retry")
            val Routing: String get() = text("ProfilesPage.Kokoro.Routing")
            val RuleProviderAutoUpdate: String get() = text("ProfilesPage.Kokoro.RuleProviderAutoUpdate")
            val RuleProviderAutoUpdateSummary: String get() = text("ProfilesPage.Kokoro.RuleProviderAutoUpdateSummary")
            val RuleSource: String get() = text("ProfilesPage.Kokoro.RuleSource")
            val RuleUpdate: String get() = text("ProfilesPage.Kokoro.RuleUpdate")
            val SecureTokenSession: String get() = text("ProfilesPage.Kokoro.SecureTokenSession")
            val SignInFromSettings: String get() = text("ProfilesPage.Kokoro.SignInFromSettings")
            val Subscription: String get() = text("ProfilesPage.Kokoro.Subscription")
            val SubscriptionAutoUpdate: String get() = text("ProfilesPage.Kokoro.SubscriptionAutoUpdate")
            val SubscriptionAutoUpdateSummary: String get() = text("ProfilesPage.Kokoro.SubscriptionAutoUpdateSummary")
            val SubscriptionNumber: String get() = text("ProfilesPage.Kokoro.SubscriptionNumber")
            val Traffic: String get() = text("ProfilesPage.Kokoro.Traffic")
            val TrafficUsed: String get() = text("ProfilesPage.Kokoro.TrafficUsed")
            val Unlimited: String get() = text("ProfilesPage.Kokoro.Unlimited")
            val UpdateCustom: String get() = text("ProfilesPage.Kokoro.UpdateCustom")
            val UpdateHours: String get() = text("ProfilesPage.Kokoro.UpdateHours")
            val UpdateHoursRange: String get() = text("ProfilesPage.Kokoro.UpdateHoursRange")
            val UpdateHoursValue: String get() = text("ProfilesPage.Kokoro.UpdateHoursValue")
            val UpdateOff: String get() = text("ProfilesPage.Kokoro.UpdateOff")
            val UpdateOn: String get() = text("ProfilesPage.Kokoro.UpdateOn")
            val Updates: String get() = text("ProfilesPage.Kokoro.Updates")
            val VmessRelayOnly: String get() = text("ProfilesPage.Kokoro.VmessRelayOnly")
        }
        object LinkSettings {
            val AddLink: String get() = text("ProfilesPage.LinkSettings.AddLink")
            val Close: String get() = text("ProfilesPage.LinkSettings.Close")
            val DefaultLink: String get() = text("ProfilesPage.LinkSettings.DefaultLink")
            val DefaultLinkSummary: String get() = text("ProfilesPage.LinkSettings.DefaultLinkSummary")
            val EditLink: String get() = text("ProfilesPage.LinkSettings.EditLink")
            val Name: String get() = text("ProfilesPage.LinkSettings.Name")
            val OpenMode: String get() = text("ProfilesPage.LinkSettings.OpenMode")
            val OpenModeExternal: String get() = text("ProfilesPage.LinkSettings.OpenModeExternal")
            val OpenModeInApp: String get() = text("ProfilesPage.LinkSettings.OpenModeInApp")
            val Title: String get() = text("ProfilesPage.LinkSettings.Title")
            val Url: String get() = text("ProfilesPage.LinkSettings.Url")
            object Validation {
                val EnterName: String get() = text("ProfilesPage.LinkSettings.Validation.EnterName")
                val EnterUrl: String get() = text("ProfilesPage.LinkSettings.Validation.EnterUrl")
                val InvalidUrl: String get() = text("ProfilesPage.LinkSettings.Validation.InvalidUrl")
            }
        }
        object Message {
            val UnknownFile: String get() = text("ProfilesPage.Message.UnknownFile")
        }
        object Misc {
            val Complete: String get() = text("ProfilesPage.Misc.Complete")
            val Error: String get() = text("ProfilesPage.Misc.Error")
        }
        object Progress {
            val Downloading: String get() = text("ProfilesPage.Progress.Downloading")
        }
        object QrScanner {
            val NeedCamera: String get() = text("ProfilesPage.QrScanner.NeedCamera")
            val NeedPermission: String get() = text("ProfilesPage.QrScanner.NeedPermission")
            val RecognizeError: String get() = text("ProfilesPage.QrScanner.RecognizeError")
            val RecognizeFailed: String get() = text("ProfilesPage.QrScanner.RecognizeFailed")
            val RecognizeSuccess: String get() = text("ProfilesPage.QrScanner.RecognizeSuccess")
            val ScanSuccess: String get() = text("ProfilesPage.QrScanner.ScanSuccess")
            val SelectFromAlbum: String get() = text("ProfilesPage.QrScanner.SelectFromAlbum")
        }
        object SettingsDialog {
            val ChangeLink: String get() = text("ProfilesPage.SettingsDialog.ChangeLink")
            val ConfigMissing: String get() = text("ProfilesPage.SettingsDialog.ConfigMissing")
            val EditProfile: String get() = text("ProfilesPage.SettingsDialog.EditProfile")
            val EditSettings: String get() = text("ProfilesPage.SettingsDialog.EditSettings")
            val NoDescription: String get() = text("ProfilesPage.SettingsDialog.NoDescription")
            val OpenConfig: String get() = text("ProfilesPage.SettingsDialog.OpenConfig")
            val SaveFailed: String get() = text("ProfilesPage.SettingsDialog.SaveFailed")
            val SystemPreset: String get() = text("ProfilesPage.SettingsDialog.SystemPreset")
            val SystemPresetSummary: String get() = text("ProfilesPage.SettingsDialog.SystemPresetSummary")
            val Title: String get() = text("ProfilesPage.SettingsDialog.Title")
        }
        object ShareDialog {
            val ImportedConfigMissing: String get() = text("ProfilesPage.ShareDialog.ImportedConfigMissing")
            val NoLink: String get() = text("ProfilesPage.ShareDialog.NoLink")
            val ShareFile: String get() = text("ProfilesPage.ShareDialog.ShareFile")
            val ShareLink: String get() = text("ProfilesPage.ShareDialog.ShareLink")
            val Title: String get() = text("ProfilesPage.ShareDialog.Title")
        }
        object Sheet {
            val AddTitle: String get() = text("ProfilesPage.Sheet.AddTitle")
            val EditTitle: String get() = text("ProfilesPage.Sheet.EditTitle")
        }
        val Title: String get() = text("ProfilesPage.Title")
        object Type {
            val Kokoro: String get() = text("ProfilesPage.Type.Kokoro")
            val LocalFile: String get() = text("ProfilesPage.Type.LocalFile")
            val QrScan: String get() = text("ProfilesPage.Type.QrScan")
            val Subscription: String get() = text("ProfilesPage.Type.Subscription")
            val Title: String get() = text("ProfilesPage.Type.Title")
        }
        object Validation {
            val EnterUrl: String get() = text("ProfilesPage.Validation.EnterUrl")
            val SelectFile: String get() = text("ProfilesPage.Validation.SelectFile")
            val YamlOnly: String get() = text("ProfilesPage.Validation.YamlOnly")
        }
    }
    object ProfilesVM {
        object Error {
            val ProfileNotExist: String get() = text("ProfilesVM.Error.ProfileNotExist")
        }
        object Message {
            val AddFailed: String get() = text("ProfilesVM.Message.AddFailed")
            val DeleteFailed: String get() = text("ProfilesVM.Message.DeleteFailed")
            val ImportFailed: String get() = text("ProfilesVM.Message.ImportFailed")
            val ProfileAdded: String get() = text("ProfilesVM.Message.ProfileAdded")
            val ProfileAddedAndActivated: String get() = text("ProfilesVM.Message.ProfileAddedAndActivated")
            val ProfileDeleted: String get() = text("ProfilesVM.Message.ProfileDeleted")
            val ProfileImported: String get() = text("ProfilesVM.Message.ProfileImported")
            val ProfileUpdated: String get() = text("ProfilesVM.Message.ProfileUpdated")
            val ToggleFailed: String get() = text("ProfilesVM.Message.ToggleFailed")
            val UpdateFailed: String get() = text("ProfilesVM.Message.UpdateFailed")
        }
        object Progress {
            val ImportComplete: String get() = text("ProfilesVM.Progress.ImportComplete")
            val ImportPreparing: String get() = text("ProfilesVM.Progress.ImportPreparing")
            val Preparing: String get() = text("ProfilesVM.Progress.Preparing")
            val Verifying: String get() = text("ProfilesVM.Progress.Verifying")
        }
    }
    object Providers {
        object Action {
            val Operation: String get() = text("Providers.Action.Operation")
            val Update: String get() = text("Providers.Action.Update")
            val UpdateAll: String get() = text("Providers.Action.UpdateAll")
            val Upload: String get() = text("Providers.Action.Upload")
        }
        object Empty {
            val NoProviders: String get() = text("Providers.Empty.NoProviders")
            val NoProvidersHint: String get() = text("Providers.Empty.NoProvidersHint")
            val NotRunning: String get() = text("Providers.Empty.NotRunning")
            val NotRunningHint: String get() = text("Providers.Empty.NotRunningHint")
        }
        val InfoSummary: String get() = text("Providers.InfoSummary")
        val InfoTitle: String get() = text("Providers.InfoTitle")
        object Message {
            val AllUpdated: String get() = text("Providers.Message.AllUpdated")
            val FetchFailed: String get() = text("Providers.Message.FetchFailed")
            val UpdateFailed: String get() = text("Providers.Message.UpdateFailed")
            val UpdateSuccess: String get() = text("Providers.Message.UpdateSuccess")
            val UploadFailed: String get() = text("Providers.Message.UploadFailed")
            val UploadSuccess: String get() = text("Providers.Message.UploadSuccess")
        }
        val ProviderPath: String get() = text("Providers.ProviderPath")
        val Title: String get() = text("Providers.Title")
        object Type {
            val ProxyProviders: String get() = text("Providers.Type.ProxyProviders")
            val RuleProviders: String get() = text("Providers.Type.RuleProviders")
        }
        object VehicleType {
            val Compatible: String get() = text("Providers.VehicleType.Compatible")
            val File: String get() = text("Providers.VehicleType.File")
            val Http: String get() = text("Providers.VehicleType.Http")
            val Inline: String get() = text("Providers.VehicleType.Inline")
        }
    }
    object Proxy {
        object Action {
            val Sort: String get() = text("Proxy.Action.Sort")
            val Test: String get() = text("Proxy.Action.Test")
        }
        object DisplayMode {
            val DoubleDetailed: String get() = text("Proxy.DisplayMode.DoubleDetailed")
            val DoubleSimple: String get() = text("Proxy.DisplayMode.DoubleSimple")
            val SingleDetailed: String get() = text("Proxy.DisplayMode.SingleDetailed")
            val SingleSimple: String get() = text("Proxy.DisplayMode.SingleSimple")
        }
        object Empty {
            val Hint: String get() = text("Proxy.Empty.Hint")
            val NoNodes: String get() = text("Proxy.Empty.NoNodes")
        }
        object Mode {
            val Direct: String get() = text("Proxy.Mode.Direct")
            val Global: String get() = text("Proxy.Mode.Global")
            val Rule: String get() = text("Proxy.Mode.Rule")
            val SwitchFailed: String get() = text("Proxy.Mode.SwitchFailed")
            val Switched: String get() = text("Proxy.Mode.Switched")
            val Unknown: String get() = text("Proxy.Mode.Unknown")
        }
        object Node {
            val Count: String get() = text("Proxy.Node.Count")
            val Timeout: String get() = text("Proxy.Node.Timeout")
        }
        object Selection {
            val Error: String get() = text("Proxy.Selection.Error")
            val Failed: String get() = text("Proxy.Selection.Failed")
            val Switched: String get() = text("Proxy.Selection.Switched")
        }
        object SortMode {
            val ByLatency: String get() = text("Proxy.SortMode.ByLatency")
            val ByName: String get() = text("Proxy.SortMode.ByName")
            val Default: String get() = text("Proxy.SortMode.Default")
        }
        object Testing {
            val All: String get() = text("Proxy.Testing.All")
            val Failed: String get() = text("Proxy.Testing.Failed")
            val Group: String get() = text("Proxy.Testing.Group")
            val InProgress: String get() = text("Proxy.Testing.InProgress")
            val RequestSent: String get() = text("Proxy.Testing.RequestSent")
        }
        val Title: String get() = text("Proxy.Title")
        object Type {
            val Compatible: String get() = text("Proxy.Type.Compatible")
            val Direct: String get() = text("Proxy.Type.Direct")
            val Fallback: String get() = text("Proxy.Type.Fallback")
            val LoadBalance: String get() = text("Proxy.Type.LoadBalance")
            val Pass: String get() = text("Proxy.Type.Pass")
            val Reject: String get() = text("Proxy.Type.Reject")
            val RejectDrop: String get() = text("Proxy.Type.RejectDrop")
            val Relay: String get() = text("Proxy.Type.Relay")
            val Selector: String get() = text("Proxy.Type.Selector")
            val Smart: String get() = text("Proxy.Type.Smart")
            val Unknown: String get() = text("Proxy.Type.Unknown")
            val UrlTest: String get() = text("Proxy.Type.UrlTest")
        }
    }
    object Service {
        object AutoRestart {
            val ChannelDescription: String get() = text("Service.AutoRestart.ChannelDescription")
            val ChannelName: String get() = text("Service.AutoRestart.ChannelName")
            val Checking: String get() = text("Service.AutoRestart.Checking")
        }
        object Notification {
            val Running: String get() = text("Service.Notification.Running")
            val SpeedFormat: String get() = text("Service.Notification.SpeedFormat")
            val TodayTrafficFormat: String get() = text("Service.Notification.TodayTrafficFormat")
            val TrafficFormat: String get() = text("Service.Notification.TrafficFormat")
            val UnknownProfile: String get() = text("Service.Notification.UnknownProfile")
        }
        object Tile {
            val ClickToOpen: String get() = text("Service.Tile.ClickToOpen")
            val ClickToStartProxy: String get() = text("Service.Tile.ClickToStartProxy")
            val ClickToStopProxy: String get() = text("Service.Tile.ClickToStopProxy")
            val Connecting: String get() = text("Service.Tile.Connecting")
            val Disconnecting: String get() = text("Service.Tile.Disconnecting")
        }
    }
    object Settings {
        object DataSettings {
            val AppDataManagement: String get() = text("Settings.DataSettings.AppDataManagement")
            val AppDataManagementSummary: String get() = text("Settings.DataSettings.AppDataManagementSummary")
            val ExportBackup: String get() = text("Settings.DataSettings.ExportBackup")
            val ExportBackupSummary: String get() = text("Settings.DataSettings.ExportBackupSummary")
            val ImportBackup: String get() = text("Settings.DataSettings.ImportBackup")
            val ImportBackupSummary: String get() = text("Settings.DataSettings.ImportBackupSummary")
        }
        object Error {
            val WebviewFailed: String get() = text("Settings.Error.WebviewFailed")
        }
        object Kokoro {
            val CustomRules: String get() = text("Settings.Kokoro.CustomRules")
            val CustomRulesSummary: String get() = text("Settings.Kokoro.CustomRulesSummary")
            val Summary: String get() = text("Settings.Kokoro.Summary")
            val Title: String get() = text("Settings.Kokoro.Title")
        }
        object More {
            val About: String get() = text("Settings.More.About")
            val AboutSummary: String get() = text("Settings.More.AboutSummary")
            val Logs: String get() = text("Settings.More.Logs")
            val LogsSummary: String get() = text("Settings.More.LogsSummary")
        }
        object NetworkSettings {
            val Lab: String get() = text("Settings.NetworkSettings.Lab")
            val LabSummary: String get() = text("Settings.NetworkSettings.LabSummary")
            val MetaFeatures: String get() = text("Settings.NetworkSettings.MetaFeatures")
            val MetaFeaturesSummary: String get() = text("Settings.NetworkSettings.MetaFeaturesSummary")
            val Network: String get() = text("Settings.NetworkSettings.Network")
            val NetworkSummary: String get() = text("Settings.NetworkSettings.NetworkSummary")
            val Override: String get() = text("Settings.NetworkSettings.Override")
            val OverrideSummary: String get() = text("Settings.NetworkSettings.OverrideSummary")
        }
        object Section {
            val DataSettings: String get() = text("Settings.Section.DataSettings")
            val Kokoro: String get() = text("Settings.Section.Kokoro")
            val More: String get() = text("Settings.Section.More")
            val NetworkSettings: String get() = text("Settings.Section.NetworkSettings")
            val UiSettings: String get() = text("Settings.Section.UiSettings")
        }
        val Title: String get() = text("Settings.Title")
        object UiSettings {
            val App: String get() = text("Settings.UiSettings.App")
            val AppSummary: String get() = text("Settings.UiSettings.AppSummary")
        }
    }
    object TrafficStatistics {
        object Action {
            val Clear: String get() = text("TrafficStatistics.Action.Clear")
            val ClearConfirmMessage: String get() = text("TrafficStatistics.Action.ClearConfirmMessage")
            val ClearSuccess: String get() = text("TrafficStatistics.Action.ClearSuccess")
        }
        object Chart {
            val Daily: String get() = text("TrafficStatistics.Chart.Daily")
            val Hourly: String get() = text("TrafficStatistics.Chart.Hourly")
        }
        object Compare {
            val LessThanYesterday: String get() = text("TrafficStatistics.Compare.LessThanYesterday")
            val MoreThanYesterday: String get() = text("TrafficStatistics.Compare.MoreThanYesterday")
            val SameAsYesterday: String get() = text("TrafficStatistics.Compare.SameAsYesterday")
            val WeekStats: String get() = text("TrafficStatistics.Compare.WeekStats")
        }
        object Donut {
            val Other: String get() = text("TrafficStatistics.Donut.Other")
        }
        val EntrySummary: String get() = text("TrafficStatistics.EntrySummary")
        object Metric {
            val Download: String get() = text("TrafficStatistics.Metric.Download")
            val Upload: String get() = text("TrafficStatistics.Metric.Upload")
            val UsageLine: String get() = text("TrafficStatistics.Metric.UsageLine")
        }
        object Section {
            val EmptyApps: String get() = text("TrafficStatistics.Section.EmptyApps")
            val TopApps: String get() = text("TrafficStatistics.Section.TopApps")
            val Traffic: String get() = text("TrafficStatistics.Section.Traffic")
        }
        object Summary {
            val TodayTraffic: String get() = text("TrafficStatistics.Summary.TodayTraffic")
            val WeekTraffic: String get() = text("TrafficStatistics.Summary.WeekTraffic")
        }
        object TimeRange {
            val Today: String get() = text("TrafficStatistics.TimeRange.Today")
            val Week: String get() = text("TrafficStatistics.TimeRange.Week")
        }
        val Title: String get() = text("TrafficStatistics.Title")
    }
    object Util {
        object Error {
            val UnknownError: String get() = text("Util.Error.UnknownError")
        }
    }
}
