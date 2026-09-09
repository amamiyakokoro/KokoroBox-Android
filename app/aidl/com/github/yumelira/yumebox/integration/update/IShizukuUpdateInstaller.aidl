package com.github.yumelira.yumebox.integration.update;

import android.os.ParcelFileDescriptor;

interface IShizukuUpdateInstaller {
    void destroy() = 16777114; // Reserved transaction code required by Shizuku UserService.
    String install(in ParcelFileDescriptor apk, long length) = 1;
}
