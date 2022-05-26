package de.presti.pissai.trainer;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.BaseImageTranslator;
import ai.djl.ndarray.NDList;
import ai.djl.nn.Activation;
import ai.djl.translate.ArgumentsUtil;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

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
        return Activation.sigmoid(list.singletonOrThrow()).toFloatArray()[0];
    }

    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        super.prepare(ctx);
    }

    /**
     * Creates a builder to build a {@code BinaryImageTranslator}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder to build a {@code BinaryImageTranslator} with specified arguments.
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

        Builder() {}

        /** {@inheritDoc} */
        @Override
        protected Builder self() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        protected void configPostProcess(Map<String, ?> arguments) {
            super.configPostProcess(arguments);
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
