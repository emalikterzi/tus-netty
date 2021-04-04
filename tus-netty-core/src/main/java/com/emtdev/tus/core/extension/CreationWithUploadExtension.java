package com.emtdev.tus.core.extension;

import com.emtdev.tus.core.domain.OperationResult;
import io.netty.buffer.ByteBuf;

public interface CreationWithUploadExtension extends Extension {

    OperationResult createAndWrite(String fileId, ByteBuf byteBuf, boolean finalBytes);

}
