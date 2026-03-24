# OpenSignal Website

Modern, performant website for the OpenSignal AI Trading Copilot built with Next.js 14, TypeScript, and Tailwind CSS.

## Features

- ✨ Modern, responsive design with gold and dark theme
- 🚀 Fast performance with Next.js 14 App Router
- 📱 Mobile-first responsive design
- ♿ Accessible components with semantic HTML
- 🎨 Tailwind CSS with custom OpenSignal theme
- 📦 Reusable component library
- 🔍 SEO optimized
- 🌙 Dark mode by default

## Project Structure

```
opensignal-website/
├── app/
│   ├── layout.tsx           # Root layout
│   ├── globals.css          # Global styles
│   ├── page.tsx             # Home page
│   ├── features/
│   │   └── page.tsx         # Features page
│   ├── resources/
│   │   └── page.tsx         # Resources & docs
│   ├── about/
│   │   └── page.tsx         # About page
│   ├── download/
│   │   └── page.tsx         # Download APK page
│   └── not-found.tsx        # 404 page
├── components/
│   ├── Navbar.tsx           # Navigation bar
│   ├── Footer.tsx           # Footer
│   ├── Section.tsx          # Reusable section wrapper
│   ├── FeatureCard.tsx      # Feature card component
│   ├── CTAButton.tsx        # Call-to-action button
│   └── ErrorBoundary.tsx    # Error boundary
├── lib/
│   ├── types.ts             # TypeScript types & interfaces
│   └── utils.ts             # Utility functions
├── public/                   # Static assets
├── package.json
├── tsconfig.json
├── tailwind.config.ts       # Tailwind configuration
├── next.config.ts           # Next.js configuration
└── README.md
```

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Run development server
npm run dev

# Open browser to http://localhost:3000
```

### Build for Production

```bash
# Build static site
npm build

# Start production server
npm start
```

## Pages

### Home (`/`)
Landing page with hero section, feature highlights, and CTAs

### Features (`/features`)
Detailed breakdown of OpenSignal's AI analysis capabilities

### Resources (`/resources`)
Documentation, API docs, FAQs, and getting started guides

### About (`/about`)
Company mission, values, and technology stack

### Download (`/download`)
Android APK and Desktop app downloads with installation guides

## Components

All components are built with TypeScript and include:
- Proper type definitions
- Accessibility features
- Responsive design
- Tailwind CSS styling

### Reusable Components

- **Section**: Wrapper for page sections with title/subtitle
- **FeatureCard**: Card for displaying features
- **CTAButton**: Call-to-action button (gold or outline variant)
- **Navbar**: Fixed navigation with mobile menu
- **Footer**: Footer with links and social

## Styling

The site uses Tailwind CSS with custom OpenSignal theme colors:
- **Gold** (`#D4AF37`): Primary brand color
- **Dark** (`#0F0F0F`): Dark background
- **Text** (`#F5F5F5`): Light text

Custom CSS utilities:
- `.button-gold` - Gold CTA button
- `.button-outline` - Border CTA button
- `.gradient-gold-text` - Gold gradient text effect
- `.glass` - Glassmorphism effect

## Development

### Code Style

- Prefer TypeScript for type safety
- Use functional components
- Prop drilling sparingly (context for global state)
- Keep components small and reusable

### Naming Conventions

- Components: PascalCase (`Navbar.tsx`)
- Utilities: camelCase (`utils.ts`)
- Files: Match component name

## Performance

- Optimized images with Next.js Image component
- Code splitting at route level
- Minimal JavaScript bundle
- CSS optimization with Tailwind purge

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Deployment

The site is ready to deploy to:
- Vercel (recommended)
- Netlify
- AWS Amplify
- Any Node.js host

```bash
# Deploy to Vercel
npm install -g vercel
vercel
```

## License

MIT License - See LICENSE file for details

## Support

For issues or questions, please open an issue on GitHub or contact the team.
