## PinYourAddressOnTheMap is a simple app to help getting started with Huawei Mobile Services

### Application description

When you open the app, the app locates user’s current location and shows
Huawei map. Marks user’s current location on the map with Star and popup user’s
current address when click the Star. (Address description, not the
Geocoding value) Display a pin on the map. User can move and click on the map, the pin
will move to the point as user clicks. Popup the address description when
user pins a point on the map.

### First you need to create an account on https://developer.huawei.com/consumer/en/ so you can use Huawei mobile services 

Step one register here https://developer.huawei.com/consumer/en/ you need an account to start using Huawei mobile serives 
on this app (PinYourAddressOnTheMap) we are going to use mapKit and location Kit

### when you've registered please follow these steps to create you your first app on in AppGallery Connect
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#0

Now open your application on android studio 

### configure maven repository address for the HMS SDK open build.gradle on the project layer
please see here from the PinYourAddressOnTheMap project

''''
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        maven { url 'http://developer.huawei.com/repo/' } // HUAWEI Maven repository
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:4.1.0-alpha06'
        classpath 'com.huawei.agconnect:agcp:1.2.0.300'   //HUAWEI agcp plugin
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'http://developer.huawei.com/repo/' } // HUAWEI Maven repository
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
'''

link to the code : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/build.gradle


### please look at these examples also from Huawei

#### mapKit documentation
Doc: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/hms-map-
v4-abouttheservice
#### Codelab to also help you get started with mapit
Codelab: https://developer.huawei.com/consumer/en/codelab/HMSMapKit/index.html#0

#### location kit documentation
Doc: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/location-
#### Codelab to also help you get started with location kit
Codelab: https://developer.huawei.com/consumer/en/codelab/HMSLocationKit/index.html#0

when you are done with the above steps it's should be very easy to create this app



1. create your app on android studio select empty activity from the provided templates

reference: https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#2

2.


