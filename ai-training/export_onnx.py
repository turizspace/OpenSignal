from __future__ import annotations

from pathlib import Path

from ultralytics import YOLO


def export(model_path: str, output_dir: str = "outputs") -> None:
    model = YOLO(model_path)
    exported = model.export(format="onnx", dynamic=True, simplify=True)
    out = Path(output_dir)
    out.mkdir(parents=True, exist_ok=True)
    print(f"Exported: {exported}")


if __name__ == "__main__":
    export("outputs/candle_detector/run/weights/best.pt")
    export("outputs/liquidity_sweep/run/weights/best.pt")
    export("outputs/structure_detector/run/weights/best.pt")
