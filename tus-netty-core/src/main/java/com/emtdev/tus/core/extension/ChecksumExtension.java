package com.emtdev.tus.core.extension;

public interface ChecksumExtension extends Extension {

    String[] checksumStrategies();

    String checksum(String alg, String fileId);

}
