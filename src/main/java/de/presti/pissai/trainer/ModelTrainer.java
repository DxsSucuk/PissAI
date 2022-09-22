package de.presti.pissai.trainer;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.Parameter;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.Repository;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.evaluator.BinaryAccuracy;
import ai.djl.training.initializer.Initializer;
import ai.djl.training.initializer.XavierInitializer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.training.tracker.WarmUpTracker;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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

    public ImageFolder createDataSet() throws IOException {
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
        dataset.prepare(new ProgressBar());

        return dataset;
    }

    public Trainer getTrainer(Model model, ImageFolder dataset, int[] epochs, int batch) throws IOException {
        dataset.prepare(new ProgressBar());

        int[] steps = Arrays
                .stream(epochs)
                .map(k -> k * 60000 / batch).toArray();

        //initialize neural network weights using Xavier initializer
        Initializer initializer = new XavierInitializer(
                XavierInitializer.RandomType.UNIFORM,
                XavierInitializer.FactorType.AVG, 2);

        //set the learning rate
        //adjusts weights of network based on loss
        WarmUpTracker learningRateTracker = Tracker.warmUp().setMainTracker(Tracker.multiFactor()
                        .setSteps(steps)
                        .setBaseValue(0.01f)
                        .optFactor(0.1f).build())
                .optWarmUpBeginValue(1e-3f)
                .optWarmUpSteps(500)
                .build();

        //set optimization technique
        //minimizes loss to produce better and faster results
        //Stochastic gradient descent
        Optimizer optimizer = Optimizer
                .sgd()
                .setRescaleGrad(1.0f / batch)
                .setLearningRateTracker(learningRateTracker)
                .optMomentum(0.9f)
                .optWeightDecays(0.001f)
                .optClipGrad(1f)
                .build();

        TrainingConfig config = new DefaultTrainingConfig(Loss.sigmoidBinaryCrossEntropyLoss())
                .addEvaluator(new BinaryAccuracy())
                .optOptimizer(optimizer)
                .optInitializer(initializer, Parameter.Type.WEIGHT)
                // .optDevices(DeviceUtil.tryAllGpus())
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
        sequentialBlock.addSingleton(a -> a.get(":, 0"));

        Path modelDir = Paths.get("datasets");
        Model model = Model.newInstance("mlp");
        model.setBlock(sequentialBlock);
        if (!create) model.load(modelDir);

        return model;
    }

    public void runTrainer(Trainer trainer, ImageFolder dataset, Model model) throws TranslateException, IOException {
        int epochen = (model.getProperty("Epoch") != null ?
                Integer.parseInt(model.getProperty("Epoch")) : 50);
        System.out.println("Using about " + epochen + " Epochs on " + dataset.size() + " Images.");
        trainer.setMetrics(new Metrics());
        trainer.initialize(new Shape(32, 256 * 256 * 3));

        EasyTrain.fit(trainer, epochen, dataset, getValidationDataSet());

        saveModel(model, epochen);

        LoggerFactory.getLogger(this.getClass()).info("Train-Loss: " + trainer.getTrainingResult().getTrainLoss());
        LoggerFactory.getLogger(this.getClass()).info("Valid-Loss: " + trainer.getTrainingResult().getValidateLoss());
        LoggerFactory.getLogger(this.getClass()).info("Epochs: " + trainer.getTrainingResult().getEpoch());
        LoggerFactory.getLogger(this.getClass()).info("Evals: " + trainer.getTrainingResult().getEvaluations());
    }

    public void saveModel(Model model, int epochen) throws IOException {
        model.setProperty("Epoch", String.valueOf(epochen));
        model.save(Paths.get("datasets"), "mlp");
    }

}
