/*
 * PlanetarySystemShape.fx
 *
 * Created on Jan 23, 2008, 2:48:48 PM
 */

package jfx.assortis.samples.physics.planets;


import javafx.ui.*;
import javafx.ui.canvas.*;

import java.lang.Math;
                                                                                                                                              

public class PlanetarySystemShape extends CompositeNode{
    attribute planetarySystem: PlanetarySystem;
    attribute scale: Scale;

function composeNode():Node{
    return Group{
        content: [
         Group{
            content: for(i in  [1..15])
            Star{
                cx: Math.random() * 300 - 150
                cy: Math.random() * 300 - 150
                points: 8
                startAngle: 0
                rin: 1
                rout: 3
                fill: Color.WHITE
            }
         }, 
          Group{
            content: for(planet in planetarySystem.planets)[
            Circle{
                cx : bind planet.coordinate[0] * scale.coordinateScale
                cy: bind planet.coordinate[1] * scale.coordinateScale
                radius: planet.radius * scale.radiusScale
                fill: planet.color
            }
            
            ]
        }]
        
    };
}

}
