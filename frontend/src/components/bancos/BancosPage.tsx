import { BancosSection } from "./BancosSection";

export function BancosPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-foreground">Bancos</h1>
        <p className="text-sm text-muted-foreground">
          Gestiona los bancos del sistema
        </p>
      </div>
      <BancosSection />
    </div>
  );
}
