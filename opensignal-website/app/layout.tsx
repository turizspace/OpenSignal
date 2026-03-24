import '@/app/globals.css'
import { Inter } from 'next/font/google'
import Navbar from '@/components/Navbar'
import Footer from '@/components/Footer'

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' })

export const metadata = {
  title: 'OpenSignal - AI Trading Copilot',
  description: 'AI-powered chart analysis, Nostr-native signal publishing, and risk-managed trading copilot.',
  keywords: ['trading', 'AI', 'chart analysis', 'Nostr', 'crypto'],
  authors: [{ name: 'OpenSignal Team' }],
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={`${inter.variable} bg-opensignal-dark text-opensignal-text antialiased`}>
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
