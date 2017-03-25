## yaml-settings

[![](https://jitpack.io/v/abe-winter/yaml-settings.svg)](https://jitpack.io/#abe-winter/yaml-settings)

This is an android `View` widget that creates a custom settings screen based on a YAML

### quick how-to

* in your build.gradle (change v0.0.0 below to newest tag on github):
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}
dependencies {
	compile 'com.github.abe-winter:yaml-settings:v0.0.0'
}
```
* in res/raw/settings.yaml, create something like:
```yaml
# todo: need better docs for this
- type: group
  name: your_info
  children:
  - type: email
    name: email
    description: "We use your email to communicate with you, but don't share it with other users."
  - {type: text, name: display_name}
- {type: image, name: profile_picture}
- type: group
  name: connect_with
  children:
  - type: radio
    name: kind
    options: ["Singles", "Professionals", "Community"]
- type: stringlist
  name: locations
  description: Add postal code
- type: group
  name: email_preferences
  children:
    - type: switch
      name: match
      description: "Email when someone accepts a match (at most once daily)."
    - type: switch
      name: terms
      description: "Email when there's a security notice or a change in our terms."
```
* in your view XML
```xml
<app_package.YamlSettings
    android:id="@+id/yaml_settings"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:yamlRawId="@raw/settings"
    app:strListDelIcon="@drawable/ic_delete_black_24dp"
    android:padding="18dp"/>
```
* notes on view XML
	- `app:yamlRawId` has to match the res/raw/whatever.yml file you created
	- `app:strListDelIcon` has to be a drawable in your project. Leave it out if your yaml file doesn't use the StringList widget
	- I'm not sure why, but this needs to be app_package.YamlSettings (as opposed to com.github.abe_winter.yaml_settings.YamlSettings). app_package is whatever you set in the AndroidManifest.xml for your app.
	- the `android:id` field attr is optional; you use it to access 

### examples

(todo)

### tests

Sorry.
