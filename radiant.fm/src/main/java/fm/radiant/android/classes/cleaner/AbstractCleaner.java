package fm.radiant.android.classes.cleaner;

import android.util.Log;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fm.radiant.android.interfaces.Audioable;

public abstract class AbstractCleaner {
    private Collection<? extends Audioable> queue;

    public AbstractCleaner(Collection<? extends Audioable> queue) {
        this.queue = queue;
    }

    public void clean() {
        for (File unnecessaryFile : getUnnecessaryFiles()) { FileUtils.deleteQuietly(unnecessaryFile); }
        for (File corruptedFile   : getCorruptedFiles())   { FileUtils.deleteQuietly(corruptedFile); }
    }

    protected abstract File getDirectory();

    protected Collection<File> getUnnecessaryFiles() {
        List<String> filenames = Lists.newArrayList(Iterables.transform(queue, new Function<Audioable, String>() {
            @Override
            public String apply(Audioable model) {
                return model.getFilename();
            }
        }));

        return FileUtils.listFiles(getDirectory(), new NotFileFilter(new NameFileFilter(filenames)), null);
    }

    protected Collection<File> getCorruptedFiles() {
        return Collections.emptyList();
    }
}