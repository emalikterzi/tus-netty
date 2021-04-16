package com.emtdev.tus.core.extension;

import com.emtdev.tus.core.domain.OperationResult;
import io.netty.buffer.ByteBuf;

import java.io.InputStream;

public interface CreationExtension extends Extension {

    OperationResult createFile(String fileId);

    OperationResult write(String fileId, InputStream inputStream);

    boolean exist(String fileId);

    long offset(String fileId);

    OperationResult finalizeFile(String fileId);

}
