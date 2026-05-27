/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        medin: {
          // Azul marino principal - #1F2B6C
          navy: '#1F2B6C',
          'navy-dark': '#161F4D',
          'navy-light': '#2A3A7D',
          // Cyan/Turquesa - acentos
          cyan: '#159EEC',
          'cyan-light': '#4DD0E1',
          'cyan-dark': '#0D7FC2',
          // Azul claro - botones y elementos secundarios
          blue: '#BFD2F8',
          'blue-light': '#D6E4FF',
          'blue-dark': '#8BA8E8',
          // Grises
          gray: {
            50: '#F8F9FA',
            100: '#E8E8E8',
            200: '#D1D1D1',
            300: '#B4B4B4',
            400: '#8E8E8E',
            500: '#6B6B6B',
            600: '#4A4A4A',
            700: '#2E2E2E',
            800: '#1A1A1A',
            900: '#0D0D0D',
          },
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Yeseva One', 'Georgia', 'serif'],
      },
      fontSize: {
        'xs': '0.875rem',      // 14px
        'sm': '1rem',          // 16px (aumentado de 0.875)
        'base': '1.125rem',    // 18px (aumentado de 1)
        'lg': '1.375rem',      // 22px (aumentado de 1.125)
        'xl': '1.75rem',       // 28px (aumentado de 1.5)
        '2xl': '2.25rem',      // 36px (aumentado de 1.875)
        '3xl': '2.75rem',      // 44px (aumentado de 2.25)
        '4xl': '3.5rem',       // 56px (aumentado de 3)
        '5xl': '4.5rem',       // 72px (aumentado de 3.75)
        '6xl': '5.5rem',       // 88px (aumentado de 4.5)
      },
    },
  },
  plugins: [],
}
