import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        opensignal: {
          gold: '#D4AF37',
          'gold-light': '#E5C158',
          'gold-dark': '#B8941B',
          dark: '#0F0F0F',
          'dark-secondary': '#1A1A1A',
          'dark-tertiary': '#252525',
          text: '#F5F5F5',
          'text-secondary': '#A0A0A0',
        },
      },
      fontFamily: {
        sans: [
          '"Space Grotesk"',
          '"Sora"',
          '"Manrope"',
          'ui-sans-serif',
          'system-ui',
          'sans-serif',
        ],
      },
      backgroundImage: {
        'gradient-gold': 'linear-gradient(135deg, #D4AF37 0%, #E5C158 100%)',
        'gradient-dark': 'linear-gradient(135deg, #0F0F0F 0%, #1A1A1A 100%)',
      },
    },
  },
  plugins: [],
}

export default config
