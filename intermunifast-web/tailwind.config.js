/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // Primary colors based on the design
                primary: {
                    50: '#FFF1F2',
                    100: '#FFE4E6',
                    200: '#FECDD3',
                    300: '#FDA4AF',
                    400: '#FB7185',
                    500: '#F43F5E', // Main red/pink
                    600: '#E11D48',
                    700: '#BE123C',
                    800: '#9F1239',
                    900: '#881337',
                },
                // Accent color (the bright red from the design)
                accent: {
                    DEFAULT: '#FF5A5F',
                    light: '#FF6B6F',
                    dark: '#E04F54', // Darker shade for dark-theme components
                },
                // Neutral grays
                neutral: {
                    50: '#FAFAFA',
                    100: '#F5F5F5',
                    200: '#E5E5E5',
                    300: '#D4D4D4',
                    400: '#A3A3A3',
                    500: '#737373',
                    600: '#525252',
                    700: '#404040', // Lighter dark
                    800: '#262626', // Dark
                    900: '#171717', // Darkest
                    950: '#0A0A0A', // Almost black
                },
            },
            borderRadius: {
                'ticket': '1.5rem', // For ticket-style cards
            },
            boxShadow: {
                'card': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
                'card-hover': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                'ticket': '0 8px 16px rgba(0, 0, 0, 0.12)',
                'glow-accent': '0 0 20px rgba(255, 90, 95, 0.5), 0 0 40px rgba(255, 90, 95, 0.3)',
            },
            fontFamily: {
                sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
            },
            animation: {
                'gradient': 'gradient 3s ease infinite',
                'float': 'float 6s ease-in-out infinite',
                'shimmer': 'shimmer 2s infinite',
            },
            keyframes: {
                gradient: {
                    '0%, 100%': {
                        backgroundPosition: '0% 50%',
                    },
                    '50%': {
                        backgroundPosition: '100% 50%',
                    },
                },
                float: {
                    '0%, 100%': {
                        transform: 'translateY(0px)',
                    },
                    '50%': {
                        transform: 'translateY(-20px)',
                    },
                },
                shimmer: {
                    '0%': {
                        backgroundPosition: '-1000px 0',
                    },
                    '100%': {
                        backgroundPosition: '1000px 0',
                    },
                },
            },
        },
    },
    plugins: [],
}
