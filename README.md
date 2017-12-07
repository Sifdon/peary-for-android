# peary for Android

[//]: # (References)
[firebase app builds]: https://firebase.googleblog.com/2016/08/organizing-your-firebase-enabled-android-app-builds.html
[alternate firebase app builds]: https://stackoverflow.com/a/34364376
[flavors and built variants]: https://blog.davidmedenjak.com/android/2016/11/09/build-variants.html
[graphische website]: http://www.graphische.net/
[coding style]: #coding-style
[license readme]: LICENSE
[code style rules]: https://source.android.com/setup/code-style
[peary banner]: imgs/peary-banner.jpg

peary is a shopping list which is linked to a virtual refrigerator that notifies the user if a product is about to expire. It's a lightweight app and lets you share groceries with your flatmates and beloved ones.

The application has a many-to-many relationship and uses the BaaS (Backend as a Service) NoSQL database from Firebase.

![peary][peary banner]

## Introduction
peary was a collaborative work and diploma project for the Viennese university [Die Graphische][graphische website] by Alexander Lechner and Sascha Kovacs.

The application is for students who handle food responsibly and want to avoid food waste by consuming groceries before the expiration date. The product is a real-time grocery list that provides a notification service for soon expiring products to help save the environment. Unlike other shopping list solutions like *Out of Milk* or *Wunderlist*, peary keeps track of the contents of the fridge and their expiration date.

The idea arose from the uneconomical use of food in many private households. Approximately 53% of food waste in Austria is produced by private households. Food manufacturers have to provide a best-before date for their products because of food regulation. Even salt manufacturers who advertise their salt being over thousands of years old are forced to provide a best-before date. Unfortunately, most people take the best-before date very serious and dump the still edible food.

The project started in September 2015 and ended in September 2016. In order to graduate successfully the students had to develop a fully functional mobile application either for iOS or Android by the end of September 2016. Additionally, an intensive research work and project documentation had to be done. Alexander Lechner's research work was about "Graphical User Interfaces and User Experience" and Sascha Kovacs' research work was about "The development of Android and iOS applications".

Alexander Lechner has developed the Android app and Sascha Kovacs has developed the iOS app. Both students have learned the operating system's official programming language (Java for Android & Swift for iOS) within one year autodidactically.

The corporate design and app icons were created by Alexander Lechner. The layout and screen designs were created by Sascha Kovacs.

## Contribution
We actively welcome any type of support and contribution to this project. 

To submit a pull request: 
1. fork the repo from the ``master`` branch
2. take a look at open issues and our milestones
3. update the documentation accordingly

Also, we encourage you to adhere our [Coding Style][coding style].

If you like to develop your own version of peary, feel free to do so! But please only in compliance with our [license][license readme] and the following requirements:
1. **Please do not use the name *peary* (or word modifications of any kind e.g. upper and lower case of certain letters, puns, etc.) for your app.**
2. **Please do not use or modify our logo, app icon, and artworks (e.g. splash screen, background images, etc.) for your app.**
3. **In order to comply with the license please make sure to publish your code as well.**

## Setup

### Staging Environment with Firebase
This repo uses a staging environment: Production (prod) and Development (dev). The Production environment uses the database from real customers and the Development environment uses an own database for debugging and development purposes.

To use a staging environment in Firebase you have to create two projects in the Firebase console (one for Production and one for Development) and in both projects, you have to create two apps (one for release build and one for debug build).

After this step you should have:
* 1 Production-project with 1 release-app and 1 debug-app
* 1 Development-project with 1 release-app and 1 debug-app

When you're done creating the projects and their apps you have to download the ``google-services.json`` file of each project. It is important that you download the ``google-services.json`` files **after** both apps - release and debug - are created because the files contain information of both apps.

After you have downloaded the ``google-services.json`` files, you have to create a *dev* and a *prod* folder in your *src* folder and move your ``google-services.json`` files in these folders accordingly like shown below: 

``` 
app/
    src/
        main/
        dev/
             google-services.json (for dev only)
        prod/
             google-services.json (for prod only)
```

We're almost done! Now open the ``app/src/build.gradle`` file for some configurations. Between the ``android { ... }`` block put the following at the bottom:
````gradle
android {

    flavorDimensions "env"
    productFlavors {
        dev {
            dimension "env"
        }
        prod{
            dimension "env"
        }
    }
}
````
After the Gradle sync you should be able to see ``devDebug``, ``devRelease``, ``prodDebug`` & ``prodRelease`` if you navigate to the ``Build Variants`` tab at the very left of Android Studio and click on the build variant.

**Important:** If you clone this repo and use my ``build.gradle`` file, please notice that I have set an application suffix in my debug build types:
````gradle
android {
    buildTypes {
        debug {
            applicationIdSuffix ".debug"
        }
    }
}
````
If you want this as well, you have to set as package name *com.yourapp.app.**debug*** in both of your debug apps in the Production-project and the Development-project. Otherwise, you will get an error from your IDE and will be unable to compile your app.

If you get stuck please take a look at [the official blog post from Firebase about Android app builds][firebase app builds]. Also, this [Stackoverflow answer][alternate firebase app builds] might help you get your environment up and running.

If you are interested in working with product flavors and build variants, you can find out more about [Working with multiple flavors and build variants on David Medenjak's blog][flavors and built variants].

### Signing your app
If you want to sign your app for a release build and save your settings (store file, passwords, etc.) in the app's ``build.gradle`` file, then do the following steps:
1. Add the following to the ``android { ... }`` block:
````gradle
android {
     signingConfigs {
            release {
                storeFile file(keyStoreFile)
                storePassword keyStorePassword
                keyAlias keyAliasName
                keyPassword keyAliasPassword
            }
        }
        buildTypes {
            release {
                signingConfig signingConfigs.release
            }
        }
}
````
2. Set your data in the ``gradle.properties`` file like so:
````
keyStoreFile=your/path/to/keys.jks
keyStorePassword=mySecretKeyStorePassword
keyAliasName=myAppReleaseName
keyAliasPassword=mySecretPrivateKeyPassword
````
I have included a ``gradle.properties.example`` file which you can use for this step.

**Important**: Since your ``gradle.properties`` contains sensitive data, this file should never be tracked by your Version Control System. For safety reasons the ``gradle.properties`` is already defined in the ``.gitignore`` file (only for Git users!).

When you're done and you want to run a release build then type `gradlew assembleRelease` in the projects root directory either from the built-in Terminal in Android Studio or from the Command Line on Windows.
On Linux run `./gradlew assembleRelease` in the projects root directory.
If you are using this method then Android Studio is building the .apk-File in ``yourProject/app/build/outputs/apk/prod/release``.

Alternatively, you can use ``Build`` > ``Generate Signed APK...`` in the top menu of Android Studio.

## Coding Style
Basically, the project aims to follow the [Java Code Style Rules][code style rules]. Here are the most important:
* Tab size and Indent are 4
* Continuation indent is 8
* Opening braces to appear on the same line as code
* Same variables to declare on the same line if possible 
* Use CamelCase notation
* Non-public, non-static field names start with  ``m``
* Static field names start with ``s``
* Other fields start with a lower case letter
* Public static final fields (constants) are ``ALL_CAPS_WITH_UNDERSCORES``.

**Hint:** When working on a file, it's always recommended to reformat the code before committing any changes by navigating to the menu ``Code`` > ``Reformat Code`` in Android Studio.

## License
peary is licensed under the [GNU GPLv2][license readme].