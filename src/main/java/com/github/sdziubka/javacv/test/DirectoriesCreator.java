package com.github.sdziubka.javacv.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoriesCreator {

    public static void main(String[] args) throws IOException {

        removeDirWithFiles(args[1]);
        removeDirWithFiles(args[3]);

        FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        Path newTrainingDir = Files.createDirectory(Paths.get(args[1]));
        Path newTestDir = Files.createDirectory(Paths.get(args[3]));
        File[] dirs = new File(args[0]).listFiles();

        int filesToTraining = Integer.parseInt(args[2]);
        int filesToTest = Integer.parseInt(args[4]);
        int filesTotal = filesToTest + filesToTraining;

        for (int i = 0; i < dirs.length; i++) {

            File[] imgFiles = dirs[i].listFiles(imgFilter);

            Arrays.sort(imgFiles);

            int j = 0;
            for (; j < filesToTraining; j++) {
                File imgFile = imgFiles[j];
                Files.copy(imgFile.toPath(), Paths.get(String.format("%s/%d_%d%s",
                        newTrainingDir,
                        i,
                        j,
                        getExtension(imgFile)
                )));
            }

            for (; j < filesTotal; j++) {
                File imgFile = imgFiles[j];
                Files.copy(imgFile.toPath(), Paths.get(String.format("%s/%d_%d%s",
                        newTestDir,
                        i,
                        j,
                        getExtension(imgFile)
                )));
            }
        }
    }

    static void removeDirWithFiles(String path) throws IOException {
        Files.walk(Paths.get(path))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    static String getExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf(".")).toLowerCase();
    }
}
