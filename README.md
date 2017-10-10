# DocsFragileBackend
Duct tape backend

=========

DocsFragileBackend is a quick-and-dirty way to supply your Android app with dynamic data, backed by a Google spreadsheet.

Add it to your app's build.gradle file:

```
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.github.briandherbert:DocsFragileBackend:v0.3'
}
```
and call 

```
DuctTapeBackend.downloadGoogleSpreadsheetData(String key, DuctTapeBackend.DownloadGoogleSpreadsheetDataListener listener)
```

where ***key*** is the unique key in the doc's url, something like ***18JyepUBU2-QAF4agQo7BI25fe5gARfxBr5AvBHFkgpg***. 
YOU MUST MAKE THE DOC PUBLICLY VISIBLE IN SHARING SETTINGS.

In the callback, you can use a provided utility to parse the returned CSV doc data into rows and columns:


```    
@Override
public void onSpreadsheetDataLoaded(String csv) {
    try {
        Log.v(TAG, "Got spreadsheet data");
        String[][] rowsCols = DuctTapeBackend.parseCsvToRowColData(csv);
    } catch (Exception e) {
    }
}
```
