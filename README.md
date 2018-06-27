# appscope
An Android library implementing Service Locator pattern

Examples:
```
AppScope.init(context, new Gson()); // Initialize one-tine in your Application.onCreate() method
private static final TypedKey<String> COUNTRY_CODE = new TypedKey("countryCode", String.class, false, true);
AppScope.bind(COUNTRY_CODE, "US"); // bind the key COUNTRY_CODE to the value "US"

.... // Anywhere in app
String country = AppScope.get(COUNTRY_CODE)  ==> Returns "US"
```

In the example above, since the last constructor parameter (persist) of TypedKey COUNTRY_CODE is true, the COUNTRY_CODE is persisted (in prefs) and usable across sessions.

The type of the key can be any arbitrary type that the Gson instance can serialize/deserialize.

# Use with Gradle
add to your repositories

```
repositories {
    maven { url "https://jitpack.io" }
}
```

In your app build.gradle, add:  `compile "com.github.PeelTechnologies:appscope:1.7.5"`

If you use Amplitude, also checkout [appscope-amplitude-extension](https://github.com/PeelTechnologies/appscope-amplitude-extension) project to automatically sync AppScope properties with Amplitude.

# User Guide
TypedKey can take arbitrarily complex Java object that Gson can serialize/deserialize. For example, `TypedKey<Customer>` may represent a class with nested fields for `Address`, name, phone numbers, etc.

While defining TypedKeys, set `config` constructor parameter to `true` if you want the key to not be cleared when AppScope.reset() is called.

For Junit tests, use `AppScope.TestAccess.init()` method in `setUp()`. This is to ensure that any values set by other tests will get cleared

Any key that is marked as persistent, is stored in an LRU cache and discarded (after persisting) to conserve memory.
