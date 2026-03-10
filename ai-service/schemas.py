from __future__ import annotations

from typing import List, Literal, Optional
from pydantic import BaseModel, Field


TrendDirection = Literal["BULLISH", "BEARISH", "SIDEWAYS"]
TradeSide = Literal["BUY", "SELL"]


class AnalyzeRequest(BaseModel):
    symbol: str = Field(default="BTCUSDT")
    timeframe: str = Field(default="H1")
    screenshot_url: Optional[str] = None
    screenshot_path: Optional[str] = None
    screenshot_base64: Optional[str] = None
    account_balance: float = 10_000.0
    risk_per_trade_percent: float = 1.0
    leverage: float = 3.0
    minimum_rr: float = 2.0
    context: str = ""


class PriceLevel(BaseModel):
    label: str
    price: float
    strength: float
    kind: Literal["SUPPORT", "RESISTANCE"]


class LiquiditySweep(BaseModel):
    side: TradeSide
    level: float
    confidence: float
    note: str


class StructureEvent(BaseModel):
    type: str
    direction: TrendDirection
    confidence: float
    description: str


class TechnicalAnalysis(BaseModel):
    current_price: float
    trend: TrendDirection
    trend_confidence: float
    support_resistance: List[PriceLevel]
    liquidity_sweeps: List[LiquiditySweep]
    structure_events: List[StructureEvent]
    summary: str


class FundamentalFactor(BaseModel):
    title: str
    impact: str
    bias: TrendDirection
    confidence: float
    details: str


class FundamentalAnalysis(BaseModel):
    summary: str
    overall_bias: TrendDirection
    score: float
    factors: List[FundamentalFactor]
    risk_flags: List[str]


class TradeOption(BaseModel):
    side: TradeSide
    entry: float
    stop_loss: float
    take_profit: float
    risk_reward: float
    position_size_units: float
    capital_at_risk: float
    confidence: float
    note: str


class RiskPlan(BaseModel):
    max_loss_per_trade: float
    max_total_portfolio_risk: float
    warnings: List[str]


class TradePlan(BaseModel):
    buy_options: List[TradeOption]
    sell_options: List[TradeOption]
    risk: RiskPlan


class AnalyzeResponse(BaseModel):
    symbol: str
    timeframe: str
    screenshot_hash: str
    technical: TechnicalAnalysis
    fundamental: FundamentalAnalysis
    trade_plan: TradePlan
    confidence: float
    published_event_id: Optional[str] = None
