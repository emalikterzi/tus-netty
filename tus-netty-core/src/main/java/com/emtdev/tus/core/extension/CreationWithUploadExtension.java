package com.emtdev.tus.core.extension;

import com.emtdev.tus.core.domain.OperationResult;
import io.netty.buffer.ByteBuf;

import java.io.InputStream;

public interface CreationWithUploadExtension extends Extension {

    OperationResult createAndWrite(String fileId, InputStream inputStream);

}
