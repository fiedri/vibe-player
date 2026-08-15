import { Item } from "$lib/services/stores";
export interface SortableStore<T = any> {
  sort(sortBy: string): void | Promise<void>;
  itemType: Item;
  availableSortOptions: Array<{ value: string; label: string }>;
}
