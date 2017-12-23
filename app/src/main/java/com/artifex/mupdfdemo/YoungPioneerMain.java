package com.artifex.mupdfdemo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.artifex.mupdfdemo.downloadManager.DownloadHandler;
import com.artifex.mupdfdemo.webViewer.WebViewer;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import in.youngpioneer.dps.R;
import in.youngpioneer.dps.notificationMessages.DBHelper;
import in.youngpioneer.dps.notificationMessages.DbDataMap;
import in.youngpioneer.dps.notificationMessages.FileIO.LoadStrings;
import in.youngpioneer.dps.notificationMessages.NotificationFloat;
import in.youngpioneer.dps.notificationMessages.YPLoginManager;
import in.youngpioneer.dps.serverRelations.NotificationRefreshAndStore;
import in.youngpioneer.dps.serverRelations.NotificationServerConnectionService;
import in.youngpioneer.dps.serverRelations.UpdateManager;
import in.youngpioneer.dps.utils.Util;


class ThreadPerTaskExecutor implements Executor {
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class YoungPioneerMain extends Activity implements FilePicker.FilePickerSupport {

	/* The core rendering instance */
	enum TopBarMode {
		Main, Search, Annot, Delete, More, Accept
	}

	;
	enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText};

	private int pushIndex = 0;
	public static boolean DOWNLOAD_COMPLETE_FLAG = false;
	ArrayList<DbDataMap> map;
	public  NotificationFloat floatingNotificationScreen;
	private WebView web;
	AlertDialog notification;
	public  DBHelper Pushstore;
	public static boolean isFileChanged = false;
	public static YoungPioneerMain mReference;
	public Uri uri;
	private MuPDFCore core;
	private String mFileName;
	private MuPDFReaderView mDocView;
	private View mButtonsView;
	private LoadStrings stringLoader;

	private boolean mButtonsVisible;
	private EditText mPasswordView;
	private TextView mFilenameView;
	private SeekBar mPageSlider;
	private int mPageSliderRes;
	private TextView mPageNumberView;
	private TextView mInfoView;
	private ImageButton mSearchButton;

	private ImageButton mHomeButton;
	private ImageButton mRateButton;
	private ImageButton mInfoButton;



	private ViewAnimator mTopBarSwitcher;
	private TopBarMode mTopBarMode = TopBarMode.Main;
	private AcceptMode mAcceptMode;
	private ImageButton mSearchBack;
	private ImageButton mSearchFwd;
	private ImageButton mNotificationButton;
	private ImageView splashView;
	int width;
	int height;
	public static ProgressDialog progress;
	private String BaseURL;
	private EditText mSearchText;
	private SearchTask mSearchTask;
	private ImageButton mProofButton;
	private ImageButton mSepsButton;
	private AlertDialog.Builder mAlertBuilder;
	private final Handler mHandler = new Handler();
	private boolean mAlertsActive = false;
	private boolean mReflow = false;
	private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
	private AlertDialog mAlertDialog;
	private FilePicker mFilePicker;
	private String mProofFile;
	private boolean mSepEnabled[][];
	DownloadHandler handler;
	TextView notificationTitle;
	TextView Message;

	static private AlertDialog.Builder gAlertBuilder;

	static public AlertDialog.Builder getAlertBuilder() {
		return gAlertBuilder;
	}

	public UpdateManager updater;

	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid stalling
		// the UI. Some calls can lead to javascript-invoked requests to display an
		// alert dialog and collect a reply from the user. The task has to be blocked
		// until the user's reply is received. This method creates an asynchronous task,
		// the purpose of which is to wait of these requests and produce the dialog
		// in response, while leaving the core blocked. When the dialog receives the
		// user's response, it is sent to the core via replyToAlert, unblocking it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for (int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
								case AlertDialog.BUTTON1:
									index = 0;
									break;
								case AlertDialog.BUTTON2:
									index = 1;
									break;
								case AlertDialog.BUTTON3:
									index = 2;
									break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType) {
					case Error:
						break;
					case Warning:
						break;
					case Question:
						break;
					case Status:
						break;
				}
				switch (result.buttonGroupType) {
					case OkCancel:
						mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
						pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
					case Ok:
						mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
						pressed[0] = MuPDFAlert.ButtonPressed.Ok;
						break;
					case YesNoCancel:
						mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
						pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
					case YesNo:
						mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
						pressed[0] = MuPDFAlert.ButtonPressed.Yes;
						mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
						pressed[1] = MuPDFAlert.ButtonPressed.No;
						break;
				}
				mAlertDialog.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mAlertDialog = null;
						if (mAlertsActive) {
							result.buttonPressed = MuPDFAlert.ButtonPressed.None;
							core.replyToAlert(result);
							createAlertWaiter();
						}
					}
				});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	public boolean isFirstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return !prefs.contains("runNumber");
	}

	public void endFirstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt("runNumber", 1);
		edit.apply();
	}

	private MuPDFCore openFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1
				? path
				: path.substring(lastSlashPos + 1));
		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(this, path);
			if (core != null && core.needsPassword())
				core.authenticatePassword("password");
			// New file: drop the old oe data

		} catch (Exception e) {
			System.out.println(e);
			return null;
		} catch (OutOfMemoryError e) {
			//  out of memory is not an Exception, so we catch it separately.
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[], String magic) {
		System.out.println("Trying to open byte buffer");
		try {
			core = new MuPDFCore(this, buffer, magic);
			// New file: drop the old outline data
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Called when the activity is first created.
	 */
	boolean checkPresence(String filename) {
		File checkFile = new File(filename);
		return checkFile.exists();
	}

	int getFileSizeInFromPrivateMemory() {
		File heraldPDF = new File(this.getFilesDir() + "/dps.pdf");
		if (heraldPDF.exists())
			return Integer.parseInt(String.valueOf(heraldPDF.length() / 1024));
		else
			return 0;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
		mReference = this;
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();
		stringLoader = new LoadStrings(this);


		//Content update service here
		Intent serviceStarter = new Intent(this, NotificationServerConnectionService.class);
		startService(serviceStarter);



		BaseURL = stringLoader.RuntimeStrings.getBaseURL() + "/CurrentIssue/";

		Intent mainIntent = getIntent();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mainIntent.setAction("android.intent.action.VIEW");
		Log.i("FILEIO", "TRACKER ONCREATE1");
		super.onCreate(savedInstanceState);
		Log.i("FILEIO", "TRACKER ONCREATE2");

		//Starting Database Helper
		Pushstore = new DBHelper(this);

		//This hardcode should be changed for good
		if (getFileSizeInFromPrivateMemory() >= 1000) {
			endFirstRun();
			Log.i("FILESIZE", "THE file is sized at" + getFileSizeInFromPrivateMemory());
		}
		Log.i("FILEIO", "TRACKER ONCREATE3");
		//splashView =  (ImageView)mButtonsVi
		// ew.findViewById(R.id.Splash);

		mAlertBuilder = new AlertDialog.Builder(this);
		gAlertBuilder = mAlertBuilder;  //  keep a static copy of this that other classes can use

		if (core == null) {
			core = (MuPDFCore) getLastNonConfigurationInstance();

			if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}

		assetCopy();         //copy assets to home Directory of the  application
		//downloadFromUrl();
		DownloadHandler.endDownload(this);
		if (core == null) {
			Intent intent = getIntent();
			byte buffer[] = null;

			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				//Uri uri = intent.getData();
				uri = getFileUri("dps.pdf");
				//You think the data is being passed from here pal?
				System.out.println("URI to open is: " + uri);
				if (uri.toString().startsWith("content://")) {
					String reason = null;
					try {
						System.out.println("INside: " + uri);
						InputStream is = getContentResolver().openInputStream(uri);
						int len = is.available();
						buffer = new byte[len];
						is.read(buffer, 0, len);
						is.close();
					} catch (OutOfMemoryError e) {
						System.out.println("Out of memory during buffer reading");
						reason = e.toString();
					} catch (Exception e) {
						System.out.println("Exception reading from stream: " + e);

						// Handle view requests from the Transformer Prime's file manager
						// Hopefully other file managers will use this same scheme, if not
						// using explicit paths.
						// I'm hoping that this case below is no longer needed...but it's
						// hard to test as the file manager seems to have changed in 4.x.
						try {
							Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
							if (cursor.moveToFirst()) {
								String str = cursor.getString(0);
								if (str == null) {
									reason = "Couldn't parse data in intent";
								} else {
									uri = Uri.parse(str);
								}
							}
						} catch (Exception e2) {
							System.out.println("Exception in Transformer Prime file manager code: " + e2);
							reason = e2.toString();
						}
					}
					if (reason != null) {
						buffer = null;
						Resources res = getResources();
						AlertDialog alert = mAlertBuilder.create();
						setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
						alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										finish();
									}
								});
						alert.show();
						return;
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer, intent.getType());
				} else {
					String path = Uri.decode(uri.getEncodedPath());
					if (path == null) {
						path = uri.toString();
						//path="file:///storage/emulated/0/Download/beta-gamma-functins-material.pdf";
						//delete this to clear
					}
					core = openFile(path);
				}

				SearchTaskResult.set(null);
			}
			if (core != null && core.needsPassword()) {
				Log.i("PASSWORD", "core needs password");
				core.authenticatePassword("password");
				return;
			}

			if (core != null && core.countPages() == 0) {
				core = null;
			}
		}
		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			alert.show();
			return;
		}
		//if(core.authenticatePassword("password"))
		createUI(savedInstanceState);

		//  hide the proof button if this file can't be proofed
		if (!core.canProof()) {
			mProofButton.setVisibility(View.INVISIBLE);
		}



		mNotificationButton.performClick();
	}


	public void downloadFromUrlString(String URLstring) {
		String url = URLstring;
		// see if it can be converted into !=null, then init
		DownloadHandler handler = new DownloadHandler(this);
		File fileDir = this.getFilesDir();
		Log.i("FILEIO", fileDir.getAbsolutePath().toString());

		handler.downloadFiles(this, url, fileDir + "/");
		isFileChanged = true;

	}

	void assetCopy() {
		Log.i("FILEIO", "Init File copy");
		InputStream stream = null;
		OutputStream output = null;

		try {

			for (String fileName : this.getAssets().list("Docs")) {
				if (!isFilePresent(fileName)) {
					Log.i("FILEIO", "FileOpenTime " + fileName);

					stream = this.getAssets().open("Docs/" + fileName);
					Log.i("FILEIO", "FileOpen " + fileName);
					output = new BufferedOutputStream(new FileOutputStream(this.getFilesDir() + "/" + fileName));
					Log.i("FILEIO", "FileSaved" + fileName);

					byte data[] = new byte[1024];
					int count;

					while ((count = stream.read(data)) != -1) {
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					stream.close();

					stream = null;
					output = null;
				} else {
					Log.i("FILEIO", "File Already Present");
					Log.i("FILEIO", "FileURI in App folder " + getFileUri(fileName));
				}
			}
		} catch (FileNotFoundException e) {
			Log.i("FILEIO", "File not found");

		} catch (IOException e) {
			Log.i("FILEIO", "IO Exception");
		}


	}

	Uri getFileUri(String Filename) {
		File file = getBaseContext().getFileStreamPath(Filename);
		Uri returnURI = Uri.fromFile(file);

		return returnURI;

	}

	public boolean isFilePresent(String fname) {
		File file = getBaseContext().getFileStreamPath(fname);
		return file.exists();
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view


		mDocView = new MuPDFReaderView(this) {

			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;

				mPageNumberView.setText(String.format("%d / %d", i + 1,
						core.countPages()));
				mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else {
					if (mTopBarMode == TopBarMode.Main)
						hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
			}


		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

		mSearchTask = new SearchTask(this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Make the pdf_home_screen overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;

		// Set the file-name text
		//mFilenameView.setText(mFileName);

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
			}
		});

		// add some color
		// You can add your random color generator here
		// and set color


		// Activate the search-preparing button
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOn();
			}
		});
		mNotificationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				setNewMessageFlag(false);
				mNotificationButton.setImageResource(R.drawable.notification_nor);


				if(NotificationRefreshAndStore.getUserToken(mReference)==-1) {
					YPLoginManager manager = new YPLoginManager(YoungPioneerMain.mReference);
					manager.createLoginDialog();
				} else {
					floatingNotificationScreen = new NotificationFloat(mReference);
					floatingNotificationScreen.showNotificationInList();
				}
			}
		});
		// mNotificationButton.setVisibility(View.INVISIBLE);
		// Activate the reflow button


		// Search invoking pdf_home_screen are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				setButtonEnabled(mSearchBack, haveText);
				setButtonEnabled(mSearchFwd, haveText);

				// Remove any previous search results
				if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
			}
		});

		//React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE)
					search(1);
				return false;
			}
		});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
					search(1);
				return false;
			}
		});

		// Activate search invoking pdf_home_screen
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

		mInfoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(YoungPioneerMain.this)
						.setPositiveButton(android.R.string.ok, null)
						.setMessage(R.string.infoMesage).show();
			}
		});
		//Add Star Listener Here

		mRateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final AlertDialog.Builder rateDialog = new AlertDialog.Builder(YoungPioneerMain.this)
						.setNegativeButton(android.R.string.cancel, null)
						.setTitle("Feedback");
				rateDialog.setIcon(R.drawable.ic_launcher);
				LinearLayout linearBox = new LinearLayout(YoungPioneerMain.this);
				linearBox.setPadding(30, 30, 30, 30);


				final RatingBar rateStars = new RatingBar(YoungPioneerMain.this);

				linearBox.setDividerPadding(12);
				linearBox.setGravity(Gravity.CENTER);
				linearBox.setOrientation(LinearLayout.VERTICAL);


				rateStars.setPadding(0, 70, 0, 20);
				rateStars.animate();
				rateStars.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				linearBox.addView(rateStars);

				final TextView ReviewString = new TextView(YoungPioneerMain.this);
				ReviewString.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				ReviewString.setText("");
				ReviewString.setTextColor(Color.BLUE);

				rateStars.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
					@Override
					public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
						switch ((int) rating) {
							case 1:
								ReviewString.setText("Hated it");
								ReviewString.setTextColor(Color.BLUE);
								break;
							case 2:
								ReviewString.setText("Disliked it");
								ReviewString.setTextColor(Color.BLUE);
								break;
							case 3:
								ReviewString.setText("It's OK");
								ReviewString.setTextColor(Color.BLUE);
								break;
							case 4:
								ReviewString.setText("Liked it");
								ReviewString.setTextColor(Color.RED);
								break;
							case 5:
								ReviewString.setText("Loved it");
								ReviewString.setTextColor(Color.RED);
								break;
							default:
								ReviewString.setText("");

						}
					}
				});

				linearBox.addView(ReviewString);


				final EditText input = new EditText(YoungPioneerMain.this);

				LinearLayout.LayoutParams textLay = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT);

				input.setText("write here...");

				input.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						input.setText("");
					}
				});

				input.setLayoutParams(textLay);
				rateDialog.setCancelable(false);

				rateDialog.setNeutralButton("SUBMIT", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if(rateStars.getRating()>0.0f) {
							new AsyncTask<Void, Void, Void>() {
								@Override
								protected void onPreExecute() {
									super.onPreExecute();
									Toast.makeText(YoungPioneerMain.this, "Sending...", Toast.LENGTH_SHORT).show();
								}

								String message = input.getText().toString();


								@Override
								protected Void doInBackground(Void... params) {
									Looper.prepare();
									if(message.equals("write here..."))
										message = "";

									Log.i("Message", " " + message);
										Log.i("Message", " Sending...");

										int timeout = 7000;


										try {
											HttpGet httpGet = null;
											HttpClient httpclient = new DefaultHttpClient();
											httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
											Log.i("Message", "URL=" + stringLoader.RuntimeStrings.getReviewURL() + "?userstar=" + String.valueOf(rateStars.getNumStars()) +
													"&userid=" + String.valueOf(NotificationRefreshAndStore.getUserToken(mReference)) + "&usercomment=" + message);



											httpGet = new HttpGet(stringLoader.RuntimeStrings.getReviewURL() + "?userstar=" + String.valueOf(rateStars.getRating()) +
													"&userid="
													+ String.valueOf(NotificationRefreshAndStore.getUserToken(mReference)) + "&usercomment="
													+ URLEncoder.encode(message,"UTF-8"));

											HttpResponse response = httpclient.execute(httpGet);

											Log.i("Message", " Sent.  " + response.toString() + "   br  " + response.getStatusLine().toString()
											);

											input.setText(""); //reset the message text field

										} catch (ClientProtocolException e) {
											cancel(true);
											e.printStackTrace();
										} catch (IOException e) {
											cancel(true);
											e.printStackTrace();
										} catch (IllegalArgumentException e) {
											cancel(true);
											e.printStackTrace();
										}

									return null;
								}

								@Override
								protected void onPostExecute(Void aVoid) {
									super.onPostExecute(aVoid);
									Toast.makeText(YoungPioneerMain.this, "Sent", Toast.LENGTH_SHORT).show();
								}

								@Override
								protected void onCancelled() {
									super.onCancelled();
									Toast.makeText(YoungPioneerMain.this, "Failed...", Toast.LENGTH_SHORT).show();

								}
							}.execute();
						} else {
							Toast.makeText(YoungPioneerMain.this, "Please Rate Again...", Toast.LENGTH_LONG).show();

						}
					}
				});
				linearBox.addView(input);
				rateStars.setNumStars(5);
				rateDialog.setView(linearBox);

				rateDialog.show();


			}
		});

		mHomeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDocView.setDisplayedViewIndex(0);
				mPageSlider.setProgress(0);


			}
		});


		// Reenstate last state if it was recorded
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();


		// Stick the document view and the pdf_home_screen overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		setContentView(layout);

	}

	void showProgressDialog() {
		Log.i("PROGRESS", "trying to show progress");
		if (checkInternetConnectivity()) {
			progress = new ProgressDialog(this);
			progress.setTitle("Downloading Latest Release...");
			progress.setMessage("Please keep the app in foreground.");
			progress.setCancelable(false);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.show();
			Log.i("PROGRESS", "PROGRESS DIALOG SHOWING");
		} else {
			showAlertDialogOnNoConnection();
		}
	}

	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.apply();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mTopBarMode == TopBarMode.Search)
			outState.putBoolean("SearchMode", true);

		if (mReflow)
			outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSearchTask != null)
			mSearchTask.stop();

		if (!isFileChanged) {
			if (mFileName != null && mDocView != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mReference);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
				edit.apply();
			}
		} else {
			if (mFileName != null && mDocView != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mReference);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt("page" + mFileName, 0);
				edit.apply();
			}
		}

		/*if (sensorManager != null) {
			sensorManager.unregisterListener(this);
			sensorManager = null;
		}*/
	}

	@Override
	public void onDestroy() {
		if (null != mDocView) {
			mDocView.applyToChildren(new AKReaderView.ViewMapper() {
				void applyToView(View view) {
					((MuPDFView) view).releaseBitmaps();
				}
			});
		}
		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
		super.onDestroy();
		finish();

	}


	private void setButtonEnabled(ImageButton button, boolean enabled) {
		button.setEnabled(enabled);
		//button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255) : Color.argb(255, 128, 128, 128));
	}


	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
			mPageSlider.setProgress(index * mPageSliderRes);
			if (mTopBarMode == TopBarMode.Search) {
				mSearchText.requestFocus();
				showKeyboard();
			}

			Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mTopBarSwitcher.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageSlider.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();

			Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mTopBarSwitcher.setVisibility(View.INVISIBLE);
				}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageSlider.setVisibility(View.INVISIBLE);
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private void searchModeOn() {
		if (mTopBarMode != TopBarMode.Search) {
			mTopBarMode = TopBarMode.Search;
			//Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}

	private void searchModeOff() {
		if (mTopBarMode == TopBarMode.Search) {
			mTopBarMode = TopBarMode.Main;
			hideKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	private void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d / %d", index + 1, core.countPages()));
	}


	private void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.pdf_home_screen, null);
		mFilenameView = (TextView) mButtonsView.findViewById(R.id.docNameText);
		mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
		mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);
		mInfoView = (TextView) mButtonsView.findViewById(R.id.info);
		mSearchButton = (ImageButton) mButtonsView.findViewById(R.id.searchButton);
		mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.switcher);
		mSearchBack = (ImageButton) mButtonsView.findViewById(R.id.searchBack);
		mSearchFwd = (ImageButton) mButtonsView.findViewById(R.id.searchForward);
		mSearchText = (EditText) mButtonsView.findViewById(R.id.searchText);
		mNotificationButton = (ImageButton) mButtonsView.findViewById(R.id.Notifications);

		if(getNewMessageFlag())
		mNotificationButton.setImageResource(R.drawable.notification_new);

		//Rate and Info Buttons
		mRateButton = (ImageButton) mButtonsView.findViewById(R.id.starButton);
		mInfoButton = (ImageButton) mButtonsView.findViewById(R.id.infoButton);
		mHomeButton = (ImageButton) mButtonsView.findViewById(R.id.homeButton);

		mTopBarSwitcher.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);
		mInfoView.setVisibility(View.INVISIBLE);
		mPageSlider.setVisibility(View.INVISIBLE);


	}


	public void OnCancelSearchButtonClick(View v) {
		searchModeOff();
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	private void search(int direction) {
		hideKeyboard();
		int displayPage = mDocView.getDisplayedViewIndex();
		SearchTaskResult r = SearchTaskResult.get();
		int searchPage = r != null ? r.pageNumber : -1;
		mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
	}

	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
		if (core != null) {
			core.startAlerts();
			createAlertWaiter();
		}

		super.onStart();
	}

	@Override
	protected void onStop() {
		if (core != null) {
			destroyAlertWaiter();
			core.stopAlerts();
		}
		super.onStop();
		System.runFinalizersOnExit(true);

	}


	public boolean isDownloading() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mReference);
		return prefs.getBoolean("isDownloading", false);

	}

	void clearIfNoDownload() {
		URI FileURI;
		Uri FileUri;
		File flag = null;
		if (getExternalFilesDir("/") != null)
			FileURI = getExternalFilesDir("/").toURI();

		else {
			FileURI = getFilesDir().toURI();
		}
		FileUri = Uri.parse(FileURI.toString() + "pdfFile.pdf");
		Log.i("DOWNLOAD", "Uri is " + FileUri.toString());
		try {
			flag = new File(FileUri.getPath());
		} catch (NullPointerException e) {

		}
		if (flag.exists()) {
			try {
				flag.delete();
				Log.i("RUNCHECK", "FILE DELETED FROM EXIST");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public boolean checkInternetConnectivity() {
		ConnectivityManager cm =
				(ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
				activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}

	void firstRunTODOWithURL(String Url) {
		if (isFirstRun() && !isDownloading()) {

			Log.i("RUNCHECK", "First Run");
			downloadFromUrlString(Url);
			showProgressDialog();

			//TODO use website from file
			//popupOnDelay("http://dpsbokaro.youngpioneer.in");
			popupOnDelay(stringLoader.RuntimeStrings.getSiteForWebView());
			// downloadFromUrlString("http://youngpioneer.in/ncjindal/content/current/jindalreflections.pdf");
		}
	}

	String getCurrentVersionName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mReference);
		String currentName = prefs.getString("_currentRelease", "null");
		return currentName;
	}

	void popupOnDelay(final String Url) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				int counter = 10;
				while (counter >= 0) {

					counter--;
					try {
						if(DOWNLOAD_COMPLETE_FLAG==true){
							cancel(true);
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Log.i("DOWNLOAD", "Counter is" + counter);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				new WebViewer().showInWebView(Url,mReference);
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				DOWNLOAD_COMPLETE_FLAG = false;
			}
		}.execute();
	}
	//String BaseURL= "http://youngpioneer.in/dpsbokaro/content/current/";
	//String BaseURL= "http://unnayan.co.in/unnayan.co.in/vyomkesh/yp/CurrentIssue/";

	void showAlertDialogOnNoConnection() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setTitle("New release available");
		alertDialogBuilder.setMessage("Internet connectivity is required to download current release.");
		alertDialogBuilder.show();
	}

	protected void onResume() {
		super.onResume();
		if (!DownloadHandler.isDownloadIncomplete(this)) {
			clearIfNoDownload();
			Log.i("DOWNLOAD", "ALL downloads are complete");
		}

		if (isFirstRun() && !checkInternetConnectivity()) {
			showProgressDialog();
		}

		updater = new UpdateManager(getApplicationContext()) {
			@Override
			public void runAfterFilenameIsRetrieved() {
				super.runAfterFilenameIsRetrieved();
				String URL = updater.getLatestNameFromPreferences();
				firstRunTODOWithURL(BaseURL + URL);
			}
		};


		if (updater.isUpdateAvailable() && !DownloadHandler.isDownloadIncomplete(this)) {
			downloadFromUrlString(BaseURL + updater.getLatestNameFromPreferences());
			showProgressDialog();

		}
		if (updater.isUpdateAvailable() && DownloadHandler.isDownloadIncomplete(this)) {
			//  popupOnDelay("http://ncjindal.youngpioneer.in");
			showProgressDialog();

		}
//		fixTapMargins();


		refreshNotificationMessages();
	}

	public void refreshNotificationMessages() {

		NotificationRefreshAndStore url = new NotificationRefreshAndStore(mReference);
		ConnectivityManager cm =
				(ConnectivityManager) YoungPioneerMain.mReference.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
				activeNetwork.isConnectedOrConnecting();
		if (isConnected) {
			url.getMessagesFromServer();
		}
	}

	public boolean getNewMessageFlag() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mReference);
		return prefs.getBoolean("messageAvailable",false);
	}

	public void setNewMessageFlag(boolean flagState) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean("messageAvailable", flagState);
		edit.apply();
	}

	void fixTapMargins() {
		int heightPixels = Util.getScreenHeightPixelWithOrientation(this);
		int tapPageMargin = heightPixels / 10;
		if (tapPageMargin < 10)
			tapPageMargin = 10;
		mDocView.setTapPageMargin(tapPageMargin);
	}

}
