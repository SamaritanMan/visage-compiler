<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    
    <xsl:variable name="use-toc-tables">true</xsl:variable>
    <xsl:param name="master-css">master.css</xsl:param>
    <xsl:param name="extra-css"/>
    <xsl:param name="extra-js"/>
    <xsl:param name="extra-js2"/> <!-- josh: this is a hack -->
    <xsl:param name="target-class">javafx.ui.ToggleButton</xsl:param>
    
    
<!-- starter template -->    
    <xsl:template match="/">
        
        <xsl:if test="not (/classList)">
            <xsl:apply-templates select="//class[@qualifiedName=$target-class]"/>
            <!--
            <xsl:apply-templates select="//abstractClass[@qualifiedName=$target-class]"/>
            <xsl:apply-templates select="//interface[@qualifiedName=$target-class]"/>
            -->
        </xsl:if>
        
        <xsl:apply-templates select="/classList"/>
        <xsl:apply-templates select="/packageList"/>
        
    </xsl:template>
    
    
<!-- ====================== -->    
<!-- indexes and overviews -->
<!-- ====================== -->    
    <xsl:template match="packageList[@mode='overview-frame']">
        <html>
            <head>
                <link href="{$master-css}" rel="stylesheet"/>
                <xsl:if test="$extra-css">
                    <link href="{$extra-css}" rel="stylesheet"/>
                </xsl:if>
                <xsl:if test="$extra-js">
                    <script src="{$extra-js}"></script>
                </xsl:if>
            </head>
            <body>
                <ul id="packageList">
                    <xsl:for-each select="package">
                        <li>
                            <a target='classListFrame'>
                                <xsl:attribute name="href"><xsl:value-of select="@name"/>/package-frame.html</xsl:attribute>
                                <xsl:value-of select="@name"/>
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="packageList[@mode='overview-summary']">
        <html>
            <head>
                <link href="{$master-css}" rel="stylesheet"/>
                <xsl:if test="$extra-css">
                    <link href="{$extra-css}" rel="stylesheet"/>
                </xsl:if>
                <xsl:if test="$extra-js">
                    <script src="{$extra-js}"></script>
                </xsl:if>
            </head>
            <body>
                <h3>this is the summary</h3>
                <table>
                    <tr><th>This is a summary</th></tr>
                    <xsl:for-each select="package">
                        <tr>
                            <td>
                                <a target='classFrame'>
                                    <xsl:attribute name="href"><xsl:value-of select="@name"/>/package-summary.html</xsl:attribute>
                                    <xsl:value-of select="@name"/>
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="first-line-comment"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
    
    
    
    
    <xsl:template match="classList[@mode='overview-frame']">
        <html>
            <head>
                <link href="../{$master-css}" rel="stylesheet"/>
                <xsl:if test="$extra-css">
                    <link href="../{$extra-css}" rel="stylesheet"/>
                </xsl:if>
                <xsl:if test="$extra-js">
                    <script src="../{$extra-js}"></script>
                </xsl:if>
            </head>
            <body>
                <p><b>
                    <a href="package-summary.html" target="classFrame"><xsl:value-of select="@packageName"/></a>
                </b></p>
                <ul id="classList">
                    <xsl:for-each select="class">
                        <li>
                            <a target='classFrame'>
                                <xsl:attribute name="href"><xsl:value-of select="@qualifiedName"/>.html</xsl:attribute>
                                <xsl:value-of select="@name"/>
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </body>
        </html>
    </xsl:template>
    
    
    <xsl:template match="classList[@mode='overview-summary']">
        <html>
            <head>
                <link href="../{$master-css}" rel="stylesheet"/>
                <xsl:if test="$extra-css">
                    <link href="../{$extra-css}" rel="stylesheet"/>
                </xsl:if>
                <xsl:if test="$extra-js">
                    <script src="../{$extra-js}"></script>
                </xsl:if>
            </head>
            <body>
                <p><b><xsl:value-of select="@packageName"/></b></p>
                <table>
                    <tr><th>This is a summary</th></tr>
                    <xsl:for-each select="class">
                        <tr>
                            <td>
                                <a target='classFrame'>
                                    <xsl:attribute name="href"><xsl:value-of select="@qualifiedName"/>.html</xsl:attribute>
                                    <xsl:value-of select="@name"/>
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="first-line-comment"/>
                            </td>
                         </tr>
                     </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
    
    
    
    <xsl:template match="class">
        <xsl:call-template name="classOutput"/>
    </xsl:template>
    <xsl:template match="abstractClass">
        <xsl:call-template name="classOutput"/>
    </xsl:template>
    <xsl:template match="interface">
        <xsl:call-template name="classOutput"/>
    </xsl:template>

    
    
    
    
    
    
<!-- ====================== -->    
<!-- the actual class -->
<!-- ====================== -->    
    
    
    <xsl:template name="classOutput">
        <html>
            <head>
                <link href="../{$master-css}" rel="stylesheet"/>
                <style type="text/css"></style>
                <xsl:if test="$extra-css"><link href="../{$extra-css}" rel="stylesheet"/></xsl:if>
                <xsl:if test="$extra-js"><script src="../{$extra-js}"></script></xsl:if>
                <xsl:if test="$extra-js2"><script src="../{$extra-js2}"></script></xsl:if>
                <script src="../navigation.js"></script>
            </head>
            <body>
                <xsl:call-template name="header"/>
                <div id="content">
                    <a id="overview"><h3>Overview</h3></a>
                    <div class="overview">
                        <xsl:apply-templates select="docComment/inlineTags"/>
                    </div>
                    <xsl:call-template name="toc"/>
                    <xsl:call-template name="inherited"/>
                    <xsl:call-template name="members"/>
                </div>
            </body>
        </html>
    </xsl:template>
    
    
    
    
    
    <!-- =========== comments =========== -->
    <xsl:template match="docComment/commentText">
        <p class="comment">
            <xsl:value-of select="." disable-output-escaping="yes"/>
        </p>
    </xsl:template>
    
    <xsl:template match="docComment/inlineTags">
        <p class="comment"><xsl:apply-templates/></p>
    </xsl:template>
    
    <xsl:template match="docComment/firstSentenceTags">
        <p class="comment"><xsl:apply-templates/></p>
    </xsl:template>
    
    <xsl:template match="Text"><xsl:value-of select="." disable-output-escaping="yes"/></xsl:template>
    <xsl:template match="see">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
            <xsl:value-of select="@label"/>
        </a>
    </xsl:template>
    
    <xsl:template match="code"><code><xsl:value-of select="." disable-output-escaping="yes"/></code></xsl:template>
    
    
    
    
    
    
    
    
<!-- ====================== -->    
<!--    header    -->
<!-- ====================== -->    
    <xsl:template name="header">
        <div id="nav">
            <!-- name of the class -->
            <h1 class="classname">
                <i class="modifiers">
                    <xsl:value-of select="modifiers/@text"/>
                </i>
                class
                <a class="classname">
                    <strong><xsl:value-of select="@packageName"/>.</strong>
                    <b><xsl:value-of select="@name"/></b>
                </a>
            </h1>
            
            
            <!-- inheritance hierarchy -->
            <h2>
                <!--<xsl:variable name="blah" select="superclass/@qualifiedTypeName"/>-->
                <!--//class[@qualifiedName=$blah]"-->
                <xsl:apply-templates select="." mode="super"/>
                <xsl:apply-templates select="." mode="interface"/>
            </h2>
            
            <!-- navigation header -->
            <xsl:if test="@language='javafx'">
                <ul id="tabs">
                    <li><a href="#overview">overview</a></li><li><a href="#fields-summary">attributes</a></li><li><a href="#methods-summary">functions</a></li>
                </ul>
            </xsl:if>
            
            <xsl:if test="@language='java'">
                <ul id="tabs">
                    <li><a href="#overview">overview</a></li><li><a href="#fields-summary">fields</a></li><li><a href="#constructors-summary">constructors</a></li><li><a href="#methods-summary">methods</a></li>
                </ul>
            </xsl:if>
            
            
            <!-- view toggles -->
            <ul id="toggles">
                <li><a class="toggle-advanced" 
                       href="javascript:togglecss('.advanced','display','block','none');togglecss('.toggle-advanced','backgroundColor','transparent','red');">advanced</a></li>
            </ul>
            
        </div>
    </xsl:template>
    
    
    
    
    <xsl:template match="class[@language='javafx']" mode="interface">
        <xsl:for-each select="interfaces/interface">
            <a>
                <xsl:attribute name="title"><xsl:value-of select="@packageName"/>.<xsl:value-of select="@typeName"/></xsl:attribute>
                <xsl:attribute name="href">../<xsl:value-of select="@packageName"/>/<xsl:value-of select="@packageName"/>.<xsl:value-of select="@typeName"/>.html</xsl:attribute>
                <strong><xsl:value-of select="@packageName"/>.</strong>
                <b><xsl:value-of select="@typeName"/></b>
            </a>
            <xsl:text>, </xsl:text>
            <xsl:variable name="super"><xsl:value-of select="@packageName"/>.<xsl:value-of select="@typeName"/></xsl:variable>
            <xsl:apply-templates select="//class[@qualifiedName=$super]" mode="interface"/>
        </xsl:for-each>        
    </xsl:template>
    
    <xsl:template match="class" mode="interface">
        <xsl:if test="interfaces/interface">implements </xsl:if>
        <xsl:for-each select="interfaces/interface">
            <a>
                <xsl:attribute name="title"><xsl:value-of select="@packageName"/>.<xsl:value-of select="@typeName"/></xsl:attribute>
                <xsl:attribute name="href">../<xsl:value-of select="@packageName"/>/<xsl:value-of select="@packageName"/>.<xsl:value-of select="@typeName"/>.html</xsl:attribute>
                <strong><xsl:value-of select="@packageName"/>.</strong>
                <b><xsl:value-of select="@typeName"/></b>
            </a>
            <xsl:text>, </xsl:text>
        </xsl:for-each>
    </xsl:template>
    
    
    <xsl:template match="class" mode="super">        
        <!-- only do stuff if super exists at all -->
        <xsl:variable name="super" select="superclass/@qualifiedTypeName"/>
        <xsl:if test="$super">
            <!-- if super can't be found -->
            <xsl:if test="not(//class[@qualifiedName=$super])">
                <!-- be sure to skip java.lang.Object -->
                <xsl:if test="not($super='java.lang.Object')">
                    <a>
                        <xsl:attribute name="title"><xsl:value-of select="superclass/@packageName"/>.<xsl:value-of select="superclass/@typeName"/></xsl:attribute>
                        <strong><xsl:value-of select="superclass/@packageName"/>.</strong>
                        <b><xsl:value-of select="superclass/@typeName"/></b>
                    </a>
                </xsl:if>
            </xsl:if>

            <!-- if super can be found -->
            <xsl:apply-templates select="//class[@qualifiedName=$super]" mode="super"/>
            &gt;
            <a>
                <xsl:attribute name="title"><xsl:value-of select="@packageName"/>.<xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="href">../<xsl:value-of select="@packageName"/>/<xsl:value-of select="@packageName"/>.<xsl:value-of select="@name"/>.html</xsl:attribute>
                <strong><xsl:value-of select="@packageName"/>.</strong>
                <b><xsl:value-of select="@name"/></b>
            </a>
        
        </xsl:if>
        
    </xsl:template>
    

    
    
    
    
    
    
    
<!-- ====================== -->    
<!-- The Table of Contents  -->
<!-- ====================== -->    
    
    <xsl:template name="toc">
        <div id="toc">
            
            <xsl:if test="count(attribute) > 0">
                <a id="fields-summary"><h3>Attribute Summary</h3></a>
                <table>
                    <tr><th>name</th><th>type</th><th>description</th></tr>
                    <tr><th colspan="3">Public</th></tr>
                    <xsl:for-each select="attribute[modifiers/public]">
                        <xsl:sort select="@name" order="ascending"/>
                        <xsl:apply-templates select="." mode="toc"/>
                    </xsl:for-each>
                    <tr><th colspan="3">Protected</th></tr>
                    <xsl:for-each select="attribute[modifiers/protected]">
                        <xsl:sort select="@name" order="ascending"/>
                        <xsl:apply-templates select="." mode="toc"/>
                    </xsl:for-each>
                </table>
            </xsl:if>
            
            <xsl:if test="count(field) > 0">
                <a id="fields-summary"><h3>Field Summary</h3></a>
                <table class="fields">
                    <tr><th>public</th><th>name</th><th>type</th></tr>
                    <xsl:for-each select="field">
                        <xsl:sort select="@name" order="ascending"/>
                        <xsl:apply-templates select="." mode="toc"/>
                    </xsl:for-each>
                </table>
            </xsl:if>
            
            
            <!-- inherited attributes -->
            <xsl:variable name="blah" select="superclass/@qualifiedTypeName"/>
            <h3>Inherited Attributes</h3>
            <xsl:apply-templates select="//class[@qualifiedName=$blah]" mode="inherited-field"/>

            
            
            
            <!-- constructors -->
            
            <xsl:if test="count(constructor) > 0">
                <a id="constructors-summary"><h3>Constructor Summary</h3></a>
                <dl>
                    <xsl:for-each select="constructor">
                        <xsl:sort select="@name" order="ascending"/>
                        <xsl:call-template name="method-like-toc"/>
                    </xsl:for-each>
                </dl>
            </xsl:if>
            
            
            
            
            
            <!-- methods and functions -->
            
            <!-- functions -->
            <xsl:if test="count(function) > 0">
                <a id="methods-summary"><h3>Function Summary</h3></a>
                <dl>
                    <xsl:for-each select="function">
                        <xsl:sort select="@name" order="ascending"/>
                        <xsl:call-template name="method-like-toc"/>
                    </xsl:for-each>
                </dl>
            </xsl:if>
            
            
            <!-- methods -->
            <xsl:if test="count(method) > 0">
                <a id="methods-summary"><h3>Method Summary</h3></a>
                <dl>
                    <xsl:for-each select="method">
                        <xsl:sort select="@name" order="ascending"/>
                        <xsl:call-template name="method-like-toc"/>
                    </xsl:for-each>
                </dl>
            </xsl:if>
            
            <!-- inherited -->
            <h3>Inherited Functions</h3>
            <xsl:apply-templates select="//class[@qualifiedName=$blah]" mode="inherited-method"/>
        </div>
        
    </xsl:template>
    
    
    <!--  ==== Member details: attributes, fields, functions, methods === -->
    
    <xsl:template name="members">
        <xsl:if test="count(attribute) > 0">
            <div id="attributes">
                <h3>Attributes</h3>
                <xsl:for-each select="attribute">
                    <xsl:sort select="@name" order="ascending"/>
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </div>
        </xsl:if>
        
        <xsl:if test="count(field) > 0">
            <div id="fields">
                <h3>Fields</h3>
                <xsl:for-each select="field">
                    <xsl:sort select="@name" order="ascending"/>
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </div>
        </xsl:if>
        
        <xsl:if test="count(constructor) > 0">
            <div id="constructors">
                <h3>Constructors</h3>
                <xsl:for-each select="constructor">
                    <xsl:sort select="@name" order="ascending"/>
                    <xsl:call-template name="method-like"/>
                </xsl:for-each>
            </div>
        </xsl:if>
        
        <xsl:if test="count(function) > 0">
            <div id="functions">
                <h3>Functions</h3>
                <xsl:for-each select="function">
                    <xsl:sort select="@name" order="ascending"/>
                    <xsl:call-template name="method-like"/>
                </xsl:for-each>
            </div>
        </xsl:if>
        
        <xsl:if test="count(method) > 0">
            <div id="methods">
                <h3>Methods</h3>
                <xsl:for-each select="method">
                    <xsl:sort select="@name" order="ascending"/>
                    <xsl:call-template name="method-like"/>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template name="inherited">
    </xsl:template>
    
    <xsl:template match="class" mode="inherited-field">
        <xsl:if test="count(attribute) > 0">
            <h4><xsl:value-of select="@qualifiedName"/></h4>
            <ul class="inherited-field">
                <xsl:for-each select="attribute">
                    <xsl:sort select="@name" order="ascending"/>
                    <li><a>
                            <xsl:attribute name="href">
                                <xsl:text>../</xsl:text>
                                <xsl:value-of select="../@packageName"/>
                                <xsl:text>/</xsl:text>
                                <xsl:value-of select="../@packageName"/>
                                <xsl:text>.</xsl:text>
                                <xsl:value-of select="../@name"/>
                                <xsl:text>.html#attribute_</xsl:text>
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                            <xsl:value-of select="@name"/>
                    </a></li>
                </xsl:for-each>
            </ul>
        </xsl:if>
        <xsl:variable name="blah" select="superclass/@qualifiedTypeName"/>
        <xsl:apply-templates select="//class[@qualifiedName=$blah]" mode="inherited-field"/>
    </xsl:template>
    
    <xsl:template match="class" mode="inherited-method">
        <xsl:if test="count(function) > 0">
            <h4><xsl:value-of select="@qualifiedName"/></h4>
            <ul class="inherited-method">
                <xsl:for-each select="function">
                    <xsl:sort select="@name" order="ascending"/>
                    <li><a>
                            <xsl:attribute name="href">
                                <xsl:text>../</xsl:text>
                                <xsl:value-of select="../@packageName"/>
                                <xsl:text>/</xsl:text>
                                <xsl:value-of select="../@packageName"/>
                                <xsl:text>.</xsl:text>
                                <xsl:value-of select="../@name"/>
                                <xsl:text>.html#method_</xsl:text>
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                            <xsl:value-of select="@name"/>
                    </a></li>
                </xsl:for-each>
            </ul>
        </xsl:if>
        <xsl:variable name="blah" select="superclass/@qualifiedTypeName"/>
        <xsl:apply-templates select="//class[@qualifiedName=$blah]" mode="inherited-method"/>
    </xsl:template>
    
    
    
<!-- ====================== -->    
<!-- Attributes and Fields  -->
<!-- ====================== -->    
    
    <!-- summary line -->
    <xsl:template match="attribute" mode="toc">
        <tr class="attribute">
            <td>
                <a>
                    <xsl:attribute name="href">#<xsl:value-of select="@name"/></xsl:attribute>
                    <b class="name"><xsl:value-of select="@name"/></b>
                </a>
            </td>
            <td>
                <xsl:variable name="atype" select="type/@qualifiedTypeName"/>
                <a>
                    <xsl:if test="//class[@qualifiedName=$atype]">
                        <xsl:apply-templates select="type" mode="href"/>
                    </xsl:if>
                    <i class="type"><xsl:value-of select="type/@simpleTypeName"/>
                    <xsl:value-of select="type/@dimension"/></i>
                </a>
            </td>
            <td>
                <xsl:apply-templates select="docComment/firstSentenceTags"/>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="attribute/type" mode="href">
        <xsl:attribute name="href">
            <xsl:text>../</xsl:text>
            <xsl:value-of select="@packageName"/>
            <xsl:text>/</xsl:text>
            <xsl:value-of select="@qualifiedTypeName"/>
            <xsl:text>.html</xsl:text>
        </xsl:attribute>
        
    </xsl:template>
    
    <!-- full description -->
    <xsl:template match="attribute">
        <div class="attribute member">
            <a>
                <h4>
                    <xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
                    <i class="modifiers"><xsl:value-of select="modifiers/@text"/></i>
                    <xsl:text> </xsl:text>
                    <b class="name"><xsl:value-of select="@name"/></b>
                    <xsl:text>: </xsl:text>
                    <xsl:variable name="atype" select="type/@qualifiedTypeName"/>
                    <a>
                        <xsl:if test="//class[@qualifiedName=$atype]">
                            <xsl:apply-templates select="type" mode="href"/>
                        </xsl:if>
                        <i class="type"><xsl:value-of select="type/@simpleTypeName"/>
                        <xsl:value-of select="type/@dimension"/></i>
                    </a>
                </h4>
            </a>
            
            <xsl:apply-templates select="docComment/inlineTags"/>
        </div>
    </xsl:template>
    
    
    
    
    
    
<!-- ====================== -->    
<!--  Functions and Methods -->
<!-- ====================== -->    

    <!-- summary line -->
    <xsl:template name="method-like-toc">
        <dt>
            <xsl:if test="docComment/tags/advanced">
                <xsl:attribute name="class">advanced</xsl:attribute>
            </xsl:if>
             <xsl:apply-templates select="." mode="signature"/>
        </dt>
        <dd>
            <xsl:if test="docComment/tags/advanced">
                <xsl:attribute name="class">advanced</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="docComment/firstSentenceTags"/>
        </dd>
    </xsl:template>

    
    <!-- full description -->
    <xsl:template name="method-like">
        <div class="method member">
            <a>
                <xsl:attribute name="id"><xsl:apply-templates select="." mode="anchor-signature"/></xsl:attribute>
                <h4><xsl:apply-templates select="." mode="signature"/></h4>
            </a>
            
            
            <xsl:if test="parameters/parameter">
                <dl class="parameters">
                    Parameters
                    <xsl:for-each select="parameters/parameter">
                        <dt><xsl:value-of select="@name"/></dt>
                        <dd><xsl:apply-templates select="../../docComment/tags/param/inlineTags"/></dd>
                    </xsl:for-each>
                </dl>
            </xsl:if>
            
            <xsl:if test="not(returns/@simpleTypeName='void')">
                <dl class="returns">
                    Returns
                    <dt><xsl:value-of select="returns/@simpleTypeName"/>
                    <xsl:value-of select="returns/@dimension"/></dt>
                    <dd><xsl:apply-templates select="docComment/tags/return/inlineTags"/></dd>
                </dl>
            </xsl:if>
            
            <xsl:apply-templates select="docComment/inlineTags"/>
            
        </div>  
    </xsl:template>
    
    

    <!-- signature sub functions -->
    <xsl:template match="function | method | constructor" mode="anchor-signature">
        <xsl:value-of select="@name"/>
        <xsl:text>(</xsl:text>
        <xsl:for-each select="parameters/parameter">
            <xsl:value-of select="@name"/>:
            <xsl:value-of select="type/@toString"/>,
        </xsl:for-each>
        <xsl:text>)</xsl:text>
    </xsl:template>
    
    <xsl:template match="function | method | constructor" mode="signature">
        <i class="modifiers"><xsl:value-of select="modifiers/@text"/></i>
        <xsl:text> </xsl:text>
        
        <!-- fx -->
        <xsl:if test="not(../@language='java')">
            
            <a>
                <xsl:attribute name="href">#<xsl:apply-templates select="." mode="anchor-signature"/></xsl:attribute>
                <b><xsl:value-of select="@name"/></b>
            </a>
            (
            <i class="parameters">
                <xsl:for-each select="parameters/parameter">
                    <b><xsl:value-of select="@name"/></b>:
                    <!-- build parameter type link, if appropriate -->
                    <xsl:variable name="ptype" select="type/@qualifiedTypeName"/>
                    <a>
                        <xsl:if test="//class[@qualifiedName=$ptype]">
                           <xsl:attribute name="href">../<xsl:value-of select="type/@packageName"/>/<xsl:value-of select="type/@qualifiedTypeName"/>.html</xsl:attribute>
                        </xsl:if>
                        <i><xsl:value-of select="type/@typeName"/></i>
                    </a>,
                </xsl:for-each>
            </i>
            )
            :
            
            <!-- build return type link, if appropriate -->
            <a>
               <xsl:variable name="ptype" select="returns/@qualifiedTypeName"/>
                <xsl:if test="//class[@qualifiedName=$ptype]">
                   <xsl:attribute name="href">../<xsl:value-of select="returns/@packageName"/>/<xsl:value-of select="returns/@qualifiedTypeName"/>.html</xsl:attribute>
                </xsl:if>
               <i><xsl:value-of select="returns/@simpleTypeName"/>
               <xsl:value-of select="returns/@dimension"/></i>
            </a>
            <xsl:value-of select="type/@dimension"/>
        </xsl:if>
        
        <!-- java -->
        <xsl:if test="../@language='java'">
            <i><xsl:value-of select="returns/@simpleTypeName"/>
            <xsl:value-of select="returns/@dimension"/></i>
            <xsl:text> </xsl:text>
            <b><xsl:value-of select="@name"/></b>
            (
            <i class="parameters">
                <xsl:for-each select="parameters/parameter">
                    <i><xsl:value-of select="type/@toString"/></i>
                    <xsl:text> </xsl:text>
                    <b><xsl:value-of select="@name"/></b>,
                </xsl:for-each>
            </i>
            )
        </xsl:if>
            
    </xsl:template>
    

    
    
    
    
    <!--  Functions -->
    <!--    
    <xsl:template match="method" mode="toc">
        <div class="function">
            <li>
                <a>
                    <xsl:attribute name="href">#method_<xsl:value-of select="@name"/></xsl:attribute>
                    
                    <i class="modifiers"><xsl:value-of select="modifiers/@text"/></i>
                    <xsl:text> </xsl:text>
                    <b><xsl:value-of select="@name"/></b>
                    (
                    <i class="parameters">
                        <xsl:for-each select="parameters/parameter">
                            <b><xsl:value-of select="@name"/></b>:
                            <i><xsl:value-of select="type/@toString"/></i>,
                        </xsl:for-each>
                    </i>
                    )
                    :
                    <i><xsl:value-of select="returns/@simpleTypeName"/>
                    <xsl:value-of select="returns/@dimension"/>
                    <xsl:value-of select="type/@dimension"/></i>
                </a>
            </li>
        </div>  
    </xsl:template>
    
    <xsl:template match="method">
        <div class="function">
            <h4>
                <a>
                    <xsl:attribute name="id">method_<xsl:value-of select="@name"/></xsl:attribute>
                    <i class="modifiers"><xsl:value-of select="modifiers/@text"/></i>
                    <xsl:text> </xsl:text>
                    <b><xsl:value-of select="@name"/></b>
                    (
                    <i class="parameters">
                        <xsl:for-each select="parameters/parameter">
                            <i><xsl:value-of select="type/@toString"/></i>
                            <b><xsl:value-of select="@name"/></b>,
                        </xsl:for-each>
                    </i>
                    )
                    :
                    <i><xsl:value-of select="returns/@simpleTypeName"/>
                    <xsl:value-of select="type/@dimension"/></i>
                </a>
            </h4>
        </div>  
    </xsl:template>
    
                    -->
                </xsl:stylesheet>
                