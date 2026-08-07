import { SvelteSet } from "svelte/reactivity";
class Selection {
  isActive: boolean = $state(false);
  seletedIds = new SvelteSet<string | number>();
  count: number = $derived(this.seletedIds.size);
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
  selectAll(id) {}
  clear() {
    this.seletedIds.clear();
    this.isActive = false;
  }
  isSelected(id) {}
}

export const selection = new Selection();
