package com.gingernet.chia.utils;

import com.gingernet.chia.proto.BIP39;

public class SeedUtil {
    public String seed;
    public String[] seedarray;
    public boolean populate(String seed){
        this.seed = seed;
        this.seedarray = seed.split(" ");
        for(String lseed : this.seedarray){
            if(!wordExists(lseed)) return false;
        }
        return BIP39.verifySeed(this.seed);
    }

    public boolean selfpopulate(){
        String lseed = BIP39.generateSeed();
        if(lseed == null) return false;
        return this.populate(lseed);
    }

    public boolean verifyWord(String word, int position){
        return seedarray[position].equalsIgnoreCase(word);
    }

    public static boolean wordExists(String word){
        return BIP39.wordlist.contains(word);
    }
}

