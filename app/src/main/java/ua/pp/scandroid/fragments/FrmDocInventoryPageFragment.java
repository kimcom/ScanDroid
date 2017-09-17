package ua.pp.scandroid.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.pp.scandroid.R;
import ua.pp.scandroid.forms.FrmDocInventoryActivity;

public class FrmDocInventoryPageFragment extends Fragment {
    private static final String TAG = "inventory";
    private static final String SAVE_PAGE_NUMBER = "save_page_number";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private int pageNumber;

    public FrmDocInventoryPageFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_PAGE_NUMBER, pageNumber);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d(TAG, "onDestroy: " + pageNumber);
    }
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FrmDocInventoryPageFragment newInstance(Bundle argums) {
//        Log.i(TAG,"newInstance: " + String.valueOf(sectionNumber));
        FrmDocInventoryPageFragment fragment = new FrmDocInventoryPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, argums.getInt("sectionNumber"));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARG_SECTION_NUMBER);
//        Log.d(TAG, "onCreate: " + pageNumber);

//        int savedPageNumber = -1;
//        if (savedInstanceState != null) {
//            savedPageNumber = savedInstanceState.getInt(SAVE_PAGE_NUMBER);
//        }
//        Log.d(TAG, "savedPageNumber = " + savedPageNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.i(TAG,"create View: " + String.valueOf(pageNumber));
        View rootView = null;
        if (pageNumber == 0) {
            rootView = inflater.inflate(R.layout.activity_doc_inventory_header, container, false);
        }else if (pageNumber == 1) {
            rootView = inflater.inflate(R.layout.activity_doc_inventory_body, container, false);
        }else if (pageNumber == 2) {
            rootView = inflater.inflate(R.layout.activity_camera, container, false);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrmDocInventoryActivity f = (FrmDocInventoryActivity) getActivity();
        if (pageNumber == 0) {
            f.startFrmDocHeader();
        }else if (pageNumber == 1) {
            f.startFrmDocBody();
//        }else if (pageNumber == 2) {
//            f.startFrmDocCamera();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
