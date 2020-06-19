# StartApp mediation adapter for AdMob

This library lets you serve ads to your apps from StartApp network via AdMob integration.

## Basic integration steps

### AdMob Console

1. Create new or edit existing Mediation Group, then click `ADD CUSTOM EVENT`

![Step 1](/images/step1.png)

2. Fill the fields `Label` and `Manual eCPM`, then click `CONTINUE`

![Step 2](/images/step2.png)

3. Fill the fields `Class Name` and `Parameter`, then click `DONE`.

```
Class Name : com.startapp.mediation.admob.StartappAdapter
Parameter  : { startappAppId : 'YOUR_APP_ID' }
```

![Step 3](/images/step3.png)

4. Make sure new item is appeared in your group, then click `SAVE`

![Step 4](/images/step4.png)

### Project

5. Add dependency on StartApp AdMob Mediation library

```
dependencies {
    // noinspection GradleDependency
    implementation 'com.startapp:admob-mediation:2.+'
}
```

6. Use [AdMob Mediation Test Suite][1] to make sure StartApp is working correctly

[1]: https://developers.google.com/admob/android/mediation-test-suite
