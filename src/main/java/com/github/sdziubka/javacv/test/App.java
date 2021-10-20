package com.github.sdziubka.javacv.test;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.TickMeter;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class App {

    public static void main(String[] args) {
//            FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
//             FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
        faceRecognizer.setThreshold(8.0);
        int counter = 0;

        trainFaceRecognizer(faceRecognizer, readImageFiles(args[0]), counter);

        File[] test = readImageFiles(args[1]);

        int found = 0;
        int failed = 0;
        int notFound = 0;

        for (File testImgFile : test) {
            int[] label = new int[]{-1};
            double[] confidence = new double[]{0.0};
            faceRecognizer.predict(imread(testImgFile.getPath(), IMREAD_GRAYSCALE), label, confidence);
            int testLabel = readLabelFromFileName(testImgFile);
            boolean matches = testLabel == label[0];
            System.out.printf("Source label: %s Predicted labels: %s, Matched: %s, Confidence: %s%n", testLabel, Arrays.toString(label), matches, Arrays.toString(confidence));
            if (matches) {
                found++;
            } else if (label[0] == -1) {
                notFound++;
            } else {
                failed++;
            }
        }
        System.out.printf("%nTest finished%nFound: %d%nNot found: %d%nFailed: %d%n", found, notFound, failed);
    }

    public static void trainFaceRecognizer(FaceRecognizer faceRecognizer, File[] imageFiles, int counter) {
        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        MatVector images = new MatVector(imageFiles.length);
        bindLabelsToImages(counter, imageFiles, images, labels);
        TickMeter tickMeter = new TickMeter();
        System.out.println("Start training");
        tickMeter.start();
        faceRecognizer.train(images, labels);
//            faceRecognizer.update(images, labels);
        tickMeter.stop();
        System.out.printf("Model trained in %s seconds%n", tickMeter.getTimeSec());
    }

    public static void bindLabelsToImages(int counter, File[] imageFiles, MatVector images, Mat labels) {
        IntBuffer labelsBuf = labels.createBuffer();
        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), IMREAD_GRAYSCALE);
            int label = readLabelFromFileName(image);
            images.put(counter, img);
            labelsBuf.put(counter, label);
            counter++;
        }
        System.out.printf("Read %d image files%n", imageFiles.length);
    }

    static int readLabelFromFileName(File file) {
        return Integer.parseInt(file.getName().split("\\_")[0]);
    }

    public static File[] readImageFiles(String directory) {
        File root = new File(directory);

        FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        return root.listFiles(imgFilter);
    }
}
