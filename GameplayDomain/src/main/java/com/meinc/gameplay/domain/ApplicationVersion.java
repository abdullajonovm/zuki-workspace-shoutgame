package com.meinc.gameplay.domain;

public class ApplicationVersion {

    public int major;
    public int minor;
    public int patch;
    public ApplicationVersion(String version) {
        if (version == null || version.trim().length() < 3) version = "1.0";
        String[] parts = version.trim().split("\\.");
        if (parts.length < 2) {
            major = 1;
            minor = 0;
        }
        else {
            try {
                major = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) { major = 1; }
            try {
                minor = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) { minor = 1; }
            if (parts.length > 2) {
                try {
                    patch = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) { patch = 0; }
            }
        }
    }
    
}
