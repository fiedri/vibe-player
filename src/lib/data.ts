import { colors } from "./colors";

export interface Playlist {
  id: string;
  albumId: number;
  title: string;
  color: (typeof colors)[keyof typeof colors];
  cover: string;
  artists: string[];
}

export const playlists: Playlist[] = [
  {
    id: '1',
    albumId: 1,
    title: "Chill Lo-Fi Beats",
    color: colors.yellow,
    cover: "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=60",
    artists: ["Ketsa", "FASSounds"],
  },
  {
    id: '2',
    albumId: 2,
    title: "Synthwave Dreams",
    color: colors.purple,
    cover: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=60",
    artists: ["ARPMedia", "echoes of lumen"],
  },
  {
    id: '3',
    albumId: 3,
    title: "Acoustic Reflections",
    color: colors.indigo,
    cover: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=60",
    artists: ["dferun"],
  },
  {
    id: '4',
    albumId: 4,
    title: "Ambient & Focus",
    color: colors.blue,
    cover: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop&q=60",
    artists: ["Morgan ambient", "PaulYudin"],
  },
  {
    id: '5',
    albumId: 5,
    title: "Jazz Hop Session",
    color: colors.black,
    cover: "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=500&auto=format&fit=crop&q=60",
    artists: ["NerdWorld", "AberrantRealitiesh"],
  },
  {
    id: '6',
    albumId: 6,
    title: "Indie Rock Drive",
    color: colors.teal,
    cover: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
    artists: ["AlexGrohl", "PaulYudin"],
  },
];

export const morePlaylists = playlists.map((item) => ({
  ...item,
  id: item.id + "_more",
}));

export const sidebarPlaylists = playlists.map((item) => ({
  ...item,
  id: item.id + "_side",
}));

export const allPlaylists = [
  ...playlists,
  ...morePlaylists,
  ...sidebarPlaylists,
];

export interface Song {
  id: number;
  albumId: number;
  title: string;
  image: string;
  artists: string[];
  album: string;
  duration: string;
  audioUrl: string;
}

export const songs: Song[] = [
  // Album 1: Chill Lo-Fi Beats
  {
    id: 1,
    albumId: 1,
    title: "Day Trips",
    image: "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=60",
    artists: ["Ketsa"],
    album: "Chill Lo-Fi Beats",
    duration: "3:02",
    audioUrl: "/music/1/1.mp3"
  },
  {
    id: 2,
    albumId: 1,
    title: "Good Night",
    image: "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=60",
    artists: ["FASSounds"],
    album: "Chill Lo-Fi Beats",
    duration: "2:27",
    audioUrl: "/music/1/2.mp3"
  },
  {
    id: 3,
    albumId: 1,
    title: "Lofi Study",
    image: "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=60",
    artists: ["FASSounds"],
    album: "Chill Lo-Fi Beats",
    duration: "2:27",
    audioUrl: "/music/1/3.mp3"
  },
  // Album 2: Synthwave Dreams
  {
    id: 1,
    albumId: 2,
    title: "Neon Synthwave Vibe",
    image: "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=60",
    artists: ["echoes of lumen"],
    album: "Synthwave Dreams",
    duration: "2:07",
    audioUrl: "/music/2/1.mp3"
  },



  {
    id: 2,
    albumId: 2,
    title: "Sinthwave",
    image: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=60",
    artists: ["ARPMedia"],
    album: "Synthwave Dreams",
    duration: "2:50",
    audioUrl: "/music/2/2.mp3"
  },
  {
    id: 3,
    albumId: 2,
    title: "Synthwave Retro 80s",
    image: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=60",
    artists: ["ARPMedia"],
    album: "Synthwave Dreams",
    duration: "2:43",
    audioUrl: "/music/2/3.mp3"
  },

  // Album 3: Acoustic Reflections
  {
    id: 1,
    albumId: 3,
    title: "melancolic_2",
    image: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=60",
    artists: ["dferun"],
    album: "Acoustic Reflections",
    duration: "2:36",
    audioUrl: "/music/3/1.mp3"
  },

  // Album 4: Ambient & Focus
  {
    id: 1,
    albumId: 4,
    title: "Calm Ambient Dreamscape",
    image: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop&q=60",
    artists: ["Morgan ambient"],
    album: "Ambient & Focus",
    duration: "3:42",
    audioUrl: "/music/4/1.mp3"
  },  {
    id: 2,
    albumId: 4,
    title: "Ambient",
    image: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop&q=60",
    artists: ["PaulYudin"],
    album: "Ambient & Focus",
    duration: "2:09",
    audioUrl: "/music/4/2.mp3"
  },

  // Album 5: Jazz Hop Session
  {
    id: 1,
    albumId: 5,
    title: "Upbeat Hip-hop Jazz",
    image: "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=500&auto=format&fit=crop&q=60",
    artists: ["NerdWorld"],
    album: "Jazz Hop Session",
    duration: "2:21",
    audioUrl: "/music/5/1.mp3"
  },
  {
    id: 2,
    albumId: 5,
    title: "Floating Valley",
    image: "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=500&auto=format&fit=crop&q=60",
    artists: ["AberrantRealitiesh"],
    album: "Jazz Hop Session",
    duration: "2:59",
    audioUrl: "/music/5/2.mp3"
  },
  // Album 6: Indie Rock Drive
  {
    id: 1,
    albumId: 6,
    title: "Motivation Epic Rock",
    image: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
    artists: ["AlexGrohl"],
    album: "Indie Rock Drive",
    duration: "1:59",
    audioUrl: "/music/6/1.mp3"
  },
    {
    id: 2,
    albumId: 6,
    title: "Electronic Stylish Rock",
    image: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
    artists: ["AlexGrohl"],
    album: "Indie Rock Drive",
    duration: "1:50",
    audioUrl: "/music/6/2.mp3"
  },
    {
    id: 3,
    albumId: 6,
    title: "Rock",
    image: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
    artists: ["PaulYudin"],
    album: "Indie Rock Drive",
    duration: "1:59",
    audioUrl: "/music/6/3.mp3"
  },
    {
    id: 4,
    albumId: 6,
    title: "Motivation Sport Rock Trailer",
    image: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
    artists: ["AlexGrohl"],
    album: "Indie Rock Drive",
    duration: "2:08",
    audioUrl: "/music/6/4.mp3"
  }
];
