# Start.io (StartApp) mediation adapter for AdMob

This library lets you serve ads to your apps from Start.io (StartApp) network via AdMob integration.

## Demo app

The demo [app](/app) is fully workable. Change ad unit IDs in file [ad_ids.xml](/app/src/main/res/values/ad_ids.xml) in order to try your integration.

## Basic integration steps

### AdMob Console

1. Create new or edit existing Mediation Group, then click `Add custom event`.

![Step 1](/images/step1.png)

2. Fill the fields `Label` and `Manual eCPM`, then click `Continue`.

![Step 2](/images/step2.png)

3. Create a mapping for each ad unit.

![Step 3](/images/step3.png)

4. Fill the fields `Mapping name`, `Class Name` and `Parameter`, then click `Done`.

Make sure to change string `YOUR_APP_ID` with the actual app ID from the [portal.start.io](https://portal.start.io).

Mapping name: `Start.io`

Class Name: `io.start.mediation.admob.StartIoAdapter`

Parameter: `{ appId : 'YOUR_APP_ID' }`

**Advanced option**:

Pass `adTag` to the parameter: `{ appId : 'YOUR_APP_ID', adTag: 'YOUR_AD_TAG' }`

![Step 4](/images/step4.png)

5. Select a mapping, then click `Done`.

![Step 5](/images/step5.png)

5. Make sure new ad source is appeared in list, then click `Save`.

![Step 6](/images/step6.png)

### Project

6. Add dependency on Start.io (StartApp) AdMob Mediation library.

```
dependencies {
    implementation 'io.start:admob-mediation:3.+'
}
```

7. Use [Ad inspector][1] to make sure your integration is working correctly.

[1]: https://developers.google.com/admob/android/ad-inspector
