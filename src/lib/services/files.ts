import { CapacitorMediaStore } from "@odion-cloud/capacitor-mediastore";
import type { MediaFile } from "@odion-cloud/capacitor-mediastore";
import { registerPlugin } from "@capacitor/core";

export type { MediaFile } from "@odion-cloud/capacitor-mediastore";

export interface BatchDeleteOptions {
  files?: (string | { uri?: string; path?: string; filePath?: string })[];
  uris?: string[];
  paths?: string[];
  filePaths?: string[];
  urls?: string[];
}

export interface BatchDeleteResult {
  success: boolean;
  deletedCount: number;
  failedCount: number;
  failedFiles: string[];
  error?: string;
}

export interface MediaDeletePlugin {
  deleteFile(options: {
    uri?: string;
    path?: string;
    filePath?: string;
  }): Promise<{ success: boolean }>;

  deleteFiles(options: BatchDeleteOptions): Promise<BatchDeleteResult>;
  deleteMultipleFiles(options: BatchDeleteOptions): Promise<BatchDeleteResult>;
}
const MediaDelete = registerPlugin<MediaDeletePlugin>("MediaDelete");

export async function eliminarCancion(rutaABorrar: string) {
  try {
    const resultado = await MediaDelete.deleteFile({ uri: rutaABorrar });

    if (resultado.success) {
      console.log("¡Canción eliminada del dispositivo con éxito!");
      return true;
    }
    return false;
  } catch (error) {
    console.error("Error al intentar eliminar la canción:", error);
    throw error;
  }
}

export async function eliminarCanciones(
  rutasABorrar: string[],
): Promise<BatchDeleteResult> {
  try {
    const resultado = await MediaDelete.deleteFiles({ files: rutasABorrar });

    if (resultado.success) {
      console.log(
        `¡${resultado.deletedCount} canciones eliminadas del dispositivo con éxito!`,
      );
    } else {
      console.warn(
        `Eliminación de lote finalizada: ${resultado.deletedCount} eliminadas, ${resultado.failedCount} fallidas.`,
        resultado.failedFiles,
      );
    }
    return resultado;
  } catch (error) {
    console.error("Error al intentar eliminar el lote de canciones:", error);
    throw error;
  }
}

export async function solicitarPermisosAudio() {
  try {
    return await CapacitorMediaStore.requestPermissions({ types: ["audio"] });
  } catch (e) {
    console.warn("Error al pedir permisos:", e);
  }
}

// files.ts
export async function cargarBiblioteca(
  limit = 500,
  offset = 0,
): Promise<MediaFile[]> {
  const opciones: any = {
    mediaType: "audio",
    sortBy: "DATE",
    includeExternal: true,
  };

  // Añadimos la paginación a la consulta nativa
  if (limit > 0) opciones.limit = limit;
  if (offset > 0) opciones.offset = offset;

  const resultado = await CapacitorMediaStore.getMediasByType(opciones);
  return (
    resultado.media.map((el) => {
      return {
        ...el,
        dateModified: el.dateModified ?? Date.now()
      };
    }) || []
  );
}
