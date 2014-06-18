package fm.radiant.android.lib;

import android.util.SparseIntArray;

public class ExtendedSparseIntArray extends SparseIntArray {
    public void inc(int key, int value) {
        super.put(key, super.get(key) + value);
    }

    public void inc(int key) {
        super.put(key, super.get(key) + 1);
    }

    public void dec(int key, int value) {
        super.put(key, super.get(key) - value);
    }

    public void dec(int key) {
        super.put(key, super.get(key) - 1);
    }
}

