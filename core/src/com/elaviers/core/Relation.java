package com.elaviers.core;

public class Relation
{
    public enum GotoMode
    {
        NEVER(2),
        ALWAYS(1),
        NEUTRAL(0);

        public final int id;

        GotoMode(int id)
        {
            this.id = id;
        }

        public static GotoMode fromID(int id)
        {
            return (id == NEVER.id) ? NEVER : ((id == ALWAYS.id) ? ALWAYS : NEUTRAL);
        }
    }

    public GotoMode gotoMode;

    public Relation(GotoMode gotoMode)
    {
        this.gotoMode = gotoMode;
    }
}