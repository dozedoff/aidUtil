package com.github.dozedoff.aidUtil;

/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'AidUtil', a collection of maintenance tools for 'Aid'.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.dozedoff.aidUtil.archiveIndexer.ArchiveIndexerTestSuit;
import com.github.dozedoff.aidUtil.duplicateViewer.DuplicateViewerTestSuit;

@RunWith(Suite.class)
@SuiteClasses({
	ArchiveIndexerTestSuit.class,
	DuplicateViewerTestSuit.class
})
public class AllAidUtilTests {}
