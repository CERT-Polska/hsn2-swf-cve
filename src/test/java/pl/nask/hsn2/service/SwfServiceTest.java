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

import java.io.InputStream;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import pl.nask.hsn2.ServiceConnector;
import pl.nask.hsn2.TaskContext;
import pl.nask.hsn2.wrappers.ObjectDataWrapper;
import pl.nask.swftool.cvetool.CveTool;

public class SwfServiceTest {
    private final static String PLUGINS_DIRECTORUY_PATH = "target/plugins";
    private final static Long FIELD_ID = 1L;
    private final static long JOB_ID = 1L;
    private final static long RES_ID = 1L;

    @Mocked
    ServiceConnector connector;

    @Mocked
    ObjectDataWrapper objectDataWrapper;
    
    TaskContext jobContext;
    
    CveTool tool;

    @BeforeTest
    public void beforeTest() {
        tool = new CveTool();
        tool.loadPlugins(PLUGINS_DIRECTORUY_PATH);
        tool.printPluginsInfo();
        tool.bulidPluginsDistributor();
    }

    @Test
    public void positiveCVE_2009_1869Test() throws Exception {
        SwfTask task = createTask(FIELD_ID);

        final InputStream swfInputStream = getClass().getResourceAsStream("/maliciousCVE_2009_1869.swf");
        new NonStrictExpectations(jobContext) {
            byte[] bytes = new byte[0];
            {
                jobContext.getFileAsInputStream(FIELD_ID);result=swfInputStream;
                jobContext.saveInDataStore(withAny(bytes));result=RES_ID;
                jobContext.addAttribute("swf_cve_detected", 1);times=1;
            }
        };
        task.process();
    }

    private SwfTask createTask(final Long contentId) {
        jobContext = new TaskContext(JOB_ID, 0, 0, connector);
        new Expectations() {
            {
                objectDataWrapper.getReferenceId("content");result=contentId;
                objectDataWrapper.getId();
            }
        };
        return new SwfTask(tool, jobContext, objectDataWrapper);
    }

    @Test
    public void positiveCVE_2007_0071Test() throws Exception {
        SwfTask task = createTask(FIELD_ID);

        final InputStream swfInputStream = getClass().getResourceAsStream("/maliciousCVE_2007_0071.swf");
        new NonStrictExpectations(jobContext) {
            byte[] bytes = new byte[0];
            {
                jobContext.getFileAsInputStream(FIELD_ID);result=swfInputStream;
                jobContext.saveInDataStore(withAny(bytes));result=RES_ID;
                jobContext.addAttribute("swf_cve_detected", 1);times=1;
            }
        };
        task.process();
    }

    @Test
    public void negativeTest() throws Exception {
        SwfTask task = createTask(FIELD_ID);

        final InputStream swfInputStream = getClass().getResourceAsStream("/standard.swf");
        new NonStrictExpectations(jobContext) {
            {
                jobContext.getFileAsInputStream(FIELD_ID);result=swfInputStream;
                jobContext.addAttribute("swf_cve_detected", 0);times=1;
            }
        };
        task.process();
    }

    @Test
    public void nullContentTest() throws Exception {
        SwfTask task = createTask(null);        
        
        new NonStrictExpectations(jobContext) {
        	{
        		jobContext.addAttribute(null, null);times=0;
        		jobContext.addAttribute(null, anyInt);times=0;
        		jobContext.addAttribute(null, anyBoolean);times=0;
        		jobContext.addTimeAttribute(null, anyLong);times=0;
        		jobContext.addReference(null, anyLong);times=0;
        	}
		};
        
        task.process();
    }

}
