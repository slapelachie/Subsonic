/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package xyz.slapelachie.supersonic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import xyz.slapelachie.supersonic.R;
import xyz.slapelachie.supersonic.domain.MusicDirectory;
import xyz.slapelachie.supersonic.domain.PodcastEpisode;
import xyz.slapelachie.supersonic.service.DownloadService;
import xyz.slapelachie.supersonic.service.DownloadFile;
import xyz.slapelachie.supersonic.util.DrawableTint;
import xyz.slapelachie.supersonic.util.ImageLoader;
import xyz.slapelachie.supersonic.util.SongDBHandler;
import xyz.slapelachie.supersonic.util.ThemeUtil;
import xyz.slapelachie.supersonic.util.Util;

import java.io.File;
import java.util.Arrays;

/**
 * Used to display songs in a {@code ListView}.
 *
 * @author Sindre Mehus
 */
public class SongView extends UpdateView2<MusicDirectory.Entry, Boolean> {
    private static final String TAG = SongView.class.getSimpleName();

    private TextView trackTextView;
    private TextView titleTextView;
    private TextView playingTextView;
    private TextView artistTextView;
    private TextView durationTextView;
    private TextView statusTextView;
    private ImageView statusImageView;
    private ImageView bookmarkButton;
    private ImageView playedButton;
    private View bottomRowView;
    private ImageView statusDownloadView;
    private ImageView songAlbumArtView;
    private ConstraintLayout songAlbumArtViewContainer;

    private DownloadService downloadService;
    private long revision = -1;
    private DownloadFile downloadFile;
    private boolean dontChangeDownloadFile = false;

    private boolean playing = false;
    private boolean rightImage = false;
    private int moreImage = 0;
    private boolean isWorkDone = false;
    private boolean isSaved = false;
    private File partialFile;
    private boolean partialFileExists = false;
    private boolean loaded = false;
    private boolean isBookmarked = false;
    private boolean isBookmarkedShown = false;
    private boolean showPodcast = false;
    private boolean isPlayed = false;
    private boolean isPlayedShown = false;
    private boolean showAlbum = false;
    private boolean isQueue;

    public SongView(Context context, boolean isQueue) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.song_list_item, this, true);

        this.isQueue = isQueue;

        trackTextView = (TextView) findViewById(R.id.song_track);
        songAlbumArtViewContainer = (ConstraintLayout) findViewById(R.id.song_album_art_container);
        songAlbumArtView = (ImageView) findViewById(R.id.song_album_art);
        titleTextView = (TextView) findViewById(R.id.song_title);
        artistTextView = (TextView) findViewById(R.id.song_artist);
        durationTextView = (TextView) findViewById(R.id.song_duration);
        statusTextView = (TextView) findViewById(R.id.song_status);
        statusImageView = (ImageView) findViewById(R.id.song_status_icon);
        ratingBar = (RatingBar) findViewById(R.id.song_rating);
        starButton = (ImageButton) findViewById(R.id.song_star);
        starButton.setFocusable(false);
        bookmarkButton = (ImageButton) findViewById(R.id.song_bookmark);
        bookmarkButton.setFocusable(false);
        playedButton = (ImageButton) findViewById(R.id.song_played);
        moreButton = (ImageView) findViewById(R.id.item_more);
        bottomRowView = findViewById(R.id.song_bottom);
        statusDownloadView = (ImageView) findViewById(R.id.song_status_download);
    }

    public void setObjectImpl(MusicDirectory.Entry song, Boolean checkable) {
        this.checkable = checkable;

        StringBuilder artist = new StringBuilder(40);

        boolean isPodcast = song instanceof PodcastEpisode;
        if (!song.isVideo() || isPodcast) {
            if (isPodcast) {
                PodcastEpisode episode = (PodcastEpisode) song;
                if (showPodcast && episode.getArtist() != null) {
                    artist.append(episode.getArtist());
                }

                String date = episode.getDate();
                if (date != null) {
                    if (artist.length() != 0) {
                        artist.append(" - ");
                    }
                    artist.append(Util.formatDate(context, date, false));
                }
            } else if (song.getArtist() != null) {
                if (showAlbum) {
                    artist.append(song.getAlbum());
                } else {
                    artist.append(song.getArtist());
                }
            }

            if (isPodcast) {
                String status = ((PodcastEpisode) song).getStatus();
                int statusRes = -1;

                if ("error".equals(status)) {
                    statusRes = R.string.song_details_error;
                } else if ("skipped".equals(status)) {
                    statusRes = R.string.song_details_skipped;
                } else if ("downloading".equals(status)) {
                    statusRes = R.string.song_details_downloading;
                }

                if (statusRes != -1) {
                    artist.append(" (");
                    artist.append(getContext().getString(statusRes));
                    artist.append(")");
                }
            }

            durationTextView.setText(Util.formatDuration(song.getDuration()));
            bottomRowView.setVisibility(View.VISIBLE);
        } else {
            bottomRowView.setVisibility(View.GONE);
            statusTextView.setText(Util.formatDuration(song.getDuration()));
        }

        String title = song.getTitle();
        Integer track = song.getTrack();
        if (song.getCustomOrder() != null) {
            track = song.getCustomOrder();
        }
        TextView newPlayingTextView;

        if (track != null && Util.getDisplayTrack(context) && !isQueue) {
            trackTextView.setText(String.format("%02d", track));
            trackTextView.setVisibility(View.VISIBLE);
            newPlayingTextView = trackTextView;
            songAlbumArtViewContainer.setVisibility(View.GONE);
        } else {
            ImageLoader imageLoader = new ImageLoader(context);
            imageLoader.loadImage(songAlbumArtView, song, false, false);
            songAlbumArtViewContainer.setVisibility(View.VISIBLE);
            trackTextView.setVisibility(View.GONE);
            newPlayingTextView = titleTextView;
        }

        if (newPlayingTextView != playingTextView || playingTextView == null) {
            if (playing) {
                playingTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                playing = false;
            }

            playingTextView = newPlayingTextView;
        }

        titleTextView.setText(title);
        artistTextView.setText(artist);

        this.setBackgroundColor(0x00000000);
        ratingBar.setVisibility(View.GONE);
        rating = 0;

        revision = -1;
        loaded = false;
        dontChangeDownloadFile = false;
    }

    public void setDownloadFile(DownloadFile downloadFile) {
        this.downloadFile = downloadFile;
        dontChangeDownloadFile = true;
    }

    public DownloadFile getDownloadFile() {
        return downloadFile;
    }

    private int getTextColor(Context context, int attrId) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{attrId});
        int textColor = typedArray.getColor(0, 0);
        typedArray.recycle();
        return textColor;
    }

    @Override
    protected void updateBackground() {
        if (downloadService == null) {
            downloadService = DownloadService.getInstance();
            if (downloadService == null) {
                return;
            }
        }

        long newRevision = downloadService.getDownloadListUpdateRevision();
        if ((revision != newRevision && dontChangeDownloadFile == false) || downloadFile == null) {
            downloadFile = downloadService.forSong(item);
            revision = newRevision;
        }

        isWorkDone = downloadFile.isWorkDone();
        isSaved = downloadFile.isSaved();
        partialFile = downloadFile.getPartialFile();
        partialFileExists = partialFile.exists();
        isStarred = item.isStarred();
        isBookmarked = item.getBookmark() != null;
        isRated = item.getRating();

        // Check if needs to load metadata: check against all fields that we know are null in offline mode
        if (item.getBitRate() == null && item.getDuration() == null && item.getDiscNumber() == null && isWorkDone) {
            item.loadMetadata(downloadFile.getCompleteFile());
            loaded = true;
        }

        if (item instanceof PodcastEpisode || item.isAudioBook() || item.isPodcast()) {
            isPlayed = SongDBHandler.getHandler(context).hasBeenCompleted(item);
        }
    }

    @Override
    protected void update() {
        if (loaded) {
            setObjectImpl(item, item2);
        }
        if (downloadService == null || downloadFile == null) {
            return;
        }

        if (item.isStarred()) {
            if (!starred) {
                if (starButton.getDrawable() == null) {
                    starButton.setImageDrawable(DrawableTint.getTintedDrawable(context, R.drawable.ic_toggle_star));
                }
                starButton.setVisibility(View.VISIBLE);
                starred = true;
            }
        } else {
            if (starred) {
                starButton.setVisibility(View.GONE);
                starred = false;
            }
        }

        if (isWorkDone) {
            int moreImage = isSaved ? R.drawable.download_pinned : R.drawable.download_cached;
            if (moreImage != this.moreImage) {
                statusDownloadView.setImageResource(moreImage);
                this.moreImage = moreImage;
            }
        } else if (this.moreImage != 0) {
            statusDownloadView.setImageResource(0);
            this.moreImage = 0;
        }

        if (downloadFile.isDownloading() && !downloadFile.isDownloadCancelled() && partialFileExists) {
            double percentage = (partialFile.length() * 100.0) / downloadFile.getEstimatedSize();
            percentage = Math.min(percentage, 100);
            statusTextView.setText((int) percentage + " %");
            if (!rightImage) {
                statusImageView.setVisibility(View.VISIBLE);
                rightImage = true;
            }
        } else if (rightImage) {
            statusTextView.setText(null);
            statusImageView.setVisibility(View.GONE);
            rightImage = false;
        }

        boolean playing = Util.equals(downloadService.getCurrentPlaying(), downloadFile);
        int textColor = getTextColor(context, android.R.attr.textColorPrimary);
        int textColorSecondary = getTextColor(context, android.R.attr.textColorSecondary);

        if (playing) {
            if (!this.playing) {
                this.playing = playing;
                for (TextView textView : Arrays.asList(titleTextView, trackTextView)) {
                    textView.setTextColor(textColor);
                    textView.setTypeface(null, Typeface.BOLD);
                }
            }
        } else {
            if (this.playing) {
                this.playing = playing;
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                for (TextView textView : Arrays.asList(titleTextView, trackTextView)) {
                    textView.setTextColor(textColorSecondary);
                    textView.setTypeface(null, Typeface.NORMAL);
                }
            }
        }

        if (isBookmarked) {
            if (!isBookmarkedShown) {
                if (bookmarkButton.getDrawable() == null) {
                    bookmarkButton.setImageDrawable(DrawableTint.getTintedDrawable(context, R.drawable.ic_menu_bookmark_selected));
                }

                bookmarkButton.setVisibility(View.VISIBLE);
                isBookmarkedShown = true;
            }
        } else {
            if (isBookmarkedShown) {
                bookmarkButton.setVisibility(View.GONE);
                isBookmarkedShown = false;
            }
        }

        if (isPlayed) {
            if (!isPlayedShown) {
                if (playedButton.getDrawable() == null) {
                    playedButton.setImageDrawable(DrawableTint.getTintedDrawable(context, R.drawable.ic_toggle_played));
                }

                playedButton.setVisibility(View.VISIBLE);
                isPlayedShown = true;
            }
        } else {
            if (isPlayedShown) {
                playedButton.setVisibility(View.GONE);
                isPlayedShown = false;
            }
        }

        if (isRated != rating) {
            if (isRated > 1) {
                if (rating <= 1) {
                    ratingBar.setVisibility(View.VISIBLE);
                }

                ratingBar.setNumStars(isRated);
                ratingBar.setRating(isRated);
            } else if (isRated <= 1) {
                if (rating > 1) {
                    ratingBar.setVisibility(View.GONE);
                }
            }

            // Still highlight red if a 1-star
            if (isRated == 1) {
                this.setBackgroundColor(Color.RED);

                String theme = ThemeUtil.getTheme(context);
                if ("black".equals(theme)) {
                    this.getBackground().setAlpha(80);
                } else if ("dark".equals(theme) || "holo".equals(theme)) {
                    this.getBackground().setAlpha(60);
                } else {
                    this.getBackground().setAlpha(20);
                }
            } else if (rating == 1) {
                this.setBackgroundColor(0x00000000);
            }

            rating = isRated;
        }
    }

    public MusicDirectory.Entry getEntry() {
        return item;
    }

    public void setShowPodcast(boolean showPodcast) {
        this.showPodcast = showPodcast;
    }

    public void setShowAlbum(boolean showAlbum) {
        this.showAlbum = showAlbum;
    }
}
