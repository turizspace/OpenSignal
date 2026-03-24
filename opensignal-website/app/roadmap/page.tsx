'use client'

import Section from '@/components/Section'

export default function RoadmapPage() {
  const roadmapItems = [
    {
      quarter: 'Q1 2026',
      status: 'Completed',
      items: [
        'Candle & liquidity detection models',
        'Nostr integration & publishing',
        'Android app launch',
        'Basic risk management engine',
      ],
    },
    {
      quarter: 'Q2 2026',
      status: 'In Progress',
      items: [
        'Desktop app launch',
        'Advanced technical indicators',
        'Multi-pair analysis',
        'Relay management UI',
      ],
    },
    {
      quarter: 'Q3 2026',
      status: 'Planned',
      items: [
        'Mobile web version',
        'API suite release',
        'Backtesting engine',
        'Community model sharing',
      ],
    },
    {
      quarter: 'Q4 2026',
      status: 'Planned',
      items: [
        'Automated signal generation',
        'Zap integration (Lightning)',
        'Advanced charting tools',
        'Enterprise licensing',
      ],
    },
  ]

  return (
    <div className="pt-20">
      <Section
        title="OpenSignal Roadmap"
        subtitle="Our vision for the future of AI-powered trading"
      >
        <div className="max-w-4xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {roadmapItems.map((item, idx) => (
              <div key={idx} className="glass p-6 rounded-lg border-opensignal-gold/20">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-xl font-bold text-opensignal-text">
                    {item.quarter}
                  </h3>
                  <span
                    className={`text-sm font-semibold px-3 py-1 rounded-full ${
                      item.status === 'Completed'
                        ? 'bg-green-500/20 text-green-400'
                        : item.status === 'In Progress'
                        ? 'bg-opensignal-gold/20 text-opensignal-gold'
                        : 'bg-opensignal-text-secondary/20 text-opensignal-text-secondary'
                    }`}
                  >
                    {item.status}
                  </span>
                </div>
                <ul className="space-y-2">
                  {item.items.map((subitem, i) => (
                    <li
                      key={i}
                      className="flex items-start gap-2 text-opensignal-text-secondary"
                    >
                      <span className="text-opensignal-gold mt-1">→</span>
                      <span>{subitem}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>
      </Section>
    </div>
  )
}
