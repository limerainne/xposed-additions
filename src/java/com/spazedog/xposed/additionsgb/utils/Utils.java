/*
 * This file is part of the Xposed Additions Project: https://github.com/spazedog/xposed-additions
 *  
 * Copyright (c) 2015 Daniel Bergløv
 *
 * Xposed Additions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Xposed Additions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Xposed Additions. If not, see <http://www.gnu.org/licenses/>
 */

package com.spazedog.xposed.additionsgb.utils;

import android.content.Context;
import android.util.Log;

import com.spazedog.xposed.additionsgb.backend.service.BackendServiceMgr;

import java.io.File;

public final class Utils {
	
	private static int oDebugEnabled = 0;
    private static int oUserId = -1;
	
	public enum Level {INFO, DEBUG, WARNING, ERROR}
	
	public static void log(Level level, String tag, String message) {
		log(level, tag, message, null);
	}
	
	public static void log(Level level, String tag, String message, Throwable e) {
		if (level == Level.DEBUG && !debugEnabled()) {
			/*
			 * If we have a DEBUG log before the service is ready, 
			 * transform it into an INFO log instead. 
			 */
			if (oDebugEnabled != 0) {
				return;
			}
			
			level = Level.INFO;
		}
		
		switch (level) {
			case DEBUG: Log.d(tag, message, e); break;
			case WARNING: Log.w(tag, message, e); break;
			case ERROR: Log.e(tag, message, e); break;
			default: Log.i(tag, message, e);
		}
	}
	
	public static boolean debugEnabled() {
		/*
		 * We do not want to check this value against the service every time. 
		 * Only do it until the service is ready and we can get the preference for debug settings. 
		 */
		if (oDebugEnabled == 0) {
			/*
			 * Avoid recursive calls when we start calling the service
			 */
			oDebugEnabled = -1;
			
			BackendServiceMgr backenedMgr = BackendServiceMgr.getInstance(true);
			
			if (backenedMgr != null && backenedMgr.isServiceReady()) {
				oDebugEnabled = backenedMgr.isDebugEnabled() ? 1 : -1;
				
			} else {
				oDebugEnabled = 0;
			}
		}
		
		return oDebugEnabled == 1;
	}

    public static boolean isOwner(Context context) {
        if (oUserId == -1) {
            try {
                /*
                 * /data/data/<package> on version without multi user support, always the owner
                 * /data/user/<userId>/<package> on newer Android versions, we want <userId>
                 */
                oUserId = Integer.valueOf(new File(context.getApplicationInfo().dataDir).getParentFile().getName());

            } catch (NumberFormatException e) {
                /*
                 * We hit an older Android version, owner is all we have
                 */
                oUserId = 0;
            }
        }

        return oUserId == 0;
    }

    public static ClassLoader getAppClassLoader() {
        return Utils.class.getClassLoader();
    }
}
