/**
 * Copyright 2020 StartApp Inc
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

package com.startapp.example.admob;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

import com.google.android.ads.mediationtestsuite.MediationTestSuite;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.startapp.example.admob.databinding.ActivityMainBinding;
import com.startapp.example.admob.databinding.NativeAdUnifiedBinding;
import com.startapp.mediation.admob.StartappAdapter;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        MobileAds.initialize(this);

        // DON'T ADD THIS LINE TO YOUR REAL PROJECT, IT ENABLES TEST ADS WHICH GIVE NO REVENUE
        StartAppSDK.setTestAdsEnabled(true);
        // -----------------------------------------------------------------------------------

        initBanner();
        initMrec();

        // uncomment if you want to run AdMob's Test Suite
        // MediationTestSuite.launch(this);
    }

    //region Banner
    @Nullable
    private AdView banner;

    private void initBanner() {
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        banner = new AdView(this);
        banner.setAdUnitId(getString(R.string.bannerId));
        banner.setAdSize(AdSize.BANNER);
        banner.setId(ViewCompat.generateViewId());
        viewBinding.layout.addView(banner, params);

        final ConstraintSet constraints = new ConstraintSet();
        constraints.clone(viewBinding.layout);
        constraints.connect(banner.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraints.centerHorizontally(banner.getId(), ConstraintSet.PARENT_ID);
        constraints.applyTo(viewBinding.layout);

        banner.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Toast.makeText(MainActivity.this, "Load failed, " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                Toast.makeText(MainActivity.this, "Banner - onAdLoaded", Toast.LENGTH_SHORT).show();

                if (mrec != null && mrec.getVisibility() == View.VISIBLE) {
                    mrec.setVisibility(View.GONE);
                }

                banner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdOpened() {
                Toast.makeText(MainActivity.this, "Banner - onAdOpened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
                Toast.makeText(MainActivity.this, "Banner - onAdClosed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClicked() {
                Toast.makeText(MainActivity.this, "Banner - onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(MainActivity.this, "Banner - onAdImpression", Toast.LENGTH_SHORT).show();
            }
        });

        // prevent navite ad view covering
        banner.setVisibility(View.GONE);
    }

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused fields can be omitted:
     * {startappAppId:'204653131', adTag:'bannerTagFromServer', minCPM:0.02, is3DBanner:false}
     * each value from the admob interface overrides corresponding value from the extras bundle
     */
    public void onClickLoadBanner(@NonNull View view) {
        if (banner == null) {
            return;
        }

        // optionally you can set additional parameters for Startapp banner
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("bannerTagFromAdRequest")
                .enable3DBanner()
                .setMinCPM(0.01)
                .toBundle();

        banner.loadAd(new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build());
    }
    //endregion

    //region Medium Rectangle
    @Nullable
    private AdView mrec;

    private void initMrec() {
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mrec = new AdView(this);
        mrec.setAdUnitId(getString(R.string.mrecId));
        mrec.setAdSize(AdSize.MEDIUM_RECTANGLE);
        mrec.setId(ViewCompat.generateViewId());
        viewBinding.layout.addView(mrec, params);

        final ConstraintSet constraints = new ConstraintSet();
        constraints.clone(viewBinding.layout);
        constraints.connect(mrec.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraints.centerHorizontally(mrec.getId(), ConstraintSet.PARENT_ID);
        constraints.applyTo(viewBinding.layout);

        mrec.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Toast.makeText(MainActivity.this, "Mrec load failed, " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                Toast.makeText(MainActivity.this, "Mrec - onAdLoaded", Toast.LENGTH_SHORT).show();

                if (banner != null && banner.getVisibility() == View.VISIBLE) {
                    banner.setVisibility(View.GONE);
                }

                mrec.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdOpened() {
                Toast.makeText(MainActivity.this, "Mrec - onAdOpened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
                Toast.makeText(MainActivity.this, "Mrec - onAdClosed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClicked() {
                Toast.makeText(MainActivity.this, "Mrec - onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(MainActivity.this, "Mrec - onAdImpression", Toast.LENGTH_SHORT).show();
            }
        });

        // prevent navite ad view covering
        mrec.setVisibility(View.GONE);
    }

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused fields can be omitted:
     * {startappAppId:'204653131', adTag:'mrecTagFromServer', minCPM:0.02, is3DBanner:false}
     * each value from the admob interface overrides corresponding value from the extras bundle
     */
    public void onClickLoadMrec(@NonNull View view) {
        if (mrec == null) {
            return;
        }

        // optionally you can set additional parameters for Startapp banner
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("mrecTagFromAdRequest")
                .enable3DBanner()
                .setMinCPM(0.01)
                .toBundle();

        mrec.loadAd(new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build());
    }
    //endregion

    //region Interstitial
    @Nullable
    private InterstitialAd interstitial;

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused by you fields can be omitted:
     * {startappAppId:'204653131', adTag:'interstitialTagFromServer', interstitialMode:'OVERLAY', minCPM:0.02, muteVideo:false}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void onClickLoadInterstitial(@NonNull View view) {
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
                // The interstitial reference will be null until
                // an ad is loaded.
                interstitial = interstitialAd;
                setupInterstitialDisplayCallbacks();

                viewBinding.interstitialShowButton.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Toast.makeText(MainActivity.this, "interstitial load failed, " + loadAdError.getMessage(), Toast.LENGTH_SHORT).show();
                interstitial = null;
            }
        });
    }

    private void setupInterstitialDisplayCallbacks() {
        if (interstitial == null) {
            return;
        }

        interstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Toast.makeText(MainActivity.this, "interstitial - onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                // Called when fullscreen content failed to show.
                Toast.makeText(MainActivity.this, "interstitial - onAdFailedToShowFullScreenContent, " + adError.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
                Toast.makeText(MainActivity.this, "interstitial - onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();

                interstitial = null;
            }
        });

        interstitial.setOnPaidEventListener(new OnPaidEventListener() {
            @Override
            public void onPaidEvent(@NonNull AdValue adValue) {
                Toast.makeText(MainActivity.this, "interstitial - setOnPaidEventListener", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickShowInterstitial(@NonNull View view) {
        view.setEnabled(false);

        if (interstitial == null) {
            return;
        }

        interstitial.show(this);
    }
    //endregion

    //region Rewarded
    @Nullable
    private RewardedAd rewarded;

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused by you fields can be omitted:
     * {startappAppId:'204653131', adTag:'rewardedTagFromServer', minCPM:0.02, muteVideo:false}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void onClickLoadRewarded(@NonNull View view) {
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
                rewarded = rewardedAd;
                viewBinding.rewardedShowButton.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                viewBinding.rewardedShowButton.setEnabled(false);
                Toast.makeText(MainActivity.this, "Load failed: " + loadAdError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickShowRewarded(@NonNull View view) {
        view.setEnabled(false);

        if (rewarded == null) {
            return;
        }

        rewarded.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Toast.makeText(MainActivity.this, "rewarded - onAdFailedToShowFullScreenContent: " + adError, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                rewarded = null;
                Toast.makeText(MainActivity.this, "rewarded - onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                rewarded = null;
                Toast.makeText(MainActivity.this, "rewarded - onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(MainActivity.this, "rewarded - onAdImpression", Toast.LENGTH_SHORT).show();
            }
        });

        rewarded.show(this, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                Toast.makeText(MainActivity.this, "rewarded - onUserEarnedReward: type=" + rewardItem.getType() + " amount=" + rewardItem.getAmount(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //endregion

    //region Native
    @Nullable
    private NativeAd nativeAdObject;

    /**
     * you can as well write to admob network custom event interface optional parameter
     * which must be in json format, unused fields can be omitted:
     * {startappAppId:'204653131', adTag:'nativeTagFromServer', minCPM:0.02, nativeImageSize:'SIZE340X340', nativeSecondaryImageSize:'SIZE72X72'}
     * each value from the admob interface overrides corresponding value from the extras map
     */
    public void onClickLoadNative(@NonNull View view) {
        // clear previous ad
        viewBinding.nativeAdPlaceholder.removeAllViews();

        final AdLoader loader = new AdLoader.Builder(this, getString(R.string.nativeId))
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        nativeAdObject = nativeAd;
                        viewBinding.nativeShowButton.setEnabled(true);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        viewBinding.nativeShowButton.setEnabled(false);

                        Toast.makeText(MainActivity.this, "Load failed: " + adError, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdOpened() {
                        Toast.makeText(MainActivity.this, "native - onAdOpened", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClosed() {
                        Toast.makeText(MainActivity.this, "native - onAdClosed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClicked() {
                        Toast.makeText(MainActivity.this, "native - onAdClicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdImpression() {
                        Toast.makeText(MainActivity.this, "native - onAdImpression", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        // optionally you can set additional parameters
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("nativeTagFromAdRequest")
                .setMinCPM(0.01)
                .setNativeImageSize(StartappAdapter.Size.SIZE150X150)
                .setNativeSecondaryImageSize(StartappAdapter.Size.SIZE100X100)
                .toBundle();

        loader.loadAd(new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build());
    }

    public void onClickShowNative(@NonNull View view) {
        final NativeAdUnifiedBinding adUnifiedBinding = NativeAdUnifiedBinding.inflate(getLayoutInflater());
        populateNativeAdView(adUnifiedBinding.nativeView, adUnifiedBinding);

        viewBinding.nativeAdPlaceholder.addView(adUnifiedBinding.getRoot());

        view.setEnabled(false);
    }

    private void populateNativeAdView(@NonNull NativeAdView adView, @NonNull NativeAdUnifiedBinding unifiedBinding) {
        if (nativeAdObject == null) {
            return;
        }

        unifiedBinding.headlineTextView.setText(nativeAdObject.getHeadline());
        adView.setHeadlineView(unifiedBinding.headlineTextView);

        // the asset is populated automatically, so there's one less step
        adView.setMediaView(unifiedBinding.mediaView);

        final List<NativeAd.Image> images = nativeAdObject.getImages();
        if (images.size() > 0) {
            unifiedBinding.imageView.setImageDrawable(images.get(0).getDrawable());
            adView.setImageView(unifiedBinding.imageView);
        }

        unifiedBinding.callToActionTextView.setText(nativeAdObject.getCallToAction());
        adView.setCallToActionView(unifiedBinding.callToActionTextView);

        unifiedBinding.bodyTextView.setText(nativeAdObject.getBody());
        adView.setBodyView(unifiedBinding.bodyTextView);

        unifiedBinding.advertiserTextView.setText(nativeAdObject.getAdvertiser());
        adView.setAdvertiserView(unifiedBinding.advertiserTextView);

        final NativeAd.Image image = nativeAdObject.getIcon();
        if (image != null) {
            unifiedBinding.logoImageView.setImageDrawable(image.getDrawable());
            adView.setIconView(unifiedBinding.logoImageView);
        }

        final String price = nativeAdObject.getPrice();
        if (price != null) {
            unifiedBinding.priceTextView.setText(price);
            adView.setPriceView(unifiedBinding.priceTextView);
        }

        final Double rating = nativeAdObject.getStarRating();
        if (rating != null) {
            unifiedBinding.ratingTextView.setText(String.valueOf(rating));
            adView.setStarRatingView(unifiedBinding.ratingTextView);
        }

        final String store = nativeAdObject.getStore();
        if (store != null) {
            unifiedBinding.storeTextView.setText(store);
            adView.setStoreView(unifiedBinding.storeTextView);
        }

        adView.setNativeAd(nativeAdObject);
    }
    //endregion
}
