/*
  This file is part of Subsonic.
	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with Subsonic. If not, see <http://www.gnu.org/licenses/>.
	Copyright 2015 (C) Scott Jackson
*/

package xyz.slapelachie.supersonic.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import xyz.slapelachie.supersonic.R;
import xyz.slapelachie.supersonic.adapter.SectionAdapter;
import xyz.slapelachie.supersonic.domain.Genre;
import xyz.slapelachie.supersonic.service.MusicService;
import xyz.slapelachie.supersonic.util.Constants;
import xyz.slapelachie.supersonic.util.ProgressListener;
import xyz.slapelachie.supersonic.adapter.GenreAdapter;
import xyz.slapelachie.supersonic.view.UpdateView;

import java.util.List;

public class SelectGenreFragment extends SelectRecyclerFragment<Genre> {
	private static final String TAG = SelectGenreFragment.class.getSimpleName();

	@Override
	public int getOptionsMenu() {
		return R.menu.empty;
	}

	@Override
	public SectionAdapter getAdapter(List<Genre> objs) {
		return new GenreAdapter(context, objs, this);
	}

	@Override
	public List<Genre> getObjects(MusicService musicService, boolean refresh, ProgressListener listener) throws Exception {
		return musicService.getGenres(refresh, context, listener);
	}

	@Override
	public int getTitleResource() {
		return R.string.main_albums_genres;
	}
	
	@Override
	public void onItemClicked(UpdateView<Genre> updateView, Genre genre) {
		SubsonicFragment fragment = new SelectDirectoryFragment();
		Bundle args = new Bundle();
		args.putString(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_TYPE, "genres");
		args.putInt(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_SIZE, 20);
		args.putInt(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_OFFSET, 0);
		args.putString(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_EXTRA, genre.getName());
		fragment.setArguments(args);

		replaceFragment(fragment);
	}

	@Override
	public void onCreateContextMenu(Menu menu, MenuInflater menuInflater, UpdateView<Genre> updateView, Genre item) {}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem, UpdateView<Genre> updateView, Genre item) {
		return false;
	}
}
