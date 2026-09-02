<script lang="ts">
  import { onMount } from "svelte";
  import { m } from "$lib/paraglide/messages.js";
  import { setLocale, type Locale } from "$lib/paraglide/runtime";
  import { Device } from "@capacitor/device";
  import { ui } from "$lib/stores/ui.svelte";
  import { cargarIdiomaPreferido, guardarIdiomaPreferido } from "$lib/services/stores";
  import Button from "../../button/button.svelte";

  type LanguageOption = "auto" | Locale;

  let options: { value: LanguageOption; label: string }[] = [
    { value: "auto", label: m["settings.language_auto"]() },
    { value: "en", label: "English" },
    { value: "es", label: "Español" },
  ];
  let selected = $state<LanguageOption>("auto");

  onMount(async () => {
    const pref = await cargarIdiomaPreferido();
    if (pref) selected = pref;
  });

  async function confirmSelection() {
    let locale: Locale;
    if (selected === "auto") {
      try {
        const { value } = await Device.getLanguageTag();
        locale = value.toLowerCase().startsWith("es") ? "es" : "en";
      } catch {
        locale = "en";
      }
    } else {
      locale = selected;
    }
    await guardarIdiomaPreferido(selected);
    setLocale(locale, { reload: true });
    ui.closeDialog();
  }
</script>

<div class="flex flex-col gap-4">
  <h3 class="font-medium text-foreground uppercase text-xl">
    {m["settings.language"]()}
  </h3>

  <fieldset class="border border-border">
    {#each options as option (option.value)}
      <label
        for={`lang-${option.value}`}
        class="p-2 flex flex-row items-center gap-3 border-b border-border last:border-b-0"
      >
        <input
          type="radio"
          id={`lang-${option.value}`}
          name="language"
          class="size-3.5 accent-primary"
          value={option.value}
          bind:group={selected}
        />
        <span>{option.label}</span>
      </label>
    {/each}
  </fieldset>

  <div class="flex justify-end gap-3 pt-2">
    <Button variant="ghost" onclick={() => ui.closeDialog()} type="button">
      {m.cancel()}
    </Button>
    <Button onclick={confirmSelection} type="button">
      {m["dialogs.accept"]()}
    </Button>
  </div>
</div>
