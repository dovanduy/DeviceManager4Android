/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wuba.device;

import javax.swing.text.html.Option;

/**
 * Container for {@link ITestDevice} {@link Option}s
 */
public class TestDeviceOptions {

    private boolean mEnableAdbRoot = true;

    private boolean mDisableKeyguard = true;

    private String mDisableKeyguardCmd = "input keyevent 82";

    private long mMaxLogcatDataSize = 20 * 1024 * 1024;

    private int mFastbootTimeout = 1 * 60 * 1000;

    private int mAdbRecoveryTimeout = 1 * 60 * 1000;

    private int mRebootTimeout = 2 * 60 * 1000;

    private boolean mUseFastbootErase = false;

    private int mUnencryptRebootTimeout = 0;

    private long mOnlineTimeout = 1 * 60 * 1000;

    private long mAvailableTimeout = 6 * 60 * 1000;

    private String mCommPort = null;
    
    
    public String getCommPort() {
		return mCommPort;
	}

	public void setCommPort(String commPort) {
		mCommPort = commPort;
	}

	/**
     * Check whether adb root should be enabled on boot for this device
     */
    public boolean isEnableAdbRoot() {
        return mEnableAdbRoot;
    }

    /**
     * Set whether adb root should be enabled on boot for this device
     */
    public void setEnableAdbRoot(boolean enableAdbRoot) {
        mEnableAdbRoot = enableAdbRoot;
    }

    /**
     * Check whether or not we should attempt to disable the keyguard once boot has completed
     */
    public boolean isDisableKeyguard() {
        return mDisableKeyguard;
    }

    /**
     * Set whether or not we should attempt to disable the keyguard once boot has completed
     */
    public void setDisableKeyguard(boolean disableKeyguard) {
        mDisableKeyguard = disableKeyguard;
    }

    /**
     * Fetch the command to disable the keyguard
     */
    public String getDisableKeyguardCmd() {
        return mDisableKeyguardCmd;
    }

    /**
     * Set the command to be used to disable the keyguard
     */
    public void setDisableKeyguardCmd(String disableKeyguardCmd) {
        mDisableKeyguardCmd = disableKeyguardCmd;
    }

    /**
     * Get the approximate maximum size of a tmp logcat data to retain, in bytes.
     */
    public long getMaxLogcatDataSize() {
        return mMaxLogcatDataSize;
    }

    /**
     * Set the approximate maximum size of a tmp logcat to retain, in bytes
     */
    public void setMaxLogcatDataSize(long maxLogcatDataSize) {
        mMaxLogcatDataSize = maxLogcatDataSize;
    }

    /**
     * @return the timeout to boot into fastboot mode in msecs.
     */
    public int getFastbootTimeout() {
        return mFastbootTimeout;
    }

    /**
     * @param fastbootTimeout the timout in msecs to boot into fastboot mode.
     */
    public void setFastbootTimeout(int fastbootTimeout) {
        mFastbootTimeout = fastbootTimeout;
    }

    /**
     * @return the timeout in msecs to boot into recovery mode.
     */
    public int getAdbRecoveryTimeout() {
        return mAdbRecoveryTimeout;
    }

    /**
     * @param adbRecoveryTimeout the timeout in msecs to boot into recovery mode.
     */
    public void setAdbRecoveryTimeout(int adbRecoveryTimeout) {
        mAdbRecoveryTimeout = adbRecoveryTimeout;
    }

    /**
     * @return the timeout in msecs for the full system boot.
     */
    public int getRebootTimeout() {
        return mRebootTimeout;
    }

    /**
     * @param rebootTimeout the timeout in msecs for the system to fully boot.
     */
    public void setRebootTimeout(int rebootTimeout) {
        mRebootTimeout = rebootTimeout;
    }

    /**
     * @return whether to use fastboot erase instead of fastboot format to wipe partitions.
     */
    public boolean getUseFastbootErase() {
        return mUseFastbootErase;
    }

    /**
     * @param useFastbootErase whether to use fastboot erase instead of fastboot format to wipe
     * partitions.
     */
    public void setUseFastbootErase(boolean useFastbootErase) {
        mUseFastbootErase = useFastbootErase;
    }

    /**
     * @return the timeout in msecs for the filesystem to be formatted and the device to reboot
     * after unencryption.
     */
    public int getUnencryptRebootTimeout() {
        return mUnencryptRebootTimeout;
    }

    /**
     * @param unencryptRebootTimeout the timeout in msecs for the filesystem to be formatted and
     * the device to reboot after unencryption.
     */
    public void setUnencryptRebootTimeout(int unencryptRebootTimeout) {
        mUnencryptRebootTimeout = unencryptRebootTimeout;
    }

    /**
     * @return the default time in ms to to wait for a device to be online.
     */
    public long getOnlineTimeout() {
        return mOnlineTimeout;
    }

    public void setOnlineTimeout(long onlineTimeout) {
        mOnlineTimeout = onlineTimeout;
    }

    /**
     * @return the default time in ms to to wait for a device to be available.
     */
    public long getAvailableTimeout() {
        return mAvailableTimeout;
    }
}
