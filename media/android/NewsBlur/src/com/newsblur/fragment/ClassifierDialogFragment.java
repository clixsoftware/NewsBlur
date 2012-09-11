package com.newsblur.fragment;

import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.gridlayout.R.string;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.newsblur.R;
import com.newsblur.domain.Classifier;
import com.newsblur.network.APIManager;

public class ClassifierDialogFragment extends DialogFragment {

	private static final String KEY = "key";
	private static final String FEED_ID = "feed_id";
	private static final String TYPE = "type";
	private static final String CLASSIFIER = "classifier";
	private static final String TAG = "classifierDialogFragment";

	private String key, feedId;
	private Classifier classifier;
	private int classifierType;

	private APIManager apiManager;


	public static ClassifierDialogFragment newInstance(final String feedId, final Classifier classifier, final String key, final int classifierType) {
		ClassifierDialogFragment frag = new ClassifierDialogFragment();
		Bundle args = new Bundle();
		args.putString(KEY, key);
		args.putString(FEED_ID, feedId);
		args.putInt(TYPE, classifierType);
		args.putSerializable(CLASSIFIER, classifier);
		frag.setArguments(args);
		return frag;
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.dialog);
		feedId = getArguments().getString(FEED_ID);
		key = getArguments().getString(KEY);
		classifierType = getArguments().getInt(TYPE);
		classifier = (Classifier) getArguments().getSerializable(CLASSIFIER);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		apiManager = new APIManager(getActivity());
		View v = inflater.inflate(R.layout.fragment_classify_dialog, container, false);
		final TextView message = (TextView) v.findViewById(R.id.dialog_message);
		message.setText(key);

		final ImageView classifyPositive = (ImageView) v.findViewById(R.id.tag_positive);
		final ImageView classifyNegative = (ImageView) v.findViewById(R.id.tag_negative);

		final HashMap<String, Integer> typeHashMap;
		
		switch (classifierType) {
		case Classifier.TAG:
			typeHashMap = classifier.tags;
			break;
		case Classifier.AUTHOR:
			typeHashMap = classifier.authors;
			break;
		case Classifier.FEED:
			typeHashMap = classifier.feeds;
			break;
		default:
			typeHashMap = null;
			Log.e(TAG, "Error - no classifier type passed");
		}
		
		setupTypeUI(classifyPositive, classifyNegative, typeHashMap);
		
		classifyNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Void... arg0) {
						if (typeHashMap.containsKey(key) && typeHashMap.get(key) == Classifier.DISLIKE) {
							return apiManager.trainClassifier(feedId, key, classifierType, Classifier.CLEAR_DISLIKE);
						} else {
							return apiManager.trainClassifier(feedId, key, classifierType, Classifier.DISLIKE);
						}
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (!result.booleanValue()) {
							Toast.makeText(getActivity(), R.string.error_saving_classifier, Toast.LENGTH_SHORT).show();
						}
						ClassifierDialogFragment.this.dismiss();
					};
				}.execute();
			}
		});

		classifyPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... arg0) {
						if (classifier.tags.containsKey(key) && classifier.tags.get(key) == Classifier.LIKE) {
							return apiManager.trainClassifier(feedId, key, classifierType, Classifier.CLEAR_LIKE);
						} else {
							return apiManager.trainClassifier(feedId, key, classifierType, Classifier.LIKE);
						}
					}
					@Override
					protected void onPostExecute(Boolean result) {
						if (!result.booleanValue()) {
							Toast.makeText(getActivity(), R.string.error_saving_classifier, Toast.LENGTH_SHORT).show();
						}
						ClassifierDialogFragment.this.dismiss();
					}
				}.execute();
			}
		});

		return v;
	}

	private void setupTypeUI(final ImageView classifyPositive, final ImageView classifyNegative, final HashMap<String, Integer> typeHashMap) {
		if (typeHashMap != null && typeHashMap.containsKey(key)) {
			switch (typeHashMap.get(key)) {
			case Classifier.LIKE:
				classifyPositive.setImageResource(R.drawable.tag_positive_already);
				break;
			case Classifier.DISLIKE:
				classifyNegative.setImageResource(R.drawable.tag_negative_already);
				break;	
			}
		}
	}


}