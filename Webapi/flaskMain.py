from flask import Flask, request
import tensorflow as tf
from tensorflow import keras

app = Flask(__name__)

# Images get scaled to this:
img_height = 256
img_width = 256

class_names = ['dream', 'nondream']

model = keras.models.load_model('../datasets/trained.h5')


@app.route('/pissai')
def pissai():
    queryImgUrl = request.args.get('imgUrl')
    image_url = tf.keras.utils.get_file('Court', origin=queryImgUrl)
    img = keras.utils.load_img(
        image_url, target_size=(img_height, img_width)
    )

    img_array = keras.utils.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)

    score = model.predict(img_array)[0]
    print(
        "This is {}, {:.2f} percent confidence."
        .format(class_names[int(score)], 100 * score)
    )
    return "This is {}, {:.2f} percent confidence.".format(class_names[int(score)], 100 * score)
