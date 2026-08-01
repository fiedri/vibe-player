class UiStore{
 query = $state<string>("");
 isOpenDialog = $state<boolean>(false);

 handleDialog(){
  this.isOpenDialog = !this.isOpenDialog
  }
}


export const ui = new UiStore()
