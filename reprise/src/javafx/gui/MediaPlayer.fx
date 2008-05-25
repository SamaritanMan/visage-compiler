/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */ 

package javafx.gui;
import javafx.lang.Duration;

import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.AudioControl;
import java.net.URI;

import java.lang.System;

/**
 * The {@code MediaPlayer} class provides the controls for playing media.
 * It is used in combination with the {@code Media} and {@code MediaViewer}
 * classes to display and control media playing.
 * @profile common
 * @see Media MediaViewer
 */
public class MediaPlayer {
    attribute mediaProvider:MediaProvider = new MediaProvider();
    private attribute view:MediaView;
    // FIXME: multiple views
    
   /**
    * Defines the source {@code Media} to be played
    * @see Media
    * @profile common
    */
    public attribute media:Media on replace {
        mediaProvider.setSource(new URI(media.source));
        view.setComponent();
        startTime.millis = mediaProvider.getStartTime()/1000.0;
        
        stopTime.millis = mediaProvider.getStopTime()/1000.0;
        if (autoPlay) {
            play();
        }
        
   
    }
    
   /**
    * If {@code autoPlay} is {@code true}, playing will start as soon
    * as possible
    *
    * @profile common
    */
    public attribute autoPlay:Boolean on replace {
        if (autoPlay) {
            play();
        }
    }

    
   /**
    * Starts or resumes playing
    */
    public
    function play() {
        mediaProvider.play();
        paused = false;
    }
    
   /**
    * Pauses playing
    */
   public
   function pause() {
       mediaProvider.pause();
       paused = true;
   }

   /**
    * Indicated if the player has been paused, either programatically,
    * by the user, or because the media has finished playing
    */
   public attribute paused:Boolean;

   /**
    * Defines the rate at which the media is being played.
    * Rate {@code 1.0} is normal play, {@code 2.0} is 2 time normal,
    * {@code -1.0} is backwards, etc...
    */
   public attribute rate:Number on replace {
       mediaProvider.setRate(rate);
   }

   /**
    * Defines the volume at which the media is being played.
    * {@code 1.0} is full volume, which is the default.
    */
   public attribute volume:Number = 1.0 on replace {
       var ac : AudioControl;
       if ((ac = mediaProvider.getControl(ac.getClass())) <> null) {
           ac.setVolume(volume.floatValue());
       }
   }
   /**
    * Defines the balance, or left right setting,  of the audio output.
    * Value ranges continuously from {@code -1.0} being left,
    * {@code 0} being center, and {@code 1.0} being right.
    */
   public attribute balance:Number=0 on replace {
       ;
   }

   /**
    * The fader, or forward and back setting, of audio output
    * on 4+ channel output.
    * value ranges continuously from {@code -1.0} being rear,
    * {@code 0} being center, and {@code 1.0} being forward.
    */
   public attribute fader:Number on replace {
       ;
   }

   private static attribute  DURATION_UNKNOWN:Duration = Duration{millis:-1};

   /**
    * Defines the time offset where media should start playing,
    * or restart from when repeating
    */
   public attribute startTime:Duration on replace {
       mediaProvider.setStartTime(1000.0*(startTime.millis));
   }
   /**
    * Defines the time offset where media should stop playing
    * or restart when repeating
    */
   public attribute stopTime:Duration = DURATION_UNKNOWN on replace {
       if (stopTime == DURATION_UNKNOWN) {
            // do nothing for now, 
            // mediaProvider.setStopTime(java.lang.Double.POSITIVE_INFINITY);
       } else {
            mediaProvider.setStopTime(1000*stopTime.millis);
        }
   }
   
   /**
    * Defines the current media time
    */
   public attribute currentTime:Duration on replace {
       mediaProvider.setMediaTime(1000 * currentTime.millis);
   }
   
    /**
    * Defines the media timers for this player
    */
   public attribute timers:MediaTimer[];
   
   /**
    * Defines the number of times the media should repeat.
    * @profile core
    */
   public attribute repeatCount: Number = 1 on replace {
      // not yet in MediaProvider
       // mediaProvider.setRepeatCount(repeatCount);
   }

   /**
    * Defines the current number of time the media has repeated
    * @profile core
    */
  public attribute currentCount:Number=0; // How many times have we repeated


  /**
   * Value of {@code repeatCount} for no repeating (play once)
   * @profile core
   */
   public static attribute REPEAT_NONE:Number = 1;

 /**
   * Value of {@code repeatCount} to repeat forever
   * @profile core
   */
   public static attribute REPEAT_FOREVER:Integer = -1;//infinity;// where is Number.infinity;
   
   /**
    * Equals {@code true} if the player's audio is muted, false otherwise.
    * @profile core
    * @see volume
    */
   public attribute mute: Boolean on replace {
       var ac : AudioControl;
       if ((ac = mediaProvider.getControl(ac.getClass())) <> null) {
           ac.setMute(mute);
       }
   }

    /**
     * Current status of player
     * @profile core
     */
    public static attribute status:Integer;

    /**
     * Status value when player is paused
     * @profile core
     */
    public static attribute PAUSED:Integer=0;

    /**
     * status value when player is playing
     * @profile core
     */
    public static attribute PLAYING:Integer=2;

    /**
     * Status value when player is buffering.
     * Buffering may occur when player is paused or playing
     * @profile core
     */
    public static attribute BUFFERING: Integer=3;

    /**
     * Status value when player is stalled.
     * {@code STALLED} occurs when media is being played, but
     * data is not being delivered fast enough to continue playing
     * @see onStalled
     * @profile core
     */
    public static  attribute STALLED: Integer=4; // occurs during play
   
    /**
     * The {@code onError} function is called when a {@code mediaError} occurs
     * on this player.
     * @profile common
     * @see MediaError
     */
    public attribute onError: function (e: MediaError):Void; // Error in Media


   /**
     * Invoked when the player reaches the end of media
     * @profile common
     */
    public attribute onEndOfMedia: function():Void; // Media has reached its end.

   /**
     * Invoked when the player reaches the end of media.
     * @profile common
     * @see repeatCount
     */
    public attribute onRepeat:function():Void; // Media has hit the end and is repeating
    
    /**
     * Invoked when the player is buffering data.
     * {@code timeRemaining} is an estimate of how much time it will take before
     * the {@code mediaPlayer} can play the media
     * @profile common
     */
    public attribute onBuffering:function(timeRemaining: Duration):Void;

    /**
     * Invoked when the player has stalled
     * because it was not receiving data fast enough to continue playing.
     * {@code timeRemaining} is an estimate of how much time it will take before
     * the {@code mediaPlayer} can continue playing.
     * @profile common
     */
    public attribute onStalled:function(timeRemaining: Duration):Void;
    
   /**
    * Indicates if this player can have multiple views
    * associated with it.
    * @see MediaView
    * @profile common
    */
    public attribute supportsMultiViews:Boolean;
   

    
   // this can fail, or effect other views
   function addView(view:MediaView) {
       this.view = view;
   }

}
