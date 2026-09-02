<script lang="ts">
import {m} from '$lib/paraglide/messages.js';
  import Button from "../button/button.svelte";
  import { ui } from "$lib/stores/ui.svelte";
  import {
    exportPlaylistsBackup,
    importPlaylistsBackup,
  } from "$lib/db/backup";
  import { copyToClipboard } from "$lib/utils/clipboard";

  let importText = $state("");
  let status = $state<{ ok: boolean; message: string } | null>(null);
  let busy = $state(false);

  async function handleExport(e: Event) {
    e.preventDefault();
    busy = true;
    status = null;
    try {
      const json = await exportPlaylistsBackup();
      await copyToClipboard(json);
      status = { ok: true, message: m['playlist.backup.copied']() };
    } catch (err) {
      status = { ok: false, message: errorMessage(err) };
    } finally {
      busy = false;
    }
  }

  async function handleImport(e: Event) {
    e.preventDefault();
    if (!importText.trim()) return;
    busy = true;
    status = null;
    try {
      const count = await importPlaylistsBackup(importText);
      status = {
        ok: true,
        message: m['playlist.backup.restore_result']({count}),
      };
      importText = "";
    } catch (err) {
      status = { ok: false, message: errorMessage(err) };
    } finally {
      busy = false;
    }
  }

  function errorMessage(err: unknown): string {
    return err instanceof Error ? err.message : "Ocurrió un error inesperado.";
  }
</script>

<div class="flex flex-col gap-4">
  <h2 class="font-medium text-foreground uppercase text-xl">Playlists</h2>
  <p class="text-sm text-muted-foreground">
    {m['playlist.backup.explanation']()}
  </p>

  <div>
    <Button onclick={handleExport} disabled={busy}>{m['playlist.backup.export']()}</Button>
    <p class="text-xs text-muted-foreground">{m['playlist.backup.copy_json']()}</p>
  </div>

  <label for="backup-import" class="font-medium text-foreground">{m['playlist.backup.restore']()}</label>
  <textarea
    id="backup-import"
    bind:value={importText}
    placeholder='{m['playlist.backup.placeholder']()}'
    rows={5}
    class="w-full bg-background px-2 py-2 text-foreground border border-border outline-none focus:outline-none focus:ring-0 focus:border-primary"
  ></textarea>

  <div class="flex justify-end gap-3">
    <Button variant="ghost" onclick={() => ui.closeDialog()} type="button"
      >{m.cancel()}</Button
    >
    <Button
      onclick={handleImport}
      type="button"
      disabled={busy || importText.trim() === ""}
      >{m['playlist.backup.restore']()}</Button
    >
  </div>

  {#if status}
    <p class:text={status.ok ? "text-primary" : "text-destructive"}>{status.message}</p>
  {/if}
</div>
