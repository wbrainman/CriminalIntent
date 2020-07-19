package android.bignerdranch.com;

import android.util.Log;

import androidx.fragment.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity {

    private final static String TAG = "cc";

    @Override
    protected Fragment createFragment() {
        Log.d(TAG, "createFragment: CrimeListActivity");
        return new CrimeListFragment();
    }
}
