/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.swingeditor;

import javax.swing.*;
import java.util.List;

/**
 * A simple window for viewing the contents of the campaign editor log
 *
 * @author Jared
 */
public class LogViewer extends JFrame {
    private static final long serialVersionUID = -3584718115393019656L;
    private JTextArea area;

    /**
     * Creates a new LogViewer and populates it with the log entries
     */
    public LogViewer(List<String> logEntries) {
        setSize(400, 300);
        setTitle("Hale Campaign Editor Log Viewer");

        area = new JTextArea();
        area.setEditable(false);

        JScrollPane scroll = new JScrollPane(area);
        add(scroll);

        for (String entry : logEntries) {
            area.append(entry);
            area.append(SwingEditor.NewLine);
        }
    }

    /**
     * Appends the specified entry to the bottom of the content of this viewer's
     * textArea
     *
     * @param entry
     */
    public void addLogEntry(String entry) {
        area.append(entry);
        area.append(SwingEditor.NewLine);
    }
}
