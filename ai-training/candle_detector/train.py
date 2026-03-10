from __future__ import annotations

from ultralytics import YOLO

from common.training_utils import ensure_output_dir, load_yaml


def train(config_path: str = "configs/candle_detector.yaml") -> None:
    cfg = load_yaml(config_path)
    output = ensure_output_dir("candle_detector")

    model = YOLO(cfg["model"])
    model.train(
        data=cfg["data"],
        epochs=cfg["epochs"],
        imgsz=cfg["imgsz"],
        batch=cfg["batch"],
        project=str(output),
        name="run",
    )
    model.export(format="onnx", dynamic=True, simplify=True)


if __name__ == "__main__":
    train()
