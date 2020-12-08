package com.gingernet.utils;


import java.util.HashMap;
import java.util.Map;

public final class CmdLineArgs {
    static final String PREFIX_FLAG = "--";
    static final String PREFIX_OPT = "-";
    final Map<String, String> map;
    final CmdLineArgs.Spec spec;

    private CmdLineArgs(CmdLineArgs.Spec spec, Map<String, String> map) {
        assert map != null : "map is null";

        this.spec = spec;
        this.map = map;
    }

    public boolean checkFlag(String f) {
        return this.map.containsKey(flagKey(f));
    }

    public String getOption(String k, String defval) {
        String v = (String)this.map.get(optKey(k));
        if (v == null) {
            v = defval;
        }

        return v;
    }

    public String getOptionStrict(String k) {
        return (String)assertNotNull(this.map.get(optKey(k)), "must specify required option " + k);
    }

    public int getIntOption(String k, int defval) {
        int res = defval;
        String optv = this.getOption(k, (String)null);
        if (optv != null) {
            res = decodeInt(optv, "invalid value for numeric option " + k);
        }

        return res;
    }

    public String getparam(int n) {
        return (String)this.map.get(paramKey(n));
    }

    private static String paramKey(int n) {
        return String.format("$%d", n);
    }

    private static String flagKey(String f) {
        return String.format("%s%s", "--", f);
    }

    private static String optKey(String o) {
        return String.format("%s%s", "-", o);
    }

    public static CmdLineArgs parse(CmdLineArgs.Spec spec, String... args) {
        if (args == null) {
            return null;
        } else {
            Map<String, String> map = new HashMap();
            int fn = 0;
            int on = 0;
            int pn = 0;

            for(int i = 0; i < args.length; ++i) {
                String k = null;
                String v = null;
                if (args[i] != null) {
                    if (args[i].startsWith("--")) {
                        k = args[i];
                        v = k;
                        ++fn;
                    } else if (args[i].startsWith("-")) {
                        assert0(args.length - 1 > i, String.format("no value for arg %s at args end", args[i]));
                        assert0(!args[i + 1].startsWith("-"), String.format("no value for arg %s", args[i]));
                        assert0(!args[i + 1].startsWith("--"), String.format("no value for arg %s", args[i]));
                        k = args[i];
                        ++i;
                        v = args[i];
                        ++on;
                    } else {
                        ++pn;
                        k = paramKey(pn);
                        v = args[i];
                    }

                    map.put(k, v);
                }
            }

            return new CmdLineArgs(spec, map);
        }
    }

    public static void assert0(boolean prop, String errmsg) throws IllegalStateException {
        if (!prop) {
            throw new IllegalStateException(errmsg);
        }
    }

    public static <T> T assertNotNull(T o, String errmsg) throws IllegalStateException {
        if (o == null) {
            throw new IllegalStateException(errmsg);
        } else {
            return o;
        }
    }

    public static int decodeInt(String spec, String errmsg) throws IllegalArgumentException {
        try {
            int v = Integer.parseInt(spec);
            return v;
        } catch (NumberFormatException var4) {
            throw new IllegalArgumentException(String.format(errmsg, spec));
        }
    }

    public interface Spec {
    }
}
