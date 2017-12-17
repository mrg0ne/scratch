package com.scratch.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class TaskListAdapter extends FragmentPagerAdapter {

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#destroyItem(android.view.ViewGroup, int, java.lang.Object)
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.destroyItem(container, position, object);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#finishUpdate(android.view.ViewGroup)
	 */
	@Override
	public void finishUpdate(ViewGroup container) {
		// TODO Auto-generated method stub
		super.finishUpdate(container);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getItemPosition(java.lang.Object)
	 */
	@Override
	public int getItemPosition(Object pObject) {
		TaskListFragment fragment = (TaskListFragment)pObject;

		if (fragment != null) {
			mLogger.log(Level.INFO, "getItemPosition called, restarting loader...");
			fragment.restartLoader();
		} else {
			mLogger.log(Level.WARNING, "getItemPosition of null object!");
		}

		return super.getItemPosition(pObject);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#instantiateItem(android.view.ViewGroup, int)
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		return super.instantiateItem(container, position);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#isViewFromObject(android.view.View, java.lang.Object)
	 */
	@Override
	public boolean isViewFromObject(View view, Object object) {
		// TODO Auto-generated method stub
		return super.isViewFromObject(view, object);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#startUpdate(android.view.ViewGroup)
	 */
	@Override
	public void startUpdate(ViewGroup container) {
		// TODO Auto-generated method stub
		super.startUpdate(container);
	}

	public static final int NUM_ITEMS = 3;

	private Fragment mIncompleteTasksListFragment = null;
	private Fragment mAllTasksListFragment = null;
	private Fragment mCompletedTasksListFragment = null;

	private Logger mLogger;

	public TaskListAdapter(FragmentManager pFm) {
		super(pFm);
		mLogger = Logger.getLogger(this.getClass().getName());
	}

	@Override
	public Fragment getItem(int pArg0) {
		Fragment fragment = null;

		switch (pArg0)
		{
		case 0:
			if (mIncompleteTasksListFragment == null){
				mLogger.log(Level.INFO, "Creating IncompleteTaskListFragment");
				mIncompleteTasksListFragment = IncompleteTaskListFragment.newInstance();
			}

			fragment = mIncompleteTasksListFragment;

			break;

		case 1:
			if (mAllTasksListFragment == null){
				mLogger.log(Level.INFO, "Creating AllTasksFragment");
				mAllTasksListFragment = AllTaskListFragment.newInstance();
			}

			fragment = mAllTasksListFragment;

			break;

		case 2:
			if (mCompletedTasksListFragment == null){
				mLogger.log(Level.INFO, "Creating CompleteTasksFragment");
				mCompletedTasksListFragment = CompletedTaskListFragment.newInstance();
			}

			fragment = mCompletedTasksListFragment;

			break;

		default:
			mLogger.log(Level.INFO, "Unknown item number: " + pArg0);

		}

		return fragment;
	}

	public void setPrimaryItem(ViewGroup pContainer, int pPosition, 
			Object pObject) {
		super.setPrimaryItem(pContainer, pPosition, pObject);

		if (pObject instanceof TaskListFragment){
			if (mIncompleteTasksListFragment != null){
				mIncompleteTasksListFragment.setUserVisibleHint(false);
			}

			if (mAllTasksListFragment != null){
				mAllTasksListFragment.setUserVisibleHint(false);
			}

			if (mCompletedTasksListFragment != null){
				mCompletedTasksListFragment.setUserVisibleHint(false);
			}

			((TaskListFragment)pObject).setUserVisibleHint(true);
		}


	}

	public int getCount() {
		return NUM_ITEMS;
	}

	public CharSequence getPageTitle(int pPosition) {
		String title = "";

		switch(pPosition) {
		case 0:
			title = "INCOMPLETE";
			break;

		case 1:
			title = "ALL";
			break;

		case 2:
			title = "COMPLETE";
			break;

		default:
			mLogger.log(Level.WARNING, "UNKNOWN position");
		}

		return title;
	}


}
