import onnxruntime as ort

model_path = "app/src/main/assets/multicroponnx/mobilevit_multicrop.onnx"
session = ort.InferenceSession(model_path)
input_tensor = session.get_inputs()[0]
print("Input Name:", input_tensor.name)
print("Input Shape:", input_tensor.shape)
print("Input Type:", input_tensor.type)
