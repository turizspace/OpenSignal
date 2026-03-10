# OpenSignal

OpenSignal is a Kotlin Multiplatform + Compose trading copilot scaffold with Nostr-native publishing, Blossom NIP-96 uploads, and an AI chart analysis pipeline.

## What this scaffold now includes

- KMP modules for shared domain logic, charts, Nostr, Blossom, and AI inference hooks.
- Android and Desktop Compose app shells.
- Dual auth paths:
  - Nostr `nsec` login.
  - External signer login.
- Screenshot upload flow into Blossom/NIP-96 client abstraction.
- AI analysis contracts and pipeline output for:
  - trend,
  - liquidity sweeps,
  - break of structure,
  - support/resistance.
- Technical + fundamental analysis output.
- Buy and sell trade option generation with risk management sizing/warnings.
- Nostr event builder + signal publishing abstraction.
- Python AI service scaffold (FastAPI) with multi-stage vision pipeline.
- Python training scaffold for YOLOv8/PyTorch/OpenCV/ONNX workflows.

## Repository architecture

```text
opensignal
├── androidApp                # Android Compose shell
├── desktopApp                # Desktop Compose shell
├── shared                    # Domain models, use-cases, risk engine, settings
├── nostr                     # Auth/signing, event builder, relay manager, publisher
├── blossom                   # NIP-96 client, media hash, upload service
├── charts                    # Compose chart + overlays + trade markers
├── ai-inference              # KMP inference contracts and ONNX pipeline hooks
├── ai-service                # FastAPI AI service and signal engine
├── ai-training               # Model training/export scripts
└── dataset                   # Synthetic dataset generation + YAML configs
```

## Core Kotlin flow

```text
Screenshot bytes
  -> Blossom NIP-96 upload
  -> Vision analyzer (candle/liquidity/structure/trend)
  -> Fundamental analyzer
  -> Risk engine (buy/sell options + sizing)
  -> Trade signal JSON
  -> Nostr event publish
```

## AI service flow

```text
Screenshot
  -> Candle detector
  -> Liquidity sweep detector
  -> Structure detector
  -> Trend classifier
  -> Technical summary + fundamental stub
  -> Risk-managed trade plan
```

## Running the AI service

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python inference_pipeline.py
```

Before running, place real ONNX model files as described in `models/README.md`:

- `models/candle_detector.onnx`
- `models/liquidity_sweep.onnx`
- `models/structure_detector.onnx`
- `models/trend_classifier.onnx`

Service endpoints:

- `GET /health`
- `POST /analyze`

Example payload: `ai-service/examples/analyze_request.json`

## Training scripts

```bash
cd ai-training
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python candle_detector/train.py
python liquidity_model/train.py
python structure_model/train.py
python trend_classifier/train.py
python export_onnx.py
python sync_models.py
```

## Notes

- This is a production-grade scaffold with concrete module boundaries and implementation hooks.
- ONNX loading is strict now (no fallback heuristics): invalid/missing model files fail fast.
- Use `./gradlew build` with the bundled Gradle wrapper (8.10.2).
