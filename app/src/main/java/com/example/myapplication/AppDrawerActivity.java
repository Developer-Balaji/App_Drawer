package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppDrawerActivity extends Activity {
    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listadaptor = null;
    private GridView grid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);
        packageManager = getPackageManager();
        new LoadApplications().execute();
        grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(listadaptor);
        grid.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo app = applist.get(position);
            try {
                Intent intent = packageManager
                        .getLaunchIntentForPackage(app.packageName);

                if (null != intent) {
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(AppDrawerActivity.this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

//Set the OnItemSelectedListener for the GridView[zoom in and out the icon item of gridview when get focused or lost]
        final boolean[] isFirstTime = {true};
        grid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && isFirstTime[0]) {
                    // Zoom in the first child view of the GridView
                    View view = grid.getChildAt(0);
                    if (view != null) {
                        view.setBackgroundResource(R.drawable.grid_item_border);
                        view.setScaleX(1.2f);
                        view.setScaleY(1.2f);
                    }
                    isFirstTime[0] = false; // Set the flag to false after zooming in for the first time
                } else if (hasFocus && !
                        isFirstTime[0]) {
                    // Zoom in the selected child view of the GridView
                    View view = grid.getSelectedView();
                    if (view != null) {
                        view.setBackgroundResource(R.drawable.grid_item_border);
                        view.setScaleX(1.2f);
                        view.setScaleY(1.2f);
                    }
                } else {
                    // Zoom out the child views of the GridView
                    for (int i = 0; i < grid.getChildCount(); i++) {
                        View view = grid.getChildAt(i);
                        if (view != null) {
                            view.setBackgroundResource(R.color.lb_grey);
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);
                        }
                    }
                }
            }
        });

        grid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private View prevView;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Zoom out the previously selected view
                if (prevView != null) {
                    prevView.setBackgroundResource(R.color.lb_grey);
                    prevView.setScaleX(1.0f);
                    prevView.setScaleY(1.0f);
                }
                // Zoom in the selected view
                if (view != null) {
                    view.setBackgroundResource(R.drawable.grid_item_border);
                    view.setScaleX(1.2f);
                    view.setScaleY(1.2f);
                    // Zoom out the first child view of the GridView if not selected
                    if (position != 0) {
                        View firstView = grid.getChildAt(0);
                        if (firstView != null) {
                            firstView.setBackgroundResource(R.color.lb_grey);
                            firstView.setScaleX(1.0f);
                            firstView.setScaleY(1.0f);
                        }
                    }
                }
                prevView = view;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Zoom out the previously selected view
                if (prevView != null) {
                    prevView.setBackgroundResource(R.color.lb_grey);
                    prevView.setScaleX(1.0f);
                    prevView.setScaleY(1.0f);
                    prevView = null;
                }
                // Zoom out the first child view of the GridView
                View firstView = grid.getChildAt(0);
                if (firstView != null) {
                    firstView.setBackgroundResource(R.color.lb_grey);
                    firstView.setScaleX(1.0f);
                    firstView.setScaleY(1.0f);
                }
            }
        });


//grid view focusable not existing the focus of gridview
        grid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    int selectedItemPosition = grid.getSelectedItemPosition();
                    int lastItemPosition = grid.getCount() - 1;
                    if (selectedItemPosition == lastItemPosition) {
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    int selectedItemPosition = grid.getSelectedItemPosition();
                    int numColumns = grid.getNumColumns();
                    if (selectedItemPosition >= 1 && selectedItemPosition % numColumns == 0) {
                        return true;
                    }
                }
                return false;
            }
        });

    }
    //List application info
    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    applist.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(applist, new ApplicationInfo.DisplayNameComparator(packageManager));
        return applist;
    }
    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        /*private ProgressDialog progress = null;*/

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA));
            listadaptor = new ApplicationAdapter(AppDrawerActivity.this,
                    R.layout.grid_item, applist);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            grid.setAdapter(listadaptor);
            /*progress.dismiss();*/
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    //Adapter
    public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
        private final List<ApplicationInfo> appsList;
        private final Context context;
        private final PackageManager packageManager;
        public ApplicationAdapter(Context context, int textViewResourceId,
                                  List<ApplicationInfo> appsList) {
            super(context, textViewResourceId, appsList);
            this.context = context;
            this.appsList = appsList;
            packageManager = context.getPackageManager();
        }

        @Override
        public int getCount() {
            return ((null != appsList) ? appsList.size() : 0);
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return ((null != appsList) ? appsList.get(position) : null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (null == view) {
                LayoutInflater layoutInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.grid_item, null);
            }
            ApplicationInfo data = appsList.get(position);
            if (null != data) {
                TextView appName = (TextView) view.findViewById(R.id.app_name);
                ImageView iconView = (ImageView) view.findViewById(R.id.app_icon);
                // Set the text for the view
                appName.setText(data.loadLabel(packageManager));
                // Load the icon drawable from the package manager
                Drawable iconDrawable = data.loadIcon(packageManager);
                // Set the icon drawable on the image view
                iconView.setImageDrawable(iconDrawable);
                // Set the focusable and focusableInTouchMode properties to true
            }
            return view;
        }

    }
}

