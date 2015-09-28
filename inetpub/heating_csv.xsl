<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text" encoding="ISO-8859-1" />

	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="/heating/zones"></xsl:template>

	<xsl:template match="/heating/report">
		<xsl:text>n,t</xsl:text>
		<xsl:for-each select="/heating/zones/zone">
			<xsl:if test="type = 'Vents'">
				<xsl:text>,</xsl:text>
				<xsl:value-of select="description" />
				<xsl:text> Occ</xsl:text>
			</xsl:if>
			<xsl:if test="type != 'Vents'">
				<xsl:text>,</xsl:text>
				<xsl:if test="type = 'Zone'">
					<xsl:value-of select="description" />
					<xsl:text> Temp</xsl:text>
				</xsl:if>
				<xsl:if test="type = 'Controller'">
					<xsl:text>Outside Temp</xsl:text>
				</xsl:if>
				<xsl:text>,</xsl:text>
				<xsl:value-of select="description" />
				<xsl:text> Flow</xsl:text>
				<xsl:if test="type = 'Zone'">
					<xsl:text>,</xsl:text>
					<xsl:value-of select="description" />
					<xsl:text> Rqd</xsl:text>
					<xsl:text>,</xsl:text>
					<xsl:value-of select="description" />
					<xsl:text> Set</xsl:text>
					<xsl:text>,</xsl:text>
					<xsl:value-of select="description" />
					<xsl:text> Occ</xsl:text>
					<xsl:text>,</xsl:text>
					<xsl:value-of select="description" />
					<xsl:text> OptOn</xsl:text>
					<xsl:text>,</xsl:text>
					<xsl:value-of select="description" />
					<xsl:text> OptOff</xsl:text>
					<xsl:text>,</xsl:text>
					<xsl:value-of select="description" />
					<xsl:text> Valve</xsl:text>
				</xsl:if>
				<xsl:if test="type = 'Controller'">
					<xsl:for-each select="cascades/cascade">
						<xsl:text>,</xsl:text>
						<xsl:value-of select="name" />
					</xsl:for-each>
				</xsl:if>
			</xsl:if>
		</xsl:for-each>
		<xsl:text>&#13;&#10;</xsl:text>
		<xsl:for-each select="/heating/report/data">
			<xsl:variable name="cur" select="." />
			<xsl:variable name="pos" select="position()" />
			<xsl:value-of select="$pos" />
			<xsl:text>,</xsl:text>
			<xsl:value-of select="polltime/timestamp" />
			<xsl:for-each select="/heating/zones/zone">
				<xsl:variable name="zonename" select="name" />
				<xsl:if test="type = 'Vents'">
					<xsl:choose>
						<xsl:when
							test="$cur/occupied[zone = $zonename]">
							<xsl:text>,</xsl:text>
							<xsl:value-of
								select="$cur/occupied[zone = $zonename]/value" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>,-</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="type != 'Vents'">
					<xsl:choose>
						<xsl:when
							test="$cur/temperature[zone = $zonename]/value">
							<xsl:text>,</xsl:text>
							<xsl:value-of
								select="$cur/temperature[zone = $zonename]/value" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>,-</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when
							test="$cur/flow[zone = $zonename]/value">
							<xsl:text>,</xsl:text>
							<xsl:value-of
								select="$cur/flow[zone = $zonename]/value" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>,-</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:if test="type='Zone'">
						<xsl:choose>
							<xsl:when
								test="$cur/actual[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/actual[zone = $zonename]/value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>,-</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when
								test="$cur/required[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/required[zone = $zonename]/value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>,-</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when
								test="$cur/occupied[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/occupied[zone = $zonename]/value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>,-</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when
								test="$cur/optoff[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/optoff[zone = $zonename]/value" />
							</xsl:when>
							<xsl:when
								test="$cur/optimumon[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/optimumon[zone = $zonename]/value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>,-</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when
								test="$cur/opton[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/opton[zone = $zonename]/value" />
							</xsl:when>
							<xsl:when
								test="$cur/optimumoff[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/optimumoff[zone = $zonename]/value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>,-</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when
								test="$cur/valve[zone = $zonename]">
								<xsl:text>,</xsl:text>
								<xsl:value-of
									select="$cur/valve[zone = $zonename]/value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>,-</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
					<xsl:if test="type='Controller'">
						<xsl:for-each select="cascades/cascade">
							<xsl:variable name="cascadename"
								select="name" />
							<xsl:choose>
								<xsl:when
									test="$cur/boiler[zone = $zonename]/state[cascade = $cascadename]/value">
									<xsl:text>,</xsl:text>
									<xsl:value-of
										select="$cur/boiler[zone = $zonename]/state[cascade = $cascadename]/value" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>,-</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
			<xsl:text>&#13;&#10;</xsl:text>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>