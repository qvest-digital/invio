package de.tarent.nic.android.admin;

import android.widget.ZoomButtonsController;
import android.widget.ZoomControls;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowZoomButtonsController;

/**
 * This CustomShadowClass exists because Robolectric itself does not implement the ZoomButtonsController methods we
 * need for our tests.
 */
@Implements(ZoomButtonsController.class)
public class CustomShadowZoomButtonsController extends ShadowZoomButtonsController{

    private ZoomControls zoomControls = Robolectric.newInstanceOf(ZoomControls.class);

    @Implementation
    public  void setZoomInEnabled(boolean enabled) {

    }

    @Implementation
    public  void setZoomOutEnabled(boolean enabled) {

    }
}
