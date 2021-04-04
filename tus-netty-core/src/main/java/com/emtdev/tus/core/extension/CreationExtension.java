package com.emtdev.tus.core.extension;

import com.emtdev.tus.core.domain.OperationResult;
import io.netty.buffer.ByteBuf;

public interface CreationExtension  extends Extension{

    OperationResult createFile(String fileId);

    OperationResult write(String fileId, ByteBuf byteBuf, boolean finalBytes);

    boolean exist(String fileId);

    long offset(String fileId);

}
