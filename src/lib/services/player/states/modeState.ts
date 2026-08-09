import { QueueManager } from "../subsystem/queue.svelte";
import type { Song } from "$lib/types/songs";

export abstract class ModeState {
  protected queueContext!: QueueManager;

  public setContext(context: QueueManager) {
    this.queueContext = context;
  }
public nextHelper(): Song | null {
      if (
      this.queueContext.currentSongIndex === null ||
      this.queueContext.currentSongIndex == -1
    )
      return null;
    if (
      this.queueContext.currentSongIndex >=
      this.queueContext.queue.length - 1
    )
      return null;
    const nextSong =
      this.queueContext.queue[this.queueContext.currentSongIndex + 1];
    this.queueContext.currentSong = nextSong;
    return nextSong;
}
public previousHelper(): Song | null {
    if (
      this.queueContext.currentSongIndex === null ||
      this.queueContext.currentSongIndex == -1
    )
      return null;
    if (this.queueContext.currentSongIndex - 1 < 0) return null;
    const previousSong =
      this.queueContext.queue[this.queueContext.currentSongIndex - 1];
    this.queueContext.currentSong = previousSong;
    return previousSong;
}
  public abstract next(): Song | null;
  public abstract previous(): Song | null;
  public abstract handleTrackEndednext(): Song | null;
}

export class RepeatOffmode extends ModeState {
  public next(): Song | null {
return this.nextHelper()
  }
  public previous(): Song | null {
return this.previousHelper()
  }
  public handleTrackEndednext(): Song | null {
    return this.nextHelper();
  }
}

export class RepeatOneMode extends ModeState {
  public next(): Song | null {
    return this.nextHelper();
  }
  public previous(): Song | null {
    return this.previousHelper()
  }
  public handleTrackEndednext(): Song | null {
    return this.queueContext.currentSong;
  }
}

export class RepeatAllMode extends ModeState{
  public next(): Song | null {
    if ((this.queueContext.queue.length-1) <= this.queueContext.currentSongIndex) {
        this.queueContext.currentSong = this.queueContext.queue[0];
        return this.queueContext.currentSong;
      }
    return this.nextHelper();
  }
  public previous(): Song | null {
    return this.previousHelper()
  }
  public handleTrackEndednext(): Song | null {
      return this.next()
  }
}
