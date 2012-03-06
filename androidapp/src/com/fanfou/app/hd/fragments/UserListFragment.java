package com.fanfou.app.hd.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;

import com.fanfou.app.hd.App;
import com.fanfou.app.hd.adapter.UserCursorAdapter;
import com.fanfou.app.hd.api.Paging;
import com.fanfou.app.hd.dao.model.UserColumns;
import com.fanfou.app.hd.dao.model.UserModel;
import com.fanfou.app.hd.service.FanFouService;
import com.fanfou.app.hd.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2012.02.07
 * @version 1.1 2012.02.08
 * @version 1.2 2012.02.09
 * @version 1.3 2012.02.22
 * @version 1.4 2012.02.24
 * 
 */
public abstract class UserListFragment extends PullToRefreshListFragment implements FilterQueryProvider{

	private static final String TAG = UserListFragment.class.getSimpleName();

	private int page;
	private String userId;
	
	private OnInitCompleteListener mListener;
	
	public void setOnInitCompleteListener(OnInitCompleteListener listener){
		this.mListener=listener;
	}
	
	private void onInitComplete(){
		if(mListener!=null){
			mListener.onInitComplete(null);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Cursor c = (Cursor) parent.getItemAtPosition(position);
		final UserModel u =UserModel.from(c);
		if (u != null) {
			if (App.DEBUG){		
				Log.d(TAG, "userId=" + u.getId() + " username=" + u.getScreenName());
			}
//			ActionManager.doProfile(getActivity(), u);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle data=getArguments();
		if(data!=null){
			userId=data.getString("id");
		}
		if(StringHelper.isEmpty(userId)){
			userId=App.getAccount();
		}
		
		if (App.DEBUG) {
			Log.d(TAG, "onCreate() userId="+userId);
		}
	}

	@Override
	protected CursorAdapter onCreateAdapter() {
		return new UserCursorAdapter(getActivity(), getCursor());
	}

	@Override
	protected void doFetch(boolean doGetMore) {
		Paging p=new Paging();
		
		if (doGetMore) {
			page++;
		} else {
			page = 1;
		}
		p.page=page;
		
		final ResultHandler handler = new ResultHandler(this);
		FanFouService.getUsers(getActivity(), userId, getType(), p, handler);
	}

	@Override
	protected void showToast(int count) {
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = UserColumns.CONTENT_URI;
		String where = UserColumns.TYPE + "=? AND " + UserColumns.OWNER + "=?";
		String[] whereArgs = new String[] { String.valueOf(getType()), userId };
		CursorLoader loader=new CursorLoader(getActivity(), uri, null, where, whereArgs, null);
		if(App.DEBUG){
			Log.d(TAG, "onCreateLoader() uri=["+uri+"] where=["+where+"] whereArgs=["+whereArgs+"]");
		}
		return loader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		super.onLoadFinished(loader, newCursor);
		getAdapter().setFilterQueryProvider(this);
		onInitComplete();
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		if(App.DEBUG){
			Log.d(TAG, "runQuery() constraint="+constraint);
		}
		String where = UserColumns.TYPE + " = " + getType() + " AND "
				+ UserColumns.OWNER + " = '" + userId + "' AND ("
				+ UserColumns.SCREEN_NAME + " like '%" + constraint + "%' OR "
				+ UserColumns.ID + " like '%" + constraint + "%' )";
		;
		return getActivity().managedQuery(UserColumns.CONTENT_URI, null, where,
				null, null);
	}

}