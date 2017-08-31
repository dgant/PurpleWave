package Micro.Squads.Goals

import Micro.Squads.Squad
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass

trait SquadGoal {
  
  var squad: Squad = _
  
  def updateUnits()
  def updateNeeds() {
    requiresDetectors     = shouldRequireDetectors
    requiresTransport     = shouldRequireTransport
    requiresSpotters      = shouldRequireSpotters
    requiresRepairers     = shouldRequireRepairers
    requiresHealers       = shouldRequireHealers
    requiresBuilders      = shouldRequireBuilders
    requiresAirToAir      = shouldRequireAirToAir
    requiresAirToGround   = shouldRequireAirToGround
    requiresAntiAir       = shouldRequireAntiAir
    requiresAirToGround   = shouldRequireAirToGround
    requiresSplashAir     = shouldRequireSplashAir
    requiresSplashGround  = shouldRequireSplashGround
    requiresSiege         = shouldRequireSiege
  }
  
  var requiresDetectors           : Boolean = false
  var requiresTransport           : Boolean = false
  var requiresSpotters            : Boolean = false
  var requiresRepairers           : Boolean = false
  var requiresHealers             : Boolean = false
  var requiresBuilders            : Boolean = false
  var requiresAirToAir            : Boolean = false
  var requiresAirToGround         : Boolean = false
  var requiresAntiAir             : Boolean = false
  var requiresAntiGround          : Boolean = false
  var requiresSplashAir           : Boolean = false
  var requiresSplashGround        : Boolean = false
  var requiresSiege               : Boolean = false
  
  def acceptsHelp                 : Boolean = true
  def shouldRequireDetectors      : Boolean = squad.enemies.exists(e => e.cloaked || e.burrowed || e.is(Zerg.Lurker) || e.is(Terran.Ghost) || e.is(Terran.Wraith) || e.is(Protoss.Arbiter))
  def shouldRequireTransport      : Boolean = squad.recruits.exists(u => unitsNeedingTransport.contains(u.unitClass))
  def shouldRequireSpotters       : Boolean = squad.recruits.exists(_.unitClass.isSiegeTank)
  def shouldRequireRepairers      : Boolean = squad.recruits.exists(_.unitClass.isMechanical)
  def shouldRequireHealers        : Boolean = squad.recruits.exists(_.unitClass.isOrganic)
  def shouldRequireBuilders       : Boolean = false
  def shouldRequireAirToAir       : Boolean = squad.enemies.exists(   _.flying)
  def shouldRequireAirToGround    : Boolean = squad.enemies.exists( ! _.flying)
  def shouldRequireAntiAir        : Boolean = squad.enemies.exists(   _.flying)
  def shouldRequireAntiGround     : Boolean = squad.enemies.exists( ! _.flying)
  def shouldRequireSplashAir      : Boolean = squad.enemies.count(    _.flying) > 3
  def shouldRequireSplashGround   : Boolean = squad.enemies.count( !  _.flying) > 3
  def shouldRequireSiege          : Boolean = squad.enemies.exists(e => e.unitClass.isStaticDefense || e.unitClass.isSiegeTank)
  
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  private val unitsNeedingTransport = Vector[UnitClass](
    Protoss.DarkArchon,
    Protoss.HighTemplar,
    Protoss.Reaver,
    Zerg.Defiler
  )
}
