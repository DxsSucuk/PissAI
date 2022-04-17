package de.presti.pissai.main;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.training.Trainer;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import de.presti.pissai.trainer.ModelTrainer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

public class Main {

    static Main instance;
    ModelTrainer modelTrainer;

    public static void main(String[] args) throws Exception {
        instance = new Main();

        instance.modelTrainer = new ModelTrainer();

        /*Object[] newCreation = instance.createNew();

        Model model = (Model) newCreation[0];
        Trainer trainer = (Trainer) newCreation[1];*/

        Model model = instance.loadPrevious();

        instance.runTest(model, 7);
    }

    public Object[] createNew() throws Exception {
        ImageFolder imageFolder = instance.modelTrainer.createDataSet();

        Model model = instance.modelTrainer.getModel(imageFolder);

        Trainer trainer = instance.modelTrainer.getTrainer(model, imageFolder);

        instance.modelTrainer.runTrainer(trainer, imageFolder, model);

        return new Object[] {model, trainer};
    }

    public Model loadPrevious() {
        return instance.modelTrainer.getModel(7);
    }

    public void runTest(Model model, int output) throws IOException, TranslateException {
        Image imageToCheck = ImageFactory.getInstance().fromFile(Paths.get("test", "dream.jpg"));
        Object wrappedImage = imageToCheck.getWrappedImage();

        Translator<Image, Classifications> translator = new Translator<>() {
            @Override
            public Classifications processOutput(TranslatorContext ctx, NDList list) {
                // Create a Classifications with the output probabilities
                NDArray probabilities = list.singletonOrThrow().softmax(0);
                List<String> classNames = IntStream.range(0, output).mapToObj(String::valueOf).toList();
                return new Classifications(classNames, probabilities);
            }

            @Override
            public NDList processInput(TranslatorContext ctx, Image input) {
                // Convert Image to NDArray
                NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
                return new NDList(NDImageUtils.toTensor(NDImageUtils.resize(array, 128 * 128)));
            }

            @Override
            public Batchifier getBatchifier() {
                return Batchifier.STACK;
            }
        };

        Predictor<Image, Classifications> predictor = model.newPredictor(translator);

        Classifications classifications = predictor.predict(imageToCheck);
        System.out.println(classifications.toJson());
    }

}
