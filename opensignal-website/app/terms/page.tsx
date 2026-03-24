'use client'

import Section from '@/components/Section'

export default function TermsPage() {
  return (
    <div className="pt-20">
      <Section
        title="Terms of Service"
        subtitle="Last updated: March 2026"
      >
        <div className="max-w-3xl mx-auto space-y-8">
          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              1. Agreement to Terms
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed">
              By accessing and using the OpenSignal application and website, you accept and agree to be bound by the terms and provision of this agreement. If you do not agree to abide by the above, please do not use this service.
            </p>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              2. License and Use Rights
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed mb-4">
              OpenSignal grants you a limited, non-exclusive, non-transferable, revocable license to use the application and website for personal, non-commercial purposes. You are not permitted to:
            </p>
            <ul className="space-y-2 text-opensignal-text-secondary ml-4">
              <li>• Reproduce, modify, or distribute the content</li>
              <li>• Use the application for commercial purposes</li>
              <li>• Attempt to reverse engineer or decompile the software</li>
              <li>• Use the service to harass or harm others</li>
            </ul>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              3. Disclaimer of Warranties
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed">
              OpenSignal is provided &quot;AS IS&quot; without warranty of any kind, express or implied. We do not warrant that the application will be uninterrupted, error-free, or free of harmful components. Trading signals are provided for informational purposes only and should not be considered financial advice.
            </p>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              4. Limitation of Liability
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed">
              In no event shall OpenSignal be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your use of or inability to use the application or website.
            </p>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              5. User Responsibility
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed">
              You are responsible for maintaining the confidentiality of your private keys and authentication credentials. OpenSignal is not responsible for any loss resulting from unauthorized use of your account.
            </p>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              6. Changes to Terms
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed">
              OpenSignal reserves the right to modify these terms at any time. Continued use of the application following any changes constitutes acceptance of those changes.
            </p>
          </div>

          <div className="glass p-8 rounded-lg border-opensignal-gold/20">
            <h3 className="text-2xl font-bold text-opensignal-text mb-4">
              7. Contact
            </h3>
            <p className="text-opensignal-text-secondary leading-relaxed">
              For questions about these Terms of Service, please contact us at support@opensignal.io
            </p>
          </div>
        </div>
      </Section>
    </div>
  )
}
