package com.gingernet.utils;

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import com.gingernet.utils.CmdLineArgs;
import com.gingernet.utils.CmdLineArgs.Spec;
import com.gingernet.utils.Blake2b.Digest;
import com.gingernet.utils.Blake2b.Param;

public class Bench implements Runnable {
    static volatile boolean f_run = true;
    private final String md_name;
    private final int iters;
    private final int datalen;
    private final byte[] b;
    private final Bench.Call call;

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.exit(Bench.Usage.usage());
        }

        CmdLineArgs clargs = CmdLineArgs.parse((Spec)null, args);

        try {
            String md_name = null;
            md_name = clargs.getOption("d", "blake2b");
            int iters = clargs.getIntOption("i", 1000);
            int datalen = clargs.getIntOption("n", 1024);
            Bench bench = new Bench(md_name, iters, datalen);
            Thread brth = new Thread(bench, "bench-runner");
            brth.start();
            System.in.read();
            f_run = false;
            brth.join();
        } catch (Throwable var7) {
            System.exit(Bench.Usage.usage());
        }

    }

    Bench(String md_name, int iters, int datalen) {
        this.md_name = md_name;
        this.iters = iters;
        this.datalen = datalen;
        this.b = new byte[datalen];

        for(int i = 0; i < this.b.length; ++i) {
            this.b[i] = (byte)i;
        }

        this.call = this.getBenchedCall();
    }

    private Bench.Call getBenchedCall() {
        Bench.Call call = null;
        if (this.md_name.equalsIgnoreCase("blake2b")) {
            call = newCallBlake2b();
        } else {
            call = newCallJCEAlgorithm(this.md_name);
        }

        return call;
    }

    private static final void puts(String s) {
        System.out.format("%s\n", s);
    }

    public void run() {
        puts("Bench - hit any key to stop.");
        puts("");
        puts("digest   | iterations | size (b/iter) | dt (nsec/iter) | throughput (b/usec)");

        while(f_run) {
            long start = System.nanoTime();

            for(int i = 0; i < this.iters; ++i) {
                this.call.func(this.b);
            }

            long delta = System.nanoTime() - start;
            long delta_us = TimeUnit.NANOSECONDS.toMicros(delta);
            double thrpt = (double)this.b.length * (double)this.iters / (double)delta_us;
            System.out.format("%-8s | %10d | %13d | %14d |    %16.6f\r", this.md_name, this.iters, this.b.length, delta, thrpt);
        }

    }

    public static Bench.Call newCallBlake2b() {
        final Blake2b digest = Digest.newInstance((new Param()).setDigestLength(20));
        return new Bench.Call() {
            public final byte[] func(byte[] b) {
                digest.update(b, 0, b.length);
                return digest.digest();
            }
        };
    }

    public static Bench.Call newCallJCEAlgorithm(String md_name) {
        final MessageDigest digest = silentGet(md_name);
        return new Bench.Call() {
            public final byte[] func(byte[] b) {
                digest.update(b, 0, b.length);
                return digest.digest();
            }
        };
    }

    public static MessageDigest silentGet(String mdname) {
        try {
            return MessageDigest.getInstance(mdname);
        } catch (Throwable var2) {
            throw new Error(String.format("Error getting instance of digest <%s>", mdname), var2);
        }
    }

    interface Call {
        byte[] func(byte[] var1);
    }

    static class Usage {
        Usage() {
        }

        private static void explain(String opt, String details) {
            System.out.format("%3s\t%s\n", opt, details);
        }

        static int usage() {
            System.out.println("usage: java -cp .. ove.crypto.digest.Bench [options]");
            System.out.println("[options]");
            explain("-d", "digest to bench, one of {blake2, sha1, md5}. default: blake2b");
            explain("-i", "number of iterations (digest function calls) per bench round. default: 1000");
            explain("-n", "size of the digested buffer in bytes. default: 1024 b / call");
            return -1;
        }
    }

    interface Default {
        int iterations = 1000;
        int datalen = 1024;
        String digest = "blake2b";
    }
}

