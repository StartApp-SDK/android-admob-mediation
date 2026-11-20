package com.startapp.mediation.admob.example;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.startapp.mediation.admob.StartappAdapter;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.Arrays;
import java.util.List;

import io.start.mediation.admob.StartIoAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AdMob_StartIo";

    // NOTE change to false in order to test adapter in a real environment
    private static final boolean TEST_ADS = true;

    /**
     * Mediation adapter class names of Start.io, as set in the AdMob custom event.
     */
    private static final List<String> ADAPTER_CLASSES = Arrays.asList(
            StartIoAdapter.class.getName(),
            StartappAdapter.class.getName()
    );

    enum AdState {
        IDLE,
        LOADING,
        VISIBLE,
    }

    private static final MutableLiveData<Boolean> initialized = new MutableLiveData<>(null);

    private final MutableLiveData<Pair<InterstitialAd, AdState>> interstitialLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<RewardedAd, AdState>> rewardedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<AdView, AdState>> bannerLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<AdView, AdState>> mrecLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<NativeAd, AdState>> nativeLiveData = new MutableLiveData<>();

    private AdUnits adUnits;

    private ViewGroup bannerContainer;
    private ViewGroup mrecContainer;
    private ViewGroup nativeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adUnits = TEST_ADS ? testAdUnits() : productionAdUnits();

        setTitle("AdMob " + MobileAds.getVersion() + " - Start.io " + StartAppSDK.getVersion());

        setContentView(R.layout.activity_main);

        View loadInterstitial = findViewById(R.id.load_interstitial);
        View showInterstitial = findViewById(R.id.show_interstitial);
        View loadRewarded = findViewById(R.id.load_rewarded);
        View showRewarded = findViewById(R.id.show_rewarded);
        View loadBanner = findViewById(R.id.load_banner);
        View showBanner = findViewById(R.id.show_banner);
        View hideBanner = findViewById(R.id.hide_banner);
        View loadMrec = findViewById(R.id.load_mrec);
        View showMrec = findViewById(R.id.show_mrec);
        View hideMrec = findViewById(R.id.hide_mrec);
        View loadNative = findViewById(R.id.load_native);
        View showNative = findViewById(R.id.show_native);
        View hideNative = findViewById(R.id.hide_native);
        bannerContainer = findViewById(R.id.banner_container);
        mrecContainer = findViewById(R.id.mrec_container);
        nativeContainer = findViewById(R.id.native_container);

        loadInterstitial.setOnClickListener(this::loadInterstitial);
        showInterstitial.setOnClickListener(this::showInterstitial);
        loadRewarded.setOnClickListener(this::loadRewarded);
        showRewarded.setOnClickListener(this::showRewarded);
        loadBanner.setOnClickListener(this::loadBanner);
        showBanner.setOnClickListener(this::showBanner);
        hideBanner.setOnClickListener(this::hideBanner);
        loadMrec.setOnClickListener(this::loadMrec);
        showMrec.setOnClickListener(this::showMrec);
        hideMrec.setOnClickListener(this::hideMrec);
        loadNative.setOnClickListener(this::loadNative);
        showNative.setOnClickListener(this::showNative);
        hideNative.setOnClickListener(this::hideNative);

        interstitialLiveData.observe(this, pair -> {
            loadInterstitial.setEnabled(isLoadButtonEnabled(pair));
            showInterstitial.setEnabled(isShowButtonEnabled(pair));
        });

        rewardedLiveData.observe(this, pair -> {
            loadRewarded.setEnabled(isLoadButtonEnabled(pair));
            showRewarded.setEnabled(isShowButtonEnabled(pair));
        });

        bannerLiveData.observe(this, pair -> {
            loadBanner.setEnabled(isLoadButtonEnabled(pair));
            showBanner.setEnabled(isShowButtonEnabled(pair));
            hideBanner.setEnabled(isHideButtonVisible(pair));
            bannerContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        mrecLiveData.observe(this, pair -> {
            loadMrec.setEnabled(isLoadButtonEnabled(pair));
            showMrec.setEnabled(isShowButtonEnabled(pair));
            hideMrec.setEnabled(isHideButtonVisible(pair));
            mrecContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        nativeLiveData.observe(this, pair -> {
            loadNative.setEnabled(isLoadButtonEnabled(pair));
            showNative.setEnabled(isShowButtonEnabled(pair));
            hideNative.setEnabled(isHideButtonVisible(pair));
            nativeContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        initialized.observe(this, value -> {
            if (value == null) {
                initialized.setValue(false);

                new Thread(() -> MobileAds.initialize(this, status -> {
                    // TODO remove this line in production
                    StartAppSDK.setTestAdsEnabled(true);

                    initialized.postValue(true);
                })).start();
            } else if (value) {
                interstitialLiveData.setValue(null);
                rewardedLiveData.setValue(null);
                bannerLiveData.setValue(null);
                mrecLiveData.setValue(null);
                nativeLiveData.setValue(null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Release the ads of this screen, they are not reused after it is destroyed.
        // An ad that is still loading is released by its load callback, see isDestroyed().
        destroyAdView(bannerLiveData);
        destroyAdView(mrecLiveData);
        destroyNativeAd(nativeLiveData.getValue());

        super.onDestroy();
    }

    private static void destroyNativeAd(@Nullable Pair<NativeAd, AdState> pair) {
        if (pair != null && pair.first != null) {
            pair.first.destroy();
        }
    }

    private static boolean isInitialized() {
        return Boolean.TRUE.equals(initialized.getValue());
    }

    private static <T> boolean isLoadButtonEnabled(@Nullable Pair<T, AdState> pair) {
        return (pair == null || pair.first == null && pair.second != AdState.LOADING) && isInitialized();
    }

    private static <T> boolean isShowButtonEnabled(@Nullable Pair<T, AdState> pair) {
        return pair != null && pair.first != null && pair.second != AdState.VISIBLE;
    }

    private static <T> boolean isHideButtonVisible(@Nullable Pair<T, AdState> pair) {
        return pair != null && pair.second == AdState.VISIBLE;
    }

    @Nullable
    private static String adapterOf(@Nullable ResponseInfo responseInfo) {
        return responseInfo != null ? responseInfo.getMediationAdapterClassName() : null;
    }

    private boolean isAllowedAd(@Nullable ResponseInfo responseInfo) {
        if (TEST_ADS || ADAPTER_CLASSES.contains(adapterOf(responseInfo))) {
            return true;
        }

        Toast.makeText(getApplicationContext(), "Non-Start.io production ad skipped", Toast.LENGTH_SHORT).show();
        return false;
    }

    // region Banner & Medium Rectangle
    private void loadBanner(@NonNull View view) {
        loadAdView(adUnits.bannerAdUnitId, AdSize.BANNER, bannerLiveData);
    }

    private void loadMrec(@NonNull View view) {
        loadAdView(adUnits.mrecAdUnitId, AdSize.MEDIUM_RECTANGLE, mrecLiveData);
    }

    private void showBanner(@NonNull View view) {
        showAdView(bannerLiveData, bannerContainer);
    }

    private void showMrec(@NonNull View view) {
        showAdView(mrecLiveData, mrecContainer);
    }

    private void hideBanner(@NonNull View view) {
        hideAdView(bannerLiveData, bannerContainer);
    }

    private void hideMrec(@NonNull View view) {
        hideAdView(mrecLiveData, mrecContainer);
    }

    private void loadAdView(@NonNull String adUnitId, @NonNull AdSize adSize, @NonNull MutableLiveData<Pair<AdView, AdState>> liveData) {
        liveData.setValue(new Pair<>(null, AdState.LOADING));

        AdView adView = new AdView(this);
        adView.setAdUnitId(adUnitId);
        adView.setAdSize(adSize);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.v(LOG_TAG, "loadAdView: onAdLoaded: " + adSize + ", adapter: " + adapterOf(adView.getResponseInfo()));

                if (isDestroyed()) {
                    destroyAdView(adView);
                    return;
                }

                Pair<AdView, AdState> pair = liveData.getValue();
                if (pair == null || pair.second == AdState.LOADING) {
                    if (isAllowedAd(adView.getResponseInfo())) {
                        liveData.setValue(new Pair<>(adView, AdState.IDLE));
                    } else {
                        liveData.setValue(null);
                        destroyAdView(adView);
                    }
                } else if (pair.first == adView && !isAllowedAd(adView.getResponseInfo())) {
                    // An auto-refresh of a loaded or visible ad brought an ad of another network
                    liveData.setValue(null);
                    destroyAdView(adView);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.w(LOG_TAG, "loadAdView: onAdFailedToLoad: " + adSize + ", " + error);

                if (isDestroyed()) {
                    destroyAdView(adView);
                    return;
                }

                // A failed auto-refresh of a visible ad keeps the current ad on screen
                Pair<AdView, AdState> pair = liveData.getValue();
                if (pair == null || pair.second == AdState.LOADING) {
                    liveData.setValue(null);
                    destroyAdView(adView);
                }
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    private void showAdView(@NonNull MutableLiveData<Pair<AdView, AdState>> liveData, @NonNull ViewGroup container) {
        Pair<AdView, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            container.removeAllViews();
            container.addView(pair.first, new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            liveData.setValue(new Pair<>(pair.first, AdState.VISIBLE));
        } else {
            Toast.makeText(this, "AdView is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideAdView(@NonNull MutableLiveData<Pair<AdView, AdState>> liveData, @NonNull ViewGroup container) {
        destroyAdView(liveData);

        container.removeAllViews();
        liveData.setValue(new Pair<>(null, AdState.IDLE));
    }

    private static void destroyAdView(@NonNull MutableLiveData<Pair<AdView, AdState>> liveData) {
        Pair<AdView, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            destroyAdView(pair.first);
        }
    }

    /**
     * Stops auto-refresh and releases the ad view, removing it from the screen if it is shown.
     */
    private static void destroyAdView(@NonNull AdView adView) {
        adView.setAdListener(new AdListener() {
        });

        if (adView.getParent() instanceof ViewGroup) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }

        adView.destroy();
    }
    // endregion

    // region Interstitial

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused by you fields can be omitted:
     * {startappAppId:'204653131', adTag:'interstitialTagFromServer', interstitialMode:'OVERLAY', minCPM:0.02, muteVideo:false}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void loadInterstitial(@NonNull View view) {
        interstitialLiveData.setValue(new Pair<>(null, AdState.LOADING));

        // optionally you can set additional parameters for Startapp interstitial
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("interstitialTagFromAdRequest")
                .setInterstitialMode(StartappAdapter.Mode.OFFERWALL)
                .muteVideo()
                .setMinCPM(0.01)
                .toBundle();

        final AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build();

        InterstitialAd.load(this, adUnits.interstitialAdUnitId, request, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                Log.v(LOG_TAG, "loadInterstitial: onAdLoaded, adapter: " + adapterOf(interstitialAd.getResponseInfo()));

                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                        Log.w(LOG_TAG, "loadInterstitial: onAdFailedToShowFullScreenContent: " + error);
                    }
                });

                if (isAllowedAd(interstitialAd.getResponseInfo())) {
                    interstitialLiveData.setValue(new Pair<>(interstitialAd, AdState.IDLE));
                } else {
                    interstitialLiveData.setValue(null);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.w(LOG_TAG, "loadInterstitial: onAdFailedToLoad: " + error);

                interstitialLiveData.setValue(null);
            }
        });
    }

    public void showInterstitial(@NonNull View view) {
        Pair<InterstitialAd, AdState> pair = interstitialLiveData.getValue();
        if (pair != null && pair.first != null) {
            interstitialLiveData.setValue(null);

            pair.first.show(this);
        } else {
            Toast.makeText(this, "Interstitial is not ready", Toast.LENGTH_SHORT).show();
        }
    }
    // endregion

    // region Rewarded

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused by you fields can be omitted:
     * {startappAppId:'204653131', adTag:'rewardedTagFromServer', minCPM:0.02, muteVideo:false}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void loadRewarded(@NonNull View view) {
        rewardedLiveData.setValue(new Pair<>(null, AdState.LOADING));

        // optionally you can set additional parameters for Startapp interstitial
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("rewardedTagFromAdRequest")
                .muteVideo()
                .setMinCPM(0.01)
                .toBundle();

        final AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build();

        RewardedAd.load(this, adUnits.rewardedAdUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.v(LOG_TAG, "loadRewarded: onAdLoaded, adapter: " + adapterOf(rewardedAd.getResponseInfo()));

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                        Log.w(LOG_TAG, "loadRewarded: onAdFailedToShowFullScreenContent: " + error);
                    }
                });

                if (isAllowedAd(rewardedAd.getResponseInfo())) {
                    rewardedLiveData.setValue(new Pair<>(rewardedAd, AdState.IDLE));
                } else {
                    rewardedLiveData.setValue(null);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.w(LOG_TAG, "loadRewarded: onAdFailedToLoad: " + error);

                rewardedLiveData.setValue(null);
            }
        });
    }

    public void showRewarded(@NonNull View view) {
        Pair<RewardedAd, AdState> pair = rewardedLiveData.getValue();
        if (pair != null && pair.first != null) {
            rewardedLiveData.setValue(null);

            pair.first.show(this, rewardItem -> {
                Log.i(LOG_TAG, "User earned a reward: " + rewardItem.getType() + ", amount=" + rewardItem.getAmount());

                Toast.makeText(this, "User earned a reward", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Rewarded is not ready", Toast.LENGTH_SHORT).show();
        }
    }
    // endregion

    // region Native

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused fields can be omitted:
     * {startappAppId:'204653131', adTag:'nativeTagFromServer', minCPM:0.02, nativeImageSize:'SIZE340X340', nativeSecondaryImageSize:'SIZE72X72'}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void loadNative(@NonNull View view) {
        nativeLiveData.setValue(new Pair<>(null, AdState.LOADING));

        new AdLoader.Builder(this, adUnits.nativeAdUnitId)
                .forNativeAd(nativeAd -> {
                    Log.v(LOG_TAG, "loadNative: onNativeAdLoaded, adapter: " + adapterOf(nativeAd.getResponseInfo()));

                    if (isDestroyed()) {
                        nativeAd.destroy();
                        return;
                    }

                    if (isAllowedAd(nativeAd.getResponseInfo())) {
                        nativeLiveData.setValue(new Pair<>(nativeAd, AdState.IDLE));
                    } else {
                        nativeLiveData.setValue(null);
                        nativeAd.destroy();
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.w(LOG_TAG, "loadNative: onAdFailedToLoad: " + error);

                        nativeLiveData.setValue(null);
                    }
                })
                .build()
                .loadAd(new AdRequest.Builder().build());
    }

    public void showNative(@NonNull View view) {
        Pair<NativeAd, AdState> pair = nativeLiveData.getValue();
        if (pair == null || pair.first == null) {
            Toast.makeText(this, "Native is not ready", Toast.LENGTH_SHORT).show();

            nativeLiveData.setValue(null);
            return;
        }

        NativeAd nativeAd = pair.first;

        NativeAdView nativeView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad_unified, nativeContainer, false);
        TextView headlineTextView = nativeView.findViewById(R.id.headline_text_view);
        MediaView mediaView = nativeView.findViewById(R.id.media_view);
        ImageView imageView = nativeView.findViewById(R.id.image_view);
        TextView callToActionTextView = nativeView.findViewById(R.id.call_to_action_text_view);
        TextView bodyTextView = nativeView.findViewById(R.id.body_text_view);
        TextView advertiserTextView = nativeView.findViewById(R.id.advertiser_text_view);
        ImageView logoImageView = nativeView.findViewById(R.id.logo_image_view);
        TextView priceTextView = nativeView.findViewById(R.id.price_text_view);
        TextView ratingTextView = nativeView.findViewById(R.id.rating_text_view);
        TextView storeTextView = nativeView.findViewById(R.id.store_text_view);

        headlineTextView.setText(nativeAd.getHeadline());
        nativeView.setHeadlineView(headlineTextView);

        // the asset is populated automatically, so there's one less step
        nativeView.setMediaView(mediaView);

        final List<NativeAd.Image> images = nativeAd.getImages();
        if (!images.isEmpty()) {
            imageView.setImageDrawable(images.get(0).getDrawable());
            nativeView.setImageView(imageView);
        }

        callToActionTextView.setText(nativeAd.getCallToAction());
        nativeView.setCallToActionView(callToActionTextView);

        bodyTextView.setText(nativeAd.getBody());
        nativeView.setBodyView(bodyTextView);

        advertiserTextView.setText(nativeAd.getAdvertiser());
        nativeView.setAdvertiserView(advertiserTextView);

        final NativeAd.Image image = nativeAd.getIcon();
        if (image != null) {
            logoImageView.setImageDrawable(image.getDrawable());
            nativeView.setIconView(logoImageView);
        }

        final String price = nativeAd.getPrice();
        if (price != null) {
            priceTextView.setText(price);
            nativeView.setPriceView(priceTextView);
        }

        final Double rating = nativeAd.getStarRating();
        if (rating != null) {
            ratingTextView.setText(String.valueOf(rating));
            nativeView.setStarRatingView(ratingTextView);
        }

        final String store = nativeAd.getStore();
        if (store != null) {
            storeTextView.setText(store);
            nativeView.setStoreView(storeTextView);
        }

        nativeView.setNativeAd(nativeAd);

        nativeContainer.removeAllViews();
        nativeContainer.addView(nativeView);
        nativeLiveData.setValue(new Pair<>(nativeAd, AdState.VISIBLE));
    }

    private void hideNative(@NonNull View view) {
        destroyNativeAd(nativeLiveData.getValue());

        nativeContainer.removeAllViews();
        nativeLiveData.setValue(null);
    }
    // endregion

    // region Ad units
    @NonNull
    private static AdUnits testAdUnits() {
        return new AdUnits(
                "ca-app-pub-3940256099942544/1033173712",
                "ca-app-pub-3940256099942544/5224354917",
                "ca-app-pub-3940256099942544/6300978111",
                "ca-app-pub-3940256099942544/6300978111",
                "ca-app-pub-3940256099942544/2247696110"
        );
    }

    /* TODO put your production Ad Units here to test the integration */
    @NonNull
    private static AdUnits productionAdUnits() {
        return new AdUnits(
                "ca-app-pub-8106958183505772/5353138600",
                "ca-app-pub-8106958183505772/6418808269",
                "ca-app-pub-8106958183505772/7330941398",
                "ca-app-pub-8106958183505772/9114796352",
                "ca-app-pub-8106958183505772/3792644929"
        );
    }

    static class AdUnits {
        @NonNull
        final String interstitialAdUnitId;

        @NonNull
        final String rewardedAdUnitId;

        @NonNull
        final String bannerAdUnitId;

        @NonNull
        final String mrecAdUnitId;

        @NonNull
        final String nativeAdUnitId;

        AdUnits(
                @NonNull String interstitialAdUnitId,
                @NonNull String rewardedAdUnitId,
                @NonNull String bannerAdUnitId,
                @NonNull String mrecAdUnitId,
                @NonNull String nativeAdUnitId
        ) {
            this.interstitialAdUnitId = interstitialAdUnitId;
            this.rewardedAdUnitId = rewardedAdUnitId;
            this.bannerAdUnitId = bannerAdUnitId;
            this.mrecAdUnitId = mrecAdUnitId;
            this.nativeAdUnitId = nativeAdUnitId;
        }
    }
    // endregion
}
