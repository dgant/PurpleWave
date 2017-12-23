package Micro.Actions.Combat.Spells

import Lifecycle.With
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Nuke extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Terran.Ghost
  override protected def tech           : Tech      = Terran.NuclearStrike
  override protected def aoe            : Boolean   = true
  override protected def castRangeTiles : Int       = if (With.self.hasUpgrade(Terran.GhostVisionRange)) 10 else 8
  override protected def thresholdValue : Double    = Terran.NuclearMissile.subjectiveValue * 2.0
  
  override protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = {
    unit.cloaked &&
    With.units.ours.exists(unit => unit.is(Terran.NuclearMissile) && unit.complete)
  }
  override protected def valueTarget(target: UnitInfo): Double = {
    if (target.invincible)                              return 0.0
    if (target.is(Zerg.Larva))                          return 0.0
    if (target.is(Zerg.Egg))                            return 0.0
    
    val multiplierSpeed = if (target.moving) 0.5 else if (target.canMove) 0.75 else 1.0
    val multiplierOwner = if (target.isOurs) -4.0 else 1.0
    val output          = multiplierSpeed * multiplierOwner * target.subjectiveValue
    output
  }
  
  // TODO: Still uses the 3x3 default targeting of TargetAOE
}
