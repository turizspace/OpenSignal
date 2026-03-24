'use client'

import { Download, Github, Zap, Sparkles, Shield, Brain } from 'lucide-react'
import Section from '@/components/Section'
import FeatureCard from '@/components/FeatureCard'
import CTAButton from '@/components/CTAButton'
import { OpenSignalIconSVG } from '@/components/OpenSignalLogo'

const highlights = [
  {
    icon: <Brain size={28} />,
    title: 'AI-Powered',
    description: 'Vision-based chart analysis with deep learning models',
  },
  {
    icon: <Zap size={28} />,
    title: 'Real-Time Signals',
    description: 'Instant trading recommendations with risk management',
  },
  {
    icon: <Shield size={28} />,
    title: 'Secure & Private',
    description: 'Nostr-native, decentralized, never share your keys',
  },
]

export default function HomePage() {
  return (
    <div className="pt-20">
      {/* Hero Section */}
      <section className="hero-ambient min-h-screen flex items-center justify-center px-4 py-20">
        <div className="max-w-4xl mx-auto text-center">
          <div className="mb-8 flex justify-center">
            <div className="text-opensignal-gold">
              <OpenSignalIconSVG size={120} />
            </div>
          </div>

          <div className="inline-block mb-6">
            <span className="px-4 py-2 rounded-full bg-opensignal-gold/10 border border-opensignal-gold/30 text-opensignal-gold text-sm font-semibold">
              ✨ AI Trading Copilot
            </span>
          </div>

          <h1 className="text-5xl sm:text-7xl font-bold mb-6 leading-tight">
            Chart Analysis,{' '}
            <span className="gradient-gold-text">Powered by AI</span>
          </h1>

          <p className="text-xl sm:text-2xl text-opensignal-text-secondary mb-8 max-w-2xl mx-auto">
            Detect candlestick patterns, liquidity sweeps, and market structure breaks 
            in seconds. Publish trading signals to Nostr with Nostr-native authentication.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-16">
            <CTAButton href="/download" className="flex items-center justify-center gap-2">
              <Download size={20} />
              Download APK
            </CTAButton>
            <a
              href="https://github.com/turizspace/opensignal"
              target="_blank"
              rel="noopener noreferrer"
              className="button-outline flex items-center justify-center gap-2"
            >
              <Github size={20} />
              View on GitHub
            </a>
          </div>

          {/* Feature Highlights */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-20">
            {highlights.map((item, idx) => (
              <div key={idx} className="flex flex-col items-center">
                <div className="mb-4 text-opensignal-gold">{item.icon}</div>
                <h3 className="text-lg font-semibold text-opensignal-text mb-2">
                  {item.title}
                </h3>
                <p className="text-opensignal-text-secondary">{item.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <Section
        title="Comprehensive Trading Analysis"
        subtitle="Everything you need for professional chart analysis"
        className="bg-opensignal-dark-secondary"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <FeatureCard
            icon={<Zap size={28} />}
            title="Multi-Stage Vision Pipeline"
            description="Candle detection, liquidity sweep identification, structure recognition, and trend classification in a single pipeline."
          />
          <FeatureCard
            icon={<Sparkles size={28} />}
            title="Risk-Managed Trading"
            description="Automated position sizing, buy/sell options generation, and comprehensive risk management recommendations."
          />
          <FeatureCard
            icon={<Shield size={28} />}
            title="Nostr Native"
            description="Decentralized signal publishing, relay management, and nsec key authentication for maximum security."
          />
          <FeatureCard
            icon={<Download size={28} />}
            title="Seamless Integration"
            description="Screenshot analysis, Blossom NIP-96 uploads, and instant signal generation in one workflow."
          />
        </div>
      </Section>

      {/* Tech Stack Section */}
      <Section
        title="Built With Modern Tech"
        subtitle="Leveraging the latest in AI, cryptography, and cross-platform development"
      >
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[
            { name: 'Kotlin Multiplatform', desc: 'Shared code for Android & Desktop' },
            { name: 'PyTorch + OpenCV', desc: 'Advanced vision model training' },
            { name: 'ONNX Runtime', desc: 'Efficient on-device inference' },
            { name: 'Nostr Protocol', desc: 'Decentralized authentication' },
          ].map((tech, idx) => (
            <div key={idx} className="glass p-6 rounded-lg border-opensignal-gold/20">
              <p className="text-opensignal-gold font-semibold mb-1">{tech.name}</p>
              <p className="text-opensignal-text-secondary text-sm">{tech.desc}</p>
            </div>
          ))}
        </div>
      </Section>

      {/* CTA Section */}
      <section className="py-20 px-4 bg-gradient-dark">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-4xl font-bold text-opensignal-text mb-6">
            Ready to Trade Smarter?
          </h2>
          <p className="text-opensignal-text-secondary text-lg mb-8">
            Download OpenSignal today and start analyzing charts with AI-powered precision.
          </p>
          <CTAButton href="/download" className="mb-4">
            Download APK Now
          </CTAButton>
        </div>
      </section>
    </div>
  )
}
