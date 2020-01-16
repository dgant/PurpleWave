package Micro.Squads

import Planning.UnitMatchers.UnitMatcher

trait Quality extends UnitMatcher {
  val counteredBy: Array[Quality] = Array.empty
  def counterScaling(input: Double): Double = input
}