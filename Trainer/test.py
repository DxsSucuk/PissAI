import urllib.request
import tensorflow as tf
from tensorflow import keras

# Images get scaled to this:
img_height = 256
img_width = 256

class_names = ['nondream', 'dream']

# Step 1: Download the image from the URL
image_url = 'https://images.pexels.com/photos/1831234/pexels-photo-1831234.jpeg'
image_path = 'test.jpg'  # Specify the path to save the downloaded image
urllib.request.urlretrieve(image_url, image_path)

# Step 2: Load and preprocess the downloaded image
img = keras.preprocessing.image.load_img(image_path, target_size=(img_height, img_width))
img_array = keras.preprocessing.image.img_to_array(img)
img_array = tf.expand_dims(img_array, 0)  # Create a batch

# Step 3: Load the trained model
model = keras.models.load_model('../datasets/model.h5')

# Step 4: Make predictions on the test image
predictions = model.predict(img_array)
score = predictions[0]
print(score)
class_index = int(score >= 0.75)  # Assuming threshold of 0.5 for binary classification
class_label = class_names[class_index]  # Replace with your class labels

print("Predicted class label:", class_label)
