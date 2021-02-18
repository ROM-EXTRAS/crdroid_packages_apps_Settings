/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.display;

import static android.provider.Settings.Secure.CAMERA_AUTOROTATE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.testutils.FakeFeatureFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class SmartAutoRotatePreferenceControllerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Context mContext;
    @Mock
    private PackageManager mPackageManager;
    private ContentResolver mContentResolver;
    private SmartAutoRotatePreferenceController mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        FakeFeatureFactory.setupForTest();
        mContentResolver = RuntimeEnvironment.application.getContentResolver();
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
        when(mContext.getContentResolver()).thenReturn(mContentResolver);
        when(mContext.getString(R.string.auto_rotate_option_off))
                .thenReturn("Off");
        when(mContext.getString(R.string.auto_rotate_option_on))
                .thenReturn("On");
        when(mContext.getString(R.string.auto_rotate_option_face_based))
                .thenReturn("On - Face-based");

        disableCameraBasedRotation();

        mController = new SmartAutoRotatePreferenceController(mContext, "smart_auto_rotate");
    }

    @Test
    public void isAvailableWhenPolicyAllows() {
        assertThat(mController.isAvailable()).isFalse();

        enableAutoRotationPreference();

        assertThat(mController.isAvailable()).isTrue();
    }

    @Test
    public void updatePreference_settingsIsOff_shouldTurnOffToggle() {
        disableAutoRotation();

        assertThat(mController.getSummary()).isEqualTo("Off");
    }

    @Test
    public void updatePreference_settingsIsOn_shouldTurnOnToggle() {
        enableAutoRotation();

        assertThat(mController.getSummary()).isEqualTo("On");
    }

    @Test
    public void updatePreference_settingsIsCameraBased_shouldTurnOnToggle() {
        enableCameraBasedRotation();
        enableAutoRotation();

        assertThat(mController.getSummary()).isEqualTo("On - Face-based");

        disableAutoRotation();

        assertThat(mController.getSummary()).isEqualTo("Off");
    }

    @Test
    public void testGetAvailabilityStatus() {
        assertThat(mController.getAvailabilityStatus()).isEqualTo(BasePreferenceController
                .UNSUPPORTED_ON_DEVICE);

        enableAutoRotationPreference();

        assertThat(mController.getAvailabilityStatus()).isEqualTo(BasePreferenceController
                .AVAILABLE);

        disableAutoRotationPreference();

        assertThat(mController.getAvailabilityStatus()).isEqualTo(BasePreferenceController
                .UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void isSliceableCorrectKey_returnsTrue() {
        final AutoRotatePreferenceController controller =
                new AutoRotatePreferenceController(mContext, "auto_rotate");
        assertThat(controller.isSliceable()).isTrue();
    }

    @Test
    public void isSliceableIncorrectKey_returnsFalse() {
        final AutoRotatePreferenceController controller =
                new AutoRotatePreferenceController(mContext, "bad_key");
        assertThat(controller.isSliceable()).isFalse();
    }

    private void enableAutoRotationPreference() {
        when(mPackageManager.hasSystemFeature(anyString())).thenReturn(true);
        when(mContext.getResources().getBoolean(anyInt())).thenReturn(true);
        Settings.System.putInt(mContentResolver,
                Settings.System.HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY, 0);
    }

    private void disableAutoRotationPreference() {
        when(mPackageManager.hasSystemFeature(anyString())).thenReturn(true);
        when(mContext.getResources().getBoolean(anyInt())).thenReturn(true);
        Settings.System.putInt(mContentResolver,
                Settings.System.HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY, 1);
    }

    private void enableAutoRotation() {
        Settings.System.putIntForUser(mContentResolver,
                Settings.System.ACCELEROMETER_ROTATION, 1, UserHandle.USER_CURRENT);
    }

    private void disableAutoRotation() {
        Settings.System.putIntForUser(mContentResolver,
                Settings.System.ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT);
    }

    private void enableCameraBasedRotation() {
        Settings.Secure.putIntForUser(mContentResolver,
                CAMERA_AUTOROTATE, 1, UserHandle.USER_CURRENT);
    }

    private void disableCameraBasedRotation() {
        Settings.Secure.putIntForUser(mContentResolver,
                CAMERA_AUTOROTATE, 0, UserHandle.USER_CURRENT);
    }
}