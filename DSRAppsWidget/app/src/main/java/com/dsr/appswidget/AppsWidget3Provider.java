package com.dsr.appswidget;

/** DSR Apps 3: CommBank, Binance, CommSec, Macquarie, Pocket, Messenger, Greater Bank. */
public class AppsWidget3Provider extends BaseAppsWidget {

    @Override
    protected int layout() {
        return R.layout.apps_widget_3;
    }

    @Override
    protected int[] tiles() {
        return new int[] {
                R.id.tile_commbank, R.id.tile_binance, R.id.tile_commsec,
                R.id.tile_macquarie, R.id.tile_pocket, R.id.tile_messenger, R.id.tile_greater,
        };
    }

    @Override
    protected String[][] packages() {
        return new String[][] {
                {"com.commbank.netbank"},
                {"com.binance.dev"},
                {"au.com.commsec.android.CommSec"},
                {"au.com.macquarie.banking"},
                {"au.com.commsec.android.CommSecPocket"},
                {"com.facebook.orca"},
                {"com.greater.Greater"},
        };
    }
}
