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
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.Trainer;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.presti.pissai.trainer.ModelTrainer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PissAI {

    static PissAI instance;
    ModelTrainer modelTrainer;

    public static void main(String[] args) throws Exception {
        instance = new PissAI();

        instance.modelTrainer = new ModelTrainer();

        /*Object[] newCreation = instance.createNew();

        Model model = (Model) newCreation[0];
        Trainer trainer = (Trainer) newCreation[1];*/

        ImageFolder imageFolder = instance.modelTrainer.createDataSet();

        Model model = instance.loadPrevious();

        instance.runTest(model, 7, imageFolder.getClasses());
    }

    public Object[] createNew() throws Exception {
        ImageFolder imageFolder = instance.modelTrainer.createDataSet();

        Model model = instance.modelTrainer.getModel(imageFolder);

        Trainer trainer = instance.modelTrainer.getTrainer(model, imageFolder);

        instance.modelTrainer.runTrainer(trainer, imageFolder, model);

        return new Object[] {model, trainer};
    }

    public Model loadPrevious() throws MalformedModelException, IOException {
        return instance.modelTrainer.getModel(7);
    }

    public void runTest(Model model, int output, List<String> classes) throws IOException, TranslateException {
        Image imageToCheck = ImageFactory.getInstance().fromUrl("https://cdn.discordapp.com/attachments/913079825336512562/965162639514800178/he_hates_bl.jpg");
        Object wrappedImage = imageToCheck.getWrappedImage();

        Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder()
                        .addTransform(new Resize(128, 128))
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

}
