import java.lang.System;

/* Sample from Language Reference section on: "Querying sequences with foreach"
 * This had two or three minor problems (jfxc-835)"
 *   1. tracks should be declared as String[] not the old style String*
 *   2. 'foreach' needs to be changed to simply 'for'
 *   3. Title of section should probably not use 'foreach', at least not unquoted.
 *
 * @test
 * @run
 *
 */

class Album {
     var title: String;
     var artist: String;
     var tracks: String[];
}

var albums =
[Album {
     title: "A Hard Day's Night"
     artist: "The Beatles"
     tracks:
     ["A Hard Day's Night",
      "I Should Have Known Better",
      "If I Fell",
      "I'm Happy Just To Dance With You",
      "And I Love Her",
      "Tell Me Why",
      "Can't Buy Me Love",
      "Any Time At All",
      "I'll Cry Instead",
      "Things We Said Today",
      "When I Get Home",
       "You Can't Do That"]
     },
     Album {
          title: "Circle Of Love"
          artist: "Steve Miller Band"
          tracks:
          ["Heart Like A Wheel",
           "Get On Home",
           "Baby Wanna Dance",
           "Circle Of Love",
           "Macho City"]
     }];

     // Get the track numbers of the albums' title tracks

     var titleTracks =
          for (album in albums, track in album.tracks where track == album.title)
                       indexof track + 1;  // yields [1,4]
	if(titleTracks != [1,4] ) { 
		throw new java.lang.Exception("FAILED: querying with for-each");
   }else {
         for (album in albums, track in album.tracks where track == album.title)
         System.out.println("Album: {album.title}  titleTrack: {indexof track + 1}");  // yields [1,4]
   }

