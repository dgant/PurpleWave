package Global.Information.Combat

import bwapi.Position

import scala.collection.mutable

class CombatGroup(
  val vanguard:Position,
  val units:mutable.HashSet[bwapi.Unit]) {
  
}
