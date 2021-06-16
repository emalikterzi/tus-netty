package com.emtdev.tus.store;

import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.Operation;
import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.extension.ConcatenationExtension;
import com.emtdev.tus.core.extension.CreationWithUploadExtension;
import com.emtdev.tus.core.extension.TerminationExtension;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileStore extends TusStore implements ConcatenationExtension, CreationWithUploadExtension, TerminationExtension {

    private final static int BUFFER = 4096;
    private final String baseDirectory;
    private final Map<String, OutputStream> fileIdOutputMap = new HashMap<>();

    public FileStore(String baseDirectory, TusConfigStore configStore) {
        super(configStore);
        this.baseDirectory = baseDirectory;
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
    public OperationResult write(String fileId, ByteBuf byteBuf) {
        return internalWrite(fileId, byteBuf);
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
    public OperationResult finalizeFile(String fileId) {
        OutputStream outputStream = this.fileIdOutputMap.get(fileId);

        if (outputStream == null) {
            return OperationResult.SUCCESS;
        }

        closeSilently(outputStream);

        this.fileIdOutputMap.remove(fileId);

        return OperationResult.SUCCESS;
    }

    @Override
    public OperationResult delete(String fileId) {
        File file = getFile(fileId);
        boolean status = file.delete();
        return status ? OperationResult.SUCCESS : OperationResult.of(Operation.FAILED, "File Delete Failed");
    }

    protected File getFile(String fileId) {
        return Paths.get(baseDirectory, fileId).toFile();
    }

    private OperationResult internalWrite(String fileId, ByteBuf byteBuf) {
        ByteBufInputStream inputStream = new ByteBufInputStream(byteBuf, false);

        OutputStream outputStream;
        try {
            outputStream = getOutputStream(fileId);
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

        closeSilently(inputStream);

        return OperationResult.SUCCESS;
    }

    private OutputStream getOutputStream(String fileId) throws Exception {
        OutputStream outputStream = this.fileIdOutputMap.get(fileId);

        if (outputStream == null) {
            outputStream = new FileOutputStream(getFile(fileId), true);
            this.fileIdOutputMap.put(fileId, outputStream);
        }

        return outputStream;

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

}
