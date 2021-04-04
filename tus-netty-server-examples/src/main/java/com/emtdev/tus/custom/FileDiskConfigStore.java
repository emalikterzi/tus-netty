package com.emtdev.tus.custom;

import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.domain.FileStat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

public class FileDiskConfigStore implements TusConfigStore {

    private final String baseDirectory;

    public FileDiskConfigStore(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void save(FileStat fileStat) {
        saveToFile(fileStat);
    }

    @Override
    public void update(FileStat fileStat) {
        saveToFile(fileStat);
    }

    private void saveToFile(FileStat stat) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(Paths.get(baseDirectory, stat.getFileId()).toFile()));
            objectOutputStream.writeObject(stat);
            objectOutputStream.close();
        } catch (Exception e) {

        }
    }


    @Override
    public FileStat get(String fileId) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(Paths.get(baseDirectory, fileId).toFile()));
            return (FileStat) objectInputStream.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
