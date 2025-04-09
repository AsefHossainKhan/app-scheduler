### Download Link
The application release APK can be downloaded from [here](https://github.com/AsefHossainKhan/app-scheduler/releases/download/v1.0/app-scheduler.apk)

### Limitations
An application is not allowed to turn on another application while it is in the background or off (idle/doze mode). It is not allowed due to user privacy and overall user flexibility reasons. The permission document can be found [here](https://developer.android.com/guide/components/activities/background-starts). What we can do is display a notification which the user can tap to open the respective application, that is allowed.

### My approach
While I initially attempted to still launch the other applications activity from the background, I found that on latest Android devices that is nearly impossible, as such, I have opted for an additional notification method. 
So far, from what I have found, if the application is on in the screen, then the other application will be launched directly. Regardless, there are no issues with launching the notification.

### UI/UX
The UI/UX has been kept minimal. It ensures easier extensibility for a possible future bespoke option.
