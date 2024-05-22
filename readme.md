# Start.io (StartApp) mediation adapter for AdMob

This library lets you serve ads to your apps from Start.io (StartApp) network via AdMob integration.

## Demo app

The demo [app](/app) is fully workable. Change ad unit IDs in file [ad_ids.xml](/app/src/main/res/values/ad_ids.xml) in order to try your integration.

## Basic integration steps

### AdMob Console

1. Create new or edit existing Mediation Group, then click `ADD CUSTOM EVENT`

![Step 1](/images/step1.png)

2. Fill the fields `Label` and `Manual eCPM`, then click `CONTINUE`

![Step 2](/images/step2.png)

3. Fill the fields `Class Name` and `Parameter`, then click `DONE`.

```
Class Name : io.start.mediation.admob.StartIoAdapter
Parameter  : { appId : 'YOUR_APP_ID' }
```

![Step 3](/images/step3.png)

4. Make sure new item is appeared in your group, then click `SAVE`

![Step 4](/images/step4.png)

### Project

5. Add dependency on Start.io (StartApp) AdMob Mediation library

```
dependencies {
    implementation 'com.startapp:admob-mediation:3.+'
}
```

6. Use [Ad inspector][1] to make sure your integration is working correctly

[1]: https://developers.google.com/admob/android/ad-inspector
