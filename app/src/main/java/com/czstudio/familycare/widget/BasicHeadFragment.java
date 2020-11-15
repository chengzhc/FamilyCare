package com.czstudio.familycare.widget;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.czstudio.familycare.R;

/**

 */
public class BasicHeadFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    BasicHeadFragment instance;
    public View rootView;

    ImageView img_back;
    TextView tv_title;
    TextView tv_right_menu;

    String title,rightMenuTitle;

    public BasicHeadFragment() {
        // Required empty public constructor
    }

    public static BasicHeadFragment getInstance(String title,String rightMenuTitle,OnClickWidgetListener listener) {
        // Required empty public constructor
        BasicHeadFragment fragment = new BasicHeadFragment();
        fragment.title=title;
        fragment.rightMenuTitle=rightMenuTitle;
        fragment.onClickWidgetListener=listener;
        fragment.instance=fragment;
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_basic_head, container, false);
        img_back=rootView.findViewById(R.id.img_back);

        tv_title=rootView.findViewById(R.id.tv_title);
        tv_title.setText(title);
        tv_right_menu=rootView.findViewById(R.id.tv_end_time);
        tv_right_menu.setText(rightMenuTitle);

        if(onClickWidgetListener!=null){
            img_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickWidgetListener.onClickBack(instance,img_back);
                }
            });
            tv_right_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickWidgetListener.onClickRightMenu(instance,tv_right_menu);
                }
            });
        }
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface OnClickWidgetListener{
        public void onClickBack(BasicHeadFragment fragment, ImageView iv);
        public void onClickRightMenu(BasicHeadFragment fragment, TextView tv);
    }

    OnClickWidgetListener onClickWidgetListener=null;

}
