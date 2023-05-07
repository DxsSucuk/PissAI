import matplotlib.pyplot as plt
import numpy as np
import os
import PIL
import tensorflow as tf

from tensorflow import keras
from keras import layers
from keras.models import Sequential
import pathlib

# Images get scaled to this:
img_height = 256
img_width = 256

class_names = ['dream', 'nondream']

model = keras.models.load_model('saved/trained.h5')

img = keras.utils.load_img(
    './validation/nondream/24754615-1-2-1.jpeg', target_size=(img_height, img_width)
)

plt.imshow(img)

img_array = keras.utils.img_to_array(img)
img_array = tf.expand_dims(img_array, 0)

score = model.predict(img_array)[0]

if score == 0:
    plt.xlabel('Dream')
elif score == 1:
    plt.xlabel('Not Dream')

plt.show()
print(
    "This is {}, {:.2f} percent confidence."
    .format(class_names[int(score)], 100 * score)
)
