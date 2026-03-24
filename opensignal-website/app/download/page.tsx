'use client'

import { Download, Smartphone, Monitor, Copy, Check } from 'lucide-react'
import { useState } from 'react'
import Section from '@/components/Section'

interface DownloadOption {
  name: string
  icon: React.ReactNode
  description: string
  file: string
  url?: string
  size: string
  available?: boolean
}

const downloadOptions: DownloadOption[] = [
  {
    name: 'Android APK',
    icon: <Smartphone size={32} />,
    description: 'Compatible with Android 8.0+. Install directly on your phone.',
    file: 'OpenSignal-Android-v1.0.0.apk',
    url: '/downloads/OpenSignal-Android-v1.0.0.apk',
    size: '232 MB',
    available: true,
  },
  {
    name: 'Desktop App (Linux)',
    icon: <Monitor size={32} />,
    description: 'Debian/Ubuntu package for 64-bit Linux.',
    file: 'OpenSignal-Desktop-v1.0.0-amd64.deb',
    url: '/downloads/OpenSignal-Desktop-v1.0.0-amd64.deb',
    size: '195 MB',
    available: true,
  },
  {
    name: 'Desktop App (Windows)',
    icon: <Monitor size={32} />,
    description: 'Windows 10/11 64-bit installer (.msi).',
    file: 'OpenSignal-Desktop-v1.0.0-win64.msi',
    url: '/downloads/OpenSignal-Desktop-v1.0.0-win64.msi',
    size: '200 MB',
    available: true,
  },
]

function CopyButton({ text, label }: { text: string; label: string }) {
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    await navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <button
      onClick={handleCopy}
      className="flex items-center gap-2 px-3 py-2 rounded-lg bg-opensignal-gold/10 hover:bg-opensignal-gold/20 border border-opensignal-gold/30 text-opensignal-gold transition-all duration-200"
    >
      {copied ? (
        <>
          <Check size={16} />
          Copied!
        </>
      ) : (
        <>
          <Copy size={16} />
          Copy
        </>
      )}
    </button>
  )
}

export default function DownloadPage() {
  return (
    <div className="pt-20">
      <Section
        title="Download OpenSignal"
        subtitle="Get the latest version of OpenSignal for your platform"
      >
        <div className="max-w-4xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
            {downloadOptions.map((option, idx) => (
              <div
                key={idx}
                className="glass p-8 rounded-lg border-opensignal-gold/20 flex flex-col"
              >
                <div className="text-opensignal-gold mb-4">{option.icon}</div>
                <h3 className="text-2xl font-bold text-opensignal-text mb-2">
                  {option.name}
                </h3>
                <p className="text-opensignal-text-secondary mb-6 flex-1">
                  {option.description}
                </p>
                <div className="space-y-4">
                  <div className="flex items-center justify-between bg-opensignal-dark-tertiary px-4 py-3 rounded-lg">
                    <div>
                      <p className="text-opensignal-text-secondary text-sm">
                        Filename
                      </p>
                      <p className="text-opensignal-gold font-mono">
                        {option.file}
                      </p>
                    </div>
                    <CopyButton text={option.file} label="Copy filename" />
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-opensignal-text-secondary">
                      Size: {option.size}
                    </span>
                  </div>
                  {option.available && option.url ? (
                    <a
                      href={option.url}
                      download
                      className="button-gold w-full flex items-center justify-center gap-2"
                    >
                      <Download size={20} />
                      Download {option.name}
                    </a>
                  ) : (
                    <button
                      className="button-outline w-full flex items-center justify-center gap-2 cursor-not-allowed opacity-60"
                      disabled
                    >
                      <Download size={20} />
                      Build Pending
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </Section>

      {/* Installation Guide */}
      <Section
        title="Installation Guide"
        subtitle="Step-by-step instructions to get started"
        className="bg-opensignal-dark-secondary"
      >
        <div className="max-w-3xl mx-auto">
          <div className="space-y-8">
            {/* Android */}
            <div>
              <h3 className="text-2xl font-bold text-opensignal-text mb-4">
                Android Installation
              </h3>
              <ol className="space-y-4">
                {[
                  'Download the APK file from above',
                  'Open your file manager and navigate to Downloads',
                  'Tap the APK file to start installation',
                  'Grant permissions when prompted',
                  'Wait for installation to complete',
                  'Open OpenSignal and create your account',
                  'Start analyzing charts!',
                ].map((step, idx) => (
                  <li key={idx} className="flex gap-4">
                    <div className="flex-shrink-0 w-8 h-8 rounded-full bg-opensignal-gold/20 flex items-center justify-center border border-opensignal-gold/50">
                      <span className="text-opensignal-gold font-bold text-sm">
                        {idx + 1}
                      </span>
                    </div>
                    <div className="flex-1 pt-1">
                      <p className="text-opensignal-text">{step}</p>
                    </div>
                  </li>
                ))}
              </ol>
              <div className="mt-6 p-4 bg-opensignal-gold/5 border border-opensignal-gold/20 rounded-lg">
                <p className="text-opensignal-text-secondary text-sm">
                  <strong>Note:</strong> If you encounter "Unknown sources" warning, 
                  enable installation from unknown sources in your security settings.
                </p>
              </div>
            </div>

            {/* Desktop */}
            <div>
              <h3 className="text-2xl font-bold text-opensignal-text mb-4">
                Desktop Installation
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="glass p-6 rounded-lg border-opensignal-gold/20">
                  <h4 className="text-lg font-semibold text-opensignal-gold mb-4">
                    Linux (Debian/Ubuntu)
                  </h4>
                  <ol className="space-y-4">
                    {[
                      'Download the .deb package above',
                      'Open your terminal and navigate to Downloads',
                      'Install with sudo dpkg -i OpenSignal-Desktop-v1.0.0-amd64.deb',
                      'If prompted, run sudo apt -f install',
                      'Launch OpenSignal from your applications',
                      'Authenticate with Nostr or external signer',
                      'Ready to analyze!',
                    ].map((step, idx) => (
                      <li key={idx} className="flex gap-4">
                        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-opensignal-gold/20 flex items-center justify-center border border-opensignal-gold/50">
                          <span className="text-opensignal-gold font-bold text-sm">
                            {idx + 1}
                          </span>
                        </div>
                        <div className="flex-1 pt-1">
                          <p className="text-opensignal-text">{step}</p>
                        </div>
                      </li>
                    ))}
                  </ol>
                </div>
                <div className="glass p-6 rounded-lg border-opensignal-gold/20">
                  <h4 className="text-lg font-semibold text-opensignal-gold mb-4">
                    Windows 10/11
                  </h4>
                  <ol className="space-y-4">
                    {[
                      'Download the .msi installer above',
                      'Double-click to start the installer',
                      'Follow the setup wizard prompts',
                      'If Windows SmartScreen appears, click More info → Run anyway',
                      'Launch OpenSignal from the Start Menu',
                      'Authenticate with Nostr or external signer',
                      'Ready to analyze!',
                    ].map((step, idx) => (
                      <li key={idx} className="flex gap-4">
                        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-opensignal-gold/20 flex items-center justify-center border border-opensignal-gold/50">
                          <span className="text-opensignal-gold font-bold text-sm">
                            {idx + 1}
                          </span>
                        </div>
                        <div className="flex-1 pt-1">
                          <p className="text-opensignal-text">{step}</p>
                        </div>
                      </li>
                    ))}
                  </ol>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Section>

      {/* Requirements */}
      <Section
        title="System Requirements"
        subtitle="Make sure your device is compatible"
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
          {[
            {
              title: 'Android',
              reqs: ['Android 8.0 or higher', 'Minimum 2GB RAM', '50 MB storage space'],
            },
            {
              title: 'Desktop',
              reqs: ['Windows 10/11 64-bit or Debian/Ubuntu 64-bit', 'Intel i5+ or equivalent', '200 MB storage space'],
            },
            {
              title: 'Network',
              reqs: ['Stable internet connection', 'Access to Nostr relays', 'Camera for screenshots'],
            },
          ].map((section, idx) => (
            <div
              key={idx}
              className="glass p-6 rounded-lg border-opensignal-gold/20"
            >
              <h3 className="text-opensignal-gold font-semibold mb-4">
                {section.title}
              </h3>
              <ul className="space-y-3">
                {section.reqs.map((req, i) => (
                  <li key={i} className="flex items-start gap-3">
                    <span className="text-opensignal-gold mt-1">✓</span>
                    <span className="text-opensignal-text">{req}</span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </Section>

      {/* Support */}
      <section className="py-20 px-4 text-center">
        <div className="max-w-2xl mx-auto">
          <h2 className="text-3xl font-bold text-opensignal-text mb-6">
            Need Help?
          </h2>
          <p className="text-opensignal-text-secondary text-lg mb-8">
            Check our documentation or reach out to our support team
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button className="button-gold">
              View Documentation
            </button>
            <button className="button-outline">
              Contact Support
            </button>
          </div>
        </div>
      </section>
    </div>
  )
}
