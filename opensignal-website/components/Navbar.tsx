'use client'

import Link from 'next/link'
import Image from 'next/image'
import { useState } from 'react'
import { Menu, X, Github } from 'lucide-react'

export default function Navbar() {
  const [isOpen, setIsOpen] = useState(false)

  const navLinks = [
    { href: '/', label: 'Home' },
    { href: '/features', label: 'Features' },
    { href: '/resources', label: 'Resources' },
    { href: '/about', label: 'About' },
  ]

  return (
    <nav className="fixed top-0 w-full bg-opensignal-dark/80 backdrop-blur-md z-50 border-b border-opensignal-gold/10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-3 group">
            <Image
              src="/images/logo-icon.png"
              alt="OpenSignal"
              width={40}
              height={40}
              className="rounded-lg"
            />
            <span className="gradient-gold-text font-bold text-xl hidden sm:inline">OpenSignal</span>
          </Link>

          {/* Desktop Menu */}
          <div className="hidden md:flex items-center gap-8">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="text-opensignal-text-secondary hover:text-opensignal-gold transition-colors"
              >
                {link.label}
              </Link>
            ))}
            <a
              href="https://github.com/turizspace/opensignal"
              target="_blank"
              rel="noopener noreferrer"
              className="text-opensignal-text-secondary hover:text-opensignal-gold transition-colors"
              title="View on GitHub"
            >
              <Github size={20} />
            </a>
            <a
              href="/download"
              className="button-gold text-sm"
            >
              Download APK
            </a>
          </div>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden text-opensignal-gold"
            onClick={() => setIsOpen(!isOpen)}
          >
            {isOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Mobile Menu */}
        {isOpen && (
          <div className="md:hidden pb-4 border-t border-opensignal-gold/10">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="block py-2 text-opensignal-text-secondary hover:text-opensignal-gold transition-colors"
                onClick={() => setIsOpen(false)}
              >
                {link.label}
              </Link>
            ))}
            <a
              href="https://github.com/turizspace/opensignal"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 py-2 text-opensignal-text-secondary hover:text-opensignal-gold transition-colors"
            >
              <Github size={18} />
              GitHub
            </a>
            <a
              href="/download"
              className="block mt-4 button-gold text-sm text-center"
            >
              Download APK
            </a>
          </div>
        )}
      </div>
    </nav>
  )
}
