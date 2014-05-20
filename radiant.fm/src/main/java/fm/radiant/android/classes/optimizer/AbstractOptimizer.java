package fm.radiant.android.classes.optimizer;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.List;

import fm.radiant.android.interfaces.Audioable;

public abstract class AbstractOptimizer {
    private Collection<? extends Audioable> queue;

    public AbstractOptimizer(Collection<? extends Audioable> queue) {
        this.queue = queue;
    }

    public void optimize() {
        for (File expiredFile : getExpiredFiles()) { FileUtils.deleteQuietly(expiredFile); }
    }

    protected abstract File getDirectory();

    protected Collection<File> getExpiredFiles() {
        List<String> filenames = Lists.newArrayList(Iterables.transform(queue, new Function<Audioable, String>() {
            @Override
            public String apply(Audioable model) {
                return model.getFilename();
            }
        }));

        return FileUtils.listFiles(getDirectory(), new NameFileFilter(filenames), null);
    }
}