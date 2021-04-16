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

import java.io.*;
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
    public OperationResult write(String fileId, InputStream inputStream) {
        return internalWrite(fileId, inputStream);
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

    private OperationResult internalWrite(String fileId, InputStream inputStream) {
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(getFile(fileId), true);
        } catch (Exception e) {
            return OperationResult.of(Operation.FAILED, "Could Not Open Stream");
        }

        byte[] buff = new byte[BUFFER];
        int len;
        try {
            while ((len = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
        } catch (Exception e) {
            return OperationResult.of(Operation.FAILED, "Could Not Write Stream");
        }

        closeSilently(outputStream);

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
    public OperationResult createAndWrite(String fileId, InputStream inputStream) {
        return this.internalWrite(fileId, inputStream);
    }

    @Override
    public void afterConnectionClose(String fileId) {

    }

    @Override
    public void onException(String fileId, Throwable e) {

    }
}
