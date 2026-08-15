export type SortStrategy = (a: any, b: any) => number;

export const SortStrategies: Record<string, SortStrategy> = {
  titleAsc: (a, b) => a.title.localeCompare(b.title),
  titleDesc: (a, b) => b.title.localeCompare(a.title),
  durationAsc: (a, b) => a.duration - b.duration,
  durationDesc: (a, b) => b.duration - a.duration,
  dateAsc: (a, b) => a.dateAdded - b.dateAdded,
  dateDesc: (a,b)=> b.dateAdded - a.dateAdded,
  artistAsc: (a,b)=> a.artist.localeCompare(b.artist),
  artistDesc: (a,b)=> b.artist.localeCompare(a.artist),
nameAsc: (a,b) => a.name.localeCompare(b.name),
nameDesc: (a,b) => b.name.localeCompare(a.name),
songCountAsc: (a,b) => a.songCount - b.songCount,
songCountDesc: (a,b) => b.songCount - a.songCount,
};
