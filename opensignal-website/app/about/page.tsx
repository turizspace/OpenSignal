'use client'

import { Users, Rocket, Target, Github } from 'lucide-react'
import Section from '@/components/Section'
import CTAButton from '@/components/CTAButton'

export default function AboutPage() {
  return (
    <div className="pt-20">
      <Section
        title="About OpenSignal"
        subtitle="Building the future of AI-powered trading analysis"
      >
        <div className="max-w-3xl mx-auto">
          <div className="glass p-8 rounded-lg border-opensignal-gold/20 mb-12">
            <p className="text-opensignal-text text-lg leading-relaxed mb-6">
              OpenSignal is a Kotlin Multiplatform trading copilot that leverages 
              advanced AI and computer vision to analyze trading charts in real-time. 
              Born from the need for decentralized, trustless trading analysis, 
              OpenSignal integrates Nostr protocol for signal publishing and 
              authentication.
            </p>
            <p className="text-opensignal-text text-lg leading-relaxed">
              Our mission is to democratize professional-grade technical and fundamental 
              analysis by making it accessible, automated, and decentralized. Whether 
              you’re a day trader, swing trader, or long-term investor, OpenSignal 
              provides AI-powered insights to inform your trading decisions.
            </p>
          </div>
        </div>
      </Section>

      {/* Values Section */}
      <Section
        title="Our Values"
        subtitle="What drives OpenSignal forward"
        className="bg-opensignal-dark-secondary"
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto">
          {[
            {
              icon: <Target size={32} />,
              title: 'Accuracy',
              description:
                'We obsess over model accuracy and validation. Every prediction is backed by rigorous testing and real-world data.',
            },
            {
              icon: <Rocket size={32} />,
              title: 'Innovation',
              description:
                'We push the boundaries of what\'s possible with vision-based chart analysis and AI-driven trading.',
            },
            {
              icon: <Users size={32} />,
              title: 'Decentralization',
              description:
                'Your keys, your data. We believe in Nostr-native authentication and censorship-resistant signal publishing.',
            },
          ].map((value, idx) => (
            <div
              key={idx}
              className="glass p-8 rounded-lg border-opensignal-gold/20 text-center"
            >
              <div className="text-opensignal-gold mb-4 flex justify-center">
                {value.icon}
              </div>
              <h3 className="text-xl font-semibold text-opensignal-text mb-3">
                {value.title}
              </h3>
              <p className="text-opensignal-text-secondary">{value.description}</p>
            </div>
          ))}
        </div>
      </Section>

      {/* Team Section */}
      <Section
        title="Technology Stack"
        subtitle="Built with cutting-edge technologies"
      >
        <div className="max-w-4xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
            <div className="glass p-8 rounded-lg border-opensignal-gold/20">
              <h3 className="text-opensignal-gold text-xl font-semibold mb-4">
                AI & Machine Learning
              </h3>
              <ul className="space-y-2 text-opensignal-text-secondary">
                <li>• PyTorch for model training</li>
                <li>• YOLOv5/ResNet50 architectures</li>
                <li>• ONNX for cross-platform inference</li>
                <li>• OpenCV for image processing</li>
              </ul>
            </div>
            <div className="glass p-8 rounded-lg border-opensignal-gold/20">
              <h3 className="text-opensignal-gold text-xl font-semibold mb-4">
                Application Development
              </h3>
              <ul className="space-y-2 text-opensignal-text-secondary">
                <li>• Kotlin Multiplatform (KMP)</li>
                <li>• Jetpack Compose UI</li>
                <li>• FastAPI backend</li>
                <li>• Nostr protocol integration</li>
              </ul>
            </div>
            <div className="glass p-8 rounded-lg border-opensignal-gold/20">
              <h3 className="text-opensignal-gold text-xl font-semibold mb-4">
                Data & Protocols
              </h3>
              <ul className="space-y-2 text-opensignal-text-secondary">
                <li>• Nostr protocol (NIPs)</li>
                <li>• NIP-96 Blossom uploads</li>
                <li>• Relay management</li>
                <li>• Event signing & verification</li>
              </ul>
            </div>
            <div className="glass p-8 rounded-lg border-opensignal-gold/20">
              <h3 className="text-opensignal-gold text-xl font-semibold mb-4">
                Security & Privacy
              </h3>
              <ul className="space-y-2 text-opensignal-text-secondary">
                <li>• Private key management</li>
                <li>• External signer support</li>
                <li>• Hardware wallet integration</li>
                <li>• Decentralized authentication</li>
              </ul>
            </div>
          </div>
        </div>
      </Section>

      {/* Open Source Section */}
      <Section
        title="Open Source & Community"
        subtitle="Built by the community, for the community"
        className="bg-opensignal-dark-secondary"
      >
        <div className="max-w-3xl mx-auto">
          <div className="glass p-8 rounded-lg border-opensignal-gold/20 mb-8">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              Fully Open Source
            </h3>
            <p className="text-opensignal-text-secondary mb-6">
              OpenSignal is a free and open-source project. All code, models, and training pipelines are available on GitHub. 
              We believe in transparency, community-driven development, and making advanced trading tools accessible to everyone.
            </p>
            <a
              href="https://github.com/turizspace/opensignal"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 button-gold"
            >
              <Github size={20} />
              View Full Repository
            </a>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              Creator
            </h3>
            <p className="text-opensignal-text mb-2">
              <strong>@turizspace</strong>
            </p>
            <p className="text-opensignal-text-secondary mb-6">
              Passionate about decentralized finance, trading automation, and open-source software. 
              Building tools that empower traders with AI and blockchain technology.
            </p>
            <a
              href="https://github.com/turizspace"
              target="_blank"
              rel="noopener noreferrer"
              className="text-opensignal-gold hover:text-opensignal-gold-light transition-colors"
            >
              View Creator Profile →
            </a>
          </div>
        </div>
      </Section>
      <section className="py-20 px-4 text-center">
        <div className="max-w-2xl mx-auto">
          <h2 className="text-4xl font-bold text-opensignal-text mb-6">
            Join the Trading Revolution
          </h2>
          <p className="text-opensignal-text-secondary text-lg mb-8">
            Be part of the decentralized future of trading analysis. Download OpenSignal today.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <CTAButton href="/download">
              Get Started Now
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
        </div>
      </section>
    </div>
  )
}
