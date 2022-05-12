package de.presti.pissai.trainer;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.LambdaBlock;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.Repository;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.evaluator.BinaryAccuracy;
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

    public ImageFolder getValidationDataSet() throws TranslateException, IOException {
        int batchSize = 32;
        // set the image folder path
        Repository repository = Repository.newInstance("folder", Paths.get("validation"));
        ImageFolder dataset =
                ImageFolder.builder()
                        .setRepository(repository)
                        .addTransform(new Resize(256, 256))
                        .addTransform(new ToTensor())
                        .setSampling(batchSize, true)
                        .build();
        // call prepare before using
        dataset.prepare();

        return dataset;
    }

    public ImageFolder createDataSet() throws TranslateException, IOException {
        int batchSize = 32;
        // set the image folder path
        Repository repository = Repository.newInstance("folder", Paths.get("imagefolder"));
        ImageFolder dataset =
               ImageFolder.builder()
                        .setRepository(repository)
                        .addTransform(new Resize(256, 256))
                        .addTransform(new ToTensor())
                        .setSampling(batchSize, true)
                        .build();
        // call prepare before using
        dataset.prepare();

        return dataset;
    }

    public Trainer getTrainer(Model model, ImageFolder dataset) throws IOException {
        dataset.prepare(new ProgressBar());

        TrainingConfig config = new DefaultTrainingConfig(Loss.sigmoidBinaryCrossEntropyLoss())
                .addEvaluator(new BinaryAccuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());

        return model.newTrainer(config);
    }

    public Model getModel(ImageFolder dataset, boolean create) throws TranslateException, IOException, MalformedModelException {
        return getModel(dataset.getSynset().size(), create);
    }

    public Model getModel(int outputSize, boolean create) throws MalformedModelException, IOException {
        long inputSize = 256 * 256 * 3;

        SequentialBlock sequentialBlock = new SequentialBlock();

        sequentialBlock.add(Blocks.batchFlattenBlock(inputSize));
        sequentialBlock.add(Linear.builder().setUnits(256).build());
        sequentialBlock.add(Activation::relu);
        sequentialBlock.add(Linear.builder().setUnits(128).build());
        sequentialBlock.add(Activation::relu);
        sequentialBlock.add(Linear.builder().setUnits(outputSize).build());
        sequentialBlock.add(LambdaBlock.singleton(NDArray::squeeze));

        Path modelDir = Paths.get("datasets");
        Model model = Model.newInstance("mlp");
        model.setBlock(sequentialBlock);
        if (!create) model.load(modelDir);
        ;

        return model;
    }

    public void runTrainer(Trainer trainer, ImageFolder dataset, Model model) throws TranslateException, IOException {
        int epochen = (int) dataset.size();
        System.out.println("Using about " + epochen + " Epochs on " + dataset.size() + " Images.");
        trainer.initialize(new Shape(256 * 256 * 3));

        EasyTrain.fit(trainer, epochen, dataset, null);

        saveModel(model, epochen);
    }

    public void saveModel(Model model, int epochen) throws IOException {
        model.setProperty("Epoch", String.valueOf(epochen));
        model.save(Paths.get("datasets"), "mlp");
    }

}
