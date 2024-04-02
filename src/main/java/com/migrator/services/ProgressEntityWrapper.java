package com.migrator.services;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressEntityWrapper extends HttpEntityWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressEntityWrapper.class);

	private ProgressListener listener;

	public ProgressEntityWrapper(HttpEntity wrappedEntity, ProgressListener listener) {
		super(wrappedEntity);
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, listener, getContentLength()));
	}
	
	public static interface ProgressListener {

		 void progress(float percentage);
	}

	public static class CountingOutputStream extends FilterOutputStream {

		private ProgressListener listener;
		private long transferred;
		private long totalBytes;

		public CountingOutputStream(OutputStream out, ProgressListener listener, long totalBytes) {
			super(out);
			this.listener = listener;
			transferred = 0;
			this.totalBytes = totalBytes;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			final DecimalFormat df = new DecimalFormat("0.0000");
			transferred += len;
			listener.progress(getCurrentProgress());
			LOGGER.info("transferred : {} remaining : {} {}% completed",humanReadableByteCountBin(transferred),(humanReadableByteCountBin(this.totalBytes-transferred)),df.format(getCurrentProgress()));
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			transferred++;
			listener.progress(getCurrentProgress());
		}

		private float getCurrentProgress() {
			return ((float) transferred / totalBytes) * 100;
		}

	}
	
	public static String humanReadableByteCountBin(long bytes) {
	    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
	    if (absB < 1024) {
	        return bytes + " B";
	    }
	    long value = absB;
	    CharacterIterator ci = new StringCharacterIterator("KMGTPE");
	    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
	        value >>= 10;
	        ci.next();
	    }
	    value *= Long.signum(bytes);
	    return String.format("%.4f %cB", value / 1024.0, ci.current());
	}
	

}
