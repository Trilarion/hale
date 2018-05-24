package net.sf.hale;

import org.lwjgl.Sys;

public class ShowInfo {

    public static void main(String[] args) {
        System.out.println(String.format("LWJGL %s", Sys.getVersion()));
    }

}
