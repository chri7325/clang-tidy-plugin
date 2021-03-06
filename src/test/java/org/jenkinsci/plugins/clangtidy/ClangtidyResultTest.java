/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Copyright (c) 2017 PIXMAP                                                    *
 * Author : Gregory Boissinot                                                   *
 * Author : Mickael Germain                                                     *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package org.jenkinsci.plugins.clangtidy;

import org.jenkinsci.plugins.clangtidy.config.ClangtidyConfigSeverityEvaluation;

import org.jenkinsci.plugins.clangtidy.ClangtidyResult;
import org.jenkinsci.plugins.clangtidy.ClangtidyStatistics;
import org.jenkinsci.plugins.clangtidy.ClangtidyBuildAction;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClangtidyResultTest {

	private BuildListener listener;
	private AbstractBuild<?, ?> owner;
	private ClangtidyStatistics report;

	private int getNewError(boolean hasPreviousResult, int nbPreviousReportError, int nbReportError) {

		if (hasPreviousResult) {
			// Previous Report
			ClangtidyStatistics previousStat = mock(ClangtidyStatistics.class);
			when(previousStat.getNumberTotal()).thenReturn(nbPreviousReportError);

			// Previous Result and associate previous report
			ClangtidyResult previousClangtidyResult = mock(ClangtidyResult.class);
			when(previousClangtidyResult.getReport()).thenReturn(previousStat);

			// Previous build and bind with the current build
			AbstractBuild<?, ?> previousBuild = mock(AbstractBuild.class);
			ClangtidyBuildAction buildAction = new ClangtidyBuildAction(previousBuild, previousClangtidyResult,
					ClangtidyBuildAction.computeHealthReportPercentage(previousClangtidyResult,
							mock(ClangtidyConfigSeverityEvaluation.class)));
			when(previousBuild.getAction(ClangtidyBuildAction.class)).thenReturn(buildAction);
			Mockito.<AbstractBuild<?, ?>>when(owner.getPreviousBuild()).thenReturn(previousBuild);
		} else {
			when(owner.getPreviousBuild()).thenReturn(null);
		}

		// New report
		when(report.getNumberTotal()).thenReturn(nbReportError);

		ClangtidyResult clangtidyResult = new ClangtidyResult(report, owner);
		return clangtidyResult.getNumberNewErrorsFromPreviousBuild();
	}

	private void processSecondBuild(int previousErrors, int atualErrors, int expectedNewErrors) {
		String message = String.format(
				"With a second build, the number of new errors have to be %d with %d errors for the previous build and %d errors for the current build.",
				expectedNewErrors, previousErrors, atualErrors);
		Assert.assertEquals(message, expectedNewErrors, getNewError(true, previousErrors, atualErrors));
	}

	@Before
	public void setUp() throws Exception {

		// initialize the logger
		listener = mock(BuildListener.class);
		when(listener.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));

		owner = mock(AbstractBuild.class);
		report = mock(ClangtidyStatistics.class);

	}

	@Test
	public void testNumberNewErrorsFromPreviousBuildFirstBuild1() {
		Assert.assertEquals("With a first build, the number of new errors must be 0.", 0, getNewError(false, 0, 0));
	}

	@Test
	public void testNumberNewErrorsFromPreviousBuildFirstBuild2() {
		Assert.assertEquals("With a first build, the number of new errors must be 0.", 0, getNewError(false, 0, 1));
	}

	@Test
	public void testNumberNewErrorsFromPreviousBuildFirstBuild3() {
		Assert.assertEquals("With a first build, the number of new errors must be 0.", 0, getNewError(false, 0, 3));
	}

	@Test
	public void testNumberNewErrorsFromPreviousBuildSecondBuild1() {

		processSecondBuild(0, 0, 0);
		processSecondBuild(0, 1, 1);
		processSecondBuild(0, 2, 2);
		processSecondBuild(0, 3, 3);

		processSecondBuild(1, 0, 0);
		processSecondBuild(1, 1, 0);
		processSecondBuild(1, 2, 1);
		processSecondBuild(1, 3, 2);

		processSecondBuild(2, 0, 0);
		processSecondBuild(2, 1, 0);
		processSecondBuild(2, 2, 0);
		processSecondBuild(2, 3, 1);

		processSecondBuild(3, 0, 0);
		processSecondBuild(3, 1, 0);
		processSecondBuild(3, 2, 0);
		processSecondBuild(3, 3, 0);
	}

}
