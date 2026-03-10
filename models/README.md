# ONNX Models

Default models have been downloaded for you:

- `candle_detector.onnx` → YOLOv5n (generic COCO detector)
- `liquidity_sweep.onnx` → YOLOv5n (placeholder; needs fine-tuning)
- `structure_detector.onnx` → YOLOv5n (placeholder; needs fine-tuning)
- `trend_classifier.onnx` → ResNet50-v2 (generic classifier; needs fine-tuning)

These are **baseline** models only. For production accuracy, fine‑tune on labeled chart data
and replace the files with your trained ONNX exports.

If you want to replace them, use these exact names:

- `candle_detector.onnx`
- `liquidity_sweep.onnx`
- `structure_detector.onnx`
- `trend_classifier.onnx`

Both the Kotlin `ai-inference` module and Python `ai-service` load these files directly using ONNX Runtime.

Model loading is strict and will fail if files are missing or invalid.

Android packaging:
- Copies are bundled under `androidApp/src/main/assets/models` and are extracted to
  the app's internal storage on first use.

Sources (for reference):
- YOLOv5n ONNX: `https://github.com/yakhyo/yolov5-onnx-inference/releases/tag/v0.0.1`
- ResNet50-v2 ONNX: `https://github.com/onnx/models/tree/main/validated/vision/classification/resnet`
