package de.presti.pissai.trainer;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.BaseImageTranslator;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.ArgumentsUtil;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.Arrays;
import java.util.Map;

public class BinaryImageTranslator extends BaseImageTranslator<Float> implements Translator<Image, Float> {

    /**
     * Constructs an ImageTranslator with the provided builder.
     *
     * @param builder the data to build with
     */
    public BinaryImageTranslator(BaseBuilder<?> builder) {
        super(builder);
    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }

    @Override
    public Float processOutput(TranslatorContext ctx, NDList list) {
        NDArray ndArray = list.singletonOrThrow();
        ndArray = ndArray.softmax(0);
        float[] array = ndArray.toFloatArray();
        System.out.println(Arrays.toString(array));
        return array[0];
    }

    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        super.prepare(ctx);
    }

    /**
     * Creates a builder to build a {@code ImageClassificationTranslator}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder to build a {@code ImageClassificationTranslator} with specified arguments.
     *
     * @param arguments arguments to specify builder options
     * @return a new builder
     */
    public static Builder builder(Map<String, ?> arguments) {
        Builder builder = new Builder();
        // builder.configPreProcess(arguments);
        builder.configPostProcess(arguments);
        return builder;
    }

    /** A Builder to construct a {@code BinaryImageTranslator}. */
    public static class Builder extends BaseBuilder<Builder> {

        private boolean applySoftmax;
        private int topK = 5;

        Builder() {}

        /**
         * Set the topK number of classes to be displayed.
         *
         * @param topK the number of top classes to return
         * @return the builder
         */
        public Builder optTopK(int topK) {
            this.topK = topK;
            return this;
        }

        /**
         * Sets whether to apply softmax when processing output. Some models already include softmax
         * in the last layer, so don't apply softmax when processing model output.
         *
         * @param applySoftmax boolean whether to apply softmax
         * @return the builder
         */
        public Builder optApplySoftmax(boolean applySoftmax) {
            this.applySoftmax = applySoftmax;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        protected Builder self() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        protected void configPostProcess(Map<String, ?> arguments) {
            super.configPostProcess(arguments);
            applySoftmax = ArgumentsUtil.booleanValue(arguments, "applySoftmax");
            topK = ArgumentsUtil.intValue(arguments, "topK", 5);
        }

        /**
         * Builds the {@link BinaryImageTranslator} with the provided data.
         *
         * @return an {@link BinaryImageTranslator}
         */
        public BinaryImageTranslator build() {
            validate();
            return new BinaryImageTranslator(this);
        }
    }
}
