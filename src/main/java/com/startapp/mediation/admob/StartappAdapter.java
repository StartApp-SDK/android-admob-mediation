package com.startapp.mediation.admob;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventNative;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerBase;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.ads.banner.banner3d.Banner3D;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdDisplayListener;
import com.startapp.sdk.ads.nativead.NativeAdInterface;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.VideoListener;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Keep
public class StartappAdapter extends Adapter implements CustomEventInterstitial, CustomEventBanner, MediationRewardedAd, CustomEventNative {
    private static final String LOG_TAG = StartappAdapter.class.getSimpleName();

    // region Lifecycle
    @Override
    public void onDestroy() {
        removeFromParent(bannerContainer);
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }
    // endregion

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
        SIZE1200X628,
        SIZE320X480,
        SIZE480X320
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
                @NonNull MediationAdRequest mediationAdRequest,
                @Nullable Bundle customEventExtras,
                @Nullable String serverParameter
        ) {
            adPreferences = makeAdPreferences(customEventExtras, serverParameter, false, null);
            setKeywords(adPreferences, mediationAdRequest);
            setLocation(adPreferences, mediationAdRequest);
        }

        Extras(
                @NonNull NativeMediationAdRequest mediationAdRequest,
                @Nullable Bundle customEventExtras,
                @Nullable String serverParameter
        ) {
            adPreferences = makeAdPreferences(customEventExtras, serverParameter, true, mediationAdRequest.getNativeAdOptions());
            setKeywords(adPreferences, mediationAdRequest);
            setLocation(adPreferences, mediationAdRequest);
        }

        Extras(@NonNull MediationRewardedAdConfiguration configuration) {
            final Bundle serverParameters = configuration.getServerParameters();
            String serverParameter = null;
            if (serverParameters != null) {
                serverParameter = serverParameters.getString("parameter");
            }

            adPreferences = makeAdPreferences(configuration.getMediationExtras(), serverParameter, false, null);

            if (configuration.getLocation() != null) {
                adPreferences.setLongitude(configuration.getLocation().getLongitude());
                adPreferences.setLatitude(configuration.getLocation().getLatitude());
            }
        }

        private static void setKeywords(@NonNull AdPreferences prefs, @NonNull MediationAdRequest request) {
            if (request.getKeywords() == null) {
                return;
            }

            final StringBuilder sb = new StringBuilder();
            for (String keyWord : request.getKeywords()) {
                sb.append(keyWord).append(",");
            }

            prefs.setKeywords(sb.substring(0, sb.length() - 1));
        }

        private static void setLocation(@NonNull AdPreferences prefs, @NonNull MediationAdRequest request) {
            if (request.getLocation() == null) {
                return;
            }

            prefs.setLongitude(request.getLocation().getLongitude());
            prefs.setLatitude(request.getLocation().getLatitude());
        }

        @NonNull
        private AdPreferences makeAdPreferences(
                @Nullable Bundle customEventExtras,
                @Nullable String serverParameter,
                boolean isNative,
                @Nullable NativeAdOptions nativeAdOptions
        ) {
            String adTag = null;
            boolean isVideoMuted = false;
            Double minCPM = null;
            StartappAdapter.Size nativeImageSize = null;
            StartappAdapter.Size nativeSecondaryImageSize = null;

            if (customEventExtras != null) {
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
            }

            if (serverParameter != null) {
                try {
                    final JSONObject json = new JSONObject(serverParameter);
                    Log.v(LOG_TAG, "Startapp serverParameter:" + json.toString());

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
                        final String name = json.getString(NATIVE_IMAGE_SIZE);
                        try {
                            nativeImageSize = StartappAdapter.Size.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(LOG_TAG, "Could not parse imageSize parameter: " + name);
                        }
                    }

                    if (json.has(NATIVE_SECONDARY_IMAGE_SIZE)) {
                        final String name = json.getString(NATIVE_SECONDARY_IMAGE_SIZE);
                        try {
                            nativeSecondaryImageSize = StartappAdapter.Size.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(LOG_TAG, "Could not parse secondaryImageSize parameter: " + name);
                        }
                    }

                    if (json.has(INTERSTITIAL_MODE)) {
                        final String mode = json.getString(INTERSTITIAL_MODE);
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
                    Log.e(LOG_TAG, "Could not parse malformed JSON: " + serverParameter);
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
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);

    public static boolean initializeSdkIfNeeded(@NonNull Context context, @Nullable String appId, boolean testAds) {
        if (TextUtils.isEmpty(appId)) {
            Log.e("StartAppSDK", "App ID not found\n" +
                    "+-------------------------------------------------------------+\n" +
                    "|                S   T   A   R   T   A   P   P                |\n" +
                    "| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |\n" +
                    "| AdMob Mediation is configured wrongly, App ID not found     |\n" +
                    "| Put your App ID as Parameter for Custom Event:              |\n" +
                    "|                                                             |\n" +
                    "|              { startappAppId : 'YOUR_APP_ID' }              |\n" +
                    "|                                                             |\n" +
                    "| https://support.startapp.com/hc/en-us/articles/360005100893 |\n" +
                    "+-------------------------------------------------------------+\n");
            return false;
        }

        if (!sIsInitialized.getAndSet(true)) {
            StartAppSDK.setTestAdsEnabled(testAds);
            StartAppSDK.init(context, appId, false);
            StartAppAd.disableSplash();
            StartAppAd.enableConsent(context, false);
            StartAppSDK.addWrapper(context, "AdMob", BuildConfig.VERSION_NAME);
            return true;
        }

        return false;
    }

    private static void removeFromParent(@Nullable View view) {
        if (view == null || view.getParent() == null) {
            return;
        }

        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
    //endregion

    //region Interstitial
    @Nullable
    private StartAppAd interstitial;

    @Nullable
    private CustomEventInterstitialListener interstitialListener;

    @Override
    public void requestInterstitialAd(
            @NonNull Context context,
            @NonNull final CustomEventInterstitialListener listener,
            @Nullable String serverParameter,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras
    ) {
        final Extras extras = new Extras(mediationAdRequest, customEventExtras, serverParameter);

        initializeSdkIfNeeded(context, extras.getAppId(), mediationAdRequest.isTesting());

        interstitialListener = listener;
        interstitial = new StartAppAd(context);
        final AdEventListener loadListener = new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                listener.onAdLoaded();
            }

            @Override
            public void onFailedToReceiveAd(@NonNull Ad ad) {
                final String message = ad.getErrorMessage();
                Log.v(LOG_TAG, "ad loading failed: " + message);

                listener.onAdFailedToLoad((message != null && message.contains("204"))
                        ? AdRequest.ERROR_CODE_NO_FILL
                        : AdRequest.ERROR_CODE_INTERNAL_ERROR);
            }
        };

        if (extras.getAdMode() == null) {
            interstitial.loadAd(extras.getAdPreferences(), loadListener);
        } else {
            interstitial.loadAd(extras.getAdMode(), extras.getAdPreferences(), loadListener);
        }
    }

    @Override
    public void showInterstitial() {
        if (interstitial == null) {
            return;
        }

        interstitial.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(@NonNull Ad ad) {
                if (interstitialListener == null) {
                    return;
                }

                interstitialListener.onAdClosed();
            }

            @Override
            public void adDisplayed(@NonNull Ad ad) {
                if (interstitialListener == null) {
                    return;
                }

                interstitialListener.onAdOpened();
            }

            @Override
            public void adClicked(@NonNull Ad ad) {
                if (interstitialListener == null) {
                    return;
                }

                interstitialListener.onAdClicked();
                interstitialListener.onAdLeftApplication();
            }

            @Override
            public void adNotDisplayed(@NonNull Ad ad) {
            }
        });
    }
    //endregion

    //region Banner
    @Nullable
    private FrameLayout bannerContainer;

    @Override
    public void requestBannerAd(
            @NonNull Context context,
            @NonNull final CustomEventBannerListener listener,
            @Nullable String serverParameter,
            @NonNull AdSize adSize,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras
    ) {
        if (!(context instanceof Activity)) {
            return;
        }

        bannerContainer = new FrameLayout(context);
        final BannerListener loadListener = new BannerListener() {
            @Override
            public void onReceiveAd(@NonNull View view) {
                listener.onAdLoaded(bannerContainer);
            }

            @Override
            public void onFailedToReceiveAd(@NonNull View view) {
                listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
            }

            @Override
            public void onImpression(@NonNull View view) {
            }

            @Override
            public void onClick(@NonNull View view) {
                listener.onAdClicked();
                listener.onAdOpened();
                listener.onAdLeftApplication();
            }
        };

        final BannerBase banner = loadBanner(context, serverParameter, adSize, mediationAdRequest, customEventExtras, loadListener);
        // force banner to calculate its view size
        bannerContainer.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        bannerContainer.addView(banner, new FrameLayout.LayoutParams(
                adSize.getWidthInPixels(context),
                adSize.getHeightInPixels(context),
                Gravity.CENTER));
    }

    @NonNull
    private BannerBase loadBanner(
            @NonNull Context context,
            @Nullable String serverParameter,
            @NonNull AdSize adSize,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras,
            @NonNull BannerListener loadListener
    ) {
        final Extras extras = new Extras(mediationAdRequest, customEventExtras, serverParameter);

        initializeSdkIfNeeded(context, extras.getAppId(), mediationAdRequest.isTesting());

        final Activity activity = (Activity) context;
        final BannerBase result;

        if (adSize.equals(AdSize.MEDIUM_RECTANGLE)) {
            result = new Mrec(activity, extras.getAdPreferences(), loadListener);
        } else if (extras.is3DBanner()) {
            result = new Banner3D(activity, extras.getAdPreferences(), loadListener);
        } else {
            result = new Banner(activity, extras.getAdPreferences(), loadListener);
        }

        result.loadAd(adSize.getWidth(), adSize.getHeight());
        return result;
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
        completeCallback.onInitializationSucceeded();
    }

    @Override
    @Nullable
    public VersionInfo getVersionInfo() {
        if (true) return getSDKVersionInfo();

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
    @Nullable
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
            return null;
        }

        final String[] parts = version.split("\\.");
        if (parts.length < 3) {
            return null;
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(leadingDigits(parts[2]));
            return new VersionInfo(major, minor, patch);
        } catch (Throwable e) {
            return null;
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
            @NonNull MediationRewardedAdConfiguration adConfiguration,
            @NonNull final MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> loadCallback
    ) {
        final Context context = adConfiguration.getContext();
        if (context == null) {
            return;
        }

        final Extras extras = new Extras(adConfiguration);

        initializeSdkIfNeeded(context, extras.getAppId(), adConfiguration.isTestRequest());

        rewarded = new StartAppAd(context);
        rewarded.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {
                if (rewardedListener != null) {
                    rewardedListener.onVideoComplete();
                    rewardedListener.onUserEarnedReward(new StartappRewardItem());
                }
            }
        });

        final AdEventListener loadListener = new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                rewardedListener = loadCallback.onSuccess(StartappAdapter.this);
            }

            @Override
            public void onFailedToReceiveAd(@NonNull Ad ad) {
                final String message = ad.getErrorMessage();
                Log.v(LOG_TAG, "ad loading failed: " + message);

                loadCallback.onFailure(message != null ? message : "No fill.");
            }
        };

        rewarded.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, extras.getAdPreferences(), loadListener);
    }

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
                    rewardedListener.onAdFailedToShow(ad.getErrorMessage());
                }
            }
        });
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
    @Override
    public void requestNativeAd(
            @NonNull final Context context,
            @NonNull final CustomEventNativeListener listener,
            @Nullable String serverParameter,
            @NonNull NativeMediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras
    ) {
        final Extras extras = new Extras(mediationAdRequest, customEventExtras, serverParameter);

        initializeSdkIfNeeded(context, extras.getAppId(), mediationAdRequest.isTesting());

        final StartAppNativeAd startappAds = new StartAppNativeAd(context);
        final NativeAdPreferences prefs = (NativeAdPreferences) extras.getAdPreferences();

        startappAds.loadAd(prefs, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                final ArrayList<NativeAdDetails> ads = startappAds.getNativeAds();
                if (ads != null && !ads.isEmpty()) {
                    listener.onAdLoaded(new NativeMapper(context, ads.get(0), prefs.isContentAd(), listener));
                } else {
                    Log.v(LOG_TAG, "ad loading failed: no fill");
                    listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                }
            }

            @Override
            public void onFailedToReceiveAd(@NonNull Ad ad) {
                final String message = ad.getErrorMessage();
                Log.v(LOG_TAG, "ad loading failed: " + message);

                listener.onAdFailedToLoad((message != null && (message.contains("204") || message.contains("Empty Response")))
                        ? AdRequest.ERROR_CODE_NO_FILL
                        : AdRequest.ERROR_CODE_INTERNAL_ERROR);
            }
        });
    }

    @NonNull
    private static String mapCallToAction(@NonNull StartAppNativeAd.CampaignAction action) {
        switch (action) {
            case OPEN_MARKET:
                return "Install";
            case LAUNCH_APP:
                return "Launch app";
        }
        return "Open";
    }

    private static class NativeMapper extends UnifiedNativeAdMapper {
        @NonNull
        private final NativeAdDetails details;

        @NonNull
        private final WeakReference<CustomEventNativeListener> listener;

        NativeMapper(
                @NonNull Context context,
                @NonNull NativeAdDetails details,
                boolean isContentAd,
                @NonNull CustomEventNativeListener listener
        ) {
            this.details = details;
            this.listener = new WeakReference<>(listener);

            setHasVideoContent(false);
            setHeadline(details.getTitle());
            setBody(details.getDescription());
            setCallToAction(mapCallToAction(details.getCampaignAction()));
            setStarRating((double) details.getRating());

            if (!isContentAd) {
                final List<NativeAd.Image> images = new ArrayList<>(2);

                if (!TextUtils.isEmpty(details.getImageUrl())) {
                    final Uri uri = Uri.parse(details.getImageUrl());
                    if (uri != null) {
                        images.add(new MappedImage(context, uri, details.getImageBitmap()));
                    }
                }

                if (!TextUtils.isEmpty(details.getSecondaryImageUrl())) {
                    final Uri uri = Uri.parse(details.getSecondaryImageUrl());
                    if (uri != null) {
                        final MappedImage icon = new MappedImage(context, uri, details.getSecondaryImageBitmap());
                        images.add(icon);
                        setIcon(icon);
                    }
                }

                if (!images.isEmpty()) {
                    setImages(images);
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
            final List<View> clickableViews = new ArrayList<>(clickableAssetViews.values());

            details.registerViewForInteraction(containerView, clickableViews, new NativeAdDisplayListener() {
                @Override
                public void adHidden(@NonNull NativeAdInterface nativeAdInterface) {
                    final CustomEventNativeListener callbacks = listener.get();
                    if (callbacks != null) {
                        callbacks.onAdClosed();
                    }
                }

                @Override
                public void adDisplayed(@NonNull NativeAdInterface nativeAdInterface) {
                    final CustomEventNativeListener callbacks = listener.get();
                    if (callbacks != null) {
                        callbacks.onAdImpression();
                    }
                }

                @Override
                public void adClicked(@NonNull NativeAdInterface nativeAdInterface) {
                    final CustomEventNativeListener callbacks = listener.get();
                    if (callbacks != null) {
                        callbacks.onAdClicked();
                        callbacks.onAdOpened();
                        callbacks.onAdLeftApplication();
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
        @Nullable
        public Drawable getDrawable() {
            if (bitmap == null) {
                return null;
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
}
