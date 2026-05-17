/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#f0fdf4',
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
        },
        surface: {
          50:  'rgba(255,255,255,0.05)',
          100: 'rgba(255,255,255,0.08)',
          200: 'rgba(255,255,255,0.12)',
          300: 'rgba(255,255,255,0.18)',
        },
      },
      backgroundImage: {
        'gradient-brand': 'linear-gradient(135deg, #10b981, #22d3ee)',
        'gradient-violet': 'linear-gradient(135deg, #a78bfa, #f472b6)',
        'gradient-warm': 'linear-gradient(135deg, #fb923c, #ef4444)',
      },
      boxShadow: {
        'glow-emerald': '0 0 30px rgba(16,185,129,0.2)',
        'glow-cyan':    '0 0 30px rgba(34,211,238,0.2)',
        'glow-violet':  '0 0 30px rgba(167,139,250,0.2)',
        'glass':        '0 8px 32px rgba(0,0,0,0.4)',
      },
      animation: {
        'aurora':     'aurora-shift 8s ease-in-out infinite',
        'float':      'float 3s ease-in-out infinite',
        'pulse-ring': 'pulse-ring 2s ease-in-out infinite',
        'shimmer':    'shimmer 2s linear infinite',
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
}
