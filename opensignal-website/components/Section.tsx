'use client'

interface SectionProps {
  title: string
  subtitle?: string
  children: React.ReactNode
  className?: string
}

export default function Section({
  title,
  subtitle,
  children,
  className = '',
}: SectionProps) {
  return (
    <section className={`py-20 px-4 sm:px-6 lg:px-8 ${className}`}>
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="gradient-gold-text text-4xl sm:text-5xl font-bold mb-4">
            {title}
          </h2>
          {subtitle && (
            <p className="text-opensignal-text-secondary text-lg max-w-2xl mx-auto">
              {subtitle}
            </p>
          )}
        </div>
        {children}
      </div>
    </section>
  )
}
