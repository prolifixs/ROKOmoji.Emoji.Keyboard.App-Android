# ROKOMoji
Build your own Android custom keyboard

## About ROKOmoji
Create your own custom keyboard with ROKO Labs! We've open sourced our emoji keyboard app so you can easily build your own emoji and sticker keyboard app to promote your company and brand.

The application is utilizing ROKO Mobi - http://roko.mobi - Stickers SDK to manage your keyboard and see analytics, all for free! 

Should you have any questions or concerns, feel free to email us at help@rokolabs.com

## Android Project Settings
Import project in Android studio:

1. Import project in Android Studio
2. Add a new key to your AndroidManifest.xml file
name: ROKOMobiAPIToken
value: API key of your ROKO Mobi portal application (see below for ROKO Mobi portal information)
(for example " <meta-data android:name="ROKOMobiAPIToken" android:value="Your ROKOMobi API Token key" /> ")
3. Update release signing config in build.gradle for release application build

## Portal settings
ROKO Mobi provides app developers and product owners with a suite of tools to accelerate mobile development, engage and track customers, and measure their app's success

See here for ROKO Mobi documentation, interaction guides, and instructions:
https://docs.roko.mobi/docs/setting-up-your-account

## Activate your new keyboard
Run application on device or emulator and follow the instructions on the main screen.

## Reference Links
https://developer.android.com/guide/topics/text/image-keyboard.html
