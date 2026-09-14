package dev.oom_wg.purejoy.mlang

import java.util.Locale

object MLang {
    private var activeLanguageTag: String = "zh"

    fun updateLocale(locale: Locale) {
        activeLanguageTag = normalize(locale.toLanguageTag())
    }

    fun updateLocale(languageTag: String?) {
        activeLanguageTag = normalize(languageTag)
    }

    private fun normalize(languageTag: String?): String {
        val tag = languageTag?.lowercase()?.replace('_', '-').orEmpty()
        return when {
            tag == "zh-tw" || tag.startsWith("zh-tw-") || tag == "zh-hant" || tag.startsWith("zh-hant-") -> "zh-TW"
            tag == "en" || tag.startsWith("en-") -> "en"
            tag == "zh" || tag.startsWith("zh-") -> "zh"
            else -> "zh"
        }
    }

    private fun text(key: String): String = when (activeLanguageTag) {
        "en" -> TEXT_EN[key]
        "zh" -> TEXT_ZH[key]
        "zh-TW" -> TEXT_ZH_TW[key]
        else -> null
    } ?: TEXT_ZH[key] ?: TEXT_EN[key] ?: key

    private val TEXT_EN = mapOf(
        "About.App.Description" to "A Material You Android client powered by Mihomo",
        "About.App.VersionFailed" to "Failed to load",
        "About.App.VersionLoading" to "Loading...",
        "About.Copyright" to "© 2026 KokoroBox Contributors",
        "About.License.AgplDescription" to "This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License.",
        "About.License.AgplName" to "GNU Affero General Public License v3.0",
        "About.License.CheckUpdate" to "Check for Updates",
        "About.License.CheckUpdateSummary" to "Check the latest GitHub release manually",
        "About.License.Libraries" to "Libraries",
        "About.License.LibrariesSummary" to "View all third-party libraries used in this app",
        "About.Section.License" to "License",
        "About.Section.ProjectLinks" to "Project Links",
        "About.Title" to "About",
        "About.Update.Available" to "Update available",
        "About.Update.BrowserDownload" to "Download the latest APK in your browser.",
        "About.Update.Checking" to "Checking GitHub Releases…",
        "About.Update.ContinueInstall" to "Continue",
        "About.Update.Download" to "Download APK",
        "About.Update.Downloading" to "Downloading update…",
        "About.Update.InAppDownload" to "Download and update",
        "About.Update.InAppDownloadSummary" to "The APK will be downloaded, verified, and handed to Android's installer.",
        "About.Update.InstallPermissionRequired" to "Allow KokoroBox to install updates, then continue.",
        "About.Update.Installed" to "Update installation completed.",
        "About.Update.InvalidResponse" to "GitHub returned unsupported release information. Please try again later.",
        "About.Update.NetworkError" to "Unable to check for updates. Check your connection and try again.",
        "About.Update.NoApk" to "The compatible APK is not available yet. View the release page or check again later.",
        "About.Update.NoBrowser" to "No app is available to open this download.",
        "About.Update.NoRelease" to "No published stable release is available yet.",
        "About.Update.Ok" to "OK",
        "About.Update.OpenInstallSettings" to "Open settings",
        "About.Update.OpenRelease" to "View release",
        "About.Update.PreparingInstall" to "Preparing the system installer…",
        "About.Update.RateLimited" to "GitHub is limiting requests. Please wait before checking again.",
        "About.Update.Retry" to "Retry",
        "About.Update.UnknownVersion" to "Cannot compare this build’s version. Check GitHub Releases manually.",
        "About.Update.UpToDate" to "You are already using this version or a newer version.",
        "About.Update.UpdateFailed" to "Update failed",
        "About.Update.Verifying" to "Verifying the downloaded APK…",
        "About.Update.WaitingForInstallConfirmation" to "Continue in the Android installation dialog.",
        "AccessControl.AppList.Loading" to "Loading...",
        "AccessControl.AppList.Title" to "App List (%d selected)",
        "AccessControl.Button.Cancel" to "Cancel",
        "AccessControl.Button.Confirm" to "Confirm",
        "AccessControl.Search.Empty" to "No matching apps",
        "AccessControl.Search.Placeholder" to "Search apps...",
        "AccessControl.Settings.BatchOperation" to "Batch Operation",
        "AccessControl.Settings.ChinaApps" to "China Apps",
        "AccessControl.Settings.DescendingOrder" to "Descending Order",
        "AccessControl.Settings.DeselectAll" to "Deselect All",
        "AccessControl.Settings.Export" to "Export to Clipboard",
        "AccessControl.Settings.ExportSuccess" to "Copied %d package names to clipboard",
        "AccessControl.Settings.Import" to "Import from Clipboard",
        "AccessControl.Settings.ImportExport" to "Import/Export",
        "AccessControl.Settings.ImportFailed" to "Import failed",
        "AccessControl.Settings.ImportSuccess" to "Imported: %d package names",
        "AccessControl.Settings.Invert" to "Invert",
        "AccessControl.Settings.OverseasApps" to "Non-China Apps",
        "AccessControl.Settings.RegionQuickSelect" to "Region Quick Select",
        "AccessControl.Settings.RegionSelectResult" to "Quick selected by \"%s\", total %d",
        "AccessControl.Settings.SelectAction" to "Select",
        "AccessControl.Settings.SelectAll" to "Select All",
        "AccessControl.Settings.SelectedFirst" to "Selected Apps First",
        "AccessControl.Settings.ShowSystemApps" to "Show System Apps",
        "AccessControl.Settings.SortMode" to "Sort Mode",
        "AccessControl.Settings.SortModeCurrent" to "Current: %s",
        "AccessControl.Settings.Title" to "Access Control Settings",
        "AccessControl.SortMode.InstallTime" to "Install Time",
        "AccessControl.SortMode.Label" to "App Name",
        "AccessControl.SortMode.PackageName" to "Package Name",
        "AccessControl.SortMode.UpdateTime" to "Update Time",
        "AccessControl.Title" to "Access Control",
        "AppDataManagement.GeoFiles.CacheItemSummary" to "%s · %s",
        "AppDataManagement.GeoFiles.DeleteComplete" to "Deleted %d historical GeoX files",
        "AppDataManagement.GeoFiles.DeleteConfirmMessage" to "Delete %d selected historical GeoX files? This action cannot be undone.",
        "AppDataManagement.GeoFiles.DeleteConfirmTitle" to "Delete GeoX files?",
        "AppDataManagement.GeoFiles.EmptyHistory" to "No historical downloads to clean",
        "AppDataManagement.GeoFiles.EmptyHistorySummary" to "GeoX files that fail validation are deleted immediately and will not appear here",
        "AppDataManagement.GeoFiles.HistorySummary" to "Historical download cache: %d",
        "AppDataManagement.GeoFiles.HistoryTitle" to "Manage GeoX Files",
        "AppDataManagement.Logs.DeleteComplete" to "Deleted %d log files",
        "AppDataManagement.Logs.DeleteConfirmMessage" to "Delete %d selected log files? This action cannot be undone.",
        "AppDataManagement.Logs.DeleteConfirmTitle" to "Delete log files?",
        "AppDataManagement.Logs.EmptyLogContent" to "Log is empty",
        "AppDataManagement.Logs.EmptyLogContentSummary" to "This file has no displayable log entries",
        "AppDataManagement.Logs.EmptyLogs" to "No logs to clean",
        "AppDataManagement.Logs.EmptyLogsSummary" to "After starting recording on the Log page, log files will appear here",
        "AppDataManagement.Logs.LogItemSummary" to "%s · %s",
        "AppDataManagement.Logs.LogLineTitle" to "[%s] [%s]",
        "AppDataManagement.Logs.ManagementSummary" to "Log files: %d",
        "AppDataManagement.Logs.ManagementTitle" to "Manage Logs",
        "AppDataManagement.Logs.RecordingFileTitle" to "%s (recording)",
        "AppDataManagement.Logs.ViewerLimitHint" to "Showing recent logs only",
        "AppDataManagement.Logs.ViewerLimitSummary" to "Currently showing the latest %d entries",
        "AppDataManagement.Logs.ViewerTitle" to "View Log: %s",
        "AppDataManagement.Section.GeoFiles" to "Geo Files",
        "AppDataManagement.Section.Logs" to "Logs",
        "AppDataManagement.Title" to "App Data Management",
        "AppSettings.Backup.ExportFailed" to "Failed to export settings backup",
        "AppSettings.Backup.ExportFailedDetail" to "Failed to export settings backup: %s",
        "AppSettings.Backup.ExportSuccess" to "Settings backup exported",
        "AppSettings.Backup.ExportSummary" to "Save app preferences, network options, profile links, and display settings as a JSON file",
        "AppSettings.Backup.ExportTitle" to "Export Settings Backup",
        "AppSettings.Backup.ImportFailedDetail" to "Failed to import settings backup: %s",
        "AppSettings.Backup.ImportReadFailed" to "Failed to read backup file",
        "AppSettings.Backup.ImportSuccess" to "Settings backup imported",
        "AppSettings.Backup.ImportSummary" to "Restore settings from a KokoroBox backup JSON file",
        "AppSettings.Backup.ImportTitle" to "Import Settings Backup",
        "AppSettings.Behavior.AutoStartSummary" to "Automatically start proxy service on app launch and boot",
        "AppSettings.Behavior.AutoStartTitle" to "Auto Start",
        "AppSettings.Behavior.AutoUpdateOnStartSummary" to "Auto-update the active subscription profile on start",
        "AppSettings.Behavior.AutoUpdateOnStartTitle" to "Update on Start",
        "AppSettings.Behavior.AutomaticUpdateCheckSummary" to "Check GitHub Releases once a day when the app opens",
        "AppSettings.Behavior.AutomaticUpdateCheckTitle" to "Automatically Check for Updates",
        "AppSettings.Behavior.UpdateChannelNightly" to "Nightly",
        "AppSettings.Behavior.UpdateChannelStable" to "Stable",
        "AppSettings.Behavior.UpdateChannelSummary" to "Choose stable releases or development nightly builds",
        "AppSettings.Behavior.UpdateChannelTitle" to "Update Channel",
        "AppSettings.Behavior.UpdateInstallMethodRoot" to "Root",
        "AppSettings.Behavior.UpdateInstallMethodShizuku" to "Shizuku",
        "AppSettings.Behavior.UpdateInstallMethodSummary" to "Advanced: System is recommended. Shizuku and Root bypass Android's install confirmation.",
        "AppSettings.Behavior.UpdateInstallMethodSystem" to "System",
        "AppSettings.Behavior.UpdateInstallMethodTitle" to "Update Installation Method",
        "AppSettings.Button.Apply" to "Apply",
        "AppSettings.Experimental.AcgHomeSummary" to "Enable the new ACG-inspired home layout",
        "AppSettings.Experimental.AcgHomeTitle" to "ACG Experimental Home",
        "AppSettings.Experimental.AcgSidebarExpandedSummary" to "Open the left info sidebar by default on the ACG home page",
        "AppSettings.Experimental.AcgSidebarExpandedTitle" to "Expand ACG Sidebar by Default",
        "AppSettings.Experimental.HealthCheckConcurrencySummary" to "当前选项：%s",
        "AppSettings.Experimental.HealthCheckConcurrencyTitle" to "测速并发数",
        "AppSettings.Experimental.ResetWallpaperSuccess" to "Default wallpaper restored",
        "AppSettings.Experimental.ResetWallpaperSummary" to "Restore the built-in default wallpaper and reset crop position and wallpaper color seed",
        "AppSettings.Experimental.ResetWallpaperTitle" to "Reset ACG Wallpaper",
        "AppSettings.Experimental.WallpaperSummary" to "Choose a custom wallpaper for the ACG home page",
        "AppSettings.Experimental.WallpaperTitle" to "ACG Wallpaper",
        "AppSettings.Interface.AutoHideNavbarSummary" to "Hide navbar on scroll down and show on scroll up",
        "AppSettings.Interface.AutoHideNavbarTitle" to "Auto-hide Navbar",
        "AppSettings.Interface.ColorThemeAcgWallpaperSummary" to "Generate the theme from the main color extracted from the current ACG home wallpaper. The color seed updates automatically after changing and applying a wallpaper.",
        "AppSettings.Interface.ColorThemeCodeLabel" to "Theme Color Code (#RRGGBB)",
        "AppSettings.Interface.ColorThemeCustomSummary" to "Current theme color: %s",
        "AppSettings.Interface.ColorThemeDynamicSummary" to "Currently following system colors",
        "AppSettings.Interface.ColorThemeModeAcgWallpaper" to "From ACG Wallpaper",
        "AppSettings.Interface.ColorThemeModeCustom" to "Custom Theme Color",
        "AppSettings.Interface.ColorThemeModeMonet" to "Follow System",
        "AppSettings.Interface.ColorThemeModeSummary" to "Choose between following system colors and a manual theme seed",
        "AppSettings.Interface.ColorThemeModeTitle" to "Color Source",
        "AppSettings.Interface.ColorThemePickerTitle" to "Pick Theme Color",
        "AppSettings.Interface.ColorThemeTitle" to "Theme Palette",
        "AppSettings.Interface.HomeControlFabSummary" to "Use a floating action button instead of tapping the traffic panel to start or stop the proxy",
        "AppSettings.Interface.HomeControlFabTitle" to "Home Control FAB",
        "AppSettings.Interface.LanguageChinese" to "简体中文",
        "AppSettings.Interface.LanguageEnglish" to "English",
        "AppSettings.Interface.LanguageSummary" to "Choose the app language",
        "AppSettings.Interface.LanguageSystem" to "Follow System",
        "AppSettings.Interface.LanguageTitle" to "App Language",
        "AppSettings.Interface.LanguageTraditionalChinese" to "繁體中文",
        "AppSettings.Interface.LegacyNavbarStyleSummary" to "Floating Navigation Bar Style",
        "AppSettings.Interface.LegacyNavbarStyleTitle" to "Floating Navigation Bar",
        "AppSettings.Interface.PageScaleDialogSummary" to "80% - 120%",
        "AppSettings.Interface.PageScaleSummary" to "Adjust the overall UI scaling ratio of the app",
        "AppSettings.Interface.PageScaleTitle" to "Page Scale",
        "AppSettings.Interface.ThemeColorPolarityInvertSummary" to "If themed colors look odd, try enabling this",
        "AppSettings.Interface.ThemeColorPolarityInvertTitle" to "Invert Theme Foreground",
        "AppSettings.Interface.ThemeModeDark" to "Dark",
        "AppSettings.Interface.ThemeModeLight" to "Light",
        "AppSettings.Interface.ThemeModeSummary" to "Select the app's theme style",
        "AppSettings.Interface.ThemeModeSystem" to "Follow System",
        "AppSettings.Interface.ThemeModeTitle" to "Theme Mode",
        "AppSettings.Privacy.BiometricDialogTitleDisable" to "Disable Biometric Unlock",
        "AppSettings.Privacy.BiometricDialogTitleEnable" to "Enable Biometric Unlock",
        "AppSettings.Privacy.BiometricExitButton" to "Exit",
        "AppSettings.Privacy.BiometricPromptMessage" to "Complete authentication to continue into the app",
        "AppSettings.Privacy.BiometricPromptTitle" to "Verify Identity",
        "AppSettings.Privacy.BiometricRetryButton" to "Retry",
        "AppSettings.Privacy.BiometricUnavailableHwUnavailable" to "Biometric hardware is currently unavailable. Please try again later",
        "AppSettings.Privacy.BiometricUnavailableMessage" to "Authentication is currently unavailable. Please try again later or check device credential and biometric settings",
        "AppSettings.Privacy.BiometricUnavailableNoDeviceCredential" to "No secure screen lock is configured. Set up a PIN, pattern, or password first",
        "AppSettings.Privacy.BiometricUnavailableNoHardware" to "This device does not support biometrics. Enable a secure screen lock or disable this feature",
        "AppSettings.Privacy.BiometricUnavailableNoneEnrolled" to "No biometric data is enrolled yet. Add biometrics in system settings or enable a secure screen lock first",
        "AppSettings.Privacy.BiometricUnavailableTitle" to "Biometric Unavailable",
        "AppSettings.Privacy.BiometricUnlockSummary" to "Require biometric or device credential authentication on app launch",
        "AppSettings.Privacy.BiometricUnlockTitle" to "Biometric Unlock",
        "AppSettings.Privacy.HideFromRecentsSummary" to "Do not show this app in the recent tasks list (recents card)",
        "AppSettings.Privacy.HideFromRecentsTitle" to "Hide Recents Card",
        "AppSettings.Privacy.HideIconSummary" to "After hiding, you can open via dialer *#*#0721#*#*",
        "AppSettings.Privacy.HideIconTitle" to "Hide App Icon",
        "AppSettings.Privacy.ScreenshotDialogTitleDisable" to "Disable Screenshot Protection",
        "AppSettings.Privacy.ScreenshotDialogTitleEnable" to "Enable Screenshot Protection",
        "AppSettings.Privacy.ScreenshotProtectionSummary" to "Block screenshots, screen recording, and recents preview",
        "AppSettings.Privacy.ScreenshotProtectionTitle" to "Screenshot Protection",
        "AppSettings.Section.Backup" to "Backup",
        "AppSettings.Section.Behavior" to "Behavior",
        "AppSettings.Section.Experimental" to "Experimental",
        "AppSettings.Section.Interface" to "Interface",
        "AppSettings.Section.Privacy" to "Privacy",
        "AppSettings.Section.Service" to "Service",
        "AppSettings.ServiceSection.BatteryOptimizationTitle" to "Unrestricted Battery",
        "AppSettings.ServiceSection.ExitUiWhenBackgroundSummary" to "Release the activity UI when the app is fully hidden in background while keeping the proxy service running",
        "AppSettings.ServiceSection.ExitUiWhenBackgroundTitle" to "Release UI In Background",
        "AppSettings.ServiceSection.SingleNodeTestSummary" to "Tap icon on node card to test individual node delay",
        "AppSettings.ServiceSection.SingleNodeTestTitle" to "Single Node Test",
        "AppSettings.ServiceSection.TrafficNotificationSummary" to "Display traffic usage in notification bar",
        "AppSettings.ServiceSection.TrafficNotificationTitle" to "Show Traffic Notification",
        "AppSettings.Title" to "App Settings",
        "AppSettings.WarningDialog.HideIconMsg1" to "Please make sure you can access this app's settings before hiding!",
        "AppSettings.WarningDialog.HideIconMsg2" to "For HyperOS, please enable Auto-start and Background Pop-up permissions to receive dialer codes!",
        "AppSettings.WarningDialog.Title" to "Warning",
        "Component.BottomBar.Config" to "Config",
        "Component.BottomBar.Home" to "Home",
        "Component.BottomBar.Proxy" to "Proxy",
        "Component.BottomBar.Setting" to "Settings",
        "Component.Button.Cancel" to "Cancel",
        "Component.Button.Clear" to "Clear",
        "Component.Button.Confirm" to "Confirm",
        "Component.Button.Copy" to "Copy",
        "Component.Button.Delete" to "Delete",
        "Component.Button.Ok" to "OK",
        "Component.ConfigInput.CountItems" to "%d items",
        "Component.ConfigInput.PortLabel" to "Port (leave empty to not modify)",
        "Component.Editor.Action.Add" to "Add",
        "Component.Editor.Action.Delete" to "Delete",
        "Component.Editor.Action.Reset" to "Reset",
        "Component.Editor.Action.Search" to "Search",
        "Component.Editor.CountItems" to "Total %d items",
        "Component.Editor.Dialog.AddTitle" to "Add Entry",
        "Component.Editor.Dialog.EditTitle" to "Edit Entry",
        "Component.Editor.Dialog.ResetMessage" to "Clear all entries and restore to unmodified state?",
        "Component.Editor.Dialog.ResetTitle" to "Confirm Reset",
        "Component.Editor.Empty.Hint" to "Click top-right button to add",
        "Component.Editor.Empty.Title" to "No Entries",
        "Component.Editor.Error.KeyEmpty" to "Key cannot be empty",
        "Component.Editor.Error.KeyExists" to "Key already exists",
        "Component.Editor.Rule.Content" to "Rule Content",
        "Component.Editor.Rule.ErrorContentRequired" to "Rule content cannot be empty",
        "Component.Editor.Rule.ErrorTargetRequired" to "Please select target",
        "Component.Editor.Rule.NoResolve" to "No Resolve",
        "Component.Editor.Rule.Src" to "Src IP",
        "Component.Editor.Rule.Target" to "Target",
        "Component.Editor.Rule.TargetDirect" to "DIRECT",
        "Component.Editor.Rule.TargetMatch" to "MATCH",
        "Component.Editor.Rule.TargetReject" to "REJECT",
        "Component.Editor.Rule.Type" to "Rule Type",
        "Component.Flag.ContentDescription" to "%s flag",
        "Component.Loading.Starting" to "Starting...",
        "Component.Message.Confirm" to "Confirm",
        "Component.Message.Error" to "Error",
        "Component.Message.Hint" to "Hint",
        "Component.Message.Success" to "Success",
        "Component.Navigation.Back" to "Back",
        "Component.Navigation.Refresh" to "Refresh",
        "Component.ProfileCard.ClickToUpdate" to "Click update to get subscription info",
        "Component.ProfileCard.DaysAgo" to "%d days ago",
        "Component.ProfileCard.Delete" to "Delete",
        "Component.ProfileCard.Edit" to "Edit",
        "Component.ProfileCard.ExpireAt" to "Expires: %s (%d days left)",
        "Component.ProfileCard.ExpireToday" to "Expires: Today",
        "Component.ProfileCard.Expired" to "Expired: %s",
        "Component.ProfileCard.Export" to "Export",
        "Component.ProfileCard.HoursAgo" to "%d hours ago",
        "Component.ProfileCard.JustNow" to "Just now",
        "Component.ProfileCard.LocalConfig" to "Local Configuration",
        "Component.ProfileCard.LocalFile" to "Local File",
        "Component.ProfileCard.MinutesAgo" to "%d min ago",
        "Component.ProfileCard.RemoteSubscription" to "Remote Subscription",
        "Component.ProfileCard.Traffic" to "Traffic: %s / %s (%d%%)",
        "Component.ProfileCard.Update" to "Update",
        "Component.ProfileCard.UsedTraffic" to "Used: %s",
        "Component.Selector.Append" to "Append",
        "Component.Selector.Disable" to "Disable",
        "Component.Selector.Enable" to "Enable",
        "Component.Selector.Merge" to "Merge",
        "Component.Selector.NotModify" to "Don't Modify",
        "Component.Selector.Prepend" to "Prepend",
        "Component.Selector.Replace" to "Replace",
        "Component.Update.Action.DownloadNow" to "Download Now",
        "Component.Update.Message.Available" to "A new update is available",
        "Component.Update.Message.CheckFailed" to "Failed to check updates: %s",
        "Component.Update.Message.Checking" to "Checking for updates...",
        "Component.Update.Message.Close" to "Got it",
        "Component.Update.Message.CoverDesc" to "Update cover",
        "Component.Update.Message.CurrentVersion" to "Current Version",
        "Component.Update.Message.DownloadAlreadyRunning" to "The update package is still downloading",
        "Component.Update.Message.DownloadErrorWithCode" to "Download failed (%d): %s",
        "Component.Update.Message.DownloadReady" to "Download complete, ready to install",
        "Component.Update.Message.Downloading" to "Downloading update package...",
        "Component.Update.Message.DownloadingWithProgress" to "Downloading update package %d%%",
        "Component.Update.Message.Error" to "Download failed, please try again later",
        "Component.Update.Message.Finished" to "Download complete, waiting for install confirmation",
        "Component.Update.Message.InstallFailed" to "Failed to open installer: %s",
        "Component.Update.Message.InstallPromptOpened" to "Download complete, installer opened",
        "Component.Update.Message.MissingReleaseMetadata" to "The latest release is missing update metadata",
        "Component.Update.Message.NoCompatibleAsset" to "No compatible APK was found in the latest release",
        "Component.Update.Message.NoUpdate" to "Already on the latest version",
        "Component.Update.Message.Preparing" to "Preparing download...",
        "Component.Update.Message.RemoteVersion" to "Target Version",
        "Component.Update.Message.Updating" to "Updating",
        "Component.Update.Message.VerifyFailed" to "Package verification failed, please retry",
        "Component.Update.Message.Verifying" to "Verifying update package...",
        "Component.Update.Message.Waiting" to "Waiting for download...",
        "Component.Update.Title.Available" to "New Version Available",
        "Component.WebView.InvalidUrl" to "Invalid URL",
        "Connection.ChainCount" to "x%d",
        "Connection.Detail.Action.Interrupt" to "Interrupt connection",
        "Connection.Detail.Action.Interrupting" to "Interrupting connection...",
        "Connection.Detail.Label.Content" to "Content",
        "Connection.Detail.Label.DestinationAddress" to "Destination Address",
        "Connection.Detail.Label.Download" to "Download",
        "Connection.Detail.Label.Duration" to "Duration",
        "Connection.Detail.Label.Process" to "Process",
        "Connection.Detail.Label.Protocol" to "Protocol",
        "Connection.Detail.Label.SourceAddress" to "Source Address",
        "Connection.Detail.Label.Type" to "Type",
        "Connection.Detail.Label.Upload" to "Upload",
        "Connection.Detail.Section.Info" to "Connection Info",
        "Connection.Detail.Section.Rule" to "Rule",
        "Connection.Empty" to "No active connections",
        "Connection.Loading" to "Loading...",
        "Connection.NoResults" to "No matching connections",
        "Connection.RelativeTime.Date" to "%02d-%02d",
        "Connection.RelativeTime.DaysAgo" to "%d days ago",
        "Connection.RelativeTime.HoursAgo" to "%d hours ago",
        "Connection.RelativeTime.JustNow" to "Just now",
        "Connection.RelativeTime.MinutesAgo" to "%d minutes ago",
        "Connection.Search" to "Search",
        "Connection.SearchHint" to "Search host, process...",
        "Connection.Sort.Download" to "Download",
        "Connection.Sort.Host" to "Host",
        "Connection.Sort.Time" to "Time",
        "Connection.Sort.Upload" to "Upload",
        "Connection.SortBy" to "Sort by:",
        "Connection.Summary" to "View active connections",
        "Connection.Tab.Active" to "Active",
        "Connection.Tab.Closed" to "Closed",
        "Connection.Title" to "Connections",
        "Editor.Action.Discard" to "Discard",
        "Editor.Action.Format" to "Format",
        "Editor.Action.Save" to "Save",
        "Editor.Common.ConfigPreviewTitle" to "Config Preview",
        "Editor.Common.EditConfigTitle" to "Edit Config",
        "Editor.Common.EditOverrideConfigTitle" to "Edit Override Config",
        "Editor.Common.EditProfileConfigTitle" to "Edit Profile Config",
        "Editor.Common.JsonSubtitle" to "Edit using JSON format",
        "Editor.Diagnostic.DuplicateKey" to "Duplicate key",
        "Editor.Diagnostic.Expected" to "Expected %s",
        "Editor.Diagnostic.JsonFormatError" to "JSON format error",
        "Editor.Diagnostic.JsonMustStartWithObjectOrArray" to "JSON must start with '{' or '['",
        "Editor.Diagnostic.JsonSyntaxError" to "JSON syntax error",
        "Editor.Diagnostic.NoValue" to "Missing value",
        "Editor.Diagnostic.Unknown" to "Unknown",
        "Editor.Diagnostic.Unterminated" to "Unterminated string or object",
        "Editor.Dialog.DiscardTitle" to "Discard Changes",
        "Editor.Dialog.UnsavedChangesMessage" to "There are unsaved changes. Discard them?",
        "Editor.Dialog.UnsavedChangesTitle" to "Unsaved Changes",
        "Editor.Toast.FormatFailedOrUnchanged" to "Format failed or no changes needed",
        "Editor.Toast.FormatSuccess" to "Formatted successfully",
        "Editor.Toast.SaveFailed" to "Save failed",
        "Editor.Toast.SyntaxError" to "Syntax error, please check the content",
        "Feature.Node.HealthCheckConcurrencySummary" to "Current option: %s",
        "Feature.Node.HealthCheckConcurrencyTitle" to "Health Check Concurrency",
        "Feature.Node.Section" to "Nodes",
        "Feature.RuntimeConfig.Empty" to "The runtime configuration file is empty",
        "Feature.RuntimeConfig.NotReady" to "The runtime configuration is not ready yet",
        "Feature.RuntimeConfig.NotRunning" to "Start the proxy before viewing its runtime configuration",
        "Feature.RuntimeConfig.PreviewTitle" to "Runtime Configuration · %s",
        "Feature.RuntimeConfig.RuntimeChanged" to "The running profile changed; open the configuration again",
        "Feature.RuntimeConfig.Section" to "Diagnostics",
        "Feature.RuntimeConfig.Summary" to "Inspect the final read-only YAML currently loaded by Mihomo; it may contain sensitive data",
        "Feature.RuntimeConfig.Title" to "View Runtime Configuration",
        "Feature.RuntimeConfig.Unavailable" to "The runtime configuration file is unavailable",
        "Feature.RuntimeConfig.UnknownProfile" to "Unknown profile",
        "Feature.SpeedTest.Cancel" to "Cancel",
        "Feature.SpeedTest.DataUsage" to "Uses up to 32 MB download and 8 MB upload per test",
        "Feature.SpeedTest.Download" to "Download",
        "Feature.SpeedTest.EdgeLocation" to "Cloudflare edge",
        "Feature.SpeedTest.Error" to "The speed test could not be completed. Check your connection and try again.",
        "Feature.SpeedTest.Jitter" to "Jitter",
        "Feature.SpeedTest.Latency" to "Latency",
        "Feature.SpeedTest.LocationUnknown" to "Unknown",
        "Feature.SpeedTest.Preparing" to "Preparing test…",
        "Feature.SpeedTest.PrivacyNotice" to "Test traffic is sent to Cloudflare. Packet-loss testing is not included.",
        "Feature.SpeedTest.Section" to "Network Test",
        "Feature.SpeedTest.Start" to "Start test",
        "Feature.SpeedTest.Summary" to "Measure the current connection to Cloudflare's edge",
        "Feature.SpeedTest.TestingDownload" to "Measuring download speed…",
        "Feature.SpeedTest.TestingLatency" to "Measuring latency…",
        "Feature.SpeedTest.TestingUpload" to "Measuring upload speed…",
        "Feature.SpeedTest.Title" to "Cloudflare Speed Test",
        "Feature.SpeedTest.Upload" to "Upload",
        "Feature.Title" to "Advanced Features",
        "Home.Control.HintAddProfile" to "Please add a profile first",
        "Home.Control.HintEnableProfile" to "Please enable a profile in Config page first",
        "Home.Control.HintProfilesLoading" to "Profiles are still loading. Please wait.",
        "Home.Control.Start" to "Start",
        "Home.Control.Stop" to "Stop",
        "Home.IpInfo.ExitIp" to "Exit IP",
        "Home.Message.ConfigSwitchFailed" to "Config switch failed: %s",
        "Home.Message.ConfigSwitched" to "Config switched",
        "Home.Message.ControlBusy" to "Proxy is %s. Please wait.",
        "Home.Message.Preparing" to "Preparing...",
        "Home.Message.StartFailed" to "Start failed: %s",
        "Home.Message.StopFailed" to "Stop failed: %s",
        "Home.Message.WaitingForVpnPermission" to "Waiting for VPN permission",
        "Home.NodeInfo.Delay" to "Delay",
        "Home.NodeInfo.DelayValue" to "%dms",
        "Home.NodeInfo.Node" to "Node",
        "Home.NodeInfo.Unknown" to "Unknown",
        "Home.ProxyMode.Http" to "HTTP",
        "Home.ProxyMode.Tun" to "TUN",
        "Home.ProxyMode.Vpn" to "VPN",
        "Home.Status.Connecting" to "Connecting",
        "Home.Status.Disconnecting" to "Disconnecting",
        "Home.Status.Running" to "Running",
        "Home.Status.TapFabToStart" to "Tap the floating button to start",
        "Home.Status.TapToStart" to "Tap to start",
        "Home.Title" to "KokoroBox",
        "Home.Traffic.DownShort" to "DOWN",
        "Home.Traffic.NoProfile" to "No Profile",
        "Home.Traffic.UpShort" to "UP",
        "Log.Action.Save" to "Save",
        "Log.Action.StartRecording" to "Start Recording",
        "Log.Action.StopRecording" to "Stop Recording",
        "Log.Detail.WaitingLog" to "Waiting for logs...",
        "Log.Detail.WillShowWhenGenerated" to "Logs will appear when generated",
        "Log.Empty.NoLogs" to "No log records",
        "Log.Empty.StartRecordingHint" to "Click the bottom-right button to start recording logs",
        "Log.Title" to "Logs",
        "MetaFeature.AgeKey.DerivePublicKey" to "Derive Public Key",
        "MetaFeature.AgeKey.Generate" to "Generate",
        "MetaFeature.AgeKey.HybridTitle" to "mlkem768-x25519",
        "MetaFeature.AgeKey.PublicKey" to "Public Key",
        "MetaFeature.AgeKey.SecretKey" to "Secret Key",
        "MetaFeature.AgeKey.Section" to "Age Keys",
        "MetaFeature.AgeKey.X25519Title" to "X25519",
        "MetaFeature.CustomRules.AddRule" to "Add rule",
        "MetaFeature.CustomRules.BackToKokoroSettings" to "Back to Kokoro Settings",
        "MetaFeature.CustomRules.Cancel" to "Cancel",
        "MetaFeature.CustomRules.Confirm" to "Confirm",
        "MetaFeature.CustomRules.ConflictMessage" to "Choose the remote version, or keep your local edits and save them again using the latest revision.",
        "MetaFeature.CustomRules.ConflictTitle" to "Rules changed on another device",
        "MetaFeature.CustomRules.DeleteRule" to "Delete rule",
        "MetaFeature.CustomRules.DiscardMessage" to "This action will discard unsaved changes.",
        "MetaFeature.CustomRules.DiscardTitle" to "Discard local changes?",
        "MetaFeature.CustomRules.EditRule" to "Edit rule",
        "MetaFeature.CustomRules.Empty" to "The default rule list is empty.",
        "MetaFeature.CustomRules.ErrorLoad" to "Unable to load custom rules.",
        "MetaFeature.CustomRules.ErrorNotFound" to "The default rule list is unavailable; server state was reloaded.",
        "MetaFeature.CustomRules.ErrorRateLimited" to "Too many requests. Try again later.",
        "MetaFeature.CustomRules.ErrorRequest" to "The server rejected the request.",
        "MetaFeature.CustomRules.ErrorUnknown" to "The save result could not be confirmed. Review the remote version before continuing.",
        "MetaFeature.CustomRules.ErrorValidation" to "Rule %d is invalid. Refresh options and correct it before saving.",
        "MetaFeature.CustomRules.ErrorValidationGeneral" to "The rule list is invalid. Refresh options and correct it before saving.",
        "MetaFeature.CustomRules.KeepLocal" to "Keep local",
        "MetaFeature.CustomRules.Loading" to "Loading custom rules…",
        "MetaFeature.CustomRules.MatchPayloadHint" to "MATCH does not use a payload.",
        "MetaFeature.CustomRules.MoveDown" to "Move rule down",
        "MetaFeature.CustomRules.MoveUp" to "Move rule up",
        "MetaFeature.CustomRules.Payload" to "Payload",
        "MetaFeature.CustomRules.Provider" to "Rule provider",
        "MetaFeature.CustomRules.Refresh" to "Reload from server",
        "MetaFeature.CustomRules.Retry" to "Retry",
        "MetaFeature.CustomRules.Rules" to "Rules",
        "MetaFeature.CustomRules.Save" to "Save rules",
        "MetaFeature.CustomRules.Saved" to "Custom rules saved",
        "MetaFeature.CustomRules.Target" to "Target",
        "MetaFeature.CustomRules.Title" to "Custom Rules",
        "MetaFeature.CustomRules.Type" to "Type",
        "MetaFeature.CustomRules.UseRemote" to "Use remote",
        "MetaFeature.Download.DialogTitle" to "Update GeoX Online",
        "MetaFeature.Download.DownloadComplete" to "Download complete: %d/%d",
        "MetaFeature.Download.ImportFailed" to "%s import failed: invalid file or validation failed",
        "MetaFeature.Download.ImportSuccess" to "%s imported and applied",
        "MetaFeature.Download.LastUpdate" to "Last update (%s): %s",
        "MetaFeature.Download.LastUpdateNever" to "No record",
        "MetaFeature.Download.LastUpdateSourceLocal" to "local",
        "MetaFeature.Download.LastUpdateSourceOnline" to "online",
        "MetaFeature.Download.LocalDialogTitle" to "Update GeoX Locally",
        "MetaFeature.Download.ProgressDetail" to "%s / %s · %s",
        "MetaFeature.Download.ProgressDetailUnknownTotal" to "%s · %s",
        "MetaFeature.Download.ProgressFailed" to "Download or validation failed; usable fallback file was kept",
        "MetaFeature.Download.ProgressSuccess" to "File updated and validated",
        "MetaFeature.Download.ProgressSummary" to "Keep this page open while files are downloaded and validated",
        "MetaFeature.Download.ProgressTitle" to "Updating GeoX",
        "MetaFeature.Download.ProgressWaiting" to "Waiting to start",
        "MetaFeature.Download.SelectFiles" to "Please select files to update",
        "MetaFeature.Download.StatusDownloading" to "Downloading %d%%",
        "MetaFeature.Download.StatusFailed" to "Failed",
        "MetaFeature.Download.StatusPending" to "Pending",
        "MetaFeature.Download.StatusSuccess" to "Done",
        "MetaFeature.Download.StatusValidating" to "Validating",
        "MetaFeature.GeoX.LocalUpdateSummary" to "Import and apply database files from local storage",
        "MetaFeature.GeoX.LocalUpdateTitle" to "Update GeoX Locally",
        "MetaFeature.GeoX.OnlineUpdateSummary" to "Download database files from remote sources",
        "MetaFeature.GeoX.OnlineUpdateTitle" to "Update GeoX Online",
        "MetaFeature.GeoX.RuntimeHomeInfo" to "GeoX data downloads into the core directory: %s",
        "MetaFeature.Section.ConnectionAndTraffic" to "Connections & Traffic",
        "MetaFeature.Section.GeoXUpdate" to "GeoX Update",
        "MetaFeature.Title" to "Meta Features",
        "NetworkSettings.Error.RootRequired" to "RootTun requires KokoroBox to have working root access",
        "NetworkSettings.Error.VpnDenied" to "VPN permission denied",
        "NetworkSettings.Experimental.AntiPollutionDnsSummary" to "Override profile DNS with encrypted regional and global resolvers; requires GeoSite data",
        "NetworkSettings.Experimental.AntiPollutionDnsTitle" to "DNS Pollution Protection",
        "NetworkSettings.Network.CustomUserAgentSummaryDefault" to "Not set, using default",
        "NetworkSettings.Network.CustomUserAgentTitle" to "Custom User-Agent",
        "NetworkSettings.Network.UserAgentDialogTitle" to "Edit User-Agent",
        "NetworkSettings.ProxyOptions.AccessControlModeTitle" to "Access Control Mode",
        "NetworkSettings.ProxyOptions.AllowAll" to "Allow All",
        "NetworkSettings.ProxyOptions.AllowSelected" to "Allow Selected",
        "NetworkSettings.ProxyOptions.ManageAccessControlSummary" to "Configure access control rules for apps and domains",
        "NetworkSettings.ProxyOptions.ManageAccessControlTitle" to "Manage Access Control",
        "NetworkSettings.ProxyOptions.RejectSelected" to "Reject Selected",
        "NetworkSettings.ProxyOptions.TunStackTitle" to "TUN Stack",
        "NetworkSettings.RootTun.AutoRedirectSummary" to "Automatically enable required redirect rules",
        "NetworkSettings.RootTun.AutoRedirectTitle" to "Auto Redirect",
        "NetworkSettings.RootTun.AutoRouteSummary" to "Automatically add required routes",
        "NetworkSettings.RootTun.AutoRouteTitle" to "Auto Route",
        "NetworkSettings.RootTun.DnsModeFakeIp" to "FakeIP",
        "NetworkSettings.RootTun.DnsModeRedirHost" to "RedirHost",
        "NetworkSettings.RootTun.DnsModeSummary" to "Choose between RedirHost and FakeIP",
        "NetworkSettings.RootTun.DnsModeTitle" to "DNS Mode",
        "NetworkSettings.RootTun.FakeIpRange6Summary" to "Only effective when DNS mode is FakeIP",
        "NetworkSettings.RootTun.FakeIpRange6Title" to "FakeIP IPv6 Range",
        "NetworkSettings.RootTun.FakeIpRangeSummary" to "Only effective when DNS mode is FakeIP",
        "NetworkSettings.RootTun.FakeIpRangeTitle" to "FakeIP IPv4 Range",
        "NetworkSettings.RootTun.IfNameSummary" to "Virtual interface name created by RootTun",
        "NetworkSettings.RootTun.IfNameTitle" to "Interface Name",
        "NetworkSettings.RootTun.MtuSummary" to "Maximum transmission unit used by RootTun",
        "NetworkSettings.RootTun.MtuTitle" to "MTU",
        "NetworkSettings.RootTun.StrictRouteSummary" to "Only matched traffic is routed into RootTun",
        "NetworkSettings.RootTun.StrictRouteTitle" to "Strict Route",
        "NetworkSettings.Section.Experimental" to "Experimental",
        "NetworkSettings.Section.Network" to "Network",
        "NetworkSettings.Section.ProxyOptions" to "Access Control",
        "NetworkSettings.Section.VpnOptions" to "Service Options",
        "NetworkSettings.Section.VpnService" to "Proxy Mode",
        "NetworkSettings.Title" to "Network Settings",
        "NetworkSettings.VpnOptions.AllowBypassSummary" to "Allow apps to bypass VPN",
        "NetworkSettings.VpnOptions.AllowBypassTitle" to "Allow App Bypass",
        "NetworkSettings.VpnOptions.BypassPrivateSummary" to "Bypass private networks and local addresses",
        "NetworkSettings.VpnOptions.BypassPrivateTitle" to "Bypass Private Networks",
        "NetworkSettings.VpnOptions.DnsHijackSummary" to "Redirect all DNS requests to KokoroBox",
        "NetworkSettings.VpnOptions.DnsHijackTitle" to "DNS Hijack",
        "NetworkSettings.VpnOptions.EnableIpv6Summary" to "Allow IPv6 traffic through VPN",
        "NetworkSettings.VpnOptions.EnableIpv6Title" to "Enable IPv6",
        "NetworkSettings.VpnOptions.SystemProxySummary" to "Only in VPN mode, set an HTTP proxy for apps outside the TUN path",
        "NetworkSettings.VpnOptions.SystemProxyTitle" to "VPN Built-in System Proxy",
        "NetworkSettings.VpnService.RootTunMode" to "Root TUN",
        "NetworkSettings.VpnService.RouteTrafficSummary" to "Select which proxy mode should take over system traffic",
        "NetworkSettings.VpnService.RouteTrafficTitle" to "Route System Traffic",
        "NetworkSettings.VpnService.SystemProxy" to "HTTP System Proxy",
        "NetworkSettings.VpnService.VpnMode" to "VPN Mode",
        "Onboarding.Finish.Subtitle" to "Setup complete, entering the main app",
        "Onboarding.Finish.Title" to "Ready to Go",
        "Onboarding.Navigation.Back" to "Back",
        "Onboarding.Navigation.Enter" to "Enter App",
        "Onboarding.Navigation.Next" to "Next",
        "Onboarding.Navigation.Start" to "Start Setup",
        "Onboarding.Permission.AppList.SummaryNeed" to "Required for per-app proxy features",
        "Onboarding.Permission.AppList.Title" to "App List Permission",
        "Onboarding.Permission.Common.Granted" to "Granted",
        "Onboarding.Permission.Notification.SummaryNeed" to "Show connection status and traffic notifications",
        "Onboarding.Permission.Notification.SummaryNotRequired" to "Not required on your Android version",
        "Onboarding.Permission.Notification.Title" to "Notification Permission",
        "Onboarding.Permission.Subtitle" to "Permissions affect notifications and per-app proxy features",
        "Onboarding.Permission.Title" to "Confirm Runtime Access",
        "Onboarding.Personalize.Subtitle" to "Theme mode and accent color can be changed anytime in settings",
        "Onboarding.Personalize.Title" to "Tune the Interface",
        "Onboarding.Privacy.Accept.Title" to "I have read and agree to the privacy policy",
        "Onboarding.Privacy.PolicyLink" to "Privacy Policy",
        "Onboarding.Privacy.Privacy.Title" to "Privacy Policy",
        "Onboarding.Privacy.RichTextLead" to "You need to accept the privacy policy before using KokoroBox.",
        "Onboarding.Privacy.RichTextPrefix" to "Before continuing, please review and accept the ",
        "Onboarding.Privacy.RichTextSuffix" to ".",
        "Onboarding.Privacy.Subtitle" to "Review and accept the privacy policy to continue",
        "Onboarding.Privacy.Title" to "Confirm Privacy Policy",
        "Onboarding.Sheet.LoadFailed" to "Failed to load policy content",
        "Onboarding.Sheet.PrivacyPolicyTitle" to "Privacy Policy",
        "OpenSourceLicenses.LicenseSheet.NoContent" to "No license content",
        "OpenSourceLicenses.Title" to "Open Source Licenses",
        "Override.Action.Create" to "Create Config",
        "Override.Action.Import" to "Import JSON",
        "Override.Action.ImportFile" to "Import Config File",
        "Override.Action.New" to "New Config",
        "Override.Card.Copy" to "Copy Config",
        "Override.Card.Delete" to "Delete Config",
        "Override.Card.DeleteButton" to "Delete",
        "Override.Card.Edit" to "Edit Config",
        "Override.Card.EditButton" to "Edit",
        "Override.Card.Export" to "Export Config",
        "Override.Card.NoDescription" to "No description",
        "Override.Dialog.Button.Cancel" to "Cancel",
        "Override.Dialog.Button.Delete" to "Delete",
        "Override.Dialog.Create.Description" to "Config Description",
        "Override.Dialog.Create.ImportHint" to "Select a JSON file to import override config",
        "Override.Dialog.Create.Name" to "Config Name",
        "Override.Dialog.Create.Title" to "Add Config",
        "Override.Dialog.Delete.InUseMessage" to "Config %s is being used by subscriptions. Deleting will unbind the relationship. Are you sure? This action cannot be undone.",
        "Override.Dialog.Delete.Message" to "Are you sure you want to delete config %s? This action cannot be undone.",
        "Override.Dialog.Delete.Title" to "Delete Config",
        "Override.Dialog.EditOptions.CodeEditor" to "Code Editor",
        "Override.Dialog.EditOptions.Title" to "Edit Config",
        "Override.Dialog.EditOptions.VisualEditor" to "Visual Editor",
        "Override.Dns.AppendSystem" to "Append System DNS",
        "Override.Dns.Default" to "Default DNS",
        "Override.Dns.DefaultHint" to "For resolving DNS server domains",
        "Override.Dns.EnhancedDisable" to "Disable",
        "Override.Dns.EnhancedFakeip" to "FakeIP",
        "Override.Dns.EnhancedMapping" to "Mapping",
        "Override.Dns.EnhancedMode" to "Enhanced Mode",
        "Override.Dns.EnhancedNotModify" to "Don't Modify",
        "Override.Dns.FakeipBlacklist" to "Blacklist",
        "Override.Dns.FakeipFilter" to "FakeIP Filter",
        "Override.Dns.FakeipFilterHint" to "e.g.: +.lan, localhost",
        "Override.Dns.FakeipFilterMode" to "FakeIP Filter Mode",
        "Override.Dns.FakeipWhitelist" to "Whitelist",
        "Override.Dns.Fallback" to "Fallback DNS",
        "Override.Dns.FallbackDomain" to "Domain Fallback",
        "Override.Dns.FallbackDomainHint" to "e.g.: +.google.com",
        "Override.Dns.FallbackGeoip" to "GeoIP Fallback",
        "Override.Dns.FallbackGeoipCode" to "GeoIP Code",
        "Override.Dns.FallbackGeoipCodeHint" to "e.g.: CN",
        "Override.Dns.FallbackHint" to "e.g.: 1.1.1.1",
        "Override.Dns.FallbackIpcidr" to "IP CIDR Fallback",
        "Override.Dns.FallbackIpcidrHint" to "e.g.: 240.0.0.0/4",
        "Override.Dns.Ipv6" to "DNS IPv6",
        "Override.Dns.Listen" to "Listen Address",
        "Override.Dns.ListenHint" to "e.g.: 0.0.0.0:53",
        "Override.Dns.NameserverPolicy" to "Nameserver Policy",
        "Override.Dns.NameserverPolicyKey" to "Domain match rule",
        "Override.Dns.NameserverPolicyValue" to "DNS server",
        "Override.Dns.Policy" to "DNS Policy",
        "Override.Dns.PolicyForceEnable" to "Force Enable",
        "Override.Dns.PolicyNotModify" to "Don't Modify",
        "Override.Dns.PolicyUseBuiltin" to "Use Built-in",
        "Override.Dns.PreferH3" to "Prefer HTTP/3",
        "Override.Dns.Servers" to "DNS Servers",
        "Override.Dns.ServersHint" to "e.g.: 8.8.8.8, tls://dns.google",
        "Override.Dns.UseHosts" to "Use Hosts",
        "Override.Draft.AddExtraField" to "Add Extra Field",
        "Override.Draft.AddHealthCheckField" to "Add health-check extra field",
        "Override.Draft.AddOverrideField" to "Add override extra field",
        "Override.Draft.Apply" to "Apply",
        "Override.Draft.BasicIdentity" to "Basic Identity",
        "Override.Draft.BasicInfo" to "Basic Info",
        "Override.Draft.BasicRouting" to "Basic Routing",
        "Override.Draft.BooleanOptions" to "Boolean Options",
        "Override.Draft.ClickToAddExtraField" to "Click to add extra field",
        "Override.Draft.ConfigDescription" to "Config Description",
        "Override.Draft.ConfigName" to "Config Name",
        "Override.Draft.ConfigSections" to "Config Sections",
        "Override.Draft.CoreSource" to "Core Source",
        "Override.Draft.DeleteExtraField" to "Delete extra field",
        "Override.Draft.DoubleValue" to "Double value",
        "Override.Draft.EditExtraField" to "Edit Extra Field",
        "Override.Draft.EditHealthCheckField" to "Edit health-check extra field",
        "Override.Draft.EditOverrideField" to "Edit override extra field",
        "Override.Draft.EditSubRules" to "Edit Sub Rules",
        "Override.Draft.ExtraFields" to "Extra Fields",
        "Override.Draft.ExtraFieldsConfigured" to "%d extra fields configured",
        "Override.Draft.FallbackRegionGroupTitle" to "Fallback Region Groups",
        "Override.Draft.GroupTypeFallback" to "Fallback",
        "Override.Draft.GroupTypeTitle" to "Group Types",
        "Override.Draft.GroupTypeUrlTest" to "UrlTest",
        "Override.Draft.HeaderHint" to "One header per line, format: Key: value1 | value2",
        "Override.Draft.HealthCheckFields" to "Health Check Extra Fields",
        "Override.Draft.HealthCheckSwitch" to "Health Check Switch",
        "Override.Draft.IntValue" to "Integer value",
        "Override.Draft.JsonFragment" to "Single JSON fragment",
        "Override.Draft.KeyNameEmpty" to "Key name cannot be empty",
        "Override.Draft.Name" to "Name",
        "Override.Draft.NameRequired" to "Name cannot be empty",
        "Override.Draft.NetworkAuth" to "Network & Auth",
        "Override.Draft.NoRules" to "No rules configured",
        "Override.Draft.Object" to "Object",
        "Override.Draft.OfficialMrs" to "Official MRS Common Routing",
        "Override.Draft.OfficialMrsSummary" to "Top template editor with regional auto-groups and individual switches; applies by rebuilding rule sections in current override.",
        "Override.Draft.OverrideFields" to "Override Extra Fields",
        "Override.Draft.OverrideSwitch" to "Override Switch",
        "Override.Draft.PresetApplySummary" to "Applying will replace the current override's rule providers, proxy groups, and rules",
        "Override.Draft.PresetTemplate" to "Preset Routing Template",
        "Override.Draft.RegionalAutoGroup" to "Regional Auto Group",
        "Override.Draft.RuleList" to "Rule List",
        "Override.Draft.RulesConfigured" to "%d rules configured",
        "Override.Draft.Save" to "Save",
        "Override.Draft.ServiceRouting" to "Service Routing",
        "Override.Draft.StringValue" to "String value",
        "Override.Draft.SubRuleGroup" to "Sub Rule Group",
        "Override.Draft.UrlTestRegionGroupTitle" to "UrlTest Region Groups",
        "Override.Draft.ValueType" to "Value Type",
        "Override.Draft.ValueTypeMismatch" to "Current value does not match selected type",
        "Override.Edit.Button.Cancel" to "Cancel",
        "Override.Edit.Button.Discard" to "Discard",
        "Override.Edit.EmptyName.Summary" to "Current name is empty, cannot save in real-time. Are you sure to discard these unsaved changes?",
        "Override.Edit.EmptyName.Title" to "Empty Name",
        "Override.Edit.PresetApplied" to "Preset routing template updated",
        "Override.Edit.TitleEdit" to "Edit Config",
        "Override.Edit.TitleNew" to "New Config",
        "Override.Editor.AddCustom" to "Add Custom",
        "Override.Editor.AddItem" to "Add Item",
        "Override.Editor.AddObject" to "Add Object",
        "Override.Editor.AddSubRuleGroup" to "Add Sub Rule Group",
        "Override.Editor.AdditionalParams" to "Additional Params",
        "Override.Editor.ArrayItems" to "Array %d items",
        "Override.Editor.BasicConnection" to "Basic Connection",
        "Override.Editor.CancelDelete" to "Cancel Delete",
        "Override.Editor.Clear" to "Clear",
        "Override.Editor.ClearCurrentMode" to "Clear Current Mode",
        "Override.Editor.ClearDialog.Summary" to "Clearing will remove all %s in current mode.",
        "Override.Editor.ClearDialog.Title" to "Clear %s",
        "Override.Editor.ClearMode" to "Clear Current Mode",
        "Override.Editor.ClearSubRules" to "Clear Sub Rules",
        "Override.Editor.Confirm" to "Confirm",
        "Override.Editor.ContentEmpty" to "Content cannot be empty",
        "Override.Editor.Copy" to "Copy",
        "Override.Editor.CustomMatchResult" to "Custom Match Result",
        "Override.Editor.CustomMember" to "Custom Member",
        "Override.Editor.CustomProxyGroupTarget" to "Custom Proxy Group Target",
        "Override.Editor.CustomSubRuleTarget" to "Custom Sub Rule Target",
        "Override.Editor.Delete" to "Delete",
        "Override.Editor.DeleteLastItem" to "Delete Last Item",
        "Override.Editor.DeleteSelected" to "Delete Selected",
        "Override.Editor.DeleteSelectedRules" to "Delete Selected Rules",
        "Override.Editor.DragToSort" to "Drag to Sort",
        "Override.Editor.Edit" to "Edit",
        "Override.Editor.EditItem" to "Edit Item",
        "Override.Editor.EditProxyGroup" to "Edit Proxy Group",
        "Override.Editor.EditProxyNode" to "Edit Proxy Node",
        "Override.Editor.EditRule" to "Edit Rule",
        "Override.Editor.EditSubRule" to "Edit Sub Rule",
        "Override.Editor.EditSubRuleGroup" to "Edit Sub Rule Group",
        "Override.Editor.EmptyString" to "Empty String",
        "Override.Editor.EnterDeleteMode" to "Enter Delete Mode",
        "Override.Editor.ExtraParamsHint" to "Extra params beyond src,no-resolve\\nFor logical rules, enter full payload directly, e.g. ((DOMAIN,google.com),(NETWORK,udp)).",
        "Override.Editor.HealthCheckAndFilter" to "Health Check & Filter",
        "Override.Editor.JsonBlockSubtitle" to "Edit this config block in JSON format",
        "Override.Editor.KeyName" to "Key Name",
        "Override.Editor.List" to "List",
        "Override.Editor.LogicalRuleHint" to "Logical rules can have full payload directly",
        "Override.Editor.MatchResult" to "Match Result",
        "Override.Editor.MemberSource" to "Member Source",
        "Override.Editor.Mode.Title" to "Modifier Mode",
        "Override.Editor.MoveDown" to "Move Down",
        "Override.Editor.MoveUp" to "Move Up",
        "Override.Editor.NetworkAndRoute" to "Network & Route",
        "Override.Editor.New" to "Add",
        "Override.Editor.NewProvider" to "Add Provider",
        "Override.Editor.NewProxyGroup" to "Add Proxy Group",
        "Override.Editor.NewProxyNode" to "Add Proxy Node",
        "Override.Editor.NewRule" to "Add New Rule",
        "Override.Editor.NewSubRuleGroup" to "Add New Sub Rule Group",
        "Override.Editor.NoRules" to "No rules",
        "Override.Editor.ObjectFallbackTitle" to "Object %d",
        "Override.Editor.ObjectFieldCount" to "%d fields",
        "Override.Editor.ObjectFieldHint" to "Field values support simple values and JSON structures.",
        "Override.Editor.ObjectFields" to "Object %d fields",
        "Override.Editor.ObjectJsonPlaceholder" to "{ \"name\": \"proxy\", \"type\": \"ss\" }",
        "Override.Editor.ObjectListHint" to "Structured object list editor. Field values support strings, numbers, booleans, and JSON fragments.",
        "Override.Editor.OneItemPerLine" to "One item per line",
        "Override.Editor.OtherExtraParams" to "Other extra params, comma-separated",
        "Override.Editor.Payload" to "Payload",
        "Override.Editor.PayloadEmpty" to "Payload cannot be empty",
        "Override.Editor.PortEmptyHint" to "Leave empty to not override port",
        "Override.Editor.ProviderMapHint" to "Structured Provider dictionary editor. Duplicate keys overwrite previous values.",
        "Override.Editor.ProxyGroup" to "Proxy Group",
        "Override.Editor.ProxyGroupTarget" to "Proxy Group Target",
        "Override.Editor.ProxyNode" to "Proxy Node",
        "Override.Editor.RuleBody" to "Rule Body",
        "Override.Editor.RuleEdit" to "Rule Edit",
        "Override.Editor.RulePlaceholder" to "DOMAIN-SUFFIX,example.com,DIRECT",
        "Override.Editor.RuleProviderInputHint" to "Custom content in input field; uses selected rule provider when empty",
        "Override.Editor.RuleType" to "Type",
        "Override.Editor.RuleTypeEmpty" to "Rule type cannot be empty",
        "Override.Editor.Rules" to "Rules",
        "Override.Editor.RulesConfiguredInline" to "%d rules configured",
        "Override.Editor.SaveProxyGroup" to "Save Proxy Group",
        "Override.Editor.SaveProxyNode" to "Save Proxy Node",
        "Override.Editor.SaveRule" to "Save Rule",
        "Override.Editor.SelectMatchResult" to "Select Match Result",
        "Override.Editor.SelectProxyGroupMember" to "Select Proxy Group Member",
        "Override.Editor.SelectProxyGroupTarget" to "Select Proxy Group Target",
        "Override.Editor.SelectRuleProvider" to "Select Rule Provider",
        "Override.Editor.SelectSubRuleTarget" to "Select Sub Rule Target",
        "Override.Editor.SubRuleGroupHint" to "Each sub-rule group contains a name and a list of rules.",
        "Override.Editor.SubRuleName" to "Sub Rule Name",
        "Override.Editor.SubRuleTarget" to "Sub Rule Target",
        "Override.Editor.TargetEmpty" to "Target cannot be empty",
        "Override.Editor.TypeEmpty" to "Type cannot be empty",
        "Override.Editor.Unnamed" to "Unnamed %s",
        "Override.Editor.UnnamedProvider" to "Unnamed Provider",
        "Override.Editor.UnnamedProxyGroup" to "Unnamed Proxy Group",
        "Override.Editor.UnnamedProxyNode" to "Unnamed Proxy Node",
        "Override.Editor.UnnamedRule" to "Unnamed Rule",
        "Override.Editor.UnnamedSubRuleGroup" to "Unnamed Sub Rule Group",
        "Override.Empty.Hint" to "Click the button below to create a new config, or import JSON",
        "Override.Empty.Title" to "No override configs",
        "Override.Export.Failed" to "Export failed: %s",
        "Override.Export.Success" to "Exported config: %s",
        "Override.Form.AdvancedJson" to "%s · Advanced JSON",
        "Override.Form.AllowPrivateNetwork" to "Allow Private Network",
        "Override.Form.AllowedIPs" to "Allowed IPs",
        "Override.Form.ApiSecret" to "API Secret",
        "Override.Form.AutoDetectInterface" to "Auto Detect Interface",
        "Override.Form.AutoRedirect" to "Auto Redirect",
        "Override.Form.AutoRoute" to "Auto Route",
        "Override.Form.AutoUpdateGeo" to "Auto Update GEO",
        "Override.Form.BasicPolicy" to "Basic Policy",
        "Override.Form.BindAddress" to "Bind Address",
        "Override.Form.CacheLimit" to "Cache Limit",
        "Override.Form.ConfigPersistence" to "Config Persistence",
        "Override.Form.ConnectionNetwork" to "Connection & Network",
        "Override.Form.ControllerCors" to "Controller CORS",
        "Override.Form.DirectFollowPolicy" to "Direct follows Policy",
        "Override.Form.DisableIcmpForward" to "Disable ICMP Forward",
        "Override.Form.DisallowedIPs" to "Disallowed IPs",
        "Override.Form.DnsBasicParams" to "DNS Basic Params",
        "Override.Form.DnsBasicSwitch" to "Basic Switch",
        "Override.Form.DnsFakeIpRange" to "FakeIP Range",
        "Override.Form.DnsHijack" to "DNS Hijack",
        "Override.Form.DnsPolicyMode" to "Policy Mode",
        "Override.Form.DnsUpstream" to "Upstream Servers",
        "Override.Form.DnsUpstreamServers" to "Upstream Servers",
        "Override.Form.EnableGso" to "Enable GSO",
        "Override.Form.EndpointIndependentNat" to "Endpoint Independent NAT",
        "Override.Form.ExcludePackage" to "Exclude Package",
        "Override.Form.ExternalControl" to "External Control",
        "Override.Form.ExternalController" to "External Controller",
        "Override.Form.ExternalControllerHttps" to "HTTPS Controller",
        "Override.Form.ExternalDoH" to "External DoH Service",
        "Override.Form.FakeIpIpv6Range" to "Fake-IP IPv6 Range",
        "Override.Form.FakeIpMode" to "Fake-IP Mode",
        "Override.Form.FakeIpParams" to "Fake-IP Params",
        "Override.Form.FallbackFilter" to "Fallback Filter",
        "Override.Form.FallbackParams" to "Fallback Params",
        "Override.Form.FallbackSwitch" to "Fallback Switch",
        "Override.Form.FilterList" to "Filter List",
        "Override.Form.GeoResources" to "GEO Resources",
        "Override.Form.GeoUpdateInterval" to "GEO Update Interval",
        "Override.Form.GeodataMode" to "Geodata Mode",
        "Override.Form.GeoipUrl" to "GeoIP URL",
        "Override.Form.GeositeMatcher" to "Geosite Matcher",
        "Override.Form.GeositeUrl" to "GeoSite URL",
        "Override.Form.GlobalClientFingerprint" to "Global Client Fingerprint",
        "Override.Form.Hours" to "hours",
        "Override.Form.HttpPorts" to "HTTP Ports",
        "Override.Form.IncludePackage" to "Include Package",
        "Override.Form.Ipv6Timeout" to "IPv6 Timeout",
        "Override.Form.ItemsConfigured" to "%d items configured",
        "Override.Form.LanAccess" to "LAN Access",
        "Override.Form.LanAddress" to "LAN Address",
        "Override.Form.MmdbUrl" to "MMDB URL",
        "Override.Form.NameserverPolicySection" to "Nameserver Policy",
        "Override.Form.NetworkPerfParams" to "Network Perf Params",
        "Override.Form.NetworkPerfSwitch" to "Network Perf Switch",
        "Override.Form.NotModify" to "Don't Modify",
        "Override.Form.OpenAdvancedEdit" to "Open Advanced Edit",
        "Override.Form.OpenAdvancedEditSummary" to "Edit raw object directly, for fields not covered by structured form",
        "Override.Form.OutboundInterface" to "Outbound Interface",
        "Override.Form.ProcessMode" to "Process Match Mode",
        "Override.Form.ProxyGroups" to "Proxy Groups",
        "Override.Form.ProxyGroupsHint" to "Structured proxy groups",
        "Override.Form.ProxyNodes" to "Proxy Nodes",
        "Override.Form.ProxyNodesHint" to "Structured proxy entries",
        "Override.Form.ProxyPorts" to "Proxy Ports",
        "Override.Form.ProxyProviders" to "Proxy Providers",
        "Override.Form.ProxyProvidersAdvanced" to "Enter Advanced JSON when protocol details, validation or extra fields are needed",
        "Override.Form.ProxyProvidersHint" to "Structured Providers",
        "Override.Form.ProxyServerNameserverPolicy" to "Proxy Server Nameserver Policy",
        "Override.Form.QuicPorts" to "QUIC Ports",
        "Override.Form.RouteAddress" to "Route Address",
        "Override.Form.RouteExcludeAddress" to "Route Exclude Address",
        "Override.Form.RoutingMark" to "Routing Mark",
        "Override.Form.RuleChain" to "Rule Chain",
        "Override.Form.RuleChainNotSet" to "Rule chain not set",
        "Override.Form.RuleProviders" to "Rule Providers",
        "Override.Form.RuleProvidersAdvanced" to "Enter Advanced JSON when complex Provider fields are needed",
        "Override.Form.RuleProvidersHint" to "Structured Providers",
        "Override.Form.RunAndLog" to "Run & Log",
        "Override.Form.RunAndLogExtra" to "Run & Log Extra",
        "Override.Form.SaveFakeIpMapping" to "Save Fake-IP Mapping",
        "Override.Form.SaveGroupSelection" to "Save Group Selection",
        "Override.Form.Seconds" to "seconds",
        "Override.Form.SkipAndForce" to "Skip & Force",
        "Override.Form.SkipAuthIPs" to "Skip Auth IPs",
        "Override.Form.SkipDstAddress" to "Skip Dst Address",
        "Override.Form.SkipSrcAddress" to "Skip Src Address",
        "Override.Form.SnifferForceDomain" to "Force Domain",
        "Override.Form.SnifferOverride" to "Override Destination",
        "Override.Form.SnifferParsePureIp" to "Parse Pure IP",
        "Override.Form.SnifferPorts" to "Ports",
        "Override.Form.SnifferSkipDomain" to "Skip Domain",
        "Override.Form.SnifferSwitch" to "Switch",
        "Override.Form.Stack" to "Stack",
        "Override.Form.StrictRoute" to "Strict Route",
        "Override.Form.StructuredEdit" to "%s · Structured Edit",
        "Override.Form.SubRules" to "Sub Rules",
        "Override.Form.SubRulesAdvanced" to "Complex sub rule structures are collected in Advanced JSON",
        "Override.Form.SubRulesHint" to "Structured rule groups",
        "Override.Form.TcpConcurrent" to "TCP Concurrent",
        "Override.Form.TlsPorts" to "TLS Ports",
        "Override.Form.TunBasicSwitch" to "Basic Switch",
        "Override.Form.TunRouteAndApps" to "Route & Apps",
        "Override.Form.UnifiedDelay" to "Unified Delay",
        "Override.Form.UserAuth" to "User Auth",
        "Override.General.AllowLan" to "Allow LAN",
        "Override.General.HttpPort" to "HTTP Port",
        "Override.General.Ipv6" to "IPv6",
        "Override.General.LogLevel" to "Log Level",
        "Override.General.MixedPort" to "Mixed Port",
        "Override.General.ProxyMode" to "Proxy Mode",
        "Override.General.RedirectPort" to "Redirect Port",
        "Override.General.SocksPort" to "SOCKS Port",
        "Override.General.TproxyPort" to "TProxy Port",
        "Override.Import.Failed" to "Import failed: %s",
        "Override.Import.FileError" to "Failed to read file: %s",
        "Override.Import.ReadError" to "Cannot read import file",
        "Override.Import.Success" to "Imported %d configs from %s",
        "Override.Import.SuccessDefault" to "Imported %d configs",
        "Override.Label.CacheAlgorithm" to "Cache Algorithm",
        "Override.Label.Enable" to "Enable",
        "Override.Label.FakeIpRange" to "FakeIP Range",
        "Override.Label.ForceDnsMapping" to "Force DNS Mapping",
        "Override.Label.ForceDomain" to "Force Domain",
        "Override.Label.HttpOverride" to "HTTP Override",
        "Override.Label.KeepAliveIdle" to "Keep Alive Idle",
        "Override.Label.KeepAliveInterval" to "Keep Alive Interval",
        "Override.Label.OverrideDestination" to "Override Destination",
        "Override.Label.ParsePureIp" to "Parse Pure IP",
        "Override.Label.QuicOverride" to "QUIC Override",
        "Override.Label.RespectRules" to "Respect Rules",
        "Override.Label.RulesReplace" to "Override Rules",
        "Override.Label.SkipDomain" to "Skip Domain",
        "Override.Label.TlsOverride" to "TLS Override",
        "Override.Label.UseSystemHosts" to "Use System Hosts",
        "Override.Modifier.End" to "Append",
        "Override.Modifier.Force" to "Force Replace",
        "Override.Modifier.ItemsCount" to "%d items",
        "Override.Modifier.Merge" to "Merge",
        "Override.Modifier.NoChanges" to "No changes",
        "Override.Modifier.NotModified" to "Not Modified",
        "Override.Modifier.Replace" to "Replace",
        "Override.Modifier.Start" to "Prepend",
        "Override.ProxyGroup.Field.DisableUdp" to "Disable UDP",
        "Override.ProxyGroup.Field.ExcludeFilter" to "Exclude Filter",
        "Override.ProxyGroup.Field.ExcludeType" to "Exclude Type",
        "Override.ProxyGroup.Field.ExpectedStatus" to "Expected Status",
        "Override.ProxyGroup.Field.Filter" to "Filter",
        "Override.ProxyGroup.Field.Hidden" to "Hidden",
        "Override.ProxyGroup.Field.Icon" to "Icon",
        "Override.ProxyGroup.Field.IncludeAll" to "Include All",
        "Override.ProxyGroup.Field.IncludeAllProviders" to "Include All Providers",
        "Override.ProxyGroup.Field.IncludeAllProxies" to "Include All Proxies",
        "Override.ProxyGroup.Field.InterfaceName" to "Interface Name",
        "Override.ProxyGroup.Field.Interval" to "Interval",
        "Override.ProxyGroup.Field.Lazy" to "Lazy",
        "Override.ProxyGroup.Field.MaxFailedTimes" to "Max Failed Times",
        "Override.ProxyGroup.Field.Proxies" to "Members",
        "Override.ProxyGroup.Field.RoutingMark" to "Routing Mark",
        "Override.ProxyGroup.Field.Timeout" to "Timeout",
        "Override.ProxyGroup.Field.Url" to "URL",
        "Override.ProxyGroup.Field.Use" to "Use Providers",
        "Override.ProxyGroup.Field.UseHint" to "One Provider name per line",
        "Override.Rule.EmptyWarning" to "Rule #%d is empty",
        "Override.Rule.InvalidFormatWarning" to "Rule #%d format may be incorrect: %s",
        "Override.Rule.MissingTargetWarning" to "Rule #%d missing policy group target: %s",
        "Override.Save.ApplyFailed" to "Override saved, but failed to reapply to current config",
        "Override.Save.Failed" to "Failed to save override config",
        "Override.Save.ImportDefaultName" to "Imported Override Config",
        "Override.Save.ImportEmpty" to "Import content cannot be empty",
        "Override.Save.PresetNotModifiable" to "System preset cannot be modified",
        "Override.Section.Dns.Summary" to "Basic switches, Fake-IP, upstream & policy",
        "Override.Section.Dns.Title" to "DNS",
        "Override.Section.General.Summary" to "Run mode, controller, persistence & GEO",
        "Override.Section.General.Title" to "General",
        "Override.Section.Inbound.Summary" to "Ports, authentication, LAN access",
        "Override.Section.Inbound.Title" to "Inbound",
        "Override.Section.Proxies.Summary" to "Proxy nodes & protocol objects",
        "Override.Section.Proxies.Title" to "Outbound Proxies",
        "Override.Section.ProxyGroups.Summary" to "Proxy Groups prepend, override, append",
        "Override.Section.ProxyGroups.Title" to "Proxy Groups",
        "Override.Section.ProxyProviders.Summary" to "Proxy Providers merge & override",
        "Override.Section.ProxyProviders.Title" to "Proxy Providers",
        "Override.Section.RuleProviders.Summary" to "Rule Providers merge & override",
        "Override.Section.RuleProviders.Title" to "Rule Providers",
        "Override.Section.Rules.Summary" to "Rule chain & matching order",
        "Override.Section.Rules.Title" to "Routing Rules",
        "Override.Section.Sniffer.Summary" to "Policy switches, protocol ports, skip rules",
        "Override.Section.Sniffer.Title" to "Domain Sniffer",
        "Override.Section.SubRules.Summary" to "Sub Rules grouping & merging",
        "Override.Section.SubRules.Title" to "Sub Rules",
        "Override.Section.Tun.Summary" to "Tun inbound, routing & app scope",
        "Override.Section.Tun.Title" to "Tun",
        "Override.Status.InUse" to "In Use",
        "Override.Status.NotInUse" to "Not in Use",
        "Override.Structured.Proxies.EmptyHint" to "No proxy nodes",
        "Override.Structured.Proxies.ItemLabel" to "Proxy Node",
        "Override.Structured.Proxies.Title" to "Proxy Nodes",
        "Override.Structured.ProxyGroups.EmptyHint" to "No proxy groups",
        "Override.Structured.ProxyGroups.ItemLabel" to "Proxy Group",
        "Override.Structured.ProxyGroups.Title" to "Proxy Groups",
        "Override.Structured.ProxyProviders.ItemLabel" to "Provider",
        "Override.Structured.ProxyProviders.Title" to "Proxy Providers",
        "Override.Structured.RuleProviders.ItemLabel" to "Provider",
        "Override.Structured.RuleProviders.Title" to "Rule Providers",
        "Override.Structured.SubRules.ItemLabel" to "Sub Rule Group",
        "Override.Structured.SubRules.Title" to "Sub Rules",
        "Override.Title" to "Override Configs",
        "ProfilesPage.Action.AddProfile" to "Add Profile",
        "ProfilesPage.Action.UpdateAll" to "Update All",
        "ProfilesPage.Button.Cancel" to "Cancel",
        "ProfilesPage.Button.Confirm" to "Confirm",
        "ProfilesPage.DeleteDialog.Confirm" to "Delete",
        "ProfilesPage.DeleteDialog.Message" to "Are you sure you want to delete '%s'?",
        "ProfilesPage.DeleteDialog.Title" to "Delete Profile",
        "ProfilesPage.EditDialog.Title" to "Edit Profile Name",
        "ProfilesPage.Empty.Hint" to "Click top-right to add profile",
        "ProfilesPage.Empty.NoProfiles" to "No profiles",
        "ProfilesPage.Input.NewProfile" to "New Profile",
        "ProfilesPage.Input.ProfileName" to "Profile Name",
        "ProfilesPage.Input.SelectFile" to "Click to select file",
        "ProfilesPage.Input.SubscriptionUrl" to "Subscription URL (HTTP/HTTPS)",
        "ProfilesPage.Input.SubscriptionUserAgent" to "User-Agent (blank uses global setting)",
        "ProfilesPage.Kokoro.Account" to "Account",
        "ProfilesPage.Kokoro.AvatarDescription" to "%s's osu! avatar",
        "ProfilesPage.Kokoro.BandwidthLimit" to "Bandwidth limit",
        "ProfilesPage.Kokoro.CheckFailed" to "Unable to check account",
        "ProfilesPage.Kokoro.CheckFailedDetail" to "Check your connection, then try again.",
        "ProfilesPage.Kokoro.Checking" to "Checking sign-in status...",
        "ProfilesPage.Kokoro.DecreaseUpdateHours" to "Decrease update interval",
        "ProfilesPage.Kokoro.DefaultProfileName" to "Kokoro",
        "ProfilesPage.Kokoro.Direct" to "Direct",
        "ProfilesPage.Kokoro.Disabled" to "Disabled",
        "ProfilesPage.Kokoro.Enabled" to "Enabled",
        "ProfilesPage.Kokoro.Expires" to "Expires",
        "ProfilesPage.Kokoro.Fallback" to "Unmatched traffic",
        "ProfilesPage.Kokoro.FinalRoute" to "Final route",
        "ProfilesPage.Kokoro.IncreaseUpdateHours" to "Increase update interval",
        "ProfilesPage.Kokoro.InvalidUpdateHours" to "Update interval must be a positive number of hours",
        "ProfilesPage.Kokoro.Isp" to "ISP",
        "ProfilesPage.Kokoro.IspAuto" to "Automatic",
        "ProfilesPage.Kokoro.IspCm" to "China Mobile",
        "ProfilesPage.Kokoro.IspCt" to "China Telecom",
        "ProfilesPage.Kokoro.IspCu" to "China Unicom",
        "ProfilesPage.Kokoro.IspOther" to "Other",
        "ProfilesPage.Kokoro.KeepFallback" to "Keep configuration fallback",
        "ProfilesPage.Kokoro.LoggedIn" to "Signed in",
        "ProfilesPage.Kokoro.LoggedInAs" to "Signed in as %s",
        "ProfilesPage.Kokoro.LoggedOut" to "Not signed in",
        "ProfilesPage.Kokoro.Login" to "Sign in with osu!",
        "ProfilesPage.Kokoro.LoginFailed" to "Sign-in failed or was cancelled",
        "ProfilesPage.Kokoro.LoginHint" to "Sign in with osu! to load your proxy subscription.",
        "ProfilesPage.Kokoro.LoginRequired" to "Sign in and select an active subscription first",
        "ProfilesPage.Kokoro.Logout" to "Sign out",
        "ProfilesPage.Kokoro.Mirror" to "Mirror",
        "ProfilesPage.Kokoro.Mode" to "Connection mode",
        "ProfilesPage.Kokoro.NoSubscription" to "This account has no active proxy subscription.",
        "ProfilesPage.Kokoro.Origin" to "Original",
        "ProfilesPage.Kokoro.Plan" to "Plan",
        "ProfilesPage.Kokoro.ProfileUpdate" to "Profile Update Interval",
        "ProfilesPage.Kokoro.Protocol" to "Protocol",
        "ProfilesPage.Kokoro.Proxy" to "Proxy",
        "ProfilesPage.Kokoro.Relay" to "Relay",
        "ProfilesPage.Kokoro.Retry" to "Retry",
        "ProfilesPage.Kokoro.Routing" to "Routing",
        "ProfilesPage.Kokoro.RuleProviderAutoUpdate" to "Update Rule Providers",
        "ProfilesPage.Kokoro.RuleProviderAutoUpdateSummary" to "Automatically refresh remote rule sets",
        "ProfilesPage.Kokoro.RuleSource" to "Rule source",
        "ProfilesPage.Kokoro.RuleUpdate" to "Remote rule updates",
        "ProfilesPage.Kokoro.SecureTokenSession" to "Tokens are protected by Android Keystore.",
        "ProfilesPage.Kokoro.SignInFromSettings" to "Sign in from Settings → Kokoro Settings before adding a Kokoro Subscription.",
        "ProfilesPage.Kokoro.Subscription" to "Subscription",
        "ProfilesPage.Kokoro.SubscriptionAutoUpdate" to "Automatically Update Profile",
        "ProfilesPage.Kokoro.SubscriptionAutoUpdateSummary" to "Refresh this configuration on schedule",
        "ProfilesPage.Kokoro.SubscriptionNumber" to "Subscription %s",
        "ProfilesPage.Kokoro.Traffic" to "Traffic",
        "ProfilesPage.Kokoro.TrafficUsed" to "Traffic used",
        "ProfilesPage.Kokoro.Unlimited" to "Unlimited",
        "ProfilesPage.Kokoro.UpdateCustom" to "Custom",
        "ProfilesPage.Kokoro.UpdateHours" to "Hours (positive integer)",
        "ProfilesPage.Kokoro.UpdateHoursRange" to "Update interval (%s–%s hours)",
        "ProfilesPage.Kokoro.UpdateHoursValue" to "%s hr",
        "ProfilesPage.Kokoro.UpdateOff" to "Disabled",
        "ProfilesPage.Kokoro.UpdateOn" to "Every hour",
        "ProfilesPage.Kokoro.Updates" to "Updates",
        "ProfilesPage.Kokoro.VmessRelayOnly" to "VMess always uses relay mode",
        "ProfilesPage.LinkSettings.AddLink" to "Add Link",
        "ProfilesPage.LinkSettings.Close" to "Close",
        "ProfilesPage.LinkSettings.DefaultLink" to "Default Link",
        "ProfilesPage.LinkSettings.DefaultLinkSummary" to "Link opened when clicking top-left shortcut button",
        "ProfilesPage.LinkSettings.EditLink" to "Edit Link",
        "ProfilesPage.LinkSettings.Name" to "Name",
        "ProfilesPage.LinkSettings.OpenMode" to "Open Mode",
        "ProfilesPage.LinkSettings.OpenModeExternal" to "External Browser",
        "ProfilesPage.LinkSettings.OpenModeInApp" to "In App",
        "ProfilesPage.LinkSettings.Title" to "Link Settings",
        "ProfilesPage.LinkSettings.Url" to "URL",
        "ProfilesPage.LinkSettings.Validation.EnterName" to "Please enter name",
        "ProfilesPage.LinkSettings.Validation.EnterUrl" to "Please enter URL",
        "ProfilesPage.LinkSettings.Validation.InvalidUrl" to "Please enter a valid URL",
        "ProfilesPage.Message.UnknownFile" to "Unknown file",
        "ProfilesPage.Misc.Complete" to "Complete",
        "ProfilesPage.Misc.Error" to "Error",
        "ProfilesPage.Progress.Downloading" to "Downloading...",
        "ProfilesPage.QrScanner.NeedCamera" to "Camera permission required for scanning",
        "ProfilesPage.QrScanner.NeedPermission" to "Camera permission required",
        "ProfilesPage.QrScanner.RecognizeError" to "Recognition failed: %s",
        "ProfilesPage.QrScanner.RecognizeFailed" to "Failed to recognize QR code",
        "ProfilesPage.QrScanner.RecognizeSuccess" to "Recognition successful",
        "ProfilesPage.QrScanner.ScanSuccess" to "Scan successful",
        "ProfilesPage.QrScanner.SelectFromAlbum" to "Select QR code from album",
        "ProfilesPage.SettingsDialog.ChangeLink" to "Change Subscription Link",
        "ProfilesPage.SettingsDialog.ConfigMissing" to "Config not found: %s",
        "ProfilesPage.SettingsDialog.EditProfile" to "Edit Profile",
        "ProfilesPage.SettingsDialog.EditSettings" to "Edit Settings",
        "ProfilesPage.SettingsDialog.NoDescription" to "No description set",
        "ProfilesPage.SettingsDialog.OpenConfig" to "Open Config",
        "ProfilesPage.SettingsDialog.SaveFailed" to "Failed to save profile",
        "ProfilesPage.SettingsDialog.SystemPreset" to "Enable Override Preset",
        "ProfilesPage.SettingsDialog.SystemPresetSummary" to "Enable the built-in override preset",
        "ProfilesPage.SettingsDialog.Title" to "Subscription Settings",
        "ProfilesPage.ShareDialog.ImportedConfigMissing" to "Imported config missing: %s",
        "ProfilesPage.ShareDialog.NoLink" to "This profile has no subscription link",
        "ProfilesPage.ShareDialog.ShareFile" to "Share Config File",
        "ProfilesPage.ShareDialog.ShareLink" to "Share Subscription Link",
        "ProfilesPage.ShareDialog.Title" to "Share Profile",
        "ProfilesPage.Sheet.AddTitle" to "Add Profile",
        "ProfilesPage.Sheet.EditTitle" to "Edit Profile",
        "ProfilesPage.Title" to "Profiles",
        "ProfilesPage.Type.Kokoro" to "Kokoro Subscription",
        "ProfilesPage.Type.LocalFile" to "Local File",
        "ProfilesPage.Type.QrScan" to "Scan QR Code",
        "ProfilesPage.Type.Subscription" to "Subscription URL",
        "ProfilesPage.Type.Title" to "Profile Type",
        "ProfilesPage.Validation.EnterUrl" to "Please enter URL",
        "ProfilesPage.Validation.SelectFile" to "Please select a file",
        "ProfilesPage.Validation.YamlOnly" to "Only .yaml or .yml format supported",
        "ProfilesVM.Error.ProfileNotExist" to "Profile does not exist",
        "ProfilesVM.Message.AddFailed" to "Add profile failed: %s",
        "ProfilesVM.Message.DeleteFailed" to "Delete profile failed: %s",
        "ProfilesVM.Message.ImportFailed" to "Import profile failed: %s",
        "ProfilesVM.Message.ProfileAdded" to "Profile added: %s",
        "ProfilesVM.Message.ProfileAddedAndActivated" to "Profile added and activated: %s",
        "ProfilesVM.Message.ProfileDeleted" to "Profile deleted",
        "ProfilesVM.Message.ProfileImported" to "Profile imported: %s",
        "ProfilesVM.Message.ProfileUpdated" to "Profile updated: %s",
        "ProfilesVM.Message.ToggleFailed" to "Toggle state failed: %s",
        "ProfilesVM.Message.UpdateFailed" to "Update profile failed: %s",
        "ProfilesVM.Progress.ImportComplete" to "Import complete",
        "ProfilesVM.Progress.ImportPreparing" to "Preparing to import file...",
        "ProfilesVM.Progress.Preparing" to "Preparing download...",
        "ProfilesVM.Progress.Verifying" to "Verifying configuration...",
        "Providers.Action.Operation" to "Operation",
        "Providers.Action.Update" to "Update",
        "Providers.Action.UpdateAll" to "Update All",
        "Providers.Action.Upload" to "Upload",
        "Providers.Empty.NoProviders" to "No external resources",
        "Providers.Empty.NoProvidersHint" to "Current profile doesn't contain external resources",
        "Providers.Empty.NotRunning" to "Proxy not running",
        "Providers.Empty.NotRunningHint" to "Please start proxy service to view external resources",
        "Providers.InfoSummary" to "Geo assets and shared runtime files live in: %s. Rule and proxy providers are always written into the active profile's private directory.",
        "Providers.InfoTitle" to "Core Directory",
        "Providers.Message.AllUpdated" to "All updated",
        "Providers.Message.FetchFailed" to "Failed to fetch resources: %s",
        "Providers.Message.UpdateFailed" to "Update failed: %s",
        "Providers.Message.UpdateSuccess" to "%s updated successfully",
        "Providers.Message.UploadFailed" to "Upload failed: %s",
        "Providers.Message.UploadSuccess" to "%s uploaded successfully",
        "Providers.ProviderPath" to "Resource path: %s",
        "Providers.Title" to "External Resources",
        "Providers.Type.ProxyProviders" to "Proxy Providers (%d)",
        "Providers.Type.RuleProviders" to "Rule Providers (%d)",
        "Providers.VehicleType.Compatible" to "Compatible",
        "Providers.VehicleType.File" to "File",
        "Providers.VehicleType.Http" to "HTTP",
        "Providers.VehicleType.Inline" to "Inline",
        "Proxy.Action.Sort" to "Sort",
        "Proxy.Action.Test" to "Test",
        "Proxy.DisplayMode.DoubleDetailed" to "Double Detailed",
        "Proxy.DisplayMode.DoubleSimple" to "Double Simple",
        "Proxy.DisplayMode.SingleDetailed" to "Single Detailed",
        "Proxy.DisplayMode.SingleSimple" to "Single Simple",
        "Proxy.Empty.Hint" to "Please load a profile in Config page",
        "Proxy.Empty.NoNodes" to "No nodes",
        "Proxy.Mode.Direct" to "Direct",
        "Proxy.Mode.Global" to "Global",
        "Proxy.Mode.Rule" to "Rule",
        "Proxy.Mode.SwitchFailed" to "Mode switch failed: %s",
        "Proxy.Mode.Switched" to "Switched to: %s mode",
        "Proxy.Mode.Unknown" to "Unknown",
        "Proxy.Node.Count" to "%d nodes",
        "Proxy.Node.Timeout" to "Timeout",
        "Proxy.Selection.Error" to "Switch failed: %s",
        "Proxy.Selection.Failed" to "Switch failed",
        "Proxy.Selection.Switched" to "Switched to: %s",
        "Proxy.SortMode.ByLatency" to "By Latency",
        "Proxy.SortMode.ByName" to "By Name",
        "Proxy.SortMode.Default" to "Default",
        "Proxy.Testing.All" to "Testing all node groups...",
        "Proxy.Testing.Failed" to "Test failed: %s",
        "Proxy.Testing.Group" to "Testing node group: %s",
        "Proxy.Testing.InProgress" to "Testing the node",
        "Proxy.Testing.RequestSent" to "Test request sent",
        "Proxy.Title" to "Proxy",
        "Proxy.Type.Compatible" to "Compatible",
        "Proxy.Type.Direct" to "Direct",
        "Proxy.Type.Fallback" to "Fallback",
        "Proxy.Type.LoadBalance" to "Load Balance",
        "Proxy.Type.Pass" to "Pass",
        "Proxy.Type.Reject" to "Reject",
        "Proxy.Type.RejectDrop" to "Drop",
        "Proxy.Type.Relay" to "Relay",
        "Proxy.Type.Selector" to "Selector",
        "Proxy.Type.Smart" to "Smart",
        "Proxy.Type.Unknown" to "Unknown",
        "Proxy.Type.UrlTest" to "UrlTest",
        "Service.AutoRestart.ChannelDescription" to "Used to restart proxy service automatically",
        "Service.AutoRestart.ChannelName" to "Auto Restart Service",
        "Service.AutoRestart.Checking" to "Checking auto-start...",
        "Service.Notification.Running" to "Running",
        "Service.Notification.SpeedFormat" to "Down %s  Up %s",
        "Service.Notification.TodayTrafficFormat" to "Today's traffic %s",
        "Service.Notification.TrafficFormat" to "Total: %s",
        "Service.Notification.UnknownProfile" to "Unknown profile",
        "Service.Tile.ClickToOpen" to "Click to open app",
        "Service.Tile.ClickToStartProxy" to "Start proxy",
        "Service.Tile.ClickToStopProxy" to "Stop proxy",
        "Service.Tile.Connecting" to "Connecting...",
        "Service.Tile.Disconnecting" to "Disconnecting...",
        "Settings.DataSettings.AppDataManagement" to "App Data Management",
        "Settings.DataSettings.AppDataManagementSummary" to "Clean up some cache files",
        "Settings.DataSettings.ExportBackup" to "Export Backup",
        "Settings.DataSettings.ExportBackupSummary" to "Save user settings to a JSON file",
        "Settings.DataSettings.ImportBackup" to "Import Backup",
        "Settings.DataSettings.ImportBackupSummary" to "Restore user settings from a backup file",
        "Settings.Error.WebviewFailed" to "Unable to open WebView: %s",
        "Settings.Kokoro.CustomRules" to "Custom Rules",
        "Settings.Kokoro.CustomRulesSummary" to "Edit the default rules applied to generated profiles",
        "Settings.Kokoro.Summary" to "Account · Custom Rules",
        "Settings.Kokoro.Title" to "Kokoro Settings",
        "Settings.More.About" to "About",
        "Settings.More.AboutSummary" to "Version & License",
        "Settings.More.Logs" to "Logs",
        "Settings.More.LogsSummary" to "Runtime Logs",
        "Settings.NetworkSettings.Lab" to "Lab",
        "Settings.NetworkSettings.LabSummary" to "Node testing",
        "Settings.NetworkSettings.MetaFeatures" to "Meta Features",
        "Settings.NetworkSettings.MetaFeaturesSummary" to "Meta Extensions",
        "Settings.NetworkSettings.Network" to "Network",
        "Settings.NetworkSettings.NetworkSummary" to "DNS · Port · Inbound",
        "Settings.NetworkSettings.Override" to "Override",
        "Settings.NetworkSettings.OverrideSummary" to "Rule Override",
        "Settings.Section.DataSettings" to "Data Settings",
        "Settings.Section.Kokoro" to "Kokoro",
        "Settings.Section.More" to "More",
        "Settings.Section.NetworkSettings" to "Network Settings",
        "Settings.Section.UiSettings" to "UI Settings",
        "Settings.Title" to "Settings",
        "Settings.UiSettings.App" to "App",
        "Settings.UiSettings.AppSummary" to "Appearance · Language · Theme",
        "TrafficStatistics.Action.Clear" to "Clear Statistics",
        "TrafficStatistics.Action.ClearConfirmMessage" to "This will permanently clear all traffic statistics.",
        "TrafficStatistics.Action.ClearSuccess" to "Statistics cleared",
        "TrafficStatistics.Chart.Daily" to "Daily",
        "TrafficStatistics.Chart.Hourly" to "4 Hours",
        "TrafficStatistics.Compare.LessThanYesterday" to "vs yesterday %s",
        "TrafficStatistics.Compare.MoreThanYesterday" to "vs yesterday +%s",
        "TrafficStatistics.Compare.SameAsYesterday" to "Same as yesterday",
        "TrafficStatistics.Compare.WeekStats" to "Last 7 days stats",
        "TrafficStatistics.Donut.Other" to "Other",
        "TrafficStatistics.EntrySummary" to "View traffic usage",
        "TrafficStatistics.Metric.Download" to "Download",
        "TrafficStatistics.Metric.Upload" to "Upload",
        "TrafficStatistics.Metric.UsageLine" to "Down %s  Up %s",
        "TrafficStatistics.Section.EmptyApps" to "No app traffic statistics",
        "TrafficStatistics.Section.TopApps" to "Top Apps",
        "TrafficStatistics.Section.Traffic" to "Traffic",
        "TrafficStatistics.Summary.TodayTraffic" to "Today's Traffic",
        "TrafficStatistics.Summary.WeekTraffic" to "This Week's Traffic",
        "TrafficStatistics.TimeRange.Today" to "Today",
        "TrafficStatistics.TimeRange.Week" to "This Week",
        "TrafficStatistics.Title" to "Traffic Statistics",
        "Util.Error.UnknownError" to "Unknown error",
    )

    private val TEXT_ZH = mapOf(
        "About.App.Description" to "基于 Mihomo 的 Material You Android 代理客户端",
        "About.App.VersionFailed" to "Failed to load",
        "About.App.VersionLoading" to "Loading...",
        "About.Copyright" to "© 2026 KokoroBox 贡献者",
        "About.License.AgplDescription" to "This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License.",
        "About.License.AgplName" to "GNU Affero General Public License v3.0",
        "About.License.CheckUpdate" to "检查更新",
        "About.License.CheckUpdateSummary" to "手动检查 GitHub Release 更新",
        "About.License.Libraries" to "引用库",
        "About.License.LibrariesSummary" to "View all third-party libraries used in this app",
        "About.Section.License" to "许可证",
        "About.Section.ProjectLinks" to "项目链接",
        "About.Title" to "关于",
        "About.Update.Available" to "有可用更新",
        "About.Update.BrowserDownload" to "将通过浏览器下载最新 APK。",
        "About.Update.Checking" to "正在检查 GitHub Releases…",
        "About.Update.ContinueInstall" to "继续",
        "About.Update.Download" to "下载 APK",
        "About.Update.Downloading" to "正在下载更新…",
        "About.Update.InAppDownload" to "下载并更新",
        "About.Update.InAppDownloadSummary" to "APK 会先下载并验证，再交由 Android 系统安装程序处理。",
        "About.Update.InstallPermissionRequired" to "请允许 KokoroBox 安装更新后再继续。",
        "About.Update.Installed" to "更新安装已完成。",
        "About.Update.InvalidResponse" to "GitHub 返回的版本信息不受支持，请稍后再试。",
        "About.Update.NetworkError" to "无法检查更新，请确认网络连接后重试。",
        "About.Update.NoApk" to "兼容的 APK 尚未就绪，请查看 Release 页面或稍后再检查。",
        "About.Update.NoBrowser" to "没有可打开此下载的应用。",
        "About.Update.NoRelease" to "目前尚无已发布的正式版本。",
        "About.Update.Ok" to "确定",
        "About.Update.OpenInstallSettings" to "打开设置",
        "About.Update.OpenRelease" to "查看 Release",
        "About.Update.PreparingInstall" to "正在准备系统安装程序…",
        "About.Update.RateLimited" to "GitHub 已限制请求次数，请稍后再检查。",
        "About.Update.Retry" to "重试",
        "About.Update.UnknownVersion" to "无法比较此构建的版本，请手动查看 GitHub Releases。",
        "About.Update.UpToDate" to "当前已是此版本或更新的版本。",
        "About.Update.UpdateFailed" to "更新失败",
        "About.Update.Verifying" to "正在验证下载的 APK…",
        "About.Update.WaitingForInstallConfirmation" to "请在 Android 安装对话框中继续。",
        "AccessControl.AppList.Loading" to "加载中...",
        "AccessControl.AppList.Title" to "应用列表 (%d 已选择)",
        "AccessControl.Button.Cancel" to "取消",
        "AccessControl.Button.Confirm" to "确定",
        "AccessControl.Search.Empty" to "没有匹配的应用",
        "AccessControl.Search.Placeholder" to "搜索应用...",
        "AccessControl.Settings.BatchOperation" to "批量操作",
        "AccessControl.Settings.ChinaApps" to "中国应用",
        "AccessControl.Settings.DescendingOrder" to "倒序排列",
        "AccessControl.Settings.DeselectAll" to "全不选",
        "AccessControl.Settings.Export" to "导出到剪贴板",
        "AccessControl.Settings.ExportSuccess" to "已复制 %d 个包名到剪贴板",
        "AccessControl.Settings.Import" to "从剪贴板导入",
        "AccessControl.Settings.ImportExport" to "导入/导出",
        "AccessControl.Settings.ImportFailed" to "导入失败",
        "AccessControl.Settings.ImportSuccess" to "导入成功：%d 个包名",
        "AccessControl.Settings.Invert" to "反选",
        "AccessControl.Settings.OverseasApps" to "非中国应用",
        "AccessControl.Settings.RegionQuickSelect" to "地区快捷选择",
        "AccessControl.Settings.RegionSelectResult" to "已按「%s」快捷选择，共 %d 个",
        "AccessControl.Settings.SelectAction" to "选择",
        "AccessControl.Settings.SelectAll" to "全选",
        "AccessControl.Settings.SelectedFirst" to "已选应用优先",
        "AccessControl.Settings.ShowSystemApps" to "显示系统应用",
        "AccessControl.Settings.SortMode" to "排序方式",
        "AccessControl.Settings.SortModeCurrent" to "当前：%s",
        "AccessControl.Settings.Title" to "访问控制设置",
        "AccessControl.SortMode.InstallTime" to "安装时间",
        "AccessControl.SortMode.Label" to "应用名称",
        "AccessControl.SortMode.PackageName" to "包名",
        "AccessControl.SortMode.UpdateTime" to "更新时间",
        "AccessControl.Title" to "访问控制",
        "AppDataManagement.GeoFiles.CacheItemSummary" to "%s · %s",
        "AppDataManagement.GeoFiles.DeleteComplete" to "已删除 %d 个历史 GeoX 文件",
        "AppDataManagement.GeoFiles.DeleteConfirmMessage" to "确定删除已选中的 %d 个历史 GeoX 文件吗？此操作无法撤销。",
        "AppDataManagement.GeoFiles.DeleteConfirmTitle" to "删除 GeoX 文件？",
        "AppDataManagement.GeoFiles.EmptyHistory" to "没有可清理的历史下载",
        "AppDataManagement.GeoFiles.EmptyHistorySummary" to "校验失败的 GeoX 文件会被直接删除，不会进入这里",
        "AppDataManagement.GeoFiles.HistorySummary" to "历史下载缓存：%d 个",
        "AppDataManagement.GeoFiles.HistoryTitle" to "管理 GeoX 文件",
        "AppDataManagement.Logs.DeleteComplete" to "已删除 %d 个日志文件",
        "AppDataManagement.Logs.DeleteConfirmMessage" to "确定删除已选中的 %d 个日志文件吗？此操作无法撤销。",
        "AppDataManagement.Logs.DeleteConfirmTitle" to "删除日志文件？",
        "AppDataManagement.Logs.EmptyLogContent" to "日志为空",
        "AppDataManagement.Logs.EmptyLogContentSummary" to "该文件没有可显示的日志条目",
        "AppDataManagement.Logs.EmptyLogs" to "没有可清理的日志",
        "AppDataManagement.Logs.EmptyLogsSummary" to "在日志页面开始记录后，日志文件会显示在这里",
        "AppDataManagement.Logs.LogItemSummary" to "%s · %s",
        "AppDataManagement.Logs.LogLineTitle" to "[%s] [%s]",
        "AppDataManagement.Logs.ManagementSummary" to "日志文件：%d 个",
        "AppDataManagement.Logs.ManagementTitle" to "管理日志",
        "AppDataManagement.Logs.RecordingFileTitle" to "%s（记录中）",
        "AppDataManagement.Logs.ViewerLimitHint" to "仅显示最近的日志",
        "AppDataManagement.Logs.ViewerLimitSummary" to "当前显示最近 %d 条",
        "AppDataManagement.Logs.ViewerTitle" to "查看日志：%s",
        "AppDataManagement.Section.GeoFiles" to "Geo 文件",
        "AppDataManagement.Section.Logs" to "日志",
        "AppDataManagement.Title" to "应用数据管理",
        "AppSettings.Backup.ExportFailed" to "导出设置备份失败",
        "AppSettings.Backup.ExportFailedDetail" to "导出设置备份失败：%s",
        "AppSettings.Backup.ExportSuccess" to "已导出设置备份",
        "AppSettings.Backup.ExportSummary" to "将应用偏好、网络选项、配置链接和显示设置保存为 JSON 文件",
        "AppSettings.Backup.ExportTitle" to "导出设置备份",
        "AppSettings.Backup.ImportFailedDetail" to "导入设置备份失败：%s",
        "AppSettings.Backup.ImportReadFailed" to "读取备份文件失败",
        "AppSettings.Backup.ImportSuccess" to "已导入设置备份",
        "AppSettings.Backup.ImportSummary" to "从 KokoroBox 备份 JSON 文件恢复设置",
        "AppSettings.Backup.ImportTitle" to "导入设置备份",
        "AppSettings.Behavior.AutoStartSummary" to "应用启动和开机时自动启动代理服务",
        "AppSettings.Behavior.AutoStartTitle" to "自动启动",
        "AppSettings.Behavior.AutoUpdateOnStartSummary" to "启动时自动更新当前订阅配置",
        "AppSettings.Behavior.AutoUpdateOnStartTitle" to "启动更新配置",
        "AppSettings.Behavior.AutomaticUpdateCheckSummary" to "打开应用时每天检查一次 GitHub Release",
        "AppSettings.Behavior.AutomaticUpdateCheckTitle" to "自动检查更新",
        "AppSettings.Behavior.UpdateChannelNightly" to "测试版（nightly）",
        "AppSettings.Behavior.UpdateChannelStable" to "正式版",
        "AppSettings.Behavior.UpdateChannelSummary" to "选择正式版或开发中的 nightly 测试版",
        "AppSettings.Behavior.UpdateChannelTitle" to "更新通道",
        "AppSettings.Behavior.UpdateInstallMethodRoot" to "Root",
        "AppSettings.Behavior.UpdateInstallMethodShizuku" to "Shizuku",
        "AppSettings.Behavior.UpdateInstallMethodSummary" to "高级设置：推荐系统安装。Shizuku 和 Root 会跳过 Android 安装确认。",
        "AppSettings.Behavior.UpdateInstallMethodSystem" to "系统",
        "AppSettings.Behavior.UpdateInstallMethodTitle" to "更新安装方式",
        "AppSettings.Button.Apply" to "应用",
        "AppSettings.Experimental.AcgHomeSummary" to "启用新的 ACG 风格首页布局",
        "AppSettings.Experimental.AcgHomeTitle" to "ACG 实验首页",
        "AppSettings.Experimental.AcgSidebarExpandedSummary" to "进入 ACG 首页时默认展开左侧信息栏",
        "AppSettings.Experimental.AcgSidebarExpandedTitle" to "ACG 侧栏默认展开",
        "AppSettings.Experimental.HealthCheckConcurrencySummary" to "当前选项：%s",
        "AppSettings.Experimental.HealthCheckConcurrencyTitle" to "测速并发数",
        "AppSettings.Experimental.ResetWallpaperSuccess" to "已恢复默认壁纸",
        "AppSettings.Experimental.ResetWallpaperSummary" to "恢复内置默认壁纸，并重置裁剪位置与壁纸取色",
        "AppSettings.Experimental.ResetWallpaperTitle" to "重置 ACG 壁纸",
        "AppSettings.Experimental.WallpaperSummary" to "为 ACG 首页选择自定义壁纸",
        "AppSettings.Experimental.WallpaperTitle" to "ACG 壁纸",
        "AppSettings.Interface.AutoHideNavbarSummary" to "向下滑动时自动隐藏底栏，向上滑动时显示",
        "AppSettings.Interface.AutoHideNavbarTitle" to "滑动隐藏底栏",
        "AppSettings.Interface.ColorThemeAcgWallpaperSummary" to "将使用当前 ACG 首页壁纸提取的主色生成主题；更换并应用壁纸后会自动更新取色。",
        "AppSettings.Interface.ColorThemeCodeLabel" to "主题色代码（#RRGGBB）",
        "AppSettings.Interface.ColorThemeCustomSummary" to "当前主题色：%s",
        "AppSettings.Interface.ColorThemeDynamicSummary" to "当前跟随系统配色",
        "AppSettings.Interface.ColorThemeModeAcgWallpaper" to "根据 ACG 壁纸取色",
        "AppSettings.Interface.ColorThemeModeCustom" to "自定义主题色",
        "AppSettings.Interface.ColorThemeModeMonet" to "跟随系统",
        "AppSettings.Interface.ColorThemeModeSummary" to "选择跟随系统或手动指定主题色",
        "AppSettings.Interface.ColorThemeModeTitle" to "取色来源",
        "AppSettings.Interface.ColorThemePickerTitle" to "选择主题色",
        "AppSettings.Interface.ColorThemeTitle" to "主题配色",
        "AppSettings.Interface.HomeControlFabSummary" to "使用悬浮按钮启动或停止代理，不再通过点击流量面板控制",
        "AppSettings.Interface.HomeControlFabTitle" to "首页控制悬浮按钮",
        "AppSettings.Interface.LanguageChinese" to "简体中文",
        "AppSettings.Interface.LanguageEnglish" to "English",
        "AppSettings.Interface.LanguageSummary" to "选择应用语言",
        "AppSettings.Interface.LanguageSystem" to "跟随系统",
        "AppSettings.Interface.LanguageTitle" to "应用语言",
        "AppSettings.Interface.LanguageTraditionalChinese" to "繁體中文",
        "AppSettings.Interface.LegacyNavbarStyleSummary" to "悬浮导航栏样式",
        "AppSettings.Interface.LegacyNavbarStyleTitle" to "浮动导航栏",
        "AppSettings.Interface.PageScaleDialogSummary" to "80% - 120%",
        "AppSettings.Interface.PageScaleSummary" to "调整应用界面的整体缩放比例",
        "AppSettings.Interface.PageScaleTitle" to "页面缩放",
        "AppSettings.Interface.ThemeColorPolarityInvertSummary" to "当主题取色看起来怪异时可尝试开启",
        "AppSettings.Interface.ThemeColorPolarityInvertTitle" to "反转主题前景色",
        "AppSettings.Interface.ThemeModeDark" to "深色",
        "AppSettings.Interface.ThemeModeLight" to "浅色",
        "AppSettings.Interface.ThemeModeSummary" to "选择应用的主题样式",
        "AppSettings.Interface.ThemeModeSystem" to "跟随系统",
        "AppSettings.Interface.ThemeModeTitle" to "主题模式",
        "AppSettings.Privacy.BiometricDialogTitleDisable" to "关闭启动验证",
        "AppSettings.Privacy.BiometricDialogTitleEnable" to "开启启动验证",
        "AppSettings.Privacy.BiometricExitButton" to "退出",
        "AppSettings.Privacy.BiometricPromptMessage" to "请完成验证以继续进入应用",
        "AppSettings.Privacy.BiometricPromptTitle" to "验证身份",
        "AppSettings.Privacy.BiometricRetryButton" to "重试",
        "AppSettings.Privacy.BiometricUnavailableHwUnavailable" to "生物识别硬件当前不可用，请稍后再试",
        "AppSettings.Privacy.BiometricUnavailableMessage" to "当前无法使用生物识别，请稍后重试或检查系统锁屏与生物识别设置",
        "AppSettings.Privacy.BiometricUnavailableNoDeviceCredential" to "当前未设置系统锁屏验证，请先启用 PIN、图案或密码",
        "AppSettings.Privacy.BiometricUnavailableNoHardware" to "当前设备不支持生物识别，请启用系统锁屏验证或关闭此功能",
        "AppSettings.Privacy.BiometricUnavailableNoneEnrolled" to "您尚未录入生物信息，请先在系统设置中录入，或启用 PIN、图案、密码等锁屏验证",
        "AppSettings.Privacy.BiometricUnavailableTitle" to "无法使用指纹验证",
        "AppSettings.Privacy.BiometricUnlockSummary" to "每次打开应用需要生物识别或锁屏凭据验证才能进入",
        "AppSettings.Privacy.BiometricUnlockTitle" to "启动时生物验证",
        "AppSettings.Privacy.HideFromRecentsSummary" to "启用后不在最近任务列表（后台卡片）中显示应用",
        "AppSettings.Privacy.HideFromRecentsTitle" to "隐藏后台卡片",
        "AppSettings.Privacy.HideIconSummary" to "隐藏后可通过拨号盘 *#*#0721#*#* 打开",
        "AppSettings.Privacy.HideIconTitle" to "隐藏应用图标",
        "AppSettings.Privacy.ScreenshotDialogTitleDisable" to "关闭禁止截图",
        "AppSettings.Privacy.ScreenshotDialogTitleEnable" to "开启禁止截图",
        "AppSettings.Privacy.ScreenshotProtectionSummary" to "禁止截图、录屏及最近任务预览",
        "AppSettings.Privacy.ScreenshotProtectionTitle" to "禁止截图",
        "AppSettings.Section.Backup" to "备份",
        "AppSettings.Section.Behavior" to "行为",
        "AppSettings.Section.Experimental" to "实验性内容",
        "AppSettings.Section.Interface" to "界面",
        "AppSettings.Section.Privacy" to "隐私",
        "AppSettings.Section.Service" to "服务",
        "AppSettings.ServiceSection.BatteryOptimizationTitle" to "电池优化无限制",
        "AppSettings.ServiceSection.ExitUiWhenBackgroundSummary" to "应用退到后台且界面不可见时主动释放 UI，仅保留运行中的代理服务",
        "AppSettings.ServiceSection.ExitUiWhenBackgroundTitle" to "后台隐藏时释放界面",
        "AppSettings.ServiceSection.SingleNodeTestSummary" to "点击节点卡片右侧图标测试单个节点延迟",
        "AppSettings.ServiceSection.SingleNodeTestTitle" to "单节点测试",
        "AppSettings.ServiceSection.TrafficNotificationSummary" to "在通知栏中显示流量使用情况",
        "AppSettings.ServiceSection.TrafficNotificationTitle" to "显示流量通知",
        "AppSettings.Title" to "应用设置",
        "AppSettings.WarningDialog.HideIconMsg1" to "请在隐藏之前确认你能够访问本应用的设置界面！",
        "AppSettings.WarningDialog.HideIconMsg2" to "对于 HyperOS, 请开启 自启动 和 后台弹出界面 权限，以接受拨号界面代码！",
        "AppSettings.WarningDialog.Title" to "警告",
        "Component.BottomBar.Config" to "配置文件",
        "Component.BottomBar.Home" to "首页",
        "Component.BottomBar.Proxy" to "代理",
        "Component.BottomBar.Setting" to "设置",
        "Component.Button.Cancel" to "取消",
        "Component.Button.Clear" to "清除",
        "Component.Button.Confirm" to "确定",
        "Component.Button.Copy" to "复制",
        "Component.Button.Delete" to "删除",
        "Component.Button.Ok" to "确定",
        "Component.ConfigInput.CountItems" to "%d 项",
        "Component.ConfigInput.PortLabel" to "端口号 (留空表示不修改)",
        "Component.Editor.Action.Add" to "添加",
        "Component.Editor.Action.Delete" to "删除",
        "Component.Editor.Action.Reset" to "重置",
        "Component.Editor.Action.Search" to "搜索",
        "Component.Editor.CountItems" to "共 %d 项",
        "Component.Editor.Dialog.AddTitle" to "添加条目",
        "Component.Editor.Dialog.EditTitle" to "编辑条目",
        "Component.Editor.Dialog.ResetMessage" to "清空所有条目并恢复为不修改状态？",
        "Component.Editor.Dialog.ResetTitle" to "重置确认",
        "Component.Editor.Empty.Hint" to "点击右上角按钮添加",
        "Component.Editor.Empty.Title" to "暂无条目",
        "Component.Editor.Error.KeyEmpty" to "键不能为空",
        "Component.Editor.Error.KeyExists" to "键已存在",
        "Component.Editor.Rule.Content" to "规则内容",
        "Component.Editor.Rule.ErrorContentRequired" to "规则内容不能为空",
        "Component.Editor.Rule.ErrorTargetRequired" to "请选择目标",
        "Component.Editor.Rule.NoResolve" to "不解析",
        "Component.Editor.Rule.Src" to "源 IP",
        "Component.Editor.Rule.Target" to "目标",
        "Component.Editor.Rule.TargetDirect" to "DIRECT",
        "Component.Editor.Rule.TargetMatch" to "MATCH",
        "Component.Editor.Rule.TargetReject" to "REJECT",
        "Component.Editor.Rule.Type" to "规则类型",
        "Component.Flag.ContentDescription" to "%s 旗帜",
        "Component.Loading.Starting" to "启动中...",
        "Component.Message.Confirm" to "确定",
        "Component.Message.Error" to "错误",
        "Component.Message.Hint" to "提示",
        "Component.Message.Success" to "成功",
        "Component.Navigation.Back" to "返回",
        "Component.Navigation.Refresh" to "刷新",
        "Component.ProfileCard.ClickToUpdate" to "点击更新获取订阅信息",
        "Component.ProfileCard.DaysAgo" to "%d 天前",
        "Component.ProfileCard.Delete" to "删除",
        "Component.ProfileCard.Edit" to "编辑",
        "Component.ProfileCard.ExpireAt" to "到期：%s (剩余%d 天)",
        "Component.ProfileCard.ExpireToday" to "到期：今天",
        "Component.ProfileCard.Expired" to "已过期：%s",
        "Component.ProfileCard.Export" to "导出",
        "Component.ProfileCard.HoursAgo" to "%d 小时前",
        "Component.ProfileCard.JustNow" to "刚刚",
        "Component.ProfileCard.LocalConfig" to "本地配置文件",
        "Component.ProfileCard.LocalFile" to "本地文件",
        "Component.ProfileCard.MinutesAgo" to "%d 分钟前",
        "Component.ProfileCard.RemoteSubscription" to "远程订阅",
        "Component.ProfileCard.Traffic" to "流量：%s / %s (%d%%)",
        "Component.ProfileCard.Update" to "更新",
        "Component.ProfileCard.UsedTraffic" to "已用：%s",
        "Component.Selector.Append" to "后置",
        "Component.Selector.Disable" to "禁用",
        "Component.Selector.Enable" to "启用",
        "Component.Selector.Merge" to "合并",
        "Component.Selector.NotModify" to "不修改",
        "Component.Selector.Prepend" to "前置",
        "Component.Selector.Replace" to "替换",
        "Component.Update.Action.DownloadNow" to "立即下载",
        "Component.Update.Message.Available" to "检测到可用更新",
        "Component.Update.Message.CheckFailed" to "检查更新失败：%s",
        "Component.Update.Message.Checking" to "正在检查更新...",
        "Component.Update.Message.Close" to "知道了",
        "Component.Update.Message.CoverDesc" to "更新封面",
        "Component.Update.Message.CurrentVersion" to "当前版本",
        "Component.Update.Message.DownloadAlreadyRunning" to "更新包仍在下载中",
        "Component.Update.Message.DownloadErrorWithCode" to "下载失败 (%d): %s",
        "Component.Update.Message.DownloadReady" to "下载完成，准备安装",
        "Component.Update.Message.Downloading" to "正在下载更新包...",
        "Component.Update.Message.DownloadingWithProgress" to "正在下载更新包 %d%%",
        "Component.Update.Message.Error" to "下载失败，请稍后重试",
        "Component.Update.Message.Finished" to "下载完成，等待安装确认",
        "Component.Update.Message.InstallFailed" to "打开安装器失败：%s",
        "Component.Update.Message.InstallPromptOpened" to "下载完成，已打开安装器",
        "Component.Update.Message.MissingReleaseMetadata" to "最新发布缺少版本元数据",
        "Component.Update.Message.NoCompatibleAsset" to "最新发布中没有找到适合当前设备的 APK",
        "Component.Update.Message.NoUpdate" to "当前已经是最新版本",
        "Component.Update.Message.Preparing" to "正在准备下载...",
        "Component.Update.Message.RemoteVersion" to "推送版本",
        "Component.Update.Message.Updating" to "正在更新",
        "Component.Update.Message.VerifyFailed" to "安装包校验失败，请重试",
        "Component.Update.Message.Verifying" to "正在校验安装包...",
        "Component.Update.Message.Waiting" to "等待下载开始...",
        "Component.Update.Title.Available" to "发现新版本",
        "Component.WebView.InvalidUrl" to "无效的 URL",
        "Connection.ChainCount" to "x%d",
        "Connection.Detail.Action.Interrupt" to "打断连接",
        "Connection.Detail.Action.Interrupting" to "正在打断连接...",
        "Connection.Detail.Label.Content" to "内容",
        "Connection.Detail.Label.DestinationAddress" to "目标地址",
        "Connection.Detail.Label.Download" to "下载",
        "Connection.Detail.Label.Duration" to "连接时长",
        "Connection.Detail.Label.Process" to "进程",
        "Connection.Detail.Label.Protocol" to "协议",
        "Connection.Detail.Label.SourceAddress" to "源地址",
        "Connection.Detail.Label.Type" to "类型",
        "Connection.Detail.Label.Upload" to "上传",
        "Connection.Detail.Section.Info" to "连接信息",
        "Connection.Detail.Section.Rule" to "规则",
        "Connection.Empty" to "暂无活动连接",
        "Connection.Loading" to "加载中...",
        "Connection.NoResults" to "没有匹配的连接",
        "Connection.RelativeTime.Date" to "%02d-%02d",
        "Connection.RelativeTime.DaysAgo" to "%d 天前",
        "Connection.RelativeTime.HoursAgo" to "%d 小时前",
        "Connection.RelativeTime.JustNow" to "刚刚",
        "Connection.RelativeTime.MinutesAgo" to "%d 分钟前",
        "Connection.Search" to "搜索",
        "Connection.SearchHint" to "搜索主机、进程...",
        "Connection.Sort.Download" to "下载",
        "Connection.Sort.Host" to "主机",
        "Connection.Sort.Time" to "时间",
        "Connection.Sort.Upload" to "上传",
        "Connection.SortBy" to "排序:",
        "Connection.Summary" to "查看当前活动连接",
        "Connection.Tab.Active" to "活动中",
        "Connection.Tab.Closed" to "已关闭",
        "Connection.Title" to "连接",
        "Editor.Action.Discard" to "放弃",
        "Editor.Action.Format" to "格式化",
        "Editor.Action.Save" to "保存",
        "Editor.Common.ConfigPreviewTitle" to "配置预览",
        "Editor.Common.EditConfigTitle" to "编辑配置",
        "Editor.Common.EditOverrideConfigTitle" to "编辑覆写配置",
        "Editor.Common.EditProfileConfigTitle" to "编辑订阅配置",
        "Editor.Common.JsonSubtitle" to "使用 JSON 格式编辑",
        "Editor.Diagnostic.DuplicateKey" to "重复的键",
        "Editor.Diagnostic.Expected" to "期望 %s",
        "Editor.Diagnostic.JsonFormatError" to "JSON 格式错误",
        "Editor.Diagnostic.JsonMustStartWithObjectOrArray" to "JSON 必须以 '{' 或 '[' 开头",
        "Editor.Diagnostic.JsonSyntaxError" to "JSON 语法错误",
        "Editor.Diagnostic.NoValue" to "缺少值",
        "Editor.Diagnostic.Unknown" to "未知",
        "Editor.Diagnostic.Unterminated" to "未终止的字符串或对象",
        "Editor.Dialog.DiscardTitle" to "放弃修改",
        "Editor.Dialog.UnsavedChangesMessage" to "当前有未保存的修改，确定要放弃吗？",
        "Editor.Dialog.UnsavedChangesTitle" to "未保存的修改",
        "Editor.Toast.FormatFailedOrUnchanged" to "格式化失败或无需格式化",
        "Editor.Toast.FormatSuccess" to "格式化成功",
        "Editor.Toast.SaveFailed" to "保存失败",
        "Editor.Toast.SyntaxError" to "语法错误，请检查内容",
        "Feature.Node.HealthCheckConcurrencySummary" to "当前选项：%s",
        "Feature.Node.HealthCheckConcurrencyTitle" to "测速并发数",
        "Feature.Node.Section" to "节点",
        "Feature.RuntimeConfig.Empty" to "运行时配置文件没有内容",
        "Feature.RuntimeConfig.NotReady" to "运行时配置尚未准备完成",
        "Feature.RuntimeConfig.NotRunning" to "请先启动代理，再查看运行时配置",
        "Feature.RuntimeConfig.PreviewTitle" to "运行时配置 · %s",
        "Feature.RuntimeConfig.RuntimeChanged" to "运行中的配置已变更，请重新打开",
        "Feature.RuntimeConfig.Section" to "诊断",
        "Feature.RuntimeConfig.Summary" to "查看 Mihomo 当前实际加载的只读 YAML；内容可能包含敏感数据",
        "Feature.RuntimeConfig.Title" to "查看运行时配置",
        "Feature.RuntimeConfig.Unavailable" to "找不到当前的运行时配置文件",
        "Feature.RuntimeConfig.UnknownProfile" to "未知配置",
        "Feature.SpeedTest.Cancel" to "取消",
        "Feature.SpeedTest.DataUsage" to "每次测试最多使用 32 MB 下载与 8 MB 上传流量",
        "Feature.SpeedTest.Download" to "下载",
        "Feature.SpeedTest.EdgeLocation" to "Cloudflare Edge",
        "Feature.SpeedTest.Error" to "无法完成速度测试，请检查连接后再试一次。",
        "Feature.SpeedTest.Jitter" to "抖动",
        "Feature.SpeedTest.Latency" to "延迟",
        "Feature.SpeedTest.LocationUnknown" to "未知",
        "Feature.SpeedTest.Preparing" to "正在准备测试…",
        "Feature.SpeedTest.PrivacyNotice" to "测试流量会发送至 Cloudflare；不包含丢包测试。",
        "Feature.SpeedTest.Section" to "网络测试",
        "Feature.SpeedTest.Start" to "开始测试",
        "Feature.SpeedTest.Summary" to "测量当前连接至 Cloudflare Edge 的质量",
        "Feature.SpeedTest.TestingDownload" to "正在测量下载速度…",
        "Feature.SpeedTest.TestingLatency" to "正在测量延迟…",
        "Feature.SpeedTest.TestingUpload" to "正在测量上传速度…",
        "Feature.SpeedTest.Title" to "Cloudflare 速度测试",
        "Feature.SpeedTest.Upload" to "上传",
        "Feature.Title" to "高级功能",
        "Home.Control.HintAddProfile" to "请先添加配置文件",
        "Home.Control.HintEnableProfile" to "请先在「配置」页面启用一个配置",
        "Home.Control.HintProfilesLoading" to "配置文件仍在加载，请稍候。",
        "Home.Control.Start" to "启动",
        "Home.Control.Stop" to "停止",
        "Home.IpInfo.ExitIp" to "出口 IP",
        "Home.Message.ConfigSwitchFailed" to "配置切换失败：%s",
        "Home.Message.ConfigSwitched" to "配置已切换",
        "Home.Message.ControlBusy" to "代理%s，请稍候。",
        "Home.Message.Preparing" to "正在准备...",
        "Home.Message.StartFailed" to "启动失败：%s",
        "Home.Message.StopFailed" to "停止失败：%s",
        "Home.Message.WaitingForVpnPermission" to "正在等待 VPN 授权",
        "Home.NodeInfo.Delay" to "延迟",
        "Home.NodeInfo.DelayValue" to "%dms",
        "Home.NodeInfo.Node" to "节点",
        "Home.NodeInfo.Unknown" to "未知",
        "Home.ProxyMode.Http" to "HTTP",
        "Home.ProxyMode.Tun" to "TUN",
        "Home.ProxyMode.Vpn" to "VPN",
        "Home.Status.Connecting" to "连接中",
        "Home.Status.Disconnecting" to "断开中",
        "Home.Status.Running" to "运行中",
        "Home.Status.TapFabToStart" to "点击悬浮按钮启动",
        "Home.Status.TapToStart" to "轻触启动",
        "Home.Title" to "KokoroBox",
        "Home.Traffic.DownShort" to "DOWN",
        "Home.Traffic.NoProfile" to "无配置",
        "Home.Traffic.UpShort" to "UP",
        "Log.Action.Save" to "保存",
        "Log.Action.StartRecording" to "开始记录",
        "Log.Action.StopRecording" to "停止记录",
        "Log.Detail.WaitingLog" to "等待日志...",
        "Log.Detail.WillShowWhenGenerated" to "日志将在产生时显示",
        "Log.Empty.NoLogs" to "暂无日志记录",
        "Log.Empty.StartRecordingHint" to "点击右下角按钮开始记录日志",
        "Log.Title" to "日志",
        "MetaFeature.AgeKey.DerivePublicKey" to "推导公钥",
        "MetaFeature.AgeKey.Generate" to "生成",
        "MetaFeature.AgeKey.HybridTitle" to "mlkem768-x25519",
        "MetaFeature.AgeKey.PublicKey" to "公钥",
        "MetaFeature.AgeKey.SecretKey" to "私钥",
        "MetaFeature.AgeKey.Section" to "Age 密钥",
        "MetaFeature.AgeKey.X25519Title" to "X25519",
        "MetaFeature.CustomRules.AddRule" to "新增规则",
        "MetaFeature.CustomRules.BackToKokoroSettings" to "返回 Kokoro 设置",
        "MetaFeature.CustomRules.Cancel" to "取消",
        "MetaFeature.CustomRules.Confirm" to "确认",
        "MetaFeature.CustomRules.ConflictMessage" to "请选择远程版本，或保留本地编辑并使用最新 revision 再次保存。",
        "MetaFeature.CustomRules.ConflictTitle" to "规则已在其他设备更改",
        "MetaFeature.CustomRules.DeleteRule" to "删除规则",
        "MetaFeature.CustomRules.DiscardMessage" to "此操作会放弃尚未保存的更改。",
        "MetaFeature.CustomRules.DiscardTitle" to "放弃本地更改？",
        "MetaFeature.CustomRules.EditRule" to "编辑规则",
        "MetaFeature.CustomRules.Empty" to "default 规则目前为空。",
        "MetaFeature.CustomRules.ErrorLoad" to "无法加载自定义规则。",
        "MetaFeature.CustomRules.ErrorNotFound" to "default 规则目前无法使用，已重新加载服务器状态。",
        "MetaFeature.CustomRules.ErrorRateLimited" to "请求过于频繁，请稍后再试。",
        "MetaFeature.CustomRules.ErrorRequest" to "服务器拒绝了此项请求。",
        "MetaFeature.CustomRules.ErrorUnknown" to "无法确认保存结果，继续前请检查远程版本。",
        "MetaFeature.CustomRules.ErrorValidation" to "第 %d 条规则无效，请刷新选项并修正后再保存。",
        "MetaFeature.CustomRules.ErrorValidationGeneral" to "规则列表无效，请刷新选项并修正后再保存。",
        "MetaFeature.CustomRules.KeepLocal" to "保留本地版本",
        "MetaFeature.CustomRules.Loading" to "正在加载自定义规则…",
        "MetaFeature.CustomRules.MatchPayloadHint" to "MATCH 不使用内容字段。",
        "MetaFeature.CustomRules.MoveDown" to "向下移动规则",
        "MetaFeature.CustomRules.MoveUp" to "向上移动规则",
        "MetaFeature.CustomRules.Payload" to "内容",
        "MetaFeature.CustomRules.Provider" to "规则提供者",
        "MetaFeature.CustomRules.Refresh" to "从服务器重新加载",
        "MetaFeature.CustomRules.Retry" to "重试",
        "MetaFeature.CustomRules.Rules" to "规则",
        "MetaFeature.CustomRules.Save" to "保存规则",
        "MetaFeature.CustomRules.Saved" to "自定义规则已保存",
        "MetaFeature.CustomRules.Target" to "目标",
        "MetaFeature.CustomRules.Title" to "自定义规则",
        "MetaFeature.CustomRules.Type" to "类型",
        "MetaFeature.CustomRules.UseRemote" to "使用远程版本",
        "MetaFeature.Download.DialogTitle" to "在线更新 GeoX",
        "MetaFeature.Download.DownloadComplete" to "下载完成：%d/%d",
        "MetaFeature.Download.ImportFailed" to "%s 导入失败：文件无效或校验未通过",
        "MetaFeature.Download.ImportSuccess" to "%s 已导入并应用",
        "MetaFeature.Download.LastUpdate" to "上次更新（%s）：%s",
        "MetaFeature.Download.LastUpdateNever" to "暂无记录",
        "MetaFeature.Download.LastUpdateSourceLocal" to "本地",
        "MetaFeature.Download.LastUpdateSourceOnline" to "在线",
        "MetaFeature.Download.LocalDialogTitle" to "本地更新 GeoX",
        "MetaFeature.Download.ProgressDetail" to "%s / %s · %s",
        "MetaFeature.Download.ProgressDetailUnknownTotal" to "%s · %s",
        "MetaFeature.Download.ProgressFailed" to "下载或校验失败，已保留可用回退文件",
        "MetaFeature.Download.ProgressSuccess" to "文件已更新并通过校验",
        "MetaFeature.Download.ProgressSummary" to "请保持此页面打开，正在下载并校验文件",
        "MetaFeature.Download.ProgressTitle" to "正在更新 GeoX",
        "MetaFeature.Download.ProgressWaiting" to "等待开始",
        "MetaFeature.Download.SelectFiles" to "请选择要更新的文件",
        "MetaFeature.Download.StatusDownloading" to "下载中 %d%%",
        "MetaFeature.Download.StatusFailed" to "失败",
        "MetaFeature.Download.StatusPending" to "等待中",
        "MetaFeature.Download.StatusSuccess" to "已完成",
        "MetaFeature.Download.StatusValidating" to "校验中",
        "MetaFeature.GeoX.LocalUpdateSummary" to "从本地文件导入并应用数据库",
        "MetaFeature.GeoX.LocalUpdateTitle" to "本地更新 GeoX",
        "MetaFeature.GeoX.OnlineUpdateSummary" to "从远程源下载数据库文件",
        "MetaFeature.GeoX.OnlineUpdateTitle" to "在线更新 GeoX",
        "MetaFeature.GeoX.RuntimeHomeInfo" to "GeoX 数据库文件下载并存放在内核目录：%s",
        "MetaFeature.Section.ConnectionAndTraffic" to "连接与流量统计",
        "MetaFeature.Section.GeoXUpdate" to "GeoX 更新",
        "MetaFeature.Title" to "Meta 功能",
        "NetworkSettings.Error.RootRequired" to "RootTun 需要 KokoroBox 已获取可用的 Root 权限",
        "NetworkSettings.Error.VpnDenied" to "VPN 权限被拒绝",
        "NetworkSettings.Experimental.AntiPollutionDnsSummary" to "使用加密的境内外 DNS 与分流策略覆盖订阅配置；需要 GeoSite 数据",
        "NetworkSettings.Experimental.AntiPollutionDnsTitle" to "抗污染 DNS",
        "NetworkSettings.Network.CustomUserAgentSummaryDefault" to "未设置，使用默认值",
        "NetworkSettings.Network.CustomUserAgentTitle" to "自定义 User-Agent",
        "NetworkSettings.Network.UserAgentDialogTitle" to "编辑 User-Agent",
        "NetworkSettings.ProxyOptions.AccessControlModeTitle" to "访问控制模式",
        "NetworkSettings.ProxyOptions.AllowAll" to "允许所有",
        "NetworkSettings.ProxyOptions.AllowSelected" to "允许选择",
        "NetworkSettings.ProxyOptions.ManageAccessControlSummary" to "为应用和域名配置访问控制规则",
        "NetworkSettings.ProxyOptions.ManageAccessControlTitle" to "管理访问控制列表",
        "NetworkSettings.ProxyOptions.RejectSelected" to "拒绝选择",
        "NetworkSettings.ProxyOptions.TunStackTitle" to "TUN 协议栈",
        "NetworkSettings.RootTun.AutoRedirectSummary" to "自动启用 RootTun 所需的重定向规则",
        "NetworkSettings.RootTun.AutoRedirectTitle" to "自动重定向",
        "NetworkSettings.RootTun.AutoRouteSummary" to "自动添加转发所需路由",
        "NetworkSettings.RootTun.AutoRouteTitle" to "自动路由",
        "NetworkSettings.RootTun.DnsModeFakeIp" to "FakeIP",
        "NetworkSettings.RootTun.DnsModeRedirHost" to "RedirHost",
        "NetworkSettings.RootTun.DnsModeSummary" to "选择 RedirHost 或 FakeIP",
        "NetworkSettings.RootTun.DnsModeTitle" to "DNS 模式",
        "NetworkSettings.RootTun.FakeIpRange6Summary" to "仅在 FakeIP 模式下生效",
        "NetworkSettings.RootTun.FakeIpRange6Title" to "FakeIP IPv6 地址段",
        "NetworkSettings.RootTun.FakeIpRangeSummary" to "仅在 FakeIP 模式下生效",
        "NetworkSettings.RootTun.FakeIpRangeTitle" to "FakeIP IPv4 地址段",
        "NetworkSettings.RootTun.IfNameSummary" to "RootTun 创建的虚拟网卡名",
        "NetworkSettings.RootTun.IfNameTitle" to "接口名称",
        "NetworkSettings.RootTun.MtuSummary" to "RootTun 链路的最大传输单元",
        "NetworkSettings.RootTun.MtuTitle" to "MTU",
        "NetworkSettings.RootTun.StrictRouteSummary" to "仅允许命中的流量走 RootTun",
        "NetworkSettings.RootTun.StrictRouteTitle" to "严格路由",
        "NetworkSettings.Section.Experimental" to "实验性功能",
        "NetworkSettings.Section.Network" to "网络",
        "NetworkSettings.Section.ProxyOptions" to "访问控制",
        "NetworkSettings.Section.VpnOptions" to "服务配置",
        "NetworkSettings.Section.VpnService" to "代理模式",
        "NetworkSettings.Title" to "网络设置",
        "NetworkSettings.VpnOptions.AllowBypassSummary" to "允许应用绕过 VPN",
        "NetworkSettings.VpnOptions.AllowBypassTitle" to "允许应用绕过",
        "NetworkSettings.VpnOptions.BypassPrivateSummary" to "绕过私有网络和本地地址",
        "NetworkSettings.VpnOptions.BypassPrivateTitle" to "绕过私有网络",
        "NetworkSettings.VpnOptions.DnsHijackSummary" to "将所有 DNS 请求重定向到 KokoroBox",
        "NetworkSettings.VpnOptions.DnsHijackTitle" to "DNS 劫持",
        "NetworkSettings.VpnOptions.EnableIpv6Summary" to "允许通过 VPN 路由 IPv6 流量",
        "NetworkSettings.VpnOptions.EnableIpv6Title" to "运行 IPv6",
        "NetworkSettings.VpnOptions.SystemProxySummary" to "仅在 VPN 模式下，为未走 TUN 的应用设置 HTTP 代理",
        "NetworkSettings.VpnOptions.SystemProxyTitle" to "VPN 系统代理",
        "NetworkSettings.VpnService.RootTunMode" to "Root TUN",
        "NetworkSettings.VpnService.RouteTrafficSummary" to "选择当前用于接管系统流量的代理模式",
        "NetworkSettings.VpnService.RouteTrafficTitle" to "路由系统流量",
        "NetworkSettings.VpnService.SystemProxy" to "HTTP 系统代理",
        "NetworkSettings.VpnService.VpnMode" to "VPN 模式",
        "Onboarding.Finish.Subtitle" to "基础设置已就绪即将进入主页",
        "Onboarding.Finish.Title" to "准备完成",
        "Onboarding.Navigation.Back" to "返回",
        "Onboarding.Navigation.Enter" to "进入应用",
        "Onboarding.Navigation.Next" to "下一步",
        "Onboarding.Navigation.Start" to "开始设置",
        "Onboarding.Permission.AppList.SummaryNeed" to "用于分应用代理等功能",
        "Onboarding.Permission.AppList.Title" to "应用列表权限",
        "Onboarding.Permission.Common.Granted" to "已授权",
        "Onboarding.Permission.Notification.SummaryNeed" to "用于显示连接状态和流量通知",
        "Onboarding.Permission.Notification.SummaryNotRequired" to "当前系统无需额外授权",
        "Onboarding.Permission.Notification.Title" to "通知权限",
        "Onboarding.Permission.Subtitle" to "权限会影响通知和分应用代理等功能",
        "Onboarding.Permission.Title" to "确认运行权限",
        "Onboarding.Personalize.Subtitle" to "主题模式和主色可随时在设置中修改",
        "Onboarding.Personalize.Title" to "调整界面风格",
        "Onboarding.Privacy.Accept.Title" to "我已阅读并同意隐私政策",
        "Onboarding.Privacy.PolicyLink" to "《隐私政策》",
        "Onboarding.Privacy.Privacy.Title" to "隐私政策",
        "Onboarding.Privacy.RichTextLead" to "在开始使用前需要同意隐私政策，才能继续使用 KokoroBox。",
        "Onboarding.Privacy.RichTextPrefix" to "继续前请先阅读并同意",
        "Onboarding.Privacy.RichTextSuffix" to "。",
        "Onboarding.Privacy.Subtitle" to "阅读并同意隐私政策后方可继续",
        "Onboarding.Privacy.Title" to "确认隐私政策",
        "Onboarding.Sheet.LoadFailed" to "无法加载协议内容",
        "Onboarding.Sheet.PrivacyPolicyTitle" to "隐私政策",
        "OpenSourceLicenses.LicenseSheet.NoContent" to "暂无许可证内容",
        "OpenSourceLicenses.Title" to "开源许可证",
        "Override.Action.Create" to "创建配置",
        "Override.Action.Import" to "导入 JSON",
        "Override.Action.ImportFile" to "导入配置文件",
        "Override.Action.New" to "新建配置",
        "Override.Card.Copy" to "复制配置",
        "Override.Card.Delete" to "删除配置",
        "Override.Card.DeleteButton" to "删除",
        "Override.Card.Edit" to "编辑配置",
        "Override.Card.EditButton" to "编辑",
        "Override.Card.Export" to "导出配置",
        "Override.Card.NoDescription" to "未填写描述",
        "Override.Dialog.Button.Cancel" to "取消",
        "Override.Dialog.Button.Delete" to "删除",
        "Override.Dialog.Create.Description" to "配置描述",
        "Override.Dialog.Create.ImportHint" to "选择 JSON 文件导入覆写配置",
        "Override.Dialog.Create.Name" to "配置名称",
        "Override.Dialog.Create.Title" to "添加配置",
        "Override.Dialog.Delete.InUseMessage" to "配置 %s 正在被订阅使用，删除后将解除绑定关系。确定要删除吗？此操作不可恢复。",
        "Override.Dialog.Delete.Message" to "确定要删除配置 %s 吗？此操作不可恢复。",
        "Override.Dialog.Delete.Title" to "删除配置",
        "Override.Dialog.EditOptions.CodeEditor" to "代码编辑器",
        "Override.Dialog.EditOptions.Title" to "编辑配置",
        "Override.Dialog.EditOptions.VisualEditor" to "可视化编辑",
        "Override.Dns.AppendSystem" to "追加系统 DNS",
        "Override.Dns.Default" to "默认 DNS",
        "Override.Dns.DefaultHint" to "用于解析 DNS 服务器域名",
        "Override.Dns.EnhancedDisable" to "禁用",
        "Override.Dns.EnhancedFakeip" to "FakeIP",
        "Override.Dns.EnhancedMapping" to "Mapping",
        "Override.Dns.EnhancedMode" to "增强模式",
        "Override.Dns.EnhancedNotModify" to "不修改",
        "Override.Dns.FakeipBlacklist" to "黑名单",
        "Override.Dns.FakeipFilter" to "FakeIP 过滤",
        "Override.Dns.FakeipFilterHint" to "例如：+.lan, localhost",
        "Override.Dns.FakeipFilterMode" to "FakeIP 过滤模式",
        "Override.Dns.FakeipWhitelist" to "白名单",
        "Override.Dns.Fallback" to "备用 DNS",
        "Override.Dns.FallbackDomain" to "域名回退",
        "Override.Dns.FallbackDomainHint" to "例如：+.google.com",
        "Override.Dns.FallbackGeoip" to "GeoIP 回退",
        "Override.Dns.FallbackGeoipCode" to "GeoIP 代码",
        "Override.Dns.FallbackGeoipCodeHint" to "例如：CN",
        "Override.Dns.FallbackHint" to "例如：1.1.1.1",
        "Override.Dns.FallbackIpcidr" to "IP CIDR 回退",
        "Override.Dns.FallbackIpcidrHint" to "例如：240.0.0.0/4",
        "Override.Dns.Ipv6" to "DNS IPv6",
        "Override.Dns.Listen" to "监听地址",
        "Override.Dns.ListenHint" to "例如：0.0.0.0:53",
        "Override.Dns.NameserverPolicy" to "DNS 策略",
        "Override.Dns.NameserverPolicyKey" to "域名匹配规则",
        "Override.Dns.NameserverPolicyValue" to "DNS 服务器",
        "Override.Dns.Policy" to "DNS 策略",
        "Override.Dns.PolicyForceEnable" to "强制启用",
        "Override.Dns.PolicyNotModify" to "不修改",
        "Override.Dns.PolicyUseBuiltin" to "使用内置",
        "Override.Dns.PreferH3" to "优先 HTTP/3",
        "Override.Dns.Servers" to "DNS 服务器",
        "Override.Dns.ServersHint" to "例如：8.8.8.8, tls://dns.google",
        "Override.Dns.UseHosts" to "使用 hosts",
        "Override.Draft.AddExtraField" to "新增额外字段",
        "Override.Draft.AddHealthCheckField" to "新增 health-check 额外字段",
        "Override.Draft.AddOverrideField" to "新增 override 额外字段",
        "Override.Draft.Apply" to "应用",
        "Override.Draft.BasicIdentity" to "基础身份",
        "Override.Draft.BasicInfo" to "基础信息",
        "Override.Draft.BasicRouting" to "基础分流",
        "Override.Draft.BooleanOptions" to "布尔选项",
        "Override.Draft.ClickToAddExtraField" to "点击添加额外字段",
        "Override.Draft.ConfigDescription" to "配置说明",
        "Override.Draft.ConfigName" to "配置名称",
        "Override.Draft.ConfigSections" to "配置分区",
        "Override.Draft.CoreSource" to "核心来源",
        "Override.Draft.DeleteExtraField" to "删除额外字段",
        "Override.Draft.DoubleValue" to "浮点数值",
        "Override.Draft.EditExtraField" to "编辑额外字段",
        "Override.Draft.EditHealthCheckField" to "编辑 health-check 额外字段",
        "Override.Draft.EditOverrideField" to "编辑 override 额外字段",
        "Override.Draft.EditSubRules" to "编辑子规则",
        "Override.Draft.ExtraFields" to "额外字段",
        "Override.Draft.ExtraFieldsConfigured" to "已配置 %d 个额外字段",
        "Override.Draft.FallbackRegionGroupTitle" to "Fallback 地区组",
        "Override.Draft.GroupTypeFallback" to "Fallback",
        "Override.Draft.GroupTypeTitle" to "策略组类型",
        "Override.Draft.GroupTypeUrlTest" to "UrlTest",
        "Override.Draft.HeaderHint" to "每行一个 header，格式：Key: value1 | value2",
        "Override.Draft.HealthCheckFields" to "Health Check 额外字段",
        "Override.Draft.HealthCheckSwitch" to "Health Check 开关",
        "Override.Draft.IntValue" to "整数值",
        "Override.Draft.JsonFragment" to "单个 JSON 片段",
        "Override.Draft.KeyNameEmpty" to "键名不能为空",
        "Override.Draft.Name" to "名称",
        "Override.Draft.NameRequired" to "名称不能为空",
        "Override.Draft.NetworkAuth" to "网络与认证",
        "Override.Draft.NoRules" to "未配置规则",
        "Override.Draft.Object" to "对象",
        "Override.Draft.OfficialMrs" to "官方 MRS 常用分流",
        "Override.Draft.OfficialMrsSummary" to "顶部模板编辑器，支持地区自动组和每个分流项单独开关；应用时会重建当前覆写里的规则三块。",
        "Override.Draft.OverrideFields" to "Override 额外字段",
        "Override.Draft.OverrideSwitch" to "Override 开关",
        "Override.Draft.PresetApplySummary" to "应用后会覆盖当前覆写里的规则提供者、策略组和规则",
        "Override.Draft.PresetTemplate" to "预设分流模板",
        "Override.Draft.RegionalAutoGroup" to "地区自动组",
        "Override.Draft.RuleList" to "规则列表",
        "Override.Draft.RulesConfigured" to "已配置 %d 条规则",
        "Override.Draft.Save" to "保存",
        "Override.Draft.ServiceRouting" to "服务分流",
        "Override.Draft.StringValue" to "字符串值",
        "Override.Draft.SubRuleGroup" to "子规则组",
        "Override.Draft.UrlTestRegionGroupTitle" to "UrlTest 地区组",
        "Override.Draft.ValueType" to "值类型",
        "Override.Draft.ValueTypeMismatch" to "当前值与所选类型不匹配",
        "Override.Edit.Button.Cancel" to "取消",
        "Override.Edit.Button.Discard" to "放弃",
        "Override.Edit.EmptyName.Summary" to "当前名称为空，无法实时保存。确定放弃这次未保存的修改吗？",
        "Override.Edit.EmptyName.Title" to "名称为空",
        "Override.Edit.PresetApplied" to "已更新预设分流模板",
        "Override.Edit.TitleEdit" to "编辑配置",
        "Override.Edit.TitleNew" to "新建配置",
        "Override.Editor.AddCustom" to "添加自定义",
        "Override.Editor.AddItem" to "新增条目",
        "Override.Editor.AddObject" to "新增对象",
        "Override.Editor.AddSubRuleGroup" to "新增子规则组",
        "Override.Editor.AdditionalParams" to "附加参数",
        "Override.Editor.ArrayItems" to "数组 %d 项",
        "Override.Editor.BasicConnection" to "基础连接",
        "Override.Editor.CancelDelete" to "取消删除",
        "Override.Editor.Clear" to "清空",
        "Override.Editor.ClearCurrentMode" to "清空当前模式",
        "Override.Editor.ClearDialog.Summary" to "清空后将移除当前模式里的所有%s。",
        "Override.Editor.ClearDialog.Title" to "清空%s",
        "Override.Editor.ClearMode" to "清空当前模式",
        "Override.Editor.ClearSubRules" to "清空子规则",
        "Override.Editor.Confirm" to "确定",
        "Override.Editor.ContentEmpty" to "内容不能为空",
        "Override.Editor.Copy" to "复制",
        "Override.Editor.CustomMatchResult" to "自定义匹配结果",
        "Override.Editor.CustomMember" to "自定义成员",
        "Override.Editor.CustomProxyGroupTarget" to "自定义策略组目标",
        "Override.Editor.CustomSubRuleTarget" to "自定义子规则目标",
        "Override.Editor.Delete" to "删除",
        "Override.Editor.DeleteLastItem" to "删除最后一项",
        "Override.Editor.DeleteSelected" to "删除已选条目",
        "Override.Editor.DeleteSelectedRules" to "删除已选规则",
        "Override.Editor.DragToSort" to "拖拽排序",
        "Override.Editor.Edit" to "编辑",
        "Override.Editor.EditItem" to "编辑条目",
        "Override.Editor.EditProxyGroup" to "编辑策略组",
        "Override.Editor.EditProxyNode" to "编辑代理节点",
        "Override.Editor.EditRule" to "编辑规则",
        "Override.Editor.EditSubRule" to "编辑子规则",
        "Override.Editor.EditSubRuleGroup" to "编辑子规则组",
        "Override.Editor.EmptyString" to "空字符串",
        "Override.Editor.EnterDeleteMode" to "进入删除模式",
        "Override.Editor.ExtraParamsHint" to "例如 src,no-resolve 之外的额外参数\\n逻辑规则请直接填写完整 payload，例如 ((DOMAIN,google.com),(NETWORK,udp))。",
        "Override.Editor.HealthCheckAndFilter" to "健康检查与过滤",
        "Override.Editor.JsonBlockSubtitle" to "使用 JSON 格式编辑该配置块",
        "Override.Editor.KeyName" to "键名",
        "Override.Editor.List" to "列表",
        "Override.Editor.LogicalRuleHint" to "逻辑规则可直接填写完整 payload",
        "Override.Editor.MatchResult" to "匹配结果",
        "Override.Editor.MemberSource" to "成员来源",
        "Override.Editor.Mode.Title" to "修饰符模式",
        "Override.Editor.MoveDown" to "下移",
        "Override.Editor.MoveUp" to "上移",
        "Override.Editor.NetworkAndRoute" to "网络与路由",
        "Override.Editor.New" to "新增",
        "Override.Editor.NewProvider" to "新增 Provider",
        "Override.Editor.NewProxyGroup" to "新增策略组",
        "Override.Editor.NewProxyNode" to "新增代理节点",
        "Override.Editor.NewRule" to "新增规则",
        "Override.Editor.NewSubRuleGroup" to "新增子规则组",
        "Override.Editor.NoRules" to "暂无规则",
        "Override.Editor.ObjectFallbackTitle" to "对象 %d",
        "Override.Editor.ObjectFieldCount" to "%d 个字段",
        "Override.Editor.ObjectFieldHint" to "键值内容支持简单值和 JSON 结构。",
        "Override.Editor.ObjectFields" to "对象 %d 个字段",
        "Override.Editor.ObjectJsonPlaceholder" to "{ \"name\": \"proxy\", \"type\": \"ss\" }",
        "Override.Editor.ObjectListHint" to "结构化编辑对象列表，字段值支持字符串、数字、布尔和 JSON 片段。",
        "Override.Editor.OneItemPerLine" to "每行一个条目",
        "Override.Editor.OtherExtraParams" to "其他附加参数，多个值用逗号分隔",
        "Override.Editor.Payload" to "匹配内容",
        "Override.Editor.PayloadEmpty" to "匹配内容不能为空",
        "Override.Editor.PortEmptyHint" to "留空表示不覆写端口",
        "Override.Editor.ProviderMapHint" to "结构化编辑 Provider 字典，同名键会覆盖旧值。",
        "Override.Editor.ProxyGroup" to "策略组",
        "Override.Editor.ProxyGroupTarget" to "策略组目标",
        "Override.Editor.ProxyNode" to "代理节点",
        "Override.Editor.RuleBody" to "规则主体",
        "Override.Editor.RuleEdit" to "规则编辑",
        "Override.Editor.RulePlaceholder" to "DOMAIN-SUFFIX,example.com,DIRECT",
        "Override.Editor.RuleProviderInputHint" to "输入框是自定义内容；留空时使用下面选中的规则提供者",
        "Override.Editor.RuleType" to "类型",
        "Override.Editor.RuleTypeEmpty" to "规则类型不能为空",
        "Override.Editor.Rules" to "规则",
        "Override.Editor.RulesConfiguredInline" to "已配置 %d 条规则",
        "Override.Editor.SaveProxyGroup" to "保存策略组",
        "Override.Editor.SaveProxyNode" to "保存代理节点",
        "Override.Editor.SaveRule" to "保存规则",
        "Override.Editor.SelectMatchResult" to "选择匹配结果",
        "Override.Editor.SelectProxyGroupMember" to "选择策略组成员",
        "Override.Editor.SelectProxyGroupTarget" to "选择策略组目标",
        "Override.Editor.SelectRuleProvider" to "选择规则提供者",
        "Override.Editor.SelectSubRuleTarget" to "选择子规则目标",
        "Override.Editor.SubRuleGroupHint" to "每个子规则组包含一个名称和一组规则。",
        "Override.Editor.SubRuleName" to "子规则名称",
        "Override.Editor.SubRuleTarget" to "子规则目标",
        "Override.Editor.TargetEmpty" to "目标不能为空",
        "Override.Editor.TypeEmpty" to "类型不能为空",
        "Override.Editor.Unnamed" to "未命名%s",
        "Override.Editor.UnnamedProvider" to "未命名 Provider",
        "Override.Editor.UnnamedProxyGroup" to "未命名策略组",
        "Override.Editor.UnnamedProxyNode" to "未命名代理节点",
        "Override.Editor.UnnamedRule" to "未命名规则",
        "Override.Editor.UnnamedSubRuleGroup" to "未命名子规则组",
        "Override.Empty.Hint" to "点击下方按钮创建新配置，或导入 JSON",
        "Override.Empty.Title" to "暂无覆写配置",
        "Override.Export.Failed" to "导出失败：%s",
        "Override.Export.Success" to "已导出配置：%s",
        "Override.Form.AdvancedJson" to "%s · 高级 JSON",
        "Override.Form.AllowPrivateNetwork" to "允许私有网络",
        "Override.Form.AllowedIPs" to "允许 IP 段",
        "Override.Form.ApiSecret" to "API 访问密钥",
        "Override.Form.AutoDetectInterface" to "自动识别网卡",
        "Override.Form.AutoRedirect" to "自动重定向",
        "Override.Form.AutoRoute" to "自动路由",
        "Override.Form.AutoUpdateGeo" to "自动更新 GEO",
        "Override.Form.BasicPolicy" to "基础策略",
        "Override.Form.BindAddress" to "绑定地址",
        "Override.Form.CacheLimit" to "缓存上限",
        "Override.Form.ConfigPersistence" to "配置持久化",
        "Override.Form.ConnectionNetwork" to "连接与网络",
        "Override.Form.ControllerCors" to "控制器 CORS",
        "Override.Form.DirectFollowPolicy" to "Direct 遵循 Policy",
        "Override.Form.DisableIcmpForward" to "禁用 ICMP 转发",
        "Override.Form.DisallowedIPs" to "禁止 IP 段",
        "Override.Form.DnsBasicParams" to "DNS 基础参数",
        "Override.Form.DnsBasicSwitch" to "基础开关",
        "Override.Form.DnsFakeIpRange" to "FakeIP 地址段",
        "Override.Form.DnsHijack" to "DNS 劫持",
        "Override.Form.DnsPolicyMode" to "策略模式",
        "Override.Form.DnsUpstream" to "上游服务器",
        "Override.Form.DnsUpstreamServers" to "上游服务器",
        "Override.Form.EnableGso" to "启用 GSO",
        "Override.Form.EndpointIndependentNat" to "独立于端点 NAT",
        "Override.Form.ExcludePackage" to "排除应用",
        "Override.Form.ExternalControl" to "外部控制",
        "Override.Form.ExternalController" to "外部控制器",
        "Override.Form.ExternalControllerHttps" to "HTTPS 控制器",
        "Override.Form.ExternalDoH" to "外部 DoH 服务",
        "Override.Form.FakeIpIpv6Range" to "Fake-IP IPv6 网段",
        "Override.Form.FakeIpMode" to "Fake-IP 模式",
        "Override.Form.FakeIpParams" to "Fake-IP 参数",
        "Override.Form.FallbackFilter" to "Fallback 过滤",
        "Override.Form.FallbackParams" to "Fallback 参数",
        "Override.Form.FallbackSwitch" to "Fallback 开关",
        "Override.Form.FilterList" to "过滤列表",
        "Override.Form.GeoResources" to "GEO 资源开关",
        "Override.Form.GeoUpdateInterval" to "GEO 更新间隔",
        "Override.Form.GeodataMode" to "Geodata 模式",
        "Override.Form.GeoipUrl" to "GeoIP 地址",
        "Override.Form.GeositeMatcher" to "Geosite 匹配器",
        "Override.Form.GeositeUrl" to "GeoSite 地址",
        "Override.Form.GlobalClientFingerprint" to "全局客户端指纹",
        "Override.Form.Hours" to "小时",
        "Override.Form.HttpPorts" to "HTTP 端口",
        "Override.Form.IncludePackage" to "包含应用",
        "Override.Form.Ipv6Timeout" to "IPv6 超时",
        "Override.Form.ItemsConfigured" to "已配置 %d 项",
        "Override.Form.LanAccess" to "局域网访问",
        "Override.Form.LanAddress" to "局域网地址",
        "Override.Form.MmdbUrl" to "MMDB 地址",
        "Override.Form.NameserverPolicySection" to "策略映射",
        "Override.Form.NetworkPerfParams" to "网络性能参数",
        "Override.Form.NetworkPerfSwitch" to "网络性能开关",
        "Override.Form.NotModify" to "不修改",
        "Override.Form.OpenAdvancedEdit" to "打开高级编辑",
        "Override.Form.OpenAdvancedEditSummary" to "直接编辑原始对象，用于补充结构化表单未覆盖的字段",
        "Override.Form.OutboundInterface" to "出站接口",
        "Override.Form.ProcessMode" to "进程匹配模式",
        "Override.Form.ProxyGroups" to "策略组",
        "Override.Form.ProxyGroupsHint" to "结构化策略组",
        "Override.Form.ProxyNodes" to "代理节点",
        "Override.Form.ProxyNodesHint" to "结构化代理条目",
        "Override.Form.ProxyPorts" to "代理端口",
        "Override.Form.ProxyProviders" to "代理提供者",
        "Override.Form.ProxyProvidersAdvanced" to "需要协议细节、校验或额外字段时再进入高级 JSON",
        "Override.Form.ProxyProvidersHint" to "结构化 Provider",
        "Override.Form.ProxyServerNameserverPolicy" to "Proxy Server Nameserver Policy",
        "Override.Form.QuicPorts" to "QUIC 端口",
        "Override.Form.RouteAddress" to "路由网段",
        "Override.Form.RouteExcludeAddress" to "排除路由网段",
        "Override.Form.RoutingMark" to "路由标记",
        "Override.Form.RuleChain" to "规则链",
        "Override.Form.RuleChainNotSet" to "未设置规则链",
        "Override.Form.RuleProviders" to "规则提供者",
        "Override.Form.RuleProvidersAdvanced" to "需要复杂 Provider 字段时再进入高级 JSON",
        "Override.Form.RuleProvidersHint" to "结构化 Provider",
        "Override.Form.RunAndLog" to "运行与日志",
        "Override.Form.RunAndLogExtra" to "运行与日志补充",
        "Override.Form.SaveFakeIpMapping" to "保存 Fake-IP 映射",
        "Override.Form.SaveGroupSelection" to "保存策略组选择",
        "Override.Form.Seconds" to "秒",
        "Override.Form.SkipAndForce" to "跳过与强制",
        "Override.Form.SkipAuthIPs" to "跳过鉴权网段",
        "Override.Form.SkipDstAddress" to "跳过目标地址",
        "Override.Form.SkipSrcAddress" to "跳过来源地址",
        "Override.Form.SnifferForceDomain" to "强制域名",
        "Override.Form.SnifferOverride" to "覆写目标",
        "Override.Form.SnifferParsePureIp" to "解析纯 IP",
        "Override.Form.SnifferPorts" to "端口",
        "Override.Form.SnifferSkipDomain" to "跳过域名",
        "Override.Form.SnifferSwitch" to "开关",
        "Override.Form.Stack" to "协议栈",
        "Override.Form.StrictRoute" to "严格路由",
        "Override.Form.StructuredEdit" to "%s · 结构化编辑",
        "Override.Form.SubRules" to "子规则",
        "Override.Form.SubRulesAdvanced" to "复杂子规则结构统一收在高级 JSON 中",
        "Override.Form.SubRulesHint" to "结构化规则组",
        "Override.Form.TcpConcurrent" to "TCP 并发",
        "Override.Form.TlsPorts" to "TLS 端口",
        "Override.Form.TunBasicSwitch" to "基础开关",
        "Override.Form.TunRouteAndApps" to "路由与应用",
        "Override.Form.UnifiedDelay" to "统一延迟",
        "Override.Form.UserAuth" to "用户验证",
        "Override.General.AllowLan" to "允许局域网",
        "Override.General.HttpPort" to "HTTP 端口",
        "Override.General.Ipv6" to "IPv6",
        "Override.General.LogLevel" to "日志等级",
        "Override.General.MixedPort" to "Mixed 端口",
        "Override.General.ProxyMode" to "代理模式",
        "Override.General.RedirectPort" to "Redirect 端口",
        "Override.General.SocksPort" to "SOCKS 端口",
        "Override.General.TproxyPort" to "TProxy 端口",
        "Override.Import.Failed" to "导入失败: %s",
        "Override.Import.FileError" to "读取文件失败: %s",
        "Override.Import.ReadError" to "无法读取导入文件",
        "Override.Import.Success" to "已从 %s 导入 %d 个配置",
        "Override.Import.SuccessDefault" to "已导入 %d 个配置",
        "Override.Label.CacheAlgorithm" to "缓存算法",
        "Override.Label.Enable" to "启用",
        "Override.Label.FakeIpRange" to "FakeIP 地址段",
        "Override.Label.ForceDnsMapping" to "强制 DNS 映射",
        "Override.Label.ForceDomain" to "强制嗅探域名",
        "Override.Label.HttpOverride" to "HTTP 覆写",
        "Override.Label.KeepAliveIdle" to "Keep Alive 空闲阈值",
        "Override.Label.KeepAliveInterval" to "Keep Alive 间隔",
        "Override.Label.OverrideDestination" to "覆写目标地址",
        "Override.Label.ParsePureIp" to "解析纯 IP",
        "Override.Label.QuicOverride" to "QUIC 覆写",
        "Override.Label.RespectRules" to "遵循路由规则",
        "Override.Label.RulesReplace" to "覆盖规则",
        "Override.Label.SkipDomain" to "跳过嗅探域名",
        "Override.Label.TlsOverride" to "TLS 覆写",
        "Override.Label.UseSystemHosts" to "使用系统 Hosts",
        "Override.Modifier.End" to "后置追加",
        "Override.Modifier.Force" to "强制覆盖",
        "Override.Modifier.ItemsCount" to "%d 项",
        "Override.Modifier.Merge" to "合并",
        "Override.Modifier.NoChanges" to "暂无改动",
        "Override.Modifier.NotModified" to "未修改",
        "Override.Modifier.Replace" to "覆盖",
        "Override.Modifier.Start" to "前置追加",
        "Override.ProxyGroup.Field.DisableUdp" to "禁用 UDP",
        "Override.ProxyGroup.Field.ExcludeFilter" to "排除过滤器",
        "Override.ProxyGroup.Field.ExcludeType" to "排除类型",
        "Override.ProxyGroup.Field.ExpectedStatus" to "期望状态",
        "Override.ProxyGroup.Field.Filter" to "过滤器",
        "Override.ProxyGroup.Field.Hidden" to "隐藏",
        "Override.ProxyGroup.Field.Icon" to "图标",
        "Override.ProxyGroup.Field.IncludeAll" to "包含全部",
        "Override.ProxyGroup.Field.IncludeAllProviders" to "包含全部 Provider",
        "Override.ProxyGroup.Field.IncludeAllProxies" to "包含全部代理",
        "Override.ProxyGroup.Field.InterfaceName" to "接口名称",
        "Override.ProxyGroup.Field.Interval" to "间隔",
        "Override.ProxyGroup.Field.Lazy" to "懒加载",
        "Override.ProxyGroup.Field.MaxFailedTimes" to "最大失败次数",
        "Override.ProxyGroup.Field.Proxies" to "成员",
        "Override.ProxyGroup.Field.RoutingMark" to "路由标记",
        "Override.ProxyGroup.Field.Timeout" to "超时",
        "Override.ProxyGroup.Field.Url" to "URL",
        "Override.ProxyGroup.Field.Use" to "使用 Provider",
        "Override.ProxyGroup.Field.UseHint" to "每行一个 Provider 名称",
        "Override.Rule.EmptyWarning" to "规则 #%d 为空",
        "Override.Rule.InvalidFormatWarning" to "规则 #%d 格式可能不正确: %s",
        "Override.Rule.MissingTargetWarning" to "规则 #%d 缺少策略组目标: %s",
        "Override.Save.ApplyFailed" to "覆写已保存，但重新应用到当前配置失败",
        "Override.Save.Failed" to "保存覆写配置失败",
        "Override.Save.ImportDefaultName" to "导入的覆写配置",
        "Override.Save.ImportEmpty" to "导入内容不能为空",
        "Override.Save.PresetNotModifiable" to "系统预设不可修改",
        "Override.Section.Dns.Summary" to "基础开关、Fake-IP、上游与策略",
        "Override.Section.Dns.Title" to "DNS",
        "Override.Section.General.Summary" to "运行模式、控制器、持久化与 GEO",
        "Override.Section.General.Title" to "全局配置",
        "Override.Section.Inbound.Summary" to "端口、鉴权、局域网访问",
        "Override.Section.Inbound.Title" to "入站",
        "Override.Section.Proxies.Summary" to "代理节点与协议对象",
        "Override.Section.Proxies.Title" to "出站代理",
        "Override.Section.ProxyGroups.Summary" to "Proxy Groups 前置、覆盖、后置",
        "Override.Section.ProxyGroups.Title" to "代理组",
        "Override.Section.ProxyProviders.Summary" to "Proxy Providers 合并与覆盖",
        "Override.Section.ProxyProviders.Title" to "代理集合",
        "Override.Section.RuleProviders.Summary" to "Rule Providers 合并与覆盖",
        "Override.Section.RuleProviders.Title" to "规则集合",
        "Override.Section.Rules.Summary" to "规则链与匹配顺序",
        "Override.Section.Rules.Title" to "路由规则",
        "Override.Section.Sniffer.Summary" to "策略开关、协议端口、跳过规则",
        "Override.Section.Sniffer.Title" to "域名嗅探",
        "Override.Section.SubRules.Summary" to "Sub Rules 分组与合并",
        "Override.Section.SubRules.Title" to "子规则",
        "Override.Section.Tun.Summary" to "入站 Tun、路由与应用范围",
        "Override.Section.Tun.Title" to "Tun",
        "Override.Status.InUse" to "使用中",
        "Override.Status.NotInUse" to "未使用",
        "Override.Structured.Proxies.EmptyHint" to "暂无代理节点",
        "Override.Structured.Proxies.ItemLabel" to "代理节点",
        "Override.Structured.Proxies.Title" to "代理节点",
        "Override.Structured.ProxyGroups.EmptyHint" to "暂无策略组",
        "Override.Structured.ProxyGroups.ItemLabel" to "策略组",
        "Override.Structured.ProxyGroups.Title" to "策略组",
        "Override.Structured.ProxyProviders.ItemLabel" to "Provider",
        "Override.Structured.ProxyProviders.Title" to "代理提供者",
        "Override.Structured.RuleProviders.ItemLabel" to "Provider",
        "Override.Structured.RuleProviders.Title" to "规则提供者",
        "Override.Structured.SubRules.ItemLabel" to "子规则组",
        "Override.Structured.SubRules.Title" to "子规则",
        "Override.Title" to "覆写配置",
        "ProfilesPage.Action.AddProfile" to "添加配置",
        "ProfilesPage.Action.UpdateAll" to "一键更新所有",
        "ProfilesPage.Button.Cancel" to "取消",
        "ProfilesPage.Button.Confirm" to "确定",
        "ProfilesPage.DeleteDialog.Confirm" to "删除",
        "ProfilesPage.DeleteDialog.Message" to "确定要删除「%s」吗？",
        "ProfilesPage.DeleteDialog.Title" to "删除配置",
        "ProfilesPage.EditDialog.Title" to "编辑配置名称",
        "ProfilesPage.Empty.Hint" to "点击右上角添加配置",
        "ProfilesPage.Empty.NoProfiles" to "暂无配置文件",
        "ProfilesPage.Input.NewProfile" to "新配置",
        "ProfilesPage.Input.ProfileName" to "配置名称",
        "ProfilesPage.Input.SelectFile" to "点击选择文件",
        "ProfilesPage.Input.SubscriptionUrl" to "订阅链接 (HTTP/HTTPS)",
        "ProfilesPage.Input.SubscriptionUserAgent" to "User-Agent（留空使用全局设置）",
        "ProfilesPage.Kokoro.Account" to "账号",
        "ProfilesPage.Kokoro.AvatarDescription" to "%s 的 osu! 头像",
        "ProfilesPage.Kokoro.BandwidthLimit" to "流量上限",
        "ProfilesPage.Kokoro.CheckFailed" to "无法检查账号",
        "ProfilesPage.Kokoro.CheckFailedDetail" to "请检查网络连接，然后重试。",
        "ProfilesPage.Kokoro.Checking" to "正在检查登录状态…",
        "ProfilesPage.Kokoro.DecreaseUpdateHours" to "缩短配置更新间隔",
        "ProfilesPage.Kokoro.DefaultProfileName" to "Kokoro",
        "ProfilesPage.Kokoro.Direct" to "直连",
        "ProfilesPage.Kokoro.Disabled" to "停用",
        "ProfilesPage.Kokoro.Enabled" to "启用",
        "ProfilesPage.Kokoro.Expires" to "到期时间",
        "ProfilesPage.Kokoro.Fallback" to "未匹配流量",
        "ProfilesPage.Kokoro.FinalRoute" to "最终路由",
        "ProfilesPage.Kokoro.IncreaseUpdateHours" to "延长配置更新间隔",
        "ProfilesPage.Kokoro.InvalidUpdateHours" to "更新周期必须是正整数小时",
        "ProfilesPage.Kokoro.Isp" to "网络服务商",
        "ProfilesPage.Kokoro.IspAuto" to "自动判断",
        "ProfilesPage.Kokoro.IspCm" to "中国移动",
        "ProfilesPage.Kokoro.IspCt" to "中国电信",
        "ProfilesPage.Kokoro.IspCu" to "中国联通",
        "ProfilesPage.Kokoro.IspOther" to "其他",
        "ProfilesPage.Kokoro.KeepFallback" to "保留配置原本的 fallback",
        "ProfilesPage.Kokoro.LoggedIn" to "已登录",
        "ProfilesPage.Kokoro.LoggedInAs" to "已登录为 %s",
        "ProfilesPage.Kokoro.LoggedOut" to "尚未登录",
        "ProfilesPage.Kokoro.Login" to "使用 osu! 登录",
        "ProfilesPage.Kokoro.LoginFailed" to "登录失败或已取消",
        "ProfilesPage.Kokoro.LoginHint" to "使用 osu! 登录以加载 Proxy Subscription。",
        "ProfilesPage.Kokoro.LoginRequired" to "请先登录并选择有效的订阅",
        "ProfilesPage.Kokoro.Logout" to "退出登录",
        "ProfilesPage.Kokoro.Mirror" to "镜像站",
        "ProfilesPage.Kokoro.Mode" to "连接模式",
        "ProfilesPage.Kokoro.NoSubscription" to "此账号没有有效的 Proxy Subscription。",
        "ProfilesPage.Kokoro.Origin" to "原始站",
        "ProfilesPage.Kokoro.Plan" to "方案",
        "ProfilesPage.Kokoro.ProfileUpdate" to "配置更新间隔",
        "ProfilesPage.Kokoro.Protocol" to "协议",
        "ProfilesPage.Kokoro.Proxy" to "代理",
        "ProfilesPage.Kokoro.Relay" to "中继",
        "ProfilesPage.Kokoro.Retry" to "重试",
        "ProfilesPage.Kokoro.Routing" to "路由",
        "ProfilesPage.Kokoro.RuleProviderAutoUpdate" to "更新规则提供者",
        "ProfilesPage.Kokoro.RuleProviderAutoUpdateSummary" to "自动更新远程规则集",
        "ProfilesPage.Kokoro.RuleSource" to "规则来源",
        "ProfilesPage.Kokoro.RuleUpdate" to "远程规则更新",
        "ProfilesPage.Kokoro.SecureTokenSession" to "Token 已由 Android Keystore 加密保护。",
        "ProfilesPage.Kokoro.SignInFromSettings" to "请先前往“设置 → Kokoro 设置”登录，再添加 Kokoro 订阅。",
        "ProfilesPage.Kokoro.Subscription" to "订阅",
        "ProfilesPage.Kokoro.SubscriptionAutoUpdate" to "自动更新配置",
        "ProfilesPage.Kokoro.SubscriptionAutoUpdateSummary" to "按照指定周期重新获取配置",
        "ProfilesPage.Kokoro.SubscriptionNumber" to "订阅 %s",
        "ProfilesPage.Kokoro.Traffic" to "流量",
        "ProfilesPage.Kokoro.TrafficUsed" to "已用流量",
        "ProfilesPage.Kokoro.Unlimited" to "无限",
        "ProfilesPage.Kokoro.UpdateCustom" to "自定义",
        "ProfilesPage.Kokoro.UpdateHours" to "小时（正整数）",
        "ProfilesPage.Kokoro.UpdateHoursRange" to "更新间隔（%s–%s 小时）",
        "ProfilesPage.Kokoro.UpdateHoursValue" to "%s 小时",
        "ProfilesPage.Kokoro.UpdateOff" to "停用",
        "ProfilesPage.Kokoro.UpdateOn" to "每小时",
        "ProfilesPage.Kokoro.Updates" to "更新",
        "ProfilesPage.Kokoro.VmessRelayOnly" to "VMess 固定使用中继模式",
        "ProfilesPage.LinkSettings.AddLink" to "添加链接",
        "ProfilesPage.LinkSettings.Close" to "关闭",
        "ProfilesPage.LinkSettings.DefaultLink" to "默认链接",
        "ProfilesPage.LinkSettings.DefaultLinkSummary" to "点击左上角快捷按钮时打开的链接",
        "ProfilesPage.LinkSettings.EditLink" to "编辑链接",
        "ProfilesPage.LinkSettings.Name" to "名称",
        "ProfilesPage.LinkSettings.OpenMode" to "打开方式",
        "ProfilesPage.LinkSettings.OpenModeExternal" to "外部浏览器",
        "ProfilesPage.LinkSettings.OpenModeInApp" to "App 内打开",
        "ProfilesPage.LinkSettings.Title" to "链接设置",
        "ProfilesPage.LinkSettings.Url" to "链接",
        "ProfilesPage.LinkSettings.Validation.EnterName" to "请输入名称",
        "ProfilesPage.LinkSettings.Validation.EnterUrl" to "请输入链接",
        "ProfilesPage.LinkSettings.Validation.InvalidUrl" to "请输入有效的链接",
        "ProfilesPage.Message.UnknownFile" to "未知文件",
        "ProfilesPage.Misc.Complete" to "完成",
        "ProfilesPage.Misc.Error" to "错误",
        "ProfilesPage.Progress.Downloading" to "下载中...",
        "ProfilesPage.QrScanner.NeedCamera" to "需要相机权限才能扫码",
        "ProfilesPage.QrScanner.NeedPermission" to "需要相机权限",
        "ProfilesPage.QrScanner.RecognizeError" to "识别失败：%s",
        "ProfilesPage.QrScanner.RecognizeFailed" to "未能识别到二维码",
        "ProfilesPage.QrScanner.RecognizeSuccess" to "识别成功",
        "ProfilesPage.QrScanner.ScanSuccess" to "扫描成功",
        "ProfilesPage.QrScanner.SelectFromAlbum" to "从相册选择二维码图片",
        "ProfilesPage.SettingsDialog.ChangeLink" to "更改订阅链接",
        "ProfilesPage.SettingsDialog.ConfigMissing" to "配置不存在：%s",
        "ProfilesPage.SettingsDialog.EditProfile" to "编辑配置",
        "ProfilesPage.SettingsDialog.EditSettings" to "编辑设置",
        "ProfilesPage.SettingsDialog.NoDescription" to "未设置说明",
        "ProfilesPage.SettingsDialog.OpenConfig" to "打开配置",
        "ProfilesPage.SettingsDialog.SaveFailed" to "保存配置失败",
        "ProfilesPage.SettingsDialog.SystemPreset" to "启用覆写配置",
        "ProfilesPage.SettingsDialog.SystemPresetSummary" to "启用内置覆写配置",
        "ProfilesPage.SettingsDialog.Title" to "订阅设置",
        "ProfilesPage.ShareDialog.ImportedConfigMissing" to "导入配置不存在：%s",
        "ProfilesPage.ShareDialog.NoLink" to "该配置没有订阅链接",
        "ProfilesPage.ShareDialog.ShareFile" to "分享配置文件",
        "ProfilesPage.ShareDialog.ShareLink" to "分享订阅链接",
        "ProfilesPage.ShareDialog.Title" to "分享配置",
        "ProfilesPage.Sheet.AddTitle" to "添加配置文件",
        "ProfilesPage.Sheet.EditTitle" to "编辑配置文件",
        "ProfilesPage.Title" to "配置",
        "ProfilesPage.Type.Kokoro" to "Kokoro 订阅",
        "ProfilesPage.Type.LocalFile" to "本地文件",
        "ProfilesPage.Type.QrScan" to "扫码添加",
        "ProfilesPage.Type.Subscription" to "订阅链接",
        "ProfilesPage.Type.Title" to "配置类型",
        "ProfilesPage.Validation.EnterUrl" to "请输入链接",
        "ProfilesPage.Validation.SelectFile" to "请选择文件",
        "ProfilesPage.Validation.YamlOnly" to "仅支持 .yaml 或 .yml 格式的配置文件",
        "ProfilesVM.Error.ProfileNotExist" to "配置不存在",
        "ProfilesVM.Message.AddFailed" to "添加配置失败：%s",
        "ProfilesVM.Message.DeleteFailed" to "删除配置失败：%s",
        "ProfilesVM.Message.ImportFailed" to "导入配置失败：%s",
        "ProfilesVM.Message.ProfileAdded" to "配置已添加：%s",
        "ProfilesVM.Message.ProfileAddedAndActivated" to "配置已添加并启用：%s",
        "ProfilesVM.Message.ProfileDeleted" to "配置已删除",
        "ProfilesVM.Message.ProfileImported" to "配置已导入：%s",
        "ProfilesVM.Message.ProfileUpdated" to "配置已更新：%s",
        "ProfilesVM.Message.ToggleFailed" to "切换状态失败：%s",
        "ProfilesVM.Message.UpdateFailed" to "更新配置失败：%s",
        "ProfilesVM.Progress.ImportComplete" to "导入完成",
        "ProfilesVM.Progress.ImportPreparing" to "准备导入文件...",
        "ProfilesVM.Progress.Preparing" to "准备下载...",
        "ProfilesVM.Progress.Verifying" to "正在验证配置...",
        "Providers.Action.Operation" to "操作",
        "Providers.Action.Update" to "更新",
        "Providers.Action.UpdateAll" to "更新全部",
        "Providers.Action.Upload" to "上传",
        "Providers.Empty.NoProviders" to "暂无外部资源",
        "Providers.Empty.NoProvidersHint" to "当前配置未包含外部资源",
        "Providers.Empty.NotRunning" to "代理未启动",
        "Providers.Empty.NotRunningHint" to "请先启动代理服务以查看外部资源",
        "Providers.InfoSummary" to "GeoX 和内核运行时公共文件位于：%s。规则和代理提供者只会写入当前配置的私有目录。",
        "Providers.InfoTitle" to "内核目录",
        "Providers.Message.AllUpdated" to "全部更新完成",
        "Providers.Message.FetchFailed" to "获取外部资源失败: %s",
        "Providers.Message.UpdateFailed" to "更新失败: %s",
        "Providers.Message.UpdateSuccess" to "%s 更新成功",
        "Providers.Message.UploadFailed" to "上传失败: %s",
        "Providers.Message.UploadSuccess" to "%s 上传成功",
        "Providers.ProviderPath" to "资源路径：%s",
        "Providers.Title" to "外部资源",
        "Providers.Type.ProxyProviders" to "代理提供者 (%d)",
        "Providers.Type.RuleProviders" to "规则提供者 (%d)",
        "Providers.VehicleType.Compatible" to "兼容",
        "Providers.VehicleType.File" to "文件",
        "Providers.VehicleType.Http" to "HTTP",
        "Providers.VehicleType.Inline" to "内联",
        "Proxy.Action.Sort" to "排序",
        "Proxy.Action.Test" to "测试",
        "Proxy.DisplayMode.DoubleDetailed" to "双列详细",
        "Proxy.DisplayMode.DoubleSimple" to "双列简洁",
        "Proxy.DisplayMode.SingleDetailed" to "单列详细",
        "Proxy.DisplayMode.SingleSimple" to "单列简洁",
        "Proxy.Empty.Hint" to "请在配置页面加载配置文件",
        "Proxy.Empty.NoNodes" to "暂无节点",
        "Proxy.Mode.Direct" to "直连",
        "Proxy.Mode.Global" to "全局",
        "Proxy.Mode.Rule" to "规则",
        "Proxy.Mode.SwitchFailed" to "切换模式失败：%s",
        "Proxy.Mode.Switched" to "已切换到：%s 模式",
        "Proxy.Mode.Unknown" to "未知",
        "Proxy.Node.Count" to "%d 节点",
        "Proxy.Node.Timeout" to "超时",
        "Proxy.Selection.Error" to "切换失败：%s",
        "Proxy.Selection.Failed" to "切换失败",
        "Proxy.Selection.Switched" to "已切换到：%s",
        "Proxy.SortMode.ByLatency" to "按延迟排序",
        "Proxy.SortMode.ByName" to "按名称排序",
        "Proxy.SortMode.Default" to "默认顺序",
        "Proxy.Testing.All" to "正在测试所有节点组...",
        "Proxy.Testing.Failed" to "测试失败：%s",
        "Proxy.Testing.Group" to "正在测试节点组：%s",
        "Proxy.Testing.InProgress" to "正在测试节点",
        "Proxy.Testing.RequestSent" to "测试请求已发送",
        "Proxy.Title" to "代理",
        "Proxy.Type.Compatible" to "兼容",
        "Proxy.Type.Direct" to "直连",
        "Proxy.Type.Fallback" to "回退",
        "Proxy.Type.LoadBalance" to "负载均衡",
        "Proxy.Type.Pass" to "透传",
        "Proxy.Type.Reject" to "拒绝",
        "Proxy.Type.RejectDrop" to "丢弃",
        "Proxy.Type.Relay" to "中继",
        "Proxy.Type.Selector" to "选择器",
        "Proxy.Type.Smart" to "智能",
        "Proxy.Type.Unknown" to "未知",
        "Proxy.Type.UrlTest" to "UrlTest",
        "Service.AutoRestart.ChannelDescription" to "用于自动重启代理服务",
        "Service.AutoRestart.ChannelName" to "自动重启服务",
        "Service.AutoRestart.Checking" to "正在检查自动启动...",
        "Service.Notification.Running" to "运行中",
        "Service.Notification.SpeedFormat" to "下行 %s  上行 %s",
        "Service.Notification.TodayTrafficFormat" to "今日流量 %s",
        "Service.Notification.TrafficFormat" to "总计：%s",
        "Service.Notification.UnknownProfile" to "未知配置",
        "Service.Tile.ClickToOpen" to "点击打开应用",
        "Service.Tile.ClickToStartProxy" to "启动代理",
        "Service.Tile.ClickToStopProxy" to "停止代理",
        "Service.Tile.Connecting" to "正在连接...",
        "Service.Tile.Disconnecting" to "正在断开...",
        "Settings.DataSettings.AppDataManagement" to "应用数据管理",
        "Settings.DataSettings.AppDataManagementSummary" to "清理部分缓存文件",
        "Settings.DataSettings.ExportBackup" to "导出备份",
        "Settings.DataSettings.ExportBackupSummary" to "将用户设置保存为 JSON 文件",
        "Settings.DataSettings.ImportBackup" to "导入备份",
        "Settings.DataSettings.ImportBackupSummary" to "从备份文件恢复用户设置",
        "Settings.Error.WebviewFailed" to "无法打开 WebView：%s",
        "Settings.Kokoro.CustomRules" to "自定义规则",
        "Settings.Kokoro.CustomRulesSummary" to "编辑应用于生成配置的 default 规则",
        "Settings.Kokoro.Summary" to "账号 · 自定义规则",
        "Settings.Kokoro.Title" to "Kokoro 设置",
        "Settings.More.About" to "关于",
        "Settings.More.AboutSummary" to "版本与许可",
        "Settings.More.Logs" to "日志",
        "Settings.More.LogsSummary" to "运行日志",
        "Settings.NetworkSettings.Lab" to "实验室",
        "Settings.NetworkSettings.LabSummary" to "节点测试",
        "Settings.NetworkSettings.MetaFeatures" to "Meta 功能",
        "Settings.NetworkSettings.MetaFeaturesSummary" to "Meta 扩展",
        "Settings.NetworkSettings.Network" to "网络",
        "Settings.NetworkSettings.NetworkSummary" to "DNS · 端口 · 入站",
        "Settings.NetworkSettings.Override" to "覆写",
        "Settings.NetworkSettings.OverrideSummary" to "规则覆写",
        "Settings.Section.DataSettings" to "数据设置",
        "Settings.Section.Kokoro" to "Kokoro",
        "Settings.Section.More" to "更多",
        "Settings.Section.NetworkSettings" to "网络设置",
        "Settings.Section.UiSettings" to "界面设置",
        "Settings.Title" to "设置",
        "Settings.UiSettings.App" to "应用",
        "Settings.UiSettings.AppSummary" to "外观 · 语言 · 主题",
        "TrafficStatistics.Action.Clear" to "清空统计",
        "TrafficStatistics.Action.ClearConfirmMessage" to "清空后将无法恢复所有流量统计数据。",
        "TrafficStatistics.Action.ClearSuccess" to "统计数据已清空",
        "TrafficStatistics.Chart.Daily" to "按天",
        "TrafficStatistics.Chart.Hourly" to "4 小时",
        "TrafficStatistics.Compare.LessThanYesterday" to "较昨日 %s",
        "TrafficStatistics.Compare.MoreThanYesterday" to "较昨日 +%s",
        "TrafficStatistics.Compare.SameAsYesterday" to "与昨日持平",
        "TrafficStatistics.Compare.WeekStats" to "近 7 天统计",
        "TrafficStatistics.Donut.Other" to "其他",
        "TrafficStatistics.EntrySummary" to "查看流量使用情况",
        "TrafficStatistics.Metric.Download" to "下行",
        "TrafficStatistics.Metric.Upload" to "上行",
        "TrafficStatistics.Metric.UsageLine" to "下行 %s  上行 %s",
        "TrafficStatistics.Section.EmptyApps" to "暂无应用流量统计",
        "TrafficStatistics.Section.TopApps" to "高流量应用",
        "TrafficStatistics.Section.Traffic" to "流量",
        "TrafficStatistics.Summary.TodayTraffic" to "今日流量",
        "TrafficStatistics.Summary.WeekTraffic" to "本周流量",
        "TrafficStatistics.TimeRange.Today" to "今日",
        "TrafficStatistics.TimeRange.Week" to "本周",
        "TrafficStatistics.Title" to "流量统计",
        "Util.Error.UnknownError" to "未知错误",
    )

    private val TEXT_ZH_TW = mapOf(
        "About.App.Description" to "基於 Mihomo 的 Material You Android 代理用戶端",
        "About.App.VersionFailed" to "Failed to load",
        "About.App.VersionLoading" to "Loading...",
        "About.Copyright" to "© 2026 KokoroBox 貢獻者",
        "About.License.AgplDescription" to "This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License.",
        "About.License.AgplName" to "GNU Affero General Public License v3.0",
        "About.License.CheckUpdate" to "檢查更新",
        "About.License.CheckUpdateSummary" to "手動檢查 GitHub Release 更新",
        "About.License.Libraries" to "引用庫",
        "About.License.LibrariesSummary" to "View all third-party libraries used in this app",
        "About.Section.License" to "許可證",
        "About.Section.ProjectLinks" to "專案連結",
        "About.Title" to "關於",
        "About.Update.Available" to "有可用更新",
        "About.Update.BrowserDownload" to "將透過瀏覽器下載最新 APK。",
        "About.Update.Checking" to "正在檢查 GitHub Releases…",
        "About.Update.ContinueInstall" to "繼續",
        "About.Update.Download" to "下載 APK",
        "About.Update.Downloading" to "正在下載更新…",
        "About.Update.InAppDownload" to "下載並更新",
        "About.Update.InAppDownloadSummary" to "APK 會先下載並驗證，再交由 Android 系統安裝程式處理。",
        "About.Update.InstallPermissionRequired" to "請允許 KokoroBox 安裝更新後再繼續。",
        "About.Update.Installed" to "更新安裝已完成。",
        "About.Update.InvalidResponse" to "GitHub 回傳的版本資訊不受支援，請稍後再試。",
        "About.Update.NetworkError" to "無法檢查更新，請確認網路連線後重試。",
        "About.Update.NoApk" to "相容的 APK 尚未就緒，請查看 Release 頁面或稍後再檢查。",
        "About.Update.NoBrowser" to "沒有可開啟此下載的應用程式。",
        "About.Update.NoRelease" to "目前尚無已發布的正式版本。",
        "About.Update.Ok" to "確定",
        "About.Update.OpenInstallSettings" to "開啟設定",
        "About.Update.OpenRelease" to "查看 Release",
        "About.Update.PreparingInstall" to "正在準備系統安裝程式…",
        "About.Update.RateLimited" to "GitHub 已限制請求次數，請稍候再檢查。",
        "About.Update.Retry" to "重試",
        "About.Update.UnknownVersion" to "無法比較此建構的版本，請手動查看 GitHub Releases。",
        "About.Update.UpToDate" to "目前已是此版本或更新的版本。",
        "About.Update.UpdateFailed" to "更新失敗",
        "About.Update.Verifying" to "正在驗證下載的 APK…",
        "About.Update.WaitingForInstallConfirmation" to "請在 Android 安裝對話框中繼續。",
        "AccessControl.AppList.Loading" to "載入中...",
        "AccessControl.AppList.Title" to "應用程式清單 (%d 已選擇)",
        "AccessControl.Button.Cancel" to "取消",
        "AccessControl.Button.Confirm" to "確定",
        "AccessControl.Search.Empty" to "沒有相符的應用程式",
        "AccessControl.Search.Placeholder" to "搜尋應用程式...",
        "AccessControl.Settings.BatchOperation" to "批次操作",
        "AccessControl.Settings.ChinaApps" to "中國應用程式",
        "AccessControl.Settings.DescendingOrder" to "倒序排列",
        "AccessControl.Settings.DeselectAll" to "全不選",
        "AccessControl.Settings.Export" to "匯出到剪貼簿",
        "AccessControl.Settings.ExportSuccess" to "已複製 %d 個套件名稱到剪貼簿",
        "AccessControl.Settings.Import" to "從剪貼簿匯入",
        "AccessControl.Settings.ImportExport" to "匯入/匯出",
        "AccessControl.Settings.ImportFailed" to "匯入失敗",
        "AccessControl.Settings.ImportSuccess" to "匯入成功：%d 個套件名稱",
        "AccessControl.Settings.Invert" to "反選",
        "AccessControl.Settings.OverseasApps" to "非中國應用程式",
        "AccessControl.Settings.RegionQuickSelect" to "地區快捷選擇",
        "AccessControl.Settings.RegionSelectResult" to "已按「%s」快捷選擇，共 %d 個",
        "AccessControl.Settings.SelectAction" to "選擇",
        "AccessControl.Settings.SelectAll" to "全選",
        "AccessControl.Settings.SelectedFirst" to "已選應用程式優先",
        "AccessControl.Settings.ShowSystemApps" to "顯示系統應用程式",
        "AccessControl.Settings.SortMode" to "排序方式",
        "AccessControl.Settings.SortModeCurrent" to "目前：%s",
        "AccessControl.Settings.Title" to "存取控制設定",
        "AccessControl.SortMode.InstallTime" to "安裝時間",
        "AccessControl.SortMode.Label" to "應用程式名稱",
        "AccessControl.SortMode.PackageName" to "套件名稱",
        "AccessControl.SortMode.UpdateTime" to "更新時間",
        "AccessControl.Title" to "存取控制",
        "AppDataManagement.GeoFiles.CacheItemSummary" to "%s · %s",
        "AppDataManagement.GeoFiles.DeleteComplete" to "已刪除 %d 個歷史 GeoX 檔案",
        "AppDataManagement.GeoFiles.DeleteConfirmMessage" to "確定刪除已選取的 %d 個歷史 GeoX 檔案嗎？此操作無法復原。",
        "AppDataManagement.GeoFiles.DeleteConfirmTitle" to "刪除 GeoX 檔案？",
        "AppDataManagement.GeoFiles.EmptyHistory" to "沒有可清理的歷史下載",
        "AppDataManagement.GeoFiles.EmptyHistorySummary" to "校驗失敗的 GeoX 檔案會被直接刪除，不會出現在這裡",
        "AppDataManagement.GeoFiles.HistorySummary" to "歷史下載快取：%d 個",
        "AppDataManagement.GeoFiles.HistoryTitle" to "管理 GeoX 檔案",
        "AppDataManagement.Logs.DeleteComplete" to "已刪除 %d 個日誌檔案",
        "AppDataManagement.Logs.DeleteConfirmMessage" to "確定刪除已選取的 %d 個日誌檔案嗎？此操作無法復原。",
        "AppDataManagement.Logs.DeleteConfirmTitle" to "刪除日誌檔案？",
        "AppDataManagement.Logs.EmptyLogContent" to "日誌為空",
        "AppDataManagement.Logs.EmptyLogContentSummary" to "該檔案沒有可顯示的日誌條目",
        "AppDataManagement.Logs.EmptyLogs" to "沒有可清理的日誌",
        "AppDataManagement.Logs.EmptyLogsSummary" to "在日誌頁面開始記錄後，日誌檔案會顯示在這裡",
        "AppDataManagement.Logs.LogItemSummary" to "%s · %s",
        "AppDataManagement.Logs.LogLineTitle" to "[%s] [%s]",
        "AppDataManagement.Logs.ManagementSummary" to "日誌檔案：%d 個",
        "AppDataManagement.Logs.ManagementTitle" to "管理日誌",
        "AppDataManagement.Logs.RecordingFileTitle" to "%s（記錄中）",
        "AppDataManagement.Logs.ViewerLimitHint" to "僅顯示最近的日誌",
        "AppDataManagement.Logs.ViewerLimitSummary" to "目前顯示最近 %d 條",
        "AppDataManagement.Logs.ViewerTitle" to "查看日誌：%s",
        "AppDataManagement.Section.GeoFiles" to "Geo 檔案",
        "AppDataManagement.Section.Logs" to "日誌",
        "AppDataManagement.Title" to "應用程式資料管理",
        "AppSettings.Backup.ExportFailed" to "匯出設定備份失敗",
        "AppSettings.Backup.ExportFailedDetail" to "匯出設定備份失敗：%s",
        "AppSettings.Backup.ExportSuccess" to "已匯出設定備份",
        "AppSettings.Backup.ExportSummary" to "將應用程式偏好、網路選項、設定檔連結和顯示設定儲存為 JSON 檔案",
        "AppSettings.Backup.ExportTitle" to "匯出設定備份",
        "AppSettings.Backup.ImportFailedDetail" to "匯入設定備份失敗：%s",
        "AppSettings.Backup.ImportReadFailed" to "讀取備份檔案失敗",
        "AppSettings.Backup.ImportSuccess" to "已匯入設定備份",
        "AppSettings.Backup.ImportSummary" to "從 KokoroBox 備份 JSON 檔案還原設定",
        "AppSettings.Backup.ImportTitle" to "匯入設定備份",
        "AppSettings.Behavior.AutoStartSummary" to "應用程式啟動和開機時自動啟動代理服務",
        "AppSettings.Behavior.AutoStartTitle" to "自動啟動",
        "AppSettings.Behavior.AutoUpdateOnStartSummary" to "啟動時自動更新目前訂閱設定檔",
        "AppSettings.Behavior.AutoUpdateOnStartTitle" to "啟動時更新設定檔",
        "AppSettings.Behavior.AutomaticUpdateCheckSummary" to "開啟應用程式時每日檢查一次 GitHub Release",
        "AppSettings.Behavior.AutomaticUpdateCheckTitle" to "自動檢查更新",
        "AppSettings.Behavior.UpdateChannelNightly" to "測試版（nightly）",
        "AppSettings.Behavior.UpdateChannelStable" to "正式版",
        "AppSettings.Behavior.UpdateChannelSummary" to "選擇正式版或開發中的 nightly 測試版",
        "AppSettings.Behavior.UpdateChannelTitle" to "更新通道",
        "AppSettings.Behavior.UpdateInstallMethodRoot" to "Root",
        "AppSettings.Behavior.UpdateInstallMethodShizuku" to "Shizuku",
        "AppSettings.Behavior.UpdateInstallMethodSummary" to "進階設定：建議使用系統安裝。Shizuku 和 Root 會略過 Android 安裝確認。",
        "AppSettings.Behavior.UpdateInstallMethodSystem" to "系統",
        "AppSettings.Behavior.UpdateInstallMethodTitle" to "更新安裝方式",
        "AppSettings.Button.Apply" to "套用",
        "AppSettings.Experimental.AcgHomeSummary" to "啟用新的 ACG 風格首頁佈局",
        "AppSettings.Experimental.AcgHomeTitle" to "ACG 實驗首頁",
        "AppSettings.Experimental.AcgSidebarExpandedSummary" to "進入 ACG 首頁時預設展開左側資訊欄",
        "AppSettings.Experimental.AcgSidebarExpandedTitle" to "ACG 側欄預設展開",
        "AppSettings.Experimental.HealthCheckConcurrencySummary" to "目前選項：%s",
        "AppSettings.Experimental.HealthCheckConcurrencyTitle" to "測速並發數",
        "AppSettings.Experimental.ResetWallpaperSuccess" to "已恢復預設桌布",
        "AppSettings.Experimental.ResetWallpaperSummary" to "恢復內建預設桌布，並重置裁剪位置與桌布取色",
        "AppSettings.Experimental.ResetWallpaperTitle" to "重置 ACG 桌布",
        "AppSettings.Experimental.WallpaperSummary" to "為 ACG 首頁選擇自訂桌布",
        "AppSettings.Experimental.WallpaperTitle" to "ACG 桌布",
        "AppSettings.Interface.AutoHideNavbarSummary" to "向下滑動時自動隱藏底欄，向上滑動時顯示",
        "AppSettings.Interface.AutoHideNavbarTitle" to "滑動隱藏底欄",
        "AppSettings.Interface.ColorThemeAcgWallpaperSummary" to "將使用目前 ACG 首頁桌布提取的主色生成主題；更換並套用桌布後會自動更新取色。",
        "AppSettings.Interface.ColorThemeCodeLabel" to "主題色程式碼（#RRGGBB）",
        "AppSettings.Interface.ColorThemeCustomSummary" to "目前主題色：%s",
        "AppSettings.Interface.ColorThemeDynamicSummary" to "目前跟隨系統配色",
        "AppSettings.Interface.ColorThemeModeAcgWallpaper" to "根據 ACG 桌布取色",
        "AppSettings.Interface.ColorThemeModeCustom" to "自訂主題色",
        "AppSettings.Interface.ColorThemeModeMonet" to "跟隨系統",
        "AppSettings.Interface.ColorThemeModeSummary" to "選擇跟隨系統或手動指定主題色",
        "AppSettings.Interface.ColorThemeModeTitle" to "取色來源",
        "AppSettings.Interface.ColorThemePickerTitle" to "選擇主題色",
        "AppSettings.Interface.ColorThemeTitle" to "主題配色",
        "AppSettings.Interface.HomeControlFabSummary" to "使用浮動按鈕啟動或停止代理，不再透過點選流量面板控制",
        "AppSettings.Interface.HomeControlFabTitle" to "首頁控制浮動按鈕",
        "AppSettings.Interface.LanguageChinese" to "簡體中文",
        "AppSettings.Interface.LanguageEnglish" to "English",
        "AppSettings.Interface.LanguageSummary" to "選擇應用程式語言",
        "AppSettings.Interface.LanguageSystem" to "跟隨系統",
        "AppSettings.Interface.LanguageTitle" to "應用程式語言",
        "AppSettings.Interface.LanguageTraditionalChinese" to "繁體中文",
        "AppSettings.Interface.LegacyNavbarStyleSummary" to "懸浮導航欄樣式",
        "AppSettings.Interface.LegacyNavbarStyleTitle" to "浮動導航欄",
        "AppSettings.Interface.PageScaleDialogSummary" to "80% - 120%",
        "AppSettings.Interface.PageScaleSummary" to "調整應用程式介面的整體縮放比例",
        "AppSettings.Interface.PageScaleTitle" to "頁面縮放",
        "AppSettings.Interface.ThemeColorPolarityInvertSummary" to "當主題取色看起來怪異時可嘗試開啟",
        "AppSettings.Interface.ThemeColorPolarityInvertTitle" to "反轉主題前景色",
        "AppSettings.Interface.ThemeModeDark" to "深色",
        "AppSettings.Interface.ThemeModeLight" to "淺色",
        "AppSettings.Interface.ThemeModeSummary" to "選擇應用程式的主題樣式",
        "AppSettings.Interface.ThemeModeSystem" to "跟隨系統",
        "AppSettings.Interface.ThemeModeTitle" to "主題模式",
        "AppSettings.Privacy.BiometricDialogTitleDisable" to "關閉啟動驗證",
        "AppSettings.Privacy.BiometricDialogTitleEnable" to "開啟啟動驗證",
        "AppSettings.Privacy.BiometricExitButton" to "離開",
        "AppSettings.Privacy.BiometricPromptMessage" to "請完成驗證以繼續進入應用程式",
        "AppSettings.Privacy.BiometricPromptTitle" to "驗證身份",
        "AppSettings.Privacy.BiometricRetryButton" to "重試",
        "AppSettings.Privacy.BiometricUnavailableHwUnavailable" to "生物識別硬體目前不可用，請稍後再試",
        "AppSettings.Privacy.BiometricUnavailableMessage" to "目前無法使用生物識別，請稍後重試或檢查系統螢幕鎖定與生物識別設定",
        "AppSettings.Privacy.BiometricUnavailableNoDeviceCredential" to "目前未設定系統螢幕鎖定驗證，請先啟用 PIN、圖案或密碼",
        "AppSettings.Privacy.BiometricUnavailableNoHardware" to "目前裝置不支援生物識別，請啟用系統螢幕鎖定驗證或關閉此功能",
        "AppSettings.Privacy.BiometricUnavailableNoneEnrolled" to "您尚未錄入生物資訊，請先在系統設定中錄入，或啟用 PIN、圖案、密碼等螢幕鎖定驗證",
        "AppSettings.Privacy.BiometricUnavailableTitle" to "無法使用指紋驗證",
        "AppSettings.Privacy.BiometricUnlockSummary" to "每次開啟應用程式需要生物識別或螢幕鎖定憑據驗證才能進入",
        "AppSettings.Privacy.BiometricUnlockTitle" to "啟動時生物驗證",
        "AppSettings.Privacy.HideFromRecentsSummary" to "啟用後不在最近使用的應用程式清單中顯示應用程式",
        "AppSettings.Privacy.HideFromRecentsTitle" to "隱藏最近任務卡片",
        "AppSettings.Privacy.HideIconSummary" to "隱藏後可透過撥號鍵盤 *#*#0721#*#* 開啟",
        "AppSettings.Privacy.HideIconTitle" to "隱藏應用程式圖示",
        "AppSettings.Privacy.ScreenshotDialogTitleDisable" to "關閉禁止截圖",
        "AppSettings.Privacy.ScreenshotDialogTitleEnable" to "開啟禁止截圖",
        "AppSettings.Privacy.ScreenshotProtectionSummary" to "禁止截圖、螢幕錄影及最近任務預覽",
        "AppSettings.Privacy.ScreenshotProtectionTitle" to "禁止截圖",
        "AppSettings.Section.Backup" to "備份",
        "AppSettings.Section.Behavior" to "行為",
        "AppSettings.Section.Experimental" to "實驗性內容",
        "AppSettings.Section.Interface" to "介面",
        "AppSettings.Section.Privacy" to "隱私",
        "AppSettings.Section.Service" to "服務",
        "AppSettings.ServiceSection.BatteryOptimizationTitle" to "電池最佳化無限制",
        "AppSettings.ServiceSection.ExitUiWhenBackgroundSummary" to "應用程式切到背景且介面不可見時主動釋放 UI，僅保留執行中的代理服務",
        "AppSettings.ServiceSection.ExitUiWhenBackgroundTitle" to "背景隱藏時釋放介面",
        "AppSettings.ServiceSection.SingleNodeTestSummary" to "點選節點卡片右側圖示測試單一節點延遲",
        "AppSettings.ServiceSection.SingleNodeTestTitle" to "單節點測試",
        "AppSettings.ServiceSection.TrafficNotificationSummary" to "在通知欄中顯示流量使用情況",
        "AppSettings.ServiceSection.TrafficNotificationTitle" to "顯示流量通知",
        "AppSettings.Title" to "應用程式設定",
        "AppSettings.WarningDialog.HideIconMsg1" to "請在隱藏之前確認你能夠存取本應用程式的設定介面！",
        "AppSettings.WarningDialog.HideIconMsg2" to "對於 HyperOS, 請開啟 自啟動 和 背景彈出介面 權限，以接受撥號介面程式碼！",
        "AppSettings.WarningDialog.Title" to "警告",
        "Component.BottomBar.Config" to "設定檔",
        "Component.BottomBar.Home" to "首頁",
        "Component.BottomBar.Proxy" to "代理",
        "Component.BottomBar.Setting" to "設定",
        "Component.Button.Cancel" to "取消",
        "Component.Button.Clear" to "清除",
        "Component.Button.Confirm" to "確定",
        "Component.Button.Copy" to "複製",
        "Component.Button.Delete" to "刪除",
        "Component.Button.Ok" to "確定",
        "Component.ConfigInput.CountItems" to "%d 項",
        "Component.ConfigInput.PortLabel" to "埠號 (留空表示不修改)",
        "Component.Editor.Action.Add" to "新增",
        "Component.Editor.Action.Delete" to "刪除",
        "Component.Editor.Action.Reset" to "重置",
        "Component.Editor.Action.Search" to "搜尋",
        "Component.Editor.CountItems" to "共 %d 項",
        "Component.Editor.Dialog.AddTitle" to "新增條目",
        "Component.Editor.Dialog.EditTitle" to "編輯條目",
        "Component.Editor.Dialog.ResetMessage" to "清空所有條目並恢復為不修改狀態？",
        "Component.Editor.Dialog.ResetTitle" to "重置確認",
        "Component.Editor.Empty.Hint" to "點選右上角按鈕新增",
        "Component.Editor.Empty.Title" to "暫無條目",
        "Component.Editor.Error.KeyEmpty" to "鍵不能為空",
        "Component.Editor.Error.KeyExists" to "鍵已存在",
        "Component.Editor.Rule.Content" to "規則內容",
        "Component.Editor.Rule.ErrorContentRequired" to "規則內容不能為空",
        "Component.Editor.Rule.ErrorTargetRequired" to "請選擇目標",
        "Component.Editor.Rule.NoResolve" to "不解析",
        "Component.Editor.Rule.Src" to "源 IP",
        "Component.Editor.Rule.Target" to "目標",
        "Component.Editor.Rule.TargetDirect" to "DIRECT",
        "Component.Editor.Rule.TargetMatch" to "MATCH",
        "Component.Editor.Rule.TargetReject" to "REJECT",
        "Component.Editor.Rule.Type" to "規則型別",
        "Component.Flag.ContentDescription" to "%s 旗幟",
        "Component.Loading.Starting" to "啟動中...",
        "Component.Message.Confirm" to "確定",
        "Component.Message.Error" to "錯誤",
        "Component.Message.Hint" to "提示",
        "Component.Message.Success" to "成功",
        "Component.Navigation.Back" to "返回",
        "Component.Navigation.Refresh" to "重新整理",
        "Component.ProfileCard.ClickToUpdate" to "點選更新以取得訂閱資訊",
        "Component.ProfileCard.DaysAgo" to "%d 天前",
        "Component.ProfileCard.Delete" to "刪除",
        "Component.ProfileCard.Edit" to "編輯",
        "Component.ProfileCard.ExpireAt" to "到期：%s (剩餘%d 天)",
        "Component.ProfileCard.ExpireToday" to "到期：今天",
        "Component.ProfileCard.Expired" to "已過期：%s",
        "Component.ProfileCard.Export" to "匯出",
        "Component.ProfileCard.HoursAgo" to "%d 小時前",
        "Component.ProfileCard.JustNow" to "剛剛",
        "Component.ProfileCard.LocalConfig" to "本地設定檔",
        "Component.ProfileCard.LocalFile" to "本地檔案",
        "Component.ProfileCard.MinutesAgo" to "%d 分鐘前",
        "Component.ProfileCard.RemoteSubscription" to "遠端訂閱",
        "Component.ProfileCard.Traffic" to "流量：%s / %s (%d%%)",
        "Component.ProfileCard.Update" to "更新",
        "Component.ProfileCard.UsedTraffic" to "已用：%s",
        "Component.Selector.Append" to "後置",
        "Component.Selector.Disable" to "停用",
        "Component.Selector.Enable" to "啟用",
        "Component.Selector.Merge" to "合併",
        "Component.Selector.NotModify" to "不修改",
        "Component.Selector.Prepend" to "前置",
        "Component.Selector.Replace" to "替換",
        "Component.Update.Action.DownloadNow" to "立即下載",
        "Component.Update.Message.Available" to "檢測到可用更新",
        "Component.Update.Message.CheckFailed" to "檢查更新失敗：%s",
        "Component.Update.Message.Checking" to "正在檢查更新...",
        "Component.Update.Message.Close" to "知道了",
        "Component.Update.Message.CoverDesc" to "更新封面",
        "Component.Update.Message.CurrentVersion" to "目前版本",
        "Component.Update.Message.DownloadAlreadyRunning" to "更新包仍在下載中",
        "Component.Update.Message.DownloadErrorWithCode" to "下載失敗 (%d): %s",
        "Component.Update.Message.DownloadReady" to "下載完成，準備安裝",
        "Component.Update.Message.Downloading" to "正在下載更新包...",
        "Component.Update.Message.DownloadingWithProgress" to "正在下載更新包 %d%%",
        "Component.Update.Message.Error" to "下載失敗，請稍後重試",
        "Component.Update.Message.Finished" to "下載完成，等待安裝確認",
        "Component.Update.Message.InstallFailed" to "開啟安裝器失敗：%s",
        "Component.Update.Message.InstallPromptOpened" to "下載完成，已開啟安裝器",
        "Component.Update.Message.MissingReleaseMetadata" to "最新發布缺少版本後設資料",
        "Component.Update.Message.NoCompatibleAsset" to "最新發布中沒有找到適合目前裝置的 APK",
        "Component.Update.Message.NoUpdate" to "目前已經是最新版本",
        "Component.Update.Message.Preparing" to "正在準備下載...",
        "Component.Update.Message.RemoteVersion" to "推送版本",
        "Component.Update.Message.Updating" to "正在更新",
        "Component.Update.Message.VerifyFailed" to "安裝包校驗失敗，請重試",
        "Component.Update.Message.Verifying" to "正在校驗安裝包...",
        "Component.Update.Message.Waiting" to "等待下載開始...",
        "Component.Update.Title.Available" to "發現新版本",
        "Component.WebView.InvalidUrl" to "無效的 URL",
        "Connection.ChainCount" to "x%d",
        "Connection.Detail.Action.Interrupt" to "打斷連線",
        "Connection.Detail.Action.Interrupting" to "正在打斷連線...",
        "Connection.Detail.Label.Content" to "內容",
        "Connection.Detail.Label.DestinationAddress" to "目標地址",
        "Connection.Detail.Label.Download" to "下載",
        "Connection.Detail.Label.Duration" to "連線時長",
        "Connection.Detail.Label.Process" to "程序",
        "Connection.Detail.Label.Protocol" to "協議",
        "Connection.Detail.Label.SourceAddress" to "源地址",
        "Connection.Detail.Label.Type" to "型別",
        "Connection.Detail.Label.Upload" to "上傳",
        "Connection.Detail.Section.Info" to "連線資訊",
        "Connection.Detail.Section.Rule" to "規則",
        "Connection.Empty" to "暫無活動連線",
        "Connection.Loading" to "載入中...",
        "Connection.NoResults" to "沒有匹配的連線",
        "Connection.RelativeTime.Date" to "%02d-%02d",
        "Connection.RelativeTime.DaysAgo" to "%d 天前",
        "Connection.RelativeTime.HoursAgo" to "%d 小時前",
        "Connection.RelativeTime.JustNow" to "剛剛",
        "Connection.RelativeTime.MinutesAgo" to "%d 分鐘前",
        "Connection.Search" to "搜尋",
        "Connection.SearchHint" to "搜尋主機、程序...",
        "Connection.Sort.Download" to "下載",
        "Connection.Sort.Host" to "主機",
        "Connection.Sort.Time" to "時間",
        "Connection.Sort.Upload" to "上傳",
        "Connection.SortBy" to "排序:",
        "Connection.Summary" to "檢視目前活動連線",
        "Connection.Tab.Active" to "活動中",
        "Connection.Tab.Closed" to "已關閉",
        "Connection.Title" to "連線",
        "Editor.Action.Discard" to "放棄",
        "Editor.Action.Format" to "格式化",
        "Editor.Action.Save" to "儲存",
        "Editor.Common.ConfigPreviewTitle" to "設定檔預覽",
        "Editor.Common.EditConfigTitle" to "編輯設定檔",
        "Editor.Common.EditOverrideConfigTitle" to "編輯覆寫設定",
        "Editor.Common.EditProfileConfigTitle" to "編輯訂閱設定檔",
        "Editor.Common.JsonSubtitle" to "使用 JSON 格式編輯",
        "Editor.Diagnostic.DuplicateKey" to "重複的鍵",
        "Editor.Diagnostic.Expected" to "期望 %s",
        "Editor.Diagnostic.JsonFormatError" to "JSON 格式錯誤",
        "Editor.Diagnostic.JsonMustStartWithObjectOrArray" to "JSON 必須以 '{' 或 '[' 開頭",
        "Editor.Diagnostic.JsonSyntaxError" to "JSON 語法錯誤",
        "Editor.Diagnostic.NoValue" to "缺少值",
        "Editor.Diagnostic.Unknown" to "未知",
        "Editor.Diagnostic.Unterminated" to "未終止的字串或物件",
        "Editor.Dialog.DiscardTitle" to "放棄修改",
        "Editor.Dialog.UnsavedChangesMessage" to "目前有未儲存的修改，確定要放棄嗎？",
        "Editor.Dialog.UnsavedChangesTitle" to "未儲存的修改",
        "Editor.Toast.FormatFailedOrUnchanged" to "格式化失敗或無需格式化",
        "Editor.Toast.FormatSuccess" to "格式化成功",
        "Editor.Toast.SaveFailed" to "儲存失敗",
        "Editor.Toast.SyntaxError" to "語法錯誤，請檢查內容",
        "Feature.Node.HealthCheckConcurrencySummary" to "目前選項：%s",
        "Feature.Node.HealthCheckConcurrencyTitle" to "測速並發數",
        "Feature.Node.Section" to "節點",
        "Feature.RuntimeConfig.Empty" to "運行時配置檔案沒有內容",
        "Feature.RuntimeConfig.NotReady" to "運行時配置尚未準備完成",
        "Feature.RuntimeConfig.NotRunning" to "請先啟動代理，再查看運行時配置",
        "Feature.RuntimeConfig.PreviewTitle" to "運行時配置 · %s",
        "Feature.RuntimeConfig.RuntimeChanged" to "運行中的配置已變更，請重新開啟",
        "Feature.RuntimeConfig.Section" to "診斷",
        "Feature.RuntimeConfig.Summary" to "查看 Mihomo 目前實際載入的唯讀 YAML；內容可能包含敏感資料",
        "Feature.RuntimeConfig.Title" to "查看運行時配置",
        "Feature.RuntimeConfig.Unavailable" to "找不到目前的運行時配置檔案",
        "Feature.RuntimeConfig.UnknownProfile" to "未知配置",
        "Feature.SpeedTest.Cancel" to "取消",
        "Feature.SpeedTest.DataUsage" to "每次測試最多使用 32 MB 下載與 8 MB 上傳流量",
        "Feature.SpeedTest.Download" to "下載",
        "Feature.SpeedTest.EdgeLocation" to "Cloudflare Edge",
        "Feature.SpeedTest.Error" to "無法完成速度測試，請檢查連線後再試一次。",
        "Feature.SpeedTest.Jitter" to "抖動",
        "Feature.SpeedTest.Latency" to "延遲",
        "Feature.SpeedTest.LocationUnknown" to "未知",
        "Feature.SpeedTest.Preparing" to "正在準備測試…",
        "Feature.SpeedTest.PrivacyNotice" to "測試流量會傳送至 Cloudflare；不包含封包遺失測試。",
        "Feature.SpeedTest.Section" to "網路測試",
        "Feature.SpeedTest.Start" to "開始測試",
        "Feature.SpeedTest.Summary" to "測量目前連線至 Cloudflare Edge 的品質",
        "Feature.SpeedTest.TestingDownload" to "正在測量下載速度…",
        "Feature.SpeedTest.TestingLatency" to "正在測量延遲…",
        "Feature.SpeedTest.TestingUpload" to "正在測量上傳速度…",
        "Feature.SpeedTest.Title" to "Cloudflare 速度測試",
        "Feature.SpeedTest.Upload" to "上傳",
        "Feature.Title" to "進階功能",
        "Home.Control.HintAddProfile" to "請先新增設定檔",
        "Home.Control.HintEnableProfile" to "請先在「設定檔」頁面啟用一個設定檔",
        "Home.Control.HintProfilesLoading" to "設定檔仍在載入，請稍候。",
        "Home.Control.Start" to "啟動",
        "Home.Control.Stop" to "停止",
        "Home.IpInfo.ExitIp" to "出口 IP",
        "Home.Message.ConfigSwitchFailed" to "設定檔切換失敗：%s",
        "Home.Message.ConfigSwitched" to "設定檔已切換",
        "Home.Message.ControlBusy" to "代理%s，請稍候。",
        "Home.Message.Preparing" to "正在準備...",
        "Home.Message.StartFailed" to "啟動失敗：%s",
        "Home.Message.StopFailed" to "停止失敗：%s",
        "Home.Message.WaitingForVpnPermission" to "正在等待 VPN 授權",
        "Home.NodeInfo.Delay" to "延遲",
        "Home.NodeInfo.DelayValue" to "%dms",
        "Home.NodeInfo.Node" to "節點",
        "Home.NodeInfo.Unknown" to "未知",
        "Home.ProxyMode.Http" to "HTTP",
        "Home.ProxyMode.Tun" to "TUN",
        "Home.ProxyMode.Vpn" to "VPN",
        "Home.Status.Connecting" to "連線中",
        "Home.Status.Disconnecting" to "斷開中",
        "Home.Status.Running" to "執行中",
        "Home.Status.TapFabToStart" to "點選懸浮按鈕啟動",
        "Home.Status.TapToStart" to "輕觸啟動",
        "Home.Title" to "KokoroBox",
        "Home.Traffic.DownShort" to "DOWN",
        "Home.Traffic.NoProfile" to "無設定檔",
        "Home.Traffic.UpShort" to "UP",
        "Log.Action.Save" to "儲存",
        "Log.Action.StartRecording" to "開始記錄",
        "Log.Action.StopRecording" to "停止記錄",
        "Log.Detail.WaitingLog" to "等待日誌...",
        "Log.Detail.WillShowWhenGenerated" to "日誌將在產生時顯示",
        "Log.Empty.NoLogs" to "暫無日誌記錄",
        "Log.Empty.StartRecordingHint" to "點選右下角按鈕開始記錄日誌",
        "Log.Title" to "日誌",
        "MetaFeature.AgeKey.DerivePublicKey" to "推導公開金鑰",
        "MetaFeature.AgeKey.Generate" to "產生",
        "MetaFeature.AgeKey.HybridTitle" to "mlkem768-x25519",
        "MetaFeature.AgeKey.PublicKey" to "公開金鑰",
        "MetaFeature.AgeKey.SecretKey" to "私密金鑰",
        "MetaFeature.AgeKey.Section" to "Age 金鑰",
        "MetaFeature.AgeKey.X25519Title" to "X25519",
        "MetaFeature.CustomRules.AddRule" to "新增規則",
        "MetaFeature.CustomRules.BackToKokoroSettings" to "返回 Kokoro 設定",
        "MetaFeature.CustomRules.Cancel" to "取消",
        "MetaFeature.CustomRules.Confirm" to "確認",
        "MetaFeature.CustomRules.ConflictMessage" to "請選擇遠端版本，或保留本機編輯並使用最新 revision 再次儲存。",
        "MetaFeature.CustomRules.ConflictTitle" to "規則已在其他裝置變更",
        "MetaFeature.CustomRules.DeleteRule" to "刪除規則",
        "MetaFeature.CustomRules.DiscardMessage" to "此操作會放棄尚未儲存的變更。",
        "MetaFeature.CustomRules.DiscardTitle" to "放棄本機變更？",
        "MetaFeature.CustomRules.EditRule" to "編輯規則",
        "MetaFeature.CustomRules.Empty" to "default 規則目前為空。",
        "MetaFeature.CustomRules.ErrorLoad" to "無法載入自訂規則。",
        "MetaFeature.CustomRules.ErrorNotFound" to "default 規則目前無法使用，已重新載入伺服器狀態。",
        "MetaFeature.CustomRules.ErrorRateLimited" to "請求過於頻繁，請稍後再試。",
        "MetaFeature.CustomRules.ErrorRequest" to "伺服器拒絕了此項請求。",
        "MetaFeature.CustomRules.ErrorUnknown" to "無法確認儲存結果，繼續前請檢查遠端版本。",
        "MetaFeature.CustomRules.ErrorValidation" to "第 %d 條規則無效，請重新整理選項並修正後再儲存。",
        "MetaFeature.CustomRules.ErrorValidationGeneral" to "規則列表無效，請重新整理選項並修正後再儲存。",
        "MetaFeature.CustomRules.KeepLocal" to "保留本機版本",
        "MetaFeature.CustomRules.Loading" to "正在載入自訂規則…",
        "MetaFeature.CustomRules.MatchPayloadHint" to "MATCH 不使用內容欄位。",
        "MetaFeature.CustomRules.MoveDown" to "向下移動規則",
        "MetaFeature.CustomRules.MoveUp" to "向上移動規則",
        "MetaFeature.CustomRules.Payload" to "內容",
        "MetaFeature.CustomRules.Provider" to "規則提供者",
        "MetaFeature.CustomRules.Refresh" to "從伺服器重新載入",
        "MetaFeature.CustomRules.Retry" to "重試",
        "MetaFeature.CustomRules.Rules" to "規則",
        "MetaFeature.CustomRules.Save" to "儲存規則",
        "MetaFeature.CustomRules.Saved" to "自訂規則已儲存",
        "MetaFeature.CustomRules.Target" to "目標",
        "MetaFeature.CustomRules.Title" to "自訂規則",
        "MetaFeature.CustomRules.Type" to "類型",
        "MetaFeature.CustomRules.UseRemote" to "使用遠端版本",
        "MetaFeature.Download.DialogTitle" to "線上更新 GeoX",
        "MetaFeature.Download.DownloadComplete" to "下載完成：%d/%d",
        "MetaFeature.Download.ImportFailed" to "%s 匯入失敗：檔案無效或校驗未通過",
        "MetaFeature.Download.ImportSuccess" to "%s 已匯入並套用",
        "MetaFeature.Download.LastUpdate" to "上次更新（%s）：%s",
        "MetaFeature.Download.LastUpdateNever" to "暫無記錄",
        "MetaFeature.Download.LastUpdateSourceLocal" to "本機",
        "MetaFeature.Download.LastUpdateSourceOnline" to "線上",
        "MetaFeature.Download.LocalDialogTitle" to "本機更新 GeoX",
        "MetaFeature.Download.ProgressDetail" to "%s / %s · %s",
        "MetaFeature.Download.ProgressDetailUnknownTotal" to "%s · %s",
        "MetaFeature.Download.ProgressFailed" to "下載或校驗失敗，已保留可用回退檔案",
        "MetaFeature.Download.ProgressSuccess" to "檔案已更新並通過校驗",
        "MetaFeature.Download.ProgressSummary" to "請保持此頁面開啟，正在下載並校驗檔案",
        "MetaFeature.Download.ProgressTitle" to "正在更新 GeoX",
        "MetaFeature.Download.ProgressWaiting" to "等待開始",
        "MetaFeature.Download.SelectFiles" to "請選擇要更新的檔案",
        "MetaFeature.Download.StatusDownloading" to "下載中 %d%%",
        "MetaFeature.Download.StatusFailed" to "失敗",
        "MetaFeature.Download.StatusPending" to "等待中",
        "MetaFeature.Download.StatusSuccess" to "已完成",
        "MetaFeature.Download.StatusValidating" to "校驗中",
        "MetaFeature.GeoX.LocalUpdateSummary" to "從本機檔案匯入並套用資料庫",
        "MetaFeature.GeoX.LocalUpdateTitle" to "本機更新 GeoX",
        "MetaFeature.GeoX.OnlineUpdateSummary" to "從遠端來源下載資料庫檔案",
        "MetaFeature.GeoX.OnlineUpdateTitle" to "線上更新 GeoX",
        "MetaFeature.GeoX.RuntimeHomeInfo" to "GeoX 資料庫檔案下載並存放在核心目錄：%s",
        "MetaFeature.Section.ConnectionAndTraffic" to "連線與流量統計",
        "MetaFeature.Section.GeoXUpdate" to "GeoX 更新",
        "MetaFeature.Title" to "Meta 功能",
        "NetworkSettings.Error.RootRequired" to "RootTun 需要 KokoroBox 已取得可用的 Root 權限",
        "NetworkSettings.Error.VpnDenied" to "VPN 權限被拒絕",
        "NetworkSettings.Experimental.AntiPollutionDnsSummary" to "以加密的境內外 DNS 與分流策略覆寫訂閱設定；需要 GeoSite 資料",
        "NetworkSettings.Experimental.AntiPollutionDnsTitle" to "抗污染 DNS",
        "NetworkSettings.Network.CustomUserAgentSummaryDefault" to "未設定，使用預設值",
        "NetworkSettings.Network.CustomUserAgentTitle" to "自訂 User-Agent",
        "NetworkSettings.Network.UserAgentDialogTitle" to "編輯 User-Agent",
        "NetworkSettings.ProxyOptions.AccessControlModeTitle" to "存取控制模式",
        "NetworkSettings.ProxyOptions.AllowAll" to "允許所有",
        "NetworkSettings.ProxyOptions.AllowSelected" to "允許選擇",
        "NetworkSettings.ProxyOptions.ManageAccessControlSummary" to "為應用程式和網域設定存取控制規則",
        "NetworkSettings.ProxyOptions.ManageAccessControlTitle" to "管理存取控制清單",
        "NetworkSettings.ProxyOptions.RejectSelected" to "拒絕選擇",
        "NetworkSettings.ProxyOptions.TunStackTitle" to "TUN 協定堆疊",
        "NetworkSettings.RootTun.AutoRedirectSummary" to "自動啟用 RootTun 所需的重新導向規則",
        "NetworkSettings.RootTun.AutoRedirectTitle" to "自動重新導向",
        "NetworkSettings.RootTun.AutoRouteSummary" to "自動新增轉送所需路由",
        "NetworkSettings.RootTun.AutoRouteTitle" to "自動路由",
        "NetworkSettings.RootTun.DnsModeFakeIp" to "FakeIP",
        "NetworkSettings.RootTun.DnsModeRedirHost" to "RedirHost",
        "NetworkSettings.RootTun.DnsModeSummary" to "選擇 RedirHost 或 FakeIP",
        "NetworkSettings.RootTun.DnsModeTitle" to "DNS 模式",
        "NetworkSettings.RootTun.FakeIpRange6Summary" to "僅在 FakeIP 模式下生效",
        "NetworkSettings.RootTun.FakeIpRange6Title" to "FakeIP IPv6 地址段",
        "NetworkSettings.RootTun.FakeIpRangeSummary" to "僅在 FakeIP 模式下生效",
        "NetworkSettings.RootTun.FakeIpRangeTitle" to "FakeIP IPv4 地址段",
        "NetworkSettings.RootTun.IfNameSummary" to "RootTun 建立的虛擬網路卡名",
        "NetworkSettings.RootTun.IfNameTitle" to "介面名稱",
        "NetworkSettings.RootTun.MtuSummary" to "RootTun 連線的最大傳輸單元",
        "NetworkSettings.RootTun.MtuTitle" to "MTU",
        "NetworkSettings.RootTun.StrictRouteSummary" to "僅允許命中的流量經由 RootTun",
        "NetworkSettings.RootTun.StrictRouteTitle" to "嚴格路由",
        "NetworkSettings.Section.Experimental" to "實驗性功能",
        "NetworkSettings.Section.Network" to "網路",
        "NetworkSettings.Section.ProxyOptions" to "存取控制",
        "NetworkSettings.Section.VpnOptions" to "服務設定",
        "NetworkSettings.Section.VpnService" to "代理模式",
        "NetworkSettings.Title" to "網路設定",
        "NetworkSettings.VpnOptions.AllowBypassSummary" to "允許應用程式繞過 VPN",
        "NetworkSettings.VpnOptions.AllowBypassTitle" to "允許應用程式繞過",
        "NetworkSettings.VpnOptions.BypassPrivateSummary" to "繞過私有網路和本地地址",
        "NetworkSettings.VpnOptions.BypassPrivateTitle" to "繞過私有網路",
        "NetworkSettings.VpnOptions.DnsHijackSummary" to "將所有 DNS 請求重新導向到 KokoroBox",
        "NetworkSettings.VpnOptions.DnsHijackTitle" to "DNS 劫持",
        "NetworkSettings.VpnOptions.EnableIpv6Summary" to "允許透過 VPN 路由 IPv6 流量",
        "NetworkSettings.VpnOptions.EnableIpv6Title" to "啟用 IPv6",
        "NetworkSettings.VpnOptions.SystemProxySummary" to "僅在 VPN 模式下，為未經由 TUN 的應用程式設定 HTTP 代理",
        "NetworkSettings.VpnOptions.SystemProxyTitle" to "VPN 系統代理",
        "NetworkSettings.VpnService.RootTunMode" to "Root TUN",
        "NetworkSettings.VpnService.RouteTrafficSummary" to "選擇目前用於接管系統流量的代理模式",
        "NetworkSettings.VpnService.RouteTrafficTitle" to "路由系統流量",
        "NetworkSettings.VpnService.SystemProxy" to "HTTP 系統代理",
        "NetworkSettings.VpnService.VpnMode" to "VPN 模式",
        "Onboarding.Finish.Subtitle" to "基礎設定已就緒即將進入主頁",
        "Onboarding.Finish.Title" to "準備完成",
        "Onboarding.Navigation.Back" to "返回",
        "Onboarding.Navigation.Enter" to "進入應用程式",
        "Onboarding.Navigation.Next" to "下一步",
        "Onboarding.Navigation.Start" to "開始設定",
        "Onboarding.Permission.AppList.SummaryNeed" to "用於依應用程式分流等功能",
        "Onboarding.Permission.AppList.Title" to "應用程式清單權限",
        "Onboarding.Permission.Common.Granted" to "已授權",
        "Onboarding.Permission.Notification.SummaryNeed" to "用於顯示連線狀態和流量通知",
        "Onboarding.Permission.Notification.SummaryNotRequired" to "目前系統無需額外授權",
        "Onboarding.Permission.Notification.Title" to "通知權限",
        "Onboarding.Permission.Subtitle" to "權限會影響通知和依應用程式分流等功能",
        "Onboarding.Permission.Title" to "確認執行權限",
        "Onboarding.Personalize.Subtitle" to "主題模式和主色可隨時在設定中修改",
        "Onboarding.Personalize.Title" to "調整介面風格",
        "Onboarding.Privacy.Accept.Title" to "我已閱讀並同意隱私政策",
        "Onboarding.Privacy.PolicyLink" to "《隱私政策》",
        "Onboarding.Privacy.Privacy.Title" to "隱私政策",
        "Onboarding.Privacy.RichTextLead" to "在開始使用前需要同意隱私政策，才能繼續使用 KokoroBox。",
        "Onboarding.Privacy.RichTextPrefix" to "繼續前請先閱讀並同意",
        "Onboarding.Privacy.RichTextSuffix" to "。",
        "Onboarding.Privacy.Subtitle" to "閱讀並同意隱私政策後方可繼續",
        "Onboarding.Privacy.Title" to "確認隱私政策",
        "Onboarding.Sheet.LoadFailed" to "無法載入協議內容",
        "Onboarding.Sheet.PrivacyPolicyTitle" to "隱私政策",
        "OpenSourceLicenses.LicenseSheet.NoContent" to "暫無許可證內容",
        "OpenSourceLicenses.Title" to "開源許可證",
        "Override.Action.Create" to "建立覆寫設定",
        "Override.Action.Import" to "匯入 JSON",
        "Override.Action.ImportFile" to "匯入設定檔",
        "Override.Action.New" to "新增覆寫設定",
        "Override.Card.Copy" to "複製設定",
        "Override.Card.Delete" to "刪除設定",
        "Override.Card.DeleteButton" to "刪除",
        "Override.Card.Edit" to "編輯設定",
        "Override.Card.EditButton" to "編輯",
        "Override.Card.Export" to "匯出設定",
        "Override.Card.NoDescription" to "未填寫描述",
        "Override.Dialog.Button.Cancel" to "取消",
        "Override.Dialog.Button.Delete" to "刪除",
        "Override.Dialog.Create.Description" to "設定描述",
        "Override.Dialog.Create.ImportHint" to "選擇 JSON 檔案匯入覆寫設定",
        "Override.Dialog.Create.Name" to "設定名稱",
        "Override.Dialog.Create.Title" to "新增設定",
        "Override.Dialog.Delete.InUseMessage" to "設定 %s 正在被訂閱使用，刪除後將解除繫結關係。確定要刪除嗎？此操作不可恢復。",
        "Override.Dialog.Delete.Message" to "確定要刪除設定 %s 嗎？此操作不可恢復。",
        "Override.Dialog.Delete.Title" to "刪除設定",
        "Override.Dialog.EditOptions.CodeEditor" to "程式碼編輯器",
        "Override.Dialog.EditOptions.Title" to "編輯設定",
        "Override.Dialog.EditOptions.VisualEditor" to "視覺化編輯",
        "Override.Dns.AppendSystem" to "追加系統 DNS",
        "Override.Dns.Default" to "預設 DNS",
        "Override.Dns.DefaultHint" to "用於解析 DNS 伺服器網域",
        "Override.Dns.EnhancedDisable" to "停用",
        "Override.Dns.EnhancedFakeip" to "FakeIP",
        "Override.Dns.EnhancedMapping" to "Mapping",
        "Override.Dns.EnhancedMode" to "增強模式",
        "Override.Dns.EnhancedNotModify" to "不修改",
        "Override.Dns.FakeipBlacklist" to "黑名單",
        "Override.Dns.FakeipFilter" to "FakeIP 過濾",
        "Override.Dns.FakeipFilterHint" to "例如：+.lan, localhost",
        "Override.Dns.FakeipFilterMode" to "FakeIP 過濾模式",
        "Override.Dns.FakeipWhitelist" to "白名單",
        "Override.Dns.Fallback" to "備用 DNS",
        "Override.Dns.FallbackDomain" to "網域回退",
        "Override.Dns.FallbackDomainHint" to "例如：+.google.com",
        "Override.Dns.FallbackGeoip" to "GeoIP 回退",
        "Override.Dns.FallbackGeoipCode" to "GeoIP 程式碼",
        "Override.Dns.FallbackGeoipCodeHint" to "例如：CN",
        "Override.Dns.FallbackHint" to "例如：1.1.1.1",
        "Override.Dns.FallbackIpcidr" to "IP CIDR 回退",
        "Override.Dns.FallbackIpcidrHint" to "例如：240.0.0.0/4",
        "Override.Dns.Ipv6" to "DNS IPv6",
        "Override.Dns.Listen" to "監聽地址",
        "Override.Dns.ListenHint" to "例如：0.0.0.0:53",
        "Override.Dns.NameserverPolicy" to "DNS 策略",
        "Override.Dns.NameserverPolicyKey" to "網域匹配規則",
        "Override.Dns.NameserverPolicyValue" to "DNS 伺服器",
        "Override.Dns.Policy" to "DNS 策略",
        "Override.Dns.PolicyForceEnable" to "強制啟用",
        "Override.Dns.PolicyNotModify" to "不修改",
        "Override.Dns.PolicyUseBuiltin" to "使用內建",
        "Override.Dns.PreferH3" to "優先 HTTP/3",
        "Override.Dns.Servers" to "DNS 伺服器",
        "Override.Dns.ServersHint" to "例如：8.8.8.8, tls://dns.google",
        "Override.Dns.UseHosts" to "使用 hosts",
        "Override.Draft.AddExtraField" to "新增額外欄位",
        "Override.Draft.AddHealthCheckField" to "新增 health-check 額外欄位",
        "Override.Draft.AddOverrideField" to "新增 override 額外欄位",
        "Override.Draft.Apply" to "套用",
        "Override.Draft.BasicIdentity" to "基礎身份",
        "Override.Draft.BasicInfo" to "基礎資訊",
        "Override.Draft.BasicRouting" to "基礎分流",
        "Override.Draft.BooleanOptions" to "布林選項",
        "Override.Draft.ClickToAddExtraField" to "點選新增額外欄位",
        "Override.Draft.ConfigDescription" to "設定說明",
        "Override.Draft.ConfigName" to "設定名稱",
        "Override.Draft.ConfigSections" to "設定區塊",
        "Override.Draft.CoreSource" to "核心來源",
        "Override.Draft.DeleteExtraField" to "刪除額外欄位",
        "Override.Draft.DoubleValue" to "浮點數值",
        "Override.Draft.EditExtraField" to "編輯額外欄位",
        "Override.Draft.EditHealthCheckField" to "編輯 health-check 額外欄位",
        "Override.Draft.EditOverrideField" to "編輯 override 額外欄位",
        "Override.Draft.EditSubRules" to "編輯子規則",
        "Override.Draft.ExtraFields" to "額外欄位",
        "Override.Draft.ExtraFieldsConfigured" to "已設定 %d 個額外欄位",
        "Override.Draft.FallbackRegionGroupTitle" to "Fallback 地區組",
        "Override.Draft.GroupTypeFallback" to "Fallback",
        "Override.Draft.GroupTypeTitle" to "策略組型別",
        "Override.Draft.GroupTypeUrlTest" to "UrlTest",
        "Override.Draft.HeaderHint" to "每行一個 header，格式：Key: value1 | value2",
        "Override.Draft.HealthCheckFields" to "Health Check 額外欄位",
        "Override.Draft.HealthCheckSwitch" to "Health Check 開關",
        "Override.Draft.IntValue" to "整數值",
        "Override.Draft.JsonFragment" to "單一 JSON 片段",
        "Override.Draft.KeyNameEmpty" to "鍵名不能為空",
        "Override.Draft.Name" to "名稱",
        "Override.Draft.NameRequired" to "名稱不能為空",
        "Override.Draft.NetworkAuth" to "網路與認證",
        "Override.Draft.NoRules" to "未設定規則",
        "Override.Draft.Object" to "物件",
        "Override.Draft.OfficialMrs" to "官方 MRS 常用分流",
        "Override.Draft.OfficialMrsSummary" to "頂部模板編輯器，支援地區自動組和每個分流項單獨開關；套用時會重建目前覆寫裡的規則三塊。",
        "Override.Draft.OverrideFields" to "Override 額外欄位",
        "Override.Draft.OverrideSwitch" to "Override 開關",
        "Override.Draft.PresetApplySummary" to "套用後會覆蓋目前覆寫裡的規則提供者、策略組和規則",
        "Override.Draft.PresetTemplate" to "預設分流模板",
        "Override.Draft.RegionalAutoGroup" to "地區自動組",
        "Override.Draft.RuleList" to "規則清單",
        "Override.Draft.RulesConfigured" to "已設定 %d 條規則",
        "Override.Draft.Save" to "儲存",
        "Override.Draft.ServiceRouting" to "服務分流",
        "Override.Draft.StringValue" to "字串值",
        "Override.Draft.SubRuleGroup" to "子規則組",
        "Override.Draft.UrlTestRegionGroupTitle" to "UrlTest 地區組",
        "Override.Draft.ValueType" to "值型別",
        "Override.Draft.ValueTypeMismatch" to "目前值與所選型別不匹配",
        "Override.Edit.Button.Cancel" to "取消",
        "Override.Edit.Button.Discard" to "放棄",
        "Override.Edit.EmptyName.Summary" to "目前名稱為空，無法實時儲存。確定放棄這次未儲存的修改嗎？",
        "Override.Edit.EmptyName.Title" to "名稱為空",
        "Override.Edit.PresetApplied" to "已更新預設分流模板",
        "Override.Edit.TitleEdit" to "編輯設定",
        "Override.Edit.TitleNew" to "新增覆寫設定",
        "Override.Editor.AddCustom" to "新增自訂",
        "Override.Editor.AddItem" to "新增條目",
        "Override.Editor.AddObject" to "新增物件",
        "Override.Editor.AddSubRuleGroup" to "新增子規則組",
        "Override.Editor.AdditionalParams" to "附加參數",
        "Override.Editor.ArrayItems" to "陣列 %d 項",
        "Override.Editor.BasicConnection" to "基礎連線",
        "Override.Editor.CancelDelete" to "取消刪除",
        "Override.Editor.Clear" to "清空",
        "Override.Editor.ClearCurrentMode" to "清空目前模式",
        "Override.Editor.ClearDialog.Summary" to "清空後將移除目前模式裡的所有%s。",
        "Override.Editor.ClearDialog.Title" to "清空%s",
        "Override.Editor.ClearMode" to "清空目前模式",
        "Override.Editor.ClearSubRules" to "清空子規則",
        "Override.Editor.Confirm" to "確定",
        "Override.Editor.ContentEmpty" to "內容不能為空",
        "Override.Editor.Copy" to "複製",
        "Override.Editor.CustomMatchResult" to "自訂匹配結果",
        "Override.Editor.CustomMember" to "自訂成員",
        "Override.Editor.CustomProxyGroupTarget" to "自訂策略組目標",
        "Override.Editor.CustomSubRuleTarget" to "自訂子規則目標",
        "Override.Editor.Delete" to "刪除",
        "Override.Editor.DeleteLastItem" to "刪除最後一項",
        "Override.Editor.DeleteSelected" to "刪除已選條目",
        "Override.Editor.DeleteSelectedRules" to "刪除已選規則",
        "Override.Editor.DragToSort" to "拖拽排序",
        "Override.Editor.Edit" to "編輯",
        "Override.Editor.EditItem" to "編輯條目",
        "Override.Editor.EditProxyGroup" to "編輯策略組",
        "Override.Editor.EditProxyNode" to "編輯代理節點",
        "Override.Editor.EditRule" to "編輯規則",
        "Override.Editor.EditSubRule" to "編輯子規則",
        "Override.Editor.EditSubRuleGroup" to "編輯子規則組",
        "Override.Editor.EmptyString" to "空字串",
        "Override.Editor.EnterDeleteMode" to "進入刪除模式",
        "Override.Editor.ExtraParamsHint" to "例如 src,no-resolve 之外的額外參數\\n邏輯規則請直接填寫完整 payload，例如 ((DOMAIN,google.com),(NETWORK,udp))。",
        "Override.Editor.HealthCheckAndFilter" to "健康檢查與過濾",
        "Override.Editor.JsonBlockSubtitle" to "使用 JSON 格式編輯該設定塊",
        "Override.Editor.KeyName" to "鍵名",
        "Override.Editor.List" to "清單",
        "Override.Editor.LogicalRuleHint" to "邏輯規則可直接填寫完整 payload",
        "Override.Editor.MatchResult" to "匹配結果",
        "Override.Editor.MemberSource" to "成員來源",
        "Override.Editor.Mode.Title" to "修飾符模式",
        "Override.Editor.MoveDown" to "下移",
        "Override.Editor.MoveUp" to "上移",
        "Override.Editor.NetworkAndRoute" to "網路與路由",
        "Override.Editor.New" to "新增",
        "Override.Editor.NewProvider" to "新增 Provider",
        "Override.Editor.NewProxyGroup" to "新增策略組",
        "Override.Editor.NewProxyNode" to "新增代理節點",
        "Override.Editor.NewRule" to "新增規則",
        "Override.Editor.NewSubRuleGroup" to "新增子規則組",
        "Override.Editor.NoRules" to "暫無規則",
        "Override.Editor.ObjectFallbackTitle" to "物件 %d",
        "Override.Editor.ObjectFieldCount" to "%d 個欄位",
        "Override.Editor.ObjectFieldHint" to "鍵值內容支援簡單值和 JSON 結構。",
        "Override.Editor.ObjectFields" to "物件 %d 個欄位",
        "Override.Editor.ObjectJsonPlaceholder" to "{ \"name\": \"proxy\", \"type\": \"ss\" }",
        "Override.Editor.ObjectListHint" to "結構化編輯物件清單，欄位值支援字串、數字、布林和 JSON 片段。",
        "Override.Editor.OneItemPerLine" to "每行一個條目",
        "Override.Editor.OtherExtraParams" to "其他附加參數，多個值用逗號分隔",
        "Override.Editor.Payload" to "匹配內容",
        "Override.Editor.PayloadEmpty" to "匹配內容不能為空",
        "Override.Editor.PortEmptyHint" to "留空表示不覆寫埠",
        "Override.Editor.ProviderMapHint" to "結構化編輯 Provider 字典，同名鍵會覆蓋舊值。",
        "Override.Editor.ProxyGroup" to "策略組",
        "Override.Editor.ProxyGroupTarget" to "策略組目標",
        "Override.Editor.ProxyNode" to "代理節點",
        "Override.Editor.RuleBody" to "規則主體",
        "Override.Editor.RuleEdit" to "規則編輯",
        "Override.Editor.RulePlaceholder" to "DOMAIN-SUFFIX,example.com,DIRECT",
        "Override.Editor.RuleProviderInputHint" to "輸入框是自訂內容；留空時使用下面選中的規則提供者",
        "Override.Editor.RuleType" to "型別",
        "Override.Editor.RuleTypeEmpty" to "規則型別不能為空",
        "Override.Editor.Rules" to "規則",
        "Override.Editor.RulesConfiguredInline" to "已設定 %d 條規則",
        "Override.Editor.SaveProxyGroup" to "儲存策略組",
        "Override.Editor.SaveProxyNode" to "儲存代理節點",
        "Override.Editor.SaveRule" to "儲存規則",
        "Override.Editor.SelectMatchResult" to "選擇匹配結果",
        "Override.Editor.SelectProxyGroupMember" to "選擇策略組成員",
        "Override.Editor.SelectProxyGroupTarget" to "選擇策略組目標",
        "Override.Editor.SelectRuleProvider" to "選擇規則提供者",
        "Override.Editor.SelectSubRuleTarget" to "選擇子規則目標",
        "Override.Editor.SubRuleGroupHint" to "每個子規則組包含一個名稱和一組規則。",
        "Override.Editor.SubRuleName" to "子規則名稱",
        "Override.Editor.SubRuleTarget" to "子規則目標",
        "Override.Editor.TargetEmpty" to "目標不能為空",
        "Override.Editor.TypeEmpty" to "型別不能為空",
        "Override.Editor.Unnamed" to "未命名%s",
        "Override.Editor.UnnamedProvider" to "未命名 Provider",
        "Override.Editor.UnnamedProxyGroup" to "未命名策略組",
        "Override.Editor.UnnamedProxyNode" to "未命名代理節點",
        "Override.Editor.UnnamedRule" to "未命名規則",
        "Override.Editor.UnnamedSubRuleGroup" to "未命名子規則組",
        "Override.Empty.Hint" to "點選下方按鈕建立新的覆寫設定，或匯入 JSON",
        "Override.Empty.Title" to "暫無覆寫設定",
        "Override.Export.Failed" to "匯出失敗：%s",
        "Override.Export.Success" to "已匯出設定：%s",
        "Override.Form.AdvancedJson" to "%s · 高階 JSON",
        "Override.Form.AllowPrivateNetwork" to "允許私有網路",
        "Override.Form.AllowedIPs" to "允許 IP 段",
        "Override.Form.ApiSecret" to "API 存取金鑰",
        "Override.Form.AutoDetectInterface" to "自動識別網路卡",
        "Override.Form.AutoRedirect" to "自動重新導向",
        "Override.Form.AutoRoute" to "自動路由",
        "Override.Form.AutoUpdateGeo" to "自動更新 GEO",
        "Override.Form.BasicPolicy" to "基礎策略",
        "Override.Form.BindAddress" to "繫結地址",
        "Override.Form.CacheLimit" to "快取上限",
        "Override.Form.ConfigPersistence" to "設定持久化",
        "Override.Form.ConnectionNetwork" to "連線與網路",
        "Override.Form.ControllerCors" to "控制器 CORS",
        "Override.Form.DirectFollowPolicy" to "Direct 遵循 Policy",
        "Override.Form.DisableIcmpForward" to "停用 ICMP 轉送",
        "Override.Form.DisallowedIPs" to "禁止 IP 段",
        "Override.Form.DnsBasicParams" to "DNS 基礎參數",
        "Override.Form.DnsBasicSwitch" to "基礎開關",
        "Override.Form.DnsFakeIpRange" to "FakeIP 地址段",
        "Override.Form.DnsHijack" to "DNS 劫持",
        "Override.Form.DnsPolicyMode" to "策略模式",
        "Override.Form.DnsUpstream" to "上游伺服器",
        "Override.Form.DnsUpstreamServers" to "上游伺服器",
        "Override.Form.EnableGso" to "啟用 GSO",
        "Override.Form.EndpointIndependentNat" to "獨立於端點 NAT",
        "Override.Form.ExcludePackage" to "排除應用程式",
        "Override.Form.ExternalControl" to "外部控制",
        "Override.Form.ExternalController" to "外部控制器",
        "Override.Form.ExternalControllerHttps" to "HTTPS 控制器",
        "Override.Form.ExternalDoH" to "外部 DoH 服務",
        "Override.Form.FakeIpIpv6Range" to "Fake-IP IPv6 網段",
        "Override.Form.FakeIpMode" to "Fake-IP 模式",
        "Override.Form.FakeIpParams" to "Fake-IP 參數",
        "Override.Form.FallbackFilter" to "Fallback 過濾",
        "Override.Form.FallbackParams" to "Fallback 參數",
        "Override.Form.FallbackSwitch" to "Fallback 開關",
        "Override.Form.FilterList" to "過濾清單",
        "Override.Form.GeoResources" to "GEO 資源開關",
        "Override.Form.GeoUpdateInterval" to "GEO 更新間隔",
        "Override.Form.GeodataMode" to "Geodata 模式",
        "Override.Form.GeoipUrl" to "GeoIP 地址",
        "Override.Form.GeositeMatcher" to "Geosite 匹配器",
        "Override.Form.GeositeUrl" to "GeoSite 地址",
        "Override.Form.GlobalClientFingerprint" to "全域客戶端指紋",
        "Override.Form.Hours" to "小時",
        "Override.Form.HttpPorts" to "HTTP 埠",
        "Override.Form.IncludePackage" to "包含應用程式",
        "Override.Form.Ipv6Timeout" to "IPv6 超時",
        "Override.Form.ItemsConfigured" to "已設定 %d 項",
        "Override.Form.LanAccess" to "區域網路存取",
        "Override.Form.LanAddress" to "區域網地址",
        "Override.Form.MmdbUrl" to "MMDB 地址",
        "Override.Form.NameserverPolicySection" to "策略對映",
        "Override.Form.NetworkPerfParams" to "網路效能參數",
        "Override.Form.NetworkPerfSwitch" to "網路效能開關",
        "Override.Form.NotModify" to "不修改",
        "Override.Form.OpenAdvancedEdit" to "開啟高階編輯",
        "Override.Form.OpenAdvancedEditSummary" to "直接編輯原始物件，用於補充結構化表單未覆蓋的欄位",
        "Override.Form.OutboundInterface" to "出站介面",
        "Override.Form.ProcessMode" to "程序匹配模式",
        "Override.Form.ProxyGroups" to "策略組",
        "Override.Form.ProxyGroupsHint" to "結構化策略組",
        "Override.Form.ProxyNodes" to "代理節點",
        "Override.Form.ProxyNodesHint" to "結構化代理條目",
        "Override.Form.ProxyPorts" to "代理埠",
        "Override.Form.ProxyProviders" to "代理提供者",
        "Override.Form.ProxyProvidersAdvanced" to "需要協議細節、校驗或額外欄位時再進入高階 JSON",
        "Override.Form.ProxyProvidersHint" to "結構化 Provider",
        "Override.Form.ProxyServerNameserverPolicy" to "Proxy Server Nameserver Policy",
        "Override.Form.QuicPorts" to "QUIC 埠",
        "Override.Form.RouteAddress" to "路由網段",
        "Override.Form.RouteExcludeAddress" to "排除路由網段",
        "Override.Form.RoutingMark" to "路由標記",
        "Override.Form.RuleChain" to "規則鏈",
        "Override.Form.RuleChainNotSet" to "未設定規則鏈",
        "Override.Form.RuleProviders" to "規則提供者",
        "Override.Form.RuleProvidersAdvanced" to "需要複雜 Provider 欄位時再進入高階 JSON",
        "Override.Form.RuleProvidersHint" to "結構化 Provider",
        "Override.Form.RunAndLog" to "執行與日誌",
        "Override.Form.RunAndLogExtra" to "執行與日誌補充",
        "Override.Form.SaveFakeIpMapping" to "儲存 Fake-IP 對映",
        "Override.Form.SaveGroupSelection" to "儲存策略組選擇",
        "Override.Form.Seconds" to "秒",
        "Override.Form.SkipAndForce" to "跳過與強制",
        "Override.Form.SkipAuthIPs" to "跳過鑑權網段",
        "Override.Form.SkipDstAddress" to "跳過目標地址",
        "Override.Form.SkipSrcAddress" to "跳過來源地址",
        "Override.Form.SnifferForceDomain" to "強制網域",
        "Override.Form.SnifferOverride" to "覆寫目標",
        "Override.Form.SnifferParsePureIp" to "解析純 IP",
        "Override.Form.SnifferPorts" to "埠",
        "Override.Form.SnifferSkipDomain" to "跳過網域",
        "Override.Form.SnifferSwitch" to "開關",
        "Override.Form.Stack" to "協定堆疊",
        "Override.Form.StrictRoute" to "嚴格路由",
        "Override.Form.StructuredEdit" to "%s · 結構化編輯",
        "Override.Form.SubRules" to "子規則",
        "Override.Form.SubRulesAdvanced" to "複雜子規則結構統一收在高階 JSON 中",
        "Override.Form.SubRulesHint" to "結構化規則組",
        "Override.Form.TcpConcurrent" to "TCP 併發",
        "Override.Form.TlsPorts" to "TLS 埠",
        "Override.Form.TunBasicSwitch" to "基礎開關",
        "Override.Form.TunRouteAndApps" to "路由與應用程式",
        "Override.Form.UnifiedDelay" to "統一延遲",
        "Override.Form.UserAuth" to "使用者驗證",
        "Override.General.AllowLan" to "允許區域網",
        "Override.General.HttpPort" to "HTTP 埠",
        "Override.General.Ipv6" to "IPv6",
        "Override.General.LogLevel" to "日誌等級",
        "Override.General.MixedPort" to "Mixed 埠",
        "Override.General.ProxyMode" to "代理模式",
        "Override.General.RedirectPort" to "Redirect 埠",
        "Override.General.SocksPort" to "SOCKS 埠",
        "Override.General.TproxyPort" to "TProxy 埠",
        "Override.Import.Failed" to "匯入失敗: %s",
        "Override.Import.FileError" to "讀取檔案失敗: %s",
        "Override.Import.ReadError" to "無法讀取匯入檔案",
        "Override.Import.Success" to "已從 %s 匯入 %d 個設定",
        "Override.Import.SuccessDefault" to "已匯入 %d 個設定",
        "Override.Label.CacheAlgorithm" to "快取演算法",
        "Override.Label.Enable" to "啟用",
        "Override.Label.FakeIpRange" to "FakeIP 地址段",
        "Override.Label.ForceDnsMapping" to "強制 DNS 對映",
        "Override.Label.ForceDomain" to "強制嗅探網域",
        "Override.Label.HttpOverride" to "HTTP 覆寫",
        "Override.Label.KeepAliveIdle" to "Keep Alive 空閒閾值",
        "Override.Label.KeepAliveInterval" to "Keep Alive 間隔",
        "Override.Label.OverrideDestination" to "覆寫目標地址",
        "Override.Label.ParsePureIp" to "解析純 IP",
        "Override.Label.QuicOverride" to "QUIC 覆寫",
        "Override.Label.RespectRules" to "遵循路由規則",
        "Override.Label.RulesReplace" to "覆蓋規則",
        "Override.Label.SkipDomain" to "跳過嗅探網域",
        "Override.Label.TlsOverride" to "TLS 覆寫",
        "Override.Label.UseSystemHosts" to "使用系統 Hosts",
        "Override.Modifier.End" to "後置追加",
        "Override.Modifier.Force" to "強制覆蓋",
        "Override.Modifier.ItemsCount" to "%d 項",
        "Override.Modifier.Merge" to "合併",
        "Override.Modifier.NoChanges" to "暫無改動",
        "Override.Modifier.NotModified" to "未修改",
        "Override.Modifier.Replace" to "覆蓋",
        "Override.Modifier.Start" to "前置追加",
        "Override.ProxyGroup.Field.DisableUdp" to "停用 UDP",
        "Override.ProxyGroup.Field.ExcludeFilter" to "排除過濾器",
        "Override.ProxyGroup.Field.ExcludeType" to "排除型別",
        "Override.ProxyGroup.Field.ExpectedStatus" to "期望狀態",
        "Override.ProxyGroup.Field.Filter" to "過濾器",
        "Override.ProxyGroup.Field.Hidden" to "隱藏",
        "Override.ProxyGroup.Field.Icon" to "圖示",
        "Override.ProxyGroup.Field.IncludeAll" to "包含全部",
        "Override.ProxyGroup.Field.IncludeAllProviders" to "包含全部 Provider",
        "Override.ProxyGroup.Field.IncludeAllProxies" to "包含全部代理",
        "Override.ProxyGroup.Field.InterfaceName" to "介面名稱",
        "Override.ProxyGroup.Field.Interval" to "間隔",
        "Override.ProxyGroup.Field.Lazy" to "懶載入",
        "Override.ProxyGroup.Field.MaxFailedTimes" to "最大失敗次數",
        "Override.ProxyGroup.Field.Proxies" to "成員",
        "Override.ProxyGroup.Field.RoutingMark" to "路由標記",
        "Override.ProxyGroup.Field.Timeout" to "超時",
        "Override.ProxyGroup.Field.Url" to "URL",
        "Override.ProxyGroup.Field.Use" to "使用 Provider",
        "Override.ProxyGroup.Field.UseHint" to "每行一個 Provider 名稱",
        "Override.Rule.EmptyWarning" to "規則 #%d 為空",
        "Override.Rule.InvalidFormatWarning" to "規則 #%d 格式可能不正確: %s",
        "Override.Rule.MissingTargetWarning" to "規則 #%d 缺少策略組目標: %s",
        "Override.Save.ApplyFailed" to "覆寫已儲存，但重新套用到目前設定失敗",
        "Override.Save.Failed" to "儲存覆寫設定失敗",
        "Override.Save.ImportDefaultName" to "匯入的覆寫設定",
        "Override.Save.ImportEmpty" to "匯入內容不能為空",
        "Override.Save.PresetNotModifiable" to "系統預設不可修改",
        "Override.Section.Dns.Summary" to "基礎開關、Fake-IP、上游與策略",
        "Override.Section.Dns.Title" to "DNS",
        "Override.Section.General.Summary" to "執行模式、控制器、持久化與 GEO",
        "Override.Section.General.Title" to "全域設定",
        "Override.Section.Inbound.Summary" to "埠、鑑權、區域網路存取",
        "Override.Section.Inbound.Title" to "入站",
        "Override.Section.Proxies.Summary" to "代理節點與協議物件",
        "Override.Section.Proxies.Title" to "出站代理",
        "Override.Section.ProxyGroups.Summary" to "Proxy Groups 前置、覆蓋、後置",
        "Override.Section.ProxyGroups.Title" to "代理組",
        "Override.Section.ProxyProviders.Summary" to "Proxy Providers 合併與覆蓋",
        "Override.Section.ProxyProviders.Title" to "代理集合",
        "Override.Section.RuleProviders.Summary" to "Rule Providers 合併與覆蓋",
        "Override.Section.RuleProviders.Title" to "規則集合",
        "Override.Section.Rules.Summary" to "規則鏈與匹配順序",
        "Override.Section.Rules.Title" to "路由規則",
        "Override.Section.Sniffer.Summary" to "策略開關、協議埠、跳過規則",
        "Override.Section.Sniffer.Title" to "網域嗅探",
        "Override.Section.SubRules.Summary" to "Sub Rules 分組與合併",
        "Override.Section.SubRules.Title" to "子規則",
        "Override.Section.Tun.Summary" to "入站 Tun、路由與應用程式範圍",
        "Override.Section.Tun.Title" to "Tun",
        "Override.Status.InUse" to "使用中",
        "Override.Status.NotInUse" to "未使用",
        "Override.Structured.Proxies.EmptyHint" to "暫無代理節點",
        "Override.Structured.Proxies.ItemLabel" to "代理節點",
        "Override.Structured.Proxies.Title" to "代理節點",
        "Override.Structured.ProxyGroups.EmptyHint" to "暫無策略組",
        "Override.Structured.ProxyGroups.ItemLabel" to "策略組",
        "Override.Structured.ProxyGroups.Title" to "策略組",
        "Override.Structured.ProxyProviders.ItemLabel" to "Provider",
        "Override.Structured.ProxyProviders.Title" to "代理提供者",
        "Override.Structured.RuleProviders.ItemLabel" to "Provider",
        "Override.Structured.RuleProviders.Title" to "規則提供者",
        "Override.Structured.SubRules.ItemLabel" to "子規則組",
        "Override.Structured.SubRules.Title" to "子規則",
        "Override.Title" to "覆寫設定",
        "ProfilesPage.Action.AddProfile" to "新增設定檔",
        "ProfilesPage.Action.UpdateAll" to "一鍵更新所有",
        "ProfilesPage.Button.Cancel" to "取消",
        "ProfilesPage.Button.Confirm" to "確定",
        "ProfilesPage.DeleteDialog.Confirm" to "刪除",
        "ProfilesPage.DeleteDialog.Message" to "確定要刪除「%s」嗎？",
        "ProfilesPage.DeleteDialog.Title" to "刪除設定檔",
        "ProfilesPage.EditDialog.Title" to "編輯設定檔名稱",
        "ProfilesPage.Empty.Hint" to "點選右上角新增設定檔",
        "ProfilesPage.Empty.NoProfiles" to "暫無設定檔",
        "ProfilesPage.Input.NewProfile" to "新設定檔",
        "ProfilesPage.Input.ProfileName" to "設定檔名稱",
        "ProfilesPage.Input.SelectFile" to "點選選擇檔案",
        "ProfilesPage.Input.SubscriptionUrl" to "訂閱連結 (HTTP/HTTPS)",
        "ProfilesPage.Input.SubscriptionUserAgent" to "User-Agent（留空使用全域設定）",
        "ProfilesPage.Kokoro.Account" to "帳號",
        "ProfilesPage.Kokoro.AvatarDescription" to "%s 的 osu! 頭像",
        "ProfilesPage.Kokoro.BandwidthLimit" to "流量上限",
        "ProfilesPage.Kokoro.CheckFailed" to "無法檢查帳號",
        "ProfilesPage.Kokoro.CheckFailedDetail" to "請檢查網路連線，然後再試一次。",
        "ProfilesPage.Kokoro.Checking" to "正在檢查登入狀態…",
        "ProfilesPage.Kokoro.DecreaseUpdateHours" to "縮短配置更新間隔",
        "ProfilesPage.Kokoro.DefaultProfileName" to "Kokoro",
        "ProfilesPage.Kokoro.Direct" to "直連",
        "ProfilesPage.Kokoro.Disabled" to "停用",
        "ProfilesPage.Kokoro.Enabled" to "啟用",
        "ProfilesPage.Kokoro.Expires" to "到期時間",
        "ProfilesPage.Kokoro.Fallback" to "未匹配流量",
        "ProfilesPage.Kokoro.FinalRoute" to "最終路由",
        "ProfilesPage.Kokoro.IncreaseUpdateHours" to "延長配置更新間隔",
        "ProfilesPage.Kokoro.InvalidUpdateHours" to "更新週期必須是正整數小時",
        "ProfilesPage.Kokoro.Isp" to "網路供應商",
        "ProfilesPage.Kokoro.IspAuto" to "自動判斷",
        "ProfilesPage.Kokoro.IspCm" to "中國移動",
        "ProfilesPage.Kokoro.IspCt" to "中國電信",
        "ProfilesPage.Kokoro.IspCu" to "中國聯通",
        "ProfilesPage.Kokoro.IspOther" to "其他",
        "ProfilesPage.Kokoro.KeepFallback" to "保留設定原本的 fallback",
        "ProfilesPage.Kokoro.LoggedIn" to "已登入",
        "ProfilesPage.Kokoro.LoggedInAs" to "已登入為 %s",
        "ProfilesPage.Kokoro.LoggedOut" to "尚未登入",
        "ProfilesPage.Kokoro.Login" to "使用 osu! 登入",
        "ProfilesPage.Kokoro.LoginFailed" to "登入失敗或已取消",
        "ProfilesPage.Kokoro.LoginHint" to "使用 osu! 登入以載入 Proxy Subscription。",
        "ProfilesPage.Kokoro.LoginRequired" to "請先登入並選擇有效的訂閱",
        "ProfilesPage.Kokoro.Logout" to "登出",
        "ProfilesPage.Kokoro.Mirror" to "鏡像站",
        "ProfilesPage.Kokoro.Mode" to "連線模式",
        "ProfilesPage.Kokoro.NoSubscription" to "此帳號沒有有效的 Proxy Subscription。",
        "ProfilesPage.Kokoro.Origin" to "原始站",
        "ProfilesPage.Kokoro.Plan" to "方案",
        "ProfilesPage.Kokoro.ProfileUpdate" to "配置更新間隔",
        "ProfilesPage.Kokoro.Protocol" to "協議",
        "ProfilesPage.Kokoro.Proxy" to "代理",
        "ProfilesPage.Kokoro.Relay" to "中繼",
        "ProfilesPage.Kokoro.Retry" to "重試",
        "ProfilesPage.Kokoro.Routing" to "路由",
        "ProfilesPage.Kokoro.RuleProviderAutoUpdate" to "更新規則提供者",
        "ProfilesPage.Kokoro.RuleProviderAutoUpdateSummary" to "自動更新遠端規則集",
        "ProfilesPage.Kokoro.RuleSource" to "規則來源",
        "ProfilesPage.Kokoro.RuleUpdate" to "遠端規則更新",
        "ProfilesPage.Kokoro.SecureTokenSession" to "Token 已由 Android Keystore 加密保護。",
        "ProfilesPage.Kokoro.SignInFromSettings" to "請先前往「設定 → Kokoro 設定」登入，再新增 Kokoro 訂閱。",
        "ProfilesPage.Kokoro.Subscription" to "訂閱",
        "ProfilesPage.Kokoro.SubscriptionAutoUpdate" to "自動更新配置",
        "ProfilesPage.Kokoro.SubscriptionAutoUpdateSummary" to "依照指定週期重新取得設定檔",
        "ProfilesPage.Kokoro.SubscriptionNumber" to "訂閱 %s",
        "ProfilesPage.Kokoro.Traffic" to "流量",
        "ProfilesPage.Kokoro.TrafficUsed" to "已用流量",
        "ProfilesPage.Kokoro.Unlimited" to "無限",
        "ProfilesPage.Kokoro.UpdateCustom" to "自訂",
        "ProfilesPage.Kokoro.UpdateHours" to "小時（正整數）",
        "ProfilesPage.Kokoro.UpdateHoursRange" to "更新間隔（%s–%s 小時）",
        "ProfilesPage.Kokoro.UpdateHoursValue" to "%s 小時",
        "ProfilesPage.Kokoro.UpdateOff" to "停用",
        "ProfilesPage.Kokoro.UpdateOn" to "每小時",
        "ProfilesPage.Kokoro.Updates" to "更新",
        "ProfilesPage.Kokoro.VmessRelayOnly" to "VMess 固定使用中繼模式",
        "ProfilesPage.LinkSettings.AddLink" to "新增連結",
        "ProfilesPage.LinkSettings.Close" to "關閉",
        "ProfilesPage.LinkSettings.DefaultLink" to "預設連結",
        "ProfilesPage.LinkSettings.DefaultLinkSummary" to "點選左上角快捷按鈕時開啟的連結",
        "ProfilesPage.LinkSettings.EditLink" to "編輯連結",
        "ProfilesPage.LinkSettings.Name" to "名稱",
        "ProfilesPage.LinkSettings.OpenMode" to "開啟方式",
        "ProfilesPage.LinkSettings.OpenModeExternal" to "外部瀏覽器",
        "ProfilesPage.LinkSettings.OpenModeInApp" to "App 內開啟",
        "ProfilesPage.LinkSettings.Title" to "連結設定",
        "ProfilesPage.LinkSettings.Url" to "連結",
        "ProfilesPage.LinkSettings.Validation.EnterName" to "請輸入名稱",
        "ProfilesPage.LinkSettings.Validation.EnterUrl" to "請輸入連結",
        "ProfilesPage.LinkSettings.Validation.InvalidUrl" to "請輸入有效的連結",
        "ProfilesPage.Message.UnknownFile" to "未知檔案",
        "ProfilesPage.Misc.Complete" to "完成",
        "ProfilesPage.Misc.Error" to "錯誤",
        "ProfilesPage.Progress.Downloading" to "下載中...",
        "ProfilesPage.QrScanner.NeedCamera" to "需要相機權限才能掃描 QR Code",
        "ProfilesPage.QrScanner.NeedPermission" to "需要相機權限",
        "ProfilesPage.QrScanner.RecognizeError" to "識別失敗：%s",
        "ProfilesPage.QrScanner.RecognizeFailed" to "未能識別到 QR Code",
        "ProfilesPage.QrScanner.RecognizeSuccess" to "識別成功",
        "ProfilesPage.QrScanner.ScanSuccess" to "掃描成功",
        "ProfilesPage.QrScanner.SelectFromAlbum" to "從相簿選擇 QR Code 圖片",
        "ProfilesPage.SettingsDialog.ChangeLink" to "更改訂閱連結",
        "ProfilesPage.SettingsDialog.ConfigMissing" to "設定檔不存在：%s",
        "ProfilesPage.SettingsDialog.EditProfile" to "編輯設定檔",
        "ProfilesPage.SettingsDialog.EditSettings" to "編輯設定檔",
        "ProfilesPage.SettingsDialog.NoDescription" to "未設定說明",
        "ProfilesPage.SettingsDialog.OpenConfig" to "開啟設定檔",
        "ProfilesPage.SettingsDialog.SaveFailed" to "儲存設定檔失敗",
        "ProfilesPage.SettingsDialog.SystemPreset" to "啟用覆寫設定",
        "ProfilesPage.SettingsDialog.SystemPresetSummary" to "啟用內建覆寫設定",
        "ProfilesPage.SettingsDialog.Title" to "訂閱設定",
        "ProfilesPage.ShareDialog.ImportedConfigMissing" to "匯入的設定檔不存在：%s",
        "ProfilesPage.ShareDialog.NoLink" to "此設定檔沒有訂閱連結",
        "ProfilesPage.ShareDialog.ShareFile" to "分享設定檔",
        "ProfilesPage.ShareDialog.ShareLink" to "分享訂閱連結",
        "ProfilesPage.ShareDialog.Title" to "分享設定檔",
        "ProfilesPage.Sheet.AddTitle" to "新增設定檔",
        "ProfilesPage.Sheet.EditTitle" to "編輯設定檔",
        "ProfilesPage.Title" to "設定檔",
        "ProfilesPage.Type.Kokoro" to "Kokoro 訂閱",
        "ProfilesPage.Type.LocalFile" to "本地檔案",
        "ProfilesPage.Type.QrScan" to "掃描 QR Code 新增",
        "ProfilesPage.Type.Subscription" to "訂閱連結",
        "ProfilesPage.Type.Title" to "設定檔類型",
        "ProfilesPage.Validation.EnterUrl" to "請輸入連結",
        "ProfilesPage.Validation.SelectFile" to "請選擇檔案",
        "ProfilesPage.Validation.YamlOnly" to "僅支援 .yaml 或 .yml 格式的設定檔",
        "ProfilesVM.Error.ProfileNotExist" to "設定檔不存在",
        "ProfilesVM.Message.AddFailed" to "新增設定檔失敗：%s",
        "ProfilesVM.Message.DeleteFailed" to "刪除設定檔失敗：%s",
        "ProfilesVM.Message.ImportFailed" to "匯入設定檔失敗：%s",
        "ProfilesVM.Message.ProfileAdded" to "設定檔已新增：%s",
        "ProfilesVM.Message.ProfileAddedAndActivated" to "設定檔已新增並啟用：%s",
        "ProfilesVM.Message.ProfileDeleted" to "設定檔已刪除",
        "ProfilesVM.Message.ProfileImported" to "設定檔已匯入：%s",
        "ProfilesVM.Message.ProfileUpdated" to "設定檔已更新：%s",
        "ProfilesVM.Message.ToggleFailed" to "切換狀態失敗：%s",
        "ProfilesVM.Message.UpdateFailed" to "更新設定檔失敗：%s",
        "ProfilesVM.Progress.ImportComplete" to "匯入完成",
        "ProfilesVM.Progress.ImportPreparing" to "準備匯入檔案...",
        "ProfilesVM.Progress.Preparing" to "準備下載...",
        "ProfilesVM.Progress.Verifying" to "正在驗證設定檔...",
        "Providers.Action.Operation" to "操作",
        "Providers.Action.Update" to "更新",
        "Providers.Action.UpdateAll" to "更新全部",
        "Providers.Action.Upload" to "上傳",
        "Providers.Empty.NoProviders" to "暫無外部資源",
        "Providers.Empty.NoProvidersHint" to "目前設定未包含外部資源",
        "Providers.Empty.NotRunning" to "代理未啟動",
        "Providers.Empty.NotRunningHint" to "請先啟動代理服務以檢視外部資源",
        "Providers.InfoSummary" to "GeoX 和核心執行時公共檔案位於：%s。規則和代理提供者只會寫入目前設定的私有目錄。",
        "Providers.InfoTitle" to "核心目錄",
        "Providers.Message.AllUpdated" to "全部更新完成",
        "Providers.Message.FetchFailed" to "取得外部資源失敗: %s",
        "Providers.Message.UpdateFailed" to "更新失敗: %s",
        "Providers.Message.UpdateSuccess" to "%s 更新成功",
        "Providers.Message.UploadFailed" to "上傳失敗: %s",
        "Providers.Message.UploadSuccess" to "%s 上傳成功",
        "Providers.ProviderPath" to "資源路徑：%s",
        "Providers.Title" to "外部資源",
        "Providers.Type.ProxyProviders" to "代理提供者 (%d)",
        "Providers.Type.RuleProviders" to "規則提供者 (%d)",
        "Providers.VehicleType.Compatible" to "相容",
        "Providers.VehicleType.File" to "檔案",
        "Providers.VehicleType.Http" to "HTTP",
        "Providers.VehicleType.Inline" to "內聯",
        "Proxy.Action.Sort" to "排序",
        "Proxy.Action.Test" to "測試",
        "Proxy.DisplayMode.DoubleDetailed" to "雙列詳細",
        "Proxy.DisplayMode.DoubleSimple" to "雙列簡潔",
        "Proxy.DisplayMode.SingleDetailed" to "單列詳細",
        "Proxy.DisplayMode.SingleSimple" to "單列簡潔",
        "Proxy.Empty.Hint" to "請在設定檔頁面載入設定檔",
        "Proxy.Empty.NoNodes" to "暫無節點",
        "Proxy.Mode.Direct" to "直連",
        "Proxy.Mode.Global" to "全域",
        "Proxy.Mode.Rule" to "規則",
        "Proxy.Mode.SwitchFailed" to "切換模式失敗：%s",
        "Proxy.Mode.Switched" to "已切換到：%s 模式",
        "Proxy.Mode.Unknown" to "未知",
        "Proxy.Node.Count" to "%d 節點",
        "Proxy.Node.Timeout" to "超時",
        "Proxy.Selection.Error" to "切換失敗：%s",
        "Proxy.Selection.Failed" to "切換失敗",
        "Proxy.Selection.Switched" to "已切換到：%s",
        "Proxy.SortMode.ByLatency" to "按延遲排序",
        "Proxy.SortMode.ByName" to "按名稱排序",
        "Proxy.SortMode.Default" to "預設順序",
        "Proxy.Testing.All" to "正在測試所有節點組...",
        "Proxy.Testing.Failed" to "測試失敗：%s",
        "Proxy.Testing.Group" to "正在測試節點組：%s",
        "Proxy.Testing.InProgress" to "正在測試節點",
        "Proxy.Testing.RequestSent" to "測試請求已傳送",
        "Proxy.Title" to "代理",
        "Proxy.Type.Compatible" to "相容",
        "Proxy.Type.Direct" to "直連",
        "Proxy.Type.Fallback" to "回退",
        "Proxy.Type.LoadBalance" to "負載均衡",
        "Proxy.Type.Pass" to "透傳",
        "Proxy.Type.Reject" to "拒絕",
        "Proxy.Type.RejectDrop" to "丟棄",
        "Proxy.Type.Relay" to "中繼",
        "Proxy.Type.Selector" to "選擇器",
        "Proxy.Type.Smart" to "智慧",
        "Proxy.Type.Unknown" to "未知",
        "Proxy.Type.UrlTest" to "UrlTest",
        "Service.AutoRestart.ChannelDescription" to "用於自動重啟代理服務",
        "Service.AutoRestart.ChannelName" to "自動重啟服務",
        "Service.AutoRestart.Checking" to "正在檢查自動啟動...",
        "Service.Notification.Running" to "執行中",
        "Service.Notification.SpeedFormat" to "下行 %s  上行 %s",
        "Service.Notification.TodayTrafficFormat" to "今日流量 %s",
        "Service.Notification.TrafficFormat" to "總計：%s",
        "Service.Notification.UnknownProfile" to "未知設定",
        "Service.Tile.ClickToOpen" to "點選開啟應用程式",
        "Service.Tile.ClickToStartProxy" to "啟動代理",
        "Service.Tile.ClickToStopProxy" to "停止代理",
        "Service.Tile.Connecting" to "正在連線...",
        "Service.Tile.Disconnecting" to "正在斷開...",
        "Settings.DataSettings.AppDataManagement" to "應用程式資料管理",
        "Settings.DataSettings.AppDataManagementSummary" to "清理部分快取檔案",
        "Settings.DataSettings.ExportBackup" to "匯出備份",
        "Settings.DataSettings.ExportBackupSummary" to "將使用者設定儲存為 JSON 檔案",
        "Settings.DataSettings.ImportBackup" to "匯入備份",
        "Settings.DataSettings.ImportBackupSummary" to "從備份檔案還原使用者設定",
        "Settings.Error.WebviewFailed" to "無法開啟 WebView：%s",
        "Settings.Kokoro.CustomRules" to "自訂規則",
        "Settings.Kokoro.CustomRulesSummary" to "編輯套用於產生設定檔的 default 規則",
        "Settings.Kokoro.Summary" to "帳戶 · 自訂規則",
        "Settings.Kokoro.Title" to "Kokoro 設定",
        "Settings.More.About" to "關於",
        "Settings.More.AboutSummary" to "版本與許可",
        "Settings.More.Logs" to "日誌",
        "Settings.More.LogsSummary" to "執行日誌",
        "Settings.NetworkSettings.Lab" to "實驗室",
        "Settings.NetworkSettings.LabSummary" to "節點測試",
        "Settings.NetworkSettings.MetaFeatures" to "Meta 功能",
        "Settings.NetworkSettings.MetaFeaturesSummary" to "Meta 擴充套件",
        "Settings.NetworkSettings.Network" to "網路",
        "Settings.NetworkSettings.NetworkSummary" to "DNS · 埠 · 入站",
        "Settings.NetworkSettings.Override" to "覆寫",
        "Settings.NetworkSettings.OverrideSummary" to "規則覆寫",
        "Settings.Section.DataSettings" to "資料設定",
        "Settings.Section.Kokoro" to "Kokoro",
        "Settings.Section.More" to "更多",
        "Settings.Section.NetworkSettings" to "網路設定",
        "Settings.Section.UiSettings" to "介面設定",
        "Settings.Title" to "設定",
        "Settings.UiSettings.App" to "應用程式",
        "Settings.UiSettings.AppSummary" to "外觀 · 語言 · 主題",
        "TrafficStatistics.Action.Clear" to "清空統計",
        "TrafficStatistics.Action.ClearConfirmMessage" to "清空後將無法復原所有流量統計資料。",
        "TrafficStatistics.Action.ClearSuccess" to "統計資料已清空",
        "TrafficStatistics.Chart.Daily" to "按天",
        "TrafficStatistics.Chart.Hourly" to "4 小時",
        "TrafficStatistics.Compare.LessThanYesterday" to "較昨日 %s",
        "TrafficStatistics.Compare.MoreThanYesterday" to "較昨日 +%s",
        "TrafficStatistics.Compare.SameAsYesterday" to "與昨日持平",
        "TrafficStatistics.Compare.WeekStats" to "近 7 天統計",
        "TrafficStatistics.Donut.Other" to "其他",
        "TrafficStatistics.EntrySummary" to "檢視流量使用情況",
        "TrafficStatistics.Metric.Download" to "下行",
        "TrafficStatistics.Metric.Upload" to "上行",
        "TrafficStatistics.Metric.UsageLine" to "下行 %s  上行 %s",
        "TrafficStatistics.Section.EmptyApps" to "暫無應用程式流量統計",
        "TrafficStatistics.Section.TopApps" to "高流量應用程式",
        "TrafficStatistics.Section.Traffic" to "流量",
        "TrafficStatistics.Summary.TodayTraffic" to "今日流量",
        "TrafficStatistics.Summary.WeekTraffic" to "本週流量",
        "TrafficStatistics.TimeRange.Today" to "今日",
        "TrafficStatistics.TimeRange.Week" to "本週",
        "TrafficStatistics.Title" to "流量統計",
        "Util.Error.UnknownError" to "未知錯誤",
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
