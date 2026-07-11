# Frontend

Este documento detalla la arquitectura, el stack tecnológico y las pautas de diseño seleccionadas para la interfaz de usuario del Trabajo Práctico de la materia **Desarrollo de Aplicaciones en la Nube (DAN)**.

---

## 🛠️ Stack Tecnológico Completo

La arquitectura del frontend se ha definido bajo el paradigma de una **Single Page Application (SPA)** pura, priorizando la simplicidad del código, un entorno altamente predecible para el desarrollo asistido por Inteligencia Artificial (IA), y una comunicación directa y eficiente con el **Spring Cloud Gateway** del ecosistema distribuido.

### 1. Núcleo (Core)
* **React 18.x / 19.x:** Librería base para la construcción de la interfaz de usuario mediante componentes encapsulados y reactivos que manejan su propio estado local.
* **TypeScript 5.x:** Superset de JavaScript que añade tipado estático estricto. Crucial para definir interfaces claras de las entidades del negocio (`Hotel`, `Habitacion`, `Reserva`, `Usuario`) y mitigar errores de integración con el backend.
* **Vite 5.x:** Herramienta de construcción (bundler) ultrarrápida encargada de levantar el entorno local de desarrollo y compilar los assets estáticos del proyecto de forma óptima.

### 2. Interfaz de Usuario (UI / UX)
* **Tailwind CSS 3.4+:** Framework de CSS basado en clases utilitarias integradas directamente en el código JSX, agilizando drásticamente el maquetado sin necesidad de archivos de estilos globales complejos.
* **shadcn/ui (Radix UI):** Colección de componentes accesibles y personalizables que se copian y pegan directamente en el proyecto, ofreciendo un control total sobre el código fuente generado por la IA.
* **Lucide React:** Set de iconos limpios, consistentes y vectoriales (SVG) optimizados para React.

### 3. Gestión de Estado y Datos
* **TanStack Query v5 (React Query):** Motor encargado de la gestión del estado asincrónico, sincronización con los microservicios, almacenamiento en caché, estados de carga (`isLoading`), manejo de errores y revalidación de datos en tiempo real. Actúa como el único estado global del servidor.
* **Axios:** Cliente HTTP para la ejecución de peticiones crudas hacia el Gateway de la aplicación distribuida.

### 4. Formularios y Validaciones
* **React Hook Form:** Librería de alto rendimiento para la gestión y manipulación de formularios de manera declarativa sin provocar re-renders innecesarios.
* **Zod:** Validador de esquemas en tiempo de ejecución. Asegura que los datos capturados en el frontend cumplan estrictamente con las reglas de negocio antes de impactar los endpoints.

---

## 🎨 Estilo de la Interfaz de Usuario: Preset "Neutro Profundo"

La interfaz adopta una estética **Dark Mode minimalista monocromática**, optimizada para tableros de control de datos densos, reduciendo la fatiga visual mediante contrastes suaves y una estructura geométrica fluida.

### 1. Paleta de Colores (Variables CSS / Tokens de Tailwind)
Se define un espectro basado en tonos grises puros neutros sobre un lienzo casi negro, eliminando el uso de negro plano (`#000000`).

| Token CSS / Tailwind | Valor Hexadecimal | Propósito y Aplicación |
| :--- | :--- | :--- |
| `--background` / `bg-background` | `#0a0a0a` | Fondo principal de la aplicación. |
| `--card` / `bg-card` | `#141414` | Contenedor para tarjetas, paneles de gestión, formularios y modales. |
| `--popover` / `bg-popover` | `#141414` | Menús desplegables, selectores dinámicos y tooltips. |
| `--border` / `border-border` | `#262626` | Delimitador fino de 1px para separar bloques y tablas. |
| `--input` / `border-input` | `#262626` | Contornos de los campos de texto y elementos de entrada. |
| `--primary` / `bg-primary` | `#fafafa` | Botones de acción principal y tipografía destacada. |
| `--primary-foreground` | `#0a0a0a` | Texto o icono incrustado dentro de botones primarios. |
| `--muted` / `bg-muted` | `#262626` | Fondos secundarios pasivos (e.g., tracks de progreso, badges). |
| `--muted-foreground` | `#a3a3a3` | Descripciones secundarias, subtítulos y leyendas informativas. |

### 2. Tipografía y Jerarquía Visual
Se recomienda el uso de una fuente sans-serif geométrica y limpia del sistema (e.g., *Inter*).

* **Títulos Principales (`h1`, `h2`):** Tamaño `text-xl` a `text-2xl`, peso `font-semibold` o `font-bold`, color `#fafafa`.
* **Subtítulos / Metadatos:** Tamaño `text-sm`, peso `font-normal`, color `text-muted-foreground` (`#a3a3a3`).
* **Métricas y Precios Destacados:** Tamaño `text-3xl` a `text-4xl`, peso `font-bold`, color `#fafafa`.
* **Tablas e Inputs:** Tamaño `text-sm`, peso `font-medium`.

### 3. Anatomía de Componentes y Bordes
* **Bordes delgados:** Se descartan las sombras paralelas pronunciadas. La separación visual se produce estrictamente mediante contornos finos de 1px (`border border-neutral-800`).
* **Radios de curvatura pronunciados (Border Radius):**
    * Paneles y tarjetas principales: `rounded-xl` (`12px`) o `rounded-2xl` (`16px`).
    * Controles menores (Botones, inputs, badges): `rounded-lg` (`8px`).

---

## ⚙️ Inyección de Estilos en el Proyecto (`index.css`)

Para asegurar la consistencia del preset visual, se deben declarar las siguientes variables dentro de la configuración global de estilos de Tailwind CSS:

```css
@theme inline {
  --color-background: hsl(0 0% 3.9%);
  --color-foreground: hsl(0 0% 98%);
  
  --color-card: hsl(0 0% 7.8%);
  --color-card-foreground: hsl(0 0% 98%);
  
  --color-popover: hsl(0 0% 7.8%);
  --color-popover-foreground: hsl(0 0% 98%);
  
  --color-primary: hsl(0 0% 98%);
  --color-primary-foreground: hsl(0 0% 3.9%);
  
  --color-secondary: hsl(0 0% 14.9%);
  --color-secondary-foreground: hsl(0 0% 98%);
  
  --color-muted: hsl(0 0% 14.9%);
  --color-muted-foreground: hsl(0 0% 63.9%);
  
  --color-accent: hsl(0 0% 14.9%);
  --color-accent-foreground: hsl(0 0% 98%);
  
  --color-destructive: hsl(0 62.8% 30.6%);
  --color-destructive-foreground: hsl(0 0% 98%);
  
  --color-border: hsl(0 0% 14.9%);
  --color-input: hsl(0 0% 14.9%);
  --color-ring: hsl(0 0% 83.1%);
  
  --radius-xl: calc(var(--radius) + 4px);
  --radius-lg: var(--radius);
  --radius-md: calc(var(--radius) - 2px);
  --radius-sm: calc(var(--radius) - 4px);
}