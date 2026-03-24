'use client'

export function OpenSignalIconSVG({ size = 40, className = '' }: { size?: number; className?: string }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 200 200"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Outer circle - top left */}
      <path
        d="M 60 40 Q 40 60 40 100 Q 40 140 60 160"
        stroke="currentColor"
        strokeWidth="20"
        fill="none"
        strokeLinecap="round"
      />
      {/* Outer circle - bottom right */}
      <path
        d="M 140 40 Q 160 60 160 100 Q 160 140 140 160"
        stroke="currentColor"
        strokeWidth="20"
        fill="none"
        strokeLinecap="round"
      />
      {/* Candlestick 1 */}
      <rect x="75" y="110" width="12" height="40" fill="currentColor" />
      <line x1="81" y1="110" x2="81" y2="90" stroke="currentColor" strokeWidth="4" />
      <line x1="81" y1="150" x2="81" y2="155" stroke="currentColor" strokeWidth="4" />
      {/* Candlestick 2 */}
      <rect x="95" y="95" width="12" height="55" fill="currentColor" />
      <line x1="101" y1="95" x2="101" y2="70" stroke="currentColor" strokeWidth="4" />
      <line x1="101" y1="150" x2="101" y2="155" stroke="currentColor" strokeWidth="4" />
      {/* Candlestick 3 */}
      <rect x="115" y="105" width="12" height="45" fill="currentColor" />
      <line x1="121" y1="105" x2="121" y2="85" stroke="currentColor" strokeWidth="4" />
      <line x1="121" y1="150" x2="121" y2="155" stroke="currentColor" strokeWidth="4" />
    </svg>
  )
}

export function OpenSignalLogoDarkSVG({ className = '' }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 800 400"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Logo Icon */}
      <g>
        {/* Outer circle - top left */}
        <path
          d="M 120 80 Q 80 120 80 200 Q 80 280 120 320"
          stroke="#D4AF37"
          strokeWidth="40"
          fill="none"
          strokeLinecap="round"
        />
        {/* Outer circle - bottom right */}
        <path
          d="M 280 80 Q 320 120 320 200 Q 320 280 280 320"
          stroke="#D4AF37"
          strokeWidth="40"
          fill="none"
          strokeLinecap="round"
        />
        {/* Candlestick 1 */}
        <rect x="150" y="220" width="24" height="80" fill="#D4AF37" />
        <line x1="162" y1="220" x2="162" y2="180" stroke="#D4AF37" strokeWidth="8" />
        <line x1="162" y1="300" x2="162" y2="310" stroke="#D4AF37" strokeWidth="8" />
        {/* Candlestick 2 */}
        <rect x="190" y="190" width="24" height="110" fill="#D4AF37" />
        <line x1="202" y1="190" x2="202" y2="140" stroke="#D4AF37" strokeWidth="8" />
        <line x1="202" y1="300" x2="202" y2="310" stroke="#D4AF37" strokeWidth="8" />
        {/* Candlestick 3 */}
        <rect x="230" y="210" width="24" height="90" fill="#D4AF37" />
        <line x1="242" y1="210" x2="242" y2="170" stroke="#D4AF37" strokeWidth="8" />
        <line x1="242" y1="300" x2="242" y2="310" stroke="#D4AF37" strokeWidth="8" />
      </g>

      {/* Text */}
      <text
        x="400"
        y="240"
        fontFamily="Georgia, serif"
        fontSize="120"
        fontWeight="400"
        fill="#D4AF37"
        letterSpacing="4"
      >
        OpenSignal
      </text>

      {/* Tagline */}
      <text
        x="400"
        y="300"
        fontFamily="Georgia, serif"
        fontSize="48"
        fontWeight="400"
        fill="#D4AF37"
        letterSpacing="6"
        textAnchor="start"
      >
        AI TRADING COPILOT
      </text>
    </svg>
  )
}
