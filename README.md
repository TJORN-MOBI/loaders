# mobi.tjorn.content.loaders

### What is this?
Almost a drop-in replacement for [AsyncTask](http://developer.android.com/reference/android/os/AsyncTask.html).

### Introduction
Many online guides, including [Authorizing with Google for REST APIs](https://developers.google.com/android/guides/http-auth#extend_asynctask_to_get_the_auth_token), use [AsyncTasks](http://developer.android.com/reference/android/os/AsyncTask.html) 
to perform background operations.
Unfortunately, [AsyncTasks](http://developer.android.com/reference/android/os/AsyncTask.html) are not
integrated with [Activity Lifecycle](http://developer.android.com/guide/components/activities.html#Lifecycle).  
and developers have to write error-prone code to handle Activity pausing, stopping, destruction, configuration changes, etc.

[Loaders](http://developer.android.com/guide/components/loaders.html) are integrated with 
[Activity Lifecycle](http://developer.android.com/guide/components/activities.html#Lifecycle), 
but they are not as widely popular as [AsyncTasks](http://developer.android.com/reference/android/os/AsyncTask.html),
except [CursorLoader](http://developer.android.com/reference/android/content/CursorLoader.html).
[Loaders](http://developer.android.com/guide/components/loaders.html) are not as easy to create and use as [AsyncTasks](http://developer.android.com/reference/android/os/AsyncTask.html).
This library attempts to make [Loaders](http://developer.android.com/guide/components/loaders.html) easy to use.

### Using in Android Studio / Gradle

```
buildscript {
    repositories {
        jcenter()
    }
}
```

```
dependencies {
    compile 'mobi.tjorn.content:loaders:1.0.0'
}
```

### Migrating from [AsyncTasks](http://developer.android.com/reference/android/os/AsyncTask.html)
1. Subclass one of the Loaders in this library and, optionally, one of the Resutls.
2. In your existing code, move code
  - from [AsyncTask.onPreExecute](http://developer.android.com/reference/android/os/AsyncTask.html#onPreExecute()) to 
    [LoaderCallbacks.onCreateLoader](http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onCreateLoader(int, android.os.Bundle))
  - from [AsyncTask.doInBackground](http://developer.android.com/reference/android/os/AsyncTask.html#doInBackground(Params...)) to
    [AsyncTaskLoader.loadInBackground](http://developer.android.com/reference/android/content/AsyncTaskLoader.html#loadInBackground())
  - from [AsyncTask.onPostExecute](http://developer.android.com/reference/android/os/AsyncTask.html#onPostExecute(Result)) to
    [LoaderCallbacks.onLoadFinished](http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onLoadFinished(android.content.Loader<D>, D))
3. Remove code that deals with [AsyncTasks](http://developer.android.com/reference/android/os/AsyncTask.html) integration from your
[Activities](http://developer.android.com/reference/android/app/Activity.html) or [Fragments](http://developer.android.com/reference/android/app/Fragment.html).
[Loaders](http://developer.android.com/guide/components/loaders.html) are already integrated with [Activity Lifecycle](http://developer.android.com/guide/components/activities.html#Lifecycle).

### Example
We convert [Getting Auth Token](https://developers.google.com/android/guides/http-auth#extend_asynctask_to_get_the_auth_token) sample.  First, we create our TokenLoader:
```
private static class TokenLoader extends SimpleResultLoader<String> {
    private final String scope;
    private final String email;


    protected TokenLoader(Context context, String email, String scope) {
        super(context);
        this.scope = scope;
        this.email = email;
    }

    @Override
    public SimpleResult loadInBackground() {
        // Note that we simply return a result or an error here
        try {
            return new SimpleResult(GoogleAuthUtil.getToken(getContext(), email, scope));
        } catch (Exception e) {
            return new SimpleResult(e);
        }
    }
}
```
Note that we don't deal with errors on a background thread: we simply return a result or an error.
Then we implement [LoaderCallbacks](http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html):
```
@Override
public Loader<SimpleResult<String>> onCreateLoader(int id, Bundle args) {
    switch (id) {
        case LOADER_TOKEN:
            return new TokenLoader(this, account.getEmail(), "oauth2:" + SCOPE_BOTH);
    }
    return null;
}

@Override
public void onLoadFinished(Loader<SimpleResult<String>> loader, SimpleResult<String> data) {
    switch (loader.getId()) {
        case LOADER_TOKEN:
            if (data.hasError()) {
                Log.e(TAG, data.getError().getMessage());
                // We deal with error on UI thread
            } else {
                Log.d(TAG, data.getData());
            }
            break;
    }
}

@Override
public void onLoaderReset(Loader<SimpleResult<String>> loader) {
    switch (loader.getId()) {
        case LOADER_TOKEN:
            Log.d(TAG, "Token reset");
            break;
    }
}
```
Now we can call our error handler on UI thread to deal with
[UserRecoverableAuthException](https://developers.google.com/android/reference/com/google/android/gms/auth/UserRecoverableAuthException) 
or [GoogleAuthException](https://developers.google.com/android/reference/com/google/android/gms/auth/GoogleAuthException).
