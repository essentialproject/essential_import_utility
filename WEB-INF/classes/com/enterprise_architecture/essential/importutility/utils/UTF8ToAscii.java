/**
 * Copyright (c)2009-2014 Enterprise Architecture Solutions Ltd.
 * This file is part of Essential Architecture Manager, 
 * the Essential Architecture Meta Model and The Essential Project.
 *
 * Essential Architecture Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Essential Architecture Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Essential Architecture Manager.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 28.09.2012	JP	1st coding.
 * 
 */
package com.enterprise_architecture.essential.importutility.utils;


/**
 * Reads file in UTF-8 encoding and output to STDOUT in ASCII with unicode
 * escaped sequence for characters outside of ASCII.
 */
public class UTF8ToAscii {

    private static final char[] hexChar = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public static String unicodeEscape(String s) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if ((c >> 7) > 0) {
		sb.append("\\u");
		sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
		sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
		sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
		sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
	    }
	    else {
		sb.append(c);
	    }
	}
	return sb.toString();
    }
    
}