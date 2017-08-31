package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Companies._
import Micro.Squads.Goals.{SquadChill, SquadGoal}
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel.EnrichedPixelCollection

import scala.collection.mutable.ArrayBuffer

class Squad(val client: Plan) {
  
  var goal: SquadGoal = new SquadChill
  
  var enemies   : Iterable[UnitInfo]            = Iterable.empty
  var recruits  : ArrayBuffer[FriendlyUnitInfo] = ArrayBuffer.empty
  
  val detectors     = new Detectors     (this)
  val transports    = new Transport     (this)
  val spotters      = new Spotters      (this)
  val repairers     = new Repairers     (this)
  val healers       = new Healers       (this)
  //Air-to-air
  //Air-to-ground
  val antiAir       = new AntiAir       (this)
  val antiGround    = new AntiGround    (this)
  val siege         = new Siege         (this)
  val splashAir     = new SplashAir     (this)
  val splashGround  = new SplashGround  (this)
  
  
  var needsDetectors    : Boolean = _
  var needsTransport    : Boolean = _
  var needsSpotters     : Boolean = _
  var needsRepairers    : Boolean = _
  var needsHealers      : Boolean = _
  var needsBuilders     : Boolean = _
  var needsAirToAir     : Boolean = _
  var needsAirToGround  : Boolean = _
  var needsAntiAir      : Boolean = _
  var needsAntiGround   : Boolean = _
  var needsSplashAir    : Boolean = _
  var needsSplashGround : Boolean = _
  var needsSiege        : Boolean = _
  
  def update() {
    if (recruits.nonEmpty) {
      goal.squad = this
      goal.updateUnits()
    }
  }
  
  def conscript(units: Iterable[FriendlyUnitInfo]) {
    recruits.clear()
    recruits ++= units
    With.squads.commission(this)
    updateNeeds()
  }
  
  def recruit(unit: FriendlyUnitInfo) {
    recruits += unit
    With.squads.addUnit(this, unit)
    updateNeeds()
  }
  
  def updateNeeds() {
    goal.squad = this
    goal.updateNeeds()
    
    def needs(goalRequires: () => Boolean) = goal.acceptsHelp && goalRequires()
    
    needsDetectors    = needs(() => goal.requiresDetectors)
    needsTransport    = needs(() => goal.requiresTransport)
    needsSpotters     = needs(() => goal.requiresSpotters)
    needsRepairers    = needs(() => goal.requiresRepairers)
    needsHealers      = needs(() => goal.requiresHealers)
    needsBuilders     = needs(() => goal.requiresBuilders)
    needsAirToAir     = needs(() => goal.requiresAirToAir)
    needsAirToGround  = needs(() => goal.requiresAirToAir)
    needsAntiAir      = needs(() => goal.requiresAntiAir)
    needsAntiGround   = needs(() => goal.requiresAntiGround)
    needsSplashAir    = needs(() => goal.requiresSplashAir)
    needsSplashGround = needs(() => goal.requiresSplashGround)
    needsSiege        = needs(() => goal.requiresSiege)
  }
  
  def centroid: Pixel = {
    if (recruits.isEmpty)
      With.geography.home.pixelCenter
    else
      recruits.map(_.pixelCenter).centroid
  }
}
