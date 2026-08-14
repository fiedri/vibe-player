<script lang="ts">
  import { ui } from "$lib/stores/ui.svelte";
  import type { MediaFile } from "@odion-cloud/capacitor-mediastore";
  import {
    displayTitle,
    displayArtist,
    displayAlbum,
    displayImage,
    DEFAULT_COVER,
  } from "$lib/types/songs";
  import { formatearMS } from "$lib/utils";
  import Button from "../button/button.svelte";
  import {
    Music,
    VolumeUp,
    Time,
    Folder,
    Copy,
    Checkmark,
  } from "carbon-icons-svelte";

  let info = $derived((ui.dialogPayload) as MediaFile | null);

  let copied = $state(false);
  let copyTimeout: ReturnType<typeof setTimeout> | null = null;
  let imgError = $state(false);

  async function handleCopyPath() {
    if (!info?.uri) return;
    try {
      await navigator.clipboard.writeText(info.uri);
      copied = true;
      if (copyTimeout) clearTimeout(copyTimeout);
      copyTimeout = setTimeout(() => {
        copied = false;
      }, 2000);
    } catch (err) {
      console.error("Error al copiar la ruta:", err);
    }
  }

  function formatBytes(bytes?: number): string {
    if (!bytes || bytes <= 0) return "Desconocido";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  }

  function formatDate(timestamp?: number): string {
    if (!timestamp) return "Desconocida";
    try {
      const d = new Date(timestamp);
      if (isNaN(d.getTime())) return "Desconocida";
      return d.toLocaleDateString("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
    } catch {
      return "Desconocida";
    }
  }

  function formatBitrate(bitrate?: number): string | null {
    if (!bitrate || bitrate <= 0) return null;
    const kbps =
      bitrate > 1000 ? Math.round(bitrate / 1000) : Math.round(bitrate);
    return `${kbps} kbps`;
  }

  function formatSampleRate(sampleRate?: number): string | null {
    if (!sampleRate || sampleRate <= 0) return null;
    return `${(sampleRate / 1000).toFixed(1)} kHz`;
  }

  function formatChannels(channels?: number): string | null {
    if (!channels) return null;
    if (channels === 1) return "Mono (1 ch)";
    if (channels === 2) return "Estéreo (2 ch)";
    return `${channels} canales`;
  }

  function cleanMimeType(mime?: string): string {
    if (!mime) return "Audio";
    if (mime.includes("mpeg") || mime.includes("mp3")) return "Audio MP3";
    if (mime.includes("flac")) return "Audio FLAC";
    if (mime.includes("aac") || mime.includes("m4a")) return "Audio AAC";
    if (mime.includes("ogg")) return "Audio OGG";
    if (mime.includes("wav")) return "Audio WAV";
    return mime;
  }
</script>

{#if !info}
  <div class="flex flex-col items-center justify-center p-6 text-center gap-3">
    <p class="text-muted-foreground text-sm">No hay información disponible.</p>
    <Button onclick={() => ui.closeDialog()}>Cerrar</Button>
  </div>
{:else}
  <div class="flex flex-col gap-4 max-h-[80vh] overflow-y-auto pr-1">
    <div
      class="flex items-center gap-4 p-3 bg-secondary/30 border border-border/50"
    >
      <div
        class="relative size-20 shrink-0 overflow-hidden bg-card border border-border flex items-center justify-center shadow-md"
      >
        <img
          src={imgError ? DEFAULT_COVER : displayImage(info)}
          alt={displayTitle(info)}
          class="h-full w-full object-cover"
          onerror={() => (imgError = true)}
        />
      </div>

      <div class="flex flex-col min-w-0 flex-1 justify-center gap-1 py-0.5">
        <h2
          class="text-base font-bold text-foreground leading-snug truncate"
          title={displayTitle(info)}
        >
          {displayTitle(info)}
        </h2>
        <p
          class="text-xs font-medium text-muted-foreground truncate"
          title={displayArtist(info)}
        >
          {displayArtist(info)}
        </p>
        <p
          class="text-xs text-muted-foreground/80 truncate"
          title={displayAlbum(info)}
        >
          {displayAlbum(info)}
        </p>

        {#if info.duration}
          <div
            class="mt-1 flex items-center gap-1.5 text-xs text-primary font-mono font-semibold"
          >
            <Time class="size-3.5" />
            <span>{formatearMS(info.duration)}</span>
          </div>
        {/if}
      </div>
    </div>

    <!-- Metadata Section 1: Canción & Detalles -->
    <div class="flex flex-col gap-2">
      <h3
        class="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5 px-0.5"
      >
        <Music class="size-3.5 text-primary" />
        Información de la Canción
      </h3>

      <div
        class="grid grid-cols-1 gap-2 text-xs bg-card/60 p-3 border border-border/40"
      >
        <div class="flex flex-col gap-0.5">
          <span class="text-muted-foreground font-medium">Título:</span>
          <span class="text-foreground font-medium select-text break-words"
            >{displayTitle(info)}</span
          >
        </div>

        <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
          <span class="text-muted-foreground font-medium">Artista:</span>
          <span class="text-foreground font-medium select-text break-words"
            >{displayArtist(info)}</span
          >
        </div>

        <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
          <span class="text-muted-foreground font-medium">Álbum:</span>
          <span class="text-foreground font-medium select-text break-words"
            >{displayAlbum(info)}</span
          >
        </div>

        {#if info.albumArtist}
          <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
            <span class="text-muted-foreground font-medium"
              >Artista del Álbum:</span
            >
            <span class="text-foreground select-text break-words"
              >{info.albumArtist}</span
            >
          </div>
        {/if}

        {#if info.composer}
          <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
            <span class="text-muted-foreground font-medium">Compositor:</span>
            <span class="text-foreground select-text break-words"
              >{info.composer}</span
            >
          </div>
        {/if}

        {#if info.genre}
          <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
            <span class="text-muted-foreground font-medium">Género:</span>
            <span class="text-foreground select-text">{info.genre}</span>
          </div>
        {/if}

        {#if info.year || info.track}
          <div class="grid grid-cols-2 gap-2 border-t border-border/20 pt-2">
            {#if info.year}
              <div class="flex flex-col gap-0.5">
                <span class="text-muted-foreground font-medium">Año:</span>
                <span class="text-foreground font-mono">{info.year}</span>
              </div>
            {/if}

            {#if info.track}
              <div class="flex flex-col gap-0.5">
                <span class="text-muted-foreground font-medium"
                  >N.º de pista:</span
                >
                <span class="text-foreground font-mono">{info.track}</span>
              </div>
            {/if}
          </div>
        {/if}
      </div>
    </div>

    <!-- Metadata Section 2: Formato & Calidad -->
    <div class="flex flex-col gap-2">
      <h3
        class="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5 px-0.5"
      >
        <VolumeUp class="size-3.5 text-primary" />
        Formato y Calidad
      </h3>

      <div
        class="grid grid-cols-2 gap-2 text-xs bg-card/60 p-3 border border-border/40"
      >
        <div class="flex flex-col gap-0.5">
          <span class="text-muted-foreground font-medium">Tipo MIME:</span>
          <span class="text-foreground font-mono"
            >{cleanMimeType(info.mimeType)}</span
          >
        </div>

        <div class="flex flex-col gap-0.5">
          <span class="text-muted-foreground font-medium">Tamaño:</span>
          <span class="text-foreground font-mono"
            >{formatBytes(info.size)}</span
          >
        </div>

        {#if formatBitrate(info.bitrate)}
          <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
            <span class="text-muted-foreground font-medium">Tasa de bits:</span>
            <span class="text-foreground font-mono"
              >{formatBitrate(info.bitrate)}</span
            >
          </div>
        {/if}

        {#if formatSampleRate(info.sampleRate)}
          <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
            <span class="text-muted-foreground font-medium">Muestreo:</span>
            <span class="text-foreground font-mono"
              >{formatSampleRate(info.sampleRate)}</span
            >
          </div>
        {/if}

        {#if formatChannels(info.channels)}
          <div
            class="flex flex-col gap-0.5 border-t border-border/20 pt-2 col-span-2"
          >
            <span class="text-muted-foreground font-medium">Canales:</span>
            <span class="text-foreground font-mono"
              >{formatChannels(info.channels)}</span
            >
          </div>
        {/if}
      </div>
    </div>

    <!-- Metadata Section 3: Archivo y Ubicación -->
    <div class="flex flex-col gap-2">
      <h3
        class="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5 px-0.5"
      >
        <Folder class="size-3.5 text-primary" />
        Archivo y Ubicación
      </h3>

      <div
        class="flex flex-col gap-2 text-xs bg-card/60  p-3 border border-border/40"
      >
        <div class="flex flex-col gap-0.5">
          <span class="text-muted-foreground font-medium"
            >Nombre de archivo:</span
          >
          <span class="text-foreground font-mono select-text break-all"
            >{info.displayName || "Desconocido"}</span
          >
        </div>

        <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
          <span class="text-muted-foreground font-medium"
            >Almacenamiento:</span
          >
          <span class="text-foreground font-mono">
            {info.isExternal
              ? "Tarjeta SD / Externo"
              : "Almacenamiento Interno"}
          </span>
        </div>

        {#if info.dateModified}
          <div class="flex flex-col gap-0.5 border-t border-border/20 pt-2">
            <span class="text-muted-foreground font-medium"
              >Fecha de modificación:</span
            >
            <span class="text-foreground font-mono"
              >{formatDate(info.dateModified)}</span
            >
          </div>
        {/if}

        {#if info.uri}
          <div class="flex flex-col gap-1 border-t border-border/20 pt-2">
            <div class="flex items-center justify-between">
              <span class="text-muted-foreground font-medium font-mono"
                >Ruta / URI:</span
              >
              <button
                type="button"
                onclick={handleCopyPath}
                class="flex items-center gap-1 text-[11px] font-medium text-primary hover:underline cursor-pointer"
                title="Copiar ruta al portapapeles"
              >
                {#if copied}
                  <Checkmark class="size-3 text-green-400" />
                  <span class="text-green-400 font-semibold">¡Copiado!</span>
                {:else}
                  <Copy class="size-3" />
                  <span>Copiar</span>
                {/if}
              </button>
            </div>
            <p
              class="text-[11px] font-mono text-muted-foreground/90 bg-background/50 p-2 border border-border/30 break-all select-text leading-tight"
            >
              {info.uri}
            </p>
          </div>
        {/if}
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="flex justify-end pt-2">
      <Button class="w-full sm:w-auto px-6" onclick={() => ui.closeDialog()}>
        Aceptar
      </Button>
    </div>
  </div>
{/if}

