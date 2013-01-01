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
package com.github.dozedoff.aidUtil.misc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.junit.Test;

public class ActionListenerTest {
	JFrame frame = new JFrame();
	JButton button = new JButton();

	AtomicInteger aint = new AtomicInteger();
	
	ActionListener testListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			aint.incrementAndGet();
		}
	};
	
	@Test
	public void test() throws Exception {
		frame.add(button);
		frame.setVisible(true);
		
		for(int i=0; i<3; i++){
			button.addActionListener(testListener);
		}
		
		button.doClick();
		
		assertThat(aint.get(), is(1));
	}
}
