import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# Images get scaled to this:
img_height = 256
img_width = 256
batch_size = 32
num_epochs = 30

# Step 1: Load and preprocess your image dataset
train_ds = keras.preprocessing.image_dataset_from_directory(
    'image',
    validation_split=0.2,
    subset='training',
    image_size=(img_height, img_width),
    batch_size=batch_size
)
val_ds = keras.preprocessing.image_dataset_from_directory(
    'validation',
    validation_split=0.2,
    subset='validation',
    image_size=(img_height, img_width),
    batch_size=batch_size
)

# Step 2: Configure the dataset for performance
AUTOTUNE = tf.data.AUTOTUNE
train_ds = train_ds.cache().shuffle(1000).prefetch(buffer_size=AUTOTUNE)
val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)

# Step 3: Create your model
model = keras.Sequential([
    layers.Rescaling(1.0 / 255),  # Normalize pixel values to [0, 1]
    layers.Conv2D(16, 3, padding='same', activation='relu'),
    layers.MaxPooling2D(),
    layers.Conv2D(32, 3, padding='same', activation='relu'),
    layers.MaxPooling2D(),
    layers.Conv2D(64, 3, padding='same', activation='relu'),
    layers.MaxPooling2D(),
    layers.Flatten(),
    layers.Dense(128, activation='relu'),
    layers.Dense(1, activation='sigmoid')  # Output layer with sigmoid activation for binary classification
])

# Step 4: Compile and train your model
model.compile(optimizer='adam',
              loss=tf.keras.losses.BinaryCrossentropy(),
              metrics=['accuracy'])

# Step 5: Create a ModelCheckpoint callback to save the model during training
checkpoint_callback = keras.callbacks.ModelCheckpoint(
    filepath='../datasets/model.h5',  # Specify the path to save the model
    save_best_only=True,
    monitor='val_loss',
    mode='min',
    verbose=1
)

# Step 6: Train your model with the ModelCheckpoint callback
model.fit(train_ds, validation_data=val_ds, epochs=num_epochs, callbacks=[checkpoint_callback])
