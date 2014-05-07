package de.tarent.nic.android.admin.test;

import android.test.suitebuilder.annotation.SmallTest;
import de.tarent.nic.android.admin.MapActivity;

/**
 * Das ist jetzt der eigentliche Test! Er verwendet
 * weitestgehend die Methoden des {@link AbstractTest}
 * <p/>
 * Ziel ist es den Test-Code (in dieser Klasse) mit
 * möglichst wenig "technischen Details" zu füllen.
 * (das würde es vermutlich schwer lesbar machen).
 * <p/>
 * Die meisten {@link org.junit.Assert}s werden in den {@link AbstractTest}
 * Methoden durchgeführt.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class NicActivityTest extends AbstractTest<MapActivity> {

    public NicActivityTest() {
        super(MapActivity.class);
    }

    @SmallTest
    // TODO: we should have proper tests, but this one is outdated because NicActivity is no longer the first activity.
    public void test_initialElements() {
        //in meinem vorgänger Projekt habe ich gelernt, dass man keine Element-Inhalte
        //1:1 abtesten sollte. Denn das ändert sich zu häufig.
        //Dennoch habe ich zur demonstration einmal die Inhalte abgetestet. Denn viel
        //ist bei einer "Hello World"-App nicht zu testen ;)

        verifyActivityTitle("nic-admin");
        //verifyElementIsShown(MAP_VIEW);
    }
}

