package com.bestcasttv.bestcasttvapp;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "Home",
            "Tamil",
            "English",
            "Telungu",
            "Adventure",
            "Comedy",
            "Trailer",
    };

    private static List<Movie> list;
    private static long count = 0;

    public static List<Movie> getList() {
        if (list == null) {
            list = setupMovies();
        }
        return list;
    }

    public static List<Movie> setupMovies() {
        list = new ArrayList<>();
        String title[] = {
                "Leo",
                "Devara",
                "Fire Girl",
                "Trailer",
                "X-Man"
        };

        String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
                + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
                + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
                + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
                + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
                + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
        String studio[] = {
                "Action", "Action", "Comics", "Trailer", "Comics"
        };
        String videoUrl[] = {
                "https://bestcast-mobile-download-movies.s3.amazonaws.com/Movies/trailer-720p.mp4",
                "https://d2vkl8k9v6nxyr.cloudfront.net/Movies/Anbulla_Ghilli_Encoded/Anbulla_Ghilli.m3u8",
                "https://d2vkl8k9v6nxyr.cloudfront.net/Movies/Anbulla_Ghilli_Encoded/Anbulla_Ghilli.m3u8",
                "https://d2vkl8k9v6nxyr.cloudfront.net/Movies/Anbulla_Ghilli_Encoded/Anbulla_Ghilli.m3u8",
                "https://d2vkl8k9v6nxyr.cloudfront.net/Movies/Anbulla_Ghilli_Encoded/Anbulla_Ghilli.m3u8"
        };
//        String videoUrl[] = {
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue.mp4",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4"
//        };
//
//        String bgImageUrl[] = {
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
//        };

        String bgImageUrl[] = {
                getURLForResource(R.drawable.sample_wish_list1),
                getURLForResource(R.drawable.sample_wish_list2),
                getURLForResource(R.drawable.sample_wish_list3),
                getURLForResource(R.drawable.sample_wish_list4),
                getURLForResource(R.drawable.sample_wish_list5),
        };

        String cardImageUrl[] = {
                getURLForResource(R.drawable.sample_wish_list1),
                getURLForResource(R.drawable.sample_wish_list2),
                getURLForResource(R.drawable.sample_wish_list3),
                getURLForResource(R.drawable.sample_wish_list4),
                getURLForResource(R.drawable.sample_wish_list5),
        };

//        String cardImageUrl[] = {
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/card.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/card.jpg",
//                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/card.jpg"
//        };


        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }

        return list;
    }

    private static Movie buildMovieInfo(
            String title,
            String description,
            String studio,
            String videoUrl,
            String cardImageUrl,
            String backgroundImageUrl) {
        Movie movie = new Movie();
        movie.setId(count++);
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(backgroundImageUrl);
        movie.setVideoUrl(videoUrl);
        return movie;
    }

    public static String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }
}