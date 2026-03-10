from __future__ import annotations

from pathlib import Path
import random

import cv2
import numpy as np

ROOT = Path("dataset")


def _draw_candle_chart(width: int = 640, height: int = 640) -> np.ndarray:
    image = np.zeros((height, width, 3), dtype=np.uint8)
    image[:] = (16, 24, 32)

    price = height // 2
    for x in range(30, width - 30, 20):
        drift = random.randint(-24, 24)
        open_price = np.clip(price, 40, height - 40)
        close_price = np.clip(price + drift, 40, height - 40)
        high = min(open_price, close_price) - random.randint(5, 20)
        low = max(open_price, close_price) + random.randint(5, 20)

        color = (70, 220, 120) if close_price < open_price else (90, 120, 245)
        cv2.line(image, (x, int(high)), (x, int(low)), color, 2)
        top = int(min(open_price, close_price))
        bottom = int(max(open_price, close_price))
        cv2.rectangle(image, (x - 5, top), (x + 5, bottom), color, -1)

        price = close_price

    return image


def _write_yolo_label(label_path: Path, cls: int, x: float, y: float, w: float, h: float) -> None:
    label_path.write_text(f"{cls} {x:.6f} {y:.6f} {w:.6f} {h:.6f}\n")


def _generate_split(dataset_name: str, count: int, cls: int) -> None:
    image_dir = ROOT / dataset_name / "images" / "train"
    label_dir = ROOT / dataset_name / "labels" / "train"
    val_image_dir = ROOT / dataset_name / "images" / "val"
    val_label_dir = ROOT / dataset_name / "labels" / "val"

    for directory in [image_dir, label_dir, val_image_dir, val_label_dir]:
        directory.mkdir(parents=True, exist_ok=True)

    for idx in range(count):
        image = _draw_candle_chart()
        is_val = idx % 5 == 0
        out_image_dir = val_image_dir if is_val else image_dir
        out_label_dir = val_label_dir if is_val else label_dir

        name = f"{dataset_name}_{idx:06d}"
        image_path = out_image_dir / f"{name}.png"
        label_path = out_label_dir / f"{name}.txt"

        cv2.imwrite(str(image_path), image)

        cx = random.uniform(0.3, 0.7)
        cy = random.uniform(0.3, 0.7)
        bw = random.uniform(0.15, 0.4)
        bh = random.uniform(0.1, 0.35)
        _write_yolo_label(label_path, cls, cx, cy, bw, bh)


def generate_dataset(count_per_dataset: int = 2000) -> None:
    _generate_split("candle", count_per_dataset, cls=0)
    _generate_split("liquidity", count_per_dataset, cls=0)
    _generate_split("structure", count_per_dataset, cls=0)


def generate_trend_classifier_dataset(count: int = 3000) -> None:
    root = ROOT / "trend_classifier"
    for cls in ["bullish", "bearish", "sideways"]:
        (root / cls).mkdir(parents=True, exist_ok=True)

    for idx in range(count):
        cls = ["bullish", "bearish", "sideways"][idx % 3]
        image = _draw_candle_chart()

        if cls == "bullish":
            image = cv2.convertScaleAbs(image, alpha=1.05, beta=20)
        elif cls == "bearish":
            image = cv2.convertScaleAbs(image, alpha=0.95, beta=-10)
        else:
            image = cv2.GaussianBlur(image, (7, 7), sigmaX=0.0)

        out = root / cls / f"{cls}_{idx:06d}.png"
        cv2.imwrite(str(out), image)


if __name__ == "__main__":
    generate_dataset()
    generate_trend_classifier_dataset()
