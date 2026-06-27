import onnx

model = onnx.load("app/src/main/assets/multicroponnx/mobilevit_multicrop.onnx")

print("Metadata properties:")
for prop in model.metadata_props:
    print(f"{prop.key}: {prop.value}")

print("Doc string:", model.doc_string)
