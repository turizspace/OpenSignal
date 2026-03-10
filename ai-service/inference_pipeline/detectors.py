from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List

import cv2
import numpy as np
import onnxruntime as ort


@dataclass
class DetectorOutput:
    confidence: float
    features: Dict[str, float]


class BaseDetector:
    def __init__(self, model_path: str, input_size: int = 640) -> None:
        self.model_path = Path(model_path)
        self.input_size = input_size

        if not self.model_path.exists():
            raise FileNotFoundError(
                f"ONNX model not found: {self.model_path}. "
                "Provide actual *.onnx model artifacts before running inference."
            )

        self._session = ort.InferenceSession(
            str(self.model_path),
            providers=["CPUExecutionProvider"],
        )
        self._input_name = self._session.get_inputs()[0].name

    def _resize(self, image: np.ndarray) -> np.ndarray:
        return cv2.resize(image, (self.input_size, self.input_size), interpolation=cv2.INTER_LINEAR)

    def _to_chw_tensor(self, image: np.ndarray) -> np.ndarray:
        x = image.astype(np.float32) / 255.0
        x = np.transpose(x, (2, 0, 1))
        x = np.expand_dims(x, axis=0)
        return x

    def predict(self, image: np.ndarray) -> DetectorOutput:
        x = self._to_chw_tensor(self._resize(image))
        raw_outputs = self._session.run(None, {self._input_name: x})
        flat = np.concatenate([np.asarray(out).reshape(-1) for out in raw_outputs]).astype(np.float32)

        if flat.size == 0:
            raise RuntimeError(f"Empty ONNX output for model {self.model_path.name}")

        conf = float(np.clip(np.mean(np.abs(flat)), 0.0, 1.0))
        return DetectorOutput(
            confidence=conf,
            features={
                "mean_abs": conf,
                "p90_abs": float(np.percentile(np.abs(flat), 90)),
            },
        )


class CandleDetector(BaseDetector):
    def detect(self, image: np.ndarray) -> DetectorOutput:
        out = self.predict(image)
        out.features["candle_density"] = float(np.clip(out.features["p90_abs"], 0.0, 1.0))
        return out


class LiquiditySweepDetector(BaseDetector):
    def detect(self, image: np.ndarray) -> List[DetectorOutput]:
        out = self.predict(image)
        score = out.confidence
        return [
            DetectorOutput(
                confidence=min(1.0, score * 0.9 + 0.08),
                features={"side": 1.0, "sweep_level_pct": 0.18},
            ),
            DetectorOutput(
                confidence=min(1.0, (1.0 - score) * 0.5 + 0.35),
                features={"side": -1.0, "sweep_level_pct": 0.82},
            ),
        ]


class StructureDetector(BaseDetector):
    def detect(self, image: np.ndarray) -> DetectorOutput:
        out = self.predict(image)
        edge = cv2.Canny(image, 80, 160)
        out.features["structure_strength"] = float((edge > 0).sum() / edge.size)
        return out


class TrendClassifier(BaseDetector):
    def classify(self, image: np.ndarray) -> Dict[str, float]:
        out = self.predict(image)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        height = gray.shape[0]
        top = float(gray[: height // 2, :].mean())
        bottom = float(gray[height // 2 :, :].mean())
        drift = (bottom - top) / 255.0

        bullish = float(np.clip(0.33 + drift + out.confidence * 0.25, 0.0, 1.0))
        bearish = float(np.clip(0.33 - drift + (1.0 - out.confidence) * 0.15, 0.0, 1.0))
        sideways = float(np.clip(1.0 - abs(drift) * 1.2, 0.0, 1.0))

        total = bullish + bearish + sideways
        return {
            "bullish": bullish / total,
            "bearish": bearish / total,
            "sideways": sideways / total,
        }
