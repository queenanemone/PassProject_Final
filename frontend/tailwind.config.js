/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: "#3B82F6",
        "background-light": "#f6f7f8",
        "background-dark": "#111827",
        "card-dark": "#1F2937",
        "border-dark": "#374151",
        "text-dark-primary": "#F9FAFB",
        "text-dark-secondary": "#9CA3AF",
        "text-dark": "#E5E7EB",
        "text-secondary-dark": "#9CA3AF",
      },
      fontFamily: {
        display: ['Pretendard', 'sans-serif'],
        sans: ['Pretendard', 'sans-serif'],
      },
    },
  },
  plugins: [],
}

