package Micro.Squads

import Planning.UnitMatchers.Matcher

trait Quality extends Matcher {
  val counteredBy: Array[Quality] = Array.empty
  @inline def counterScaling: Double = 1.0
}