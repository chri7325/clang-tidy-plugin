package org.jenkinsci.plugins.clangtidy;

import org.jenkinsci.plugins.clangtidy.model.ClangtidyWorkspaceFile;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.XmlFile;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import org.jenkinsci.plugins.clangtidy.config.ClangtidyConfig;
import org.jenkinsci.plugins.clangtidy.config.ClangtidyConfigGraph;
import org.jenkinsci.plugins.clangtidy.config.ClangtidyConfigSeverityEvaluation;
import org.jenkinsci.plugins.clangtidy.util.ClangtidyBuildResultEvaluator;
import org.jenkinsci.plugins.clangtidy.util.ClangtidyLogger;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Gregory Boissinot
 * @author Mickael Germain
 */
public class ClangtidyPublisher extends Recorder {
	@Extension
	public static final class ClangtidyDescriptor extends BuildStepDescriptor<Publisher> {

		public ClangtidyDescriptor() {
			super(ClangtidyPublisher.class);
			load();
		}

		public ClangtidyConfig getConfig() {
			return new ClangtidyConfig();
		}

		@Override
		public String getDisplayName() {
			return Messages.clangtidy_PublishResults();
		}

		@Override
		public final String getHelpFile() {
			return getPluginRoot() + "help.html";
		}

		public String getPluginRoot() {
			return "/plugin/clangtidy/";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
	}

	/**
	 * XML file with source container data. Lazy loading instead of data in
	 * build.xml.
	 *
	 * @since 1.15
	 */
	public static final String XML_FILE_DETAILS = "clangtidy_details.xml";

	private ClangtidyConfig clangtidyConfig;

	public ClangtidyPublisher(ClangtidyConfig clangtidyConfig) {
		this.clangtidyConfig = clangtidyConfig;
	}

	@DataBoundConstructor
	public ClangtidyPublisher(String pattern, boolean ignoreBlankFiles, String threshold, boolean allowNoReport,
			boolean stableBuild, String newThreshold, String failureThreshold, String newFailureThreshold,
			String healthy, String unHealthy, boolean severityError, boolean severityWarning, boolean warningBoost,
			boolean warningCert, boolean warningCppcoreguidelines, boolean warningClangAnalyzer,
			boolean warningClangDiagnostic, boolean warningGoogle, boolean warningLlvm, boolean warningMisc,
			boolean warningModernize, boolean warningMpi, boolean warningPerformance, boolean warningReadability,
			int xSize, int ySize, int numBuildsInGraph, boolean displayAllErrors, boolean displayErrorSeverity,
			boolean displayWarningSeverity, boolean displayBoostWarning, boolean displayCertWarning,
			boolean displayCppcoreguidelinesWarning, boolean displayClangAnalyzerWarning,
			boolean displayClangDiagnosticWarning, boolean displayGoogleWarning, boolean displayLlvmWarning,
			boolean displayMiscWarning, boolean displayModernizeWarning, boolean displayMpiWarning,
			boolean displayPerformanceWarning, boolean displayReadabilityWarning) {

		clangtidyConfig = new ClangtidyConfig();

		clangtidyConfig.setPattern(pattern);
		clangtidyConfig.setAllowNoReport(allowNoReport);
		clangtidyConfig.setIgnoreBlankFiles(ignoreBlankFiles);
		clangtidyConfig.setStableBuild(stableBuild);
		ClangtidyConfigSeverityEvaluation configSeverityEvaluation = new ClangtidyConfigSeverityEvaluation(threshold,
				newThreshold, failureThreshold, newFailureThreshold, healthy, unHealthy, severityError, severityWarning,
				warningBoost, warningCert, warningCppcoreguidelines, warningClangAnalyzer, warningClangDiagnostic,
				warningGoogle, warningLlvm, warningMisc, warningModernize, warningMpi, warningPerformance,
				warningReadability);
		clangtidyConfig.setConfigSeverityEvaluation(configSeverityEvaluation);
		ClangtidyConfigGraph configGraph = new ClangtidyConfigGraph(xSize, ySize, numBuildsInGraph, displayAllErrors,
				displayErrorSeverity, displayWarningSeverity, displayBoostWarning, displayCertWarning,
				displayCppcoreguidelinesWarning, displayClangAnalyzerWarning, displayClangDiagnosticWarning,
				displayGoogleWarning, displayLlvmWarning, displayMiscWarning, displayModernizeWarning,
				displayMpiWarning, displayPerformanceWarning, displayReadabilityWarning);
		clangtidyConfig.setConfigGraph(configGraph);
	}

	protected boolean canContinue(final Result result) {
		return (result != Result.ABORTED) && (result != Result.FAILURE);
	}

	/**
	 * Copies all the source files from the workspace to the build folder.
	 *
	 * @param rootDir
	 *            directory to store the copied files in
	 * @param channel
	 *            channel to get the files from
	 * @param sourcesFiles
	 *            the sources files to be copied
	 * @throws IOException
	 *             if the files could not be written
	 * @throws java.io.FileNotFoundException
	 *             if the files could not be written
	 * @throws InterruptedException
	 *             if the user cancels the processing
	 */
	private void copyFilesToBuildDirectory(final File rootDir, final VirtualChannel channel,
			final Collection<ClangtidyWorkspaceFile> sourcesFiles) throws IOException, InterruptedException {

		File directory = new File(rootDir, ClangtidyWorkspaceFile.DIR_WORKSPACE_FILES);
		if (!directory.exists() && !directory.mkdir()) {
			throw new IOException("Can't create directory for copy of workspace files: " + directory.getAbsolutePath());
		}

		for (ClangtidyWorkspaceFile file : sourcesFiles) {
			if (!file.isSourceIgnored()) {
				File masterFile = new File(directory, file.getTempName());
				if (!masterFile.exists()) {
					FileOutputStream outputStream = new FileOutputStream(masterFile);
					new FilePath(channel, file.getFileName()).copyTo(outputStream);
				}
			}
		}
	}

	public ClangtidyConfig getClangtidyConfig() {
		return clangtidyConfig;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new ClangtidyProjectAction(project, clangtidyConfig.getConfigGraph());
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		if (canContinue(build.getResult())) {
			ClangtidyLogger.log(listener, "Starting the clangtidy analysis.");

			EnvVars env = build.getEnvironment(listener);
			String expandedPattern = env.expand(clangtidyConfig.getPattern());

			ClangtidyParserResult parser = new ClangtidyParserResult(listener, expandedPattern,
					clangtidyConfig.isIgnoreBlankFiles());
			ClangtidyReport clangtidyReport;
			try {
				clangtidyReport = build.getWorkspace().act(parser);
			} catch (Exception e) {
				ClangtidyLogger.log(listener, "Error on clangtidy analysis: " + e);
				build.setResult(Result.FAILURE);
				return false;
			}

			if (clangtidyReport == null) {
				// Check if we're configured to allow not having a report
				if (clangtidyConfig.getAllowNoReport()) {
					return true;
				} else {
					build.setResult(Result.FAILURE);
					return false;
				}
			}

			ClangtidySourceContainer clangtidySourceContainer = new ClangtidySourceContainer(listener,
					build.getWorkspace(), build.getModuleRoot(), clangtidyReport.getAllErrors());

			ClangtidyResult result = new ClangtidyResult(clangtidyReport.getStatistics(), build);
			ClangtidyConfigSeverityEvaluation severityEvaluation = clangtidyConfig.getConfigSeverityEvaluation();

			Result buildResult = new ClangtidyBuildResultEvaluator().evaluateBuildResult(listener,
					result.getNumberErrorsAccordingConfiguration(severityEvaluation, false),
					result.getNumberErrorsAccordingConfiguration(severityEvaluation, true), severityEvaluation);

			if (buildResult != Result.SUCCESS) {
				build.setResult(buildResult);
			}

			ClangtidyBuildAction buildAction = new ClangtidyBuildAction(build, result,
					ClangtidyBuildAction.computeHealthReportPercentage(result, severityEvaluation));

			build.addAction(buildAction);

			XmlFile xmlSourceContainer = new XmlFile(new File(build.getRootDir(), XML_FILE_DETAILS));
			xmlSourceContainer.write(clangtidySourceContainer);

			copyFilesToBuildDirectory(build.getRootDir(), launcher.getChannel(),
					clangtidySourceContainer.getInternalMap().values());

			ClangtidyLogger.log(listener, "Ending the clangtidy analysis.");
		}
		return true;
	}
}
