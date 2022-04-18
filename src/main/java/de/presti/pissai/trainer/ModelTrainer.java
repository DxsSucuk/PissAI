package de.presti.pissai.trainer;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.Repository;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModelTrainer {

    Application application;

    public ModelTrainer() {
        application = Application.CV.IMAGE_CLASSIFICATION;
    }

    public ImageFolder createDataSet() throws TranslateException, IOException {
        int batchSize = 32;
        // set the image folder path
        Repository repository = Repository.newInstance("folder", Paths.get("imagefolder"));
        ImageFolder dataset =
               ImageFolder.builder()
                        .setRepository(repository)
                        .addTransform(new Resize(128, 128))
                        .addTransform(new ToTensor())
                        .setSampling(batchSize, true)
                        .build();
        // call prepare before using
        dataset.prepare();

        return dataset;
    }

    public Trainer getTrainer(Model model, ImageFolder dataset) throws IOException {
        dataset.prepare(new ProgressBar());

        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());

        return model.newTrainer(config);
    }

    public Model getModel(ImageFolder dataset) throws TranslateException, IOException, MalformedModelException {
        return getModel(dataset.getSynset().size());
    }

    public Model getModel(int outputSize) throws MalformedModelException, IOException {
        long inputSize = 128 * 128 * 3;

        SequentialBlock sequentialBlock = new SequentialBlock();

        sequentialBlock.add(Blocks.batchFlattenBlock(inputSize));
        sequentialBlock.add(Linear.builder().setUnits(128).build());
        sequentialBlock.add(Activation::relu);
        sequentialBlock.add(Linear.builder().setUnits(64).build());
        sequentialBlock.add(Activation::relu);
        sequentialBlock.add(Linear.builder().setUnits(outputSize).build());


        Path modelDir = Paths.get("datasets");
        Model model = Model.newInstance("mlp");
        model.setBlock(sequentialBlock);
        model.load(modelDir);

        return model;
    }

    public void runTrainer(Trainer trainer, ImageFolder dataset, Model model) throws TranslateException, IOException {
        int epochen = 10000;
        trainer.initialize(new Shape(128 * 128 * 3));

        EasyTrain.fit(trainer, epochen, dataset, null);

        saveModel(model, epochen);
    }

    public void saveModel(Model model, int epochen) throws IOException {
        model.setProperty("Epoch", String.valueOf(epochen));
        model.save(Paths.get("datasets"), "mlp");
    }

}
