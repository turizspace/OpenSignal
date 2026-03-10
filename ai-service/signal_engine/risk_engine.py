from __future__ import annotations

from dataclasses import dataclass
from typing import List


@dataclass
class RiskInput:
    current_price: float
    levels: list[dict]
    trend_confidence: float
    account_balance: float
    risk_per_trade_percent: float
    leverage: float
    minimum_rr: float


def _build_option(
    side: str,
    entry: float,
    stop_loss: float,
    take_profit: float,
    confidence: float,
    risk_capital: float,
) -> dict:
    stop_distance = max(abs(entry - stop_loss), entry * 0.0005)
    reward_distance = max(abs(take_profit - entry), entry * 0.0005)
    rr = reward_distance / stop_distance
    position_units = risk_capital / stop_distance

    return {
        "side": side,
        "entry": round(entry, 4),
        "stop_loss": round(stop_loss, 4),
        "take_profit": round(take_profit, 4),
        "risk_reward": round(rr, 4),
        "position_size_units": round(position_units, 4),
        "capital_at_risk": round(risk_capital, 4),
        "confidence": round(confidence, 3),
        "note": "Generated from screenshot structure and liquidity model outputs",
    }


def generate_trade_plan(inp: RiskInput) -> dict:
    support_levels = [l for l in inp.levels if l["kind"] == "SUPPORT"]
    resistance_levels = [l for l in inp.levels if l["kind"] == "RESISTANCE"]

    support = support_levels[0]["price"] if support_levels else inp.current_price * 0.992
    resistance = resistance_levels[0]["price"] if resistance_levels else inp.current_price * 1.008

    risk_capital = inp.account_balance * (inp.risk_per_trade_percent / 100.0)

    buy_primary = _build_option(
        side="BUY",
        entry=inp.current_price,
        stop_loss=min(support, inp.current_price * 0.995),
        take_profit=max(resistance, inp.current_price + abs(inp.current_price - support) * inp.minimum_rr),
        confidence=inp.trend_confidence,
        risk_capital=risk_capital,
    )
    buy_pullback = _build_option(
        side="BUY",
        entry=support,
        stop_loss=support * 0.997,
        take_profit=max(resistance, support + abs(support * 0.003) * (inp.minimum_rr + 0.5)),
        confidence=max(0.35, inp.trend_confidence * 0.92),
        risk_capital=risk_capital,
    )

    sell_primary = _build_option(
        side="SELL",
        entry=inp.current_price,
        stop_loss=max(resistance, inp.current_price * 1.005),
        take_profit=min(support, inp.current_price - abs(resistance - inp.current_price) * inp.minimum_rr),
        confidence=inp.trend_confidence,
        risk_capital=risk_capital,
    )
    sell_rejection = _build_option(
        side="SELL",
        entry=resistance,
        stop_loss=resistance * 1.003,
        take_profit=min(support, resistance - abs(resistance * 0.003) * (inp.minimum_rr + 0.5)),
        confidence=max(0.35, inp.trend_confidence * 0.92),
        risk_capital=risk_capital,
    )

    options = [buy_primary, buy_pullback, sell_primary, sell_rejection]
    warnings: List[str] = []
    for option in options:
        if option["risk_reward"] < inp.minimum_rr:
            warnings.append(f"{option['side']} option at {option['entry']} below min RR")

    if inp.risk_per_trade_percent > 2.0:
        warnings.append("Risk per trade above 2% is aggressive")

    if inp.leverage > 5.0:
        warnings.append("Leverage above 5x increases liquidation risk")

    return {
        "buy_options": [buy_primary, buy_pullback],
        "sell_options": [sell_primary, sell_rejection],
        "risk": {
            "max_loss_per_trade": round(risk_capital, 4),
            "max_total_portfolio_risk": round(risk_capital * 3, 4),
            "warnings": warnings,
        },
    }
