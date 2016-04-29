/*
 * Copyright (c) NASK, NCSC
 *
 * This file is part of HoneySpider Network 2.1.
 *
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.ParameterException;
import pl.nask.hsn2.ResourceException;
import pl.nask.hsn2.StorageException;
import pl.nask.hsn2.TaskContext;
import pl.nask.hsn2.protobuff.Resources.CveList;
import pl.nask.hsn2.protobuff.Resources.CveList.Builder;
import pl.nask.hsn2.task.Task;
import pl.nask.hsn2.wrappers.ObjectDataWrapper;
import pl.nask.swftool.cvejob.CveJob;
import pl.nask.swftool.cvetool.CveTool;
import pl.nask.swftool.cvetool.detector.CveDetectionResults;
import pl.nask.swftool.cvetool.detector.CveResult;

public class SwfTask implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwfTask.class);

    private final TaskContext jobContext;
    private Long fileId;
    private final CveTool tool;

    private final long dataId;

    public SwfTask(CveTool tool, TaskContext jobContext, ObjectDataWrapper data) {
        this.tool = tool;
        this.jobContext = jobContext;
        fileId = data.getReferenceId("content");
        dataId = data.getId();
    }

    @Override
    public final boolean takesMuchTime() {
        return fileId != null;
    }

    @Override
    public final void process() throws ParameterException, ResourceException, StorageException {
        if (fileId == null) {
            LOGGER.info("Task (id={}, jobId={}, objectId={}) skipped", new Object[]{jobContext.getReqId(), jobContext.getJobId(), dataId});
            System.out.println("Task skipped"); //NOPMD
        } else {
            File file = null;
            try {
                jobContext.addTimeAttribute("swf_cve_time_begin", System.currentTimeMillis());
                file = downloadSwfFile();

                CveJob job = new CveJob("" + jobContext.getReqId(), file.getAbsolutePath());
                CveDetectionResults res = tool.detect(job);
                List<CveResult> detected = res.getResults();

                jobContext.addTimeAttribute("swf_cve_time_end", System.currentTimeMillis());
                jobContext.addAttribute("swf_cve_detected", detected.size());

                if (detected.size() > 0) {
                    Builder cveList = CveList.newBuilder();
                    for (CveResult r: detected) {
                        cveList.addCve(r.getId());
                    }

                    long resId = jobContext.saveInDataStore(cveList.build().toByteArray());
                    jobContext.addReference("swf_cve_list", resId);
                }
            } finally {
                if (file != null)
                    if (!file.delete()) {
                    	LOGGER.error("Cannot delete file: {}", file.getName());
                    }
            }
        }
    }

    private File downloadSwfFile() throws ResourceException, StorageException {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                long downloadTimeStart = System.currentTimeMillis();
                File tmpFile = File.createTempFile(jobContext.getJobId() + "-" + jobContext.getReqId() + "-" + fileId + "-", ".swf");
                tmpFile.deleteOnExit();
                is = jobContext.getFileAsInputStream(fileId);
                fos = new FileOutputStream(tmpFile);
                IOUtils.copy(is, fos);
                LOGGER.debug("Downloaded file (size={}) in {} ms", FileUtils.byteCountToDisplaySize(tmpFile.length()), System.currentTimeMillis() - downloadTimeStart);
                return tmpFile;
            } catch (IOException e) {
                throw new ResourceException("Cannot create temporary file", e);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(fos);
            }
    }


}
