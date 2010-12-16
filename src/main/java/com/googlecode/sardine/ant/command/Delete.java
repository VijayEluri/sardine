package com.googlecode.sardine.ant.command;

import com.googlecode.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.delete().
 * 
 * @author Jon Stevens
 */
public class Delete extends Command {
    /** */
    private String url;

    /** */
    @Override
    public void execute() throws Exception {
        this.getTask().getSardine().delete(this.url);
    }

    /** */
    @Override
    protected void validateAttributes() throws Exception {
        if (this.url == null)
            throw new NullPointerException("url cannot be null");
    }

    /** */
    public void setUrl(String url) {
        this.url = url;
    }
}
