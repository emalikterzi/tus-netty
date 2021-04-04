package com.emtdev.tus.core.extension;

import com.emtdev.tus.core.domain.OperationResult;

public interface ConcatenationExtension extends Extension {

    OperationResult merge(String fileId, String[] fileIds);

}
