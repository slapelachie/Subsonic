/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */

package xyz.slapelachie.supersonic.service.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Scott on 8/28/13.
 */

public class StarredSyncService extends Service {
	private static StarredSyncAdapter starredSyncAdapter;
	private static final Object syncLock = new Object();

	@Override
	public void onCreate() {
		synchronized (syncLock) {
			if(starredSyncAdapter == null) {
				starredSyncAdapter = new StarredSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return starredSyncAdapter.getSyncAdapterBinder();

	}
}
