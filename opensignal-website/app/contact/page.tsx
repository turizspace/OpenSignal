'use client'

import { Mail, Zap } from 'lucide-react'
import Section from '@/components/Section'
import CTAButton from '@/components/CTAButton'

export default function ContactPage() {
  return (
    <div className="pt-20">
      <Section
        title="Get in Touch"
        subtitle="We'd love to hear from you"
      >
        <div className="max-w-4xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Contact Form */}
            <div className="glass p-8 rounded-lg border-opensignal-gold/20">
              <h3 className="text-2xl font-bold text-opensignal-text mb-6">
                Send us a Message
              </h3>
              <form className="space-y-4">
                <div>
                  <label className="block text-opensignal-text mb-2 font-semibold">
                    Name
                  </label>
                  <input
                    type="text"
                    className="w-full px-4 py-2 rounded-lg bg-opensignal-dark-tertiary border border-opensignal-gold/20 text-opensignal-text placeholder-opensignal-text-secondary focus:outline-none focus:border-opensignal-gold transition-colors"
                    placeholder="Your name"
                  />
                </div>
                <div>
                  <label className="block text-opensignal-text mb-2 font-semibold">
                    Email
                  </label>
                  <input
                    type="email"
                    className="w-full px-4 py-2 rounded-lg bg-opensignal-dark-tertiary border border-opensignal-gold/20 text-opensignal-text placeholder-opensignal-text-secondary focus:outline-none focus:border-opensignal-gold transition-colors"
                    placeholder="your@email.com"
                  />
                </div>
                <div>
                  <label className="block text-opensignal-text mb-2 font-semibold">
                    Message
                  </label>
                  <textarea
                    className="w-full px-4 py-2 rounded-lg bg-opensignal-dark-tertiary border border-opensignal-gold/20 text-opensignal-text placeholder-opensignal-text-secondary focus:outline-none focus:border-opensignal-gold transition-colors resize-none"
                    rows={4}
                    placeholder="Your message..."
                  />
                </div>
                <button className="button-gold w-full">Send Message</button>
              </form>
            </div>

            {/* Contact Info */}
            <div className="space-y-6">
              <div className="glass p-6 rounded-lg border-opensignal-gold/20">
                <div className="flex items-start gap-4">
                  <Mail className="text-opensignal-gold mt-1 flex-shrink-0" size={24} />
                  <div>
                    <h4 className="text-opensignal-gold font-semibold mb-1">
                      Email
                    </h4>
                    <p className="text-opensignal-text-secondary">
                      support@opensignal.io
                    </p>
                  </div>
                </div>
              </div>

              <div className="glass p-6 rounded-lg border-opensignal-gold/20">
                <div className="flex items-start gap-4">
                  <Zap className="text-opensignal-gold mt-1 flex-shrink-0" size={24} />
                  <div>
                    <h4 className="text-opensignal-gold font-semibold mb-1">
                      Social
                    </h4>
                    <p className="text-opensignal-text-secondary">
                      @opensignal on Twitter
                    </p>
                  </div>
                </div>
              </div>

              <div className="glass p-6 rounded-lg border-opensignal-gold/20">
                <h4 className="text-opensignal-gold font-semibold mb-3">
                  Response Time
                </h4>
                <p className="text-opensignal-text-secondary">
                  We typically respond within 24 hours during business days.
                </p>
              </div>
            </div>
          </div>
        </div>
      </Section>

      {/* Quick Links */}
      <Section
        title="Other Ways to Connect"
        className="bg-opensignal-dark-secondary"
      >
        <div className="max-w-3xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            {
              title: 'Documentation',
              description: 'Read our comprehensive guides and API docs',
              link: '/resources',
            },
            {
              title: 'GitHub Repository',
              description: 'Check out our open-source code and contribute',
              link: 'https://github.com/turizspace/opensignal',
            },
            {
              title: 'Creator Profile',
              description: 'Follow @turizspace for updates and announcements',
              link: 'https://github.com/turizspace',
            },
          ].map((item, idx) => (
            <a
              key={idx}
              href={item.link}
              target={item.link.startsWith('http') ? '_blank' : undefined}
              rel={item.link.startsWith('http') ? 'noopener noreferrer' : undefined}
              className="glass p-6 rounded-lg border-opensignal-gold/20 hover:border-opensignal-gold/50 transition-all duration-300 cursor-pointer"
            >
              <h3 className="text-opensignal-gold font-semibold mb-2">
                {item.title}
              </h3>
              <p className="text-opensignal-text-secondary text-sm">
                {item.description}
              </p>
            </a>
          ))}
        </div>
      </Section>
    </div>
  )
}
