package com.artifex.mupdfdemo;

import android.util.Log;

public abstract class MuPDFCancellableTaskDefinition<Params, Result> implements CancellableTaskDefinition<Params, Result>
{
	private MuPDFCore.Cookie cookie;

	public MuPDFCancellableTaskDefinition(MuPDFCore core)
	{
		try {
			this.cookie = core.new Cookie();
		}
		catch (OutOfMemoryError e)
		{
			Log.e("ERROR","Out of memory observed");
		}
	}

	@Override
	public void doCancel()
	{
		if (cookie == null)
			return;

		cookie.abort();
	}

	@Override
	public void doCleanup()
	{
		if (cookie == null)
			return;

		cookie.destroy();
		cookie = null;
	}

	@Override
	public final Result doInBackground(Params ... params)
	{
		return doInBackground(cookie, params);
	}

	public abstract Result doInBackground(MuPDFCore.Cookie cookie, Params ... params);
}
