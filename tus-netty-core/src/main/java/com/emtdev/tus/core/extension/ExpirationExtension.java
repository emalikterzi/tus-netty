package com.emtdev.tus.core.extension;

import java.util.Date;

public interface ExpirationExtension extends Extension {

    /**
     * @return could return null value
     */
    Date expires(String fileId);

}
