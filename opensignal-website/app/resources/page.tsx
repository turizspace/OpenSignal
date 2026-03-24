'use client'

import { FileText, Code, BookOpen, Github } from 'lucide-react'
import Section from '@/components/Section'

const resources = [
  {
    icon: <BookOpen size={32} />,
    title: 'Getting Started Guide',
    description: 'Step-by-step guide to install, authenticate, and start analyzing charts.',
    href: '#',
  },
  {
    icon: <Code size={32} />,
    title: 'API Documentation',
    description: 'Complete API reference for the FastAPI backend and analysis pipeline.',
    href: '#',
  },
  {
    icon: <FileText size={32} />,
    title: 'Model Documentation',
    description: 'Details on our ONNX models, training data, and accuracy metrics.',
    href: '#',
  },
  {
    icon: <Github size={32} />,
    title: 'Source Code',
    description: 'Open-source repository with full access to training and inference code.',
    href: 'https://github.com/turizspace/opensignal',
  },
]

export default function ResourcesPage() {
  return (
    <div className="pt-20">
      <Section
        title="Resources & Documentation"
        subtitle="Everything you need to understand and use OpenSignal effectively"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
          {resources.map((resource, idx) => (
            <a
              key={idx}
              href={resource.href}
              className="glass p-8 rounded-xl border-opensignal-gold/20 hover:border-opensignal-gold/50 hover:shadow-lg hover:shadow-opensignal-gold/10 transition-all duration-300 cursor-pointer group"
            >
              <div className="text-opensignal-gold mb-4 group-hover:scale-110 transition-transform duration-300">
                {resource.icon}
              </div>
              <h3 className="text-xl font-semibold text-opensignal-text mb-2">
                {resource.title}
              </h3>
              <p className="text-opensignal-text-secondary">{resource.description}</p>
              <div className="mt-4 text-opensignal-gold text-sm font-semibold">
                Learn More →
              </div>
            </a>
          ))}
        </div>
      </Section>

      {/* FAQ Section */}
      <Section
        title="Frequently Asked Questions"
        subtitle="Get answers to common questions"
        className="bg-opensignal-dark-secondary"
      >
        <div className="max-w-3xl mx-auto space-y-6">
          {[
            {
              q: 'What models are used for chart analysis?',
              a:
                'We use custom YOLOv5 and ResNet models for candlestick detection, liquidity sweep identification, and structure recognition. All models are trained on synthetic datasets and exported to ONNX format for efficient inference.',
            },
            {
              q: 'Is my data safe with OpenSignal?',
              a:
                'Yes! OpenSignal is decentralized and Nostr-native. Your private key (nsec) never leaves your device. Screenshots are processed locally, and signals are published directly to Nostr relays.',
            },
            {
              q: 'How accurate are the AI predictions?',
              a:
                'Our models achieve 90%+ accuracy on synthetic test sets. Real-world performance depends on chart quality, timeframe, and market conditions. We recommend using signals alongside your own analysis.',
            },
            {
              q: 'Can I use this on desktop?',
              a:
                'Yes! OpenSignal is built with Kotlin Multiplatform, so we provide both Android and Desktop versions. The desktop app has the same features and UI.',
            },
            {
              q: 'Is there an API for integrations?',
              a:
                'The FastAPI backend can be deployed separately. See our API documentation for endpoints, authentication, and example requests.',
            },
            {
              q: 'How do I publish signals to Nostr?',
              a:
                'After analyzing a chart, tap "Publish Signal" to generate a Nostr event and send it to your configured relays. You need a Nostr account (public key) to publish.',
            },
          ].map((item, idx) => (
            <div
              key={idx}
              className="glass p-6 rounded-lg border-opensignal-gold/20"
            >
              <h3 className="text-lg font-semibold text-opensignal-gold mb-3">
                {item.q}
              </h3>
              <p className="text-opensignal-text-secondary">{item.a}</p>
            </div>
          ))}
        </div>
      </Section>

      {/* Technical Stack */}
      <Section
        title="Technical Stack"
        subtitle="Built on proven, modern technologies"
      >
        <div className="max-w-4xl mx-auto">
          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <div className="grid grid-cols-2 md:grid-cols-3 gap-8">
              <div>
                <h4 className="text-opensignal-gold font-semibold mb-2">
                  Frontend
                </h4>
                <ul className="text-opensignal-text-secondary text-sm space-y-1">
                  <li>Kotlin Multiplatform</li>
                  <li>Jetpack Compose</li>
                  <li>Android + Desktop</li>
                </ul>
              </div>
              <div>
                <h4 className="text-opensignal-gold font-semibold mb-2">
                  Backend
                </h4>
                <ul className="text-opensignal-text-secondary text-sm space-y-1">
                  <li>FastAPI (Python)</li>
                  <li>ONNX Runtime</li>
                  <li>OpenCV</li>
                </ul>
              </div>
              <div>
                <h4 className="text-opensignal-gold font-semibold mb-2">
                  Protocols
                </h4>
                <ul className="text-opensignal-text-secondary text-sm space-y-1">
                  <li>Nostr Protocol</li>
                  <li>NIP-96 (Blossom)</li>
                  <li>NIP-07 (Auth)</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </Section>
    </div>
  )
}
