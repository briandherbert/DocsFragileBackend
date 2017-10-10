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


Demo
-----------

On the left is the Google Docs spreadsheet, on the right is an Android emulator running the WhatIHadForBreakfast app. The app uses DuctTapeBackend to fetch data from the spreadsheet and then updates the UI:

![](/../photos/photos/breakfastQuick.gif?raw=true "Optional Title")

The loop makes it a little hard to folllow, but we start with "Fatty Special", then change to "Lucky Charms". If that gif isn't working, here are screenshots of the doc and emulator [before][2] and [after][3]. The title, background color, image, and ad presence are all determined by the doc's values. I might look into building an API if my app got more complex or needed user-dependent behavior, but if I want to turn off ads for my users on days when [LOCAL SPORTS TEAM] wins, I can do that with minor effort and no expense.

The backend doc used in WhatIHadForBreafast is:
https://docs.google.com/spreadsheets/d/18JyepUBU2-QAF4agQo7BI25fe5gARfxBr5AvBHFkgpg/edit#gid=0

Thoughts
-----------

The http call made to get the CSV data can change at Google's whim, and this will break if it does. Also, sometimes it takes a couple seconds for Google Docs to respond with the data. And I've seen some existing docs return xlxs data, even when requesting csv. So use caution; this is duct tape. If you don't mind that possible fragility, here are some other possible uses:

  - Strings throughout your app that might change or need polish later, like "about the author". Probably not great if you need translations.
  - Updating values without dealing with code. Make an app that shows your middle school's last basketball game results, then give a parent access to the spreadsheet for editing.
  - Game stuff. Use values for level-building, riddle-of-the-day, monthy geocache clues.
  - Theming that reacts to real world events. The background image is a pic of the highest-grossing weekend movie.

There are more solid solutions built around this idea that involve publishing your doc to the Web as a public page, then getting the data back as JSON, though in my tests, the server response was very slow and the payload was padded with extra data.

Thanks to Jake Wharton's [Picasso][4] for making downloading images easy.

[1]:https://github.com/briandherbert/WhatIHadForBreakfast/blob/master/src/com/example/whatihadforbreakfast/DuctTapeBackend.java 
[2]:https://raw.githubusercontent.com/briandherbert/WhatIHadForBreakfast/photos/photos/fattySides.png
[3]:https://raw.githubusercontent.com/briandherbert/WhatIHadForBreakfast/photos/photos/charmsSides.png
[4]:https://github.com/square/picasso
