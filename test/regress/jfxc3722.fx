/*
 * JFXC-3722 : Compiled bind: Cannot find symbol: variable Group$anon282
 *
 * @test
 *
 */

class Owner {
  var name : String
}

class Purse {
  var owner : Owner;
}

var XdaName = "Donna";

var pX = bind Purse {
        owner: bind Owner { name: bind XdaName }
}
