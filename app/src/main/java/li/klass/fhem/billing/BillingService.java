/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.billing;

import android.app.Activity;
import android.util.Log;

import com.android.vending.billing.IabException;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import li.klass.fhem.AndFHEMApplication;

import static com.google.common.base.Preconditions.checkNotNull;

public class BillingService {

    public interface ProductPurchasedListener {
        void onProductPurchased(String orderId, String productId);
    }

    public interface SetupFinishedListener {
        void onSetupFinished();
    }

    public static final String TAG = BillingService.class.getName();

    public static final BillingService INSTANCE = new BillingService();

    private IabHelper iabHelper;
    private AtomicReference<Inventory> inventory = new AtomicReference<Inventory>(Inventory.empty());

    private BillingService() {
    }

    public synchronized void start(final SetupFinishedListener listener) {
        checkNotNull(listener);

        try {
            Log.d(TAG, "Starting setup");
            iabHelper = new IabHelper(AndFHEMApplication.getContext(), AndFHEMApplication.PUBLIC_KEY_ENCODED);
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess()) {
                        Log.d(TAG, "=> SUCCESS");
                        loadInventory();
                    } else {
                        Log.e(TAG, "=> ERROR " + result.toString());
                    }
                    listener.onSetupFinished();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to start billing", e);
        }
    }

    public synchronized void stop() {
        iabHelper.dispose();
    }

    public synchronized void requestPurchase(Activity activity, String itemId, String payload, final ProductPurchasedListener listener) {
        Log.i(TAG, "requesting purchase of " + itemId);
        iabHelper.launchPurchaseFlow(activity, itemId, 0, new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                if (result.isSuccess()) {
                    Log.i(TAG, "purchase result: SUCCESS");
                    loadInventory();
                    listener.onProductPurchased(info.getOrderId(), info.getSku());
                } else {
                    Log.e(TAG, "purchase result: " + result.toString());
                }
            }
        }, payload);
    }

    public synchronized void loadInventory() {
        try {
            Log.i(TAG, "loading inventory");
            inventory.set(iabHelper.queryInventory(false, null, null));
        } catch (IabException e) {
            Log.e(TAG, "cannot load inventory", e);
        }
    }

    public synchronized Set<String> getOwnedItems() {
        Set<String> ownedItems = inventory.get().getAllOwnedSkus();

        Log.i(TAG, "owned items: " + ownedItems);

        return ownedItems;
    }
}