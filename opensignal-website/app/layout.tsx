import '@/app/globals.css'
import type { Metadata } from 'next'
import Navbar from '@/components/Navbar'
import Footer from '@/components/Footer'

const siteUrl = 'https://opensignal.pro'
const siteTitle = 'OpenSignal - AI Trading Copilot'
const siteDescription =
  'AI-powered chart analysis, Nostr-native signal publishing, and risk-managed trading copilot.'

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: siteTitle,
  description: siteDescription,
  keywords: ['trading', 'AI', 'chart analysis', 'Nostr', 'crypto'],
  authors: [{ name: 'OpenSignal Team' }],
  openGraph: {
    title: siteTitle,
    description: siteDescription,
    url: siteUrl,
    siteName: 'OpenSignal',
    type: 'website',
    images: [
      {
        url: '/images/opensignal-thumbnail.png',
        width: 1200,
        height: 630,
        alt: 'OpenSignal preview image',
      },
    ],
  },
  twitter: {
    card: 'summary_large_image',
    title: siteTitle,
    description: siteDescription,
    images: ['/images/opensignal-thumbnail.png'],
  },
  icons: {
    icon: '/favicon.ico',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className="bg-opensignal-dark text-opensignal-text antialiased">
        <div className="site-ambient min-h-screen">
          <Navbar />
          <main className="min-h-screen">
            {children}
          </main>
          <Footer />
        </div>
      </body>
    </html>
  )
}
