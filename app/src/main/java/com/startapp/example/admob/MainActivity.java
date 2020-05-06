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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.startapp.example.admob.databinding.ActivityMainBinding;
import com.startapp.example.admob.databinding.NativeAdUnifiedBinding;
import com.startapp.mediation.admob.StartappAdapter;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    @Nullable
    private AdView banner;

    @Nullable
    private AdView mrec;

    @Nullable
    private InterstitialAd interstitial;

    @Nullable
    private RewardedAd rewarded;

    @Nullable
    private UnifiedNativeAd nativeAd;

    @NonNull
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StartAppSDK.init(this, "204653131", false);
        StartAppAd.disableSplash();

        // DON'T ADD THIS LINE TO YOUR REAL PROJECT, IT ENABLES TEST ADS WHICH GIVE NO REVENUE
        StartAppSDK.setTestAdsEnabled(true);
        // -----------------------------------------------------------------------------------

        initInterstitial();
        initBanner();
        initMrec();
    }

    private void initBanner() {
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        banner = new AdView(this);
        banner.setAdUnitId(getString(R.string.bannerId));
        banner.setAdSize(AdSize.BANNER);
        banner.setId(ViewCompat.generateViewId());
        binding.layout.addView(banner, params);

        final ConstraintSet constraints = new ConstraintSet();
        constraints.clone(binding.layout);
        constraints.connect(banner.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraints.centerHorizontally(banner.getId(), ConstraintSet.PARENT_ID);
        constraints.applyTo(binding.layout);

        banner.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(MainActivity.this, "Load failed, errorCode=" + errorCode, Toast.LENGTH_SHORT).show();
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

            @Override
            public void onAdLeftApplication() {
                Toast.makeText(MainActivity.this, "Banner - onAdLeftApplication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMrec() {
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mrec = new AdView(this);
        mrec.setAdUnitId(getString(R.string.mrecId));
        mrec.setAdSize(AdSize.MEDIUM_RECTANGLE);
        mrec.setId(ViewCompat.generateViewId());
        binding.layout.addView(mrec, params);

        final ConstraintSet constraints = new ConstraintSet();
        constraints.clone(binding.layout);
        constraints.connect(mrec.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraints.centerHorizontally(mrec.getId(), ConstraintSet.PARENT_ID);
        constraints.applyTo(binding.layout);

        mrec.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(MainActivity.this, "Mrec load failed, errorCode=" + errorCode, Toast.LENGTH_SHORT).show();
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

            @Override
            public void onAdLeftApplication() {
                Toast.makeText(MainActivity.this, "Mrec - onAdLeftApplication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initInterstitial() {
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.interstitialId));
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(MainActivity.this, "Load failed, errorCode=" + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                binding.interstitialShowButton.setEnabled(true);
            }

            @Override
            public void onAdOpened() {
                Toast.makeText(MainActivity.this, "interstitial - onAdOpened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
                Toast.makeText(MainActivity.this, "interstitial - onAdClosed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClicked() {
                Toast.makeText(MainActivity.this, "interstitial - onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(MainActivity.this, "interstitial - onAdImpression", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                Toast.makeText(MainActivity.this, "interstitial - onAdLeftApplication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * you can as well write in admob custom event's panel optional parameter
     * which must be in json format, unused fields can be omitted:
     * {adTag:'interstitialTagFromServer', interstitialMode:'OVERLAY', minCPM:0.02, muteVideo:false}
     * each value from the admob panel overrides corresponding value from extras bundle
     */
    public void onClickLoadInterstitial(@NonNull View view) {
       if (interstitial == null) {
           return;
       }

       // optionally you can set additional parameters for Startapp interstitial
       final Bundle extras = new StartappAdapter.Extras.Builder()
               .setAdTag("interstitialTagFromAdRequest")
               .setInterstitialMode(StartappAdapter.Mode.OFFERWALL)
               .muteVideo()
               .setMinCPM(0.01)
               .toBundle();

       interstitial.loadAd(new AdRequest.Builder()
               .addCustomEventExtrasBundle(StartappAdapter.class, extras)
               .build());
    }

    public void onClickShowInterstitial(@NonNull View view) {
        view.setEnabled(false);

        if (interstitial == null) {
            return;
        }

        interstitial.show();
    }

    /**
     * you can as well write in admob custom event's panel optional parameter
     * which must be in json format, unused fields can be omitted:
     * {adTag:'bannerTagFromServer', minCPM:0.02, is3DBanner:false}
     * each value from the admob panel overrides corresponding value from extras bundle
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
                .addCustomEventExtrasBundle(StartappAdapter.class, extras)
                .build());
    }

    /**
     * you can as well write in admob custom event's panel optional parameter
     * which must be in json format, unused fields can be omitted:
     * {adTag:'mrecTagFromServer', minCPM:0.03, is3DBanner:false}
     * each value from the admob panel overrides corresponding value from extras bundle
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
                .addCustomEventExtrasBundle(StartappAdapter.class, extras)
                .build());
    }

    /**
     * you can as well write in admob custom event's panel optional parameter
     * which must be in json format, unused fields can be omitted:
     * {adTag:'rewardedTagFromServer', minCPM:0.02, muteVideo:false}
     * each value from the admob panel overrides corresponding value from extras bundle
     */
    public void onClickLoadRewarded(@NonNull View view) {
        //RewardedAd is a one-time-use object, so it should be recreated on every new load
        rewarded = new RewardedAd(this, getString(R.string.rewardedId));

        // optionally you can set additional parameters for Startapp interstitial
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("rewardedTagFromAdRequest")
                .muteVideo()
                .setMinCPM(0.01)
                .toBundle();

        final RewardedAdLoadCallback loadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                binding.rewardedShowButton.setEnabled(true);
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                binding.rewardedShowButton.setEnabled(false);

                Toast.makeText(MainActivity.this, "Load failed, errorCode=" + errorCode, Toast.LENGTH_SHORT).show();
            }
        };

        rewarded.loadAd(new AdRequest.Builder()
                .addNetworkExtrasBundle(StartappAdapter.class, extras)
                .build(), loadCallback);
    }

    public void onClickShowRewarded(@NonNull View view) {
        view.setEnabled(false);

        if (rewarded == null || !rewarded.isLoaded()) {
            return;
        }

        rewarded.show(this, new RewardedAdCallback() {
            @Override
            public void onRewardedAdOpened() {
                Toast.makeText(MainActivity.this, "rewarded - onRewardedAdOpened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedAdClosed() {
                Toast.makeText(MainActivity.this, "rewarded - onRewardedAdClosed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                Toast.makeText(MainActivity.this, "rewarded - onUserEarnedReward: type=" + rewardItem.getType() + " amount=" + rewardItem.getAmount(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedAdFailedToShow(int errorCode) {
                Toast.makeText(MainActivity.this, "rewarded - show failed, errorCode=" + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * you can as well write in admob custom event's panel optional parameter
     * which must be in json format, unused fields can be omitted:
     * {adTag:'nativeTagFromServer', minCPM:0.02, nativeImageSize:'SIZE150X150', nativeSecondaryImageSize:'SIZE340X340'}
     * each value from the admob panel overrides corresponding value from extras bundle
     */
    public void onClickLoadNative(@NonNull View view) {
        final AdLoader loader = new AdLoader.Builder(this, getString(R.string.nativeId))
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull UnifiedNativeAd unifiedNativeAd) {
                        nativeAd = unifiedNativeAd;
                        binding.nativeShowButton.setEnabled(true);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        binding.nativeShowButton.setEnabled(false);

                        Toast.makeText(MainActivity.this, "Load failed, errorCode=" + errorCode, Toast.LENGTH_SHORT).show();
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

                    @Override
                    public void onAdLeftApplication() {
                        Toast.makeText(MainActivity.this, "native - onAdLeftApplication", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        // optionally you can set additional parameters
        final Bundle extras = new StartappAdapter.Extras.Builder()
                .setAdTag("nativeTagFromAdRequest")
                .setMinCPM(0.01)
                .setNativeImageSize(StartappAdapter.Size.SIZE72X72)
                .setNativeSecondaryImageSize(StartappAdapter.Size.SIZE150X150)
                .toBundle();

        loader.loadAd(new AdRequest.Builder()
                .addCustomEventExtrasBundle(StartappAdapter.class, extras)
                .build());
    }

    public void onClickShowNative(@NonNull View view) {
        final NativeAdUnifiedBinding adUnifiedBinding = NativeAdUnifiedBinding.inflate(getLayoutInflater());
        populateUnifiedNativeAdView(adUnifiedBinding.unifiedView, adUnifiedBinding);

        binding.nativeAdPlaceholder.removeAllViews();
        binding.nativeAdPlaceholder.addView(adUnifiedBinding.getRoot());

        view.setEnabled(false);
    }

    private void populateUnifiedNativeAdView(@NonNull UnifiedNativeAdView adView, @NonNull NativeAdUnifiedBinding unifiedBinding) {
        if (nativeAd == null) {
            return;
        }

        unifiedBinding.headlineTextView.setText(nativeAd.getHeadline());
        adView.setHeadlineView(unifiedBinding.headlineTextView);

        // the asset is populated automatically, so there's one less step
        adView.setMediaView(unifiedBinding.mediaView);

        final List<NativeAd.Image> images = nativeAd.getImages();
        if (images != null && images.size() > 0) {
            unifiedBinding.imageView.setImageDrawable(images.get(0).getDrawable());
            adView.setImageView(unifiedBinding.imageView);
        }

        unifiedBinding.callToActionTextView.setText(nativeAd.getCallToAction());
        adView.setCallToActionView(unifiedBinding.callToActionTextView);

        unifiedBinding.bodyTextView.setText(nativeAd.getBody());
        adView.setBodyView(unifiedBinding.bodyTextView);

        unifiedBinding.advertiserTextView.setText(nativeAd.getAdvertiser());
        adView.setAdvertiserView(unifiedBinding.advertiserTextView);

        final NativeAd.Image image = nativeAd.getIcon();
        if (image != null) {
            unifiedBinding.logoImageView.setImageDrawable(image.getDrawable());
            adView.setIconView(unifiedBinding.logoImageView);
        }

        final String price = nativeAd.getPrice();
        if (price != null) {
            unifiedBinding.priceTextView.setText(price);
            adView.setPriceView(unifiedBinding.priceTextView);
        }

        final Double rating = nativeAd.getStarRating();
        if (rating != null) {
            unifiedBinding.ratingTextView.setText(String.valueOf(rating));
            adView.setStarRatingView(unifiedBinding.ratingTextView);
        }

        final String store = nativeAd.getStore();
        if (store != null) {
            unifiedBinding.storeTextView.setText(store);
            adView.setStoreView(unifiedBinding.storeTextView);
        }

        adView.setNativeAd(nativeAd);
    }
}
