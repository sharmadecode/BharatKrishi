import onnxruntime as ort
import numpy as np
from PIL import Image

def get_transform(img):
    img = img.convert("RGB")
    # Resize
    img = img.resize((224, 224), Image.BILINEAR)
    # ToTensor
    img_array = np.array(img).astype(np.float32) / 255.0
    # Normalize
    mean = np.array([0.485, 0.456, 0.406]).astype(np.float32)
    std = np.array([0.229, 0.224, 0.225]).astype(np.float32)
    img_array = (img_array - mean) / std
    # HWC to CHW
    img_array = np.transpose(img_array, (2, 0, 1))
    return np.expand_dims(img_array, axis=0)

model_path = "app/src/main/assets/multicroponnx/mobilevit_multicrop.onnx"
session = ort.InferenceSession(model_path)
input_name = session.get_inputs()[0].name

# Create a sample red image
img = Image.new('RGB', (256, 256), color = (255, 0, 0))
input_tensor = get_transform(img)

outputs = session.run(None, {input_name: input_tensor})[0]
probs = np.exp(outputs) / np.sum(np.exp(outputs), axis=1, keepdims=True)

classLabels = [
    "Chili Bacterial Spot", "Chili Cercospora", "Chili Curl Virus", "Chili Healthy",
    "Chili Nutrition Deficiency", "Chili White Spot", "Potato Bacteria", "Potato Fungi",
    "Potato Healthy", "Potato Pest", "Potato Phytopthora", "Potato Virus",
    "Pumpkin Bacterial Leaf Spot", "Pumpkin Downy Mildew", "Pumpkin Healthy",
    "Pumpkin Mosaic", "Pumpkin Powdery Mildew", "Soybean Frog Eye", "Soybean Healthy",
    "Soybean Mosaic", "Soybean Pest", "Soybean Rust", "Soybean Septoria"
]

print("Model output shape:", outputs.shape)
max_index = np.argmax(probs[0])
print("Max Index:", max_index)
print("Logit at max:", outputs[0][max_index])
print("Max Confidence:", probs[0][max_index])
print("Predicted Class:", classLabels[max_index])

for i, p in enumerate(probs[0]):
    if p > 0.05:
        print(f"Class {i} ({classLabels[i]}): {p*100:.2f}%")
