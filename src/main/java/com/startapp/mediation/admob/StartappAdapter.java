/*
 * Copyright 2020 Start.io Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * if you take (copy/paste) this file to your own project
 * change this package path to your own as well
 */
package com.startapp.mediation.admob;

import static com.startapp.mediation.admob.BuildConfig.DEBUG;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.google.android.gms.ads.mediation.MediationNativeAdCallback;
import com.google.android.gms.ads.mediation.MediationNativeAdConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import com.google.android.gms.ads.VersionInfo;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.startapp.sdk.ads.banner.BannerCreator;
import com.startapp.sdk.ads.banner.BannerFormat;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.BannerRequest;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdDisplayListener;
import com.startapp.sdk.ads.nativead.NativeAdInterface;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Keep
public class StartappAdapter extends Adapter {
    private static final String LOG_TAG = StartappAdapter.class.getSimpleName();

    //region Extras
    public enum Mode {
        OFFERWALL,
        VIDEO,
        OVERLAY
    }

    public enum Size {
        SIZE72X72,
        SIZE100X100,
        SIZE150X150,
        SIZE340X340,
        SIZE1200X628
    }

    public static class Extras {
        private static final String AD_TAG = "adTag";
        private static final String INTERSTITIAL_MODE = "interstitialMode";
        private static final String MIN_CPM = "minCPM";
        private static final String MUTE_VIDEO = "muteVideo";
        private static final String IS_3D_BANNER = "is3DBanner";
        private static final String NATIVE_IMAGE_SIZE = "nativeImageSize";
        private static final String NATIVE_SECONDARY_IMAGE_SIZE = "nativeSecondaryImageSize";
        private static final String APP_ID = "startappAppId";

        @NonNull
        private final AdPreferences adPreferences;

        @NonNull
        AdPreferences getAdPreferences() {
            return adPreferences;
        }

        private boolean is3DBanner;

        boolean is3DBanner() {
            return is3DBanner;
        }

        @Nullable
        private StartAppAd.AdMode adMode;

        @Nullable
        StartAppAd.AdMode getAdMode() {
            return adMode;
        }

        @Nullable
        private String appId;

        @Nullable
        public String getAppId() {
            return appId;
        }

        Extras(
                @NonNull Bundle customEventExtras,
                @NonNull Bundle serverParameter
        ) {
            adPreferences = makeAdPreferences(customEventExtras, serverParameter, false, null);
        }

        Extras(
                @NonNull NativeAdOptions nativeAdOptions,
                @NonNull Bundle customEventExtras,
                @NonNull Bundle serverParameter
        ) {
            adPreferences = makeAdPreferences(customEventExtras, serverParameter, true, nativeAdOptions);
        }

        @NonNull
        private AdPreferences makeAdPreferences(
                @NonNull Bundle customEventExtras,
                @NonNull Bundle serverParameter,
                boolean isNative,
                @Nullable NativeAdOptions nativeAdOptions
        ) {
            String adTag;
            boolean isVideoMuted;
            Double minCPM = null;
            StartappAdapter.Size nativeImageSize = null;
            StartappAdapter.Size nativeSecondaryImageSize = null;

            adTag = customEventExtras.getString(AD_TAG);
            isVideoMuted = customEventExtras.getBoolean(MUTE_VIDEO);
            is3DBanner = customEventExtras.getBoolean(IS_3D_BANNER);

            if (customEventExtras.containsKey(MIN_CPM)) {
                minCPM = customEventExtras.getDouble(MIN_CPM);
            }

            if (customEventExtras.containsKey(INTERSTITIAL_MODE)) {
                final Mode srcAdMode = (Mode) customEventExtras.getSerializable(INTERSTITIAL_MODE);
                if (srcAdMode != null) {
                    switch (srcAdMode) {
                        case OVERLAY:
                            adMode = StartAppAd.AdMode.OVERLAY;
                            break;
                        case VIDEO:
                            adMode = StartAppAd.AdMode.VIDEO;
                            break;
                        case OFFERWALL:
                            adMode = StartAppAd.AdMode.OFFERWALL;
                            break;
                    }
                }
            }

            if (customEventExtras.containsKey(NATIVE_IMAGE_SIZE)) {
                nativeImageSize = (StartappAdapter.Size) customEventExtras.getSerializable(NATIVE_IMAGE_SIZE);
            }

            if (customEventExtras.containsKey(NATIVE_SECONDARY_IMAGE_SIZE)) {
                nativeSecondaryImageSize = (StartappAdapter.Size) customEventExtras.getSerializable(NATIVE_SECONDARY_IMAGE_SIZE);
            }

            String jsonParameter = serverParameter.getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
            if (jsonParameter != null) {
                try {
                    JSONObject json = new JSONObject(jsonParameter);
                    Log.v(LOG_TAG, "Start.io server parameter:" + json);

                    if (json.has(AD_TAG)) {
                        adTag = json.getString(AD_TAG);
                    }

                    if (json.has(MUTE_VIDEO)) {
                        isVideoMuted = json.getBoolean(MUTE_VIDEO);
                    }

                    if (json.has(IS_3D_BANNER)) {
                        is3DBanner = json.getBoolean(IS_3D_BANNER);
                    }

                    if (json.has(MIN_CPM)) {
                        minCPM = json.getDouble(MIN_CPM);
                    }

                    if (json.has(NATIVE_IMAGE_SIZE)) {
                        String name = json.getString(NATIVE_IMAGE_SIZE);
                        try {
                            nativeImageSize = StartappAdapter.Size.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(LOG_TAG, "Could not parse imageSize parameter: " + name);
                        }
                    }

                    if (json.has(NATIVE_SECONDARY_IMAGE_SIZE)) {
                        String name = json.getString(NATIVE_SECONDARY_IMAGE_SIZE);
                        try {
                            nativeSecondaryImageSize = StartappAdapter.Size.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(LOG_TAG, "Could not parse secondaryImageSize parameter: " + name);
                        }
                    }

                    if (json.has(INTERSTITIAL_MODE)) {
                        String mode = json.getString(INTERSTITIAL_MODE);
                        switch (mode) {
                            case "OVERLAY":
                                adMode = StartAppAd.AdMode.OVERLAY;
                                break;
                            case "VIDEO":
                                adMode = StartAppAd.AdMode.VIDEO;
                                break;
                            case "OFFERWALL":
                                adMode = StartAppAd.AdMode.OFFERWALL;
                                break;
                        }
                    }

                    if (json.has(APP_ID)) {
                        appId = json.getString(APP_ID);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Could not parse malformed JSON: " + jsonParameter);
                }
            }

            NativeAdPreferences nativeAdPrefs = null;
            AdPreferences prefs;
            if (isNative) {
                nativeAdPrefs = new NativeAdPreferences();
                prefs = nativeAdPrefs;
            } else {
                prefs = new AdPreferences();
            }

            prefs.setAdTag(adTag);
            prefs.setMinCpm(minCPM);

            if (isVideoMuted) {
                prefs.muteVideo();
            }

            if (isNative) {
                if (nativeImageSize != null) {
                    nativeAdPrefs.setPrimaryImageSize(nativeImageSize.ordinal());
                }

                if (nativeSecondaryImageSize != null) {
                    nativeAdPrefs.setSecondaryImageSize(nativeSecondaryImageSize.ordinal());
                }

                nativeAdPrefs.setAutoBitmapDownload(nativeAdOptions != null && !nativeAdOptions.shouldReturnUrlsForImageAssets());
            }

            return prefs;
        }

        public static class Builder {
            @NonNull
            final Bundle extras = new Bundle();

            @NonNull
            public Builder setAdTag(@NonNull String adTag) {
                extras.putString(AD_TAG, adTag);
                return this;
            }

            @NonNull
            public Builder setInterstitialMode(@NonNull Mode interstitialMode) {
                extras.putSerializable(INTERSTITIAL_MODE, interstitialMode);
                return this;
            }

            @NonNull
            public Builder setMinCPM(double cpm) {
                extras.putDouble(MIN_CPM, cpm);
                return this;
            }

            @NonNull
            public Builder setNativeImageSize(@NonNull StartappAdapter.Size size) {
                extras.putSerializable(NATIVE_IMAGE_SIZE, size);
                return this;
            }

            @NonNull
            public Builder setNativeSecondaryImageSize(@NonNull StartappAdapter.Size size) {
                extras.putSerializable(NATIVE_SECONDARY_IMAGE_SIZE, size);
                return this;
            }

            @NonNull
            public Builder muteVideo() {
                extras.putBoolean(MUTE_VIDEO, true);
                return this;
            }

            @NonNull
            public Builder enable3DBanner() {
                extras.putBoolean(IS_3D_BANNER, true);
                return this;
            }

            @NonNull
            public Bundle toBundle() {
                return extras;
            }
        }
    }
    //endregion

    //region Utils
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private static void initializeIfNecessary(@NonNull Context context, @Nullable String appId) {
        if (TextUtils.isEmpty(appId)) {
            Log.e("start.io SDK", "App ID not found\n" +
                    "+-----------------------------------------------------------------------+\n" +
                    "|                S   T   A   R   T   .   I   O                          |\n" +
                    "| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |\n" +
                    "| AdMob Mediation is configured wrong, App ID not found                 |\n" +
                    "| Put your App ID as Parameter for Custom Event:                        |\n" +
                    "|                                                                       |\n" +
                    "|              { startappAppId : 'YOUR_APP_ID' }                        |\n" +
                    "|                                                                       |\n" +
                    "| https://support.start.io/hc/en-us/articles/360005100893-AdMob-Adapter |\n" +
                    "+-----------------------------------------------------------------------+\n");
            return;
        }

        if (!isInitialized.getAndSet(true)) {
            StartAppAd.enableConsent(context, false);
            StartAppSDK.enableMediationMode(context, "AdMob", BuildConfig.VERSION_NAME);
            StartAppSDK.init(context, appId, false);
        }
    }

    private static void initTestAds(@NonNull MediationAdConfiguration config) {
        if (config.isTestRequest()) {
            StartAppSDK.setTestAdsEnabled(true);
        }
    }
    //endregion

    //region Interstitial
    @Nullable
    private StartAppAd interstitial;

    @Nullable
    private MediationInterstitialAdCallback interstitialListener;

    @Override
    public void loadInterstitialAd(
            @NonNull MediationInterstitialAdConfiguration config,
            @NonNull final MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback
    ) {
        Extras extras = new Extras(config.getMediationExtras(), config.getServerParameters());
        initializeIfNecessary(config.getContext(), extras.getAppId());
        initTestAds(config);

        interstitial = new StartAppAd(config.getContext());
        AdEventListener loadListener = new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                interstitialListener = callback.onSuccess(new MediationInterstitialAd() {
                    @Override
                    public void showAd(@NonNull Context context) {
                        if (interstitial == null) {
                            return;
                        }

                        interstitial.showAd(new AdDisplayListener() {
                            @Override
                            public void adHidden(Ad ad) {
                                if (interstitialListener != null) {
                                    interstitialListener.onAdClosed();
                                }
                            }

                            @Override
                            public void adDisplayed(Ad ad) {
                                if (interstitialListener != null) {
                                    interstitialListener.onAdOpened();
                                    interstitialListener.reportAdImpression();
                                }
                            }

                            @Override
                            public void adClicked(Ad ad) {
                                if (interstitialListener != null) {
                                    interstitialListener.reportAdClicked();
                                }
                            }

                            @Override
                            public void adNotDisplayed(Ad ad) {
                                if (interstitialListener != null) {
                                    String message = ad != null ? ad.getErrorMessage() : "ad is null";
                                    interstitialListener.onAdFailedToShow(messageToError(message));
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                String message = ad != null ? ad.getErrorMessage() : "ad is null";
                Log.v(LOG_TAG, "ad loading failed: " + message);
                callback.onFailure(messageToError(message));
            }
        };

        if (extras.getAdMode() == null) {
            interstitial.loadAd(extras.getAdPreferences(), loadListener);
        } else {
            interstitial.loadAd(extras.getAdMode(), extras.getAdPreferences(), loadListener);
        }
    }
    //endregion

    //region Banner
    @Override
    public void loadBannerAd(
            @NonNull MediationBannerAdConfiguration config,
            @NonNull final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback
    ) {
        final Context context = config.getContext();
        final int adWidthDp, adHeightDp;

        AdSize adSize = config.getAdSize();
        if (adSize.getWidth() > 0 && adSize.getHeight() > 0) {
            adWidthDp = adSize.getWidth();
            adHeightDp = adSize.getHeight();
        } else {
            int adWidthPx = adSize.getWidthInPixels(context);
            int adHeightPx = adSize.getHeightInPixels(context);

            if (adWidthPx > 0 && adHeightPx > 0) {
                float density = context.getResources().getDisplayMetrics().density;
                if (density > 0) {
                    adWidthDp = (int) Math.ceil(adWidthPx / density);
                    adHeightDp = (int) Math.ceil(adHeightPx / density);
                } else {
                    callback.onFailure(messageToError(null));
                    return;
                }
            } else {
                callback.onFailure(messageToError(adSize + " is not supported"));
                return;
            }
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "loadBannerAd: " + adSize + " => " + adWidthDp + "x" + adHeightDp);
        }

        Extras extras = new Extras(config.getMediationExtras(), config.getServerParameters());
        initializeIfNecessary(context, extras.getAppId());
        initTestAds(config);

        new BannerRequest(context)
                .setAdFormat(adSize.equals(AdSize.MEDIUM_RECTANGLE) ? BannerFormat.MREC : BannerFormat.BANNER)
                .setAdSize(adWidthDp, adHeightDp)
                .setAdPreferences(extras.getAdPreferences())
                .load(new BannerRequest.Callback() {
                    @Nullable
                    MediationBannerAdCallback bannerAdCallback;

                    @Override
                    public void onFinished(@Nullable BannerCreator creator, @Nullable String error) {
                        if (creator != null) {
                            if (DEBUG) {
                                Log.v(LOG_TAG, "loadBannerAd: onFinished: success");
                            }

                            final View view = creator.create(context, new BannerListener() {
                                @Override
                                public void onReceiveAd(View view) {
                                    // none
                                }

                                @Override
                                public void onFailedToReceiveAd(View view) {
                                    // none
                                }

                                @Override
                                public void onImpression(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "loadBannerAd: onImpression");
                                    }

                                    if (bannerAdCallback != null) {
                                        bannerAdCallback.reportAdImpression();
                                    }
                                }

                                @Override
                                public void onClick(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "loadBannerAd: onClick");
                                    }

                                    if (bannerAdCallback != null) {
                                        bannerAdCallback.reportAdClicked();
                                    }
                                }
                            });

                            bannerAdCallback = callback.onSuccess(new MediationBannerAd() {
                                @NonNull
                                @Override
                                public View getView() {
                                    return view;
                                }
                            });
                        } else {
                            if (DEBUG) {
                                Log.w(LOG_TAG, "loadBannerAd: onFinished: error: " + error);
                            }

                            callback.onFailure(messageToError(error));
                        }
                    }
                });
    }
    //endregion

    //region Rewarded
    @Nullable
    private StartAppAd rewarded;

    @Nullable
    private MediationRewardedAdCallback rewardedListener;

    @Override
    public void initialize(
            @NonNull Context context,
            @NonNull InitializationCompleteCallback completeCallback,
            @NonNull List<MediationConfiguration> list
    ) {
        String appId = null;
        for (MediationConfiguration config : list) {
            String jsonParameter = config.getServerParameters().getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
            if (jsonParameter != null) {
                try {
                    JSONObject json = new JSONObject(jsonParameter);
                    if (json.has(Extras.APP_ID)) {
                        appId = json.getString(Extras.APP_ID);
                        break;
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Could not parse malformed JSON: " + jsonParameter);
                }
            }
        }

        initializeIfNecessary(context, appId);
        completeCallback.onInitializationSucceeded();
    }

    @Override
    @NonNull
    public VersionInfo getVersionInfo() {
        final String[] parts = BuildConfig.VERSION_NAME.split("\\.");
        if (parts.length < 3) {
            return new VersionInfo(0, 0, 1);
        }

        try {
            return new VersionInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception e) {
            return new VersionInfo(0, 0, 1);
        }
    }

    @Override
    @NonNull
    public VersionInfo getSDKVersionInfo() {
        String version = null;

        try {
            version = (String) StartAppSDK.class.getDeclaredMethod("getVersion").invoke(null);
        } catch (Throwable ex) {
            // ignore
        }

        if (version == null) {
            try {
                version = (String) Class.forName("com.startapp.sdk.GeneratedConstants")
                        .getDeclaredField("INAPP_VERSION")
                        .get(null);
            } catch (Throwable ex) {
                // ignore
            }
        }

        if (version == null) {
            return new VersionInfo(0, 0, 1);
        }

        final String[] parts = version.split("\\.");
        if (parts.length < 3) {
            return new VersionInfo(0, 0, 1);
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(leadingDigits(parts[2]));
            return new VersionInfo(major, minor, patch);
        } catch (Throwable e) {
            return new VersionInfo(0, 0, 1);
        }
    }

    private static String leadingDigits(String input) {
        for (int i = 0, n = input.length(); i < n; ++i) {
            char c = input.charAt(i);

            if (c < '0' || c > '9') {
                return input.substring(0, i);
            }
        }

        return input;
    }

    @Override
    public void loadRewardedAd(
            @NonNull MediationRewardedAdConfiguration config,
            @NonNull final MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> loadCallback
    ) {
        Extras extras = new Extras(config.getMediationExtras(), config.getServerParameters());
        initializeIfNecessary(config.getContext(), extras.getAppId());
        initTestAds(config);

        rewarded = new StartAppAd(config.getContext());
        rewarded.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {
                if (rewardedListener != null) {
                    rewardedListener.onVideoComplete();
                    rewardedListener.onUserEarnedReward(new StartappRewardItem());
                }
            }
        });

        AdEventListener loadListener = new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                rewardedListener = loadCallback.onSuccess(new MediationRewardedAd() {
                    @Override
                    public void showAd(@NonNull Context context) {
                        if (rewarded == null) {
                            return;
                        }

                        rewarded.showAd(new AdDisplayListener() {
                            @Override
                            public void adHidden(Ad ad) {
                                if (rewardedListener != null) {
                                    rewardedListener.onAdClosed();
                                }
                            }

                            @Override
                            public void adDisplayed(Ad ad) {
                                if (rewardedListener != null) {
                                    rewardedListener.onAdOpened();
                                    rewardedListener.onVideoStart();
                                    rewardedListener.reportAdImpression();
                                }
                            }

                            @Override
                            public void adClicked(Ad ad) {
                                if (rewardedListener != null) {
                                    rewardedListener.reportAdClicked();
                                }
                            }

                            @Override
                            public void adNotDisplayed(Ad ad) {
                                if (rewardedListener != null) {
                                    String message = ad.getErrorMessage();
                                    rewardedListener.onAdFailedToShow(
                                            new AdError(0,
                                                    message != null ? message : "adNotDisplayed",
                                                    "io.start"));
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                String message = ad != null ? ad.getErrorMessage() : "ad is null";
                Log.v(LOG_TAG, "ad loading failed: " + message);
                loadCallback.onFailure(messageToError(message));
            }
        };

        rewarded.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, extras.getAdPreferences(), loadListener);
    }

    public static class StartappRewardItem implements RewardItem {
        @NonNull
        @Override
        public String getType() {
            return "";
        }

        @Override
        public int getAmount() {
            return 1;
        }
    }
    //endregion

    //region Native

    @Nullable
    private StartAppNativeAd nativeAd;

    @Nullable
    private MediationNativeAdCallback nativeListener;

    @Override
    public void loadNativeAd(
            @NonNull final MediationNativeAdConfiguration config,
            @NonNull final MediationAdLoadCallback<UnifiedNativeAdMapper, MediationNativeAdCallback> callback
    ) {
        Extras extras = new Extras(config.getNativeAdOptions(), config.getMediationExtras(), config.getServerParameters());
        initializeIfNecessary(config.getContext(), extras.getAppId());
        initTestAds(config);

        nativeAd = new StartAppNativeAd(config.getContext());
        final NativeAdPreferences prefs = (NativeAdPreferences) extras.getAdPreferences();

        nativeAd.loadAd(prefs, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                ArrayList<NativeAdDetails> ads = nativeAd.getNativeAds();
                if (ads != null && !ads.isEmpty()) {
                    nativeListener = callback.onSuccess(
                            new NativeMapper(config.getContext(), ads.get(0), prefs.isContentAd()));
                } else {
                    Log.v(LOG_TAG, "ad loading failed: no fill");
                    callback.onFailure(messageToError("204"));
                }
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                final String message = ad != null ? ad.getErrorMessage() : "ad is null";
                Log.v(LOG_TAG, "ad loading failed: " + message);
                callback.onFailure(messageToError(message));
            }
        });
    }

    private class NativeMapper extends UnifiedNativeAdMapper {
        @NonNull
        private final NativeAdDetails details;

        NativeMapper(
                @NonNull Context context,
                @NonNull NativeAdDetails details,
                boolean isContentAd
        ) {
            this.details = details;

            setHasVideoContent(false);
            setHeadline(details.getTitle());
            setBody(details.getDescription());
            setCallToAction(details.getCallToAction());
            setStarRating((double) details.getRating());

            if (!isContentAd) {
                if (!TextUtils.isEmpty(details.getImageUrl())) {
                    final Uri uri = Uri.parse(details.getImageUrl());
                    if (uri != null) {
                        setImages(Collections.<NativeAd.Image>singletonList(new MappedImage(context, uri, details.getImageBitmap())));
                    }
                }

                if (!TextUtils.isEmpty(details.getSecondaryImageUrl())) {
                    final Uri uri = Uri.parse(details.getSecondaryImageUrl());
                    if (uri != null) {
                        setIcon(new MappedImage(context, uri, details.getSecondaryImageBitmap()));
                    }
                }
            }

            setOverrideClickHandling(true);
            setOverrideImpressionRecording(true);
        }

        @Override
        public void trackViews(
                @NonNull View containerView,
                @NonNull Map<String, View> clickableAssetViews,
                @NonNull Map<String, View> nonclickableAssetViews
        ) {
            List<View> clickableViews = new ArrayList<>(clickableAssetViews.values());
            details.registerViewForInteraction(containerView, clickableViews, new NativeAdDisplayListener() {
                @Override
                public void adHidden(@NonNull NativeAdInterface nativeAdInterface) {
                    if (nativeListener != null) {
                        nativeListener.onAdClosed();
                    }
                }

                @Override
                public void adDisplayed(@NonNull NativeAdInterface nativeAdInterface) {
                    if (nativeListener != null) {
                        nativeListener.onAdOpened();
                        nativeListener.reportAdImpression();
                    }
                }

                @Override
                public void adClicked(@NonNull NativeAdInterface nativeAdInterface) {
                    if (nativeListener != null) {
                        nativeListener.reportAdClicked();
                    }
                }

                @Override
                public void adNotDisplayed(@NonNull NativeAdInterface nativeAdInterface) {
                }
            });
        }

        @Override
        public void untrackView(@NonNull View view) {
            details.unregisterView();
        }
    }

    private static class MappedImage extends NativeAd.Image {
        @NonNull
        private final Context context;

        @NonNull
        private final Uri uri;

        @Nullable
        private final Bitmap bitmap;

        MappedImage(@NonNull Context context, @NonNull Uri uri, @Nullable Bitmap bitmap) {
            this.context = context;
            this.uri = uri;
            this.bitmap = bitmap;
        }

        @Override
        @NonNull
        public Drawable getDrawable() {
            if (bitmap == null) {
                Bitmap bm = BitmapFactory.decodeResource(Resources.getSystem(), android.R.drawable.editbox_background_normal);
                return new BitmapDrawable(context.getResources(), bm);
            }
            return new BitmapDrawable(context.getResources(), bitmap);
        }

        @Override
        @NonNull
        public Uri getUri() {
            return uri;
        }

        @Override
        public double getScale() {
            return 1.0;
        }
    }
    //endregion

    @NonNull
    private static AdError messageToError(@Nullable String message) {
        message = message != null ? message : "Internal error";
        boolean isNoFill = message.contains("204") || message.contains("Empty Response");
        return new AdError(isNoFill
                ? AdRequest.ERROR_CODE_MEDIATION_NO_FILL
                : AdRequest.ERROR_CODE_INTERNAL_ERROR,
                isNoFill ? "No Fill" : message,
                "io.start");
    }
}
