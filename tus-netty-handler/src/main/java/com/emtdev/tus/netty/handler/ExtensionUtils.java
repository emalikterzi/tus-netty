package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.extension.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExtensionUtils {

    private final static Class<CreationExtension> CREATION_CLASS = CreationExtension.class;
    private final static Class<CreationWithUploadExtension> CREATION_WITH_UPLOAD_CLASS = CreationWithUploadExtension.class;
    private final static Class<CreationDeferLengthExtension> CREATION_DEFER_LENGTH_CLASS = CreationDeferLengthExtension.class;
    private final static Class<TerminationExtension> TERMINATION_EXTENSION_CLASS = TerminationExtension.class;
    private final static Class<ConcatenationExtension> CONCATENATION_EXTENSION_CLASS = ConcatenationExtension.class;
    private final static Class<ExpirationExtension> EXPIRATION_EXTENSION_CLASS = ExpirationExtension.class;

    public static boolean supports(TusStore store, Extension extension) {
        Class<?>[] classes = extension.extensionClass;

        for (Class<?> extensionClass : classes) {
            if (!extensionClass.isAssignableFrom(store.getClass())) {
                return false;
            }
        }

        return true;
    }

    public static String extensionHeaderValue(TusStore store) {
        StringBuilder sb = new StringBuilder();
        List<String> extensionValues = new ArrayList<String>();

        Extension[] values = Extension.values();

        for (Extension each : values) {
            if (supports(store, each)) {
                extensionValues.add(each.extensionValue);
            }
        }

        if (extensionValues.isEmpty()) {
            return "";
        }

        Iterator<String> iterator = extensionValues.iterator();

        while (iterator.hasNext()) {
            String value = iterator.next();
            sb.append(value);
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    public enum Extension {

        CREATION("creation", CREATION_CLASS),
        CREATION_WITH_UPLOAD("creation-with-upload", CREATION_WITH_UPLOAD_CLASS),
        CREATION_DEFER_LENGTH("creation-defer-length", CREATION_DEFER_LENGTH_CLASS),
        TERMINATION("termination", TERMINATION_EXTENSION_CLASS),
        CONCATENATION("concatenation", CONCATENATION_EXTENSION_CLASS, CREATION_DEFER_LENGTH_CLASS),
        EXPIRATION("expiration", EXPIRATION_EXTENSION_CLASS);

        final Class<?>[] extensionClass;
        final String extensionValue;

        Extension(String extensionValue, Class<?>... extensionClass) {
            this.extensionClass = extensionClass;
            this.extensionValue = extensionValue;
        }
    }
}
