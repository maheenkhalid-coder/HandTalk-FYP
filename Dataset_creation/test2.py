import tensorflow as tf
from tensorflow.keras.preprocessing.image import load_img, img_to_array
import numpy as np

# Load the trained model
model = tf.keras.models.load_model('k-model.h5')

# Define the path to the test image
test_image_path = 'trainingData/test/4.jpg'

# Define the image parameters
image_height = 224
image_width = 224

test_image = load_img(test_image_path, target_size=(image_height, image_width))
test_image = img_to_array(test_image)
test_image = np.expand_dims(test_image, axis=0)
test_image = test_image / 255.0  # Normalize pixel values between 0 and 1

# Make predictions on the test image
predictions = model.predict(test_image)
predicted_class_index = np.argmax(predictions[0])

# Define the class labels
class_labels = ['A', 'B', 'C','D']  # Replace with your class labels

# Get the predicted class label
predicted_class_label = class_labels[predicted_class_index]

# Print the predicted class label
print('Predicted class:', predicted_class_label)
