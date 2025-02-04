// Requires JSGraphics
// Requires JQuery

window.Juno = window.Juno || {};
window.Juno.Common = window.Juno.Common || {};
window.Juno.GraphingHelper = window.Juno.GraphingHelper ||
	{
		/**
		 * Create a graphing handle using the specified element as a canvas.
		 * Pass the handle to the rest of the library to create graphs.
		 *
		 * Default opts {
		 *     color: "maroon"
		 *     lines: false
		 * }
		 *
		 * @param element Native HTML div element to use as a canvas.
		 * @param opts Object (available: color, lines)
		 */
		createGraphHandle: function (element, opts)
		{
			opts = opts || {};
			
			var graphics = new jsGraphics(element);
			graphics.setColor(opts.color || "maroon");
			graphics.setFont("arial", "15px", Font.PLAIN);
			graphics.setStroke(2);
			
			$element = jQuery(element);
			
			x0 = $element.position().left;
			x1 = x0 + $element.width();
			
			y0 = $element.position().top;
			y1 = y0 + $element.height();
			
			return {
				// Hold onto the references
				_jsGraphics: graphics,
				_element: element,
				_opts: opts,
				
				// Canvas boundaries (px) as measured from the top left of the viewport.
				_x0: x0,
				_x1: x1,
				_y0: y0,
				_y1: y1,
				
				// The minimum and maximum values of each axis (arbitrary units)
				_xMin: 0,
				_xMax: 0,
				_yMin: 0,
				_yMax: 0,
				
				// px/unit  for each axis, how many pixels correspond to an arbitrary unit
				_xNormalizationFactor: 0,
				_yNormalizationFactor: 0,
				
				// Keep track of the last point, in case we want to connect points with lines
				_lastPoint: null,
			}
		},
		
		/**
		 * Define the axes of the graph, in arbitrary units
		 *
		 * @param handle Graph returned by createGraphHandle
		 * @param xMin minimum value of x-axis
		 * @param xMax maximum value of x-axis
		 * @param yMin minimum value of y-axis
		 * @param yMax maximum value of y-axis
		 */
		defineXYAxes: function (handle, xMin, xMax, yMin, yMax)
		{
			handle._xMin = xMin;
			handle._xMax = xMax;
			handle._yMin = yMin;
			handle._yMax = yMax;
			
			var ySpan = handle._yMax - handle._yMin;    // units on axis
			var yHeight = handle._y1 - handle._y0;       // px of axis
			handle._yNormalizationFactor = yHeight / ySpan;  // px/unit
			
			var xSpan = handle._xMax - handle._xMin;
			var xWidth = handle._x1 - handle._x0;
			handle._xNormalizationFactor = xWidth / xSpan;
		},
		
		/**
		 * Draw a point on the graph represented by the X glyph.
		 *
		 * The units of x,y are the same as those used by the graph's axes.
		 * Requires defineAxes to have been set in the handle.
		 *
		 * If x and y are not numeric, or if they are outside the range of axes
		 * defined by the graph, then no point will be rendered.
		 *
		 * @param handle Graph returned by createGraphHandle
		 * @param x x-value of point to draw
		 * @param y y-value of the point to draw
		 */
		drawXYPoint: function drawXYPoint(handle, x, y) {
			var numericCheck = jQuery.isNumeric(x) && jQuery.isNumeric(y);
			if (!numericCheck) {
				return;
			}
			
			var xBoundsCheck = x >= handle._xMin && x <= handle._xMax;
			var yBoundsCheck = y >= handle._yMin && y <= handle._yMax;
			
			if (xBoundsCheck && yBoundsCheck)
			{
				var pxCoords = window.Juno.GraphingHelper._normalize(handle, x, y);
				
				// Brush is set to the top left corner of the X, so apply a correction factor to center the glyph on the coords
				handle._jsGraphics.drawString("X", pxCoords.x - 4, pxCoords.y - 8);
				handle._jsGraphics.paint();
				
				if (handle._opts.lines && handle._lastPoint)
				{
					var lastPxCoords = window.Juno.GraphingHelper._normalize(handle, handle._lastPoint.x, handle._lastPoint.y);
					
					handle._jsGraphics.drawLine(lastPxCoords.x, lastPxCoords.y, pxCoords.x, pxCoords.y);
					handle._jsGraphics.paint();
				}
				
				handle._lastPoint = {x: x, y: y};
				handle._jsGraphics.setPrintable(true);
				
				return true;
			}
		},
		
		/**
		 * Clear the graph associated with the handle
		 * @param handle handle Graph returned by createGraphHandle
		 */
		clearCanvas: function clearCanvas(handle)
		{
			handle._jsGraphics.clear();
			handle._lastPoint = null;
			
			return true;
		},
		
		_normalize: function normalize(handle, x,y)
		{
			var xPx = (x - handle._xMin) * handle._xNormalizationFactor;
			var yPx = (handle._yMax - y) * handle._yNormalizationFactor;
			
			return {x: xPx, y: yPx};
		},
	}
