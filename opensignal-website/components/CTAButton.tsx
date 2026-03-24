'use client'

import Link from 'next/link'

interface CTAButtonProps {
  href?: string
  onClick?: () => void
  variant?: 'gold' | 'outline'
  children: React.ReactNode
  className?: string
}

export default function CTAButton({
  href,
  onClick,
  variant = 'gold',
  children,
  className = '',
}: CTAButtonProps) {
  const baseStyles = variant === 'gold' ? 'button-gold' : 'button-outline'

  if (href) {
    return (
      <Link href={href} className={`inline-block ${baseStyles} ${className}`}>
        {children}
      </Link>
    )
  }

  return (
    <button onClick={onClick} className={`${baseStyles} ${className}`}>
      {children}
    </button>
  )
}
