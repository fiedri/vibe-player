export enum DialogType {
  Playlist = 'playlist',
  CreatePlaylist = 'createPlaylist',
  Unimplemented = 'unimplemented',
  Backup = 'backup',
}

class UiStore {
  query = $state<string>("");
  
  activeDialog = $state<DialogType | null>(null);
  dialogPayload = $state<unknown>(null);

  openDialog(type: DialogType, playload?: any) {
    this.activeDialog = type;
    this.dialogPayload = playload?? null;
    window.history.pushState({ dialog: true }, "");

  }

  closeDialog() {
    this.activeDialog = null;
    this.dialogPayload = null
  }

  toggleDialog(type: DialogType) {
    this.activeDialog = this.activeDialog === type ? null : type;
  }
}

export const ui = new UiStore();
