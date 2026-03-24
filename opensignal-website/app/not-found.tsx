'use client'

import Link from 'next/link'

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 pt-20">
      <div className="max-w-md text-center">
        <h1 className="gradient-gold-text text-6xl font-bold mb-4">404</h1>
        <h2 className="text-3xl font-bold text-opensignal-text mb-4">
          Page Not Found
        </h2>
        <p className="text-opensignal-text-secondary mb-8">
          The page you’re looking for doesn’t exist or has been moved.
        </p>
        <Link href="/" className="button-gold">
          Back to Home
        </Link>
      </div>
    </div>
  )
}
