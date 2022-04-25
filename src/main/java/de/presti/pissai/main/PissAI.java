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
import ai.djl.training.Trainer;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.presti.pissai.trainer.ModelTrainer;
import de.presti.pissai.utils.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
        // create();
        startCreation();
        //instance.runTest(instance.model, instance.syncs);
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
        Date finishDate = new Date(finishTime);

        System.out.println("Finished in " + finishTime + "ms!");
        System.out.println("That is about, " + TimeUtil.getTime(start));
    }

    public Object[] createNew() throws Exception {
        ImageFolder imageFolder = instance.modelTrainer.createDataSet();

        Model model = instance.modelTrainer.getModel(imageFolder, true);

        Trainer trainer = instance.modelTrainer.getTrainer(model, imageFolder);

        instance.modelTrainer.runTrainer(trainer, imageFolder, model);

        return new Object[] {model, trainer};
    }

    public Model loadPrevious() throws MalformedModelException, IOException {
        return instance.modelTrainer.getModel(7, false);
    }

    public void runTest(Model model, List<String> classes) throws IOException, TranslateException {
        String imageUrl = "https://cdn.discordapp.com/avatars/321580743488831490/fe6f07d55edc9842214dc9bef29a9a19.png?size=1024";
        Image imageToCheck = ImageFactory.getInstance().fromUrl(imageUrl);
        Object wrappedImage = imageToCheck.getWrappedImage();

        Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder()
                        .addTransform(new Resize(256, 256))
                        .addTransform(new ToTensor())
                        .optSynset(classes)
                        .optApplySoftmax(true)
                        .build();

        Predictor<Image, Classifications> predictor = model.newPredictor(translator);

        Classifications classifications = predictor.predict(imageToCheck);
        JsonElement jsonElement = JsonParser.parseString(classifications.toJson());

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

            System.out.println("It is most likely " + detected + ", about " + Math.round(highestValue * 100) + "%");
        }
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
