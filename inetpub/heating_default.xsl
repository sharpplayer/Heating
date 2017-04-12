<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" encoding="ISO-8859-1" />

	<xsl:template match="/heating">
		<html>
			<head>
				<title>HRBC Heating Control</title>
				<script src="heating.js" />
			</head>
			<body>
				<div style="float:left">
					<a>
						<xsl:attribute name="href">index.php?_command=<xsl:value-of
							select="/heating/@home" />
						</xsl:attribute>
						Menu
					</a>
				</div>
				<div style="float:right">
					<a>
						<xsl:attribute name="href">index.php?_command=<xsl:value-of
							select="/heating/@home" />&amp;access=1</xsl:attribute>
						Admin
					</a>
				</div>
				<div style="clear:both">
					<xsl:apply-templates />
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="zones"></xsl:template>

	<xsl:template match="debugs">
		<h2>Debug</h2>
		<p>
			<xsl:for-each select="debug">
				<xsl:sort select="time" />
				<xsl:value-of select="time" />
				<xsl:text>:</xsl:text>
				<xsl:value-of select="class" />
				<xsl:text>-</xsl:text>
				<xsl:value-of select="message" />
				<br />
			</xsl:for-each>
		</p>
	</xsl:template>

	<xsl:template match="histories">
		<h2>History</h2>
		<form method="post">
			<xsl:text>History:</xsl:text>
			<input name="count" id="count" type="text">
				<xsl:attribute name="value"><xsl:value-of select="count(history)" />
				</xsl:attribute>
			</input>
			<input type="submit" value="Retrieve" />
		</form>
		<xsl:for-each select="history">
			<xsl:for-each select="response/*">
				<xsl:apply-templates select="." />
			</xsl:for-each>
			<h3>Arguments</h3>
			<p>
				<xsl:choose>
					<xsl:when test="args/arg">
						<xsl:for-each select="args/arg">
							<xsl:value-of select="@id" />
							<xsl:text>=</xsl:text>
							<xsl:value-of select="." />
							<br />
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>None</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</p>
			<h3>Information</h3>
			<p>
				<xsl:text>Source:</xsl:text>
				<xsl:variable name="src" select="source" />
				<xsl:value-of
					select="/heating/histories/sources/source[id=$src]/description" />
				<br />
				<xsl:text>Mode:</xsl:text>
				<xsl:value-of select="mode" />
				<br />
				<xsl:text>At:</xsl:text>
				<xsl:value-of select="format-number(timestamp/hour,'00')" />
				<xsl:text>:</xsl:text>
				<xsl:value-of select="format-number(timestamp/minute,'00')" />
				<xsl:text>:</xsl:text>
				<xsl:value-of select="format-number(timestamp/second,'00')" />
				<br />
				<xsl:text>On:</xsl:text>
				<xsl:value-of select="timestamp/day" />
				<xsl:text>/</xsl:text>
				<xsl:value-of select="timestamp/month" />
				<xsl:text>/</xsl:text>
				<xsl:value-of select="timestamp/year" />
				<br />
			</p>
			<hr />
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="error">
		<h2>Errors</h2>
		<p>
			<xsl:value-of select="message" />
			<br />
			<xsl:if test="sql">
				<xsl:value-of select="sql" />
				<br />
			</xsl:if>
			<xsl:if test="xml">
				<xsl:value-of select="xml" />
				<br />
			</xsl:if>
			<xsl:if test="url">
				<a>
					<xsl:attribute name="href"><xsl:value-of
						select="url" />
					</xsl:attribute>
					<xsl:value-of select="url" />
				</a>
				<br />
			</xsl:if>
			<xsl:if test="address">
				<label>Failed email to:</label>
				<xsl:value-of select="address" />
				<br />
				<label>Email body:</label>
				<xsl:value-of select="body" />
				<br />
			</xsl:if>
		</p>
	</xsl:template>

	<xsl:template match="log">
		<h2>Lead Times Log</h2>
		<form method="post">
			<label>Data Set Number:</label>
			<input name="set" type="text" />
			<br />
			<label>Start:</label>
			<select name="startday">
				<option value="">(Day)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						31
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/day" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="startmonth">
				<option value="">(Month)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						12
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/month" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="startyear">
				<option value="">(Year)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						2010
					</xsl:with-param>
					<xsl:with-param name="limit">
						2020
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/year" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<label>End:</label>
			<select name="endday">
				<option value="">(Day)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						31
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/end/day" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="endmonth">
				<option value="">(Month)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						12
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/end/month" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="endyear">
				<option value="">(Year)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						2010
					</xsl:with-param>
					<xsl:with-param name="limit">
						2020
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/end/year" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<label>Number of Intervals to process per poll:</label>
			<input name="intervals" type="text" />
			<br />
			<input type="submit" value="Start" />
		</form>
		<h2>Status</h2>
		<xsl:value-of select="result" />
	</xsl:template>


	<xsl:template match="zanalysis">
		<h2>Zimbra Report</h2>
		<form method="post">
			<xsl:call-template name="zones">
				<xsl:with-param name="selection" select="zone" />
				<xsl:with-param name="list" select="'Zone'" />
				<xsl:with-param name="occ" select="'true'" />
			</xsl:call-template>

			<label>
				Start:
			</label>
			<select name="fromday">
				<option value="">(Day)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						31
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/day" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="frommonth">
				<option value="">(Month)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						12
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/month" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="fromyear">
				<option value="">(Year)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						2010
					</xsl:with-param>
					<xsl:with-param name="limit">
						2020
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/year" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<label>
				End:
			</label>
			<select name="today">
				<option value="">(Day)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						31
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/day" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="tomonth">
				<option value="">(Month)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						12
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/month" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="toyear">
				<option value="">(Year)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						2010
					</xsl:with-param>
					<xsl:with-param name="limit">
						2020
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/year" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<input type="submit" value="Retrieve" />
		</form>
		<xsl:for-each select="zimbra">
			<xsl:for-each select="occupancy">
				<table>
					<tr>
						<td>Time</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:value-of select="d" />
							</td>
						</xsl:for-each>
					</tr>
					<tr>
						<td>Predicted Out</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:value-of select="o" />
							</td>
						</xsl:for-each>
					</tr>
					<tr>
						<td>Actual Out</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:variable name="t" select="t" />
								<xsl:variable name="out"
									select="/heating/zanalysis/oss/os[t &lt; $t][last()]/v" />
								<xsl:choose>
									<xsl:when test="$out">
										<xsl:value-of select="$out" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</xsl:for-each>
					</tr>
					<tr>
						<td>Actual In</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:variable name="t" select="t" />
								<xsl:variable name="in"
									select="/heating/zanalysis/iss/is[t &lt; $t][last()]/v" />
								<xsl:choose>
									<xsl:when test="$in">
										<xsl:value-of select="$in" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</xsl:for-each>
					</tr>
					<tr>
						<td>In</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:value-of select="i" />
							</td>
						</xsl:for-each>
					</tr>
					<tr>
						<td>Target</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:value-of select="r" />
							</td>
						</xsl:for-each>
					</tr>
					<tr>
						<td>Heat</td>
						<xsl:for-each select="plan/s">
							<td>
								<xsl:value-of select="h" />
							</td>
						</xsl:for-each>
					</tr>
				</table>
				<br />
			</xsl:for-each>
		</xsl:for-each>

	</xsl:template>


	<xsl:template match="report">
		<h2>Report</h2>
		<form method="post">
			<label>
				Start:
			</label>
			<select name="startday">
				<option value="">(Day)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						31
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/day" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="startmonth">
				<option value="">(Month)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						12
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/month" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="startyear">
				<option value="">(Year)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						2010
					</xsl:with-param>
					<xsl:with-param name="limit">
						2020
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/start/year" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<label>End:</label>
			<select name="endday">
				<option value="">(Day)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						31
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/end/day" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="endmonth">
				<option value="">(Month)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						1
					</xsl:with-param>
					<xsl:with-param name="limit">
						12
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/end/month" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="endyear">
				<option value="">(Year)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						2010
					</xsl:with-param>
					<xsl:with-param name="limit">
						2020
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="interval/end/year" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<label>Include non-polled data:</label>
			<input name="nopoll" type="checkbox" />
			<br />
			<label>Format:</label>
			<select name="_format">
				<option value="html">HTML</option>
				<option value="csv">CSV</option>
				<option value="xml">XML</option>
				<option value="xslt">XSLT</option>
			</select>
			<br />
			<input type="submit" value="Generate" />
		</form>
		<table>
			<tr>
				<td>n</td>
				<td>t</td>
				<xsl:for-each select="/heating/zones/zone">
					<td>
						<xsl:if test="type = 'Zone'">
							<xsl:attribute name="colspan">8</xsl:attribute>
						</xsl:if>
						<xsl:if test="type = 'Controller'">
							<xsl:attribute name="colspan"><xsl:value-of
								select="2 + count(cascades/cascade)" />
							</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</td>
				</xsl:for-each>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<xsl:for-each select="/heating/zones/zone">
					<xsl:if test="type = 'Zone'">
						<td>T</td>
					</xsl:if>
					<xsl:if test="type = 'Controller'">
						<td>OT</td>
					</xsl:if>
					<xsl:if test="type != 'Vents'">
						<td>F</td>
					</xsl:if>
					<xsl:if test="type = 'Zone'">
						<td>R</td>
						<td>S</td>
						<td>O</td>
						<td>OB</td>
						<td>OE</td>
						<td>V</td>
					</xsl:if>
					<xsl:if test="type = 'Vents'">
						<td>O</td>
					</xsl:if>
					<xsl:if test="type = 'Controller'">
						<xsl:for-each select="cascades/cascade">
							<td>
								<xsl:value-of select="name" />
							</td>
						</xsl:for-each>
					</xsl:if>
				</xsl:for-each>
			</tr>
			<xsl:for-each select="data">
				<xsl:variable name="cur" select="." />
				<xsl:variable name="pos" select="position()" />
				<tr>
					<td>
						<xsl:value-of select="$pos" />
					</td>
					<td>
						<xsl:value-of select="polltime/timestamp" />
					</td>
					<xsl:for-each select="/heating/zones/zone">
						<xsl:variable name="zonename" select="name" />
						<xsl:if test="type='Vents'">
							<td>
								<xsl:choose>
									<xsl:when test="$cur/occupied[zone = $zonename]">
										<xsl:value-of select="$cur/occupied[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</xsl:if>
						<xsl:if test="type!='Vents'">
							<td>
								<xsl:choose>
									<xsl:when test="$cur/temperature[zone = $zonename]/value">
										<xsl:value-of select="$cur/temperature[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td>
								<xsl:choose>
									<xsl:when test="$cur/flow[zone = $zonename]/value">
										<xsl:value-of select="$cur/flow[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</xsl:if>
						<xsl:if test="type='Zone'">
							<td>
								<xsl:choose>
									<xsl:when test="$cur/actual[zone = $zonename]">
										<xsl:value-of select="$cur/actual[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td>
								<xsl:choose>
									<xsl:when test="$cur/required[zone = $zonename]">
										<xsl:value-of select="$cur/required[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td>
								<xsl:choose>
									<xsl:when test="$cur/occupied[zone = $zonename]">
										<xsl:value-of select="$cur/occupied[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td>
								<xsl:choose>
									<xsl:when test="$cur/optoff[zone = $zonename]">
										<xsl:value-of select="$cur/optoff[zone = $zonename]/value" />
									</xsl:when>
									<xsl:when test="$cur/optimumon[zone = $zonename]">
										<xsl:value-of select="$cur/optimumon[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td>
								<xsl:choose>
									<xsl:when test="$cur/opton[zone = $zonename]">
										<xsl:value-of select="$cur/opton[zone = $zonename]/value" />
									</xsl:when>
									<xsl:when test="$cur/optimumoff[zone = $zonename]">
										<xsl:value-of select="$cur/optimumoff[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td>
								<xsl:choose>
									<xsl:when test="$cur/valve[zone = $zonename]/value">
										<xsl:value-of select="$cur/valve[zone = $zonename]/value" />
									</xsl:when>
									<xsl:otherwise>
										-
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</xsl:if>
						<xsl:if test="type='Controller'">
							<xsl:for-each select="cascades/cascade">
								<xsl:variable name="cascadename" select="name" />
								<td>
									<xsl:choose>
										<xsl:when
											test="$cur/boiler[zone = $zonename]/state[cascade = $cascadename]/value">
											<xsl:value-of
												select="$cur/boiler[zone = $zonename]/state[cascade = $cascadename]/value" />
										</xsl:when>
										<xsl:otherwise>
											-
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</xsl:for-each>
						</xsl:if>
					</xsl:for-each>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>


	<xsl:template match="system[@mode='default']">
		<h2>System Status</h2>
		<p>
			<xsl:value-of select="status" />
		</p>
	</xsl:template>

	<xsl:template match="time[@mode='default']">
		<h2>System Times</h2>
		<p>
			Server:
			<xsl:value-of select="local/hour" />
			<xsl:text>:</xsl:text>
			<xsl:value-of select="local/minute" />
			<br />
			Heating:
			<xsl:value-of select="remote/hour" />
			<xsl:text>:</xsl:text>
			<xsl:value-of select="remote/minute" />
			<br />
		</p>
	</xsl:template>

	<xsl:template match="time[@mode='edit']">
		<h2>Set System Time</h2>
		<form method="post">
			<label>Week Day:</label>
			<select name="day">
				<option value="system">(As Server)</option>
				<option value="1">
					<xsl:if test="remote/weekday=1">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Sunday
				</option>
				<option value="2">
					<xsl:if test="remote/weekday=2">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Monday
				</option>
				<option value="3">
					<xsl:if test="remote/weekday=3">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Tuesday
				</option>
				<option value="4">
					<xsl:if test="remote/weekday=4">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Wednesday
				</option>
				<option value="5">
					<xsl:if test="remote/weekday=5">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Thursday
				</option>
				<option value="6">
					<xsl:if test="remote/weekday=6">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Friday
				</option>
				<option value="7">
					<xsl:if test="remote/weekday=7">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Saturday
				</option>
			</select>
			<br />
			<label>Time:</label>
			<select name="hour">
				<option value="system">(As Server)</option>
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						0
					</xsl:with-param>
					<xsl:with-param name="limit">
						23
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="remote/hour" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<select name="min">
				<xsl:call-template name="simpleloop">
					<xsl:with-param name="val">
						0
					</xsl:with-param>
					<xsl:with-param name="limit">
						59
					</xsl:with-param>
					<xsl:with-param name="selection">
						<xsl:value-of select="remote/minute" />
					</xsl:with-param>
				</xsl:call-template>
			</select>
			<br />
			<label>Server:</label>
			<xsl:value-of select="local/hour" />
			<xsl:text>:</xsl:text>
			<xsl:value-of select="local/minute" />
			<br />
			<input type="submit" value="Update" />
		</form>
	</xsl:template>
	<xsl:template match="housekeeping[@mode='default']">
		<h2>Housekeeping Status</h2>
		<p>
			<xsl:value-of select="status" />
		</p>
		<p>
			Select data to delete that is older that the house keeping
			time in
			the configuration options.
		</p>
		<form method="post">
			<select name="delete">
				<option value="nondata">All non data</option>
				<option value="web">Web data</option>
				<option value="cmd">Command line data</option>
				<option value="alert">Alert data</option>
				<option value="poll">Poll data</option>
				<option value="pending">
					Completed pending commands
				</option>
				<option value="data">Historical data</option>
				<option value="all">Empty database!</option>
			</select>
			<input type="submit" value="Delete" />
		</form>
	</xsl:template>


	<xsl:template match="system[@mode='edit']">
		<h2>System Status</h2>
		<xsl:if test="status = 'Running'">
			<form method="post">
				<select name="status" id="status">
					<xsl:for-each select="statuses/status">
						<option>
							<xsl:attribute name="value"><xsl:value-of
								select="name" />
						</xsl:attribute>
							<xsl:value-of select="description" />
						</option>
					</xsl:for-each>
				</select>
				<input type="submit" value="Update" />
			</form>
		</xsl:if>
		<p>
			<xsl:value-of select="status" />
		</p>
	</xsl:template>

	<xsl:template name="zones">
		<xsl:param name="selection" />
		<xsl:param name="list" />
		<xsl:param name="temperature" />
		<xsl:param name="occ" />
		<xsl:text>Zones</xsl:text>
		<ul>
			<xsl:for-each select="/heating/zones/zone">
				<xsl:if
					test="($list=type) or ($list='All') or ($occ='true' and type='Vents')">
					<li>
						<a>
							<xsl:attribute name="href">index.php?_command=<xsl:value-of
								select="/heating/@current" />&amp;zone=<xsl:value-of
								select="name" />
						</xsl:attribute>
							<xsl:if test="$temperature!='true' or type!='Controller'">
								<xsl:value-of select="description" />
							</xsl:if>
							<xsl:if test="$temperature='true' and type='Controller'">
								<xsl:text>Outside</xsl:text>
							</xsl:if>
						</a>
					</li>
				</xsl:if>
			</xsl:for-each>
		</ul>
		<h3>
			<xsl:if
				test="$temperature!='true' or /heating/zones/zone[name = $selection]/type!='Controller'">
				<xsl:value-of select="/heating/zones/zone[name = $selection]/description" />
			</xsl:if>
			<xsl:if
				test="$temperature='true' and /heating/zones/zone[name = $selection]/type='Controller'">
				<xsl:text>Outside</xsl:text>
			</xsl:if>
		</h3>
	</xsl:template>

	<xsl:template match="occupied[@mode='default']">
		<h2>Occupied</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
			<xsl:with-param name="occ" select="'true'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="boiler[@mode='default']">
		<h2>Boiler State</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Controller'" />
		</xsl:call-template>
		<xsl:for-each select="state">
			<p>
				<xsl:value-of select="cascade" />
				<xsl:text> is </xsl:text>
				<xsl:value-of select="value" />
			</p>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="optimumon[@mode='default']">
		<h2>Optimum Start Enabled</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="optimumoff[@mode='default']">
		<h2>Optimum Stop Enabled</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="flow[@mode='default']">
		<h2>Flow Temperature</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'All'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="actual[@mode='default']">
		<h2>Required Temperature</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="required[@mode='default']">
		<h2>Set Point Temperature</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="required[@mode='edit']">
		<h2>Required Temperature Edit</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<form method="post">
			<input name="zone" type="hidden" id="zone">
				<xsl:attribute name="value"><xsl:value-of select="zone" />
				</xsl:attribute>
			</input>
			<xsl:text>Temperature:</xsl:text>
			<input name="required" id="required" type="text">
				<xsl:attribute name="value"><xsl:value-of select="value" />
				</xsl:attribute>
			</input>
			<input type="submit" value="Update" />
		</form>
	</xsl:template>

	<xsl:template match="configs[@mode='default']">
		<h2>Configuration</h2>
		<p>
			<xsl:for-each select="config">
				<label>
					<xsl:value-of select="description" />
					:
				</label>
				<label>
					<xsl:value-of select="value" />
				</label>
				<br />
			</xsl:for-each>
		</p>
	</xsl:template>

	<xsl:template match="configs[@mode='edit']">
		<h2>Configuration</h2>
		<form method="post">
			<xsl:for-each select="config">
				<label>
					<xsl:value-of select="description" />
					:
				</label>
				<xsl:choose>
					<xsl:when test="modifiable='1'">
						<input type="hidden">
							<xsl:attribute name="name">
										        <xsl:text>id</xsl:text><xsl:value-of
								select="1 + count(preceding-sibling::config[modifiable='1'])" />
									        </xsl:attribute>
							<xsl:attribute name="value">
										        <xsl:value-of select="id" />
									        </xsl:attribute>
						</input>
						<input type="text">
							<xsl:attribute name="value"><xsl:value-of
								select="value" />
									</xsl:attribute>
							<xsl:attribute name="name">val<xsl:value-of
								select="1 + count(preceding-sibling::config[modifiable='1'])" />
									</xsl:attribute>
						</input>
					</xsl:when>
					<xsl:otherwise>
						<!-- <input type="hidden"> <xsl:attribute name="value"><xsl:value-of 
							select="value" /> </xsl:attribute> <xsl:attribute name="name">val<xsl:value-of 
							select="position()" /> </xsl:attribute> </input> -->
						<label>
							<xsl:value-of select="value" />
						</label>
					</xsl:otherwise>
				</xsl:choose>
				<br />
			</xsl:for-each>
			<input type="submit" value="Update" />
		</form>
	</xsl:template>

	<xsl:template match="zimbra[@mode='default']">
		<h2>Zimbra Processing</h2>
		<xsl:if test="errors">
			<h3>
				Errors
			</h3>
			<ul>
				<xsl:for-each select="errors/error">
					<li>
						<xsl:value-of select="." />
					</li>
				</xsl:for-each>
			</ul>
		</xsl:if>
		<form method="post">
			<label>Username:</label>
			<input name="username" type="text" />
			<br />
			<label>Password:</label>
			<input name="password" type="password" />
			<br />
			<label>Action:</label>
			<select name="action">
				<option value="preview">Preview</option>
				<option value="command">Issue Command To Heating System</option>
			</select>
			<br />
			<input type="submit" value="Go" />
		</form>
		<xsl:for-each select="occupancy">
			<h3>
				Zone:
				<xsl:variable name="zone" select="zone" />
				<xsl:value-of select="/heating/zones/zone[name = $zone]/description" />
			</h3>
			<xsl:call-template name="occstable" />
			<table>
				<tr>
					<td>Time</td>
					<xsl:for-each select="plan/s">
						<td>
							<xsl:value-of select="d" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<td>Out</td>
					<xsl:for-each select="plan/s">
						<td>
							<xsl:value-of select="o" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<td>In</td>
					<xsl:for-each select="plan/s">
						<td>
							<xsl:value-of select="i" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<td>Target</td>
					<xsl:for-each select="plan/s">
						<td>
							<xsl:value-of select="r" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<td>Heat</td>
					<xsl:for-each select="plan/s">
						<td>
							<xsl:value-of select="h" />
						</td>
					</xsl:for-each>
				</tr>
			</table>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="occupancy[@mode='default']">
		<h2>Occupancy</h2>
		<p>
			<xsl:call-template name="zones">
				<xsl:with-param name="selection" select="zone" />
				<xsl:with-param name="list" select="'Zone'" />
				<xsl:with-param name="occ" select="'true'" />
			</xsl:call-template>
			<xsl:call-template name="occstable" />
		</p>
	</xsl:template>

	<xsl:template name="occstable">
		<table>
			<tr>
				<td>Day</td>
				<td>In</td>
				<td>Out</td>
				<td>In</td>
				<td>Out</td>
				<td>In</td>
				<td>Out</td>
			</tr>
			<xsl:for-each select="day">
				<xsl:sort select="weekday" />
				<tr>
					<td>
						<xsl:choose>
							<xsl:when test="weekday=1">
								Sunday
							</xsl:when>
							<xsl:when test="weekday=2">
								Monday
							</xsl:when>
							<xsl:when test="weekday=3">
								Tuesday
							</xsl:when>
							<xsl:when test="weekday=4">
								Wednesday
							</xsl:when>
							<xsl:when test="weekday=5">
								Thursday
							</xsl:when>
							<xsl:when test="weekday=6">
								Friday
							</xsl:when>
							<xsl:when test="weekday=7">
								Saturday
							</xsl:when>
						</xsl:choose>
					</td>
					<xsl:for-each select="times/*">
						<td>
							<xsl:choose>
								<xsl:when test="position() mod 2 = 0">
									<xsl:attribute name="width">100</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="width">40</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:value-of select="format-number(floor(. div 60),'00')" />
							<xsl:text>:</xsl:text>
							<xsl:value-of select="format-number(. mod 60,'00')" />
						</td>
					</xsl:for-each>
				</tr>
			</xsl:for-each>
		</table>

	</xsl:template>

	<xsl:template match="occupancy[@mode='edit']">
		<h2>Occupancy Edit</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
			<xsl:with-param name="occ" select="'true'" />
		</xsl:call-template>
		<form method="post">
			<input name="zone" type="hidden" id="zone">
				<xsl:attribute name="value"><xsl:value-of select="zone" />
				</xsl:attribute>
			</input>
			<table id="occs">
				<tr>
					<td>Day</td>
					<td>In</td>
					<td>Out</td>
					<td>In</td>
					<td>Out</td>
					<td>In</td>
					<td>Out</td>
				</tr>
				<xsl:for-each select="day">
					<xsl:sort select="weekday" />
					<tr>
						<td>
							<xsl:choose>
								<xsl:when test="weekday=1">
									Sunday
								</xsl:when>
								<xsl:when test="weekday=2">
									Monday
								</xsl:when>
								<xsl:when test="weekday=3">
									Tuesday
								</xsl:when>
								<xsl:when test="weekday=4">
									Wednesday
								</xsl:when>
								<xsl:when test="weekday=5">
									Thursday
								</xsl:when>
								<xsl:when test="weekday=6">
									Friday
								</xsl:when>
								<xsl:when test="weekday=7">
									Saturday
								</xsl:when>
							</xsl:choose>
						</td>
						<xsl:for-each select="times/*">
							<td>
								<xsl:choose>
									<xsl:when test="position() mod 2 = 0">
										<xsl:attribute name="width">100</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="width">40</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
								<select>
									<xsl:attribute name="id">
										<xsl:value-of select="@id" />
									</xsl:attribute>
									<xsl:attribute name="name">
										<xsl:value-of select="@id" />
									</xsl:attribute>
									<xsl:call-template name="loop">
										<xsl:with-param name="time">
											0
										</xsl:with-param>
									</xsl:call-template>
								</select>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
			</table>
			<input type="submit" value="Update" onclick="return validateOccupancy()" />
		</form>
	</xsl:template>

	<xsl:template name="simpleloop">
		<xsl:param name="val" />
		<xsl:param name="limit" />
		<xsl:param name="selection" />
		<xsl:if test="$val &lt;= $limit">
			<option>
				<xsl:attribute name="value"><xsl:value-of select="$val" />
				</xsl:attribute>
				<xsl:if test="number($selection) = number($val)">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$val" />
			</option>
			<xsl:call-template name="simpleloop">
				<xsl:with-param name="val">
					<xsl:number value="number($val) + 1" />
				</xsl:with-param>
				<xsl:with-param name="limit">
					<xsl:number value="$limit" />
				</xsl:with-param>
				<xsl:with-param name="selection">
					<xsl:number value="$selection" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>


	<xsl:template name="loop">
		<xsl:param name="time" />
		<xsl:if test="$time &lt; 1441">
			<option>
				<xsl:attribute name="value"><xsl:value-of select="$time" />
				</xsl:attribute>
				<xsl:if test=". = $time">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="format-number(floor($time div 60),'00')" />
				<xsl:text>:</xsl:text>
				<xsl:value-of select="format-number($time mod 60,'00')" />
			</option>
			<xsl:call-template name="loop">
				<xsl:with-param name="time">
					<xsl:number value="number($time) + 10" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template match="commands[@mode='default']">
		<h2>Commands</h2>
		<p>
			<xsl:for-each select="group">
				<xsl:sort select="modifiable" />
				<xsl:sort select="description" />
				<a>
					<xsl:attribute name="href">index.php?_command=<xsl:value-of
						select="command" />&amp;_mode=<xsl:value-of select="mode" />
							</xsl:attribute>
					<xsl:value-of select="description" />
				</a>
				<xsl:text> </xsl:text>
				<xsl:if test="modifiable = 0">
					<a>
						<xsl:attribute name="href">index.php?_command=<xsl:value-of
							select="/heating/@commandedit" />&amp;commandid=<xsl:value-of
							select="command" />&amp;_mode=edit</xsl:attribute>
						Access
					</a>
				</xsl:if>
				<xsl:if test="modifiable = 1">
					<a>
						<xsl:attribute name="href">index.php?_command=<xsl:value-of
							select="/heating/@commandedit" />&amp;commandid=<xsl:value-of
							select="command" />&amp;_mode=edit</xsl:attribute>
						Edit
					</a>
				</xsl:if>
				<br />
			</xsl:for-each>
		</p>
	</xsl:template>

	<xsl:template match="poll[@mode='edit']">
		<h2>Poll</h2>
		<form method="post">
			<xsl:if test="pollid">
				<input type="hidden" name="pollid">
					<xsl:attribute name="value"><xsl:value-of
						select="pollid" />
						</xsl:attribute>
				</input>
			</xsl:if>
			<label>Command:</label>
			<select name="group">
				<xsl:for-each select="commands/command">
					<xsl:sort select="description" />
					<option>
						<xsl:attribute name="value"><xsl:value-of
							select="id" />
						</xsl:attribute>
						<xsl:if test="../../group = id">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</option>
				</xsl:for-each>
			</select>
			<br />
			<label>Interval:</label>
			<select name="interval">
				<option value="60000">
					<xsl:if test="number(interval) = 60000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every Minute
				</option>
				<option value="300000">
					<xsl:if test="number(interval) = 300000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every 5 Minutes
				</option>
				<option value="600000">
					<xsl:if test="number(interval) = 600000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every 10 Minutes
				</option>
				<option value="900000">
					<xsl:if test="number(interval) = 900000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every 15 Minutes
				</option>
				<option value="1800000">
					<xsl:if test="number(interval) = 1800000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every 30 Minutes
				</option>
				<option value="3600000">
					<xsl:if test="number(interval) = 3600000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every Hour
				</option>
				<option value="86400000">
					<xsl:if test="number(interval) = 86400000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every Day
				</option>
				<option value="604800000">
					<xsl:if test="number(interval) = 604800000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					Every Week
				</option>
			</select>
			<br />
			<label>Offset:</label>
			<select name="offset">
				<option value="0">
					<xsl:if test="number(offset) = 0">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					None
				</option>
				<option value="30000">
					<xsl:if test="number(offset) = 30000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					30 Seconds
				</option>
				<option value="60000">
					<xsl:if test="number(offset) = 60000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					1 Minute
				</option>
				<option value="300000">
					<xsl:if test="number(offset) = 300000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					5 Minute
				</option>
				<option value="3600000">
					<xsl:if test="number(offset) = 3600000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					1 Hour
				</option>
				<option value="7200000">
					<xsl:if test="number(offset) = 7200000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					2 Hour
				</option>
				<option value="10800000">
					<xsl:if test="number(offset) = 10800000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					3 Hour
				</option>
				<option value="86400000">
					<xsl:if test="number(offset) = 86400000">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					1 Day
				</option>
			</select>
			<br />
			<label>Active:</label>
			<input name="active" type="checkbox">
				<xsl:if test="number(active) = 1">
					<xsl:attribute name="checked">true</xsl:attribute>
				</xsl:if>
			</input>
			<br />
			<label>Arguments:</label>
			<div id="args">
				<xsl:choose>
					<xsl:when test="args/arg">
						<xsl:for-each select="args/arg">
							<div>
								<xsl:attribute name="id">divarg<xsl:value-of
									select="position()" />
								</xsl:attribute>
								<input type="text">
									<xsl:attribute name="id">arg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="name">arg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="@id" />
								</xsl:attribute>
								</input>
								<input type="text">
									<xsl:attribute name="id">val<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="name">val<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="." />
								</xsl:attribute>
								</input>
								<input type="button" value="+">
									<xsl:attribute name="onclick">
										javascript:return addArg (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
									<xsl:attribute name="id">_bparg<xsl:value-of
										select="position()" />
								</xsl:attribute>
								</input>
								<input type="button" value="-">
									<xsl:attribute name="id">_bmarg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="onclick">
										javascript:return removeArg (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
								</input>
							</div>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<div id="divarg1">
							<input type="text" id="arg1" name="arg1" />
							<input type="text" id="val1" name="val1" />
							<input type="button" onclick="javascript:return addArg(1)"
								id="_bparg1" value="+" />
							<input type="button" onClick="javascript:return removeArg(1)"
								id="_bmarg1" value="-" />
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<xsl:choose>
				<xsl:when test="pollid">
					<input type="submit" value="Update" />
				</xsl:when>
				<xsl:otherwise>
					<input type="submit" value="Create" />
				</xsl:otherwise>
			</xsl:choose>
		</form>
	</xsl:template>

	<xsl:template match="condition[@mode='edit']">
		<h2>Condition</h2>
		<form method="post">
			<label>Description:</label>
			<xsl:if test="conditionid">
				<input type="hidden" name="conditionid">
					<xsl:attribute name="value"><xsl:value-of
						select="conditionid" />
						</xsl:attribute>
				</input>
			</xsl:if>
			<input name="description" type="text">
				<xsl:attribute name="value"><xsl:value-of select="description" />
						</xsl:attribute>
			</input>
			<br />
			<label>Conditions:</label>
			<div id="conds">
				<xsl:choose>
					<xsl:when test="conds/condition">
						<xsl:for-each select="conds/condition">
							<xsl:variable name="condid" select="." />
							<div>
								<xsl:attribute name="id">div<xsl:value-of
									select="position()" />
								</xsl:attribute>
								<select>
									<xsl:attribute name="id">condition<xsl:value-of
										select="position()" />
									</xsl:attribute>
									<xsl:attribute name="name">condition<xsl:value-of
										select="position()" />
									</xsl:attribute>
									<xsl:for-each select="../../conditions/condition">
										<xsl:sort select="description" />
										<option>
											<xsl:if test="id = $condid">
												<xsl:attribute name="selected">true</xsl:attribute>
											</xsl:if>
											<xsl:attribute name="value"><xsl:value-of
												select="id" />
										</xsl:attribute>
											<xsl:value-of select="description" />
										</option>
									</xsl:for-each>
								</select>
								<input type="button" value="+">
									<xsl:attribute name="onclick">
										javascript:return addCond (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
									<xsl:attribute name="id">_bp<xsl:value-of
										select="position()" />
								</xsl:attribute>
								</input>
								<input type="button" value="-">
									<xsl:attribute name="id">_bm<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="onclick">
										javascript:return removeCond (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
								</input>
							</div>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<div id="div1">
							<select id="condition1" name="condition1">
								<xsl:for-each select="conditions/condition">
									<option>
										<xsl:attribute name="value"><xsl:value-of
											select="id" />
										</xsl:attribute>
										<xsl:value-of select="description" />
									</option>
								</xsl:for-each>
							</select>
							<input type="button" onclick="javascript:return addCond(1)"
								id="_bp1" value="+" />
							<input type="button" onClick="javascript:return removeCond(1)"
								id="_bm1" value="-" />
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<xsl:choose>
				<xsl:when test="conditionid">
					<input type="submit" value="Update" />
				</xsl:when>
				<xsl:otherwise>
					<input type="submit" value="Create" />
				</xsl:otherwise>
			</xsl:choose>
		</form>
	</xsl:template>

	<xsl:template match="command[@mode='edit']">
		<h2>Command Group</h2>
		<form method="post">
			<label>Description:</label>
			<xsl:if test="commandid">
				<input type="hidden" name="commandid">
					<xsl:attribute name="value"><xsl:value-of
						select="commandid" />
						</xsl:attribute>
				</input>
			</xsl:if>
			<xsl:if test="modifiable=1">
				<input name="description" type="text">
					<xsl:attribute name="value"><xsl:value-of
						select="description" />
						</xsl:attribute>
				</input>
			</xsl:if>
			<xsl:if test="modifiable=0">
				<xsl:value-of select="description" />
			</xsl:if>
			<br />
			Access:
			<select name="access">
				<option value="1">
					<xsl:if test="access=1">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					<xsl:text>Administrator</xsl:text>
				</option>
				<option value="2">
					<xsl:if test="access=2">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
					<xsl:text>User</xsl:text>
				</option>
			</select>
			<br />
			<xsl:if test="modifiable=1">
				<label>Commands:</label>
				<div id="conds">
					<xsl:choose>
						<xsl:when test="cmds/command">
							<xsl:for-each select="cmds/command">
								<xsl:variable name="cmdid" select="." />
								<div>
									<xsl:attribute name="id">div<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<select>
										<xsl:attribute name="id">condition<xsl:value-of
											select="position()" />
									</xsl:attribute>
										<xsl:attribute name="name">condition<xsl:value-of
											select="position()" />
									</xsl:attribute>
										<xsl:for-each select="../../commands/command">
											<xsl:sort select="description" />
											<option>
												<xsl:if test="id = $cmdid">
													<xsl:attribute name="selected">true</xsl:attribute>
												</xsl:if>
												<xsl:attribute name="value"><xsl:value-of
													select="id" />
										</xsl:attribute>
												<xsl:value-of select="description" />
											</option>
										</xsl:for-each>
									</select>
									<input type="button" value="+">
										<xsl:attribute name="onclick">
										javascript:return addCond (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
										<xsl:attribute name="id">_bp<xsl:value-of
											select="position()" />
								</xsl:attribute>
									</input>
									<input type="button" value="-">
										<xsl:attribute name="id">_bm<xsl:value-of
											select="position()" />
								</xsl:attribute>
										<xsl:attribute name="onclick">
										javascript:return removeCond (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
									</input>
								</div>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<div id="div1">
								<select id="condition1" name="condition1">
									<xsl:for-each select="commands/command">
										<option>
											<xsl:attribute name="value"><xsl:value-of
												select="id" />
										</xsl:attribute>
											<xsl:value-of select="description" />
										</option>
									</xsl:for-each>
								</select>
								<input type="button" onclick="javascript:return addCond(1)"
									id="_bp1" value="+" />
								<input type="button" onClick="javascript:return removeCond(1)"
									id="_bm1" value="-" />
							</div>
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="commandid">
					<input type="submit" value="Update" />
				</xsl:when>
				<xsl:otherwise>
					<input type="submit" value="Create" />
				</xsl:otherwise>
			</xsl:choose>
		</form>
	</xsl:template>

	<xsl:template match="alert[@mode='edit']">
		<h2>Alert</h2>
		<form method="post">
			<label>Description:</label>
			<xsl:if test="alertid">
				<input type="hidden" name="alertid">
					<xsl:attribute name="value"><xsl:value-of
						select="alertid" />
						</xsl:attribute>
				</input>
			</xsl:if>
			<input name="description" type="text">
				<xsl:attribute name="value"><xsl:value-of select="description" />
						</xsl:attribute>
			</input>
			<br />
			<label>Command:</label>
			<select name="group">
				<xsl:for-each select="commands/command">
					<option>
						<xsl:attribute name="value"><xsl:value-of
							select="id" />
						</xsl:attribute>
						<xsl:if test="../../group = id">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</option>
				</xsl:for-each>
			</select>
			<br />
			<label>Source:</label>
			<select name="source">
				<xsl:for-each select="sources/source">
					<option>
						<xsl:attribute name="value"><xsl:value-of
							select="id" />
						</xsl:attribute>
						<xsl:if test="../../source = id">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</option>
				</xsl:for-each>
			</select>
			<br />
			<label>Condition:</label>
			<select name="condition">
				<xsl:for-each select="conditions/condition">
					<option>
						<xsl:attribute name="value"><xsl:value-of
							select="id" />
						</xsl:attribute>
						<xsl:if test="../../condition = id">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</option>
				</xsl:for-each>
			</select>
			<br />
			<label>Alert:</label>
			<select name="alert">
				<xsl:for-each select="commands/command">
					<option>
						<xsl:attribute name="value"><xsl:value-of
							select="id" />
						</xsl:attribute>
						<xsl:if test="../../alert = id">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</option>
				</xsl:for-each>
			</select>
			<br />
			<label>Arguments:</label>
			<div id="args">
				<xsl:choose>
					<xsl:when test="condargs/args/arg">
						<xsl:for-each select="condargs/args/arg">
							<div>
								<xsl:attribute name="id">divarg<xsl:value-of
									select="position()" />
								</xsl:attribute>
								<input type="text">
									<xsl:attribute name="id">arg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="name">arg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="@id" />
								</xsl:attribute>
								</input>
								<input type="text">
									<xsl:attribute name="id">val<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="name">val<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="." />
								</xsl:attribute>
								</input>
								<input type="button" value="+">
									<xsl:attribute name="onclick">
										javascript:return addArg (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
									<xsl:attribute name="id">_bparg<xsl:value-of
										select="position()" />
								</xsl:attribute>
								</input>
								<input type="button" value="-">
									<xsl:attribute name="id">_bmarg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="onclick">
										javascript:return removeArg (
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
								</input>
							</div>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<div id="divarg1">
							<input type="text" id="arg1" name="arg1" />
							<input type="text" id="val1" name="val1" />
							<input type="button" onclick="javascript:return addArg(1)"
								id="_bparg1" value="+" />
							<input type="button" onClick="javascript:return removeArg(1)"
								id="_bmarg1" value="-" />
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<br />
			<br />
			<label>Recovery:</label>
			<select name="recover">
				<xsl:for-each select="commands/command">
					<option>
						<xsl:attribute name="value"><xsl:value-of
							select="id" />
						</xsl:attribute>
						<xsl:if test="../../alert = id">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="description" />
					</option>
				</xsl:for-each>
			</select>
			<br />
			<label>Arguments:</label>
			<div id="rargs">
				<xsl:choose>
					<xsl:when test="recargs/args/arg">
						<xsl:for-each select="recargs/args/arg">
							<div>
								<xsl:attribute name="id">divrarg<xsl:value-of
									select="position()" />
								</xsl:attribute>
								<input type="text">
									<xsl:attribute name="id">rarg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="name">rarg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="@id" />
								</xsl:attribute>
								</input>
								<input type="text">
									<xsl:attribute name="id">rval<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="name">rval<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="." />
								</xsl:attribute>
								</input>
								<input type="button" value="+">
									<xsl:attribute name="onclick">
										javascript:return addAnyArg ('rarg', 'rval',
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
									<xsl:attribute name="id">_bprarg<xsl:value-of
										select="position()" />
								</xsl:attribute>
								</input>
								<input type="button" value="-">
									<xsl:attribute name="id">_bmrarg<xsl:value-of
										select="position()" />
								</xsl:attribute>
									<xsl:attribute name="onclick">
										javascript:return removeAnyArg ('rarg', 'rval',
										<xsl:value-of select="position()" />
										)
									</xsl:attribute>
								</input>
							</div>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<div id="divrarg1">
							<input type="text" id="rarg1" name="rarg1" />
							<input type="text" id="rval1" name="rval1" />
							<input type="button" onclick="javascript:return addAnyArg('rarg','rval',1)"
								id="_bprarg1" value="+" />
							<input type="button"
								onClick="javascript:return removeAnyArg('rarg','rval',1)" id="_bmrarg1"
								value="-" />
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<label>Recovery delay:</label>
			<input name="rdelay" type="text">
				<xsl:attribute name="value"><xsl:value-of select="delay" />
						</xsl:attribute>
			</input>
			<br />
			<xsl:choose>
				<xsl:when test="alertid">
					<input type="submit" value="Update" />
				</xsl:when>
				<xsl:otherwise>
					<input type="submit" value="Create" />
				</xsl:otherwise>
			</xsl:choose>
		</form>
	</xsl:template>

	<xsl:template match="alerts[@mode='default']">
		<h2>Alerts</h2>
		<p>
			<xsl:for-each select="alert">
				<a>
					<xsl:attribute name="href">index.php?_mode=edit&amp;_command=<xsl:value-of
						select="/heating/@alertedit" />&amp;alertid=<xsl:value-of
						select="id" />
							</xsl:attribute>
					<xsl:value-of select="description" />
				</a>
				<br />
			</xsl:for-each>
		</p>
		<p>
			<a>
				<xsl:attribute name="href">index.php?_mode=edit&amp;_command=<xsl:value-of
					select="/heating/@alertedit" />
				</xsl:attribute>
				New Alert
			</a>
		</p>
	</xsl:template>

	<xsl:template match="conditions[@mode='default']">
		<h2>Conditions</h2>
		<p>
			<xsl:for-each select="condition">
				<a>
					<xsl:attribute name="href">index.php?_mode=edit&amp;_command=<xsl:value-of
						select="/heating/@conditionedit" />&amp;conditionid=<xsl:value-of
						select="id" />
							</xsl:attribute>
					<xsl:value-of select="description" />
				</a>
				<br />
			</xsl:for-each>
		</p>
		<p>
			<a>
				<xsl:attribute name="href">index.php?_mode=edit&amp;_command=<xsl:value-of
					select="/heating/@conditionedit" />
				</xsl:attribute>
				New Condition
			</a>
		</p>
	</xsl:template>

	<xsl:template match="polls[@mode='default']">
		<h2>Polls</h2>
		<p>
			<xsl:for-each select="poll">
				<a>
					<xsl:attribute name="href">index.php?_mode=edit&amp;_command=<xsl:value-of
						select="/heating/@polledit" />&amp;pollid=<xsl:value-of
						select="id" />
							</xsl:attribute>
					<xsl:value-of select="description" />
					<xsl:if test="number(active) = 1">
						(Active)
					</xsl:if>
				</a>
				<br />
			</xsl:for-each>
		</p>
		<p>
			<a>
				<xsl:attribute name="href">index.php?_mode=edit&amp;_command=<xsl:value-of
					select="/heating/@polledit" />
				</xsl:attribute>
				New Poll
			</a>
		</p>
	</xsl:template>

	<xsl:template match="temperature[@mode='default']">
		<h2>Temperature</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'All'" />
			<xsl:with-param name="temperature" select="'true'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="valve[@mode='default']">
		<h2>Valve Opening</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="spno[@mode='default']">
		<h2>Set Point Not Occupied (Frost) Temperature</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<p>
			<xsl:value-of select="value" />
		</p>
	</xsl:template>

	<xsl:template match="spno[@mode='edit']">
		<h2>Set Point Not Occupied (Frost) Temperature</h2>
		<xsl:call-template name="zones">
			<xsl:with-param name="selection" select="zone" />
			<xsl:with-param name="list" select="'Zone'" />
		</xsl:call-template>
		<form method="post">
			<input name="zone" type="hidden" id="zone">
				<xsl:attribute name="value"><xsl:value-of select="zone" />
				</xsl:attribute>
			</input>
			<xsl:text>Temperature:</xsl:text>
			<input name="spno" id="spno" type="text">
				<xsl:attribute name="value"><xsl:value-of select="value" />
				</xsl:attribute>
			</input>
			<input type="submit" value="Update" />
		</form>
	</xsl:template>

	<xsl:template match="aggregates[@mode='default']">
		<h2>Aggregate Times</h2>
		<form method="post">
			<label>Data Set:</label>
			<input type="text" name="reset">
				<xsl:attribute name="value">
				</xsl:attribute>
			</input>
			<br />
			<label>Zone:</label>
			<input type="text" name="zone">
				<xsl:attribute name="value"><xsl:value-of select="zone" />
				</xsl:attribute>
			</input>
			<br />
			<input name="view" type="submit" value="View" />
		</form>
		<xsl:call-template name="timedisplay">
			<xsl:with-param name="readonly">
				1
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="aggregates[@mode='edit']">
		<h2>Aggregate Times</h2>
		<form method="post">
			<label>Reset To Data Set:</label>
			<input type="text" name="reset">
				<xsl:attribute name="value">
				</xsl:attribute>
			</input>
			<br />
			<label>Zone:</label>
			<input type="text" name="zone">
				<xsl:attribute name="value"><xsl:value-of select="zone" />
				</xsl:attribute>
			</input>
			<br />
			<input type="submit" value="Reset" />
			<input name="view" type="submit" value="View" />
			<xsl:call-template name="timedisplay">
				<xsl:with-param name="readonly">
					0
				</xsl:with-param>
			</xsl:call-template>
		</form>
	</xsl:template>

	<xsl:template match="leadtimes[@mode='default']">
		<h2>Lead Times</h2>
		<form method="post">
			<label>Data Set:</label>
			<input type="text" name="set">
				<xsl:attribute name="value"><xsl:value-of select="set" />
				</xsl:attribute>
			</input>
			<br />
			<label>Zone:</label>
			<input type="text" name="zone">
				<xsl:attribute name="value"><xsl:value-of select="zone" />
				</xsl:attribute>
			</input>
			<br />
			<input type="submit" value="Retrieve" />
		</form>
		<xsl:call-template name="timedisplay">
			<xsl:with-param name="readonly">
				1
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="timedisplay">
		<xsl:param name="readonly" />
		<style>
			tr:nth-child(odd) { background-color: rgba(192,192,192,0.5)}
			td:nth-child(odd) { background-color: rgba(192,192,192,0.5)}
			.divs
			{display : none}
			.divl {display : none; white-space : nowrap}
		</style>
		<h3>Display</h3>
		<label>Average: </label>
		<input type="radio" name="show" value="a" checked="true"
			onclick="updateTable()" />
		<label> SD: </label>
		<input type="radio" name="show" value="s" onclick="updateTable()" />
		<label> Last: </label>
		<input type="radio" name="show" value="l" onclick="updateTable()" />
		<h3>Warming</h3>
		<table>
			<xsl:for-each select="warming/i">
				<xsl:variable name="line">
					<xsl:value-of select="position() - 1" />
				</xsl:variable>
				<xsl:variable name="linesize">
					<xsl:value-of select="count(os/o)" />
				</xsl:variable>
				<xsl:if test="position() = 1">
					<tr>
						<td>
							<input type="hidden" name="aggwcols">
								<xsl:attribute name="value"><xsl:value-of
									select="$linesize" /></xsl:attribute>
							</input>
							<input type="hidden" name="aggwminin">
								<xsl:attribute name="value"><xsl:value-of
									select="t" /></xsl:attribute>
							</input>
							<input type="hidden" name="aggwminout">
								<xsl:attribute name="value"><xsl:value-of
									select="os/o[1]/t" /></xsl:attribute>
							</input>
						</td>
						<xsl:for-each select="os/o">
							<td>
								<xsl:value-of select="t" />
							</td>
						</xsl:for-each>
					</tr>
				</xsl:if>
				<tr>
					<td>
						<xsl:value-of select="t" />
					</td>
					<xsl:for-each select="os/o">
						<td>
							<div class="diva">
								<xsl:if test="$readonly = 1">
									<xsl:value-of select="a" />
								</xsl:if>
								<xsl:if test="$readonly = 0">
									<input type="text">
										<xsl:attribute name="oninput">keyHandler('_aggwtemp<xsl:value-of
											select="$line * $linesize + position()" />')</xsl:attribute>
										<xsl:attribute name="name">_aggwtemp<xsl:value-of
											select="$line * $linesize + position()" /></xsl:attribute>
										<xsl:attribute name="id">_aggwtemp<xsl:value-of
											select="$line * $linesize + position()" /></xsl:attribute>
										<xsl:attribute name="value"><xsl:value-of
											select="a" />
									</xsl:attribute>
									</input>
								</xsl:if>
							</div>
							<div class="divs">
								<xsl:value-of select="s" />
							</div>
							<div class="divl">
								<xsl:value-of select="l" />
							</div>
						</td>
					</xsl:for-each>
				</tr>
			</xsl:for-each>
		</table>
		<h3>Cooling</h3>
		<table>
			<xsl:for-each select="cooling/i">
				<xsl:variable name="line">
					<xsl:value-of select="position() - 1" />
				</xsl:variable>
				<xsl:variable name="linesize">
					<xsl:value-of select="count(os/o)" />
				</xsl:variable>
				<xsl:if test="position() = 1">
					<tr>
						<td>
							<input type="hidden" name="aggccols">
								<xsl:attribute name="value"><xsl:value-of
									select="$linesize" /></xsl:attribute>
							</input>
							<input type="hidden" name="aggcminin">
								<xsl:attribute name="value"><xsl:value-of
									select="t" /></xsl:attribute>
							</input>
							<input type="hidden" name="aggcminout">
								<xsl:attribute name="value"><xsl:value-of
									select="os/o[1]/t" /></xsl:attribute>
							</input>
						</td>
						<xsl:for-each select="os/o">
							<td>
								<xsl:value-of select="t" />
							</td>
						</xsl:for-each>
					</tr>
				</xsl:if>
				<tr>
					<td>
						<xsl:value-of select="t" />
					</td>
					<xsl:for-each select="os/o">
						<td>
							<div class="diva">
								<xsl:if test="$readonly = 1">
									<xsl:value-of select="a" />
								</xsl:if>
								<xsl:if test="$readonly = 0">
									<input type="text">
										<xsl:attribute name="oninput">keyHandler('_aggctemp<xsl:value-of
											select="$line * $linesize + position()" />')</xsl:attribute>
										<xsl:attribute name="name">_aggctemp<xsl:value-of
											select="$line * $linesize + position()" /></xsl:attribute>
										<xsl:attribute name="id">_aggctemp<xsl:value-of
											select="$line * $linesize + position()" /></xsl:attribute>
										<xsl:attribute name="value"><xsl:value-of
											select="a" />
									</xsl:attribute>
									</input>
								</xsl:if>
							</div>
							<div class="divs">
								<xsl:value-of select="s" />
							</div>
							<div class="divl">
								<xsl:value-of select="l" />
							</div>
						</td>
					</xsl:for-each>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:key name="times" match="time" use="." />
	<xsl:template match="weather[@mode='default']">
		<h2>Weather</h2>
		<form method="post">
			<xsl:text>Retrieve:</xsl:text>
			<select name="fromweb">
				<option value="false">From Database</option>
				<option value="true">From Met Office</option>
			</select>
			<input type="submit" value="Retrieve" />
		</form>
		<table>
			<tr>
				<td>Day</td>
				<xsl:for-each
					select="fcs/fc/time[generate-id() = generate-id(key('times',.)[1])]">
					<xsl:sort data-type="number" />
					<td>
						<xsl:value-of select="." />
					</td>
				</xsl:for-each>
			</tr>
			<xsl:call-template name="day">
				<xsl:with-param name="day">
					<xsl:number value="1" />
				</xsl:with-param>
			</xsl:call-template>
		</table>
	</xsl:template>

	<xsl:template name="day">
		<xsl:param name="day" />
		<xsl:if test="$day &lt; 8">
			<tr>
				<td>
					<xsl:choose>
						<xsl:when test="$day = 1">
							Sunday
						</xsl:when>
						<xsl:when test="$day = 2">
							Monday
						</xsl:when>
						<xsl:when test="$day = 3">
							Tuesday
						</xsl:when>
						<xsl:when test="$day = 4">
							Wednesday
						</xsl:when>
						<xsl:when test="$day = 5">
							Thursday
						</xsl:when>
						<xsl:when test="$day = 6">
							Friday
						</xsl:when>
						<xsl:when test="$day = 7">
							Saturday
						</xsl:when>
					</xsl:choose>
				</td>
				<xsl:for-each
					select="fcs/fc/time[generate-id() = generate-id(key('times',.)[1])]">
					<xsl:sort data-type="number" />
					<xsl:variable name="cur" select="." />
					<td>
						<xsl:value-of select="../../../fcs[day=$day]/fc[time=$cur]/temp" />
					</td>
				</xsl:for-each>
			</tr>
			<xsl:call-template name="day">
				<xsl:with-param name="day">
					<xsl:number value="number($day) + 1" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>