package com.example.connectcircle

import android.app.Application

class MyApp : Application() {
//
//    private var appInBackground = false
//    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
//

    override fun onCreate() {
        super.onCreate()
//        registerActivityLifecycleCallbacks(AppLifecycleCallbacks())
    }

//    inner class AppLifecycleCallbacks : ActivityLifecycleCallbacks {
//
//        private val handler = Handler(Looper.getMainLooper())
//
//        override fun onActivityResumed(activity: Activity) {
//            appInBackground = false
//            mAuth.currentUser?.let { PresenceManager.updatePresence(it.uid, true) }
//
//        }
//
//        override fun onActivityPaused(activity: Activity) {
//            handler.postDelayed({
//                if (!activity.isChangingConfigurations && !appInBackground) {
//                    appInBackground = true
//                    // App is in background, update presence
//                    mAuth.currentUser?.let { PresenceManager.updatePresence(it.uid, false) }
//                }
//            }, 500) // Adjust delay as needed
//        }
//
//        override fun onActivityStopped(activity: Activity) {}
//        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
//
//        override fun onActivityStarted(activity: Activity) {}
//
//        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
//
//        override fun onActivityDestroyed(activity: Activity) {}
//
//    }


}