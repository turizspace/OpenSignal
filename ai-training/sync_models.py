from __future__ import annotations

from pathlib import Path
import shutil


ROOT = Path(__file__).resolve().parents[1]
OUTPUTS = ROOT / "ai-training" / "outputs"
MODELS = ROOT / "models"


CANDIDATES = {
    "candle_detector.onnx": [
        OUTPUTS / "candle_detector" / "run" / "weights" / "best.onnx",
        OUTPUTS / "candle_detector" / "run" / "best.onnx",
    ],
    "liquidity_sweep.onnx": [
        OUTPUTS / "liquidity_sweep" / "run" / "weights" / "best.onnx",
        OUTPUTS / "liquidity_sweep" / "run" / "best.onnx",
    ],
    "structure_detector.onnx": [
        OUTPUTS / "structure_detector" / "run" / "weights" / "best.onnx",
        OUTPUTS / "structure_detector" / "run" / "best.onnx",
    ],
    "trend_classifier.onnx": [
        OUTPUTS / "trend_classifier" / "trend_classifier.onnx",
    ],
}


def main() -> None:
    MODELS.mkdir(parents=True, exist_ok=True)
    copied = 0

    for target_name, candidates in CANDIDATES.items():
        source = next((path for path in candidates if path.exists()), None)
        if source is None:
            print(f"missing: {target_name} (no candidate artifacts found)")
            continue

        destination = MODELS / target_name
        shutil.copy2(source, destination)
        copied += 1
        print(f"copied: {source} -> {destination}")

    if copied == 0:
        raise SystemExit("No model artifacts copied")


if __name__ == "__main__":
    main()
