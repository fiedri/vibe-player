import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function formatearMS(ms: number): string {
  const segundosTotales = Math.floor(ms / 1000);
  const horas = Math.floor(segundosTotales / 3600);
  const minutos = Math.floor((segundosTotales % 3600) / 60);
  const segundos = segundosTotales % 60;

  if (horas > 0) {
    return `${String(horas).padStart(2, "0")}:${String(minutos).padStart(2, "0")}:${String(segundos).padStart(2, "0")}`;
  }
  return `${String(minutos).padStart(2, "0")}:${String(segundos).padStart(2, "0")}`;
}

export function sumarDuracionesCanciones(songs: { duration?: number }[]): number {
  return songs.reduce((acc, song) => acc + (song.duration ?? 0), 0);
}

export function formatearDuracionTotal(songs: { duration?: number }[]): string {
  const totalMs = sumarDuracionesCanciones(songs);
  return formatearMS(totalMs);
}

// Ahora srcWeb será algo como: "http://localhost/_capacitor_file_/storage/emulated/0/Music/cancion.mp3"
export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}
export function lerp(min: number, max: number, t: number) {
  return min + (max - min) * t;
}
export const clamp01 = (t: number) => Math.max(0, Math.min(1, t));

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type WithoutChild<T> = T extends { child?: any } ? Omit<T, "child"> : T;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type WithoutChildren<T> = T extends { children?: any } ? Omit<T, "children"> : T;
export type WithoutChildrenOrChild<T> = WithoutChildren<WithoutChild<T>>;
export type WithElementRef<T, U extends HTMLElement = HTMLElement> = T & { ref?: U | null };
