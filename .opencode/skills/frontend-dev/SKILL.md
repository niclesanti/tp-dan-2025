---
name: frontend-dev
description: Use when implementing, refactoring, or debugging frontend features (components, pages, hooks, services, forms, styles) in the React + TypeScript SPA for the hotel reservation system.
---

# Frontend Development — React SPA

Use this skill when the task involves writing or modifying frontend code in the SPA project.

## References

- `AGENTS.md` — project overview, backend services, ports, architecture
- `docs-frontend.md` — full stack specification, design tokens, color palette, typography

## Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Core | React + TypeScript | 18.x/19.x + 5.x |
| Bundler | Vite | 5.x |
| Styling | Tailwind CSS + shadcn/ui (Radix) | 3.4+ |
| Icons | Lucide React | Latest |
| Async State | TanStack Query v5 | Latest |
| HTTP Client | Axios | Latest |
| Forms | React Hook Form + Zod | Latest |

## Project structure

```
frontend/
├── public/
├── src/
│   ├── assets/              # Static assets (images, fonts)
│   ├── components/          # Reusable UI components
│   │   ├── ui/              # shadcn/ui base components (Button, Input, Card, etc.)
│   │   └── layout/          # Layout components (Header, Sidebar, Footer)
│   ├── pages/               # Route-level page components
│   ├── hooks/               # Custom React hooks
│   ├── services/            # Axios API client and endpoint definitions
│   ├── types/               # TypeScript interfaces for business entities
│   │   ├── hotel.ts
│   │   ├── habitacion.ts
│   │   ├── reserva.ts
│   │   └── usuario.ts
│   ├── lib/                 # Utility functions and formatters
│   ├── App.tsx              # Root component with router
│   ├── main.tsx             # Entry point
│   └── index.css            # Tailwind theme + CSS variables
├── components.json          # shadcn/ui configuration
├── tailwind.config.ts
├── tsconfig.json
├── vite.config.ts
├── package.json
└── index.html
```

## Implementation workflow

### 1. Types (TypeScript interfaces)

Define strict interfaces for every business entity. Never use `any`.

```typescript
// src/types/habitacion.ts
export interface Habitacion {
  id: number;
  numero: number;
  tipo: string;
  capacidad: number;
  precioPorNoche: number;
  hotelId: number;
  hotelNombre?: string;
}
```

### 2. Services (Axios API client)

Create a centralized Axios instance with interceptors. One file per backend service.

```typescript
// src/services/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    // Centralized error handling
    return Promise.reject(err);
  }
);

export default api;
```

```typescript
// src/services/habitacion.service.ts
import api from './api';
import { Habitacion } from '../types/habitacion';

export const habitacionService = {
  search: (params: Record<string, string>) =>
    api.get<{ content: Habitacion[]; totalElements: number }>('/habitaciones', { params }),
  getById: (id: number) =>
    api.get<Habitacion>(`/habitaciones/${id}`),
};
```

### 3. Custom hooks (TanStack Query)

Wrap every service call in a custom hook that uses TanStack Query for caching, loading, and error states.

```typescript
// src/hooks/useHabitaciones.ts
import { useQuery } from '@tanstack/react-query';
import { habitacionService } from '../services/habitacion.service';

export function useHabitaciones(params: Record<string, string>) {
  return useQuery({
    queryKey: ['habitaciones', params],
    queryFn: () => habitacionService.search(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}
```

### 4. Form components (React Hook Form + Zod)

Define Zod schemas for validation. Use React Hook Form for form state management.

```typescript
// src/lib/validators/reserva.ts
import { z } from 'zod';

export const reservaSchema = z.object({
  habitacionId: z.number().min(1, 'Seleccioná una habitación'),
  fechaEntrada: z.string().min(1, 'Fecha de entrada requerida'),
  fechaSalida: z.string().min(1, 'Fecha de salida requerida'),
  huespedId: z.number().min(1, 'Huésped requerido'),
});

export type ReservaFormValues = z.infer<typeof reservaSchema>;
```

```typescript
// src/pages/ReservasPage.tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { reservaSchema, ReservaFormValues } from '../lib/validators/reserva';

export function ReservasPage() {
  const form = useForm<ReservaFormValues>({
    resolver: zodResolver(reservaSchema),
  });
  // ...
}
```

### 5. Page components

One file per route. Pages compose hooks + UI components. No business logic in pages — delegate to hooks.

```
src/pages/
├── HomePage.tsx
├── HotelesPage.tsx
├── HabitacionesPage.tsx
├── ReservasPage.tsx
├── LoginPage.tsx
└── DashboardPage.tsx
```

### 6. Responsive layout

All pages must be responsive. Use Tailwind breakpoints:
- **Mobile first**: Default styles = mobile
- `sm:` (640px) — Small tablets
- `md:` (768px) — Tablets / small desktops
- `lg:` (1024px) — Desktops
- `xl:` (1280px) — Large screens

Use shadcn/ui's responsive patterns:
- `<Sheet>` for mobile navigation (hamburger menu)
- `<Dialog>` for modals on all sizes
- Grid: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4`
- Flex: `flex flex-col sm:flex-row items-start sm:items-center gap-4`

### 7. Routing (React Router)

```typescript
// src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/hoteles" element={<HotelesPage />} />
          <Route path="/hoteles/:id/habitaciones" element={<HabitacionesPage />} />
          <Route path="/reservas" element={<ReservasPage />} />
          <Route path="/login" element={<LoginPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

## Visual design system — "Neutro Profundo"

Dark mode minimalist monocromatic. No shadows, no flat black. Separation via 1px borders.

### Color palette (CSS variables in index.css)

```css
@theme inline {
  --color-background: hsl(0 0% 3.9%);       /* #0a0a0a */
  --color-foreground: hsl(0 0% 98%);        /* #fafafa */
  --color-card: hsl(0 0% 7.8%);             /* #141414 */
  --color-card-foreground: hsl(0 0% 98%);
  --color-popover: hsl(0 0% 7.8%);
  --color-popover-foreground: hsl(0 0% 98%);
  --color-primary: hsl(0 0% 98%);           /* #fafafa */
  --color-primary-foreground: hsl(0 0% 3.9%);
  --color-secondary: hsl(0 0% 14.9%);       /* #262626 */
  --color-secondary-foreground: hsl(0 0% 98%);
  --color-muted: hsl(0 0% 14.9%);
  --color-muted-foreground: hsl(0 0% 63.9%); /* #a3a3a3 */
  --color-accent: hsl(0 0% 14.9%);
  --color-accent-foreground: hsl(0 0% 98%);
  --color-destructive: hsl(0 62.8% 30.6%);
  --color-destructive-foreground: hsl(0 0% 98%);
  --color-border: hsl(0 0% 14.9%);
  --color-input: hsl(0 0% 14.9%);
  --color-ring: hsl(0 0% 83.1%);
}
```

### Typography

| Element | Tailwind classes |
|---------|-----------------|
| h1, h2 | `text-xl text-2xl font-semibold text-foreground` |
| Subtítulos | `text-sm font-normal text-muted-foreground` |
| Métricas/Precios | `text-3xl text-4xl font-bold text-foreground` |
| Tablas, Inputs | `text-sm font-medium` |

### Component anatomy

- **Borders**: `border border-border` (1px, no shadows)
- **Cards/Panels**: `bg-card rounded-xl border border-border p-6`
- **Buttons/Inputs**: `rounded-lg`
- **Badges**: `bg-muted text-muted-foreground rounded-lg px-2 py-1 text-xs`

## Responsive patterns

### Mobile navigation
```tsx
<Sheet>
  <SheetTrigger asChild>
    <Button variant="ghost" size="icon" className="md:hidden">
      <Menu className="h-5 w-5" />
    </Button>
  </SheetTrigger>
  <SheetContent side="left">
    {/* Navigation links */}
  </SheetContent>
</Sheet>
```

### Responsive grid
```tsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
  {items.map((item) => (
    <Card key={item.id} className="bg-card rounded-xl border border-border">
      {/* Card content */}
    </Card>
  ))}
</div>
```

### Responsive table (mobile card fallback)
```tsx
{/* Desktop table */}
<div className="hidden md:block overflow-x-auto">
  <table>...</table>
</div>
{/* Mobile card list */}
<div className="md:hidden space-y-4">
  {items.map((item) => (
    <Card key={item.id}>...</Card>
  ))}
</div>
```

## Validation & execution

```powershell
# Install dependencies
cd frontend && npm install

# Dev server
npm run dev

# Build for production
npm run build

# Lint
npm run lint

# Type check
npx tsc --noEmit
```

## Code conventions

- **TypeScript strict mode**: No `any`, no `@ts-ignore`
- **Functional components only**: No class components
- **Named exports**: `export function ComponentName() {}`
- **File naming**: `ComponentName.tsx`, `useHookName.ts`, `service.name.ts`
- **Barrel exports**: Use `index.ts` in `components/ui/` and `types/`
- **No inline styles**: Only Tailwind utility classes
- **Props interfaces**: Define in the same file or in `types/`
- **Error boundaries**: Wrap route-level components
- **Loading states**: Always show skeleton or spinner during async operations
- **Empty states**: Always handle with a clear message and CTA
- **Accessibility**: Use semantic HTML, ARIA labels, keyboard navigation

## Checklist

- [ ] Types defined with strict TypeScript interfaces
- [ ] Service layer uses centralized Axios instance
- [ ] TanStack Query hooks wrap all API calls
- [ ] Forms use React Hook Form + Zod validation
- [ ] Responsive design with Tailwind breakpoints
- [ ] shadcn/ui components used for consistency
- [ ] No `any` types, no `@ts-ignore`
- [ ] Loading and empty states handled
- [ ] Error states displayed to user
- [ ] Accessible (semantic HTML, ARIA, keyboard nav)
- [ ] Dark mode theme applied via CSS variables
- [ ] `npm run build` passes without errors
