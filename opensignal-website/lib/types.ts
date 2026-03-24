/**
 * Common types and interfaces for the OpenSignal website
 */

export interface Feature {
  icon: React.ReactNode
  title: string
  description: string
}

export interface TabValue {
  href: string
  label: string
}

export interface SocialLink {
  href: string
  label: string
  icon: React.ReactNode
}

export interface APIEndpoint {
  path: string
  method: 'GET' | 'POST' | 'PUT' | 'DELETE'
  description: string
}

export const SITE_CONFIG = {
  name: 'OpenSignal',
  description: 'AI Trading Copilot',
  slogan: 'Chart Analysis, Powered by AI',
  version: '1.0.0',
}

export const COLORS = {
  primary: '#D4AF37',
  secondary: '#1A1A1A',
  dark: '#0F0F0F',
  text: '#F5F5F5',
  muted: '#A0A0A0',
}
