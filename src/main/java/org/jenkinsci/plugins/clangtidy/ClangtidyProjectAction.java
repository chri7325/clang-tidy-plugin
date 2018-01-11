package org.jenkinsci.plugins.clangtidy;

import java.io.IOException;
import java.util.Calendar;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

import org.jenkinsci.plugins.clangtidy.config.ClangtidyConfigGraph;
import org.jenkinsci.plugins.clangtidy.util.AbstractClangtidyProjectAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import org.jenkinsci.plugins.clangtidy.graph.ClangtidyGraph;

/**
 * @author Gregory Boissinot
 * @author Mickael Germain
 */
public class ClangtidyProjectAction extends AbstractClangtidyProjectAction {
	/** Clangtidy graph configuration. */
	private final ClangtidyConfigGraph configGraph;

	public ClangtidyProjectAction(final AbstractProject<?, ?> project, ClangtidyConfigGraph configGraph) {
		super(project);
		this.configGraph = configGraph;
	}

	@Override
	public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (ChartUtil.awtProblemCause != null) {
			rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
			return;
		}

		AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
		Calendar timestamp = lastBuild.getTimestamp();

		if (req.checkIfModified(timestamp, rsp)) {
			return;
		}

		Graph g = new ClangtidyGraph(lastBuild, getDataSetBuilder().build(), Messages.clangtidy_NumberOfErrors(),
				configGraph.getXSize(), configGraph.getYSize());
		g.doPng(req, rsp);
	}

	private DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> getDataSetBuilder() {
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

		AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
		ClangtidyBuildAction lastAction = lastBuild.getAction(ClangtidyBuildAction.class);

		int numBuilds = 0;

		// numBuildsInGraph <= 1 means unlimited
		for (ClangtidyBuildAction a = lastAction; (a != null)
				&& ((configGraph.getNumBuildsInGraph() <= 1) || (numBuilds < configGraph.getNumBuildsInGraph())); a = a
						.getPreviousResult(), ++numBuilds) {

			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel((hudson.model.Run<?,?> )a.getOwner());
			ClangtidyStatistics statistics = a.getResult().getStatistics();

			// Error
			if (configGraph.isDisplayErrorSeverity()) {
				dsb.add(statistics.getNumberErrorSeverity(), Messages.clangtidy_Error(), label);
			}

			// Warnings
			if (configGraph.isDisplayWarningSeverity()) {
				dsb.add(statistics.getNumberWarningSeverity(), Messages.clangtidy_Warning(), label);
			}

			// Boost
			if (configGraph.isDisplayBoostWarning()) {
				dsb.add(statistics.getNumberBoostWarning(), Messages.clangtidy_Boost(), label);
			}

			// Cert
			if (configGraph.isDisplayCertWarning()) {
				dsb.add(statistics.getNumberCertWarning(), Messages.clangtidy_Cert(), label);
			}

			// Cppcoreguidelines
			if (configGraph.isDisplayCppcoreguidelinesWarning()) {
				dsb.add(statistics.getNumberCppcoreguidelinesWarning(), Messages.clangtidy_Cppcoreguidelines(), label);
			}

			// Clang-analyzer
			if (configGraph.isDisplayClangAnalyzerWarning()) {
				dsb.add(statistics.getNumberClangAnalyzerWarning(), Messages.clangtidy_ClangAnalyzer(), label);
			}

			// Clang-diagnostic
			if (configGraph.isDisplayClangDiagnosticWarning()) {
				dsb.add(statistics.getNumberClangDiagnosticWarning(), Messages.clangtidy_ClangDiagnostic(), label);
			}

			// Esri
			if (configGraph.isDisplayEsriWarning()) {
				dsb.add(statistics.getNumberEsriWarning(), Messages.clangtidy_Esri(), label);
			}

			// Google
			if (configGraph.isDisplayGoogleWarning()) {
				dsb.add(statistics.getNumberGoogleWarning(), Messages.clangtidy_Google(), label);
			}

			// Llvm
			if (configGraph.isDisplayLlvmWarning()) {
				dsb.add(statistics.getNumberLlvmWarning(), Messages.clangtidy_Llvm(), label);
			}

			// Misc
			if (configGraph.isDisplayMiscWarning()) {
				dsb.add(statistics.getNumberMiscWarning(), Messages.clangtidy_Misc(), label);
			}

			// Modernize
			if (configGraph.isDisplayModernizeWarning()) {
				dsb.add(statistics.getNumberModernizeWarning(), Messages.clangtidy_Modernize(), label);
			}

			// Mpi
			if (configGraph.isDisplayMpiWarning()) {
				dsb.add(statistics.getNumberMpiWarning(), Messages.clangtidy_Mpi(), label);
			}

			// Performance
			if (configGraph.isDisplayPerformanceWarning()) {
				dsb.add(statistics.getNumberPerformanceWarning(), Messages.clangtidy_Performance(), label);
			}

			// Readability
			if (configGraph.isDisplayReadabilityWarning()) {
				dsb.add(statistics.getNumberReadabilityWarning(), Messages.clangtidy_Readability(), label);
			}

			// all errors
			if (configGraph.isDisplayAllErrors()) {
				dsb.add(statistics.getNumberTotal(), Messages.clangtidy_AllErrors(), label);
			}
		}
		return dsb;
	}

	@Override
	public String getDisplayName() {
		return Messages.clangtidy_ClangtidyResults();
	}

	@Override
	public AbstractBuild<?, ?> getLastFinishedBuild() {
		AbstractBuild<?, ?> lastBuild = project.getLastBuild();
		while ((lastBuild != null)
				&& (lastBuild.isBuilding() || (lastBuild.getAction(ClangtidyBuildAction.class) == null))) {
			lastBuild = lastBuild.getPreviousBuild();
		}
		return lastBuild;
	}

	/**
	 * Get build action of the last finished build.
	 *
	 * @return the build action or null
	 */
	public ClangtidyBuildAction getLastFinishedBuildAction() {
		AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
		return (lastBuild != null) ? lastBuild.getAction(ClangtidyBuildAction.class) : null;
	}

	@Override
	public Integer getLastResultBuild() {
		for (AbstractBuild<?, ?> b = project.getLastBuild(); b != null; b = b.getPreviousBuiltBuild()) {
			ClangtidyBuildAction r = b.getAction(ClangtidyBuildAction.class);
			if (r != null) {
				return b.getNumber();
			}
		}
		return null;
	}

	@Override
	public String getSearchUrl() {
		return getUrlName();
	}

	@Override
	public String getUrlName() {
		return ClangtidyBuildAction.URL_NAME;
	}

	public final boolean isDisplayGraph() {
		// Latest
		AbstractBuild<?, ?> b = getLastFinishedBuild();
		if (b == null) {
			return false;
		}

		// Affect previous
		b = b.getPreviousBuild();
		if (b != null) {

			for (; b != null; b = b.getPreviousBuild()) {
				if (b.getResult().isWorseOrEqualTo(Result.FAILURE)) {
					continue;
				}
				ClangtidyBuildAction action = b.getAction(ClangtidyBuildAction.class);
				if ((action == null) || (action.getResult() == null)) {
					continue;
				}
				ClangtidyResult result = action.getResult();
				if (result == null) {
					continue;
				}

				return true;
			}
		}
		return false;
	}
}
