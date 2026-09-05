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

export interface MediaSharePlugin{
  share(options:{uri: string}): Promise<void>;
  shareMultiple(options: {uris: string[]}): Promise<void>
}

class FileService {
  private mediaDelete = registerPlugin<MediaDeletePlugin>("MediaDelete");
  private mediaShare = registerPlugin<MediaSharePlugin>("MediaShare")
  async eliminarCancion(rutaABorrar: string) {
    try {
      const resultado = await this.mediaDelete.deleteFile({ uri: rutaABorrar });

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

  async eliminarCanciones(
    rutasABorrar: string[],
  ): Promise<BatchDeleteResult> {
    try {
      const resultado = await this.mediaDelete.deleteFiles({ files: rutasABorrar });

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

  async solicitarPermisosAudio() {
    try {
      return await CapacitorMediaStore.requestPermissions({ types: ["audio"] });
    } catch (e) {
      console.warn("Error al pedir permisos:", e);
    }
  }

  async cargarBiblioteca(
    limit = 500,
    offset = 0,
  ): Promise<MediaFile[]> {
    const opciones: any = {
      mediaType: "audio",
      sortBy: "DATE",
      includeExternal: true,
    };

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
  async share(uri: string){
    await this.mediaShare.share({uri})
  }
  async shareMultiple(uris: string[]){
    await this.mediaShare.shareMultiple({uris})
  }
}

export const fileService = new FileService();
