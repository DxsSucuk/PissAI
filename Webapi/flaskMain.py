import urllib

from flask import Flask, request
import tensorflow as tf
import random
import string
import numpy as np
import PIL
from tensorflow import keras

app = Flask(__name__)

# Images get scaled to this:
img_height = 256
img_width = 256

class_names = ['dream', 'nondream']

model = keras.models.load_model('../datasets/trained.h5')

def get_random_string(length):
    # choose from all lowercase letter
    letters = string.ascii_letters
    result_str = ''.join(random.choice(letters) for i in range(length))
    return result_str
@app.route('/pissai')
def pissai():
    queryImgUrl = request.args.get('imgUrl')

    if queryImgUrl is None:
        return "This bitch empty!"

    image_url = tf.keras.utils.get_file(get_random_string(5), origin=queryImgUrl)

    img = keras.utils.load_img(
        image_url, target_size=(img_height, img_width)
    )

    img_array = keras.utils.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)

    predictions = model.predict(img_array)
    score = predictions[0]
    scoreSoft = tf.nn.softmax(predictions[0])

    formattedString = "This is {}, {:.2f} percent confidence.".format(class_names[int(score)], 100 * np.max(scoreSoft))

    print(formattedString)

    return formattedString


if __name__ == "__main__":
    app.run()