---
name: Neutro Profundo
colors:
  surface: '#121414'
  surface-dim: '#121414'
  surface-bright: '#38393a'
  surface-container-lowest: '#0d0e0f'
  surface-container-low: '#1a1c1c'
  surface-container: '#1e2020'
  surface-container-high: '#292a2a'
  surface-container-highest: '#343535'
  on-surface: '#e3e2e2'
  on-surface-variant: '#c4c7c8'
  inverse-surface: '#e3e2e2'
  inverse-on-surface: '#2f3131'
  outline: '#8e9192'
  outline-variant: '#444748'
  surface-tint: '#c6c6c7'
  primary: '#ffffff'
  on-primary: '#2f3131'
  primary-container: '#e2e2e2'
  on-primary-container: '#636565'
  inverse-primary: '#5d5f5f'
  secondary: '#c8c6c5'
  on-secondary: '#303030'
  secondary-container: '#474746'
  on-secondary-container: '#b7b5b4'
  tertiary: '#ffffff'
  on-tertiary: '#32302d'
  tertiary-container: '#e7e1dd'
  on-tertiary-container: '#676460'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e2e2e2'
  primary-fixed-dim: '#c6c6c7'
  on-primary-fixed: '#1a1c1c'
  on-primary-fixed-variant: '#454747'
  secondary-fixed: '#e4e2e1'
  secondary-fixed-dim: '#c8c6c5'
  on-secondary-fixed: '#1b1c1c'
  on-secondary-fixed-variant: '#474746'
  tertiary-fixed: '#e7e1dd'
  tertiary-fixed-dim: '#cbc6c1'
  on-tertiary-fixed: '#1d1b19'
  on-tertiary-fixed-variant: '#494643'
  background: '#121414'
  on-background: '#e3e2e2'
  surface-variant: '#343535'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  title-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin-edge: 24px
---

## Brand & Style
The design system is engineered for high-stakes hospitality environments, prioritizing operational focus through a "Neutro Profundo" aesthetic. The brand personality is clinical, authoritative, and sophisticated, catering to B2B hospitality professionals who manage complex data streams. 

The style is a refined **Minimalist Dark Mode** that leans into monochromatic precision. By eschewing vibrant colors in favor of a grayscale hierarchy, the UI reduces cognitive load and allows data (status indicators, occupancy levels, revenue metrics) to remain the primary focus. The emotional response is one of calm control and technical excellence. Structure is defined not by heavy shadows, but by surgical 1px borders and subtle tonal shifts between #0a0a0a and #141414.

## Colors
The palette is strictly monochromatic to ensure maximum contrast for functional elements.
- **Lienzo (Background):** #0a0a0a acts as the base layer, providing a deep, non-distracting canvas.
- **Superficies (Containers):** #141414 is used for cards, panels, and modals to create a subtle lift from the background.
- **Acción Primaria:** #fafafa (Off-white) is reserved exclusively for primary triggers, ensuring the "call to action" is unmistakable against the dark void.
- **Bordes y Estados:** #262626 defines the architecture of the UI, used for 1px borders and inactive/muted states.
- **Texto Secundario:** #a3a3a3 is used for meta-data and descriptions to maintain a clear typographic hierarchy.

## Typography
This design system utilizes **Inter** for its systematic, utilitarian clarity. 
- **Títulos:** Always in #fafafa with bold weights to punctuate the layout.
- **Cuerpo de texto:** High-readability sizes (14px-16px) using #a3a3a3 for general content and #fafafa for active content.
- **Densidad:** Given the B2B nature, tight line-heights are used to maximize information density without sacrificing legibility.
- **Idioma:** All labeling and interface text must be in Spanish, ensuring localized professional terminology (e.g., "Disponibilidad", "Tarifas", "Reservas").

## Layout & Spacing
The layout follows a **Fluid Grid** model optimized for data-heavy dashboards.
- **Métrica base:** A 4px baseline grid ensures consistent alignment of technical data points.
- **Contenedores:** Use 16px (md) internal padding for cards and 24px (lg) for major sections.
- **Adaptabilidad:** 
  - **Desktop:** 12-column grid with 16px gutters.
  - **Tablet:** 8-column grid with 12px gutters.
  - **Mobile:** 4-column grid with 16px margins.
- **Densidad:** In data tables, vertical spacing is reduced to 8px (xs) to allow more rows to be visible above the fold.

## Elevation & Depth
Elevation in this design system is communicated through **Tonal Layering** and **1px Outlines**, rather than shadows.
- **Nivel 0 (Lienzo):** #0a0a0a. The base of the application.
- **Nivel 1 (Paneles/Tarjetas):** #141414 surface with a 1px solid border of #262626.
- **Nivel 2 (Popovers/Dropdowns):** #141414 surface with a slightly brighter 1px border (#404040) to indicate interactivity and temporary presence.
- **Interacción:** Hover states on interactive elements should shift the background color from #141414 to #1c1c1c or lighten the border color. Shadows are strictly prohibited to maintain the sharp, monochromatic aesthetic.

## Shapes
The shape language balances modern software aesthetics with professional rigidity.
- **Contenedores Principales (Cards/Modals):** Use `rounded-xl` (12px) to soften the large blocks of dark color.
- **Elementos de Acción (Buttons/Inputs):** Use `rounded-lg` (8px) to provide a distinct visual language from the outer containers.
- **Indicadores Pequeños (Tags/Badges):** Should use a subtle 4px radius to remain sharp and technical.

## Components
- **Botones Primarios:** Background #fafafa, text #0a0a0a. Bold weight. High contrast.
- **Botones Secundarios:** Background #262626, text #fafafa, border 1px #262626.
- **Campos de Entrada (Inputs):** Background #0a0a0a, border 1px #262626, text #fafafa. Placeholder text in #525252.
- **Tarjetas (Cards):** Background #141414, border 1px #262626, radius 12px.
- **Tablas de Datos:** Header background #0a0a0a, border-bottom 1px #262626. Cells use #a3a3a3 for secondary data and #fafafa for primary identifiers.
- **Chips/Status:** Neutral background #262626 with text #a3a3a3. For critical states (Error/Success), use low-saturation color tints (muted reds/greens) only on the text or a small 6px dot indicator to maintain the monochromatic integrity.