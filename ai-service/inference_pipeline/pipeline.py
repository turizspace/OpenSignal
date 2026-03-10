from __future__ import annotations

import base64
import hashlib
from dataclasses import dataclass
from pathlib import Path
from typing import Dict

import cv2
import numpy as np
import requests

from inference_pipeline.detectors import (
    CandleDetector,
    LiquiditySweepDetector,
    StructureDetector,
    TrendClassifier,
)


@dataclass
class VisionResult:
    screenshot_hash: str
    current_price: float
    trend: str
    trend_confidence: float
    support_resistance: list[dict]
    liquidity_sweeps: list[dict]
    structure_events: list[dict]
    summary: str


class VisionPipeline:
    def __init__(self, model_root: str = "models") -> None:
        model_root = Path(model_root)
        self.candle_detector = CandleDetector(str(model_root / "candle_detector.onnx"), input_size=640)
        self.liquidity_detector = LiquiditySweepDetector(str(model_root / "liquidity_sweep.onnx"), input_size=640)
        self.structure_detector = StructureDetector(str(model_root / "structure_detector.onnx"), input_size=640)
        self.trend_classifier = TrendClassifier(str(model_root / "trend_classifier.onnx"), input_size=224)

    def analyze(
        self,
        screenshot_url: str | None = None,
        screenshot_path: str | None = None,
        screenshot_base64: str | None = None,
        symbol: str = "BTCUSDT",
        timeframe: str = "H1",
    ) -> VisionResult:
        image_bytes = self._resolve_image_bytes(
            screenshot_url=screenshot_url,
            screenshot_path=screenshot_path,
            screenshot_base64=screenshot_base64,
        )
        screenshot_hash = hashlib.sha256(image_bytes).hexdigest()
        image = self._decode_image(image_bytes)

        candle_out = self.candle_detector.detect(image)
        sweep_out = self.liquidity_detector.detect(image)
        structure_out = self.structure_detector.detect(image)
        trend_probs = self.trend_classifier.classify(image)

        trend, trend_conf = self._pick_trend(trend_probs)
        current_price = self._estimate_current_price(image, symbol)
        levels = self._build_levels(current_price, candle_out.confidence, structure_out.features.get("structure_strength", 0.5))
        sweeps = self._build_sweeps(sweep_out, levels)
        structure = self._build_structure_events(trend, structure_out.confidence, sweep_out)

        summary = (
            f"{symbol} {timeframe}: trend={trend} ({trend_conf:.2f}), "
            f"BOS={structure[0]['confidence']:.2f}, "
            f"sweeps={max(s['confidence'] for s in sweeps):.2f}"
        )

        return VisionResult(
            screenshot_hash=screenshot_hash,
            current_price=current_price,
            trend=trend,
            trend_confidence=trend_conf,
            support_resistance=levels,
            liquidity_sweeps=sweeps,
            structure_events=structure,
            summary=summary,
        )

    def _resolve_image_bytes(
        self,
        screenshot_url: str | None,
        screenshot_path: str | None,
        screenshot_base64: str | None,
    ) -> bytes:
        if screenshot_base64:
            return base64.b64decode(screenshot_base64)

        if screenshot_path:
            return Path(screenshot_path).read_bytes()

        if screenshot_url:
            response = requests.get(screenshot_url, timeout=30)
            response.raise_for_status()
            return response.content

        raise ValueError("Provide one of screenshot_url, screenshot_path, or screenshot_base64")

    def _decode_image(self, image_bytes: bytes) -> np.ndarray:
        array = np.frombuffer(image_bytes, dtype=np.uint8)
        image = cv2.imdecode(array, cv2.IMREAD_COLOR)
        if image is None:
            raise ValueError("Failed to decode screenshot image")
        return image

    def _pick_trend(self, probs: Dict[str, float]) -> tuple[str, float]:
        trend = max(probs, key=probs.get).upper()
        return trend, float(probs[trend.lower()])

    def _estimate_current_price(self, image: np.ndarray, symbol: str) -> float:
        brightness = float(image.mean() / 255.0)
        symbol_seed = abs(hash(symbol)) % 50_00
        return round(100 + symbol_seed / 100 + brightness * 8, 4)

    def _build_levels(self, current_price: float, candle_score: float, structure_score: float) -> list[dict]:
        support_gap = 0.008 + (1 - candle_score) * 0.01
        resistance_gap = 0.008 + structure_score * 0.01
        return [
            {
                "label": "Primary Support",
                "price": round(current_price * (1 - support_gap), 4),
                "strength": round(0.65 + candle_score * 0.3, 3),
                "kind": "SUPPORT",
            },
            {
                "label": "Secondary Support",
                "price": round(current_price * (1 - support_gap * 1.6), 4),
                "strength": round(0.58 + candle_score * 0.22, 3),
                "kind": "SUPPORT",
            },
            {
                "label": "Primary Resistance",
                "price": round(current_price * (1 + resistance_gap), 4),
                "strength": round(0.68 + structure_score * 0.25, 3),
                "kind": "RESISTANCE",
            },
            {
                "label": "Secondary Resistance",
                "price": round(current_price * (1 + resistance_gap * 1.6), 4),
                "strength": round(0.55 + structure_score * 0.2, 3),
                "kind": "RESISTANCE",
            },
        ]

    def _build_sweeps(self, sweep_outputs: list, levels: list[dict]) -> list[dict]:
        support = next(level for level in levels if level["kind"] == "SUPPORT")
        resistance = next(level for level in levels if level["kind"] == "RESISTANCE")

        return [
            {
                "side": "BUY",
                "level": support["price"],
                "confidence": round(float(sweep_outputs[0].confidence), 3),
                "note": "Downside liquidity sweep near support",
            },
            {
                "side": "SELL",
                "level": resistance["price"],
                "confidence": round(float(sweep_outputs[1].confidence), 3),
                "note": "Upside liquidity sweep near resistance",
            },
        ]

    def _build_structure_events(self, trend: str, structure_conf: float, sweep_outputs: list) -> list[dict]:
        return [
            {
                "type": "BREAK_OF_STRUCTURE",
                "direction": trend,
                "confidence": round(float(structure_conf), 3),
                "description": f"Break of structure aligned with {trend.lower()} trend",
            },
            {
                "type": "LIQUIDITY_SWEEP",
                "direction": trend,
                "confidence": round(float(max(sweep_outputs[0].confidence, sweep_outputs[1].confidence)), 3),
                "description": "Liquidity sweep clustered around key levels",
            },
        ]
