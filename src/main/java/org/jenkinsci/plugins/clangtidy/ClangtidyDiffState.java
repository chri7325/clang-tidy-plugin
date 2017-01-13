package org.jenkinsci.plugins.clangtidy;

/**
 * Status of comparison of two reports.
 * 
 * Implementation note: The upper case of the constants and declaration order
 * are significant, the second one is used in sorting.
 * 
 * @see ClangtidyResult#diffCurrentAndPrevious(java.util.Set)
 * @author Michal Turek
 */
public enum ClangtidyDiffState {
    /** The issue is present only in the current report. */
    NEW {
        @Override
        public String getCss() {
            return "new";
        }

        @Override
        public String getText() {
            return Messages.clangtidy_DiffNew();
        }
    },

    /** The issue is present only in the previous report. */
    SOLVED {
        @Override
        public String getCss() {
            return "solved";
        }

        @Override
        public String getText() {
            return Messages.clangtidy_DiffSolved();
        }
    },

    /** The issue is present in both the current and the previous report. */
    UNCHANGED {
        @Override
        public String getCss() {
            return "unchanged";
        }

        @Override
        public String getText() {
            return Messages.clangtidy_DiffUnchanged();
        }
    };

    /**
     * Get CSS class.
     * 
     * @return the class
     */
    public abstract String getCss();

    /**
     * Get localized text.
     * 
     * @return the localized text
     */
    public abstract String getText();
}
