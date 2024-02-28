import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator

# Define the path to your dataset directory
dataset_dir = 'trainingData/dataset'

# Define the image parameters
image_height = 224
image_width = 224
batch_size = 32

# Create an ImageDataGenerator and specify data augmentation if needed
datagen = ImageDataGenerator(
    rescale=1.0 / 255,  # Normalize pixel values between 0 and 1
    validation_split=0.2  # Split the data into training and validation sets
)

# Load and prepare the training data
train_generator = datagen.flow_from_directory(
    dataset_dir,
    target_size=(image_height, image_width),
    batch_size=batch_size,
    class_mode='categorical',
    subset='training'
)

# Load and prepare the validation data
val_generator = datagen.flow_from_directory(
    dataset_dir,
    target_size=(image_height, image_width),
    batch_size=batch_size,
    class_mode='categorical',
    subset='validation'
)

# Define the model architecture
model = tf.keras.models.Sequential([
    tf.keras.applications.MobileNetV2(input_shape=(image_height, image_width, 3), include_top=False),
    tf.keras.layers.GlobalAveragePooling2D(),
    tf.keras.layers.Dense(train_generator.num_classes, activation='softmax')
])

# Compile the model
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

# Train the model
model.fit(
    train_generator,
    steps_per_epoch=train_generator.samples // batch_size,
    validation_data=val_generator,
    validation_steps=val_generator.samples // batch_size,
    epochs=1
)

# Save the model to a directory
model.save('k-model.h5')

