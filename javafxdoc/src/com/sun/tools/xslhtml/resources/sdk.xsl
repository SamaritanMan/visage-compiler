<?xml version="1.0" encoding="UTF-8"?>

<!--
    Author     : joshua.marinacci@sun.com
    Description: customize the output with special doctags for use only
    by the JavaFX GUI project.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="sdk-overview">false</xsl:param>
    <xsl:import href="javadoc.xsl"/>
    
    <!-- duplicates from previous reprise custom.xsl -->
    <xsl:template match="attribute[docComment/tags/treatasprivate]" mode="toc"></xsl:template>
    <xsl:template match="attribute[docComment/tags/treatasprivate]" mode="toc"></xsl:template>
    

    <xsl:template name="extra-attribute">
        <xsl:if test="docComment/tags/treatasprivate">
            <xsl:text>private</xsl:text>
        </xsl:if>
    </xsl:template>
    <xsl:template name="extra-method">
        <xsl:if test="docComment/tags/treatasprivate">
            <xsl:text>private</xsl:text>
        </xsl:if>
    </xsl:template>
    <xsl:template name="extra-class">
        <xsl:if test="docComment/tags/treatasprivate">
            <xsl:text>private</xsl:text>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="extra-attribute-column-header">
        <th><a class="tooltip" title="Indicates the variable can be read">Can Read</a></th>
        <th><a title="Indicates the variable can only be set in the Object initializer. Any further changes will be ignored."
         class="tooltip">Can Init</a></th>
        <th><a title="Indicates the varible can set at any time."
         class="tooltip">Can Write</a></th>
        <th><a title="Indicates the default value of this variable"
         class="tooltip">Default Value</a></th>
    </xsl:template>
    
    <xsl:template name="extra-attribute-column-data">
        <td class="canread">
            <xsl:if test="modifiers/public"><img src="{$root-path}/images/JFX_highlight_dot.png"/></xsl:if>
            <xsl:if test="modifiers/public-init"><img src="{$root-path}/images/JFX_highlight_dot.png"/></xsl:if>
            <xsl:if test="modifiers/public-read"><img src="{$root-path}/images/JFX_highlight_dot.png"/></xsl:if>
            <xsl:if test="modifiers/protected">subclass</xsl:if>
        </td>
        <td class="caninit">
            <xsl:if test="modifiers/public"><img src="{$root-path}/images/JFX_highlight_dot.png"/></xsl:if>
            <xsl:if test="modifiers/public-init"><img src="{$root-path}/images/JFX_highlight_dot.png"/></xsl:if>
            <xsl:if test="modifiers/public-read"></xsl:if>
            <xsl:if test="modifiers/protected">subclass</xsl:if>
        </td>
        <td class="canwrite">
            <xsl:if test='modifiers/public'>
                <xsl:choose>
                    <xsl:when test="modifiers/read-only"></xsl:when>
                    <xsl:otherwise>
                        <img src="{$root-path}/images/JFX_highlight_dot.png"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
            
            <xsl:if test="modifiers/public-init"></xsl:if>
            <xsl:if test="modifiers/public-read"></xsl:if>
            <xsl:if test="modifiers/protected">subclass</xsl:if>
        </td>
        <td class="defaultvalue">
            <xsl:value-of select="docComment/tags/defaultvalue"/>
        </td>
    </xsl:template>
    
    <xsl:template name="attribute-table-width">6</xsl:template>

    <!-- new stuff -->
    <xsl:template match="seeTags">
        <p><b>See Also:</b><br/>
            <xsl:apply-templates select="see"/>
        </p>
    </xsl:template>
    
    <!-- turn off jumpdown links -->
    <xsl:template match="function | method | constructor" mode="toc-signature">
        <xsl:apply-templates select="modifiers"/>
        <xsl:text> </xsl:text>
        
        <!-- fx -->
        <xsl:if test="not(../@language='java')">
            <b><xsl:value-of select="@name"/></b>
            <xsl:apply-templates select="parameters" mode="signature"/>
            :
            <!-- build return type link, if appropriate -->
            <xsl:apply-templates select="returns" mode="signature"/>
            <xsl:value-of select="type/@dimension"/>
        </xsl:if>
        
        <!-- java -->
        <xsl:if test="../@language='java'">
            <xsl:apply-templates select="returns" mode="signature"/>
            <xsl:text> </xsl:text>
            <b><xsl:value-of select="@name"/></b>
            <xsl:apply-templates select="parameters" mode="signature"/>
        </xsl:if>
            
    </xsl:template>
    
    
    <xsl:template name="head-post">
        <link href="{$root-path}general.css" rel="stylesheet"/>
        <link href="{$root-path}sdk.css" rel="stylesheet"/>
        <script type="text/javascript" src="{$root-path}core.js"/>
        <script type="text/javascript" src="{$root-path}more.js"/>
        <script type="text/javascript" src="{$root-path}sessvars.js"/>

<!-- basic setup -->
<script>
function isdefined( variable)
{
    return (typeof(window[variable]) == "undefined")?  false: true;
}
function switchToDesktop() {
    $('select-desktop-profile').setStyle('background-color','white');
    $('select-common-profile').setStyle('background-color','black');
    $$('li.profile-desktop').setStyle('display','block');
    $$('li.profile-').setStyle('display','block');
    sessvars.currentProfile="desktop";
}
function switchToCommon() {
    $('select-desktop-profile').setStyle('background-color','black');
    $('select-common-profile').setStyle('background-color','white');
    $$('li.profile-desktop').setStyle('display','none');  
    $$('li.profile-').setStyle('display','none');  
    sessvars.currentProfile="common";
}

window.addEvent('domready', function(){


var sliders = $$('.long-desc').map(function(target) {
	//window.alert("found function");
	return new Fx.Slide(target, {
		duration: 'short'
	}).hide();
});

$$('.long-desc-open').each(function(lnk,index) {
    //window.alert("found desc short index " + sliders[index]);

    lnk.addEvent('click', function(e) { 
        //window.alert("in click");
        e = new Event(e);
        sliders[index].toggle();
        e.stop();
        var im = lnk.getElement('img');
        if(im.get('rel') == 'open') {
            im.set('src','../images/JFX_arrow_right.png').set('rel','close');
        } else {
            im.set('src','../images/JFX_arrow_down.png').set('rel','open');
        }
    });
});


//profile switchers
$('select-desktop-profile').addEvent('click', function(e) {
    switchToDesktop();
});
$('select-common-profile').addEvent('click', function(e) {
    switchToCommon();
});


//setup initial profile
if(sessvars.currentProfile == undefined) {
    sessvars.currentProfile = "desktop";
}
if(sessvars.currentProfile=="desktop") {
    switchToDesktop();
} else {
    switchToCommon();
}

//setup initial package
if(sessvars.currentPackageIndex == undefined) {
    sessvars.currentPackageIndex = 0;
}

var myAccordion2 = new Accordion($$('h4.header'), $$('ul.content'), {
	show: sessvars.currentPackageIndex,
	alwaysHide: true,
	opacity: false,
	duration: 'short'
});

$$('h4.header').each(function(lnk,index) {
	lnk.addEvent('click', function(e) { 
        sessvars.currentPackageIndex = index;
	});
});


var myTips = new Tips('.tooltip', {
    fixed: true
});

});</script>
    </xsl:template>
    
    
    <xsl:template name="header-pre">
        <div id="top-header">
        <h1><a href="{$root-path}index.html">Java<b>FX</b>: <i>Bringing Rich Experiences To All the Screens Of Your Life</i></a></h1>
        <h3 id="master-index-link"><a href="{$root-path}master-index.html">master index</a></h3>
        <h3 id="profile-switcher">Profile: <a href="#" id="select-desktop-profile">desktop</a>, <a href="#" id="select-common-profile">common</a></h3>
        </div>
        
    </xsl:template>
    
    
    <!-- new index / overview page -->
    <xsl:template match="/packageList[@mode='overview-summary']">
        <html>
            <head>
                <link href="{$root-path}{$master-css}" rel="stylesheet"/>
                <xsl:if test="$extra-css">
                    <link href="{$root-path}{$extra-css}" rel="stylesheet"/>
                </xsl:if>
                <xsl:if test="$extra-js">
                    <script src="{$root-path}{$extra-js}"></script>
                </xsl:if>
                <xsl:call-template name="head-post"/>
            </head>
            <body>
                <xsl:call-template name="header-pre"/>
                <ul id="classes-toc">
                    <xsl:for-each select="package">
                        <xsl:sort select="@name"/>
                        <li>
                            <h4 class='header'><a href="#"><xsl:value-of select="@name"/></a></h4>
                            <ul class='content'>
                                <xsl:for-each select="class">
                                    <li>
                                        <xsl:attribute name="class">
                                            <xsl:call-template name="profile-class"/>
                                            <xsl:call-template name="extra-class"/>
                                        </xsl:attribute>
                                        <a>
                                        <xsl:attribute name="href">
                                            <xsl:text></xsl:text>
                                            <xsl:value-of select="@packageName"/>
                                            <xsl:text>/</xsl:text>
                                            <xsl:value-of select="@qualifiedName"/>
                                            <xsl:text>.html</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="@name"/>
                                        </a></li>
                                </xsl:for-each>
                            </ul>
                        </li>
                    </xsl:for-each>
                </ul>

                <div id="content">
                    <h3><xsl:value-of select="$std.doctitle"/> Overview</h3>
                    
                    <xsl:if test="$sdk-overview='true'">
                        <p>The JavaFX <sup>tm</sup> Platform is a rich client platform for cross-screen rich internet applications (RIA) and content. It consists of common elements (2D graphics, Animation, Text and Media) and device specific elements for desktop, mobile and TV.  The JavaFX common set of APIs allow source level portability of the common set of functionalities across all platforms supported by JavaFX.

                        The JavaFX Runtimes targeted for different devices will ensure consistency and fidelity for content created based on the JavaFX Common APIs.

                        The JavaFX Common APIs will continue to evolve to match more powerful, common capabilities on the various device types.

                        </p>

                        <p><img src="platform_diagram.png"/></p>

                        <h3>What you can build with JavaFX:</h3>

                        <p><b>Cross Platform Applications:</b> If you want to develop a RIA across screens then you need to use JavaFX Common APIs only. The JavaFX Common APIs currently support 2D Graphics, Animation and Text across all platforms. In future, there will be support for audio, video, networking, local storage and other relevant components in JavaFX Common.</p>

                        <p><b>Desktop Applications:</b> If you are designing a desktop only application  ( Windows and Mac are currently supported) you can extend the functionality of the JavaFX applications by using APIs that are optimized for the desktop in addition to JavaFX Common. This will allow your application to adapt to a desktop look and feel with the JavaFX Swing extensions and also take advantage of Device Media Frameworks and advanced graphics support.</p>
                    </xsl:if>
                    <table class="package-docs">
                        <tr><th></th></tr>
                        <xsl:for-each select="package">
                            <xsl:sort select="@name"/>
                            <tr>
                                <td class="name">
                                    <b><!--
                                        <xsl:attribute name="href"><xsl:value-of select="@name"/>/package-summary.html</xsl:attribute>
                                        -->
                                        <xsl:value-of select="@name"/>
                                    </b>
                                </td>
                                <td class="description">
                                    <xsl:apply-templates select="docComment/firstSentenceTags"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </div>
            </body>
        </html>
    </xsl:template>    
</xsl:stylesheet>
