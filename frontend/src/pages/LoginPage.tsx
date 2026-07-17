import { Building2 } from "lucide-react";
import { Button } from "@/components/ui/button";

function GoogleIcon() {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 48 48"
      className="size-5"
      aria-hidden="true"
    >
      <path
        fill="#EA4335"
        d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
      />
      <path
        fill="#4285F4"
        d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
      />
      <path
        fill="#FBBC05"
        d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
      />
      <path
        fill="#34A853"
        d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
      />
      <path fill="none" d="M0 0h48v48H0z" />
    </svg>
  );
}

export function LoginPage() {
  return (
    <div className="flex min-h-screen flex-col text-foreground">
      {/* Header */}
      <header className="flex items-center gap-3 px-6 py-5">
        <div className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <Building2 className="size-5" />
        </div>
        <span className="text-sm font-bold leading-tight">
          DAN Hotel
        </span>
      </header>

      {/* Main content */}
      <main className="flex flex-1 flex-col items-center justify-center px-4">
        <div className="w-full max-w-sm space-y-6 text-center">
          <div className="space-y-1.5">
            <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">
              Bienvenido a DAN Hotel SaaS
            </h1>
            <p className="text-sm text-balance text-muted-foreground">
              Gestiona tus reservas de forma simple y profesional.
            </p>
          </div>

          <Button
            variant="outline"
            className="w-full gap-3 border-border bg-card px-4 py-2.5 text-sm"
            aria-label="Continuar con Google para iniciar sesión"
          >
            <GoogleIcon />
            Continuar con Google
          </Button>

          <div className="space-y-0.5 text-xs text-balance text-muted-foreground">
            <p>Acceso seguro mediante protocolos estándar de Google</p>
            <p>Tus datos son privados y seguros</p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="pb-6 text-center text-xs text-muted-foreground">
        &copy; 2026 DAN Hotel SaaS
      </footer>
    </div>
  );
}
