/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.lucee.extension.pdf.img;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

public abstract class PDF2Image {

	protected static Resource createDestinationResource(Resource dir, String prefix, int page, String format, boolean overwrite) throws PageException {
		Resource res = dir.getRealResource(prefix + "_page_" + page + "." + format);
		if(res.exists()) {
			if(!overwrite)
				throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException(
						"can't overwrite existing image [" + res + "], attribute [overwrite] is false");
		}
		return res;
	}

	public abstract BufferedImage toImage(byte[] input, int page) throws IOException, PageException;
}