# OpenSignal Website - Setup & Deployment Guide

## Quick Start

### Prerequisites
- Node.js 18+ ([Download](https://nodejs.org))
- npm or yarn package manager
- Git

### Local Development Setup

1. **Navigate to the website directory**
```bash
cd opensignal-website
```

2. **Install dependencies**
```bash
npm install
```

3. **Create environment file**
```bash
cp .env.example .env.local
```

4. **Start development server**
```bash
npm run dev
```

5. **Open in browser**
Visit `http://localhost:3000`

## Project Structure

```
opensignal-website/
├── app/                      # Next.js App Router pages
│   ├── layout.tsx           # Root layout & metadata
│   ├── page.tsx             # Home page (/)
│   ├── globals.css          # Global styles & Tailwind
│   ├── features/page.tsx    # Features page
│   ├── resources/page.tsx   # Resources & docs
│   ├── about/page.tsx       # About page
│   ├── download/page.tsx    # APK download page
│   ├── roadmap/page.tsx     # Product roadmap
│   ├── contact/page.tsx     # Contact form
│   ├── terms/page.tsx       # Terms of service
│   └── not-found.tsx        # 404 page
│
├── components/              # Reusable React components
│   ├── Navbar.tsx          # Navigation bar
│   ├── Footer.tsx          # Footer with links
│   ├── Section.tsx         # Section wrapper
│   ├── FeatureCard.tsx     # Feature card item
│   ├── CTAButton.tsx       # Call-to-action button
│   └── ErrorBoundary.tsx   # Error boundary wrapper
│
├── lib/                    # Utilities & types
│   ├── types.ts           # TypeScript interfaces
│   └── utils.ts           # Helper functions
│
├── public/                # Static files
│   ├── robots.txt        # Search engine crawlers
│   └── favicon.ico       # Website favicon
│
├── package.json          # Dependencies
├── tsconfig.json        # TypeScript config
├── tailwind.config.ts   # Tailwind CSS theme
├── next.config.ts       # Next.js config
├── .eslintrc.json      # ESLint rules
├── .gitignore          # Git ignore patterns
├── README.md           # Project documentation
└── SETUP.md            # This file
```

## Development Workflow

### Creating New Pages

1. **Create new directory under `app/`**
```bash
mkdir app/my-page
```

2. **Add `page.tsx`**
```typescript
'use client'

import Section from '@/components/Section'

export default function MyPage() {
  return (
    <div className="pt-20">
      <Section title="My Page" subtitle="Subtitle here">
        {/* Content */}
      </Section>
    </div>
  )
}
```

3. **Route is automatically available at `/my-page`**

### Creating New Components

1. **Create file in `components/`**
```typescript
// components/MyComponent.tsx
'use client'

interface Props {
  title: string
  children: React.ReactNode
}

export default function MyComponent({ title, children }: Props) {
  return (
    <div className="...">
      <h2>{title}</h2>
      {children}
    </div>
  )
}
```

2. **Import and use in pages**
```typescript
import MyComponent from '@/components/MyComponent'

export default function Page() {
  return <MyComponent title="Test">Content</MyComponent>
}
```

## Styling

### Tailwind CSS
The site uses Tailwind CSS for styling. Custom colors defined in `tailwind.config.ts`:

```typescript
colors: {
  opensignal: {
    gold: '#D4AF37',          // Primary brand color
    'gold-light': '#E5C158',
    'gold-dark': '#B8941B',
    dark: '#0F0F0F',          // Dark background
    'dark-secondary': '#1A1A1A',
    'dark-tertiary': '#252525',
    text: '#F5F5F5',          // Light text
    'text-secondary': '#A0A0A0',
  },
}
```

### Custom CSS Classes

**In `app/globals.css`:**
- `.button-gold` - Gold CTA button
- `.button-outline` - Outline button variant
- `.gradient-gold` - Gold gradient background
- `.gradient-gold-text` - Gradient text effect
- `.glass` - Glassmorphism background

### Usage Example
```jsx
<button className="button-gold">Download</button>
<h1 className="gradient-gold-text">Heading</h1>
<div className="glass p-6">Content</div>
```

## Building for Production

### Build the site
```bash
npm run build
```

This creates an optimized production build in `.next/`

### Start production server
```bash
npm start
```

## Deployment

### Option 1: Vercel (Recommended)

1. **Install Vercel CLI**
```bash
npm install -g vercel
```

2. **Deploy**
```bash
vercel
```

3. **Follow the prompts**

### Option 2: Netlify

1. **Connect GitHub repository**
2. **Set build command:** `npm run build`
3. **Set publish directory:** `.next`
4. **Deploy**

### Option 3: Docker

```dockerfile
FROM node:18-alpine

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

EXPOSE 3000
CMD ["npm", "start"]
```

Build and run:
```bash
docker build -t opensignal-website .
docker run -p 3000:3000 opensignal-website
```

## Environment Variables

Create `.env.local` in project root:

```env
# Site Configuration
NEXT_PUBLIC_SITE_NAME=OpenSignal
NEXT_PUBLIC_SITE_URL=https://opensignal.io
NEXT_PUBLIC_API_URL=http://localhost:8000

# Add analytics, forms, or other services
# NEXT_PUBLIC_GA_ID=...
```

**Public variables** (prefixed with `NEXT_PUBLIC_`) are sent to the browser.
**Private variables** are only available on the server.

## Scripts

```bash
# Development
npm run dev              # Start dev server on port 3000

# Production
npm run build            # Build for production
npm start               # Start production server

# Code Quality
npm run lint            # Run ESLint
npm run type-check      # Check TypeScript types
```

## TypeScript

The project uses strict TypeScript for type safety:

```typescript
// Good ✓
interface Props {
  title: string
  count: number
  onClick: () => void
}

function Component({ title, count, onClick }: Props) {
  return <div onClick={onClick}>{title}: {count}</div>
}

// Avoid ✗
function Component(props: any) {
  return <div onClick={props.onClick}>{props.title}</div>
}
```

## Performance Optimization

### Image Optimization
Use Next.js Image component:
```typescript
import Image from 'next/image'

<Image
  src="/logo.png"
  alt="Logo"
  width={100}
  height={100}
/>
```

### Code Splitting
Pages are automatically code-split by Next.js App Router.

### CSS Optimization
Tailwind CSS purges unused styles automatically in production.

## Testing

### Running Linting
```bash
npm run lint
```

### Checking Types
```bash
npm run type-check
```

## Troubleshooting

### Port 3000 Already in Use
```bash
# Find and kill process
lsof -i :3000
kill -9 <PID>

# Or use different port
npm run dev -- -p 3001
```

### Build Failures
```bash
# Clear Next.js cache
rm -rf .next

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install

# Rebuild
npm run build
```

### Styling Issues
- Clear browser cache
- Check `tailwind.config.ts` for color definitions
- Verify CSS classes in `app/globals.css`

## Adding Features

### Contact Form
The contact page has a form template. To make it functional:

1. **Install email service** (e.g., nodemailer, SendGrid)
2. **Create API route**
```typescript
// app/api/contact/route.ts
export async function POST(request: Request) {
  const data = await request.json()
  // Send email
  return Response.json({ success: true })
}
```

3. **Update form handler in `contact/page.tsx`**

### Analytics
Add Google Analytics:

1. **Install gtag**
```bash
npm install @next/third-parties
```

2. **Add to `app/layout.tsx`**
```typescript
import { GoogleAnalytics } from '@next/third-parties/google'

export default function RootLayout(...) {
  return (
    <html>
      <body>
        {/* content */}
        <GoogleAnalytics gaId="G-XXXXXXXXXX" />
      </body>
    </html>
  )
}
```

## Resources

- [Next.js Docs](https://nextjs.org/docs)
- [React Docs](https://react.dev)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [TypeScript Docs](https://www.typescriptlang.org/docs)

## Support

For issues or questions:
- Check the [README.md](./README.md)
- Review component examples
- Check TypeScript types in `lib/types.ts`

## License

MIT License - See LICENSE file for details
