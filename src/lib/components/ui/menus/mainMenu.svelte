<script lang="ts">

import {m} from '$lib/paraglide/messages.js';
  import {
    cargarParametrosDeOrdenamiento,
    guardarParametrosDeOrdenamiento,
  } from "$lib/services/stores";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { Renew, ChevronRight } from "carbon-icons-svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  let { isOpen, onClose, activeStore } = $props();
  let openSortByMenu = $state(false);
  let isLoaded = $state(false);

  let orderByObject = $state({
    parameter: "date",
    orderDir: "Desc",
  });

  let currentItemType = $state("");
  let lastSortKey = "";

  onMount(async () => {
    if (!activeStore) return;
    currentItemType = activeStore.itemType;
    const cargado = await cargarParametrosDeOrdenamiento(activeStore.itemType);
    if (cargado && typeof cargado === "object") {
      orderByObject = cargado;
      activeStore.sort(orderByObject.parameter + orderByObject.orderDir);
      lastSortKey = orderByObject.parameter + orderByObject.orderDir;
    }
    isLoaded = true;
  });

  $effect(() => {
    if (!activeStore || !isLoaded) return;

    const type = activeStore.itemType;
    const sortKey = orderByObject.parameter + orderByObject.orderDir;

    if (type !== currentItemType) {
      currentItemType = type;
      cargarParametrosDeOrdenamiento(type).then((cargado) => {
        if (cargado && typeof cargado === "object") {
          orderByObject = cargado;
        }
        const newKey = orderByObject.parameter + orderByObject.orderDir;
        lastSortKey = newKey;
        activeStore.sort(newKey);
        onClose();
      });
      return;
    }

    if (sortKey === lastSortKey) return;
    lastSortKey = sortKey;

    guardarParametrosDeOrdenamiento(
      {
        parameter: orderByObject.parameter,
        orderDir: orderByObject.orderDir,
      },
      type,
    ).then(() => {
      openSortByMenu = false;
      activeStore.sort(sortKey);
      onClose();
    });
  });

  let overflowMenu = $derived([
    {
      title: m['menus.home_overflow_menu.refresh_library'](),
      action: () => {
        biblioteca.refresh();
        onClose();
      },
      icon: Renew,
    },
...(activeStore?
    [{
      title: m['menus.home_overflow_menu.order_by'](),
      action: () => {
        openSortByMenu = !openSortByMenu;
      },
      icon: ChevronRight,
    }] : []
)
  ]);
</script>

{#if isOpen}
  <div
    class="absolute top-full w-50 right-4 h-auto bg-popover border border-border z-[9999]"
  >
    {#each overflowMenu as options}
      <button
        class="p-2 flex flex-row items-center gap-3 border-b border-border w-full {openSortByMenu &&
        options.title == 'Ordenar Por:'
          ? 'text-muted-foreground'
          : ''}"
        onclick={() => {
          options.action();
        }}
      >
        <options.icon
          class={openSortByMenu && options.title == "Ordenar Por:"
            ? "rotate-90 transition-all duration-300"
            : ""}
        />
        {options.title}
      </button>
    {/each}
    {#if openSortByMenu}
      <div transition:slide>
        <form action="" class="flex flex-col justify-end">
          <fieldset>
            {#each activeStore.availableSortOptions as option}
              <label
                for={option.value}
                class="p-2 flex flex-row items-center gap-3 border-b border-border"
              >
                <input
                  type="radio"
                  id={option.value}
                  name="order"
                  class="size-3"
                  value={option.value}
                  bind:group={orderByObject.parameter}
                /><span>{option.label}</span>
              </label>
            {/each}
          </fieldset>

          <fieldset class="border-t-3 border-border">
            <label
              for="ascendente"
              class="p-2 flex flex-row items-center gap-3 border-b border-border"
            >
              <input
                type="radio"
                id="ascendente"
                value="Asc"
                name="orderDir"
                class="size-3"
                bind:group={orderByObject.orderDir}
              /><span>{m['menus.home_overflow_menu.order_by_options.asc']()}</span>
            </label>
            <label
              for="descendente"
              class="p-2 flex flex-row items-center gap-3 border-b border-border"
            >
              <input
                type="radio"
                id="descendente"
                name="orderDir"
                value="Desc"
                class="size-3"
                bind:group={orderByObject.orderDir}
              /><span>{m['menus.home_overflow_menu.order_by_options.desc']()}</span>
            </label>
          </fieldset>
        </form>
      </div>
    {/if}
  </div>
{/if}
