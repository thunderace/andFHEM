/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
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
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.activities.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

import java.io.Serializable;

public class TopLevelFragment extends Fragment implements Serializable {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.top_level_view, null);
    }

    public void switchTo(FragmentType fragmentType, Bundle data) {

        ContentHolderFragment content = new ContentHolderFragment(fragmentType, data);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.topLevelContent, content)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public boolean back(Bundle data) {
        getFragmentManager().popBackStackImmediate();

        ContentHolderFragment contentFragment = getCurrentContent();
        if (contentFragment != null) {
            contentFragment.onBackPressResult(data);
            return true;
        }
        return false;
    }

    public ContentHolderFragment getCurrentContent() {
        return (ContentHolderFragment) getFragmentManager().findFragmentById(R.id.topLevelContent);
    }
}
