/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wuba.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wuba.utils.IRunUtil.IRunnableResult;

/**
 * A collection of helper methods for executing operations.
 */
public class RunUtil implements IRunUtil {
	private static Logger LOG = Logger.getLogger("RunUtil.class");

	private static final int POLL_TIME_INCREASE_FACTOR = 4;
	private static IRunUtil sDefaultInstance = null;
	private File mWorkingDir = null;
	private Map<String, String> mEnvVariables = new HashMap<String, String>();

	/**
	 * Create a new {@link RunUtil} object to use.
	 */
	public RunUtil() {
	}

	/**
	 * Get a reference to the default {@link RunUtil} object.
	 * <p/>
	 * This is useful for callers who want to use IRunUtil without
	 * customization. Its recommended that callers who do need a custom IRunUtil
	 * instance (ie need to call either {@link #setEnvVariable(String, String)}
	 * or {@link #setWorkingDir(File)} create their own copy.
	 */
	public static IRunUtil getDefault() {
		if (sDefaultInstance == null) {
			sDefaultInstance = new RunUtil();
		}
		return sDefaultInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setWorkingDir(File dir) {
		mWorkingDir = dir;
	}

	/**
	 * {@inheritDoc}
	 */

	public synchronized void setEnvVariable(String name, String value) {
		mEnvVariables.put(name, value);
	}

	/**
	 * {@inheritDoc}
	 */

	public CommandResult runTimedCmd(final long timeout,
			final String... command) {
		final CommandResult result = new CommandResult();
		IRunUtil.IRunnableResult osRunnable = new RunnableResult(result, null,
				createProcessBuilder(command));
		CommandStatus status = runTimed(timeout, osRunnable, true);
		result.setStatus(status);
		return result;
	}

	private synchronized ProcessBuilder createProcessBuilder(String... command) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (mWorkingDir != null) {
			processBuilder.directory(mWorkingDir);
		}
		if (!mEnvVariables.isEmpty()) {
			processBuilder.environment().putAll(mEnvVariables);
		}
		return processBuilder.command(command);
	}

	private synchronized ProcessBuilder createProcessBuilder(
			List<String> commandList) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (mWorkingDir != null) {
			processBuilder.directory(mWorkingDir);
		}
		if (!mEnvVariables.isEmpty()) {
			processBuilder.environment().putAll(mEnvVariables);
		}
		return processBuilder.command(commandList);
	}

	/**
	 * {@inheritDoc}
	 */

	public CommandResult runTimedCmdWithInput(final long timeout, String input,
			final String... command) {
		return runTimedCmdWithInput(timeout, input, ArrayUtil.list(command));
	}

	/**
	 * {@inheritDoc}
	 */

	public CommandResult runTimedCmdWithInput(final long timeout, String input,
			final List<String> command) {
		final CommandResult result = new CommandResult();
		IRunUtil.IRunnableResult osRunnable = new RunnableResult(result, input,
				createProcessBuilder(command));
		CommandStatus status = runTimed(timeout, osRunnable, true);
		result.setStatus(status);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */

	public CommandResult runTimedCmdSilently(final long timeout,
			final String... command) {
		final CommandResult result = new CommandResult();
		IRunUtil.IRunnableResult osRunnable = new RunnableResult(result, null,
				createProcessBuilder(command));
		CommandStatus status = runTimed(timeout, osRunnable, false);
		result.setStatus(status);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */

	public Process runCmdInBackground(final String... command)
			throws IOException {
		final String fullCmd = Arrays.toString(command);
		LOG.debug(String.format("Running %s", fullCmd));
		return createProcessBuilder(command).start();
	}

	/**
	 * {@inheritDoc}
	 */

	public Process runCmdInBackground(final List<String> command)
			throws IOException {
		LOG.debug(String.format("Running %s", command));
		return createProcessBuilder(command).start();
	}

	/**
	 * {@inheritDoc}
	 */

	public CommandStatus runTimed(long timeout,
			IRunUtil.IRunnableResult runnable, boolean logErrors) {
		RunnableNotifier runThread = new RunnableNotifier(runnable, logErrors);
		runThread.start();
		try {
			runThread.join(timeout);
		} catch (InterruptedException e) {
			LOG.info("runnable interrupted");
		}
		if (runThread.getStatus() == CommandStatus.TIMED_OUT
				|| runThread.getStatus() == CommandStatus.EXCEPTION) {
			runThread.interrupt();
		}
		return runThread.getStatus();
	}

	/**
	 * {@inheritDoc}
	 */

	public boolean runTimedRetry(long opTimeout, long pollInterval,
			int attempts, IRunUtil.IRunnableResult runnable) {
		for (int i = 0; i < attempts; i++) {
			if (runTimed(opTimeout, runnable, true) == CommandStatus.SUCCESS) {
				return true;
			}
			LOG.debug(String.format("operation failed, waiting for %d ms", pollInterval));
			sleep(pollInterval);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */

	public boolean runFixedTimedRetry(final long opTimeout,
			final long pollInterval, final long maxTime,
			final IRunUtil.IRunnableResult runnable) {
		final long initialTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < (initialTime + maxTime)) {
			if (runTimed(opTimeout, runnable, true) == CommandStatus.SUCCESS) {
				return true;
			}
			LOG.debug(String.format("operation failed, waiting for %d ms", pollInterval));
			sleep(pollInterval);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */

	public boolean runEscalatingTimedRetry(final long opTimeout,
			final long initialPollInterval, final long maxPollInterval,
			final long maxTime, final IRunUtil.IRunnableResult runnable) {
		// wait an initial time provided
		long pollInterval = initialPollInterval;
		final long initialTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < (initialTime + maxTime)) {
			if (runTimed(opTimeout, runnable, true) == CommandStatus.SUCCESS) {
				return true;
			}
			LOG.debug(String.format("operation failed, waiting for %d ms", pollInterval));
			sleep(pollInterval);
			// somewhat arbitrarily, increase the poll time by a factor of 4 for
			// each attempt,
			// up to the previously decided maximum
			pollInterval *= POLL_TIME_INCREASE_FACTOR;
			if (pollInterval > maxPollInterval) {
				pollInterval = maxPollInterval;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */

	public void sleep(long time) {
		if (time <= 0) {
			return;
		}
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// ignore
			LOG.debug("sleep interrupted");
		}
	}

	/**
	 * Helper thread that wraps a runnable, and notifies when done.
	 */
	private static class RunnableNotifier extends Thread {

		private final IRunUtil.IRunnableResult mRunnable;
		private CommandStatus mStatus = CommandStatus.TIMED_OUT;
		private boolean mLogErrors = true;

		RunnableNotifier(IRunUtil.IRunnableResult runnable, boolean logErrors) {
			mRunnable = runnable;
			mLogErrors = logErrors;
		}

		public void run() {
			CommandStatus status;
			try {
				status = mRunnable.run() ? CommandStatus.SUCCESS
						: CommandStatus.FAILED;
			} catch (InterruptedException e) {
				LOG.info("runutil interrupted");
				status = CommandStatus.EXCEPTION;
			} catch (Exception e) {
				if (mLogErrors) {
					LOG.error("Exception occurred when executing runnable");
					LOG.error(e);
				}
				status = CommandStatus.EXCEPTION;
			}
			synchronized (this) {
				mStatus = status;
			}
		}

		public void interrupt() {
			mRunnable.cancel();
			super.interrupt();
		}

		synchronized CommandStatus getStatus() {
			return mStatus;
		}
	}

	private class RunnableResult implements IRunUtil.IRunnableResult {
		private final ProcessBuilder mProcessBuilder;
		private final CommandResult mCommandResult;
		private final String mInput;
		private Process mProcess = null;

		RunnableResult(final CommandResult result, final String input,
				final ProcessBuilder processBuilder) {
			mProcessBuilder = processBuilder;
			mInput = input;
			mCommandResult = result;
		}

		public boolean run() throws Exception {
			LOG.debug(String.format("Running %s", mProcessBuilder.command()));
			mProcess = mProcessBuilder.start();
			if (mInput != null) {
				BufferedOutputStream processStdin = new BufferedOutputStream(
						mProcess.getOutputStream());
				processStdin.write(mInput.getBytes("UTF-8"));
				processStdin.flush();
				processStdin.close();
			}
			int rc = mProcess.waitFor();
			synchronized (this) {
				if (mProcess != null) {
					mCommandResult.setStdout(StreamUtil
							.getStringFromStream(mProcess.getInputStream()));
					mCommandResult.setStderr(StreamUtil
							.getStringFromStream(mProcess.getErrorStream()));
				}
			}

			if (rc == 0) {
				return true;
			} else {
				LOG.info(String.format("%s command failed. return code %d",
						mProcessBuilder.command(), rc));
			}
			return false;
		}

		public void cancel() {
			if (mProcess != null) {
				synchronized (this) {
					mProcess.destroy();
					mProcess = null;
				}
			}
		}
	};
}
