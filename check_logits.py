import onnxruntime as ort
import numpy as np

model_path = "app/src/main/assets/multicroponnx/mobilevit_multicrop.onnx"
session = ort.InferenceSession(model_path)
input_name = session.get_inputs()[0].name

# Create a random input tensor mapped roughly to normalized image values
input_tensor = np.random.uniform(-2.0, 2.0, (1, 3, 224, 224)).astype(np.float32)

outputs = session.run(None, {input_name: input_tensor})[0]

print("Outputs shape:", outputs.shape)
print("Outputs data:")
print(outputs[0])
print("Sum of outputs:", np.sum(outputs[0]))
