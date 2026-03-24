'use client'

import {
  BarChart3,
  Brain,
  Zap,
  Share2,
  Lock,
  TrendingUp,
  AlertCircle,
  Gauge,
} from 'lucide-react'
import Section from '@/components/Section'
import FeatureCard from '@/components/FeatureCard'

const features = [
  {
    icon: <Brain size={32} />,
    title: 'AI Chart Analysis',
    description:
      'Advanced vision pipeline detects candlestick patterns, liquidity sweeps, and structural breaks in real-time.',
  },
  {
    icon: <TrendingUp size={32} />,
    title: 'Trend Classification',
    description:
      'AI-powered trend detection classifies market direction and identifies key support/resistance levels.',
  },
  {
    icon: <BarChart3 size={32} />,
    title: 'Technical Analysis',
    description:
      'Comprehensive technical indicators and overlays for in-depth market analysis and signal generation.',
  },
  {
    icon: <Zap size={32} />,
    title: 'Nostr Integration',
    description:
      'Publish trading signals directly to Nostr relays for decentralized, censorship-resistant distribution.',
  },
  {
    icon: <Gauge size={32} />,
    title: 'Risk Management',
    description:
      'Intelligent position sizing and risk-managed trade options with buy/sell recommendations.',
  },
  {
    icon: <Lock size={32} />,
    title: 'Secure Authentication',
    description:
      'Dual auth paths: Nostr nsec key login or external signer/hardware wallet support for maximum security.',
  },
  {
    icon: <Share2 size={32} />,
    title: 'Media Upload',
    description:
      'Blossom NIP-96 integration for secure screenshot uploads and analysis workflow automation.',
  },
  {
    icon: <AlertCircle size={32} />,
    title: 'Real-time Signals',
    description:
      'Get instant trading signals with fundamental and technical analysis summaries for quick decision-making.',
  },
]

export default function FeaturesPage() {
  return (
    <div className="pt-20">
      <Section
        title="Powerful Trading Features"
        subtitle="Everything you need for intelligent chart analysis and signal generation"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, idx) => (
            <FeatureCard
              key={idx}
              icon={feature.icon}
              title={feature.title}
              description={feature.description}
            />
          ))}
        </div>
      </Section>

      {/* Deep Dive Section */}
      <Section
        title="How It Works"
        subtitle="A seamless pipeline from screenshot to trading signal"
        className="bg-opensignal-dark-secondary"
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            {
              step: 1,
              title: 'Screenshot Capture',
              description:
                'Take a screenshot of any trading chart from your favorite platform.',
            },
            {
              step: 2,
              title: 'AI Analysis',
              description:
                'Our vision pipeline analyzes candlestick patterns, liquidity, and market structure.',
            },
            {
              step: 3,
              title: 'Signal Publishing',
              description:
                'Receive trade recommendations and publish signals to Nostr relays instantly.',
            },
          ].map((item) => (
            <div key={item.step} className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-opensignal-gold/10 border-2 border-opensignal-gold mb-4">
                <span className="text-opensignal-gold font-bold text-2xl">
                  {item.step}
                </span>
              </div>
              <h3 className="text-xl font-semibold text-opensignal-text mb-2">
                {item.title}
              </h3>
              <p className="text-opensignal-text-secondary">{item.description}</p>
            </div>
          ))}
        </div>
      </Section>
    </div>
  )
}
