import { biblioteca } from "$lib/stores/biblioteca.svelte";
import { SvelteSet } from "svelte/reactivity";
class Selection {
  isActive: boolean = $state(false);
  seletedIds = new SvelteSet<string | number>();
  count: number = $derived(this.seletedIds.size);
  isSelectedAll = $state(false);
  toggleId(id) {
    if (this.seletedIds.has(id)) {
      this.seletedIds.delete(id);
      if (this.seletedIds.size == 0) {
        this.clear();
      }
      return;
    }

    this.seletedIds.add(id);
  }
  selectAll() {
    if (!this.isSelectedAll) {
      this.isSelectedAll = true;

      biblioteca.songs.forEach((el) => {
        this.seletedIds.add(el.id);
      });
    } else {
      this.isSelectedAll = false;
      selection.seletedIds.clear();
    }
  }
  clear() {
    this.seletedIds.clear();
    this.isActive = false;
    this.isSelectedAll = false
  }
  isSelected(id) {}
}

export const selection = new Selection();
