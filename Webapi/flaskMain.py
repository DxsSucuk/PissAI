import urllib

from flask import Flask, request, jsonify
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


def downloadFile(name, url):
    print(url)
    opener = urllib.request.build_opener()
    opener.addheaders = [('User-agent',
                          'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36')]
    urllib.request.install_opener(opener)
    urllib.request.urlretrieve(url, name + ".jpg")
    return name + ".jpg"


@app.route('/pissai')
def pissai():
    queryImgUrl = request.args.get('imgUrl')

    if queryImgUrl is None:
        return jsonify({'class': "nonedream", 'pecs': str(1)})

    img = keras.utils.load_img(
        downloadFile(get_random_string(5), queryImgUrl), target_size=(img_height, img_width)
    )

    img_array = keras.utils.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)

    predictions = model.predict(img_array)
    score = predictions[0]
    scoreSoft = tf.nn.softmax(predictions[0])

    detectedClass = class_names[int(score)]
    funnyPercs = np.max(scoreSoft)

    formattedString = "This is {}, {:.2f} percent confidence.".format(detectedClass, funnyPercs)

    print(formattedString)

    return jsonify({'class': detectedClass, 'pecs': str(funnyPercs)})


if __name__ == "__main__":
    app.run()
