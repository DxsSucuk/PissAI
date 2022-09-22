package de.presti.pissai.main;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import de.presti.pissai.trainer.BinaryImageTranslator;
import de.presti.pissai.trainer.ModelTrainer;
import de.presti.pissai.utils.TimeUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;

public class PissAI {

    static PissAI instance;
    ModelTrainer modelTrainer;
    ImageFolder imageFolder;
    Model model;

    public static void create() throws TranslateException, IOException, MalformedModelException {
        instance = new PissAI();

        instance.modelTrainer = new ModelTrainer();
        instance.imageFolder = instance.modelTrainer.createDataSet();
        instance.model = instance.modelTrainer.getModel(instance.imageFolder, false);
    }

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).contains("--clean")) {
            checkFile(new File("validation"));
            checkFile(new File("imagefolder"));
        }

        if (Arrays.asList(args).contains("--train")) {
            startCreation();
        }

        if (Arrays.asList(args).contains("--test")) {
            test();
        }
    }

    public static void test() throws TranslateException, IOException, MalformedModelException {
        create();
        instance.runTest(instance.model, false);
        instance.runTest(instance.model, true);
    }

    public static void startCreation() throws Exception {
        if (instance == null) instance = new PissAI();
        if (instance.modelTrainer == null) instance.modelTrainer = new ModelTrainer();
        System.out.println("Starting Training!");
        long start = System.currentTimeMillis();

        Object[] newCreation = instance.createNew();

        Model model = (Model) newCreation[0];
        Trainer trainer = (Trainer) newCreation[1];

        long finishTime = System.currentTimeMillis() - start;

        System.out.println("Finished in " + finishTime + "ms!");
        System.out.println("That is about, " + TimeUtil.getTime(start));
    }

    public Object[] createNew() throws Exception {
        ImageFolder imageFolder = instance.modelTrainer.createDataSet();

        Model model = instance.modelTrainer.getModel(imageFolder, true);

        Trainer trainer = instance.modelTrainer.getTrainer(model, imageFolder, new int[] { 12, 17, 23, 28},32);

        instance.modelTrainer.runTrainer(trainer, imageFolder, model);

        return new Object[]{model, trainer};
    }

    public Model loadPrevious() throws MalformedModelException, IOException, TranslateException {
        return instance.modelTrainer.getModel(instance.imageFolder.getSynset().size(), false);
    }

    public void runTest(Model model, boolean valid) throws IOException, TranslateException {
        String validImage = "https://note-store.de/upload/iblock/5b1/Dream-_-MASK.jpg";
        String invalidImage = "https://sase.org/wp-content/uploads/2019/04/red-abstract-2.png";
        String imageUrl = valid ? validImage : invalidImage;
        Image imageToCheck = ImageFactory.getInstance().fromUrl(imageUrl);

        float classifications = checkImage(imageToCheck);

        System.out.println(classifications);
        System.out.println("A confidence rate of " + Math.round((1 - classifications) * 100) + "% that it is dream.");
        System.out.println("Used Image: " + imageUrl);
    }

    public float checkImage(Image imageToCheck) throws TranslateException {
        imageToCheck = ImageFactory.getInstance().fromNDArray(imageToCheck.toNDArray(NDManager.newBaseManager()).squeeze());

        Translator<Image, Float> translator =
                BinaryImageTranslator.builder()
                        .addTransform(new Resize(256, 256))
                        .addTransform(NDArray::squeeze)
                        .addTransform(new ToTensor())
                        .build();

        // Using "1 - actualPrediction" keeps us from getting false positives, but gives us a lot of false negatives.
        return model.newPredictor(translator).predict(imageToCheck);
    }

    private static void checkFile(File folder) {
        if (folder == null) return;
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                checkFile(file);
                continue;
            }

            if (file.getName().contains(" ")) {
                File dest = new File(file.getParentFile(), file.getName().replace(" ", "_"));

                if (file.renameTo(dest)) {
                    file = dest;
                    System.out.println("Renamed " + file.getName());
                } else {
                    System.out.println("Could not rename " + file.getName());
                }
            }

            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = ImageFactory.getInstance().fromFile(file.toPath());
                NDArray ndArray = image.toNDArray(NDManager.newBaseManager()).squeeze();

                if (bufferedImage == null) {
                    file.delete();
                    System.out.println("Deleted " + file.getName() + ", reason null");
                    continue;
                }

                if (bufferedImage.getWidth() < 256 || bufferedImage.getHeight() < 256) {
                    file.delete();
                    System.out.println("Deleted " + file.getName() + ", reason Width: " + bufferedImage.getWidth() + ", Height: " + bufferedImage.getHeight());
                } else {
                    Raster ras = bufferedImage.getRaster();

                    if (ras.getNumDataElements() != 3) {
                        file.delete();
                        System.out.println("Deleted " + file.getName() + ", reason: not 3 channel image instead " + ras.getNumDataElements() + " channel image");
                    }
                }
            } catch (Exception exception) {
                file.delete();
                System.out.println("Deleted " + file.getName() + ", reason: " + exception.getMessage());
                // exception.printStackTrace();
            }
        }
    }

    public static PissAI getInstance() {
        return instance;
    }

    public ModelTrainer getModelTrainer() {
        return modelTrainer;
    }

    public ImageFolder getImageFolder() {
        return imageFolder;
    }

    public Model getModel() {
        return model;
    }
}
