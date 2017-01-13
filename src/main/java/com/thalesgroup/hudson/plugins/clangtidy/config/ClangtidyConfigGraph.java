/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
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

package com.thalesgroup.hudson.plugins.clangtidy.config;

import com.thalesgroup.hudson.plugins.clangtidy.graph.ClangtidyGraph;

import java.io.Serializable;

public class ClangtidyConfigGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    private int xSize = ClangtidyGraph.DEFAULT_CHART_WIDTH;

    private int ySize = ClangtidyGraph.DEFAULT_CHART_HEIGHT;

    private boolean diplayAllError = true;

    private boolean displaySeverityError = true;

    private boolean displaySeverityPossibleError = true;

    private boolean displaySeverityStyle = true;

    private boolean displaySeverityPossibleStyle = true;


    public ClangtidyConfigGraph() {
    }

    public ClangtidyConfigGraph(int xSize, int ySize, boolean diplayAllError,
                               boolean displaySeverityError, boolean displaySeverityPossibleError,
                               boolean displaySeverityStyle, boolean displaySeverityPossibleStyle) {
        super();
        this.xSize = xSize;
        this.ySize = ySize;
        this.diplayAllError = diplayAllError;
        this.displaySeverityError = displaySeverityError;
        this.displaySeverityPossibleError = displaySeverityPossibleError;
        this.displaySeverityStyle = displaySeverityStyle;
        this.displaySeverityPossibleStyle = displaySeverityPossibleStyle;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public boolean isDiplayAllError() {
        return diplayAllError;
    }

    public boolean isDisplaySeverityError() {
        return displaySeverityError;
    }

    public boolean isDisplaySeverityPossibleError() {
        return displaySeverityPossibleError;
    }

    public boolean isDisplaySeverityStyle() {
        return displaySeverityStyle;
    }

    public boolean isDisplaySeverityPossibleStyle() {
        return displaySeverityPossibleStyle;
    }

//    public Object readResolve() {
//        return new org.jenkinsci.plugins.clangtidy.config.ClangtidyConfigGraph(
//                xSize,
//                ySize, diplayAllError, displaySeverityError, displaySeverityPossibleError, displaySeverityStyle, displaySeverityPossibleStyle, true);
//    }

}
