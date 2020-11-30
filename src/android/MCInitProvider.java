/**
 * Copyright 2018 Salesforce, Inc
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.marketingcloud.cordova;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.salesforce.marketingcloud.InitializationStatus;
import com.salesforce.marketingcloud.MCLogListener;
import com.salesforce.marketingcloud.MarketingCloudConfig;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.registration.RegistrationManager;

public class MCInitProvider
    extends ContentProvider implements MarketingCloudSdk.InitializationListener {
    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        if (ctx != null) {
            MarketingCloudConfig.Builder builder = MCSdkConfig.prepareConfigBuilder(ctx);
            if (builder != null) {
                builder.setUrlHandler(MCSdkListener.INSTANCE);
                MarketingCloudSdk.init(ctx, builder.build(ctx), this);
            }
            
            /* create the channel for our notifications. We don't call 
               com.salesforce.marketingcloud.notifications.NotificationManager.createDefaultNotificationChannel
               because we want to set slightly different parameters. Most important is the name: CogniFit instead of "Marketing"
            */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            	String packageName = ctx.getPackageName();
                String channelName = "App";
                try {
                    Bundle metaData = ctx.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData;
                    channelName = metaData.getString("com.cognifit.channel_name");
                } catch (PackageManager.NameNotFoundException e) {
                    channelName = "App";
                }
                
                NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel marketingCloudChannel = new NotificationChannel("com.salesforce.marketingcloud.DEFAULT_CHANNEL", channelName, android.app.NotificationManager.IMPORTANCE_HIGH);
                marketingCloudChannel.enableLights(false);
                marketingCloudChannel.enableVibration(false);
                marketingCloudChannel.setShowBadge(true);
                marketingCloudChannel.setLockscreenVisibility(1);
                notificationManager.createNotificationChannel(marketingCloudChannel);
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(
        @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
        @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public void complete(@NonNull InitializationStatus status) {
        if (status.isUsable()) {
            MarketingCloudSdk.requestSdk(new MarketingCloudSdk.WhenReadyListener() {
                @Override
                public void ready(@NonNull MarketingCloudSdk marketingCloudSdk) {
                    RegistrationManager registrationManager =
                        marketingCloudSdk.getRegistrationManager();
                    registrationManager.edit().addTag("Cordova").commit();
                }
            });
        }
    }
}
