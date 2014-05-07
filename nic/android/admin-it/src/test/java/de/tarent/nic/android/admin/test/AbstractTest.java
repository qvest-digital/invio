package de.tarent.nic.android.admin.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import static android.test.ViewAsserts.assertOnScreen;

/**
 * Diese abstrakte Test-Klasse ist ein "Container" für
 * "technische" Methoden. Das heißt, dass diese Klasse
 * Methoden bereitstellt, die so wenig wie Möglich von
 * "Android-Wissen" vorraussetzt.
 * <p/>
 * In den Klassen, die diese Klasse erben, sollten diese
 * Methoden verwendet werden! Damit in der abgeleiteten
 * Klasse relativ gut ersichtlich ist, was getestet wird.
 *
 * @param <T> Activity, die getestet werden soll.
 * @author: Sven Schumann, <s.schumann@tarent.de>
 */
public abstract class AbstractTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

    protected Activity activity;

    public AbstractTest(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    /**
     * Überprüft den Titel der Anwendung.
     *
     * @param expectedTitle Erwarteter Titel.
     */
    protected void verifyActivityTitle(String expectedTitle) {
        assertEquals("Der Titel der Anwendung ist nicht richtig!",
                expectedTitle, activity.getTitle());
    }

    /**
     * Überprüft ob das angegebene Element auf dem Bildschirm sichtbar ist.
     *
     * @param element Element das überprüft werden soll.
     */
    protected void verifyElementIsShown(Element element) {
        final View targetView = findView(element);

        assertNotNull("Das Element (" + element.getName() + ") wurde nicht gefunden!", targetView);
        assertOnScreen(activity.getWindow().getDecorView(), targetView);
    }

    /**
     * Überprüft ob der Wert des Elements <b>gleich</b> des erwarteten Wertes ist.
     *
     * @param element       Element welches überprüft werden soll.
     * @param expectedValue Erwarteter Wert.
     */
    protected void verifyElementValue(Element element, Object expectedValue) {
        final View targetView = findView(element);

        if (targetView instanceof TextView) {
            verifyElementValue((TextView) targetView, element, expectedValue.toString());
        } else {
            fail("Es ist nicht bekannt, wie man aus diesem Element (" +
                    element.getName() + ") den Wert ermittelt!");
        }
    }

    /**
     * Klickt auf ein Menu-Item. Dazu wird das Menu aufgerufen und auf das
     * Item geklickt.
     *
     * @param item Auf welches Item soll geklickt werden?
     * @return True wenn dieser Klick ein Effekt hatte. False wenn er kein Effekt hatte.
     * Dies kann bswp. dann vorkommen, wenn das Item nicht existiert oder
     * dekativiert ist.
     */
    protected boolean clickMenuItem(Element item) {
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);

        return getInstrumentation().invokeMenuActionSync(activity, item.getResourceId(), 0);
    }

    private void verifyElementValue(TextView targetView, Element element, String expectedValue) {
        assertEquals("Unerwarteter Wert des Elements (" + element.getName() + ") entdeckt!",
                expectedValue, targetView.getText());
    }

    private View findView(Element element) {
        return activity.findViewById(element.getResourceId());
    }
}
