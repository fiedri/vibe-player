import { QueueManager } from "../subsystem/queue.svelte";
import type { MediaFile } from "$lib/types/songs";

export abstract class ModeState {
  protected queueContext!: QueueManager;

  public setContext(context: QueueManager) {
    this.queueContext = context;
  }
public nextHelper(): MediaFile | null {
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
    this.queueContext.currentSongIndex++
    return nextSong;
}
public previousHelper(): MediaFile | null {
    if (
      this.queueContext.currentSongIndex === null ||
      this.queueContext.currentSongIndex == -1
    )
      return null;
    if (this.queueContext.currentSongIndex - 1 < 0) return null;
    const previousSong =
      this.queueContext.queue[this.queueContext.currentSongIndex - 1];
    this.queueContext.currentSong = previousSong;
    this.queueContext.currentSongIndex--
    return previousSong;
}
  public abstract next(): MediaFile | null;
  public abstract previous(): MediaFile | null;
  public abstract handleTrackEndednext(): MediaFile | null;
}

export class RepeatOffmode extends ModeState {
  public next(): MediaFile | null {
return this.nextHelper()
  }
  public previous(): MediaFile | null {
return this.previousHelper()
  }
  public handleTrackEndednext(): MediaFile | null {
    return this.nextHelper();
  }
}

export class RepeatOneMode extends ModeState {
  public next(): MediaFile | null {
    return this.nextHelper();
  }
  public previous(): MediaFile | null {
    return this.previousHelper()
  }
  public handleTrackEndednext(): MediaFile | null {
    return this.queueContext.currentSong;
  }
}

export class RepeatAllMode extends ModeState{
  public next(): MediaFile | null {
    if (this.queueContext.currentSongIndex !== null && (this.queueContext.queue.length-1) <= this.queueContext.currentSongIndex) {
        this.queueContext.currentSong = this.queueContext.queue[0];
        this.queueContext.currentSongIndex = 0
        return this.queueContext.currentSong;
      }
    return this.nextHelper();
  }
  public previous(): MediaFile | null {
    return this.previousHelper()
  }
  public handleTrackEndednext(): MediaFile | null {
      return this.next()
  }
}
