package ru.serykhd.http.callback;

public interface HttpCallback<V> {

	/*
	 * 
	 */
	void done(V result);
	
	/*
	 * 
	 */
	void cause(Throwable cause);
}
