# gcm.js

**gcm.js** is an open source module for Titanium Android SDK that lets developers receive GCM push notifications in their Android apps.
It has a very simple API -almost identical to iOS!- yet flexible and powerful, as it executes Javascript whenever a notification is received, no matter if the app is in foreground or background. More info at:
[http://iamyellow.net/post/40100981563/gcm-appcelerator-titanium-module](http://iamyellow.net/post/40100981563/gcm-appcelerator-titanium-module)

## Forking the module

If you want to fork and compile this module, first rename manifest_template file to manifest.
Then fill the guid (line #16) with a new module uuid with the **uuidgen** command as [this post](http://developer.appcelerator.com/blog/2011/09/module-verification.html) explains.

## Author

jordi domenech, [iamyellow.net](http://iamyellow.net)

## License

Apache License, Version 2.0