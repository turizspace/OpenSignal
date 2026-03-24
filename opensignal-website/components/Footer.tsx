'use client'

import Link from 'next/link'
import Image from 'next/image'
import { Github, Twitter } from 'lucide-react'

export default function Footer() {
  const currentYear = new Date().getFullYear()

  const footerLinks = {
    product: [
      { label: 'Features', href: '/features' },
      { label: 'Download', href: '/download' },
      { label: 'Roadmap', href: '/roadmap' },
    ],
    resources: [
      { label: 'Documentation', href: '/resources' },
      { label: 'API Docs', href: '/api-docs' },
      { label: 'GitHub', href: 'https://github.com' },
    ],
    company: [
      { label: 'About', href: '/about' },
      { label: 'Contact', href: '/contact' },
      { label: 'Terms', href: '/terms' },
    ],
  }

  return (
    <footer className="bg-opensignal-dark-secondary border-t border-opensignal-gold/10 mt-20">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-12">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Image
                src="/images/logo-icon.png"
                alt="OpenSignal"
                width={32}
                height={32}
                className="rounded-md"
              />
              <span className="gradient-gold-text font-bold text-lg">OpenSignal</span>
            </div>
            <p className="text-opensignal-text-secondary text-sm">
              AI-powered trading copilot for chart analysis and signal generation.
            </p>
          </div>

          {/* Product */}
          <div>
            <h3 className="text-opensignal-gold font-semibold mb-4">Product</h3>
            <div className="space-y-2">
              {footerLinks.product.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="block text-opensignal-text-secondary hover:text-opensignal-gold transition-colors text-sm"
                >
                  {link.label}
                </Link>
              ))}
            </div>
          </div>

          {/* Resources */}
          <div>
            <h3 className="text-opensignal-gold font-semibold mb-4">Resources</h3>
            <div className="space-y-2">
              {footerLinks.resources.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="block text-opensignal-text-secondary hover:text-opensignal-gold transition-colors text-sm"
                >
                  {link.label}
                </Link>
              ))}
              <a
                href="https://github.com/turizspace/opensignal"
                target="_blank"
                rel="noopener noreferrer"
                className="block text-opensignal-text-secondary hover:text-opensignal-gold transition-colors text-sm"
              >
                GitHub Repository
              </a>
            </div>
          </div>

          {/* Company */}
          <div>
            <h3 className="text-opensignal-gold font-semibold mb-4">Company</h3>
            <div className="space-y-2">
              {footerLinks.company.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="block text-opensignal-text-secondary hover:text-opensignal-gold transition-colors text-sm"
                >
                  {link.label}
                </Link>
              ))}
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="border-t border-opensignal-gold/10 my-8"></div>

        {/* Bottom */}
        <div className="flex flex-col md:flex-row justify-between items-center">
          <p className="text-opensignal-text-secondary text-sm">
            © {currentYear} OpenSignal. All rights reserved.
          </p>
          <div className="flex gap-4 mt-4 md:mt-0">
            <a
              href="https://github.com/turizspace/opensignal"
              target="_blank"
              rel="noopener noreferrer"
              className="text-opensignal-text-secondary hover:text-opensignal-gold transition-colors"
              aria-label="GitHub Repository"
            >
              <Github size={20} />
            </a>
            <a
              href="https://twitter.com"
              className="text-opensignal-text-secondary hover:text-opensignal-gold transition-colors"
              aria-label="Twitter"
            >
              <Twitter size={20} />
            </a>
          </div>
        </div>
      </div>
    </footer>
  )
}
