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
package org.jenkinsci.plugins.clangtidy.util;

import hudson.model.*;
import org.kohsuke.stapler.StaplerProxy;

public abstract class AbstractClangtidyBuildAction extends Actionable
		implements Action, HealthReportingAction, StaplerProxy {
	protected AbstractBuild<?, ?> owner;

	protected AbstractClangtidyBuildAction(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}

	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}

	public <T extends AbstractClangtidyBuildAction> T getPreviousResult() {
		AbstractBuild<?, ?> b = owner;
		while (true) {
			b = b.getPreviousBuild();
			if (b == null) {
				return null;
			}
			if (b.getResult() == Result.FAILURE) {
				continue;
			}
			AbstractClangtidyBuildAction r = b.getAction(this.getClass());
			if (r != null) {
				return (T) r;
			}
		}
	}
}
