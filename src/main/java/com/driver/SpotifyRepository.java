package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        return  user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist curr_art = null;
        for(Artist a : artists){
            if(a.getName().equals(artistName)){
                curr_art = a;
                break;
            }
        }
        if(curr_art == null){
            curr_art = new Artist(artistName);
            artists.add(curr_art);
        }

            Album album = new Album(title);
            albums.add(album);
            List<Album> curr_albums = new ArrayList<>();
            curr_albums.add(album);
            artistAlbumMap.put(curr_art,curr_albums);
        return  album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album curr_album = null;

       for(Album a : albums){
           if(a.getTitle().equals(albumName)){
               curr_album =  a;
               break;
           }
       }
       if(curr_album == null){
           throw new Exception("Album does not exist");
       }
       else {
           Song song = new Song(title, length);
           songs.add(song);                    //chk 2
           List<Song> temp_song = new ArrayList<>();
           if (albumSongMap.containsKey(curr_album)) {
               temp_song = albumSongMap.get(curr_album);
           }
           temp_song.add(song);
           albumSongMap.put(curr_album, temp_song);
           return song;
       }
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {

         User user = null;
         for(User u: users){
             if(u.getMobile().equals(mobile)){
                 user = u;
                 break;
             }
         }
         if(user == null){
             throw  new Exception("User does not exist");
         }
         else {
             Playlist playlist = new Playlist(title);
             playlists.add(playlist);
             List<Song> song_list = new ArrayList<>();
             for (Song s : songs) {
                 if (s.getLength() == length) {
                     song_list.add(s);
                 }
             }
             playlistSongMap.put(playlist, song_list);

             creatorPlaylistMap.put(user, playlist);

         List<User> userList = new ArrayList<>();
         if(playlistListenerMap.containsKey(playlist)){
             userList = playlistListenerMap.get(playlist);
         }
         userList.add(user);
         playlistListenerMap.put(playlist,userList);

             List<Playlist> playlistList = new ArrayList<>();
             if (userPlaylistMap.containsKey(user)) {
                 playlistList = userPlaylistMap.get(user);
             }
             playlistList.add(playlist);
             userPlaylistMap.put(user, playlistList);

             return playlist;
         }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = null;
        for(User u: users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        if(user == null){
            throw  new Exception("User does not exist");
        }
        else{
            Playlist playlist = new Playlist(title);
            playlists.add(playlist);
            List<Song> song_list = new ArrayList<>();
            for (Song s : songs) {
                if (songTitles.contains(s.getTitle())) {
                    song_list.add(s);
                }
            }
            playlistSongMap.put(playlist, song_list);

            creatorPlaylistMap.put(user, playlist);
            List<User> userList = new ArrayList<>();
            if(playlistListenerMap.containsKey(playlist)){
                userList = playlistListenerMap.get(playlist);
            }
            userList.add(user);
            playlistListenerMap.put(playlist,userList);

            List<Playlist> playlistList = new ArrayList<>();
            if (userPlaylistMap.containsKey(user)) {
                playlistList = userPlaylistMap.get(user);
            }
            playlistList.add(playlist);
            userPlaylistMap.put(user, playlistList);

            return playlist;
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = null;
        for(User u: users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }


        Playlist playlist = null;
            for(Playlist p : playlists){
                if(p.getTitle().equals(playlistTitle)){
                    playlist =  p;
                    break;
                }
            }
        if(user == null){
            throw  new Exception("User does not exist");
        }
        else if(playlist==null)
                throw new Exception("Playlist does not exist");
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating
           else {
            if (creatorPlaylistMap.containsKey(user)) {
                return playlist;
            }
            List<User> user_list = playlistListenerMap.get(playlist);
            for (User u : user_list) {
                if (u.equals(user))
                    return playlist;
            }

            user_list.add(user);
            playlistListenerMap.put(playlist, user_list);

            List<Playlist> play_list = new ArrayList<>();
            if(userPlaylistMap.containsKey(user)){
                 play_list = userPlaylistMap.get(user);
             }
            if(!play_list.contains(playlist)) play_list.add(playlist);
            userPlaylistMap.put(user,play_list);



            return playlist;
        }

    }

    //The user likes the given song. The corresponding artist of the song gets auto-liked
    //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
    //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
    //If the user does not exist, throw "User does not exist" exception
    //If the song does not exist, throw "Song does not exist" exception
    //Return the song after updating
    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = null;
        for(User u: users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        Song song = null;
        for(Song s: songs){
            if(s.getTitle().equals(songTitle)){
                song = s;
                break;
            }
        }
        if(user == null){
            throw  new Exception("User does not exist");
        }
        else if(song == null){
            throw  new Exception("Song does not exist");
        }
        else{
            Album album = null;


            for(Album alb : albumSongMap.keySet())
            {
                if(albumSongMap.get(alb).contains(song))
                {
                    album = alb;
                    break;
                }
            }
            Artist artist = null;
            for(Artist a : artistAlbumMap.keySet())
            {
                if(artistAlbumMap.get(a).contains(album))
                {
                    artist = a;
                    break;
                }
            }
            if(songLikeMap.containsKey(song))
            {
                if(!songLikeMap.get(song).contains(user))
                {
                    songLikeMap.get(song).add(user);
                    song.setLikes(song.getLikes()+1);
                    artist.setLikes(artist.getLikes()+1);

                }

            }
            else {
                List<User> l = new ArrayList<>();
                l.add(user);
                songLikeMap.put(song,l);
                song.setLikes(song.getLikes()+1);
                artist.setLikes(artist.getLikes()+1);
            }
          return song;
        }
    }

    public String mostPopularArtist() {
        Artist artist = null;
        int max = 0;
        for(Artist a : artists){
            if(a.getLikes()>max){
                max = a.getLikes();
                artist = a;
            }
        }
        if(artist == null) return null;
        else return artist.getName();
    }

    public String mostPopularSong() {
        Song song = null;
        int max = 0;
        for(Song s : songs){
            if(s.getLikes()> max){
                song = s;
                max = s.getLikes();
            }
        }
        if(song == null) return null;
        else return song.getTitle();
    }
}
