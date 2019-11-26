package ru.curoviyxru.phoenix.kernel;

import ru.curoviyxru.j2vk.ProgressProvider;

/**
 *
 * @author curoviyxru
 */
public class FocusedProgressProvider implements ProgressProvider {

    private String name;
    private ProgressProvider pp;
    private long progress;

    public FocusedProgressProvider(ProgressProvider pp) {
        if (pp == null) {
            return;
        }
        name = pp.getName();
        setProvider(pp);
    }

    public void setProvider(ProgressProvider pp) {
        this.pp = pp;
        if (pp == null) {
            return;
        }
        if (progress != 0) {
            setProgress(progress);
        }
    }

    public void setProgress(long i) {
        progress = i;
        if (pp != null) {
            pp.setProgress(i);
        }
    }

    public void failed(String s) {
        if (pp != null) {
            pp.failed(s);
        }
        ProgressKernel.deleteProvider(this);
    }

    public void successful() {
        if (pp != null) {
            pp.successful();
        }
        ProgressKernel.deleteProvider(this);
    }

    public String getName() {
        return name;
    }
}
