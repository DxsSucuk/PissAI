import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf

from keras.preprocessing.image import ImageDataGenerator
from keras.optimizers import SGD
from keras import layers
from keras.models import Sequential
import pathlib

data_dir = pathlib.Path('./image')

batch_size = 32
img_height = 256
img_width = 256

# configure tensorflow to use gpu cuda
physical_devices = tf.config.experimental.list_physical_devices('GPU')
if len(physical_devices) > 0:
    tf.config.experimental.set_virtual_device_configuration(
        physical_devices[0],
        [tf.config.experimental.VirtualDeviceConfiguration(memory_limit=1024*9)])
    logical_devices = tf.config.experimental.list_logical_devices('GPU')
    print(len(physical_devices), "Physical GPUs,",
          len(logical_devices), "Logical GPUs")


train_datagen = ImageDataGenerator(
    rescale=1./255,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True
)

test_datagen = ImageDataGenerator(
    rescale=1./255
)

train_ds = train_datagen.flow_from_directory(
    data_dir,
    target_size=(img_width, img_height),
    batch_size=batch_size,
    class_mode='binary'
)

val_ds = test_datagen.flow_from_directory(
    './validation/',
    target_size=(img_width, img_height),
    batch_size=batch_size,
    class_mode='binary'
)

model = Sequential([
    # OLD NETWORK
    #    layers.Rescaling(1./255, input_shape=(img_height, img_width, 3)),
    #    layers.Conv2D(16, 3, padding='same', activation='relu'),
    #    layers.MaxPooling2D(),
    #    layers.Conv2D(32, 3, padding='same', activation='relu'),
    #    layers.MaxPooling2D(),
    #    layers.Conv2D(64, 3, padding='same', activation='relu'),
    #    layers.MaxPooling2D(),
    layers.Conv2D(32, (3, 3), activation='relu',
                  input_shape=(img_height, img_width, 3)),
    layers.Conv2D(64, (3, 3), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    layers.Conv2D(128, (3, 3), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    layers.Conv2D(128, (3, 3), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    layers.Flatten(),
    layers.Dense(512, activation='relu'),
    layers.Dropout(0.5),
    layers.Dense(1, activation='sigmoid')
])

model.compile(optimizer='adam',
              loss='binary_crossentropy',
              metrics=['accuracy'])

model.summary()

epochs = 30

history = model.fit(
    train_ds,
    validation_data=val_ds,
    epochs=epochs
)

acc = history.history['accuracy']
loss = history.history['loss']

epochs_range = range(epochs)

plt.figure(figsize=(8, 8))
plt.subplot(1, 2, 1)
plt.plot(epochs_range, acc, label='Training Accuracy')
plt.plot(epochs_range, loss, label='Training Loss')
plt.legend(loc='lower right')
plt.title('Training Accuracy & Loss')
plt.show()

model.save('../datasets/trained.h5')
print("[i] Model saved!")

# Quick test of the model, use test.py for the sex

img = tf.keras.utils.load_img(
    './validation/dream/6733b8d5-63e9-47cf-91f6-2726e9bafaea.png', target_size=(img_height, img_width)
)
img_array = tf.keras.utils.img_to_array(img)
img_array = tf.expand_dims(img_array, 0)  # Create a batch

predictions = model.predict(img_array)
score = tf.nn.softmax(predictions[0])

class_names = ['dream', 'nondream']

print(
    "This is {}, {:.2f} percent confidence."
    .format(class_names[np.argmax(score)], 100 * np.max(score))
)