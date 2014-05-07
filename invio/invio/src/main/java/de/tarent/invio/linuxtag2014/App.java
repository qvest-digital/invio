package de.tarent.invio.linuxtag2014;

import android.app.Application;
import android.content.Context;

/**
 * This class allows the context of the Application to be accessed from anywhere. The application this class is
 * referenced to is defined in the AndroidManifest.xml as the application's name. Using this class there is no
 * longer a need to give an activity or context as a parameter to other classes which is necessary for simple
 * objects with no functionality.
 *
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class App extends Application {

    /**
     * The {@link Context} of the application defined in the AndroidManifest.xml.
     */
    private static Context mContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
