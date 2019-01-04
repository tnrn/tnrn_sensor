[![npm][npm-badge]][npm]
[![react-native][rn-badge]][rn]
[![MIT][license-badge]][license]
[![bitHound Score][bithound-badge]][bithound]
[![Downloads](https://img.shields.io/npm/dm/tnrn_sensor.svg)](https://www.npmjs.com/package/tnrn_sensor)

埋点 for [React Native][rn].

[**Support me with a Follow**](https://github.com/winterwd/followers)

[npm-badge]: https://img.shields.io/npm/v/tnrn_sensor.svg
[npm]: https://www.npmjs.com/package/tnrn_sensor
[rn-badge]: https://img.shields.io/badge/react--native-v0.40-05A5D1.svg
[rn]: https://facebook.github.io/react-native
[license-badge]: https://img.shields.io/dub/l/vibe-d.svg
[license]: https://raw.githubusercontent.com/rnkit/tnrn_sensor/master/LICENSE
[bithound]: https://www.bithound.io/github/rnkit/tnrn_sensor

## Getting Started

First, `cd` to your RN project directory, and install RNMK through [rnpm](https://github.com/rnpm/rnpm) . If you don't have rnpm, you can install RNMK from npm with the command `npm i -S tnrn_sensor` and link it manually (see below).

### iOS

* #### React Native < 0.29 (Using rnpm)

  `rnpm install tnrn_sensor`

* #### React Native >= 0.29
  `$npm install -S tnrn_sensor`

  `$react-native link tnrn_sensor`

#### Manually
1. Add `node_modules/tnrn_sensor/ios/RNKitSensor.xcodeproj` to your xcode project, usually under the `Libraries` group
1. Add `libRNKitSensor.a` (from `Products` under `RNKitSensor.xcodeproj`) to build target's `Linked Frameworks and Libraries` list
1. Add tnrn_sensor framework to `$(PROJECT_DIR)/Frameworks.`

### Android

* #### React Native < 0.29 (Using rnpm)

  `rnpm install tnrn_sensor`

* #### React Native >= 0.29
  `$npm install -S tnrn_sensor`

  `$react-native link tnrn_sensor`

#### Manually
1. JDK 7+ is required
1. Add the following snippet to your `android/settings.gradle`:

  ```gradle
include ':tnrn_sensor'
project(':tnrn_sensor').projectDir = new File(rootProject.projectDir, '../node_modules/tnrn_sensor/android/app')
  ```
  
1. Declare the dependency in your `android/app/build.gradle`
  
  ```gradle
  dependencies {
      ...
      compile project(':tnrn_sensor')
  }
  ```
  
1. Import `import io.rnkit.sensor.SensorPackage;` and register it in your `MainActivity` (or equivalent, RN >= 0.32 MainApplication.java):

  ```java
  @Override
  protected List<ReactPackage> getPackages() {
      return Arrays.asList(
              new MainReactPackage(),
              new SensorPackage()
      );
  }
  ```

Finally, you're good to go, feel free to require `tnrn_sensor` in your JS files.

Have fun! :metal:

## Questions

Feel free to [contact me](mailto:liwei0990@gmail.com) or [create an issue](https://github.com/rnkit/tnrn_sensor/issues/new)

> made with ♥
