<script lang="ts">
  import { onMount } from "svelte";
  import { m } from "$lib/paraglide/messages.js";
  import { ArrowLeft, Globe as GlobeIcon } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { DialogType, ui } from "$lib/stores/ui.svelte";
  import { cargarIdiomaPreferido } from "$lib/services/stores";
  import { goto } from "$app/navigation";
  let selectedLabel = $state("");

  onMount(async () => {
    const pref = await cargarIdiomaPreferido();
    selectedLabel =
      !pref || pref === "auto"
        ? m["settings.language_auto"]()
        : pref === "es"
          ? "Español"
          : "English";
  });

  function goBack(e: MouseEvent) {
    //if (window.history.length > 1) {
      //e.preventDefault();
      //window.history.back();
    //} else {
      e.preventDefault();
      goto("/");
    //}
  }
</script>

<section
  class="h-screen w-screen overflow-y-auto overscroll-y-contain flex flex-col [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
  id="settings-view"
>
  <div
    class="w-full flex justify-between items-center px-3 flex-row py-2 shadow-md shadow-primary"
  >
    <Button variant="ghost" class="px-4 transition-all" onclick={goBack}>
      <ArrowLeft class="size-6" />
    </Button>
    <h1 class="uppercase self-center text-center text-2xl">
      {m["menus.mainmenu.settings"]()}
    </h1>
    <span class="w-8" aria-hidden="true"></span>
  </div>
  <div class="flex-1 overflow-y-auto p-3">
    <h2
      class="uppercase font-black tracking-wide text-sm text-muted-foreground mb-3"
    >
      {m["settings.general"]()}
    </h2>
    <div class="bg-card border border-border px-4 py-3 flex flex-col gap-1.5">
      <Button
        variant="outline"
        size="sm"
        class="justify-start w-full px-3"
        onclick={() => ui.openDialog(DialogType.Language)}
      >
        <GlobeIcon class="size-4 text-muted-foreground" />
        {m["settings.language"]()}
      </Button>
      <span class="pl-9 text-muted-foreground text-xs">
        {selectedLabel}
      </span>
    </div>
  </div>
</section>
