package de.presti.pissai.main;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.training.Trainer;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.presti.pissai.trainer.BinaryImageTranslator;
import de.presti.pissai.trainer.ModelTrainer;
import de.presti.pissai.utils.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PissAI {

    static PissAI instance;
    ModelTrainer modelTrainer;
    ImageFolder imageFolder;
    Model model;
    List<String> syncs = new ArrayList<>();

    public static void create() throws TranslateException, IOException, MalformedModelException {
        instance = new PissAI();

        instance.modelTrainer = new ModelTrainer();
        instance.imageFolder = instance.modelTrainer.createDataSet();
        instance.syncs = instance.imageFolder.getSynset();
        instance.model = instance.modelTrainer.getModel(instance.imageFolder, false);
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            test(true);
        } else {
            startCreation();
        }
    }

    public static void test(boolean valid) throws TranslateException, IOException, MalformedModelException {
        create();
        instance.runTest(instance.model, instance.syncs, valid);
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

    public Model loadPrevious() throws MalformedModelException, IOException {
        return instance.modelTrainer.getModel(getSyncs().size(), false);
    }

    public void runTest(Model model, List<String> classes, boolean valid) throws IOException, TranslateException {
        String validImage = "https://upload.wikimedia.org/wikipedia/en/thumb/1/1d/Dream_icon.svg/1200px-Dream_icon.svg.png";
        String invalidImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/Red.svg/2048px-Red.svg.png";
        String imageUrl = valid ? validImage : invalidImage;
        Image imageToCheck = ImageFactory.getInstance().fromUrl(imageUrl);
        NDArray ndArray = imageToCheck.toNDArray(NDManager.newBaseManager()).squeeze();
        imageToCheck = ImageFactory.getInstance().fromNDArray(ndArray);

        Translator<Image, Float> translator =
                BinaryImageTranslator.builder()
                        .addTransform(new Resize(256, 256))
                        .addTransform(NDArray::squeeze)
                        .addTransform(new ToTensor())
                        .build();

        Predictor<Image, Float> predictor = model.newPredictor(translator);

        float classifications = predictor.predict(imageToCheck);
        /*JsonElement jsonElement = JsonParser.parseString(classifications.toJson());

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            String detected = "";
            float highestValue = 0.0f;

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement jsonElement1 = jsonArray.get(i);
                if (jsonElement1.isJsonObject()) {
                    JsonObject jsonObject = jsonElement1.getAsJsonObject();

                    String name = jsonObject.get("className").getAsString();
                    float currentValue = jsonObject.get("probability").getAsFloat();

                    System.out.println(name + " - " + Math.round(currentValue * 100) + "%");

                    if (highestValue < currentValue) {
                        highestValue = currentValue;
                        detected = name;
                    }
                }
              }
            }*/

        System.out.println("It is most likely dream, about " + Math.round(classifications * 100) + "%");
    }

    public JsonObject checkImage(Image image, Model model, List<String> classes) throws TranslateException {

        Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder()
                        .addTransform(new Resize(256, 256))
                        .addTransform(new ToTensor())
                        .optSynset(classes)
                        .optApplySoftmax(true)
                        .build();

        Predictor<Image, Classifications> predictor = model.newPredictor(translator);

        Classifications classifications = predictor.predict(image);
        JsonElement jsonElement = JsonParser.parseString(classifications.toJson());

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            JsonObject detected = new JsonObject();
            float highestValue = 0.0f;

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement jsonElement1 = jsonArray.get(i);
                if (jsonElement1.isJsonObject()) {
                    JsonObject jsonObject = jsonElement1.getAsJsonObject();

                    String name = jsonObject.get("className").getAsString();
                    float currentValue = jsonObject.get("probability").getAsFloat();

                    System.out.println(name + " - " + Math.round(currentValue * 100) + "%");

                    if (highestValue < currentValue) {
                        highestValue = currentValue;
                        detected = jsonObject;
                    }
                }
            }

            return detected;
        }

        return new JsonObject();
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

    public List<String> getSyncs() {
        return syncs;
    }
}
