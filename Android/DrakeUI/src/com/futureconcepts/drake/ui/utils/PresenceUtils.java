/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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
package com.futureconcepts.drake.ui.utils;

import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Presence;
import com.futureconcepts.drake.ui.R;

import android.util.Log;

public final class PresenceUtils
{
	private static final String LOG_TAG = PresenceUtils.class.getSimpleName();
    private PresenceUtils() {}

    public static int convertStatus(int status)
    {
        switch (status) {
        case Presence.AVAILABLE:
            return Imps.Presence.AVAILABLE;

        case Presence.AWAY:
            return Imps.Presence.AWAY;

        case Presence.DO_NOT_DISTURB:
            return Imps.Presence.DO_NOT_DISTURB;

        case Presence.IDLE:
            return Imps.Presence.IDLE;

        case Presence.OFFLINE:
            return Imps.Presence.OFFLINE;

        default:
            Log.w(LOG_TAG, "Unknown presence status " + status);
            return Imps.Presence.AVAILABLE;
        }
    }

    public static int getStatusStringRes(int status)
    {
        switch (status) {
        case Imps.Presence.AVAILABLE:
            return R.string.presence_available;

        case Imps.Presence.AWAY:
            return R.string.presence_away;

        case Imps.Presence.DO_NOT_DISTURB:
            return R.string.presence_busy;

        case Imps.Presence.IDLE:
            return R.string.presence_idle;

        case Imps.Presence.INVISIBLE:
            return R.string.presence_invisible;

        case Imps.Presence.OFFLINE:
            return R.string.presence_offline;

        default:
            return R.string.presence_available;
        }
    }

    public static int getStatusIconId(int status)
    {
        switch (status) {
        case Imps.Presence.AVAILABLE:
            return android.R.drawable.presence_online;

        case Imps.Presence.IDLE:
            return android.R.drawable.presence_away;

        case Imps.Presence.AWAY:
            return android.R.drawable.presence_away;

        case Imps.Presence.DO_NOT_DISTURB:
            return android.R.drawable.presence_invisible;

        case Imps.Presence.INVISIBLE:
            return android.R.drawable.presence_invisible;

        default:
            return android.R.drawable.presence_offline;
        }
    }
}
