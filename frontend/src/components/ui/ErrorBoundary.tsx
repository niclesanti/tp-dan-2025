import { Component, type ReactNode } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "./button";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    if (import.meta.env.DEV) {
      console.error("ErrorBoundary caught:", error, info);
    }
  }

  handleRetry = () => {
    this.setState({ hasError: false });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center h-full py-20 gap-4">
          <AlertTriangle className="size-12 text-destructive" />
          <div className="text-center space-y-1">
            <h2 className="text-lg font-semibold text-foreground">
              Algo salió mal
            </h2>
            <p className="text-sm text-muted-foreground">
              Ocurrió un error inesperado al cargar esta sección.
            </p>
          </div>
          <Button variant="outline" size="sm" onClick={this.handleRetry}>
            Reintentar
          </Button>
        </div>
      );
    }

    return this.props.children;
  }
}
