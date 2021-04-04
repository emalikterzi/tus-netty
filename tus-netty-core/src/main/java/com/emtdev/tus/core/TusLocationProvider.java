package com.emtdev.tus.core;

public interface TusLocationProvider {

    String generateLocationHeader(String fileId);

}
