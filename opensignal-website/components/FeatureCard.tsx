'use client'

interface CardProps {
  icon: React.ReactNode
  title: string
  description: string
  className?: string
}

export default function FeatureCard({ icon, title, description, className = '' }: CardProps) {
  return (
    <div className={`glass p-6 rounded-xl border-opensignal-gold/20 hover:border-opensignal-gold/50 transition-all duration-300 hover:shadow-lg hover:shadow-opensignal-gold/10 ${className}`}>
      <div className="text-opensignal-gold mb-4">{icon}</div>
      <h3 className="text-lg font-semibold text-opensignal-text mb-2">{title}</h3>
      <p className="text-opensignal-text-secondary text-sm">{description}</p>
    </div>
  )
}
