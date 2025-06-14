package com.logicweaver.nmspell.client;

public class ClientSoulData {
    private static double playerSoul_Fragments;
    private static double playerMax_Soul_Fragments;

    private static int playerRank;
    private static int playerSoul_Cores;

    private static int playerSEK;

    public static void setPlayerSoul_Fragments(double playerSoul_Fragments) {
        ClientSoulData.playerSoul_Fragments = playerSoul_Fragments;
    }

    public static double getPlayerSoul_Fragments() {
        return playerSoul_Fragments;
    }

    public static void setPlayerMax_Soul_Fragments(double playerMax_Soul_Fragments) {
        ClientSoulData.playerMax_Soul_Fragments = playerMax_Soul_Fragments;
    }

    public static double getPlayerMax_Soul_Fragments() {
        return playerMax_Soul_Fragments;
    }

    public static void setPlayerRank(int playerRank) {
        ClientSoulData.playerRank = playerRank;
    }

    public static int getPlayerRank() {
        return playerRank;
    }

    public static void setPlayerSoul_Cores(int playerSoul_Cores) {
        ClientSoulData.playerSoul_Cores = playerSoul_Cores;
    }

    public static int getPlayerSoul_Cores() {
        return playerSoul_Cores;
    }

    public static void setPlayerSEK(int playerSEK) {
        ClientSoulData.playerSEK = playerSEK;
    }

    public static int getPlayerSEK() {
        return playerSEK;
    }
}
