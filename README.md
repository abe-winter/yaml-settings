## yaml-settings

[![](https://jitpack.io/v/abe-winter/yaml-settings.svg)](https://jitpack.io/#abe-winter/yaml-settings)

This is an android `View` widget that creates a custom settings screen based on a YAML

### quick how-to

* in your build.gradle (change v0.* below to newest tag):
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}
dependencies {
	compile 'com.github.abe-winter:yaml-settings:v0.0.2'
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
<com.github.abe_winter.yaml_settings.YamlSettings
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
	- the `android:id` field attr is optional; you use it to access 
* access the settings in a Fragment/Activity:
```java
public class SettingsFragment extends Fragment implements SettingsCallback {
    public SettingsFragment() {}

    // example populating a settings screen with different types from JSON
    void ondataload(JSONObject reply) throws JSONException {
        Map<String, View> view_lookup = ((YamlSettings)getView().findViewById(R.id.yaml_settings)).view_lookup;
        JSONObject body = reply.getJSONObject("body");
        for (Iterator<String> it=body.keys(); it.hasNext();){
            String key = it.next();
            if (!view_lookup.containsKey(key)){
                Log.e(TAG, "unk settings key "+key);
                continue;
            }
            if (body.isNull(key)) continue;
            switch (key){
                case "profile_picture":{
                    byte[] bytes = Base64.decode(body.getString(key), Base64.DEFAULT);
                    ((ImageSel)view_lookup.get(key)).m_img.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    break;
                }
                case "locations":{
                    StringList sl = (StringList) view_lookup.get(key);
                    JSONArray arr = body.getJSONArray(key);
                    for (int i=0;i<arr.length();++i) sl.addElt(getContext(), arr.getString(i));
                    break;
                }
                case "connect_with.kind":{
                    // radio
                    // todo: this is horrible. the indexes are going to change
                    int index = Arrays.asList("Singles", "Professionals", "Community").indexOf(body.get(key));
                    ((RadioButton)((RadioGroup)view_lookup.get(key)).getChildAt(index)).setChecked(true);
                    break;
                }
                case "email_preferences.match":
                case "email_preferences.terms":{
                    // boolean switch
                    ((Switch)view_lookup.get(key)).setChecked(body.getBoolean(key));
                    break;
                }
                default: {
                    ((TextInputEditText)view_lookup.get(key)).setText(body.getString(key));
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set listener on the YamlSettings instance.
        // Doesn't have to be in onActivityCreated, but needs to be after views have been inflated.
        ((YamlSettings)getView().findViewById(R.id.yaml_settings)).listener = this;
    }

    // callbacks from SettingsCallback interface
    public void btnclick(View sview, View v, SettingsNode node){}
    public void textchange(View sview, Editable e, String group, String name, String value) {}
    public void radiochange(View sview, CompoundButton buttonView, boolean isChecked, String group, String name, String option){}
    public void switchchange(View sview, CompoundButton buttonView, boolean isChecked, String group, String name){}
    public void listadd(StringList sl, String name, String val) {}
    public void listrm(StringList sl, final View elt, final String name, final String val) {}
    public void pickimage(ImageSel sel, String name) {}
}
```

### examples

(todo)

### tests

Sorry.

### roadmap

* [ ] include API sync logic
