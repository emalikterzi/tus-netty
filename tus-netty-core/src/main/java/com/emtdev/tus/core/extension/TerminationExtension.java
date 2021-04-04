package com.emtdev.tus.core.extension;

import com.emtdev.tus.core.domain.OperationResult;

public interface TerminationExtension extends Extension {

    OperationResult delete(String fileId);

}
