/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.common.bigwigTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a data file or other resource, which might be local file or remote resource.
 *
 * @author jrobinso
 */
public class ResourceLocator {

    public static final Logger log = LoggerFactory.getLogger(ResourceLocator.class);
    public static String ucscSNP = "snp[0-9][0-9][0-9]";

    /**
     * Display name
     */
    String name;

    /**
     * The local path or url (http, https, or ftp) for the resource.
     */
    String path;

    /**
     * URL to a database server
     */
    String dbURL;

    /**
     * Optional path to an associated index file
     */
    String indexPath;

    /**
     * /**
     * Path to an associated density file.  This is used primarily for sequence alignments
     */
    String coverage;

    /**
     * The type of resource (generally this refers to the file format)
     */
    public String format;

    private HashMap attributes = new HashMap();
    private boolean indexed;
    private boolean dataURL;

    /**
     * True if this is an htsget resource
     */
    private boolean htsget;

    /**
     * Constructor for local files and URLs
     *
     * @param path
     */
    public ResourceLocator(String path) {
        this.setPath(path);
    }

    public void setFormat(String formatOrExt) {
        this.format = formatOrExt == null ? null : formatOrExt.startsWith(".") ? formatOrExt.substring(1) : formatOrExt;
    }

    public String toString() {
        return path + (dbURL == null ? "" : " " + dbURL);
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {

        if (path != null && path.startsWith("file://")) {
            this.path = path.substring("file://".length());
        } else if (path != null && path.startsWith("s3://")) {
            this.path = path;
            String s3UrlIndexPath = detectIndexPath(path);
            this.setIndexPath(s3UrlIndexPath);
        } else {
            this.dataURL = ParsingUtils.isDataURL(path);
            this.path = path;
        }
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    // XXX: Why does IGV not do that across all providers already?

    /**
     * Takes in a non-pre-signed URL and returns its (guessed) indexfile.
     *
     * @param inputPath: Path containing vcf/bam file
     * @return indexPath: Guessed path containing the corresponding index (in the CWD-equivalent dir level)
     */
    public String detectIndexPath(String inputPath) {
        log.debug("detectIndexPath() input S3 path is: " + inputPath);
        String indexPath = "";
        if (inputPath.contains(".bam")) {
            indexPath = inputPath + ".bai";
        } else if (inputPath.endsWith(".gz")) {
            indexPath = inputPath + ".tbi";
        } else {
            log.debug("S3 index object filetype could not be determined from S3 url");
        }
        return indexPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceLocator that = (ResourceLocator) o;

        if (dbURL != null ? !dbURL.equals(that.dbURL) : that.dbURL != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (dbURL != null ? dbURL.hashCode() : 0);
        return result;
    }
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isIndexed() {
        return indexed;
    }

    static Set<String> knownFormats = new HashSet<>(Arrays.asList("gff", "bed", "gtf", "gff3",
            "seg", "bb", "bigbed", "bigwig", "bam", "cram", "vcf"));
}
