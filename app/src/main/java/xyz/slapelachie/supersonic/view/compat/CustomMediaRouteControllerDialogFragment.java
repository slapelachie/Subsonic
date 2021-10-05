package xyz.slapelachie.supersonic.view.compat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.support.v7.app.MediaRouteControllerDialogFragment;

import xyz.slapelachie.supersonic.util.ThemeUtil;

public class CustomMediaRouteControllerDialogFragment extends MediaRouteControllerDialogFragment {
	@Override
	public MediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
		return new MediaRouteControllerDialog(context, ThemeUtil.getThemeRes(context));
	}
}
