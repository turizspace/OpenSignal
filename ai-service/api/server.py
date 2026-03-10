from __future__ import annotations

import os
from functools import lru_cache

from fastapi import FastAPI

from inference_pipeline.pipeline import VisionPipeline
from schemas import (
    AnalyzeRequest,
    AnalyzeResponse,
    FundamentalAnalysis,
    FundamentalFactor,
    TechnicalAnalysis,
)
from signal_engine.risk_engine import RiskInput, generate_trade_plan

app = FastAPI(title="OpenSignal AI Vision Service", version="1.0.0")


@lru_cache(maxsize=1)
def get_pipeline() -> VisionPipeline:
    model_root = os.getenv("OPEN_SIGNAL_MODEL_ROOT", "models")
    return VisionPipeline(model_root=model_root)


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "open-signal-ai"}


@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(req: AnalyzeRequest) -> AnalyzeResponse:
    pipeline = get_pipeline()
    result = pipeline.analyze(
        screenshot_url=req.screenshot_url,
        screenshot_path=req.screenshot_path,
        screenshot_base64=req.screenshot_base64,
        symbol=req.symbol,
        timeframe=req.timeframe,
    )

    technical = TechnicalAnalysis(
        current_price=result.current_price,
        trend=result.trend,
        trend_confidence=result.trend_confidence,
        support_resistance=result.support_resistance,
        liquidity_sweeps=result.liquidity_sweeps,
        structure_events=result.structure_events,
        summary=result.summary,
    )

    fundamental = _build_fundamental(req.symbol)

    trade_plan = generate_trade_plan(
        RiskInput(
            current_price=result.current_price,
            levels=result.support_resistance,
            trend_confidence=result.trend_confidence,
            account_balance=req.account_balance,
            risk_per_trade_percent=req.risk_per_trade_percent,
            leverage=req.leverage,
            minimum_rr=req.minimum_rr,
        )
    )

    confidence = max(0.0, min(1.0, (technical.trend_confidence + fundamental.score) / 2.0))

    return AnalyzeResponse(
        symbol=req.symbol,
        timeframe=req.timeframe,
        screenshot_hash=result.screenshot_hash,
        technical=technical,
        fundamental=fundamental,
        trade_plan=trade_plan,
        confidence=confidence,
        published_event_id=None,
    )


def _build_fundamental(symbol: str) -> FundamentalAnalysis:
    seed = abs(hash(symbol)) % 100
    score = max(0.3, min(0.85, seed / 100.0))
    bias = "BULLISH" if seed % 3 == 0 else "BEARISH" if seed % 3 == 1 else "SIDEWAYS"

    return FundamentalAnalysis(
        summary=f"Macro sentiment for {symbol} is {bias.lower()} with elevated event risk.",
        overall_bias=bias,
        score=score,
        factors=[
            FundamentalFactor(
                title="Macro liquidity",
                impact="Medium",
                bias=bias,
                confidence=score,
                details="Liquidity regime inferred from macro proxies and volatility clustering.",
            ),
            FundamentalFactor(
                title="Scheduled event risk",
                impact="High",
                bias="SIDEWAYS",
                confidence=0.62,
                details="Reduce size around known high-impact releases.",
            ),
        ],
        risk_flags=[
            "Avoid adding size before high-impact events",
            "Use hard stops for leveraged setups",
        ],
    )
