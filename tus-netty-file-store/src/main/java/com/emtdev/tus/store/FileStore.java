package com.emtdev.tus.store;

import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.Operation;
import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.extension.ConcatenationExtension;
import com.emtdev.tus.core.extension.CreationDeferLengthExtension;
import com.emtdev.tus.core.extension.CreationExtension;
import com.emtdev.tus.core.extension.CreationWithUploadExtension;
import com.emtdev.tus.core.extension.TerminationExtension;
import io.netty.buffer.ByteBuf;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileStore implements TusStore, CreationDeferLengthExtension, ConcatenationExtension, CreationWithUploadExtension, TerminationExtension {

    private final String baseDirectory;
    private final TusConfigStore configStore;
    private final Map<String, OutputStream> outputStreamMap = new ConcurrentHashMap<>();
    private final static int BUFFER = 4096;

    public FileStore(String baseDirectory, TusConfigStore configStore) {
        this.baseDirectory = baseDirectory;
        this.configStore = configStore;
    }

    @Override
    public TusConfigStore configStore() {
        return this.configStore;
    }

    @Override
    public OperationResult createFile(String fileId) {
        File file = getFile(fileId);
        try {
            boolean status = file.createNewFile();
            return status ? OperationResult.SUCCESS : OperationResult.of(Operation.FAILED, "File Create Failed");
        } catch (Exception e) {
            return OperationResult.of(Operation.FAILED, "File Create Failed");
        }
    }

    @Override
    public OperationResult write(String fileId, ByteBuf content, boolean finalBytes) {
        return internalWrite(fileId, content, finalBytes);
    }

    @Override
    public boolean exist(String fileId) {
        File file = getFile(fileId);
        return file.exists();
    }

    @Override
    public long offset(String fileId) {
        File file = getFile(fileId);
        return file.length();
    }

    @Override
    public OperationResult delete(String fileId) {
        File file = getFile(fileId);
        boolean status = file.delete();
        return status ? OperationResult.SUCCESS : OperationResult.of(Operation.FAILED, "File Delete Failed");
    }

    private File getFile(String fileId) {
        return Paths.get(baseDirectory, fileId).toFile();
    }

    private OperationResult internalWrite(String fileId, ByteBuf content, boolean finalBytes) {
        OutputStream outputStream;
        try {
            outputStream = createOrGetOutputStream(fileId);
        } catch (Exception e) {
            return OperationResult.of(Operation.FAILED, "Could Not Open Stream");
        }

        try {
            while (content.isReadable()) {
                int remain = content.writerIndex() - content.readerIndex();
                content.readBytes(outputStream, Math.min(remain, BUFFER));
            }
        } catch (IOException e) {
            return OperationResult.of(Operation.FAILED, "Could Not Write File");
        }

        if (finalBytes) {
            outputStreamMap.remove(fileId);
            closeSilently(outputStream);
        }

        return OperationResult.SUCCESS;
    }

    private void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {

            }
        }
    }

    private OutputStream createOrGetOutputStream(String fileId) throws Exception {
        OutputStream outputStream = outputStreamMap.get(fileId);
        if (outputStream == null) {
            outputStream = new FileOutputStream(getFile(fileId), true);
            outputStreamMap.put(fileId, outputStream);
        }
        return outputStream;
    }

    @Override
    public OperationResult merge(String fileId, String... fileIds) {
        try {
            FileOutputStream outputStream = new FileOutputStream(getFile(fileId));

            for (String eachFile : fileIds) {

                FileInputStream fileInputStream = new FileInputStream(getFile(eachFile));

                int length;
                byte[] arr = new byte[4096];

                while ((length = fileInputStream.read(arr)) != -1) {
                    outputStream.write(arr, 0, length);
                }

                arr = null;

                fileInputStream.close();
            }

            outputStream.close();
            return OperationResult.SUCCESS;
        } catch (Exception e) {
            return OperationResult.of(Operation.FAILED, "File Merge Failed");
        }

    }

    @Override
    public OperationResult createAndWrite(String fileId, ByteBuf byteBuf, boolean finalBytes) {
        return this.internalWrite(fileId, byteBuf, finalBytes);
    }

    @Override
    public void afterConnectionClose(String fileId) {

    }

    @Override
    public void onException(String fileId, Throwable e) {

    }
}
