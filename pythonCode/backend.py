import cv2
import numpy as np
import math
from flask import Flask, request, jsonify
import HandTrackingModule as htm
from cvzone.ClassificationModule import Classifier
from flask_cors import CORS  # Import the CORS module

app = Flask(__name__)
CORS(app)  # Add this line to enable CORS for your app

# Initialize hand detector and classifiers for different models
detector = htm.HandDetector(maxHands=1)
alphabet_classifier = Classifier("models/alphabets/keras_model.h5", "models/alphabets/labels.txt")
words_classifier = Classifier("models/words/keras_model.h5", "models/words/labels.txt")
numbers_classifier = Classifier("models/numbers/keras_model.h5", "models/numbers/labels.txt")

offset = 20
imgSize = 300

# Read labels from the text files for each model
with open("models/alphabets/labels.txt") as f:
    alphabet_labels = f.read().splitlines()

with open("models/words/labels.txt") as f:
    words_labels = f.read().splitlines()

with open("models/numbers/labels.txt") as f:
    numbers_labels = f.read().splitlines()

@app.route('/predict', methods=['POST'])
def predict():
    image = request.files['image']
    image_array = np.frombuffer(image.read(), np.uint8)
    img = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

    hands, _ = detector.findHands(img)
    if hands:
        hand = hands[0]
        x, y, w, h = hand['bbox']

        imgWhite = np.ones((imgSize, imgSize, 3), np.uint8) * 255
        imgCrop = img[y - offset:y + h + offset, x - offset:x + w + offset]

        imgCropShape = imgCrop.shape

        aspectRatio = h / w

        if aspectRatio > 1:
            k = imgSize / h
            wCal = math.ceil(k * w)
            imgResize = cv2.resize(imgCrop, (wCal, imgSize))
            imgResizeShape = imgResize.shape
            wGap = math.ceil((imgSize - wCal) / 2)
            imgWhite[:, wGap:wCal + wGap] = imgResize
        else:
            k = imgSize / w
            hCal = math.ceil(k * h)
            imgResize = cv2.resize(imgCrop, (imgSize, hCal))
            imgResizeShape = imgResize.shape
            hGap = math.ceil((imgSize - hCal) / 2)
            imgWhite[hGap:hCal + hGap, :] = imgResize

        # Get the query parameter to determine which model to use
        detection_type = request.args.get('type')

        # Convert the image to HSV
        hsv = cv2.cvtColor(imgWhite, cv2.COLOR_BGR2HSV)
        # Create a mask using the current HSV range
        lower_bound = np.array([116, 147, 222])
        upper_bound = np.array([144, 255, 255])
        mask = cv2.inRange(hsv, lower_bound, upper_bound)
        # Convert the binary image to a color image (grayscale-like with 3 channels)
        binary_image_color = cv2.cvtColor(mask, cv2.COLOR_GRAY2BGR)

         # You can save the images here if needed
        #cv2.imwrite("image.png", imgWhite)
        #cv2.imwrite("binary_image_color.png", binary_image_color)

        if detection_type == 'alphabets':
            # Predict using alphabet model
            prediction, index = alphabet_classifier.getPrediction(binary_image_color, draw=False)
            return jsonify({'predicted_class': alphabet_labels[index]})
        elif detection_type == 'words':
            # Predict using words model
            prediction, index = words_classifier.getPrediction(binary_image_color, draw=False)
            return jsonify({'predicted_class': words_labels[index]})
        elif detection_type == 'numbers':
            # Predict using numbers model
            prediction, index = numbers_classifier.getPrediction(binary_image_color, draw=False)
            return jsonify({'predicted_class': numbers_labels[index]})
        else:
            return jsonify({'predicted_class': 'Invalid detection type'})

    else:
        return jsonify({'predicted_class': 'No hands detected'})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
