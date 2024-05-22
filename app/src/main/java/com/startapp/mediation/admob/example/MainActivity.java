package com.startapp.mediation.admob.example;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.startapp.mediation.admob.StartappAdapter;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AdMob_StartIo";

    private static final MutableLiveData<Boolean> initialized = new MutableLiveData<>(null);
    private final MutableLiveData<InterstitialAd> interstitialLiveData = new MutableLiveData<>();
    private final MutableLiveData<RewardedAd> rewardedLiveData = new MutableLiveData<>();
    private final MutableLiveData<AdView> bannerLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bannerVisible = new MutableLiveData<>(false);
    private final MutableLiveData<AdView> mrecLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mrecVisible = new MutableLiveData<>(false);
    private final MutableLiveData<NativeAd> nativeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> nativeVisible = new MutableLiveData<>(false);

    private ViewGroup bannerContainer;
    private ViewGroup mrecContainer;
    private ViewGroup nativeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        interstitialLiveData.observe(this, interstitialAd -> {
            loadInterstitial.setEnabled(interstitialAd == null && isInitialized());
            showInterstitial.setEnabled(interstitialAd != null);
        });

        rewardedLiveData.observe(this, rewardedAd -> {
            loadRewarded.setEnabled(rewardedAd == null && isInitialized());
            showRewarded.setEnabled(rewardedAd != null);
        });

        bannerLiveData.observe(this, adView -> {
            loadBanner.setEnabled(adView == null && isInitialized());
            showBanner.setEnabled(adView != null && !Boolean.TRUE.equals(bannerVisible.getValue()));
        });

        bannerVisible.observe(this, visible -> {
            bannerContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            showBanner.setEnabled(bannerLiveData.getValue() != null && !visible);
            hideBanner.setEnabled(visible);
        });

        mrecLiveData.observe(this, adView -> {
            loadMrec.setEnabled(adView == null && isInitialized());
            showMrec.setEnabled(adView != null && !Boolean.TRUE.equals(mrecVisible.getValue()));
        });

        mrecVisible.observe(this, visible -> {
            mrecContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            showMrec.setEnabled(mrecLiveData.getValue() != null && !visible);
            hideMrec.setEnabled(visible);
        });

        nativeLiveData.observe(this, nativeAd -> {
            loadNative.setEnabled(nativeAd == null && isInitialized());
            showNative.setEnabled(nativeAd != null && !Boolean.TRUE.equals(nativeVisible.getValue()));
        });

        nativeVisible.observe(this, visible -> {
            nativeContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            showNative.setEnabled(nativeLiveData.getValue() != null && !visible);
            hideNative.setEnabled(visible);
        });

        initialized.observe(this, value -> {
            if (value == null) {
                initialized.setValue(false);

                MobileAds.initialize(this, status -> {
                    initialized.setValue(true);

                    // TODO remove this line in production
                    StartAppSDK.setTestAdsEnabled(true);
                });
            } else if (value) {
                interstitialLiveData.setValue(null);
                rewardedLiveData.setValue(null);
                bannerLiveData.setValue(null);
                mrecLiveData.setValue(null);
                nativeLiveData.setValue(null);
            }
        });
    }

    private static boolean isInitialized() {
        return Boolean.TRUE.equals(initialized.getValue());
    }

    //region Banner
    private void loadBanner(@NonNull View view) {
        AdView adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.bannerId));
        adView.setAdSize(AdSize.BANNER);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.e(LOG_TAG, "Banner: onAdFailedToLoad: " + error);

                bannerLiveData.setValue(null);
            }

            @Override
            public void onAdLoaded() {
                bannerLiveData.setValue(adView);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    private void showBanner(@NonNull View view) {
        AdView adView = bannerLiveData.getValue();
        if (adView != null) {
            bannerContainer.removeAllViews();
            bannerContainer.addView(adView, new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

            bannerVisible.setValue(true);
        } else {
            Toast.makeText(this, "Banner is not ready", Toast.LENGTH_SHORT).show();

            bannerVisible.setValue(false);
        }
    }

    private void hideBanner(@NonNull View view) {
        bannerContainer.removeAllViews();
        bannerLiveData.setValue(null);
        bannerVisible.setValue(false);
    }
    //endregion

    //region Medium Rectangle
    private void loadMrec(@NonNull View view) {
        AdView adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.mrecId));
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.e(LOG_TAG, "Mrec: onAdFailedToLoad: " + error);

                mrecLiveData.setValue(null);
            }

            @Override
            public void onAdLoaded() {
                mrecLiveData.setValue(adView);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    private void showMrec(@NonNull View view) {
        AdView adView = mrecLiveData.getValue();
        if (adView != null) {
            mrecContainer.removeAllViews();
            mrecContainer.addView(adView, new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

            mrecVisible.setValue(true);
        } else {
            Toast.makeText(this, "Mrec is not ready", Toast.LENGTH_SHORT).show();

            mrecVisible.setValue(false);
        }
    }

    private void hideMrec(@NonNull View view) {
        mrecContainer.removeAllViews();
        mrecLiveData.setValue(null);
        mrecVisible.setValue(false);
    }
    //endregion

    //region Interstitial

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused by you fields can be omitted:
     * {startappAppId:'204653131', adTag:'interstitialTagFromServer', interstitialMode:'OVERLAY', minCPM:0.02, muteVideo:false}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void loadInterstitial(@NonNull View view) {
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

        InterstitialAd.load(this, getString(R.string.interstitialId), request, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        interstitialLiveData.setValue(null);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                        Log.e(LOG_TAG, "Interstitial: onAdFailedToShowFullScreenContent: " + error);

                        interstitialLiveData.setValue(null);
                    }
                });

                interstitialLiveData.setValue(interstitialAd);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.e(LOG_TAG, "Interstitial: onAdFailedToLoad: " + error);

                interstitialLiveData.setValue(null);
            }
        });
    }

    public void showInterstitial(@NonNull View view) {
        InterstitialAd interstitialAd = interstitialLiveData.getValue();
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            Toast.makeText(this, "Interstitial is not ready", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region Rewarded

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused by you fields can be omitted:
     * {startappAppId:'204653131', adTag:'rewardedTagFromServer', minCPM:0.02, muteVideo:false}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void loadRewarded(@NonNull View view) {
        // optionally you can set additional parameters for Startapp interstitial
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("rewardedTagFromAdRequest")
                .muteVideo()
                .setMinCPM(0.01)
                .toBundle();

        final AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build();

        RewardedAd.load(this, getString(R.string.rewardedId), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        rewardedLiveData.setValue(null);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                        Log.e(LOG_TAG, "Rewarded: onAdFailedToShowFullScreenContent: " + error);

                        rewardedLiveData.setValue(null);
                    }
                });

                rewardedLiveData.setValue(rewardedAd);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.e(LOG_TAG, "Rewarded: onAdFailedToLoad: " + error);

                rewardedLiveData.setValue(null);
            }
        });
    }

    public void showRewarded(@NonNull View view) {
        RewardedAd rewardedAd = rewardedLiveData.getValue();
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                Log.i(LOG_TAG, "User earned a reward: " + rewardItem.getType() + ", amount=" + rewardItem.getAmount());

                Toast.makeText(this, "User earned a reward", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Rewarded is not ready", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region Native

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused fields can be omitted:
     * {startappAppId:'204653131', adTag:'nativeTagFromServer', minCPM:0.02, nativeImageSize:'SIZE340X340', nativeSecondaryImageSize:'SIZE72X72'}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void loadNative(@NonNull View view) {
        new AdLoader.Builder(this, getString(R.string.nativeId))
                .forNativeAd(nativeLiveData::setValue)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e(LOG_TAG, "Native: onAdFailedToLoad: " + error);

                        nativeLiveData.setValue(null);
                    }
                })
                .build()
                .loadAd(new AdRequest.Builder().build());
    }

    public void showNative(@NonNull View view) {
        NativeAd nativeAd = nativeLiveData.getValue();
        if (nativeAd == null) {
            Toast.makeText(this, "Native is not ready", Toast.LENGTH_SHORT).show();

            nativeVisible.setValue(false);
            return;
        }

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
        nativeVisible.setValue(true);
    }

    private void hideNative(@NonNull View view) {
        nativeContainer.removeAllViews();
        nativeLiveData.setValue(null);
        nativeVisible.setValue(false);
    }
    //endregion
}
