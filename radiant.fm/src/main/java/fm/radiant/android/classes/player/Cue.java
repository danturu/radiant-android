package fm.radiant.android.classes.player;


public class Cue {
    public enum Type { RELATIVE, ABSOLUTE, RELATIVE_ONCE, ABSOLUTE_ONCE }

    private int time;
    private Type type;
    private Runnable callback;
    private boolean isExecuted;

    public Cue(int time, Type type, final Runnable callback) {
        this.time     = time;
        this.type     = type;
        this.callback = new Runnable() {
            @Override
            public void run()  {
                if ((Cue.this.type == Type.RELATIVE_ONCE || Cue.this.type == Type.ABSOLUTE_ONCE) && isExecuted) {
                    return;
                }

                isExecuted = true;
                callback.run();
            }
        };
    }

    public Runnable getCallback() {
        return callback;
    }

    public int getTime() {
        return time;
    }

    public Type getType() {
        return type;
    }

    public int getDelay(int position, int duration) {
        switch (type) {
            case ABSOLUTE: case ABSOLUTE_ONCE:
                return position - time + (time < 0 ? duration : 0);

            case RELATIVE: case RELATIVE_ONCE:
                return time;
        }

        return 0;
    }
}
